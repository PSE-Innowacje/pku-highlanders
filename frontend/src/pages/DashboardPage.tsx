import { useAuth } from '../hooks/useAuth';

export function DashboardPage() {
  const { username, roles } = useAuth();

  const displayRoles = roles.filter(r => r === 'Administrator' || r === 'Kontrahent');

  return (
    <div>
      <h1>Strona główna</h1>
      <p>Witaj, <strong>{username}</strong></p>
      <p>Role: {displayRoles.join(', ') || 'brak'}</p>
    </div>
  );
}
