import { ActionModal } from '../../components/Modal.jsx';
import { Field, FormPanel, StatusBadge } from '../../components/forms.jsx';
import { moduleLabel, permissionDescription, permissionLabel } from '../../utils/permissionLabels.js';

export function IdentityAdminPanel({ permissions, roles, users, roleForm, setRoleForm, userForm, setUserForm, onLoad, onCreateRole, onCreateUser, onOpenAssignModal, onTogglePermission, busy }) {
  const groupedPermissions = permissions.reduce((groups, permission) => {
    const key = permission.module || 'general';
    return { ...groups, [key]: [...(groups[key] || []), permission] };
  }, {});

  return <div className="stack identity-admin">
    <section className="tool-panel identity-overview">
      <header className="panel-header">
        <div>
          <h1>Usuarios, roles y permisos</h1>
          <p>Administra roles empresariales con permisos delegables y asigna accesos a usuarios.</p>
        </div>
        <div className="button-row">
          <button className="secondary" disabled={busy} onClick={onLoad} type="button">Cargar permisos y roles</button>
          <button className="primary" disabled={busy || roles.length === 0} onClick={onOpenAssignModal} type="button">Asignar rol</button>
        </div>
      </header>
      <div className="summary-strip">
        <StatusBadge label="Permisos" value={permissions.length || 0} />
        <StatusBadge label="Roles" value={roles.length || 0} />
        <StatusBadge label="Usuarios" value={users.length || 0} />
      </div>
    </section>

    <div className="split identity-split">
      <FormPanel title="Rol empresarial" submitLabel="Crear rol" onSubmit={onCreateRole} busy={busy || roleForm.permissionCodes.length === 0}>
        <div className="form-grid compact">
          <Field label="Nombre del rol" value={roleForm.name} onChange={(value) => setRoleForm({ ...roleForm, name: value })} />
          <Field label="Descripcion" value={roleForm.description} onChange={(value) => setRoleForm({ ...roleForm, description: value })} />
        </div>
        <PermissionPicker groupedPermissions={groupedPermissions} selected={roleForm.permissionCodes} onToggle={onTogglePermission} />
      </FormPanel>

      <FormPanel title="Usuario empresarial" submitLabel="Crear usuario" onSubmit={onCreateUser} busy={busy}>
        <div className="form-grid compact">
          <Field label="Nombre completo" value={userForm.fullName} onChange={(value) => setUserForm({ ...userForm, fullName: value })} />
          <Field label="Correo electronico" value={userForm.email} onChange={(value) => setUserForm({ ...userForm, email: value })} type="email" />
          <Field label="Password inicial" value={userForm.password} onChange={(value) => setUserForm({ ...userForm, password: value })} type="password" />
        </div>
      </FormPanel>
    </div>
  </div>;
}

function PermissionPicker({ groupedPermissions, selected, onToggle }) {
  const modules = Object.keys(groupedPermissions).sort();
  if (modules.length === 0) {
    return <p className="hint">Carga el catalogo de permisos para seleccionar permisos delegables.</p>;
  }
  return <div className="permission-groups">
    {modules.map((module) => (
      <section className="permission-group" key={module}>
        <h2>{moduleLabel(module)}</h2>
        <div className="permission-list">
          {groupedPermissions[module].map((permission) => (
            <label className="permission-option" key={permission.code}>
              <input checked={selected.includes(permission.code)} onChange={() => onToggle(permission.code)} type="checkbox" />
              <span>
                <b>{permissionLabel(permission.code)}</b>
                <small>{permissionDescription(permission)}</small>
              </span>
            </label>
          ))}
        </div>
      </section>
    ))}
  </div>;
}

export function RoleAssignmentModal({ users, roles, form, setForm, searchEmail, setSearchEmail, onSearch, onSubmit, onToggleRole, onClose, busy }) {
  const selectedUser = users.find((user) => user.id === form.userId);
  return <ActionModal title="Asignar rol empresarial" onClose={onClose}>
    <div className="form-grid compact modal-form-grid">
      <Field label="Buscar por correo" value={searchEmail} onChange={setSearchEmail} type="email" />
      <label>
        Usuario
        <select value={form.userId} onChange={(event) => setForm({ ...form, userId: event.target.value })}>
          <option value="">Seleccione un usuario</option>
          {users.map((user) => <option key={user.id} value={user.id}>{user.email} - {user.fullName}</option>)}
        </select>
      </label>
      <label>
        Rol empresarial
        <select value={form.roleIds[0] || ''} onChange={(event) => setForm({ ...form, roleIds: event.target.value ? [event.target.value] : [] })}>
          <option value="">Seleccione un rol</option>
          {roles.map((role) => <option key={role.id} value={role.id}>{role.name}</option>)}
        </select>
      </label>
      <Field label="Usuario ID que viaja al backend" value={selectedUser?.id || form.userId} onChange={() => {}} readOnly />
    </div>
    <div className="role-list compact-role-list">
      {roles.map((role) => (
        <label className="role-option" key={role.id}>
          <input checked={form.roleIds.includes(role.id)} onChange={() => onToggleRole(role.id)} type="checkbox" />
          <span><b>{role.name}</b><small>{role.description || 'Sin descripcion'} - {role.permissionCodes?.length || 0} permisos</small></span>
        </label>
      ))}
    </div>
    <div className="modal-actions">
      <button className="secondary" disabled={busy} onClick={onSearch} type="button">Buscar usuario</button>
      <button className="secondary" onClick={onClose} type="button">Cancelar</button>
      <button className="primary" disabled={busy || !form.userId || form.roleIds.length === 0} onClick={onSubmit} type="button">Asignar rol</button>
    </div>
  </ActionModal>;
}
