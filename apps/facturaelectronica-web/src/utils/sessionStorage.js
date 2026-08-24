export const SESSION_TIMEOUT_MS = 5 * 60 * 1000;
const SESSION_STORAGE_KEY = 'facturaelectronica.session.v1';
const SENSITIVE_SESSION_KEYS = ['accessToken', 'refreshToken', 'idToken'];

function isCookieBackedSession(session) {
  return session?.authMode === 'cognito' || session?.cookieSession === true;
}

export function sanitizeSessionSnapshot(snapshot) {
  if (!snapshot?.session) {
    return null;
  }
  const session = { ...snapshot.session };
  if (isCookieBackedSession(session)) {
    SENSITIVE_SESSION_KEYS.forEach((key) => {
      delete session[key];
    });
  }
  return { ...snapshot, session };
}

export function loadStoredSession(now = Date.now()) {
  try {
    const raw = window.sessionStorage.getItem(SESSION_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const snapshot = JSON.parse(raw);
    const sanitized = sanitizeSessionSnapshot(snapshot);
    const cookieBacked = isCookieBackedSession(sanitized?.session);
    if (!sanitized?.session || (!cookieBacked && !sanitized.session.accessToken) || !sanitized.lastActivityAt) {
      clearStoredSession();
      return null;
    }
    if (now - sanitized.lastActivityAt >= SESSION_TIMEOUT_MS) {
      clearStoredSession();
      return null;
    }
    return sanitized;
  } catch {
    clearStoredSession();
    return null;
  }
}

export function saveStoredSession(snapshot) {
  const sanitized = sanitizeSessionSnapshot(snapshot);
  const cookieBacked = isCookieBackedSession(sanitized?.session);
  if (!sanitized?.session || (!cookieBacked && !sanitized.session.accessToken)) {
    clearStoredSession();
    return;
  }
  window.sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(sanitized));
}

export function clearStoredSession() {
  window.sessionStorage.removeItem(SESSION_STORAGE_KEY);
}
