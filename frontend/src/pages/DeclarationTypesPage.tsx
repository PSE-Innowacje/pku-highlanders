import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Chip from '@mui/material/Chip';
import TextField from '@mui/material/TextField';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Paper from '@mui/material/Paper';
import Alert from '@mui/material/Alert';
import Tooltip from '@mui/material/Tooltip';
import Fade from '@mui/material/Fade';
import CircularProgress from '@mui/material/CircularProgress';
import PreviewIcon from '@mui/icons-material/Preview';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import AppTable from '../components/AppTable';
import type { Column } from '../components/AppTable';
import AppModal from '../components/AppModal';
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

interface EditingEntry {
  index: number | null; // null = new entry
  position: string;
  day: number;
  hour: number;
  dayType: string;
}

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

  // Inline edit form state
  const [editing, setEditing] = useState<EditingEntry | null>(null);

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
    setEditing(null);
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
    setGenerateResult(null);
    setEditing(null);
  };

  const startEditing = (index: number) => {
    const entry = scheduleEntries[index];
    setEditing({
      index,
      position: entry.position,
      day: entry.day,
      hour: entry.hour,
      dayType: entry.dayType,
    });
  };

  const startAdding = () => {
    setEditing({
      index: null,
      position: SCHEDULE_POSITIONS[0],
      day: 1,
      hour: 12,
      dayType: DAY_TYPES[0],
    });
  };

  const cancelEditing = () => {
    setEditing(null);
  };

  const saveEditingEntry = () => {
    if (!editing) return;
    if (editing.index !== null) {
      // Update existing
      setScheduleEntries((prev) =>
        prev.map((entry, i) =>
          i === editing.index
            ? { ...entry, position: editing.position, day: editing.day, hour: editing.hour, dayType: editing.dayType }
            : entry,
        ),
      );
    } else {
      // Add new
      setScheduleEntries((prev) => [
        ...prev,
        { id: null, position: editing.position, day: editing.day, hour: editing.hour, dayType: editing.dayType },
      ]);
    }
    setEditing(null);
  };

  const removeScheduleRow = (index: number) => {
    setScheduleEntries((prev) => prev.filter((_, i) => i !== index));
    if (editing?.index === index) setEditing(null);
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

  // -- Column definitions --

  const mainColumns: Column<DeclarationType>[] = [
      {
        id: 'code',
        label: 'Kod',
        minWidth: 80,
        render: (dt) => <Typography fontWeight={700}>{dt.code}</Typography>,
        getValue: (dt) => dt.code,
      },
      { id: 'name', label: 'Nazwa opłaty', minWidth: 200 },
      { id: 'contractorTypes', label: 'Typy kontrahentów', minWidth: 160 },
      {
        id: 'hasComment',
        label: 'Komentarz',
        minWidth: 90,
        sortable: false,
        filterable: false,
        render: (dt) => (
          <Chip label={dt.hasComment ? 'Tak' : 'Nie'} size="small" color={dt.hasComment ? 'success' : 'default'} />
        ),
      },
      {
        id: 'fieldCount',
        label: 'Pola',
        minWidth: 60,
        getValue: (dt) => dt.fieldCount,
      },
      {
        id: 'actions',
        label: 'Akcje',
        minWidth: 100,
        sortable: false,
        filterable: false,
        render: (dt) => (
          <Box sx={{ display: 'flex', gap: 0.5 }}>
            <Tooltip title="Podgląd">
              <IconButton size="small" color="primary" onClick={() => navigate(`/admin/declaration-types/${dt.code}`)}>
                <PreviewIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Edytuj harmonogram">
              <IconButton size="small" onClick={() => openScheduleModal(dt.code)}>
                <CalendarMonthIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Box>
        ),
      },
  ];

  const scheduleColumns: Column<ScheduleEntry & { _idx: number }>[] = [
      { id: 'position', label: 'Pozycja', minWidth: 240, filterable: false, sortable: false },
      {
        id: 'day',
        label: 'Dzień',
        minWidth: 60,
        filterable: false,
        sortable: false,
        getValue: (e) => e.day,
      },
      {
        id: 'hour',
        label: 'Godzina',
        minWidth: 70,
        filterable: false,
        sortable: false,
        render: (e) => `${String(e.hour).padStart(2, '0')}:00`,
      },
      { id: 'dayType', label: 'Typ dnia', minWidth: 130, filterable: false, sortable: false },
      {
        id: 'actions',
        label: 'Akcje',
        minWidth: 120,
        sortable: false,
        filterable: false,
        render: (e) => (
          <Box sx={{ display: 'flex', gap: 0.5 }}>
            <Tooltip title="Edytuj">
              <IconButton size="small" onClick={() => startEditing(e._idx)}>
                <EditIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Usuń">
              <IconButton size="small" color="error" onClick={() => removeScheduleRow(e._idx)}>
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Wygeneruj oświadczenia">
              <span>
                <IconButton
                  size="small"
                  color="success"
                  onClick={() => handleGenerate(e.day)}
                  disabled={generating !== null || e.id === null}
                >
                  {generating === e.day ? <CircularProgress size={18} /> : <PlayArrowIcon fontSize="small" />}
                </IconButton>
              </span>
            </Tooltip>
          </Box>
        ),
      },
  ];

  // Attach index to schedule entries for row actions
  const indexedScheduleEntries = useMemo(
    () => scheduleEntries.map((e, i) => ({ ...e, _idx: i })),
    [scheduleEntries],
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Typy oświadczeń
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <AppTable columns={mainColumns} rows={types} getRowKey={(dt) => dt.id} emptyMessage="Brak typów oświadczeń" />

      {/* Schedule Modal */}
      <AppModal
        open={scheduleCode !== null}
        onClose={closeScheduleModal}
        title={`Harmonogram — ${scheduleCode ?? ''}`}
        wide
        actions={
          <>
            <Button onClick={closeScheduleModal}>Anuluj</Button>
            <Button variant="contained" onClick={handleSaveSchedule} disabled={scheduleSaving}>
              {scheduleSaving ? 'Zapisywanie...' : 'Zapisz'}
            </Button>
          </>
        }
      >
        {scheduleError && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setScheduleError(null)}>
            {scheduleError}
          </Alert>
        )}
        {generateResult && (
          <Alert severity="success" sx={{ mb: 2 }} onClose={() => setGenerateResult(null)}>
            {generateResult}
          </Alert>
        )}

        <AppTable
          columns={scheduleColumns}
          rows={indexedScheduleEntries}
          getRowKey={(e) => e._idx}
          emptyMessage="Brak wpisów"
          maxHeight={360}
        />

        <Box sx={{ mt: 1.5 }}>
          <Button variant="outlined" size="small" startIcon={<AddIcon />} onClick={startAdding}>
            Dodaj wpis
          </Button>
        </Box>

        {/* Inline edit form */}
        <Fade in={editing !== null} unmountOnExit>
          <Paper variant="outlined" sx={{ mt: 2, p: 2 }}>
            <Typography variant="subtitle2" gutterBottom>
              {editing?.index !== null ? 'Edytuj wpis' : 'Nowy wpis'}
            </Typography>

            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, alignItems: 'flex-start' }}>
              <FormControl size="small" sx={{ minWidth: 280 }}>
                <InputLabel>Pozycja</InputLabel>
                <Select
                  value={editing?.position ?? SCHEDULE_POSITIONS[0]}
                  label="Pozycja"
                  onChange={(e) => setEditing((prev) => (prev ? { ...prev, position: e.target.value } : prev))}
                >
                  {SCHEDULE_POSITIONS.map((p) => (
                    <MenuItem key={p} value={p}>
                      {p}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <TextField
                size="small"
                label="Dzień"
                type="number"
                slotProps={{ htmlInput: { min: 1, max: 31 } }}
                value={editing?.day ?? 1}
                onChange={(e) =>
                  setEditing((prev) => (prev ? { ...prev, day: parseInt(e.target.value) || 1 } : prev))
                }
                sx={{ width: 90 }}
              />

              <FormControl size="small" sx={{ minWidth: 100 }}>
                <InputLabel>Godzina</InputLabel>
                <Select
                  value={editing?.hour ?? 12}
                  label="Godzina"
                  onChange={(e) =>
                    setEditing((prev) => (prev ? { ...prev, hour: Number(e.target.value) } : prev))
                  }
                >
                  {Array.from({ length: 24 }, (_, i) => (
                    <MenuItem key={i} value={i}>
                      {String(i).padStart(2, '0')}:00
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <FormControl size="small" sx={{ minWidth: 170 }}>
                <InputLabel>Typ dnia</InputLabel>
                <Select
                  value={editing?.dayType ?? DAY_TYPES[0]}
                  label="Typ dnia"
                  onChange={(e) => setEditing((prev) => (prev ? { ...prev, dayType: e.target.value } : prev))}
                >
                  {DAY_TYPES.map((d) => (
                    <MenuItem key={d} value={d}>
                      {d}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>

            <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
              <Button variant="contained" size="small" onClick={saveEditingEntry}>
                Zapisz wpis
              </Button>
              <Button size="small" onClick={cancelEditing}>
                Anuluj
              </Button>
            </Box>
          </Paper>
        </Fade>
      </AppModal>
    </Box>
  );
}
