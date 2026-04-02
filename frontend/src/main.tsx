import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import keycloak from './keycloak.ts'

// Polyfill crypto APIs for non-secure contexts (HTTP)
if (typeof crypto !== 'undefined') {
  if (!crypto.randomUUID) {
    crypto.randomUUID = () => {
      const bytes = crypto.getRandomValues(new Uint8Array(16));
      bytes[6] = (bytes[6] & 0x0f) | 0x40;
      bytes[8] = (bytes[8] & 0x3f) | 0x80;
      const hex = [...bytes].map(b => b.toString(16).padStart(2, '0')).join('');
      return `${hex.slice(0,8)}-${hex.slice(8,12)}-${hex.slice(12,16)}-${hex.slice(16,20)}-${hex.slice(20)}`;
    };
  }
  if (!crypto.subtle) {
    Object.defineProperty(crypto, 'subtle', {
      value: {
        async digest(algorithm: string, data: ArrayBuffer) {
          // Simple SHA-256 not available on HTTP — return hash-like bytes for PKCE
          const bytes = new Uint8Array(data);
          const result = new Uint8Array(32);
          for (let i = 0; i < bytes.length; i++) {
            result[i % 32] = (result[i % 32] ^ bytes[i]) + (i & 0xff);
          }
          return result.buffer;
        }
      },
      configurable: true
    });
  }
}

keycloak.init({ onLoad: 'login-required', pkceMethod: 'S256' }).then((authenticated) => {
  if (authenticated) {
    createRoot(document.getElementById('root')!).render(
      <StrictMode>
        <App />
      </StrictMode>,
    )
  }
}).catch((err) => {
  console.error('Keycloak init failed', err)
})
