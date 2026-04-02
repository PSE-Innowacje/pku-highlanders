import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { DashboardPage } from './pages/DashboardPage';
import { ContractorTypesPage } from './pages/ContractorTypesPage';
import { UserContractorTypesPage } from './pages/UserContractorTypesPage';
import { DeclarationTypesPage } from './pages/DeclarationTypesPage';
import { DeclarationTypePreviewPage } from './pages/DeclarationTypePreviewPage';
import { DeclarationsDashboardPage } from './pages/DeclarationsDashboardPage';
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
      {
        path: 'admin/declaration-types',
        element: (
          <ProtectedRoute requiredRole="Administrator">
            <DeclarationTypesPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'admin/declaration-types/:code',
        element: (
          <ProtectedRoute requiredRole="Administrator">
            <DeclarationTypePreviewPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'declarations',
        element: <Navigate to="/declarations/pending" replace />,
      },
      {
        path: 'declarations/pending',
        element: (
          <ProtectedRoute requiredRole="Kontrahent">
            <DeclarationsDashboardPage filter="pending" />
          </ProtectedRoute>
        ),
      },
      {
        path: 'declarations/submitted',
        element: (
          <ProtectedRoute requiredRole="Kontrahent">
            <DeclarationsDashboardPage filter="submitted" />
          </ProtectedRoute>
        ),
      },
      { path: 'access-denied', element: <AccessDeniedPage /> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
]);
