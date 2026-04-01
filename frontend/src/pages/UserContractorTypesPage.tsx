import { useEffect, useState } from 'react';
import {
  fetchKontrahentUsers,
  updateUserAssignments,
  updateAgreementNumber,
  type KontrahentUser,
} from '../api/kontrahentUsers';
import { fetchContractorTypes, type ContractorType } from '../api/contractorTypes';

export function UserContractorTypesPage() {
  const [users, setUsers] = useState<KontrahentUser[]>([]);
  const [allTypes, setAllTypes] = useState<ContractorType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [typeSelections, setTypeSelections] = useState<Map<string, Set<number>>>(new Map());
  const [agreementInputs, setAgreementInputs] = useState<Map<string, string>>(new Map());
  const [saving, setSaving] = useState<Set<string>>(new Set());
  const [saveSuccess, setSaveSuccess] = useState<Set<string>>(new Set());

  const [detailUser, setDetailUser] = useState<KontrahentUser | null>(null);

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

      const typeSels = new Map<string, Set<number>>();
      const agrInputs = new Map<string, string>();
      usersData.forEach(user => {
        typeSels.set(user.keycloakUserId, new Set(user.assignedTypes.map(t => t.id)));
        agrInputs.set(user.keycloakUserId, user.contractorData?.agreementNumber ?? '');
      });
      setTypeSelections(typeSels);
      setAgreementInputs(agrInputs);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleTypeToggle = (userId: string, typeId: number) => {
    setTypeSelections(prev => {
      const next = new Map(prev);
      const current = new Set(prev.get(userId) ?? []);
      if (current.has(typeId)) {
        current.delete(typeId);
      } else {
        current.add(typeId);
      }
      next.set(userId, current);
      return next;
    });
    clearSuccess(userId);
  };

  const handleAgreementChange = (userId: string, value: string) => {
    setAgreementInputs(prev => {
      const next = new Map(prev);
      next.set(userId, value);
      return next;
    });
    clearSuccess(userId);
  };

  const clearSuccess = (userId: string) => {
    setSaveSuccess(prev => {
      const next = new Set(prev);
      next.delete(userId);
      return next;
    });
  };

  const hasChanges = (user: KontrahentUser): boolean => {
    const currentTypeIds = new Set(user.assignedTypes.map(t => t.id));
    const selectedTypeIds = typeSelections.get(user.keycloakUserId) ?? new Set();
    const typesChanged = currentTypeIds.size !== selectedTypeIds.size ||
      [...currentTypeIds].some(id => !selectedTypeIds.has(id));

    const currentAgreement = user.contractorData?.agreementNumber ?? '';
    const inputAgreement = agreementInputs.get(user.keycloakUserId) ?? '';
    const agreementChanged = currentAgreement !== inputAgreement;

    return typesChanged || agreementChanged;
  };

  const handleSave = async (userId: string) => {
    const typeIds = [...(typeSelections.get(userId) ?? [])];
    const agreement = agreementInputs.get(userId) ?? '';
    setSaving(prev => new Set(prev).add(userId));
    setError(null);
    try {
      const [updated] = await Promise.all([
        updateUserAssignments(userId, typeIds),
        updateAgreementNumber(userId, agreement),
      ]);
      // Re-fetch to get consistent state
      const freshUsers = await fetchKontrahentUsers();
      setUsers(freshUsers);
      const freshUser = freshUsers.find(u => u.keycloakUserId === userId);
      if (freshUser) {
        setTypeSelections(prev => {
          const next = new Map(prev);
          next.set(userId, new Set(freshUser.assignedTypes.map(t => t.id)));
          return next;
        });
        setAgreementInputs(prev => {
          const next = new Map(prev);
          next.set(userId, freshUser.contractorData?.agreementNumber ?? '');
          return next;
        });
      }
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
            <th>Numer umowy</th>
            <th>Typy kontrahenta</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => {
            const selectedTypeIds = typeSelections.get(user.keycloakUserId) ?? new Set();
            const isSaving = saving.has(user.keycloakUserId);
            const isSuccess = saveSuccess.has(user.keycloakUserId);
            const changed = hasChanges(user);
            const agreementValue = agreementInputs.get(user.keycloakUserId) ?? '';

            return (
              <tr key={user.keycloakUserId}>
                <td>
                  <button
                    className="link-button"
                    onClick={() => setDetailUser(user)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--primary)',
                      cursor: 'pointer',
                      padding: 0,
                      font: 'inherit',
                      textDecoration: 'underline',
                      textAlign: 'left',
                    }}
                  >
                    {user.firstName} {user.lastName}
                  </button>
                  <div style={{ fontSize: '0.8rem', color: '#6b7280' }}>{user.username}</div>
                </td>
                <td>{user.email}</td>
                <td>
                  <input
                    type="text"
                    className="input"
                    value={agreementValue}
                    onChange={e => handleAgreementChange(user.keycloakUserId, e.target.value)}
                    disabled={isSaving}
                    placeholder="Numer umowy"
                    style={{ minWidth: '180px' }}
                  />
                </td>
                <td>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    {allTypes.map(type => (
                      <label key={type.id} style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}>
                        <input
                          type="checkbox"
                          checked={selectedTypeIds.has(type.id)}
                          onChange={() => handleTypeToggle(user.keycloakUserId, type.id)}
                          disabled={isSaving}
                        />
                        <span style={{ fontSize: '0.85rem' }}>{type.symbol} — {type.name}</span>
                      </label>
                    ))}
                  </div>
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

      {detailUser && (
        <div className="modal-overlay" onClick={() => setDetailUser(null)}>
          <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
            <h2>Dane kontrahenta</h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px 24px' }}>
              <div>
                <strong>Imię:</strong>
                <div>{detailUser.firstName || '—'}</div>
              </div>
              <div>
                <strong>Nazwisko:</strong>
                <div>{detailUser.lastName || '—'}</div>
              </div>
              <div>
                <strong>Email:</strong>
                <div>{detailUser.email || '—'}</div>
              </div>
              <div>
                <strong>Login:</strong>
                <div>{detailUser.username}</div>
              </div>
              <div>
                <strong>Skrót kontrahenta:</strong>
                <div>{detailUser.contractorData?.contractorAbbreviation || '—'}</div>
              </div>
              <div>
                <strong>Nazwa pełna kontrahenta:</strong>
                <div>{detailUser.contractorData?.contractorFullName || '—'}</div>
              </div>
              <div>
                <strong>Nazwa skrócona kontrahenta:</strong>
                <div>{detailUser.contractorData?.contractorShortName || '—'}</div>
              </div>
              <div>
                <strong>KRS:</strong>
                <div>{detailUser.contractorData?.krs || '—'}</div>
              </div>
              <div>
                <strong>NIP:</strong>
                <div>{detailUser.contractorData?.nip || '—'}</div>
              </div>
              <div>
                <strong>Adres siedziby:</strong>
                <div>{detailUser.contractorData?.registeredAddress || '—'}</div>
              </div>
              <div>
                <strong>Kod kontrahenta:</strong>
                <div>{detailUser.contractorData?.contractorCode || '—'}</div>
              </div>
              <div>
                <strong>Typy kontrahenta:</strong>
                <div>
                  {detailUser.assignedTypes.length > 0
                    ? detailUser.assignedTypes.map(t => t.symbol).join(', ')
                    : '—'}
                </div>
              </div>
              <div>
                <strong>Numer umowy:</strong>
                <div>{detailUser.contractorData?.agreementNumber || '—'}</div>
              </div>
              <div>
                <strong>Data umowy od:</strong>
                <div>{detailUser.contractorData?.agreementDateFrom || '—'}</div>
              </div>
              <div>
                <strong>Data umowy do:</strong>
                <div>{detailUser.contractorData?.agreementDateTo || '—'}</div>
              </div>
            </div>
            <div className="modal-actions">
              <button className="btn" onClick={() => setDetailUser(null)}>Zamknij</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
