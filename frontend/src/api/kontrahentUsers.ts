import keycloak from '../keycloak';

const BASE = '/api/admin/kontrahent-users';

async function authHeaders(): Promise<HeadersInit> {
  await keycloak.updateToken(30);
  return {
    Authorization: `Bearer ${keycloak.token}`,
    'Content-Type': 'application/json',
  };
}

export interface ContractorTypeRef {
  id: number;
  symbol: string;
  name: string;
  system: boolean;
}

export interface KontrahentUser {
  keycloakUserId: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  assignedType: ContractorTypeRef | null;
}

export async function fetchKontrahentUsers(): Promise<KontrahentUser[]> {
  const res = await fetch(BASE, { headers: await authHeaders() });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}

export async function updateUserAssignment(
  keycloakUserId: string,
  contractorTypeId: number | null
): Promise<KontrahentUser> {
  const res = await fetch(`${BASE}/${keycloakUserId}/contractor-type`, {
    method: 'PUT',
    headers: await authHeaders(),
    body: JSON.stringify({ contractorTypeId }),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
  return res.json();
}
