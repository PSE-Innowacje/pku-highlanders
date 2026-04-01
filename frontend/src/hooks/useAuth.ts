import keycloak from '../keycloak';

export function useAuth() {
  const tokenParsed = keycloak.tokenParsed;
  const roles: string[] = (tokenParsed?.realm_access?.roles as string[]) ?? [];

  const firstName = (tokenParsed?.given_name as string) ?? '';
  const lastName = (tokenParsed?.family_name as string) ?? '';

  return {
    isAdmin: roles.includes('Administrator'),
    isKontrahent: roles.includes('Kontrahent'),
    roles,
    username: (tokenParsed?.preferred_username as string) ?? '',
    email: (tokenParsed?.email as string) ?? '',
    firstName,
    lastName,
    displayName: firstName && lastName ? `${firstName} ${lastName}` : (tokenParsed?.preferred_username as string) ?? '',
    token: keycloak.token,
    logout: () => keycloak.logout(),
    updateToken: () => keycloak.updateToken(30),
  };
}
