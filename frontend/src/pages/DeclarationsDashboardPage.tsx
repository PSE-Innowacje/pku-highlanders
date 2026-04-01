import { useEffect, useMemo, useState } from 'react';
import {
  fetchMyDeclarations,
  fetchDeclarationDetail,
  saveDeclaration,
  submitDeclaration,
  type Declaration,
  type DeclarationDetail,
} from '../api/declarations';
import { buildFormulaMap, recalculate, getInputProps, validateFieldValue } from '../utils/fieldFormulas';

interface Props {
  filter: 'pending' | 'submitted';
}

export function DeclarationsDashboardPage({ filter }: Props) {
  const [allDeclarations, setAllDeclarations] = useState<Declaration[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modal state
  const [detail, setDetail] = useState<DeclarationDetail | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [viewMode, setViewMode] = useState<'fill' | 'view'>('fill');
  const [fieldValues, setFieldValues] = useState<Record<string, string>>({});
  const [fieldErrors, setFieldErrors] = useState<Record<string, string | null>>({});
  const [comment, setComment] = useState('');
  const [saving, setSaving] = useState(false);
  const [fillError, setFillError] = useState<string | null>(null);

  const formulaMap = useMemo(
    () => detail ? buildFormulaMap(detail.fields) : new Map(),
    [detail]
  );

  const declarations = useMemo(() => {
    if (filter === 'pending') {
      return allDeclarations.filter(d => d.status === 'NIE_ZLOZONE' || d.status === 'ROBOCZE');
    }
    return allDeclarations.filter(d => d.status === 'ZLOZONE');
  }, [allDeclarations, filter]);

  const hasValidationErrors = useMemo(() => {
    return Object.values(fieldErrors).some(e => e !== null);
  }, [fieldErrors]);

  const loadDeclarations = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchMyDeclarations();
      setAllDeclarations(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDeclarations();
  }, []);

  const openModal = async (id: number, mode: 'fill' | 'view') => {
    setFillError(null);
    try {
      const d = await fetchDeclarationDetail(id);
      setDetail(d);
      setViewMode(mode);
      const initialValues = d.fieldValues ?? {};
      const fm = buildFormulaMap(d.fields);
      const computed = recalculate(initialValues, fm, d.fields);
      setFieldValues(computed);
      const errors: Record<string, string | null> = {};
      d.fields.forEach(f => {
        errors[f.fieldCode] = validateFieldValue(computed[f.fieldCode] ?? '', f.dataType);
      });
      setFieldErrors(errors);
      setComment(d.comment ?? '');
      setShowModal(true);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    }
  };

  const closeModal = () => {
    setShowModal(false);
    setDetail(null);
    setFillError(null);
    setFieldErrors({});
  };

  const handleFieldChange = (fieldCode: string, value: string) => {
    if (!detail) return;

    const updated = { ...fieldValues, [fieldCode]: value };
    const recalculated = recalculate(updated, formulaMap, detail.fields);
    setFieldValues(recalculated);

    const errors = { ...fieldErrors };
    detail.fields.forEach(f => {
      errors[f.fieldCode] = validateFieldValue(recalculated[f.fieldCode] ?? '', f.dataType);
    });
    setFieldErrors(errors);
  };

  const handleSave = async () => {
    if (!detail || hasValidationErrors) return;
    setSaving(true);
    setFillError(null);
    try {
      await saveDeclaration(detail.id, fieldValues, comment || null);
      closeModal();
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
  const isReadOnly = viewMode === 'view';
  const isPending = filter === 'pending';

  if (loading) return <div className="loading">Ładowanie...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>{isPending ? 'Lista oświadczeń - niezłożone' : 'Lista oświadczeń - złożone'}</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {declarations.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">&#9993;</div>
          <p>{isPending
            ? 'Brak niezłożonych oświadczeń.'
            : 'Brak złożonych oświadczeń.'
          }</p>
        </div>
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
                <td>
                  {d.status === 'ZLOZONE' ? (
                    <button
                      onClick={() => openModal(d.id, 'view')}
                      style={{
                        background: 'none',
                        border: 'none',
                        color: 'var(--primary)',
                        cursor: 'pointer',
                        padding: 0,
                        font: 'inherit',
                        textDecoration: 'underline',
                      }}
                    >
                      <code>{d.declarationNumber}</code>
                    </button>
                  ) : (
                    <code>{d.declarationNumber}</code>
                  )}
                </td>
                <td><strong>{d.declarationTypeCode}</strong></td>
                <td>{d.declarationTypeName}</td>
                <td><span className={statusBadgeClass(d.status)}>{d.statusLabel}</span></td>
                <td>{new Date(d.createdAt).toLocaleString('pl-PL')}</td>
                <td>
                  <div style={{ display: 'flex', gap: '0.25rem' }}>
                    {canFill(d.status) && (
                      <button
                        className="btn btn-sm btn-primary"
                        onClick={() => openModal(d.id, 'fill')}
                      >
                        Wypełnij
                      </button>
                    )}
                    {canSubmit(d.status) && (
                      <button
                        className="btn btn-sm btn-success"
                        onClick={() => handleSubmit(d.id)}
                      >
                        Wyślij
                      </button>
                    )}
                    {d.status === 'ZLOZONE' && (
                      <button
                        className="btn btn-sm"
                        onClick={() => openModal(d.id, 'view')}
                      >
                        Podgląd
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showModal && detail && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
            <h2>{isReadOnly ? 'Podgląd oświadczenia' : 'Wypełnij oświadczenie'}</h2>
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
                  const fieldError = fieldErrors[f.fieldCode];
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
                          readOnly={isReadOnly || isComputed}
                          tabIndex={isReadOnly || isComputed ? -1 : undefined}
                          placeholder={isComputed ? 'Auto' : f.dataType}
                          style={fieldError ? { borderColor: 'var(--danger)', boxShadow: '0 0 0 1px var(--danger)' } : undefined}
                        />
                        {fieldError && !isReadOnly && (
                          <div style={{ color: 'var(--danger)', fontSize: '0.75rem', marginTop: '2px' }}>
                            {fieldError}
                          </div>
                        )}
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
                  readOnly={isReadOnly}
                />
              </div>
            )}

            <div className="modal-actions">
              <button className="btn" onClick={closeModal}>{isReadOnly ? 'Zamknij' : 'Anuluj'}</button>
              {!isReadOnly && (
                <button className="btn btn-primary" onClick={handleSave} disabled={saving || hasValidationErrors}>
                  {saving ? 'Zapisywanie...' : 'Zapisz'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
