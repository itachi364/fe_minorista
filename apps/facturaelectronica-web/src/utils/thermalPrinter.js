const ESC = 0x1b;
const GS = 0x1d;

export async function printReceiptWithThermalPrinter(blob, options = {}) {
  if (!canUseWebSerial()) {
    return false;
  }
  const receiptText = htmlToReceiptText(await blob.text());
  if (!receiptText.trim()) {
    return false;
  }
  const port = await window.navigator.serial.requestPort();
  await port.open({ baudRate: options.baudRate || 9600 });
  const writer = port.writable?.getWriter();
  if (!writer) {
    await closePort(port);
    return false;
  }
  try {
    await writer.write(toEscPos(receiptText, options));
    return true;
  } finally {
    writer.releaseLock();
    await closePort(port);
  }
}

export function canUseWebSerial() {
  return typeof window !== 'undefined'
    && Boolean(window.navigator?.serial?.requestPort)
    && typeof TextEncoder !== 'undefined';
}

function htmlToReceiptText(html) {
  const parser = new DOMParser();
  const document = parser.parseFromString(html, 'text/html');
  document.querySelectorAll('script, style, svg').forEach((node) => node.remove());
  return (document.body?.innerText || document.documentElement?.textContent || '')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

function toEscPos(text, options) {
  const encoder = new TextEncoder();
  const lines = [
    new Uint8Array([ESC, 0x40]),
    encoder.encode(text),
    encoder.encode('\n\n\n'),
  ];
  if (options.cut !== false) {
    lines.push(new Uint8Array([GS, 0x56, 0x00]));
  }
  return concat(lines);
}

function concat(chunks) {
  const length = chunks.reduce((total, chunk) => total + chunk.length, 0);
  const result = new Uint8Array(length);
  let offset = 0;
  chunks.forEach((chunk) => {
    result.set(chunk, offset);
    offset += chunk.length;
  });
  return result;
}

async function closePort(port) {
  try {
    await port.close();
  } catch {
    // The browser can keep a serial port busy briefly after releasing the writer.
  }
}
