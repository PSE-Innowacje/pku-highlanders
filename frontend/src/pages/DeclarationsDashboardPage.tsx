import { useEffect, useMemo, useState } from 'react';
import {
  fetchMyDeclarations,
  fetchDeclarationDetail,
  saveDeclaration,
  submitDeclaration,
  generateDeclarations,
  type Declaration,
  type DeclarationDetail,
} from '../api/declarations';
import { buildFormulaMap, recalculate, getInputProps } from '../utils/fieldFormulas';

export function DeclarationsDashboardPage() {
  const [declarations, setDeclarations] = useState<Declaration[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fill modal state
  const [detail, setDetail] = useState<DeclarationDetail | null>(null);
  const [showFillModal, setShowFillModal] = useState(false);
  const [fieldValues, setFieldValues] = useState<Record<string, string>>({});
  const [comment, setComment] = useState('');
  const [saving, setSaving] = useState(false);
  const [fillError, setFillError] = useState<string | null>(null);

  const formulaMap = useMemo(
    () => detail ? buildFormulaMap(detail.fields) : new Map(),
    [detail]
  );

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

  const openFillModal = async (id: number) => {
    setFillError(null);
    try {
      const d = await fetchDeclarationDetail(id);
      setDetail(d);
      const initialValues = d.fieldValues ?? {};
      const fm = buildFormulaMap(d.fields);
      setFieldValues(recalculate(initialValues, fm, d.fields));
      setComment(d.comment ?? '');
      setShowFillModal(true);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    }
  };

  const closeFillModal = () => {
    setShowFillModal(false);
    setDetail(null);
    setFillError(null);
  };

  const handleFieldChange = (fieldCode: string, value: string) => {
    if (!detail) return;
    const updated = { ...fieldValues, [fieldCode]: value };
    setFieldValues(recalculate(updated, formulaMap, detail.fields));
  };

  const handleSave = async () => {
    if (!detail) return;
    setSaving(true);
    setFillError(null);
    try {
      await saveDeclaration(detail.id, fieldValues, comment || null);
      closeFillModal();
      await loadDeclarations();
    } catch (e) {
      setFillError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = async (id: number) => {
    if (!window.confirm('Czy na pewno chcesz wysłać oświadczenie? Po wysłaniu nie będzie można go edytować.')) return;
    setError(null);
    try {
      const json = await submitDeclaration(id);
      const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `oswiadczenie-${id}.json`;
      a.click();
      URL.revokeObjectURL(url);
      await loadDeclarations();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
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

  const canFill = (status: string) => status === 'NIE_ZLOZONE' || status === 'ROBOCZE';
  const canSubmit = (status: string) => status === 'ROBOCZE';

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
              <th>Typ</th>
              <th>Nazwa</th>
              <th>Status</th>
              <th>Data utworzenia</th>
              <th>Akcje</th>
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
                <td>
                  <div style={{ display: 'flex', gap: '0.25rem' }}>
                    <button
                      className="btn btn-sm btn-primary"
                      onClick={() => openFillModal(d.id)}
                      disabled={!canFill(d.status)}
                    >
                      Wypełnij
                    </button>
                    <button
                      className="btn btn-sm btn-success"
                      onClick={() => handleSubmit(d.id)}
                      disabled={!canSubmit(d.status)}
                    >
                      Wyślij
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showFillModal && detail && (
        <div className="modal-overlay" onClick={closeFillModal}>
          <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
            <h2>Wypełnij oświadczenie</h2>
            <div className="detail-info" style={{ marginBottom: '1rem' }}>
              <p><strong>Numer:</strong> {detail.declarationNumber}</p>
              <p><strong>Typ:</strong> {detail.declarationTypeCode} — {detail.declarationTypeName}</p>
              <p><strong>Status:</strong> {detail.statusLabel}</p>
            </div>

            {fillError && <div className="alert alert-error">{fillError}</div>}

            <table className="table">
              <thead>
                <tr>
                  <th>Nr</th>
                  <th>Kod</th>
                  <th>Nazwa pozycji</th>
                  <th>Wartość</th>
                  <th>Jedn.</th>
                </tr>
              </thead>
              <tbody>
                {detail.fields.map(f => {
                  const isComputed = formulaMap.has(f.fieldCode);
                  const inputProps = getInputProps(f.dataType);
                  return (
                    <tr key={f.fieldCode}>
                      <td>{f.position}</td>
                      <td><code>{f.fieldCode}</code></td>
                      <td>
                        {f.fieldName}
                        {f.required && <span style={{ color: 'var(--danger)', marginLeft: '0.25rem' }}>*</span>}
                      </td>
                      <td>
                        <input
                          type="number"
                          step={inputProps.step}
                          min={inputProps.min}
                          max={inputProps.max}
                          className={`field-input ${isComputed ? 'field-computed' : ''}`}
                          value={fieldValues[f.fieldCode] ?? ''}
                          onChange={e => handleFieldChange(f.fieldCode, e.target.value)}
                          readOnly={isComputed}
                          tabIndex={isComputed ? -1 : undefined}
                          placeholder={isComputed ? 'Auto' : f.dataType}
                        />
                      </td>
                      <td>{f.unit}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>

            {detail.hasComment && (
              <div className="form-group" style={{ marginTop: '1rem' }}>
                <label>Komentarz (max 1000 znaków)</label>
                <textarea
                  className="field-textarea"
                  maxLength={1000}
                  rows={3}
                  value={comment}
                  onChange={e => setComment(e.target.value)}
                />
              </div>
            )}

            <div className="modal-actions">
              <button className="btn" onClick={closeFillModal}>Anuluj</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                {saving ? 'Zapisywanie...' : 'Zapisz'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
