import { useEffect, useState } from 'react';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import Chip from '@mui/material/Chip';
import {
  fetchContractorTypes,
  createContractorType,
  updateContractorType,
  deleteContractorType,
  updateContractorTypeDeclarations,
  type ContractorType,
  type DeclarationTypeRef,
} from '../api/contractorTypes';
import { fetchDeclarationTypes, type DeclarationType } from '../api/declarationTypes';

export function ContractorTypesPage() {
  const [types, setTypes] = useState<ContractorType[]>([]);
  const [allDeclarationTypes, setAllDeclarationTypes] = useState<DeclarationType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formSymbol, setFormSymbol] = useState('');
  const [formName, setFormName] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  // Track declaration type assignments per contractor type
  const [assignments, setAssignments] = useState<Map<number, DeclarationTypeRef[]>>(new Map());
  const [saving, setSaving] = useState<Set<number>>(new Set());
  const [saveSuccess, setSaveSuccess] = useState<Set<number>>(new Set());

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [typesData, declData] = await Promise.all([
        fetchContractorTypes(),
        fetchDeclarationTypes(),
      ]);
      setTypes(typesData);
      setAllDeclarationTypes(declData);

      const asgn = new Map<number, DeclarationTypeRef[]>();
      typesData.forEach(ct => {
        asgn.set(ct.id, ct.declarationTypes ?? []);
      });
      setAssignments(asgn);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openAdd = () => {
    setEditingId(null);
    setFormSymbol('');
    setFormName('');
    setFormError(null);
    setShowForm(true);
  };

  const openEdit = (ct: ContractorType) => {
    setEditingId(ct.id);
    setFormSymbol(ct.symbol);
    setFormName(ct.name);
    setFormError(null);
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
    setFormError(null);
  };

  const handleSave = async () => {
    setFormError(null);
    try {
      if (editingId) {
        await updateContractorType(editingId, { symbol: formSymbol, name: formName });
      } else {
        await createContractorType({ symbol: formSymbol, name: formName });
      }
      closeForm();
      await loadData();
    } catch (e) {
      setFormError(e instanceof Error ? e.message : 'Nieznany błąd');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Czy na pewno chcesz usunąć ten typ kontrahenta?')) return;
    try {
      await deleteContractorType(id);
      await loadData();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    }
  };

  const handleAssignmentChange = (ctId: number, newValues: DeclarationTypeRef[]) => {
    setAssignments(prev => {
      const next = new Map(prev);
      next.set(ctId, newValues);
      return next;
    });
    setSaveSuccess(prev => {
      const next = new Set(prev);
      next.delete(ctId);
      return next;
    });
  };

  const hasAssignmentChanges = (ct: ContractorType): boolean => {
    const current = new Set((ct.declarationTypes ?? []).map(d => d.id));
    const edited = new Set((assignments.get(ct.id) ?? []).map(d => d.id));
    if (current.size !== edited.size) return true;
    for (const id of current) {
      if (!edited.has(id)) return true;
    }
    return false;
  };

  const handleSaveAssignments = async (ctId: number) => {
    const declIds = (assignments.get(ctId) ?? []).map(d => d.id);
    setSaving(prev => new Set(prev).add(ctId));
    setError(null);
    try {
      const updated = await updateContractorTypeDeclarations(ctId, declIds);
      setTypes(prev => prev.map(ct => ct.id === ctId ? updated : ct));
      setSaveSuccess(prev => new Set(prev).add(ctId));
      setTimeout(() => {
        setSaveSuccess(prev => {
          const next = new Set(prev);
          next.delete(ctId);
          return next;
        });
      }, 2000);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setSaving(prev => {
        const next = new Set(prev);
        next.delete(ctId);
        return next;
      });
    }
  };

  // Convert DeclarationType (from list API) to DeclarationTypeRef (for Autocomplete options)
  const declOptions: DeclarationTypeRef[] = allDeclarationTypes.map(d => ({
    id: d.id,
    code: d.code,
    name: d.name,
  }));

  if (loading) return <p>Ładowanie...</p>;

  return (
    <div>
      <div className="page-header">
        <h1>Typy kontrahentów</h1>
        <button className="btn btn-primary" onClick={openAdd}>Dodaj</button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <table className="table">
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Nazwa</th>
            <th>Typ systemowy</th>
            <th>Typy oświadczeń</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {types.map(ct => {
            const selectedDecls = assignments.get(ct.id) ?? [];
            const isSaving = saving.has(ct.id);
            const isSuccess = saveSuccess.has(ct.id);
            const changed = hasAssignmentChanges(ct);

            return (
              <tr key={ct.id} style={{ verticalAlign: 'top' }}>
                <td>{ct.symbol}</td>
                <td>{ct.name}</td>
                <td>{ct.system ? 'Tak' : 'Nie'}</td>
                <td style={{ minWidth: 350 }}>
                  <Autocomplete
                    multiple
                    size="small"
                    options={declOptions}
                    getOptionLabel={(o) => `${o.code} — ${o.name}`}
                    isOptionEqualToValue={(o, v) => o.id === v.id}
                    value={selectedDecls}
                    onChange={(_, newValue) => handleAssignmentChange(ct.id, newValue)}
                    disabled={isSaving}
                    renderTags={(value, getTagProps) =>
                      value.map((option, index) => {
                        const { key, ...rest } = getTagProps({ index });
                        return <Chip key={key} label={option.code} size="small" {...rest} />;
                      })
                    }
                    renderInput={(params) => (
                      <TextField {...params} placeholder="Wybierz typy oświadczeń" variant="outlined" size="small" />
                    )}
                  />
                </td>
                <td>
                  <div style={{ display: 'flex', gap: '0.25rem', flexDirection: 'column' }}>
                    <button
                      className={`btn btn-sm ${isSuccess ? 'btn-success' : 'btn-primary'}`}
                      onClick={() => handleSaveAssignments(ct.id)}
                      disabled={isSaving || !changed}
                    >
                      {isSaving ? 'Zapisywanie...' : isSuccess ? 'Zapisano' : 'Zapisz oświadczenia'}
                    </button>
                    {!ct.system && (
                      <>
                        <button className="btn btn-sm" onClick={() => openEdit(ct)}>Edytuj</button>
                        <button className="btn btn-sm btn-danger" onClick={() => handleDelete(ct.id)}>Usuń</button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>

      {showForm && (
        <div className="modal-overlay" onClick={closeForm}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>{editingId ? 'Edytuj typ kontrahenta' : 'Dodaj typ kontrahenta'}</h2>
            {formError && <div className="alert alert-error">{formError}</div>}
            <div className="form-group">
              <label htmlFor="symbol">Symbol (max 10 znaków)</label>
              <input
                id="symbol"
                type="text"
                maxLength={10}
                value={formSymbol}
                onChange={e => setFormSymbol(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="name">Nazwa</label>
              <input
                id="name"
                type="text"
                maxLength={255}
                value={formName}
                onChange={e => setFormName(e.target.value)}
              />
            </div>
            <div className="modal-actions">
              <button className="btn" onClick={closeForm}>Anuluj</button>
              <button className="btn btn-primary" onClick={handleSave}>Zapisz</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
