import { useEffect, useState } from 'react';
import { fetchKontrahentUsers, updateUserAssignment, type KontrahentUser } from '../api/kontrahentUsers';
import { fetchContractorTypes, type ContractorType } from '../api/contractorTypes';

export function UserContractorTypesPage() {
  const [users, setUsers] = useState<KontrahentUser[]>([]);
  const [allTypes, setAllTypes] = useState<ContractorType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Track selected type per user: keycloakUserId -> typeId or null
  const [selections, setSelections] = useState<Map<string, number | null>>(new Map());
  const [saving, setSaving] = useState<Set<string>>(new Set());
  const [saveSuccess, setSaveSuccess] = useState<Set<string>>(new Set());

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [usersData, typesData] = await Promise.all([
        fetchKontrahentUsers(),
        fetchContractorTypes(),
      ]);
      setUsers(usersData);
      setAllTypes(typesData);

      const sels = new Map<string, number | null>();
      usersData.forEach(user => {
        sels.set(user.keycloakUserId, user.assignedType?.id ?? null);
      });
      setSelections(sels);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSelect = (userId: string, value: string) => {
    const typeId = value === '' ? null : Number(value);
    setSelections(prev => {
      const next = new Map(prev);
      next.set(userId, typeId);
      return next;
    });
    setSaveSuccess(prev => {
      const next = new Set(prev);
      next.delete(userId);
      return next;
    });
  };

  const hasChanges = (user: KontrahentUser): boolean => {
    const current = user.assignedType?.id ?? null;
    const selected = selections.get(user.keycloakUserId) ?? null;
    return current !== selected;
  };

  const handleSave = async (userId: string) => {
    const typeId = selections.get(userId) ?? null;
    setSaving(prev => new Set(prev).add(userId));
    setError(null);
    try {
      const updated = await updateUserAssignment(userId, typeId);
      setUsers(prev => prev.map(u => u.keycloakUserId === userId ? updated : u));
      setSaveSuccess(prev => new Set(prev).add(userId));
      setTimeout(() => {
        setSaveSuccess(prev => {
          const next = new Set(prev);
          next.delete(userId);
          return next;
        });
      }, 2000);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setSaving(prev => {
        const next = new Set(prev);
        next.delete(userId);
        return next;
      });
    }
  };

  if (loading) return <div className="loading">Ładowanie...</div>;

  return (
    <div>
      <h1>Przypisanie typów kontrahentów</h1>

      {error && <div className="alert alert-error">{error}</div>}

      <table className="table">
        <thead>
          <tr>
            <th>Użytkownik</th>
            <th>Email</th>
            <th>Typ kontrahenta</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => {
            const selectedTypeId = selections.get(user.keycloakUserId) ?? '';
            const isSaving = saving.has(user.keycloakUserId);
            const isSuccess = saveSuccess.has(user.keycloakUserId);
            const changed = hasChanges(user);

            return (
              <tr key={user.keycloakUserId}>
                <td>
                  {user.firstName} {user.lastName}
                  <div style={{ fontSize: '0.8rem', color: '#6b7280' }}>{user.username}</div>
                </td>
                <td>{user.email}</td>
                <td>
                  <select
                    className="select"
                    value={selectedTypeId === null ? '' : selectedTypeId}
                    onChange={e => handleSelect(user.keycloakUserId, e.target.value)}
                    disabled={isSaving}
                  >
                    <option value="">-- Brak --</option>
                    {allTypes.map(type => (
                      <option key={type.id} value={type.id}>
                        {type.symbol} — {type.name}
                      </option>
                    ))}
                  </select>
                </td>
                <td>
                  <button
                    className={`btn btn-sm ${isSuccess ? 'btn-success' : 'btn-primary'}`}
                    onClick={() => handleSave(user.keycloakUserId)}
                    disabled={isSaving || !changed}
                  >
                    {isSaving ? 'Zapisywanie...' : isSuccess ? 'Zapisano' : 'Zapisz'}
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
