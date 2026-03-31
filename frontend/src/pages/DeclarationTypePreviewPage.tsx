import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchDeclarationTypeByCode, type DeclarationTypeDetail } from '../api/declarationTypes';

export function DeclarationTypePreviewPage() {
  const { code } = useParams<{ code: string }>();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<DeclarationTypeDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!code) return;
    (async () => {
      try {
        setLoading(true);
        const data = await fetchDeclarationTypeByCode(code);
        setDetail(data);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Nieznany błąd');
      } finally {
        setLoading(false);
      }
    })();
  }, [code]);

  if (loading) return <p>Ładowanie...</p>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!detail) return null;

  return (
    <div>
      <button className="btn btn-sm" onClick={() => navigate('/admin/declaration-types')} style={{ marginBottom: '1rem' }}>
        ← Powrót do listy
      </button>

      <h1>{detail.code} — {detail.name}</h1>

      <div className="detail-info">
        <p><strong>Typy kontrahentów:</strong> {detail.contractorTypes}</p>
        <p><strong>Komentarz kontrahenta:</strong> {detail.hasComment ? 'Tak' : 'Nie'}</p>
      </div>

      <h2>Pola oświadczenia</h2>

      <table className="table">
        <thead>
          <tr>
            <th>Nr</th>
            <th>Kod</th>
            <th>Typ danych</th>
            <th>Nazwa pozycji</th>
            <th>Wymagane</th>
            <th>Jedn.</th>
          </tr>
        </thead>
        <tbody>
          {detail.fields.map((f, idx) => (
            <tr key={idx}>
              <td>{f.position}</td>
              <td><code>{f.fieldCode}</code></td>
              <td>{f.dataType}</td>
              <td>{f.fieldName}</td>
              <td>{f.required ? 'Tak' : 'Nie'}</td>
              <td>{f.unit}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
