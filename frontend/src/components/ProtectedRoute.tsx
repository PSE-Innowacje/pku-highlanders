import { type ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function ProtectedRoute({ requiredRole, children }: { requiredRole: string; children: ReactNode }) {
  const { roles } = useAuth();
  if (!roles.includes(requiredRole)) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}
