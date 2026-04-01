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

export interface ScheduleEntry {
  id: number | null;
  position: string;
  day: number;
  hour: number;
  dayType: string;
}

export async function fetchScheduleEntries(code: string): Promise<ScheduleEntry[]> {
  const res = await fetch(`${BASE}/${code}/schedule`, { headers: await authHeaders() });
  if (!res.ok) throw new Error(`Błąd pobierania: ${res.status}`);
  return res.json();
}

export async function saveScheduleEntries(code: string, entries: ScheduleEntry[]): Promise<ScheduleEntry[]> {
  const res = await fetch(`${BASE}/${code}/schedule`, {
    method: 'PUT',
    headers: await authHeaders(),
    body: JSON.stringify({ entries }),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
  return res.json();
}

export async function generateDeclarationsForSchedule(
  code: string,
  scheduleDay: number
): Promise<{ generated: number }> {
  const res = await fetch(`${BASE}/${code}/generate`, {
    method: 'POST',
    headers: await authHeaders(),
    body: JSON.stringify({ scheduleDay }),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.message || `Błąd: ${res.status}`);
  }
  return res.json();
}
