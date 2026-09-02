import { ActionModal } from '../../components/Modal.jsx';
import { Field } from '../../components/forms.jsx';
import { companyLabel } from '../../utils/company.js';

const assetFields = [
  { purpose: 'MAIN_LOGO', label: 'Logo principal', hint: 'Usado como identificador general de la empresa.' },
  { purpose: 'HEADER_LOGO', label: 'Logo superior', hint: 'Aparece en la parte superior derecha de la aplicacion.' },
  { purpose: 'LOGIN_LOGO', label: 'Logo de login', hint: 'Se usa cuando exista contexto empresarial disponible.' },
  { purpose: 'FAVICON', label: 'Favicon', hint: 'Icono del navegador para la empresa activa.' },
];

export function CompanyBrandingPanel({ form, setForm, branding, onSave, onUploadAsset, busy, disabled }) {
  const hasAssets = Boolean(branding?.mainLogoUrl || branding?.headerLogoUrl || branding?.loginLogoUrl || branding?.faviconUrl);

  return (
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Marca empresarial</h1>
          <p>El color principal aplica a botones y acentos de navegacion. El color acento resalta estados, focos y detalles visuales.</p>
        </div>
        <button className="primary" disabled={busy || disabled} onClick={onSave} type="button">Guardar marca</button>
      </header>
      <div className="form-grid">
        <Field label="Nombre visible" value={form.displayName} onChange={(value) => setForm({ ...form, displayName: value })} placeholder="Ej. Tienda Norte" disabled={disabled} />
        <Field label="Color principal" value={form.primaryColor || '#1f78a8'} onChange={(value) => setForm({ ...form, primaryColor: value })} type="color" disabled={disabled} />
        <Field label="Color acento" value={form.accentColor || '#2a7c61'} onChange={(value) => setForm({ ...form, accentColor: value })} type="color" disabled={disabled} />
      </div>
      <BrandingAssets branding={branding} hasAssets={hasAssets} onUploadAsset={onUploadAsset} busy={busy} disabled={disabled} />
    </section>
  );
}

export function CompanyBrandingModal({ form, setForm, branding, company, companyId, onSave, onUploadAsset, onClose, busy }) {
  const hasAssets = Boolean(branding?.mainLogoUrl || branding?.headerLogoUrl || branding?.loginLogoUrl || branding?.faviconUrl);
  return (
    <ActionModal title="Crear marca empresarial" onClose={onClose} size="wide">
      <div className="form-grid compact modal-form-grid">
        <Field label="Empresa" value={companyLabel(company) || companyId} onChange={() => {}} readOnly />
      </div>
      <section className="modal-section">
        <header className="modal-section-header">
          <div>
            <h2>Marca empresarial</h2>
            <p className="hint">El color principal aplica a botones y acentos de navegacion. El color acento resalta estados, focos y detalles visuales.</p>
          </div>
          <button className="primary" disabled={busy} onClick={onSave} type="button">Guardar marca</button>
        </header>
        <div className="form-grid">
          <Field label="Nombre visible" value={form.displayName} onChange={(value) => setForm({ ...form, displayName: value })} placeholder="Ej. Tienda Norte" />
          <Field label="Color principal" value={form.primaryColor || '#1f78a8'} onChange={(value) => setForm({ ...form, primaryColor: value })} type="color" />
          <Field label="Color acento" value={form.accentColor || '#2a7c61'} onChange={(value) => setForm({ ...form, accentColor: value })} type="color" />
        </div>
        <BrandingAssets branding={branding} hasAssets={hasAssets} onUploadAsset={onUploadAsset} busy={busy} disabled={false} />
      </section>
      <div className="modal-actions">
        <button className="secondary" onClick={onClose} type="button">Cancelar</button>
      </div>
    </ActionModal>
  );
}

function BrandingAssets({ branding, hasAssets, onUploadAsset, busy, disabled }) {
  return (
    <>
      <div className="asset-grid">
        {assetFields.map((asset) => (
          <label className="asset-upload-card" key={asset.purpose}>
            <span>
              <b>{asset.label}</b>
              <small>{asset.hint}</small>
            </span>
            <input
              accept="image/png,image/jpeg,image/webp,image/x-icon,image/vnd.microsoft.icon"
              disabled={busy || disabled}
              onChange={(event) => {
                const file = event.target.files?.[0];
                if (file) {
                  onUploadAsset(asset.purpose, file);
                }
                event.target.value = '';
              }}
              type="file"
            />
          </label>
        ))}
      </div>
      {hasAssets && (
        <div className="branding-preview-grid">
          {branding.mainLogoUrl && <BrandingPreview label="Principal" url={branding.mainLogoUrl} />}
          {branding.headerLogoUrl && <BrandingPreview label="Superior" url={branding.headerLogoUrl} />}
          {branding.loginLogoUrl && <BrandingPreview label="Login" url={branding.loginLogoUrl} />}
          {branding.faviconUrl && <BrandingPreview label="Favicon" url={branding.faviconUrl} compact />}
        </div>
      )}
    </>
  );
}

function BrandingPreview({ label, url, compact = false }) {
  return (
    <div className={compact ? 'branding-preview compact' : 'branding-preview'}>
      <span>{label}</span>
      <img alt={`Marca ${label.toLowerCase()}`} src={url} />
    </div>
  );
}
