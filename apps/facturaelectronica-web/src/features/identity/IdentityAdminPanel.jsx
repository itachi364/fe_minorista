import { Field, FormPanel, StatusBadge } from '../../components/forms.jsx';
import { moduleLabel, permissionDescription, permissionLabel } from '../../utils/permissionLabels.js';

export function RolesPanel({
  permissions,
  roles,
  form,
  setForm,
  editingRoleId,
  onNew,
  onEdit,
  onSave,
  onToggleActive,
  onTogglePermission,
  busy,
}) {
  const groupedPermissions = permissions.reduce((groups, permission) => {
    const key = permission.module || 'general';
    return { ...groups, [key]: [...(groups[key] || []), permission] };
  }, {});

  return (
    <div className="stack identity-admin">
      <section className="tool-panel identity-overview">
        <header className="panel-header">
          <div>
            <h1>Roles</h1>
            <p>Define roles por empresa con permisos delegables y mantenlos activos solo cuando se usen.</p>
          </div>
          <button className="secondary" disabled={busy} onClick={onNew} type="button">Nuevo rol</button>
        </header>
        <div className="summary-strip">
          <StatusBadge label="Permisos" value={permissions.length || 0} />
          <StatusBadge label="Roles" value={roles.length || 0} />
          <StatusBadge label="Activos" value={roles.filter((role) => role.active !== false).length || 0} />
        </div>
      </section>

      <FormPanel title={editingRoleId ? 'Actualizar rol' : 'Crear rol'} submitLabel={editingRoleId ? 'Actualizar rol' : 'Crear rol'} onSubmit={onSave} busy={busy || form.permissionCodes.length === 0}>
        <div className="form-grid compact">
          <Field label="Nombre del rol" value={form.name} onChange={(value) => setForm({ ...form, name: value })} />
          <Field label="Descripcion" value={form.description} onChange={(value) => setForm({ ...form, description: value })} />
        </div>
        <PermissionPicker groupedPermissions={groupedPermissions} selected={form.permissionCodes} onToggle={onTogglePermission} />
      </FormPanel>

      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Roles disponibles</h1>
            <p>Actualiza o inactiva roles de la empresa seleccionada.</p>
          </div>
        </header>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Rol</th>
                <th>Descripcion</th>
                <th>Permisos</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {roles.map((role) => (
                <tr key={role.id}>
                  <td>{role.name}</td>
                  <td>{role.description || 'Sin descripcion'}</td>
                  <td>{role.permissionCodes?.length || 0}</td>
                  <td>{role.active === false ? 'Inactivo' : 'Activo'}</td>
                  <td>
                    <div className="button-row">
                      <button className="secondary" disabled={busy} onClick={() => onEdit(role)} type="button">Actualizar</button>
                      <button className="secondary" disabled={busy} onClick={() => onToggleActive(role)} type="button">
                        {role.active === false ? 'Activar' : 'Inactivar'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {roles.length === 0 && (
                <tr>
                  <td colSpan="5">No hay roles creados para esta empresa.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export function UsersPanel({
  users,
  roles,
  form,
  setForm,
  editingUserId,
  onNew,
  onEdit,
  onSave,
  onToggleActive,
  busy,
}) {
  const activeRoles = roles.filter((role) => role.active !== false);
  return (
    <div className="stack identity-admin">
      <section className="tool-panel identity-overview">
        <header className="panel-header">
          <div>
            <h1>Usuarios</h1>
            <p>Crea usuarios empresariales, asigna un rol obligatorio y administra su estado.</p>
          </div>
          <button className="secondary" disabled={busy} onClick={onNew} type="button">Nuevo usuario</button>
        </header>
        <div className="summary-strip">
          <StatusBadge label="Usuarios" value={users.length || 0} />
          <StatusBadge label="Activos" value={users.filter((user) => user.status !== 'INACTIVE').length || 0} />
          <StatusBadge label="Roles activos" value={activeRoles.length || 0} />
        </div>
      </section>

      <FormPanel title={editingUserId ? 'Actualizar usuario' : 'Crear usuario'} submitLabel={editingUserId ? 'Actualizar usuario' : 'Crear usuario'} onSubmit={onSave} busy={busy || !form.roleId || (!editingUserId && !form.password)}>
        <div className="form-grid compact">
          <Field label="Nombre completo" value={form.fullName} onChange={(value) => setForm({ ...form, fullName: value })} />
          <Field label="Correo electronico" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
          {!editingUserId && <Field label="Password inicial" value={form.password} onChange={(value) => setForm({ ...form, password: value })} type="password" />}
          <label>
            Rol obligatorio
            <select value={form.roleId} onChange={(event) => setForm({ ...form, roleId: event.target.value })}>
              <option value="">Selecciona un rol</option>
              {activeRoles.map((role) => <option key={role.id} value={role.id}>{role.name}</option>)}
            </select>
          </label>
        </div>
      </FormPanel>

      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Usuarios disponibles</h1>
            <p>Actualiza datos basicos o activa/inactiva accesos empresariales.</p>
          </div>
        </header>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.fullName}</td>
                  <td>{user.email}</td>
                  <td>{user.status === 'INACTIVE' ? 'Inactivo' : 'Activo'}</td>
                  <td>
                    <div className="button-row">
                      <button className="secondary" disabled={busy} onClick={() => onEdit(user)} type="button">Actualizar</button>
                      <button className="secondary" disabled={busy} onClick={() => onToggleActive(user)} type="button">
                        {user.status === 'INACTIVE' ? 'Activar' : 'Inactivar'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan="4">No hay usuarios creados para esta empresa.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

function PermissionPicker({ groupedPermissions, selected, onToggle }) {
  const modules = Object.keys(groupedPermissions).sort();
  if (modules.length === 0) {
    return <p className="hint">No hay permisos disponibles para asignar.</p>;
  }
  return (
    <div className="permission-groups">
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
    </div>
  );
}
