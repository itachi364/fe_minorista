export const SESSION_TIMEOUT_MS = 5 * 60 * 1000;
const SESSION_STORAGE_KEY = 'facturaelectronica.session.v1';

export function loadStoredSession(now = Date.now()) {
  try {
    const raw = window.sessionStorage.getItem(SESSION_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const snapshot = JSON.parse(raw);
    if (!snapshot?.session?.accessToken || !snapshot.lastActivityAt) {
      clearStoredSession();
      return null;
    }
    if (now - snapshot.lastActivityAt >= SESSION_TIMEOUT_MS) {
      clearStoredSession();
      return null;
    }
    return snapshot;
  } catch {
    clearStoredSession();
    return null;
  }
}

export function saveStoredSession(snapshot) {
  if (!snapshot?.session?.accessToken) {
    clearStoredSession();
    return;
  }
  window.sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(snapshot));
}

export function clearStoredSession() {
  window.sessionStorage.removeItem(SESSION_STORAGE_KEY);
}
