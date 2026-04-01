import { useEffect, useState } from 'react';
import {
  fetchKontrahentUsers,
  updateUserAssignment,
  updateAgreementNumber,
  type KontrahentUser,
} from '../api/kontrahentUsers';
import { fetchContractorTypes, type ContractorType } from '../api/contractorTypes';

export function UserContractorTypesPage() {
  const [users, setUsers] = useState<KontrahentUser[]>([]);
  const [allTypes, setAllTypes] = useState<ContractorType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [typeSelections, setTypeSelections] = useState<Map<string, number | null>>(new Map());
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

      const typeSels = new Map<string, number | null>();
      const agrInputs = new Map<string, string>();
      usersData.forEach(user => {
        typeSels.set(user.keycloakUserId, user.assignedType?.id ?? null);
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

  const handleTypeSelect = (userId: string, value: string) => {
    const typeId = value === '' ? null : Number(value);
    setTypeSelections(prev => {
      const next = new Map(prev);
      next.set(userId, typeId);
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
    const currentTypeId = user.assignedType?.id ?? null;
    const selectedTypeId = typeSelections.get(user.keycloakUserId) ?? null;
    const typeChanged = currentTypeId !== selectedTypeId;

    const currentAgreement = user.contractorData?.agreementNumber ?? '';
    const inputAgreement = agreementInputs.get(user.keycloakUserId) ?? '';
    const agreementChanged = currentAgreement !== inputAgreement;

    return typeChanged || agreementChanged;
  };

  const handleSave = async (userId: string) => {
    const typeId = typeSelections.get(userId) ?? null;
    const agreement = agreementInputs.get(userId) ?? '';
    setSaving(prev => new Set(prev).add(userId));
    setError(null);
    try {
      await Promise.all([
        updateUserAssignment(userId, typeId),
        updateAgreementNumber(userId, agreement),
      ]);
      const freshUsers = await fetchKontrahentUsers();
      setUsers(freshUsers);
      const freshUser = freshUsers.find(u => u.keycloakUserId === userId);
      if (freshUser) {
        setTypeSelections(prev => {
          const next = new Map(prev);
          next.set(userId, freshUser.assignedType?.id ?? null);
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
            <th>Typ kontrahenta</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => {
            const selectedTypeId = typeSelections.get(user.keycloakUserId) ?? '';
            const isSaving = saving.has(user.keycloakUserId);
            const isSuccess = saveSuccess.has(user.keycloakUserId);
            const changed = hasChanges(user);
            const agreementValue = agreementInputs.get(user.keycloakUserId) ?? '';

            return (
              <tr key={user.keycloakUserId}>
                <td>
                  <button
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
                  <select
                    className="select"
                    value={selectedTypeId === null ? '' : selectedTypeId}
                    onChange={e => handleTypeSelect(user.keycloakUserId, e.target.value)}
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
                <strong>Typ kontrahenta:</strong>
                <div>{detailUser.assignedType ? `${detailUser.assignedType.symbol} — ${detailUser.assignedType.name}` : '—'}</div>
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
