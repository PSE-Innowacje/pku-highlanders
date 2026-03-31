import { useState } from 'react'
import keycloak from './keycloak'

function App() {
  const [apiResponse, setApiResponse] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const tokenParsed = keycloak.tokenParsed
  const roles = (tokenParsed?.realm_access?.roles ?? []).filter(
    (r: string) => r === 'Administrator' || r === 'Kontrahent'
  )

  const callApi = async () => {
    setLoading(true)
    try {
      await keycloak.updateToken(30)
      const res = await fetch('/api/me', {
        headers: { Authorization: `Bearer ${keycloak.token}` },
      })
      const data = await res.json()
      setApiResponse(JSON.stringify(data, null, 2))
    } catch (err) {
      setApiResponse(`Error: ${err}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
      <h1>PKU - System Oswiadczen</h1>

      <section style={{ marginBottom: '1.5rem' }}>
        <h2>User Info</h2>
        <p><strong>Username:</strong> {tokenParsed?.preferred_username}</p>
        <p><strong>Email:</strong> {tokenParsed?.email}</p>
        <p><strong>Roles:</strong> {roles.join(', ') || 'none'}</p>
      </section>

      <section style={{ marginBottom: '1.5rem' }}>
        <h2>API Test</h2>
        <button onClick={callApi} disabled={loading}>
          {loading ? 'Loading...' : 'Call /api/me'}
        </button>
        {apiResponse && (
          <pre style={{ background: '#f4f4f4', padding: '1rem', marginTop: '0.5rem' }}>
            {apiResponse}
          </pre>
        )}
      </section>

      <button onClick={() => keycloak.logout()}>Logout</button>
    </div>
  )
}

export default App
