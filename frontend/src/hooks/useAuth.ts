import keycloak from '../keycloak';

export function useAuth() {
  const tokenParsed = keycloak.tokenParsed;
  const roles: string[] = (tokenParsed?.realm_access?.roles as string[]) ?? [];

  return {
    isAdmin: roles.includes('Administrator'),
    isKontrahent: roles.includes('Kontrahent'),
    roles,
    username: (tokenParsed?.preferred_username as string) ?? '',
    email: (tokenParsed?.email as string) ?? '',
    token: keycloak.token,
    logout: () => keycloak.logout(),
    updateToken: () => keycloak.updateToken(30),
  };
}
