import { createBrowserRouter } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { DashboardPage } from './pages/DashboardPage';
import { ContractorTypesPage } from './pages/ContractorTypesPage';
import { UserContractorTypesPage } from './pages/UserContractorTypesPage';
import { AccessDeniedPage } from './pages/AccessDeniedPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <DashboardPage /> },
      {
        path: 'admin/contractor-types',
        element: (
          <ProtectedRoute requiredRole="Administrator">
            <ContractorTypesPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'admin/user-contractor-types',
        element: (
          <ProtectedRoute requiredRole="Administrator">
            <UserContractorTypesPage />
          </ProtectedRoute>
        ),
      },
      { path: 'access-denied', element: <AccessDeniedPage /> },
    ],
  },
]);
