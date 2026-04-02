import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Box from '@mui/material/Box';
import useMediaQuery from '@mui/material/useMediaQuery';
import { useTheme } from '@mui/material/styles';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import { Sidebar } from './Sidebar';

const DRAWER_WIDTH = 270;

export function Layout() {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleToggleSidebar = () => {
    setMobileOpen((prev) => !prev);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'row', height: '100vh' }}>
      <Sidebar
        mobileOpen={mobileOpen}
        onClose={() => setMobileOpen(false)}
      />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          overflowY: 'auto',
          p: '2.5rem',
          backgroundColor: '#FDFBD4',
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { md: `${DRAWER_WIDTH}px` },
          position: 'relative',
        }}
      >
        {isMobile && (
          <IconButton
            onClick={handleToggleSidebar}
            sx={{
              position: 'fixed',
              top: 12,
              left: 12,
              zIndex: (t) => t.zIndex.drawer + 1,
              color: '#38240D',
              bgcolor: '#FDFBD4',
              boxShadow: 2,
              '&:hover': { bgcolor: '#f0ecc0' },
            }}
          >
            <MenuIcon />
          </IconButton>
        )}

        <Box sx={{ mt: isMobile ? 5 : 0 }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
