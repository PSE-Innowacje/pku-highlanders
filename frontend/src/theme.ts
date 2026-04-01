import { createTheme, alpha } from '@mui/material/styles';

const palette = {
  primary: '#713600',
  secondary: '#C05800',
  light: '#FDFBD4',
  text: '#38240D',
};

const theme = createTheme({
  palette: {
    primary: { main: palette.primary, dark: '#5a2b00', contrastText: '#fff' },
    secondary: { main: palette.secondary, contrastText: '#fff' },
    background: { default: palette.light, paper: '#ffffff' },
    text: { primary: palette.text, secondary: '#6b5340' },
    error: { main: '#dc2626' },
    warning: { main: '#d97706' },
    success: { main: '#059669' },
  },
  typography: {
    fontFamily: '"Outfit", sans-serif',
    h1: { fontWeight: 700, fontSize: '1.75rem', letterSpacing: '-0.02em' },
    h2: { fontWeight: 600, fontSize: '1.35rem', letterSpacing: '-0.01em' },
    h3: { fontWeight: 600, fontSize: '1.1rem' },
    body1: { fontSize: '0.9rem' },
    body2: { fontSize: '0.825rem' },
    button: { fontWeight: 600, textTransform: 'none' as const },
  },
  shape: { borderRadius: 10 },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { fontFamily: '"Outfit", sans-serif' },
        '::-webkit-scrollbar': { width: 6, height: 6 },
        '::-webkit-scrollbar-track': { background: 'transparent' },
        '::-webkit-scrollbar-thumb': { background: '#c4a882', borderRadius: 3 },
        '::-webkit-scrollbar-thumb:hover': { background: '#a07850' },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 8, padding: '6px 16px', fontWeight: 600 },
        sizeSmall: { padding: '4px 10px', fontSize: '0.8rem' },
        containedPrimary: {
          background: `linear-gradient(135deg, ${palette.primary}, ${palette.secondary})`,
          '&:hover': { background: `linear-gradient(135deg, ${palette.secondary}, ${palette.primary})` },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-head': {
            background: `linear-gradient(180deg, #f7f0e0, #efe6d0)`,
            fontWeight: 700,
            fontSize: '0.7rem',
            textTransform: 'uppercase' as const,
            letterSpacing: '0.06em',
            color: '#6b5340',
          },
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          transition: 'background 0.15s',
          '&:hover': { backgroundColor: alpha(palette.primary, 0.04) },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: { padding: '10px 14px', fontSize: '0.85rem', borderColor: '#e8e0d4' },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: { borderRadius: 14, padding: '8px' },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600, fontSize: '0.75rem' },
      },
    },
    MuiTextField: {
      defaultProps: { size: 'small' as const, variant: 'outlined' as const },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-notchedOutline': { borderColor: '#d4c4a8' },
          '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: palette.primary },
          backgroundColor: '#fff',
        },
      },
    },
    MuiTooltip: {
      defaultProps: { arrow: true },
    },
  },
});

export default theme;
