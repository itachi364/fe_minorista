param(
    [string]$SourceHtml = "services/accounting-service/target/decreto_0572_2025.html",
    [string]$OutputSql = "services/accounting-service/src/main/resources/db/migration/V010__seed_self_withholding_rates.sql"
)

$html = Get-Content -Raw -LiteralPath $SourceHtml
$start = $html.IndexOf('Autorretenedores y tarifas')
$end = $html.IndexOf('ART&Iacute;CULO 9', $start)
if ($start -lt 0 -or $end -lt 0) { throw 'No se encontro la tabla oficial de autorretencion.' }
$section = $html.Substring($start, $end - $start)
$rows = [regex]::Matches($section, '<tr>(.*?)</tr>', 'Singleline')
$validRates = @(0.55, 1.10, 1.20, 1.70, 2.20, 2.40, 2.70, 2.80, 3.50, 4.50)
$values = foreach ($row in $rows) {
    $cells = [regex]::Matches($row.Groups[1].Value, '<td[^>]*>(.*?)</td>', 'Singleline')
    if ($cells.Count -ne 3) { continue }
    $codeText = [System.Net.WebUtility]::HtmlDecode(($cells[0].Groups[1].Value -replace '<[^>]+>', '')).Trim()
    if ($codeText -notmatch '^\d{3,4}$') { continue }
    $rateText = [System.Net.WebUtility]::HtmlDecode(($cells[2].Groups[1].Value -replace '<[^>]+>', ''))
    $normalized = ($rateText -replace '[^0-9,\.]', '') -replace ',', '.'
    if ($normalized -notmatch '\.') {
        $normalized = $normalized.Insert([Math]::Max(1, $normalized.Length - 2), '.')
    }
    $rate = [decimal]::Parse($normalized, [Globalization.CultureInfo]::InvariantCulture)
    if ($validRates -notcontains [double]$rate) { throw "Tarifa no reconocida '$rateText' para CIIU $codeText" }
    $code = $codeText.PadLeft(4, '0')
    "    ('$code', $($rate.ToString('0.00', [Globalization.CultureInfo]::InvariantCulture)))"
}
$values = $values | Sort-Object -Unique
if ($values.Count -lt 450) { throw "Solo se extrajeron $($values.Count) tarifas CIIU; se esperaba el catalogo completo." }

$header = @'
-- Generated from the official Decreto 572 de 2025 table by scripts/generate-self-withholding-migration.ps1.
WITH official_rates(ciiu_code, percentage) AS (
    VALUES
'@
$footer = @'
)
INSERT INTO withholding_rule (
    id, company_id, rule_set_version, operation_type, concept_code, withholding_type, base_min_amount, rate,
    requires_company_withholding_agent, requires_company_vat_responsible,
    required_third_party_tax_regime, required_third_party_responsibility, municipality_code, ciiu_code,
    valid_from, valid_to, priority, active, threshold_unit, threshold_value, threshold_operator,
    calculation_base, threshold_treatment, decision, requires_company_vat_withholding_agent,
    requires_company_ica_withholding_agent, legal_reference, source_url, specificity, published)
SELECT md5('CO-DUR-572-2025-AUTORETENCION-' || ciiu_code)::uuid, NULL, 'CO-DUR-572-2025-AUTORETENCION',
       'RECEIPT', 'ANY', 'AUTORETENCION', 0, percentage / 100,
       false, false, NULL, NULL, NULL, ciiu_code, DATE '2025-06-01', NULL, 100, true,
       'COP', 0, 'GTE', 'COMPANY_INCOME', 'FULL_AMOUNT', 'APPLIED', false, false,
       'DUR 1625 de 2016, articulo 1.2.6.8, modificado por Decreto 572 de 2025',
       'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_0572_2025.htm', 50, true
FROM official_rates
ON CONFLICT (id) DO NOTHING;
'@
$sql = $header + "`r`n" + ($values -join ",`r`n") + "`r`n" + $footer
Set-Content -LiteralPath $OutputSql -Value $sql -Encoding utf8
Write-Output "Generated $($values.Count) CIIU self-withholding rates."
