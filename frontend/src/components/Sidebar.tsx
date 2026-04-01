import { useLocation, useNavigate } from 'react-router-dom';
import Drawer from '@mui/material/Drawer';
import Box from '@mui/material/Box';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import DashboardIcon from '@mui/icons-material/Dashboard';
import AssignmentLateIcon from '@mui/icons-material/AssignmentLate';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import DescriptionIcon from '@mui/icons-material/Description';
import BusinessIcon from '@mui/icons-material/Business';
import PeopleIcon from '@mui/icons-material/People';
import LogoutIcon from '@mui/icons-material/Logout';
import { useAuth } from '../hooks/useAuth';

const DRAWER_WIDTH = 270;

interface SidebarProps {
  mobileOpen: boolean;
  onClose: () => void;
}

const drawerBackground = 'linear-gradient(180deg, #2a1800 0%, #38240D 50%, #2a1800 100%)';

const navItemSx = (isActive: boolean) => ({
  color: 'rgba(255,255,255,0.85)',
  borderLeft: isActive ? '3px solid #C05800' : '3px solid transparent',
  backgroundColor: isActive ? 'rgba(192,88,0,0.3)' : 'transparent',
  borderRadius: 1,
  mx: 1,
  mb: 0.5,
  '&:hover': {
    backgroundColor: isActive ? 'rgba(192,88,0,0.35)' : 'rgba(255,255,255,0.06)',
  },
});

export function Sidebar({ mobileOpen, onClose }: SidebarProps) {
  const { isAdmin, isKontrahent, displayName, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const isActive = (path: string) =>
    path === '/' ? location.pathname === '/' : location.pathname.startsWith(path);

  const handleNav = (path: string) => {
    navigate(path);
    onClose();
  };

  const drawerContent = (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        background: drawerBackground,
      }}
    >
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, px: 2.5, py: 2.5 }}>
        <Box
          component="img"
          src="/snoopy.png"
          alt="PKU Highlanders"
          sx={{ width: 40, height: 40, borderRadius: '50%' }}
        />
        <Box>
          <Typography
            variant="subtitle1"
            sx={{ color: '#fff', fontWeight: 700, lineHeight: 1.2 }}
          >
            PKU Highlanders
          </Typography>
          <Typography
            variant="caption"
            sx={{ color: 'rgba(255,255,255,0.55)' }}
          >
            System rozliczeniowy
          </Typography>
        </Box>
      </Box>

      {/* Navigation */}
      <Box sx={{ flexGrow: 1, overflowY: 'auto', mt: 1 }}>
        <List disablePadding>
          <ListItemButton
            onClick={() => handleNav('/')}
            sx={navItemSx(isActive('/'))}
          >
            <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>
              <DashboardIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText primary="Home" />
          </ListItemButton>

          {isKontrahent && (
            <>
              <Typography
                variant="overline"
                sx={{
                  color: 'rgba(255,255,255,0.4)',
                  px: 2.5,
                  pt: 2,
                  pb: 0.5,
                  display: 'block',
                  fontSize: '0.65rem',
                  letterSpacing: '0.1em',
                }}
              >
                PKU Rozliczenia
              </Typography>

              <ListItemButton
                onClick={() => handleNav('/declarations/pending')}
                sx={navItemSx(isActive('/declarations/pending'))}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>
                  <AssignmentLateIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText primary="Niezłożone" />
              </ListItemButton>

              <ListItemButton
                onClick={() => handleNav('/declarations/submitted')}
                sx={navItemSx(isActive('/declarations/submitted'))}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>
                  <AssignmentTurnedInIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText primary="Złożone" />
              </ListItemButton>
            </>
          )}

          {isAdmin && (
            <>
              <Typography
                variant="overline"
                sx={{
                  color: 'rgba(255,255,255,0.4)',
                  px: 2.5,
                  pt: 2,
                  pb: 0.5,
                  display: 'block',
                  fontSize: '0.65rem',
                  letterSpacing: '0.1em',
                }}
              >
                Administracja
              </Typography>

              <ListItemButton
                onClick={() => handleNav('/admin/declaration-types')}
                sx={navItemSx(isActive('/admin/declaration-types'))}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>
                  <DescriptionIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText primary="Typy oświadczeń" />
              </ListItemButton>

              <ListItemButton
                onClick={() => handleNav('/admin/contractor-types')}
                sx={navItemSx(isActive('/admin/contractor-types'))}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>
                  <BusinessIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText primary="Typy kontrahentów" />
              </ListItemButton>

              <ListItemButton
                onClick={() => handleNav('/admin/user-contractor-types')}
                sx={navItemSx(isActive('/admin/user-contractor-types'))}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>
                  <PeopleIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText primary="Przypisanie typów" />
              </ListItemButton>
            </>
          )}
        </List>
      </Box>

      {/* Footer */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          px: 2.5,
          py: 2,
          borderTop: '1px solid rgba(255,255,255,0.1)',
        }}
      >
        <Typography
          variant="body2"
          sx={{ color: 'rgba(255,255,255,0.7)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
        >
          {displayName}
        </Typography>
        <IconButton onClick={logout} size="small" sx={{ color: 'rgba(255,255,255,0.6)', '&:hover': { color: '#fff' } }}>
          <LogoutIcon fontSize="small" />
        </IconButton>
      </Box>
    </Box>
  );

  return (
    <>
      {/* Mobile drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onClose}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            border: 'none',
          },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Desktop drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            border: 'none',
          },
        }}
        open
      >
        {drawerContent}
      </Drawer>
    </>
  );
}
