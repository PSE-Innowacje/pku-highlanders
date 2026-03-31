import { useEffect, useState } from 'react';
import {
  fetchContractorTypes,
  createContractorType,
  updateContractorType,
  deleteContractorType,
  type ContractorType,
} from '../api/contractorTypes';

export function ContractorTypesPage() {
  const [types, setTypes] = useState<ContractorType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formSymbol, setFormSymbol] = useState('');
  const [formName, setFormName] = useState('');
  const [formError, setFormError] = useState<string | null>(null);

  const loadTypes = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchContractorTypes();
      setTypes(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTypes();
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
      await loadTypes();
    } catch (e) {
      setFormError(e instanceof Error ? e.message : 'Nieznany błąd');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Czy na pewno chcesz usunąć ten typ kontrahenta?')) return;
    try {
      await deleteContractorType(id);
      await loadTypes();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    }
  };

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
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {types.map(ct => (
            <tr key={ct.id}>
              <td>{ct.symbol}</td>
              <td>{ct.name}</td>
              <td>{ct.system ? 'Tak' : 'Nie'}</td>
              <td>
                {!ct.system && (
                  <>
                    <button className="btn btn-sm" onClick={() => openEdit(ct)}>Edytuj</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(ct.id)}>Usuń</button>
                  </>
                )}
              </td>
            </tr>
          ))}
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
