import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchDeclarationTypes, type DeclarationType } from '../api/declarationTypes';

export function DeclarationTypesPage() {
  const [types, setTypes] = useState<DeclarationType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const data = await fetchDeclarationTypes();
        setTypes(data);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Nieznany błąd');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <p>Ładowanie...</p>;

  return (
    <div>
      <h1>Typy oświadczeń</h1>

      {error && <div className="alert alert-error">{error}</div>}

      <table className="table">
        <thead>
          <tr>
            <th>Kod</th>
            <th>Nazwa opłaty</th>
            <th>Typy kontrahentów</th>
            <th>Komentarz</th>
            <th>Pola</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {types.map(dt => (
            <tr key={dt.id}>
              <td><strong>{dt.code}</strong></td>
              <td>{dt.name}</td>
              <td>{dt.contractorTypes}</td>
              <td>{dt.hasComment ? 'Tak' : 'Nie'}</td>
              <td>{dt.fieldCount}</td>
              <td>
                <button
                  className="btn btn-sm btn-primary"
                  onClick={() => navigate(`/admin/declaration-types/${dt.code}`)}
                >
                  Podgląd
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
