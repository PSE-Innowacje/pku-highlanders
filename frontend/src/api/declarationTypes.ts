import keycloak from '../keycloak';

const BASE = '/api/admin/declaration-types';

async function authHeaders(): Promise<HeadersInit> {
  await keycloak.updateToken(30);
  return {
    Authorization: `Bearer ${keycloak.token}`,
    'Content-Type': 'application/json',
  };
}

export interface DeclarationType {
  id: number;
  code: string;
  name: string;
  contractorTypes: string;
  hasComment: boolean;
  fieldCount: number;
}

export interface DeclarationTypeField {
  position: string;
  fieldCode: string;
  dataType: string;
  fieldName: string;
  required: boolean;
  unit: string;
}

export interface DeclarationTypeDetail {
  id: number;
  code: string;
  name: string;
  contractorTypes: string;
  hasComment: boolean;
  fields: DeclarationTypeField[];
}

export async function fetchDeclarationTypes(): Promise<DeclarationType[]> {
  const res = await fetch(BASE, { headers: await authHeaders() });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}

export async function fetchDeclarationTypeByCode(code: string): Promise<DeclarationTypeDetail> {
  const res = await fetch(`${BASE}/${code}`, { headers: await authHeaders() });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}
