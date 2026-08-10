import { ActionModal } from '../../components/Modal.jsx';
import { Field } from '../../components/forms.jsx';
import { companyLabel } from '../../utils/company.js';

export function AdminModal({ form, setForm, activeCompany, activeCompanyId, onSubmit, onClose, busy }) {
  return <ActionModal title="Crear administrador inicial" onClose={onClose}>
    <div className="form-grid compact modal-form-grid">
      <Field label="Empresa" value={companyLabel(activeCompany) || activeCompanyId} onChange={() => {}} readOnly />
      <Field label="Nombre completo" value={form.fullName} onChange={(value) => setForm({ ...form, fullName: value })} />
      <Field label="Correo electronico" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
      <Field label="Password inicial" value={form.password} onChange={(value) => setForm({ ...form, password: value })} type="password" />
      <Field label="Rol inicial" value="OWNER - Administrador empresarial" onChange={() => {}} readOnly />
    </div>
    <div className="modal-actions">
      <button className="secondary" onClick={onClose} type="button">Cancelar</button>
      <button className="primary" disabled={busy} onClick={onSubmit} type="button">Crear administrador</button>
    </div>
  </ActionModal>;
}
