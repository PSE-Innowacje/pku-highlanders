import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import keycloak from './keycloak.ts'

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
