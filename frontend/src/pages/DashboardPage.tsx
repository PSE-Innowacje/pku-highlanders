import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { fetchCurrentUserContractorData, type CurrentUserContractorData } from '../api/kontrahentUsers';

export function DashboardPage() {
  const { displayName, roles, isAdmin, isKontrahent } = useAuth();
  const navigate = useNavigate();
  const [contractorData, setContractorData] = useState<CurrentUserContractorData | null>(null);

  const displayRoles = roles.filter(r => r === 'Administrator' || r === 'Kontrahent');

  useEffect(() => {
    if (isKontrahent) {
      fetchCurrentUserContractorData()
        .then(setContractorData)
        .catch(() => {});
    }
  }, [isKontrahent]);

  return (
    <div>
      <div className="dashboard-welcome">
        <h1>Witaj, {contractorData?.firstName && contractorData?.lastName
          ? `${contractorData.firstName} ${contractorData.lastName}`
          : displayName}</h1>
        <p>
          {displayRoles.length > 0
            ? `Twoje role: ${displayRoles.join(', ')}`
            : 'Brak przypisanych ról'}
        </p>
        {isKontrahent && contractorData && (
          <div style={{ marginTop: '8px' }}>
            {contractorData.agreementNumber && (
              <p style={{ margin: '4px 0' }}>
                <strong>Numer umowy:</strong> {contractorData.agreementNumber}
              </p>
            )}
            {contractorData.contractorFullName && (
              <p style={{ margin: '4px 0' }}>
                <strong>Nazwa kontrahenta:</strong> {contractorData.contractorFullName}
              </p>
            )}
          </div>
        )}
      </div>

      <div className="dashboard-cards">
        {isKontrahent && (
          <div className="dashboard-card" onClick={() => navigate('/declarations')} style={{ cursor: 'pointer' }}>
            <div className="dashboard-card-icon purple">&#9993;</div>
            <h3>Oświadczenia</h3>
            <p>Przeglądaj, wypełniaj i wysyłaj swoje oświadczenia rozliczeniowe.</p>
          </div>
        )}

        {isAdmin && (
          <>
            <div className="dashboard-card" onClick={() => navigate('/admin/declaration-types')} style={{ cursor: 'pointer' }}>
              <div className="dashboard-card-icon blue">&#9881;</div>
              <h3>Typy oświadczeń</h3>
              <p>Zarządzaj typami oświadczeń i ich harmonogramami składania.</p>
            </div>
            <div className="dashboard-card" onClick={() => navigate('/admin/contractor-types')} style={{ cursor: 'pointer' }}>
              <div className="dashboard-card-icon green">&#9733;</div>
              <h3>Kontrahenci</h3>
              <p>Konfiguruj typy kontrahentów i przypisuj ich do użytkowników.</p>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
