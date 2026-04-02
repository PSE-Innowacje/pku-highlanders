import { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Alert from '@mui/material/Alert';
import Tooltip from '@mui/material/Tooltip';
import CircularProgress from '@mui/material/CircularProgress';
import SaveIcon from '@mui/icons-material/Save';
import AppTable from '../components/AppTable';
import AppModal from '../components/AppModal';
import type { Column } from '../components/AppTable';
import {
  fetchKontrahentUsers,
  updateUserAssignment,
  updateAgreementNumber,
  type KontrahentUser,
} from '../api/kontrahentUsers';
import { fetchContractorTypes, type ContractorType } from '../api/contractorTypes';

export function UserContractorTypesPage() {
  const [users, setUsers] = useState<KontrahentUser[]>([]);
  const [allTypes, setAllTypes] = useState<ContractorType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [typeSelections, setTypeSelections] = useState<Map<string, number | null>>(new Map());
  const [agreementInputs, setAgreementInputs] = useState<Map<string, string>>(new Map());
  const [saving, setSaving] = useState<Set<string>>(new Set());
  const [saveSuccess, setSaveSuccess] = useState<Set<string>>(new Set());

  const [detailUser, setDetailUser] = useState<KontrahentUser | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [usersData, typesData] = await Promise.all([
        fetchKontrahentUsers(),
        fetchContractorTypes(),
      ]);
      setUsers(usersData);
      setAllTypes(typesData);

      const typeSels = new Map<string, number | null>();
      const agrInputs = new Map<string, string>();
      usersData.forEach(user => {
        typeSels.set(user.keycloakUserId, user.assignedType?.id ?? null);
        agrInputs.set(user.keycloakUserId, user.contractorData?.agreementNumber ?? '');
      });
      setTypeSelections(typeSels);
      setAgreementInputs(agrInputs);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleTypeSelect = (userId: string, value: string) => {
    const typeId = value === '' ? null : Number(value);
    setTypeSelections(prev => {
      const next = new Map(prev);
      next.set(userId, typeId);
      return next;
    });
    clearSuccess(userId);
  };

  const handleAgreementChange = (userId: string, value: string) => {
    setAgreementInputs(prev => {
      const next = new Map(prev);
      next.set(userId, value);
      return next;
    });
    clearSuccess(userId);
  };

  const clearSuccess = (userId: string) => {
    setSaveSuccess(prev => {
      const next = new Set(prev);
      next.delete(userId);
      return next;
    });
  };

  const hasChanges = (user: KontrahentUser): boolean => {
    const currentTypeId = user.assignedType?.id ?? null;
    const selectedTypeId = typeSelections.get(user.keycloakUserId) ?? null;
    const typeChanged = currentTypeId !== selectedTypeId;

    const currentAgreement = user.contractorData?.agreementNumber ?? '';
    const inputAgreement = agreementInputs.get(user.keycloakUserId) ?? '';
    const agreementChanged = currentAgreement !== inputAgreement;

    return typeChanged || agreementChanged;
  };

  const handleSave = async (userId: string) => {
    const typeId = typeSelections.get(userId) ?? null;
    const agreement = agreementInputs.get(userId) ?? '';
    setSaving(prev => new Set(prev).add(userId));
    setError(null);
    try {
      await Promise.all([
        updateUserAssignment(userId, typeId),
        updateAgreementNumber(userId, agreement),
      ]);
      const freshUsers = await fetchKontrahentUsers();
      setUsers(freshUsers);
      const freshUser = freshUsers.find(u => u.keycloakUserId === userId);
      if (freshUser) {
        setTypeSelections(prev => {
          const next = new Map(prev);
          next.set(userId, freshUser.assignedType?.id ?? null);
          return next;
        });
        setAgreementInputs(prev => {
          const next = new Map(prev);
          next.set(userId, freshUser.contractorData?.agreementNumber ?? '');
          return next;
        });
      }
      setSaveSuccess(prev => new Set(prev).add(userId));
      setTimeout(() => {
        setSaveSuccess(prev => {
          const next = new Set(prev);
          next.delete(userId);
          return next;
        });
      }, 2000);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nieznany błąd');
    } finally {
      setSaving(prev => {
        const next = new Set(prev);
        next.delete(userId);
        return next;
      });
    }
  };

  const columns: Column<KontrahentUser>[] = [
      {
        id: 'user',
        label: 'Użytkownik',
        minWidth: 180,
        sortable: true,
        filterable: true,
        getValue: (row) => `${row.firstName} ${row.lastName}`,
        render: (row) => (
          <Box>
            <Typography
              variant="body2"
              component="button"
              onClick={(e: React.MouseEvent) => {
                e.stopPropagation();
                setDetailUser(row);
              }}
              sx={{
                background: 'none',
                border: 'none',
                color: 'primary.main',
                cursor: 'pointer',
                p: 0,
                font: 'inherit',
                textDecoration: 'underline',
                textAlign: 'left',
                '&:hover': { color: 'primary.dark' },
              }}
            >
              {row.firstName} {row.lastName}
            </Typography>
            <Typography variant="caption" color="text.secondary" display="block">
              {row.username}
            </Typography>
          </Box>
        ),
      },
      {
        id: 'email',
        label: 'Email',
        minWidth: 200,
        sortable: true,
        filterable: true,
        getValue: (row) => row.email,
      },
      {
        id: 'agreementNumber',
        label: 'Numer umowy',
        minWidth: 180,
        sortable: false,
        filterable: false,
        render: (row) => {
          const isSaving = saving.has(row.keycloakUserId);
          const value = agreementInputs.get(row.keycloakUserId) ?? '';
          return (
            <TextField
              size="small"
              variant="outlined"
              value={value}
              onChange={(e) => handleAgreementChange(row.keycloakUserId, e.target.value)}
              disabled={isSaving}
              placeholder="Numer umowy"
              fullWidth
              onClick={(e) => e.stopPropagation()}
            />
          );
        },
      },
      {
        id: 'contractorType',
        label: 'Typ kontrahenta',
        minWidth: 200,
        sortable: false,
        filterable: false,
        render: (row) => {
          const isSaving = saving.has(row.keycloakUserId);
          const selectedTypeId = typeSelections.get(row.keycloakUserId) ?? null;
          return (
            <FormControl size="small" fullWidth onClick={(e) => e.stopPropagation()}>
              <Select
                value={selectedTypeId === null ? '' : String(selectedTypeId)}
                onChange={(e) => handleTypeSelect(row.keycloakUserId, e.target.value)}
                disabled={isSaving}
                displayEmpty
              >
                <MenuItem value="">
                  <em>-- Brak --</em>
                </MenuItem>
                {allTypes.map(type => (
                  <MenuItem key={type.id} value={String(type.id)}>
                    {type.symbol} -- {type.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          );
        },
      },
      {
        id: 'actions',
        label: 'Akcje',
        minWidth: 100,
        sortable: false,
        filterable: false,
        render: (row) => {
          const isSaving = saving.has(row.keycloakUserId);
          const isSuccess = saveSuccess.has(row.keycloakUserId);
          const changed = hasChanges(row);
          return (
            <Box onClick={(e) => e.stopPropagation()}>
              <Tooltip title={isSuccess ? 'Zapisano' : 'Zapisz zmiany'}>
                <span>
                  <Button
                    variant={isSuccess ? 'contained' : 'outlined'}
                    color={isSuccess ? 'success' : 'primary'}
                    size="small"
                    startIcon={
                      isSaving ? <CircularProgress size={16} /> : <SaveIcon fontSize="small" />
                    }
                    onClick={() => handleSave(row.keycloakUserId)}
                    disabled={isSaving || !changed}
                  >
                    {isSaving ? 'Zapisywanie...' : isSuccess ? 'Zapisano' : 'Zapisz'}
                  </Button>
                </span>
              </Tooltip>
            </Box>
          );
        },
      },
  ];

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  const detailFields: { label: string; value: string }[] = detailUser
    ? [
        { label: 'Imię', value: detailUser.firstName || '--' },
        { label: 'Nazwisko', value: detailUser.lastName || '--' },
        { label: 'Email', value: detailUser.email || '--' },
        { label: 'Login', value: detailUser.username },
        { label: 'Skrót kontrahenta', value: detailUser.contractorData?.contractorAbbreviation || '--' },
        { label: 'Nazwa pełna', value: detailUser.contractorData?.contractorFullName || '--' },
        { label: 'Nazwa skrócona', value: detailUser.contractorData?.contractorShortName || '--' },
        { label: 'KRS', value: detailUser.contractorData?.krs || '--' },
        { label: 'NIP', value: detailUser.contractorData?.nip || '--' },
        { label: 'Adres siedziby', value: detailUser.contractorData?.registeredAddress || '--' },
        { label: 'Kod kontrahenta', value: detailUser.contractorData?.contractorCode || '--' },
        {
          label: 'Typ kontrahenta',
          value: detailUser.assignedType
            ? `${detailUser.assignedType.symbol} -- ${detailUser.assignedType.name}`
            : '--',
        },
        { label: 'Numer umowy', value: detailUser.contractorData?.agreementNumber || '--' },
        { label: 'Data umowy od', value: detailUser.contractorData?.agreementDateFrom || '--' },
        { label: 'Data umowy do', value: detailUser.contractorData?.agreementDateTo || '--' },
      ]
    : [];

  return (
    <Box>
      <Typography variant="h5" component="h1" sx={{ mb: 2 }}>
        Przypisanie typow kontrahentow
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <AppTable
        columns={columns}
        rows={users}
        getRowKey={(row) => row.keycloakUserId}
        emptyMessage="Brak uzytkownikow"
      />

      <AppModal
        open={detailUser !== null}
        onClose={() => setDetailUser(null)}
        title="Dane kontrahenta"
        wide
        actions={
          <Button variant="outlined" onClick={() => setDetailUser(null)}>
            Zamknij
          </Button>
        }
      >
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
            gap: '12px 24px',
          }}
        >
          {detailFields.map((field) => (
            <Box key={field.label}>
              <Typography variant="subtitle2" color="text.secondary">
                {field.label}
              </Typography>
              <Typography variant="body1">{field.value}</Typography>
            </Box>
          ))}
        </Box>
      </AppModal>
    </Box>
  );
}
