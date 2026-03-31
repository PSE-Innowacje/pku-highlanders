import keycloak from '../keycloak';

const BASE = '/api/admin/contractor-types';

async function authHeaders(): Promise<HeadersInit> {
  await keycloak.updateToken(30);
  return {
    Authorization: `Bearer ${keycloak.token}`,
    'Content-Type': 'application/json',
  };
}

export interface ContractorType {
  id: number;
  symbol: string;
  name: string;
  system: boolean;
}

export async function fetchContractorTypes(): Promise<ContractorType[]> {
  const res = await fetch(BASE, { headers: await authHeaders() });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}

export async function createContractorType(data: { symbol: string; name: string }): Promise<ContractorType> {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: await authHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
  return res.json();
}

export async function updateContractorType(id: number, data: { symbol: string; name: string }): Promise<ContractorType> {
  const res = await fetch(`${BASE}/${id}`, {
    method: 'PUT',
    headers: await authHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
  return res.json();
}

export async function deleteContractorType(id: number): Promise<void> {
  const res = await fetch(`${BASE}/${id}`, {
    method: 'DELETE',
    headers: await authHeaders(),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
}
