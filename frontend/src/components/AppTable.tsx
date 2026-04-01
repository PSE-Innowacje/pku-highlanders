import { useState, useMemo, useCallback } from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TableSortLabel from '@mui/material/TableSortLabel';
import TextField from '@mui/material/TextField';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import InputAdornment from '@mui/material/InputAdornment';
import SearchIcon from '@mui/icons-material/Search';

interface Column<T> {
  id: string;
  label: string;
  minWidth?: number;
  maxWidth?: number;
  sortable?: boolean;
  filterable?: boolean;
  render?: (row: T) => React.ReactNode;
  getValue?: (row: T) => string | number;
}

interface AppTableProps<T> {
  columns: Column<T>[];
  rows: T[];
  getRowKey: (row: T) => string | number;
  maxHeight?: number;
  onRowClick?: (row: T) => void;
  emptyMessage?: string;
}

type SortDirection = 'asc' | 'desc' | null;

const cellEllipsisStyle = (maxWidth?: number) => ({
  whiteSpace: 'nowrap' as const,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  ...(maxWidth != null && { maxWidth }),
});

function getCellValue<T>(row: T, col: Column<T>): string | number {
  if (col.getValue) return col.getValue(row);
  const value = (row as Record<string, unknown>)[col.id];
  return value == null ? '' : (typeof value === 'number' ? value : String(value));
}

function AppTable<T>({
  columns,
  rows,
  getRowKey,
  maxHeight = 600,
  onRowClick,
  emptyMessage = 'Brak danych',
}: AppTableProps<T>) {
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<SortDirection>(null);
  const [globalFilter, setGlobalFilter] = useState('');

  const handleSort = useCallback((columnId: string) => {
    setSortColumn((prevCol) => {
      if (prevCol !== columnId) {
        setSortDirection('asc');
        return columnId;
      }
      setSortDirection((prevDir) => {
        if (prevDir === 'asc') return 'desc';
        return null;
      });
      return prevCol;
    });
  }, []);

  const effectiveSortColumn = sortDirection === null ? null : sortColumn;

  const filterableColumns = useMemo(
    () => columns.filter((col) => col.filterable !== false),
    [columns]
  );

  const processedRows = useMemo(() => {
    let result = [...rows];

    // Global filter — match any filterable column
    const term = globalFilter.trim().toLowerCase();
    if (term.length > 0 && filterableColumns.length > 0) {
      result = result.filter((row) =>
        filterableColumns.some((col) => {
          const cellVal = String(getCellValue(row, col)).toLowerCase();
          return cellVal.includes(term);
        }),
      );
    }

    // Sort
    if (effectiveSortColumn && sortDirection) {
      const col = columns.find((c) => c.id === effectiveSortColumn);
      if (col) {
        result.sort((a, b) => {
          const aVal = getCellValue(a, col);
          const bVal = getCellValue(b, col);

          let cmp: number;
          if (typeof aVal === 'number' && typeof bVal === 'number') {
            cmp = aVal - bVal;
          } else {
            cmp = String(aVal).localeCompare(String(bVal), 'pl', { sensitivity: 'base' });
          }
          return sortDirection === 'asc' ? cmp : -cmp;
        });
      }
    }

    return result;
  }, [rows, globalFilter, filterableColumns, effectiveSortColumn, sortDirection, columns]);

  const hasFilterable = filterableColumns.length > 0;

  return (
    <Paper variant="outlined">
      {hasFilterable && (
        <Box sx={{ p: 1.5, borderBottom: '1px solid', borderColor: 'divider' }}>
          <TextField
            size="small"
            placeholder="Szukaj..."
            value={globalFilter}
            onChange={(e) => setGlobalFilter(e.target.value)}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
              },
            }}
            sx={{ minWidth: 260 }}
          />
        </Box>
      )}
      <TableContainer sx={{ maxHeight }}>
        <Table stickyHeader size="small">
          <TableHead>
            <TableRow>
              {columns.map((col) => {
                const isSortable = col.sortable !== false;
                const isActive = effectiveSortColumn === col.id && sortDirection !== null;

                return (
                  <TableCell
                    key={col.id}
                    sx={{
                      minWidth: col.minWidth,
                      fontWeight: 700,
                      ...cellEllipsisStyle(col.maxWidth),
                      zIndex: 3,
                    }}
                    sortDirection={isActive ? sortDirection! : false}
                  >
                    {isSortable ? (
                      <TableSortLabel
                        active={isActive}
                        direction={isActive ? sortDirection! : 'asc'}
                        onClick={() => handleSort(col.id)}
                      >
                        {col.label}
                      </TableSortLabel>
                    ) : (
                      col.label
                    )}
                  </TableCell>
                );
              })}
            </TableRow>
          </TableHead>

          <TableBody>
            {processedRows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 4 }}>
                  <Typography color="text.secondary">{emptyMessage}</Typography>
                </TableCell>
              </TableRow>
            ) : (
              processedRows.map((row) => (
                <TableRow
                  key={getRowKey(row)}
                  hover
                  onClick={onRowClick ? () => onRowClick(row) : undefined}
                  sx={onRowClick ? { cursor: 'pointer' } : undefined}
                >
                  {columns.map((col) => (
                    <TableCell
                      key={col.id}
                      sx={{
                        minWidth: col.minWidth,
                        ...cellEllipsisStyle(col.maxWidth),
                      }}
                    >
                      {col.render ? col.render(row) : String(getCellValue(row, col))}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}

export default AppTable;
export type { Column, AppTableProps };
