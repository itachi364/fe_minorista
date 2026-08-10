const DEFAULT_HEADERS = {
  Accept: 'application/json',
  'Content-Type': 'application/json',
};

export async function requestJson(path, { method = 'GET', body, token, companyId, userId, idempotencyKey } = {}) {
  const headers = { ...DEFAULT_HEADERS };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (companyId) {
    headers['X-Company-Id'] = companyId;
  }
  if (userId) {
    headers['X-User-Id'] = userId;
  }
  headers['X-Correlation-Id'] = crypto.randomUUID();
  if (idempotencyKey) {
    headers['Idempotency-Key'] = idempotencyKey;
  }

  const response = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;
  if (!response.ok) {
    const message = payload?.message || `HTTP ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.payload = payload;
    throw error;
  }
  return payload;
}

export function createIdempotencyKey(prefix) {
  return `${prefix}-${crypto.randomUUID()}`;
}
