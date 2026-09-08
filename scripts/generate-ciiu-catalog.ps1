param(
    [string]$SourcePath = "target/normative-data/CIIU_4_AC_Estructura_detallada_2022.xlsx",
    [string]$OutputPath = "services/catalog-service/src/main/resources/db/migration/V011__align_ciiu_catalog_with_dian_rev4_2022.sql",
    [string]$RevisionLabel = "CIIU Rev. 4 A.C. actualizacion 2022",
    [string]$SourceVersion = "Rev.4 A.C. Res.2306/2022",
    [string]$ValidFrom = "2022-12-27",
    [bool]$ReplaceCatalog = $true
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Read-ZipEntry {
    param(
        [System.IO.Compression.ZipArchive]$Archive,
        [string]$Name
    )

    $entry = $Archive.GetEntry($Name)
    if ($null -eq $entry) {
        throw "Missing XLSX entry: $Name"
    }
    $reader = [System.IO.StreamReader]::new($entry.Open())
    try {
        return $reader.ReadToEnd()
    } finally {
        $reader.Dispose()
    }
}

function Sql-Literal {
    param([string]$Value)
    return "'" + $Value.Replace("'", "''") + "'"
}

$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path $SourcePath))
try {
    [xml]$sharedStringsXml = Read-ZipEntry $archive "xl/sharedStrings.xml"
    $stringsNamespace = [System.Xml.XmlNamespaceManager]::new($sharedStringsXml.NameTable)
    $stringsNamespace.AddNamespace("x", "http://schemas.openxmlformats.org/spreadsheetml/2006/main")
    $sharedStrings = @(
        $sharedStringsXml.SelectNodes("//x:si", $stringsNamespace) | ForEach-Object {
            ($_.SelectNodes(".//x:t", $stringsNamespace) | ForEach-Object { $_.InnerText }) -join ""
        }
    )

    [xml]$sheetXml = Read-ZipEntry $archive "xl/worksheets/sheet1.xml"
    $sheetNamespace = [System.Xml.XmlNamespaceManager]::new($sheetXml.NameTable)
    $sheetNamespace.AddNamespace("x", "http://schemas.openxmlformats.org/spreadsheetml/2006/main")

    $classes = foreach ($row in $sheetXml.SelectNodes("//x:sheetData/x:row", $sheetNamespace)) {
        $values = @{}
        foreach ($cell in $row.SelectNodes("./x:c", $sheetNamespace)) {
            $column = $cell.r -replace "\d", ""
            $valueNode = $cell.SelectSingleNode("./x:v", $sheetNamespace)
            if ($null -eq $valueNode) {
                continue
            }
            $values[$column] = if ($cell.t -eq "s") {
                $sharedStrings[[int]$valueNode.InnerText]
            } else {
                $valueNode.InnerText
            }
        }
        if ($values["C"] -match "^\d{4}$" -and -not [string]::IsNullOrWhiteSpace($values["D"])) {
            [PSCustomObject]@{
                Code = $values["C"]
                Label = $values["D"].Trim()
            }
        }
    }

    if ($classes.Count -lt 400) {
        throw "Expected at least 400 CIIU classes but found $($classes.Count)."
    }

    $lines = [System.Collections.Generic.List[string]]::new()
    $lines.Add("-- Generated from the official DANE $RevisionLabel detailed structure.")
    $lines.Add("INSERT INTO catalog.catalog_definition (catalog_code, label, description, regulatory, company_configurable, global_editable_by_root, active, sort_order)")
    $lines.Add("VALUES ('CIIU', 'Actividades economicas CIIU', 'CIIU Rev. 4 A.C. 2022 usada por la DIAN para fines fiscales.', true, false, false, true, 35)")
    $lines.Add("ON CONFLICT (catalog_code) DO UPDATE SET")
    $lines.Add("    label = EXCLUDED.label, description = EXCLUDED.description, regulatory = EXCLUDED.regulatory,")
    $lines.Add("    company_configurable = EXCLUDED.company_configurable, global_editable_by_root = EXCLUDED.global_editable_by_root,")
    $lines.Add("    active = EXCLUDED.active, sort_order = EXCLUDED.sort_order;")
    $lines.Add("")
    if ($ReplaceCatalog) {
        $lines.Add("DELETE FROM catalog.catalog_item WHERE catalog_code = 'CIIU';")
        $lines.Add("")
    }
    $lines.Add("INSERT INTO catalog.catalog_item (catalog_code, item_code, label, description, active, regulatory, source, source_version, valid_from, valid_to, sort_order) VALUES")

    for ($index = 0; $index -lt $classes.Count; $index++) {
        $entry = $classes[$index]
        $label = if ($entry.Label.Length -gt 180) { $entry.Label.Substring(0, 180) } else { $entry.Label }
        $suffix = if ($index -eq $classes.Count - 1) { "" } else { "," }
        $lines.Add("    ('CIIU', $(Sql-Literal $entry.Code), $(Sql-Literal $label), $(Sql-Literal $entry.Label), true, true, 'DANE', $(Sql-Literal $SourceVersion), DATE $(Sql-Literal $ValidFrom), NULL, $($index + 1))$suffix")
    }
    $lines.Add("ON CONFLICT (catalog_code, item_code) DO UPDATE SET")
    $lines.Add("    label = EXCLUDED.label, description = EXCLUDED.description, active = EXCLUDED.active,")
    $lines.Add("    regulatory = EXCLUDED.regulatory, source = EXCLUDED.source, source_version = EXCLUDED.source_version,")
    $lines.Add("    valid_from = EXCLUDED.valid_from, valid_to = EXCLUDED.valid_to, sort_order = EXCLUDED.sort_order;")

    $resolvedOutput = Join-Path (Get-Location) $OutputPath
    [System.IO.File]::WriteAllLines($resolvedOutput, $lines, [System.Text.UTF8Encoding]::new($false))
    Write-Output "Generated $($classes.Count) CIIU classes in $OutputPath"
} finally {
    $archive.Dispose()
}
