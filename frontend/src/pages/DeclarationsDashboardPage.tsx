import { useEffect, useMemo, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Chip from '@mui/material/Chip';
import TextField from '@mui/material/TextField';
import Paper from '@mui/material/Paper';
import Alert from '@mui/material/Alert';
import Tooltip from '@mui/material/Tooltip';
import Fade from '@mui/material/Fade';
import CircularProgress from '@mui/material/CircularProgress';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import EditIcon from '@mui/icons-material/Edit';
import SendIcon from '@mui/icons-material/Send';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DescriptionIcon from '@mui/icons-material/Description';
import AppTable from '../components/AppTable';
import type { Column } from '../components/AppTable';
import AppModal from '../components/AppModal';
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

const statusChipProps = (status: string) => {
  switch (status) {
    case 'NIE_ZLOZONE': return { color: 'default' as const };
    case 'ROBOCZE': return { color: 'warning' as const };
    case 'ZLOZONE': return { color: 'success' as const };
    default: return { color: 'default' as const };
  }
};

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

  const canFill = (status: string) => status === 'NIE_ZLOZONE' || status === 'ROBOCZE';
  const canSubmit = (status: string) => status === 'ROBOCZE';
  const isReadOnly = viewMode === 'view';
  const isPending = filter === 'pending';

  const columns: Column<Declaration>[] = [
    {
      id: 'declarationNumber',
      label: 'Numer oświadczenia',
      minWidth: 160,
      sortable: true,
      filterable: true,
      render: (row) =>
        row.status === 'ZLOZONE' ? (
          <Typography
            component="span"
            color="primary"
            sx={{ cursor: 'pointer', textDecoration: 'underline', fontFamily: 'monospace', fontSize: '0.875rem' }}
            onClick={(e) => {
              e.stopPropagation();
              openModal(row.id, 'view');
            }}
          >
            {row.declarationNumber}
          </Typography>
        ) : (
          <Typography component="code" sx={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>
            {row.declarationNumber}
          </Typography>
        ),
      getValue: (row) => row.declarationNumber,
    },
    {
      id: 'declarationTypeCode',
      label: 'Typ',
      minWidth: 80,
      sortable: true,
      filterable: true,
      render: (row) => (
        <Typography fontWeight={700} variant="body2">
          {row.declarationTypeCode}
        </Typography>
      ),
      getValue: (row) => row.declarationTypeCode,
    },
    {
      id: 'declarationTypeName',
      label: 'Nazwa',
      minWidth: 200,
      sortable: true,
      filterable: true,
    },
    {
      id: 'status',
      label: 'Status',
      minWidth: 120,
      sortable: true,
      filterable: true,
      render: (row) => (
        <Chip
          label={row.statusLabel}
          size="small"
          {...statusChipProps(row.status)}
        />
      ),
      getValue: (row) => row.statusLabel,
    },
    {
      id: 'createdAt',
      label: 'Data utworzenia',
      minWidth: 160,
      sortable: true,
      render: (row) => (
        <Typography variant="body2">
          {new Date(row.createdAt).toLocaleString('pl-PL')}
        </Typography>
      ),
      getValue: (row) => row.createdAt,
    },
    {
      id: 'actions',
      label: 'Akcje',
      minWidth: 100,
      sortable: false,
      filterable: false,
      render: (row) => (
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          {isPending && canFill(row.status) && (
            <Tooltip title="Wypełnij" arrow>
              <IconButton
                size="small"
                color="primary"
                onClick={(e) => {
                  e.stopPropagation();
                  openModal(row.id, 'fill');
                }}
              >
                <EditIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          {isPending && canSubmit(row.status) && (
            <Tooltip title="Wyślij" arrow>
              <IconButton
                size="small"
                color="success"
                onClick={(e) => {
                  e.stopPropagation();
                  handleSubmit(row.id);
                }}
              >
                <SendIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          {!isPending && row.status === 'ZLOZONE' && (
            <Tooltip title="Podglad" arrow>
              <IconButton
                size="small"
                color="info"
                onClick={(e) => {
                  e.stopPropagation();
                  openModal(row.id, 'view');
                }}
              >
                <VisibilityIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      ),
    },
  ];

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8 }}>
        <CircularProgress />
        <Typography sx={{ ml: 2 }} color="text.secondary">Ladowanie...</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} sx={{ mb: 3 }}>
        {isPending ? 'Lista oświadczeń - niezłożone' : 'Lista oświadczeń - złożone'}
      </Typography>

      {error && (
        <Fade in>
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        </Fade>
      )}

      {declarations.length === 0 ? (
        <Paper sx={{ py: 6, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
          <DescriptionIcon sx={{ fontSize: 48, color: 'text.disabled' }} />
          <Typography color="text.secondary">
            {isPending ? 'Brak niezłożonych oświadczeń.' : 'Brak złożonych oświadczeń.'}
          </Typography>
        </Paper>
      ) : (
        <AppTable
          columns={columns}
          rows={declarations}
          getRowKey={(row) => row.id}
          emptyMessage={isPending ? 'Brak niezłożonych oświadczeń.' : 'Brak złożonych oświadczeń.'}
        />
      )}

      {detail && (
        <AppModal
          open={showModal}
          onClose={closeModal}
          title={isReadOnly ? 'Podgląd oświadczenia' : 'Wypełnij oświadczenie'}
          wide
          actions={
            <>
              <Button onClick={closeModal}>
                {isReadOnly ? 'Zamknij' : 'Anuluj'}
              </Button>
              {!isReadOnly && (
                <Button
                  variant="contained"
                  onClick={handleSave}
                  disabled={saving || hasValidationErrors}
                  startIcon={saving ? <CircularProgress size={16} /> : undefined}
                >
                  {saving ? 'Zapisywanie...' : 'Zapisz'}
                </Button>
              )}
            </>
          }
        >
          <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">Numer</Typography>
                <Typography variant="body2" fontWeight={600}>{detail.declarationNumber}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Typ</Typography>
                <Typography variant="body2" fontWeight={600}>
                  {detail.declarationTypeCode} — {detail.declarationTypeName}
                </Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Status</Typography>
                <Box sx={{ mt: 0.25 }}>
                  <Chip label={detail.statusLabel} size="small" {...statusChipProps(detail.status)} />
                </Box>
              </Box>
            </Box>
          </Paper>

          {fillError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {fillError}
            </Alert>
          )}

          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 700 }}>Nr</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Kod</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Nazwa pozycji</TableCell>
                  <TableCell sx={{ fontWeight: 700, minWidth: 160 }}>Wartosc</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Jedn.</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {detail.fields.map(f => {
                  const isComputed = formulaMap.has(f.fieldCode);
                  const inputProps = getInputProps(f.dataType);
                  const fieldError = fieldErrors[f.fieldCode];
                  return (
                    <TableRow key={f.fieldCode}>
                      <TableCell>{f.position}</TableCell>
                      <TableCell>
                        <Typography variant="body2" fontFamily="monospace">{f.fieldCode}</Typography>
                      </TableCell>
                      <TableCell>
                        {f.fieldName}
                        {f.required && (
                          <Typography component="span" color="error" sx={{ ml: 0.5 }}>*</Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <TextField
                          type="number"
                          size="small"
                          fullWidth
                          value={fieldValues[f.fieldCode] ?? ''}
                          onChange={e => handleFieldChange(f.fieldCode, e.target.value)}
                          placeholder={isComputed ? 'Auto' : f.dataType}
                          error={!!fieldError && !isReadOnly}
                          helperText={fieldError && !isReadOnly ? fieldError : undefined}
                          slotProps={{
                            htmlInput: {
                              step: inputProps.step,
                              min: inputProps.min,
                              max: inputProps.max,
                              readOnly: isReadOnly || isComputed,
                              tabIndex: isReadOnly || isComputed ? -1 : undefined,
                            },
                            input: {
                              sx: isComputed ? { backgroundColor: 'action.hover' } : undefined,
                            },
                          }}
                          sx={{ '& .MuiFormHelperText-root': { mx: 0 } }}
                        />
                      </TableCell>
                      <TableCell>{f.unit}</TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>

          {detail.hasComment && (
            <Box sx={{ mt: 2 }}>
              <TextField
                label="Komentarz (max 1000 znakow)"
                multiline
                rows={3}
                fullWidth
                value={comment}
                onChange={e => setComment(e.target.value)}
                slotProps={{ htmlInput: { maxLength: 1000, readOnly: isReadOnly } }}
              />
            </Box>
          )}
        </AppModal>
      )}
    </Box>
  );
}
