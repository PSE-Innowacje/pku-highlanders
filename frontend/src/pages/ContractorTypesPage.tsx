import { useEffect, useState } from 'react';
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
import Autocomplete from '@mui/material/Autocomplete';
import CircularProgress from '@mui/material/CircularProgress';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import AppTable from '../components/AppTable';
import type { Column } from '../components/AppTable';
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
  const [formDeclarations, setFormDeclarations] = useState<DeclarationTypeRef[]>([]);
  const [formError, setFormError] = useState<string | null>(null);
  const [formSaving, setFormSaving] = useState(false);

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
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const editingType = editingId != null ? types.find(t => t.id === editingId) : null;
  const isSystemEdit = editingType?.system ?? false;

  const declOptions: DeclarationTypeRef[] = allDeclarationTypes.map(d => ({
    id: d.id,
    code: d.code,
    name: d.name,
  }));

  const openAdd = () => {
    setEditingId(null);
    setFormSymbol('');
    setFormName('');
    setFormDeclarations([]);
    setFormError(null);
    setShowForm(true);
  };

  const openEdit = (ct: ContractorType) => {
    setEditingId(ct.id);
    setFormSymbol(ct.symbol);
    setFormName(ct.name);
    setFormDeclarations(ct.declarationTypes ?? []);
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
    setFormSaving(true);
    try {
      if (editingId) {
        await updateContractorType(editingId, { symbol: formSymbol, name: formName });
        const declIds = formDeclarations.map(d => d.id);
        await updateContractorTypeDeclarations(editingId, declIds);
      } else {
        const created = await createContractorType({ symbol: formSymbol, name: formName });
        if (formDeclarations.length > 0) {
          const declIds = formDeclarations.map(d => d.id);
          await updateContractorTypeDeclarations(created.id, declIds);
        }
      }
      closeForm();
      await loadData();
    } catch (e) {
      setFormError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setFormSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Czy na pewno chcesz usunac ten typ kontrahenta?')) return;
    try {
      await deleteContractorType(id);
      await loadData();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    }
  };

  const columns: Column<ContractorType>[] = [
      {
        id: 'symbol',
        label: 'Symbol',
        minWidth: 100,
        sortable: true,
        filterable: true,
        getValue: (row) => row.symbol,
      },
      {
        id: 'name',
        label: 'Nazwa',
        minWidth: 180,
        sortable: true,
        filterable: true,
        getValue: (row) => row.name,
      },
      {
        id: 'system',
        label: 'System',
        minWidth: 80,
        sortable: true,
        filterable: false,
        render: (row) => (
          <Chip
            label={row.system ? 'Tak' : 'Nie'}
            size="small"
            color={row.system ? 'primary' : 'default'}
            variant={row.system ? 'filled' : 'outlined'}
          />
        ),
        getValue: (row) => (row.system ? 'Tak' : 'Nie'),
      },
      {
        id: 'declarationTypes',
        label: 'Przypisane typy oświadczeń',
        minWidth: 250,
        sortable: false,
        filterable: false,
        render: (row) => (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {(row.declarationTypes ?? []).length === 0 ? (
              <Typography variant="body2" color="text.secondary">
                Brak
              </Typography>
            ) : (
              (row.declarationTypes ?? []).map((dt) => (
                <Chip key={dt.id} label={dt.code} size="small" variant="outlined" />
              ))
            )}
          </Box>
        ),
      },
      {
        id: 'actions',
        label: 'Akcje',
        minWidth: 100,
        sortable: false,
        filterable: false,
        render: (row) => (
          <Box sx={{ display: 'flex', gap: 0.5 }}>
            <Tooltip title="Edytuj">
              <IconButton aria-label="Edytuj" size="small" onClick={() => openEdit(row)}>
                <EditIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title={row.system ? 'Typ systemowy nie moze byc usuniety' : 'Usuń'}>
              <span>
                <IconButton
                  aria-label="Usuń"
                  size="small"
                  color="error"
                  disabled={row.system}
                  onClick={() => handleDelete(row.id)}
                >
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
          </Box>
        ),
      }
  ];

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" component="h1">
          Typy kontrahentów
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openAdd}>
          Dodaj
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <AppTable
        columns={columns}
        rows={types}
        getRowKey={(row) => row.id}
        emptyMessage="Brak typow kontrahentow"
      />

      <Fade in={showForm} unmountOnExit>
        <Paper sx={{ mt: 3, p: 3 }} elevation={2}>
          <Typography variant="h6" sx={{ mb: 2 }}>
            {editingId ? 'Edytuj typ kontrahenta' : 'Dodaj typ kontrahenta'}
          </Typography>

          {formError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {formError}
            </Alert>
          )}

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: 600 }}>
            <TextField
              id="symbol"
              label="Symbol (max 10 znakow)"
              value={formSymbol}
              onChange={(e) => setFormSymbol(e.target.value)}
              inputProps={{ maxLength: 10 }}
              size="small"
              disabled={isSystemEdit}
              fullWidth
            />

            <TextField
              id="name"
              label="Nazwa"
              value={formName}
              onChange={(e) => setFormName(e.target.value)}
              inputProps={{ maxLength: 255 }}
              size="small"
              disabled={isSystemEdit}
              fullWidth
            />

            <Autocomplete
              multiple
              size="small"
              options={declOptions}
              getOptionLabel={(o) => `${o.code} -- ${o.name}`}
              isOptionEqualToValue={(o, v) => o.id === v.id}
              value={formDeclarations}
              onChange={(_, newValue) => setFormDeclarations(newValue)}
              renderTags={(value, getTagProps) =>
                value.map((option, index) => {
                  const { key, ...rest } = getTagProps({ index });
                  return <Chip key={key} label={option.code} size="small" {...rest} />;
                })
              }
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Przypisane typy oświadczeń"
                  placeholder="Wybierz typy oświadczeń"
                  variant="outlined"
                  size="small"
                />
              )}
            />

            <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
              <Button
                variant="contained"
                onClick={handleSave}
                disabled={formSaving || !formSymbol.trim() || !formName.trim()}
              >
                {formSaving ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}
                Zapisz
              </Button>
              <Button variant="outlined" onClick={closeForm} disabled={formSaving}>
                Anuluj
              </Button>
            </Box>
          </Box>
        </Paper>
      </Fade>
    </Box>
  );
}
