import { DataTable } from '../../components/DataTable.jsx';
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
  const safePermissions = Array.isArray(permissions) ? permissions : [];
  const safeRoles = Array.isArray(roles) ? roles : [];
  const groupedPermissions = safePermissions.reduce((groups, permission) => {
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
          <StatusBadge label="Permisos" value={safePermissions.length || 0} />
          <StatusBadge label="Roles" value={safeRoles.length || 0} />
          <StatusBadge label="Activos" value={safeRoles.filter((role) => role.active !== false).length || 0} />
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
        <DataTable
          columns={['Rol', 'Permisos', 'Estado', 'Acciones']}
          emptyMessage="No hay roles creados para esta empresa."
          pageSize={8}
          rowKey={(_, index) => safeRoles[index]?.id ?? index}
          rows={safeRoles.map((role) => roleRow(role, busy, onEdit, onToggleActive))}
          sectionClassName="embedded-table"
        />
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
  const safeUsers = Array.isArray(users) ? users : [];
  const safeRoles = Array.isArray(roles) ? roles : [];
  const activeRoles = safeRoles.filter((role) => role.active !== false);
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
          <StatusBadge label="Usuarios" value={safeUsers.length || 0} />
          <StatusBadge label="Activos" value={safeUsers.filter((user) => user.status !== 'INACTIVE').length || 0} />
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
        <DataTable
          columns={['Usuario', 'Estado', 'Acciones']}
          emptyMessage="No hay usuarios creados para esta empresa."
          pageSize={8}
          rowKey={(_, index) => safeUsers[index]?.id ?? index}
          rows={safeUsers.map((user) => userRow(user, busy, onEdit, onToggleActive))}
          sectionClassName="embedded-table"
        />
      </section>
    </div>
  );
}

function roleRow(role, busy, onEdit, onToggleActive) {
  const permissionCodes = role.permissionCodes || [];
  return [
    {
      content: <EntityCell title={role.name} subtitle={role.description || 'Sin descripcion'} />,
      searchText: `${role.name} ${role.description || ''}`,
    },
    {
      content: <PermissionSummary permissionCodes={permissionCodes} />,
      searchText: permissionCodes.map((code) => `${code} ${permissionLabel(code)}`).join(' '),
    },
    {
      content: <EntityStatusBadge active={role.active !== false} />,
      searchText: role.active === false ? 'inactivo' : 'activo',
    },
    {
      content: <RowActions active={role.active !== false} busy={busy} onEdit={() => onEdit(role)} onToggle={() => onToggleActive(role)} />,
      searchText: '',
    },
  ];
}

function userRow(user, busy, onEdit, onToggleActive) {
  const active = user.status !== 'INACTIVE';
  return [
    {
      content: <EntityCell title={user.fullName} subtitle={user.email} />,
      searchText: `${user.fullName} ${user.email}`,
    },
    {
      content: <EntityStatusBadge active={active} />,
      searchText: active ? 'activo' : 'inactivo',
    },
    {
      content: <RowActions active={active} busy={busy} onEdit={() => onEdit(user)} onToggle={() => onToggleActive(user)} />,
      searchText: '',
    },
  ];
}

function EntityCell({ title, subtitle }) {
  return (
    <span className="entity-cell">
      <b>{title}</b>
      <small>{subtitle}</small>
    </span>
  );
}

function EntityStatusBadge({ active }) {
  return <span className={active ? 'status-pill active' : 'status-pill inactive'}>{active ? 'Activo' : 'Inactivo'}</span>;
}

function PermissionSummary({ permissionCodes }) {
  if (permissionCodes.length === 0) {
    return <span className="muted-block">Sin permisos</span>;
  }
  const visible = permissionCodes.slice(0, 3);
  const remaining = permissionCodes.length - visible.length;
  return (
    <span className="permission-chip-list">
      {visible.map((code) => <span className="permission-chip" key={code}>{permissionLabel(code)}</span>)}
      {remaining > 0 && <span className="permission-chip muted">+{remaining} mas</span>}
    </span>
  );
}

function RowActions({ active, busy, onEdit, onToggle }) {
  return (
    <div className="table-action-row">
      <button className="secondary compact" disabled={busy} onClick={onEdit} type="button">Actualizar</button>
      <button className={active ? 'secondary compact danger-soft' : 'secondary compact'} disabled={busy} onClick={onToggle} type="button">
        {active ? 'Inactivar' : 'Activar'}
      </button>
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
