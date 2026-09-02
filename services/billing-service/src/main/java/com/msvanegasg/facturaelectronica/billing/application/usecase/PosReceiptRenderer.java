package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.msvanegasg.facturaelectronica.billing.application.dto.PosReceiptResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;

final class PosReceiptRenderer {

    private PosReceiptRenderer() {
    }

    static PosReceiptResult render(SaleResult sale, int widthMm) {
        int safeWidth = widthMm == 58 ? 58 : 80;
        String number = sale.electronicDocument() == null
                ? sale.id().toString()
                : sale.electronicDocument().prefix() + sale.electronicDocument().documentNumber();
        String qrPayload = qrPayload(sale, number, safeWidth);
        String html = """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <title>Comprobante POS %s</title>
                  <style>
                    @page { size: %smm auto; margin: 3mm; }
                    * { box-sizing: border-box; }
                    body { width: %smm; margin: 0 auto; font-family: Arial, sans-serif; color: #111; font-size: 11px; }
                    h1, h2, p { margin: 0; }
                    h1 { font-size: 15px; text-align: center; }
                    h2 { font-size: 12px; text-align: center; font-weight: 700; }
                    .center { text-align: center; }
                    .line { border-top: 1px dashed #111; margin: 6px 0; }
                    table { width: 100%%; border-collapse: collapse; }
                    th, td { padding: 2px 0; vertical-align: top; }
                    th { text-align: left; border-bottom: 1px solid #111; }
                    .right { text-align: right; }
                    .totals td { font-weight: 700; }
                    .qr { overflow-wrap: anywhere; font-size: 9px; }
                    .qr-code { display: flex; justify-content: center; margin: 6px 0; }
                    .qr-code svg { width: 84px; height: 84px; }
                    @media print { button { display: none; } }
                  </style>
                </head>
                <body>
                  <h1>NexoFiscal POS</h1>
                  <h2>Factura electronica POS</h2>
                  <p class="center">Venta: %s</p>
                  <p class="center">Documento: %s</p>
                  <p class="center">Fecha: %s</p>
                  <div class="line"></div>
                  <table>
                    <thead><tr><th>Item</th><th class="right">Cant</th><th class="right">Total</th></tr></thead>
                    <tbody>%s</tbody>
                  </table>
                  <div class="line"></div>
                  <table class="totals">
                    <tbody>
                      <tr><td>Subtotal</td><td class="right">%s</td></tr>
                      <tr><td>IVA</td><td class="right">%s</td></tr>
                      <tr><td>Total</td><td class="right">%s</td></tr>
                    </tbody>
                  </table>
                  <div class="line"></div>
                  <p>CUFE/CUDE:</p>
                  <p class="qr">%s</p>
                  <p>QR:</p>
                  <div class="qr-code">%s</div>
                  <p class="qr">%s</p>
                  <div class="line"></div>
                  <p class="center">Representacion imprimible generada por NexoFiscal.</p>
                  <script>window.addEventListener('load', () => setTimeout(() => window.print(), 250));</script>
                </body>
                </html>
                """.formatted(escape(number), safeWidth, safeWidth, sale.id(), escape(number),
                sale.confirmedAt() == null ? sale.createdAt() : sale.confirmedAt(), lines(sale),
                money(sale.subtotal()), money(sale.taxTotal()), money(sale.total()),
                escape(sale.electronicDocument() == null ? "" : sale.electronicDocument().cufeCude()),
                qrSvg(qrPayload), escape(qrPayload));
        return new PosReceiptResult("nexofiscal-pos-" + number + ".html", "text/html; charset=UTF-8",
                html.getBytes(StandardCharsets.UTF_8));
    }

    private static String lines(SaleResult sale) {
        return sale.lines().stream().map(PosReceiptRenderer::line).reduce("", String::concat);
    }

    private static String line(SaleLineResult line) {
        return "<tr><td>" + escape(line.productName()) + "<br><small>" + escape(line.productSku())
                + "</small></td><td class=\"right\">" + number(line.quantity()) + "</td><td class=\"right\">"
                + money(line.total()) + "</td></tr>";
    }

    private static String money(BigDecimal value) {
        return "$ " + number(value);
    }

    private static String number(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String qrPayload(SaleResult sale, String number, int safeWidth) {
        String providerQr = sale.electronicDocument() == null ? null : sale.electronicDocument().qrContent();
        if (providerQr != null && !providerQr.isBlank() && !providerQr.startsWith("mock-qr:")) {
            return providerQr;
        }
        return "/api/v1/sales/%s/receipt?widthMm=%s&document=%s".formatted(sale.id(), safeWidth, number);
    }

    private static String qrSvg(String payload) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 0, 0);
            StringBuilder svg = new StringBuilder("<svg viewBox=\"0 0 ")
                    .append(matrix.getWidth()).append(' ').append(matrix.getHeight())
                    .append("\" role=\"img\" aria-label=\"Codigo QR DIAN\"><rect width=\"100%\" height=\"100%\" fill=\"#fff\"/>");
            for (int y = 0; y < matrix.getHeight(); y++) {
                for (int x = 0; x < matrix.getWidth(); x++) {
                    if (matrix.get(x, y)) {
                        svg.append("<rect x=\"").append(x).append("\" y=\"").append(y)
                                .append("\" width=\"1\" height=\"1\" fill=\"#000\"/>");
                    }
                }
            }
            return svg.append("</svg>").toString();
        } catch (WriterException exception) {
            return "";
        }
    }
}
