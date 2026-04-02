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

export interface DeclarationField {
  position: string;
  fieldCode: string;
  dataType: string;
  fieldName: string;
  required: boolean;
  unit: string;
}

export interface DeclarationDetail {
  id: number;
  declarationNumber: string;
  status: string;
  statusLabel: string;
  declarationTypeCode: string;
  declarationTypeName: string;
  hasComment: boolean;
  createdAt: string;
  fieldValues: Record<string, string>;
  comment: string | null;
  fields: DeclarationField[];
}

export async function generateMyDeclarations(): Promise<Declaration[]> {
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

export async function fetchMyDeclarations(): Promise<Declaration[]> {
  const res = await fetch(BASE, { headers: await authHeaders() });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}

export async function fetchDeclarationDetail(id: number): Promise<DeclarationDetail> {
  const res = await fetch(`${BASE}/${id}`, { headers: await authHeaders() });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}

export async function saveDeclaration(
  id: number,
  fieldValues: Record<string, string>,
  comment: string | null
): Promise<DeclarationDetail> {
  const res = await fetch(`${BASE}/${id}`, {
    method: 'PUT',
    headers: await authHeaders(),
    body: JSON.stringify({ fieldValues, comment }),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
  return res.json();
}

export async function submitDeclaration(id: number): Promise<Record<string, unknown>> {
  const res = await fetch(`${BASE}/${id}/submit`, {
    method: 'POST',
    headers: await authHeaders(),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
  return res.json();
}

