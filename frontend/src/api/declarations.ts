import keycloak from '../keycloak';

const BASE = '/api/declarations';

async function authHeaders(): Promise<HeadersInit> {
  await keycloak.updateToken(30);
  return {
    Authorization: `Bearer ${keycloak.token}`,
    'Content-Type': 'application/json',
  };
}

export interface Declaration {
  id: number;
  declarationNumber: string;
  status: string;
  statusLabel: string;
  declarationTypeCode: string;
  declarationTypeName: string;
  createdAt: string;
}

export async function fetchMyDeclarations(): Promise<Declaration[]> {
  const res = await fetch(BASE, { headers: await authHeaders() });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}

export async function generateDeclarations(): Promise<Declaration[]> {
  const res = await fetch(`${BASE}/generate`, {
    method: 'POST',
    headers: await authHeaders(),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
  return res.json();
}
