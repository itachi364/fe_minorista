import { CheckField, Field, SelectField } from '../../components/forms.jsx';

export function CatalogAdminPanel({
  definitions,
  selectedCatalogCode,
  setSelectedCatalogCode,
  items,
  form,
  setForm,
  onLoadDefinitions,
  onLoadItems,
  onNew,
  onEdit,
  onSave,
  onToggleActive,
  busy,
  isRoot,
}) {
  const catalogOptions = definitions.map((definition) => ({
    value: definition.code,
    label: definition.label,
  }));
  const selectedDefinition = definitions.find((definition) => definition.code === selectedCatalogCode);
  const canEditGlobal = Boolean(isRoot && selectedDefinition?.globalEditableByRoot);
  const canToggleActivation = Boolean(isRoot ? selectedDefinition?.globalEditableByRoot : selectedDefinition?.companyConfigurable);

  return <section className="catalog-admin stack">
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Catalogos</h1>
          <p className="hint">Administra catalogos desde base de datos. Los codigos tecnicos se conservan para integracion, pero las etiquetas visibles estan en espanol.</p>
        </div>
        <div className="button-row">
          <button className="secondary" type="button" onClick={onLoadDefinitions} disabled={busy}>Cargar catalogos</button>
          <button className="primary" type="button" onClick={onNew} disabled={busy || !selectedCatalogCode || !canEditGlobal}>Nuevo registro</button>
        </div>
      </header>
      <div className="form-grid compact">
        <SelectField label="Catalogo" value={selectedCatalogCode} onChange={setSelectedCatalogCode} options={catalogOptions} disabled={busy || catalogOptions.length === 0} />
        <label>
          Detalle
          <div className="sale-state">
            {selectedDefinition ? `${selectedDefinition.regulatory ? 'Regulatorio' : 'Operativo'} - ${selectedDefinition.companyConfigurable ? 'Configurable por empresa' : 'Global'}` : 'Carga y selecciona un catalogo'}
          </div>
        </label>
      </div>
      <button className="secondary" type="button" onClick={onLoadItems} disabled={busy || !selectedCatalogCode}>Consultar registros</button>
    </section>

    {selectedCatalogCode && <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>{form.editingCode ? 'Actualizar registro' : 'Crear registro'}</h1>
          <p className="hint">{canEditGlobal ? 'El codigo se guarda en ingles o formato tecnico; la etiqueta y descripcion se muestran en espanol.' : 'Este catalogo no permite edicion global desde la interfaz. Usa activacion empresarial cuando aplique.'}</p>
        </div>
        <button className="primary" type="button" onClick={onSave} disabled={busy || !form.code || !form.label || !canEditGlobal}>Guardar registro</button>
      </header>
      <div className="form-grid">
        <Field label="Codigo tecnico" value={form.code} onChange={(value) => setForm({ ...form, code: value })} readOnly={Boolean(form.editingCode) || !canEditGlobal} />
        <Field label="Etiqueta en espanol" value={form.label} onChange={(value) => setForm({ ...form, label: value })} readOnly={!canEditGlobal} />
        <Field label="Descripcion" value={form.description} onChange={(value) => setForm({ ...form, description: value })} readOnly={!canEditGlobal} />
        <Field label="Fuente" value={form.source} onChange={(value) => setForm({ ...form, source: value })} readOnly={!canEditGlobal} />
        <Field label="Version fuente" value={form.sourceVersion} onChange={(value) => setForm({ ...form, sourceVersion: value })} readOnly={!canEditGlobal} />
        <Field label="Orden" value={form.sortOrder} onChange={(value) => setForm({ ...form, sortOrder: value })} type="number" readOnly={!canEditGlobal} />
        <Field label="Vigente desde" value={form.validFrom} onChange={(value) => setForm({ ...form, validFrom: value })} type="date" readOnly={!canEditGlobal} />
        <Field label="Vigente hasta" value={form.validTo} onChange={(value) => setForm({ ...form, validTo: value })} type="date" readOnly={!canEditGlobal} />
        <CheckField label="Catalogo regulatorio" checked={form.regulatory} onChange={(value) => setForm({ ...form, regulatory: value })} disabled={!canEditGlobal} />
      </div>
    </section>}

    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Registros del catalogo</h1>
          <p className="hint">{isRoot ? 'Usa actualizar para editar catalogos globales permitidos y activar o inactivar disponibilidad global.' : 'Activa o inactiva registros configurables para la empresa activa.'}</p>
        </div>
      </header>
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Codigo</th>
              <th>Etiqueta</th>
              <th>Descripcion</th>
              <th>Origen</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 && <tr><td colSpan="6">No hay registros cargados.</td></tr>}
            {items.map((item) => (
              <tr key={`${item.catalogCode}-${item.code}`}>
                <td><code>{item.code}</code></td>
                <td>{item.label}</td>
                <td>{item.description || 'Sin descripcion'}</td>
                <td>{item.source} {item.sourceVersion}</td>
                <td>{item.enabledForCompany === false ? 'Inactivo para empresa' : item.active ? 'Activo' : 'Inactivo'}</td>
                <td>
                  <div className="button-row">
                    <button className="secondary" type="button" onClick={() => onEdit(item)} disabled={busy || !canEditGlobal}>Actualizar</button>
                    <button className="secondary" type="button" onClick={() => onToggleActive(item)} disabled={busy || !canToggleActivation}>
                      {(isRoot ? item.active : item.enabledForCompany !== false) ? 'Inactivar' : 'Activar'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  </section>;
}
