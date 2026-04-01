import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardActionArea from '@mui/material/CardActionArea';
import CardContent from '@mui/material/CardContent';
import Fade from '@mui/material/Fade';
import DescriptionIcon from '@mui/icons-material/Description';
import SettingsIcon from '@mui/icons-material/Settings';
import BusinessIcon from '@mui/icons-material/Business';
import { useAuth } from '../hooks/useAuth';
import { fetchCurrentUserContractorData, type CurrentUserContractorData } from '../api/kontrahentUsers';

export function DashboardPage() {
  const { displayName, roles, isAdmin, isKontrahent } = useAuth();
  const navigate = useNavigate();
  const [contractorData, setContractorData] = useState<CurrentUserContractorData | null>(null);

  const displayRoles = roles.filter(r => r === 'Administrator' || r === 'Kontrahent');

  useEffect(() => {
    if (isKontrahent) {
      fetchCurrentUserContractorData().then(setContractorData).catch(() => {});
    }
  }, [isKontrahent]);

  const name = contractorData?.firstName && contractorData?.lastName
    ? `${contractorData.firstName} ${contractorData.lastName}`
    : displayName;

  return (
    <Fade in timeout={400}>
      <Box>
        <Box sx={{
          background: 'linear-gradient(135deg, #713600, #38240D, #C05800)',
          borderRadius: 3, p: { xs: 3, md: 4 }, color: '#fff', mb: 3,
          position: 'relative', overflow: 'hidden',
        }}>
          <Box sx={{
            position: 'absolute', top: -40, right: -20,
            width: 200, height: 200, borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%)',
          }} />
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, position: 'relative' }}>
            <Box component="img" src="/snoopy.png" alt="Logo" sx={{ width: 56, height: 56, borderRadius: 2, objectFit: 'cover' }} />
            <Box>
              <Typography variant="h1" sx={{ color: '#fff', mb: 0.5 }}>Witaj, {name}</Typography>
              <Typography sx={{ color: 'rgba(255,255,255,0.8)', fontSize: '0.95rem' }}>
                {displayRoles.length > 0 ? `Twoje role: ${displayRoles.join(', ')}` : 'Brak przypisanych ról'}
              </Typography>
            </Box>
          </Box>
          {isKontrahent && contractorData && (
            <Box sx={{ mt: 2, position: 'relative' }}>
              {contractorData.agreementNumber && (
                <Typography sx={{ color: 'rgba(255,255,255,0.9)', fontSize: '0.9rem' }}>
                  <strong>Numer umowy:</strong> {contractorData.agreementNumber}
                </Typography>
              )}
              {contractorData.contractorFullName && (
                <Typography sx={{ color: 'rgba(255,255,255,0.9)', fontSize: '0.9rem' }}>
                  <strong>Nazwa kontrahenta:</strong> {contractorData.contractorFullName}
                </Typography>
              )}
            </Box>
          )}
        </Box>

        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(auto-fit, minmax(260px, 1fr))' }, gap: 2 }}>
          {isKontrahent && (
            <Card sx={{ transition: 'transform 0.2s, box-shadow 0.2s', '&:hover': { transform: 'translateY(-3px)', boxShadow: 4 } }}>
              <CardActionArea onClick={() => navigate('/declarations/pending')} sx={{ p: 2 }}>
                <CardContent>
                  <Box sx={{ width: 44, height: 44, borderRadius: 2, background: 'linear-gradient(135deg, #d1fae5, #a7f3d0)', display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 1.5 }}>
                    <DescriptionIcon sx={{ color: '#059669' }} />
                  </Box>
                  <Typography variant="h3" gutterBottom>Oświadczenia</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Przeglądaj, wypełniaj i wysyłaj swoje oświadczenia rozliczeniowe.
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          )}

          {isAdmin && (
            <>
              <Card sx={{ transition: 'transform 0.2s, box-shadow 0.2s', '&:hover': { transform: 'translateY(-3px)', boxShadow: 4 } }}>
                <CardActionArea onClick={() => navigate('/admin/declaration-types')} sx={{ p: 2 }}>
                  <CardContent>
                    <Box sx={{ width: 44, height: 44, borderRadius: 2, background: 'linear-gradient(135deg, #dbeafe, #bfdbfe)', display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 1.5 }}>
                      <SettingsIcon sx={{ color: '#2563eb' }} />
                    </Box>
                    <Typography variant="h3" gutterBottom>Typy oświadczeń</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Zarządzaj typami oświadczeń i ich harmonogramami składania.
                    </Typography>
                  </CardContent>
                </CardActionArea>
              </Card>
              <Card sx={{ transition: 'transform 0.2s, box-shadow 0.2s', '&:hover': { transform: 'translateY(-3px)', boxShadow: 4 } }}>
                <CardActionArea onClick={() => navigate('/admin/contractor-types')} sx={{ p: 2 }}>
                  <CardContent>
                    <Box sx={{ width: 44, height: 44, borderRadius: 2, background: 'linear-gradient(135deg, #d1fae5, #a7f3d0)', display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 1.5 }}>
                      <BusinessIcon sx={{ color: '#059669' }} />
                    </Box>
                    <Typography variant="h3" gutterBottom>Kontrahenci</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Konfiguruj typy kontrahentów i przypisuj ich do użytkowników.
                    </Typography>
                  </CardContent>
                </CardActionArea>
              </Card>
            </>
          )}
        </Box>
      </Box>
    </Fade>
  );
}
