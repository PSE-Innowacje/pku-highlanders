import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  fetchDeclarationTypes,
  fetchScheduleEntries,
  saveScheduleEntries,
  generateDeclarationsForSchedule,
  type DeclarationType,
  type ScheduleEntry,
} from '../api/declarationTypes';

const SCHEDULE_POSITIONS = [
  'Składanie oświadczenia rozliczeniowego',
  'Wystawienie faktury za świadczenie usług',
  'Składanie korygującego oświadczenia rozliczeniowego',
  'Wystawienie faktury za świadczenie usług po korekcie',
];

const DAY_TYPES = ['Dzień kalendarzowy', 'Dzień roboczy'];

export function DeclarationTypesPage() {
  const [types, setTypes] = useState<DeclarationType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  // Schedule modal state
  const [scheduleCode, setScheduleCode] = useState<string | null>(null);
  const [scheduleEntries, setScheduleEntries] = useState<ScheduleEntry[]>([]);
  const [scheduleSaving, setScheduleSaving] = useState(false);
  const [scheduleError, setScheduleError] = useState<string | null>(null);
  const [generating, setGenerating] = useState<number | null>(null);
  const [generateResult, setGenerateResult] = useState<string | null>(null);

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

  const openScheduleModal = async (code: string) => {
    setScheduleError(null);
    try {
      const entries = await fetchScheduleEntries(code);
      setScheduleEntries(entries);
      setScheduleCode(code);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    }
  };

  const closeScheduleModal = () => {
    setScheduleCode(null);
    setScheduleEntries([]);
    setScheduleError(null);
  };

  const addScheduleRow = () => {
    setScheduleEntries(prev => [
      ...prev,
      { id: null, position: SCHEDULE_POSITIONS[0], day: 1, hour: 12, dayType: DAY_TYPES[0] },
    ]);
  };

  const removeScheduleRow = (index: number) => {
    setScheduleEntries(prev => prev.filter((_, i) => i !== index));
  };

  const updateScheduleEntry = (index: number, field: keyof ScheduleEntry, value: string | number) => {
    setScheduleEntries(prev => prev.map((entry, i) =>
      i === index ? { ...entry, [field]: value } : entry
    ));
  };

  const handleGenerate = async (day: number) => {
    if (!scheduleCode) return;
    setGenerating(day);
    setGenerateResult(null);
    setScheduleError(null);
    try {
      const result = await generateDeclarationsForSchedule(scheduleCode, day);
      setGenerateResult(`Wygenerowano ${result.generated} oświadczeń dla dnia ${day}`);
    } catch (e) {
      setScheduleError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setGenerating(null);
    }
  };

  const handleSaveSchedule = async () => {
    if (!scheduleCode) return;
    setScheduleSaving(true);
    setScheduleError(null);
    try {
      const saved = await saveScheduleEntries(scheduleCode, scheduleEntries);
      setScheduleEntries(saved);
      closeScheduleModal();
    } catch (e) {
      setScheduleError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setScheduleSaving(false);
    }
  };

  if (loading) return <div className="loading">Ładowanie...</div>;

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
                <div style={{ display: 'flex', gap: '0.25rem' }}>
                  <button
                    className="btn btn-sm btn-primary"
                    onClick={() => navigate(`/admin/declaration-types/${dt.code}`)}
                  >
                    Podgląd
                  </button>
                  <button
                    className="btn btn-sm"
                    onClick={() => openScheduleModal(dt.code)}
                  >
                    Edytuj harmonogram
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {scheduleCode && (
        <div className="modal-overlay" onClick={closeScheduleModal}>
          <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
            <h2>Harmonogram — {scheduleCode}</h2>

            {scheduleError && <div className="alert alert-error">{scheduleError}</div>}
            {generateResult && <div className="alert alert-success" style={{ background: '#d1fae5', color: '#065f46', border: '1px solid #6ee7b7', padding: '0.5rem 1rem', borderRadius: '6px', marginBottom: '0.5rem' }}>{generateResult}</div>}

            <table className="table">
              <thead>
                <tr>
                  <th>Pozycja terminarza</th>
                  <th>Dzień</th>
                  <th>Godzina</th>
                  <th>Typ dnia</th>
                  <th>Akcje</th>
                </tr>
              </thead>
              <tbody>
                {scheduleEntries.map((entry, idx) => (
                  <tr key={idx}>
                    <td>
                      <select
                        className="select"
                        value={entry.position}
                        onChange={e => updateScheduleEntry(idx, 'position', e.target.value)}
                      >
                        {SCHEDULE_POSITIONS.map(p => (
                          <option key={p} value={p}>{p}</option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <input
                        type="number"
                        className="field-input"
                        style={{ width: '70px', minWidth: '70px' }}
                        min={1}
                        max={31}
                        value={entry.day}
                        onChange={e => updateScheduleEntry(idx, 'day', parseInt(e.target.value) || 1)}
                      />
                    </td>
                    <td>
                      <select
                        className="select"
                        style={{ minWidth: '80px' }}
                        value={entry.hour}
                        onChange={e => updateScheduleEntry(idx, 'hour', parseInt(e.target.value))}
                      >
                        {Array.from({ length: 24 }, (_, i) => (
                          <option key={i} value={i}>{String(i).padStart(2, '0')}:00</option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        className="select"
                        value={entry.dayType}
                        onChange={e => updateScheduleEntry(idx, 'dayType', e.target.value)}
                      >
                        {DAY_TYPES.map(d => (
                          <option key={d} value={d}>{d}</option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.25rem' }}>
                        <button
                          className="btn btn-sm btn-primary"
                          onClick={() => handleGenerate(entry.day)}
                          disabled={generating !== null || entry.id === null}
                        >
                          {generating === entry.day ? 'Generowanie...' : 'Wygeneruj'}
                        </button>
                        <button className="btn btn-sm btn-danger" onClick={() => removeScheduleRow(idx)}>
                          Usuń
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {scheduleEntries.length === 0 && (
                  <tr><td colSpan={5} style={{ textAlign: 'center', color: '#9ca3af' }}>Brak wpisów</td></tr>
                )}
              </tbody>
            </table>

            <div style={{ marginTop: '0.75rem' }}>
              <button className="btn btn-sm" onClick={addScheduleRow}>+ Dodaj wpis</button>
            </div>

            <div className="modal-actions">
              <button className="btn" onClick={closeScheduleModal}>Anuluj</button>
              <button className="btn btn-primary" onClick={handleSaveSchedule} disabled={scheduleSaving}>
                {scheduleSaving ? 'Zapisywanie...' : 'Zapisz'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
