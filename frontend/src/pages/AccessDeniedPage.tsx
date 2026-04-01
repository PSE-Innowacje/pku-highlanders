import { useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Button from '@mui/material/Button';
import BlockIcon from '@mui/icons-material/Block';

export function AccessDeniedPage() {
  const navigate = useNavigate();
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
      <Paper sx={{ p: 5, textAlign: 'center', maxWidth: 420 }}>
        <Box sx={{
          width: 72, height: 72, borderRadius: '50%',
          background: 'linear-gradient(135deg, #fef2f2, #fee2e2)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          mx: 'auto', mb: 2,
        }}>
          <BlockIcon sx={{ fontSize: 36, color: 'error.main' }} />
        </Box>
        <Typography variant="h2" gutterBottom>Brak dostępu</Typography>
        <Typography color="text.secondary" sx={{ mb: 3 }}>
          Nie masz uprawnień do wyświetlenia tej strony.
        </Typography>
        <Button variant="outlined" onClick={() => navigate('/')}>Wróć do strony głównej</Button>
      </Paper>
    </Box>
  );
}
