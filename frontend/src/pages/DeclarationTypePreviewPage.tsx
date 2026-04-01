import { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Fade from '@mui/material/Fade';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import AppTable from '../components/AppTable';
import type { Column } from '../components/AppTable';
import {
  fetchDeclarationTypeByCode,
  type DeclarationTypeDetail,
  type DeclarationTypeField,
} from '../api/declarationTypes';

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

  const columns = useMemo<Column<DeclarationTypeField>[]>(
    () => [
      {
        id: 'position',
        label: 'Nr',
        minWidth: 50,
        getValue: (f) => f.position,
      },
      {
        id: 'fieldCode',
        label: 'Kod',
        minWidth: 100,
        render: (f) => (
          <Typography variant="body2" component="code" sx={{ fontFamily: 'monospace' }}>
            {f.fieldCode}
          </Typography>
        ),
      },
      { id: 'dataType', label: 'Typ danych', minWidth: 100 },
      { id: 'fieldName', label: 'Nazwa pozycji', minWidth: 200 },
      {
        id: 'required',
        label: 'Wymagane',
        minWidth: 90,
        sortable: false,
        filterable: false,
        render: (f) => (
          <Chip label={f.required ? 'Tak' : 'Nie'} size="small" color={f.required ? 'primary' : 'default'} />
        ),
      },
      { id: 'unit', label: 'Jednostka', minWidth: 80 },
    ],
    [],
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        {error}
      </Alert>
    );
  }

  if (!detail) return null;

  return (
    <Fade in>
      <Box>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/admin/declaration-types')}
          sx={{ mb: 2 }}
        >
          Powrót do listy
        </Button>

        <Typography variant="h4" gutterBottom>
          {detail.code} — {detail.name}
        </Typography>

        <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Typy kontrahentów
              </Typography>
              <Typography>{detail.contractorTypes}</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Komentarz kontrahenta
              </Typography>
              <Box sx={{ mt: 0.25 }}>
                <Chip
                  label={detail.hasComment ? 'Tak' : 'Nie'}
                  size="small"
                  color={detail.hasComment ? 'success' : 'default'}
                />
              </Box>
            </Box>
          </Box>
        </Paper>

        <Typography variant="h6" gutterBottom>
          Pola oświadczenia
        </Typography>

        <AppTable
          columns={columns}
          rows={detail.fields}
          getRowKey={(f) => f.fieldCode}
          emptyMessage="Brak zdefiniowanych pól"
        />
      </Box>
    </Fade>
  );
}
