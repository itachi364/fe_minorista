import { StatusBadge } from '../../components/forms.jsx';
import { companyLabel } from '../../utils/company.js';

export function CompanySessionPanel({ accesses, companies, activeCompanyId, activeAccess, license, session, isRoot, onCompanyChange, onLogout, busy }) {
  if (isRoot) {
    return (
      <section className="top-panel app-header-panel root-header-panel">
        <div>
          <h1>Panel global</h1>
          <p>{session.fullName} - {session.email}</p>
        </div>
        <label>
          Empresa activa
          <select value={activeCompanyId} onChange={(event) => onCompanyChange(event.target.value)} disabled={busy || companies.length === 0}>
            <option value="">Seleccione una empresa</option>
            {companies.map((company) => <option key={company.id} value={company.id}>{companyLabel(company)}</option>)}
          </select>
        </label>
        <div className="status-row">
          <StatusBadge label="Alcance" value="PLATAFORMA" tone="ok" />
          <StatusBadge label="Rol" value="ROOT" />
        </div>
        <button className="secondary" onClick={onLogout} type="button">Cerrar sesion</button>
      </section>
    );
  }

  return (
    <section className="top-panel app-header-panel">
      <div>
        <h1>Empresa activa</h1>
        <p>{session.fullName} - {session.email}</p>
      </div>
      <label>
        Empresa del usuario
        <select value={activeCompanyId} onChange={(event) => onCompanyChange(event.target.value)} disabled={!accesses.length || busy}>
          {accesses.map((access) => <option key={access.companyId} value={access.companyId}>{access.companyId}</option>)}
        </select>
      </label>
      <div className="status-row">
        <StatusBadge label="Licencia" value={license?.allowed ? 'ACTIVA' : license?.status || 'SIN VALIDAR'} tone={license?.allowed ? 'ok' : 'warn'} />
        <StatusBadge label="Roles" value={activeAccess?.roles?.join(', ') || 'N/A'} />
      </div>
      <button className="secondary" onClick={onLogout} type="button">Cerrar sesion</button>
    </section>
  );
}
