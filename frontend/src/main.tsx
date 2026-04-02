import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { installCryptoPolyfill } from './crypto-polyfill'
import App from './App.tsx'
import keycloak from './keycloak.ts'

// Required for keycloak-js on HTTP (no Web Crypto API). Remove when HTTPS is enabled.
installCryptoPolyfill();

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
