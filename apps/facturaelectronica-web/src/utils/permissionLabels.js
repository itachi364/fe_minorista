import i18n from '../i18n/index.js';

export function moduleLabel(module) {
  const key = String(module || 'general').toLowerCase();
  return i18n.t(`modules.${key}`, { defaultValue: titleFromCode(module || 'general') });
}

export function permissionLabel(code) {
  return i18n.t(`permissions.${code}.label`, { defaultValue: titleFromCode(code) });
}

export function permissionDescription(permission) {
  return i18n.t(`permissions.${permission.code}.description`, {
    defaultValue: permission.description || 'Permiso empresarial configurable.',
  });
}

export function roleLabel(roleCode) {
  return i18n.t(`roles.${roleCode}`, { defaultValue: titleFromCode(roleCode) });
}

function titleFromCode(value) {
  return String(value || '')
    .toLowerCase()
    .split(/[_\s-]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}
