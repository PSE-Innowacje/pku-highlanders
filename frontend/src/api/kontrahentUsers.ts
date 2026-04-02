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

export interface ContractorData {
  contractorAbbreviation: string;
  contractorFullName: string;
  contractorShortName: string;
  krs: string | null;
  nip: string | null;
  registeredAddress: string | null;
  contractorCode: string | null;
  agreementNumber: string | null;
  agreementDateFrom: string | null;
  agreementDateTo: string | null;
}

export interface KontrahentUser {
  keycloakUserId: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  assignedType: ContractorTypeRef | null;
  contractorData: ContractorData | null;
}

export interface CurrentUserContractorData {
  firstName: string;
  lastName: string;
  agreementNumber: string | null;
  contractorFullName: string | null;
  contractorAbbreviation: string | null;
  assignedType: ContractorTypeRef | null;
  contractorData: ContractorData | null;
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

export async function updateAgreementNumber(
  keycloakUserId: string,
  agreementNumber: string
): Promise<void> {
  const res = await fetch(`${BASE}/${keycloakUserId}/agreement-number`, {
    method: 'PUT',
    headers: await authHeaders(),
    body: JSON.stringify({ agreementNumber }),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
}

export async function fetchCurrentUserContractorData(): Promise<CurrentUserContractorData> {
  await keycloak.updateToken(30);
  const res = await fetch('/api/me/contractor-data', {
    headers: {
      Authorization: `Bearer ${keycloak.token}`,
    },
  });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}
