import { NavLink } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function Sidebar() {
  const { isAdmin, username, logout } = useAuth();

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h2>PKU</h2>
      </div>

      <nav className="sidebar-nav">
        <NavLink to="/" end className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Strona główna
        </NavLink>

        {isAdmin && (
          <div className="nav-section">
            <span className="nav-section-title">Administracja</span>
            <NavLink to="/admin/contractor-types" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
              Typy kontrahentów
            </NavLink>
            <NavLink to="/admin/user-contractor-types" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
              Przypisanie typów
            </NavLink>
          </div>
        )}
      </nav>

      <div className="sidebar-footer">
        <span className="sidebar-user">{username}</span>
        <button className="btn btn-sm" onClick={logout}>Wyloguj</button>
      </div>
    </aside>
  );
}
