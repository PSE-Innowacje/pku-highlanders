import { Link } from 'react-router-dom';

export function AccessDeniedPage() {
  return (
    <div className="access-denied">
      <div className="access-denied-icon">&#9888;</div>
      <h1>Brak dostępu</h1>
      <p>Nie masz uprawnień do wyświetlenia tej strony.</p>
      <Link to="/">Powrót do strony głównej</Link>
    </div>
  );
}
