import { useEffect, useState } from 'react';
import { fetchMyDeclarations, generateDeclarations, type Declaration } from '../api/declarations';

export function DeclarationsDashboardPage() {
  const [declarations, setDeclarations] = useState<Declaration[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadDeclarations = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchMyDeclarations();
      setDeclarations(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDeclarations();
  }, []);

  const handleGenerate = async () => {
    setGenerating(true);
    setError(null);
    try {
      const data = await generateDeclarations();
      setDeclarations(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setGenerating(false);
    }
  };

  const statusBadgeClass = (status: string) => {
    switch (status) {
      case 'NIE_ZLOZONE': return 'badge badge-gray';
      case 'ROBOCZE': return 'badge badge-yellow';
      case 'ZLOZONE': return 'badge badge-green';
      default: return 'badge';
    }
  };

  if (loading) return <p>Ładowanie...</p>;

  return (
    <div>
      <div className="page-header">
        <h1>Lista oświadczeń</h1>
        <button className="btn btn-primary" onClick={handleGenerate} disabled={generating}>
          {generating ? 'Generowanie...' : 'Wygeneruj oświadczenia'}
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {declarations.length === 0 ? (
        <p style={{ color: '#6b7280' }}>
          Brak oświadczeń. Kliknij "Wygeneruj oświadczenia" aby utworzyć oświadczenia na podstawie przypisanego typu kontrahenta.
        </p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Numer oświadczenia</th>
              <th>Typ oświadczenia</th>
              <th>Nazwa</th>
              <th>Status</th>
              <th>Data utworzenia</th>
            </tr>
          </thead>
          <tbody>
            {declarations.map(d => (
              <tr key={d.id}>
                <td><code>{d.declarationNumber}</code></td>
                <td><strong>{d.declarationTypeCode}</strong></td>
                <td>{d.declarationTypeName}</td>
                <td><span className={statusBadgeClass(d.status)}>{d.statusLabel}</span></td>
                <td>{new Date(d.createdAt).toLocaleString('pl-PL')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
