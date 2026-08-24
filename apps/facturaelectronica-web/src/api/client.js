const DEFAULT_HEADERS = {
  Accept: 'application/json',
  'Content-Type': 'application/json',
};

function baseHeaders({ token, companyId, userId, idempotencyKey } = {}) {
  const headers = { Accept: 'application/json' };
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
  const csrfToken = cookieValue('NF_CSRF');
  if (csrfToken) {
    headers['X-CSRF-Token'] = csrfToken;
  }
  return headers;
}

export async function requestJson(path, { method = 'GET', body, token, companyId, userId, idempotencyKey } = {}) {
  const headers = { ...DEFAULT_HEADERS, ...baseHeaders({ token, companyId, userId, idempotencyKey }) };

  const response = await fetch(path, {
    method,
    headers,
    credentials: 'same-origin',
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

export async function requestFormData(path, { method = 'POST', formData, token, companyId, userId, idempotencyKey } = {}) {
  const response = await fetch(path, {
    method,
    headers: baseHeaders({ token, companyId, userId, idempotencyKey }),
    credentials: 'same-origin',
    body: formData,
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

export async function requestDownload(path, { method = 'GET', body, token, companyId, userId, idempotencyKey } = {}) {
  const response = await fetch(path, {
    method,
    headers: { ...baseHeaders({ token, companyId, userId, idempotencyKey }), 'Content-Type': 'application/json' },
    credentials: 'same-origin',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) {
    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;
    const message = payload?.message || `HTTP ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.payload = payload;
    throw error;
  }
  return {
    blob: await response.blob(),
    filename: filenameFromDisposition(response.headers.get('content-disposition')),
  };
}

function filenameFromDisposition(disposition) {
  const match = disposition?.match(/filename="?([^"]+)"?/i);
  return match?.[1] || 'nexofiscal-reporte.csv';
}

export function createIdempotencyKey(prefix) {
  return `${prefix}-${crypto.randomUUID()}`;
}

function cookieValue(name) {
  if (typeof document === 'undefined' || !document.cookie) {
    return '';
  }
  return document.cookie
    .split(';')
    .map((cookie) => cookie.trim())
    .find((cookie) => cookie.startsWith(`${name}=`))
    ?.slice(name.length + 1) || '';
}
