param(
    [switch]$StartContainers,
    [string]$TenantUrl = "http://localhost:8084",
    [string]$IdentityUrl = "http://localhost:8092",
    [string]$CatalogUrl = "http://localhost:8085",
    [string]$ThirdpartyUrl = "http://localhost:8086",
    [string]$InventoryUrl = "http://localhost:8087",
    [string]$BillingUrl = "http://localhost:8088",
    [string]$ProviderUrl = "http://localhost:8089",
    [string]$AccountingUrl = "http://localhost:8090",
    [string]$AuditUrl = "http://localhost:8091"
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Uri,
        [object]$Body,
        [hashtable]$Headers = @{}
    )

    $request = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
    }

    if ($null -ne $Body) {
        $request.ContentType = "application/json"
        $request.Body = ($Body | ConvertTo-Json -Depth 20)
    }

    try {
        Invoke-RestMethod @request
    }
    catch {
        $message = $_.Exception.Message
        if ($_.Exception.Response -and $_.Exception.Response.GetResponseStream()) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $message = $reader.ReadToEnd()
        }
        throw "HTTP $Method $Uri failed: $message"
    }
}

function Wait-Health {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$BaseUrl
    )

    $deadline = (Get-Date).AddMinutes(5)
    do {
        try {
            $health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
            if ($health.status -eq "UP") {
                Write-Host "$Name is UP"
                return
            }
        }
        catch {
            Start-Sleep -Seconds 3
        }
    } while ((Get-Date) -lt $deadline)

    throw "$Name did not become healthy at $BaseUrl"
}

function Assert-Equal {
    param(
        [Parameter(Mandatory = $true)]$Actual,
        [Parameter(Mandatory = $true)]$Expected,
        [Parameter(Mandatory = $true)][string]$Message
    )

    if ("$Actual" -ne "$Expected") {
        throw "$Message. Expected '$Expected', got '$Actual'."
    }
}

function Assert-DecimalEqual {
    param(
        [Parameter(Mandatory = $true)]$Actual,
        [Parameter(Mandatory = $true)][decimal]$Expected,
        [Parameter(Mandatory = $true)][string]$Message
    )

    $actualDecimal = [decimal]::Parse("$Actual", [System.Globalization.CultureInfo]::InvariantCulture)
    if ($actualDecimal -ne $Expected) {
        throw "$Message. Expected '$Expected', got '$Actual'."
    }
}

function Assert-NotEmpty {
    param(
        $Value,
        [Parameter(Mandatory = $true)][string]$Message
    )

    if ($null -eq $Value -or "$Value".Trim().Length -eq 0) {
        throw $Message
    }
}

if ($StartContainers) {
    Write-Host "Starting Docker Compose services..."
    docker compose up -d postgres tenant-service identity-service catalog-service thirdparty-service inventory-service accounting-service dian-provider-service audit-service billing-service
}

Wait-Health "tenant-service" $TenantUrl
Wait-Health "identity-service" $IdentityUrl
Wait-Health "catalog-service" $CatalogUrl
Wait-Health "thirdparty-service" $ThirdpartyUrl
Wait-Health "inventory-service" $InventoryUrl
Wait-Health "accounting-service" $AccountingUrl
Wait-Health "dian-provider-service" $ProviderUrl
Wait-Health "audit-service" $AuditUrl
Wait-Health "billing-service" $BillingUrl

$suffix = (Get-Date -Format "yyyyMMddHHmmss")
$identificationTypeId = [guid]::NewGuid().ToString()
$ownerEmail = "owner-$suffix@example.com"
$ownerPassword = "secret123"

Write-Host "Creating company..."
$company = Invoke-Api -Method Post -Uri "$TenantUrl/api/v1/companies" -Body @{
    legalName = "E2E Empresa SAS $suffix"
    tradeName = "E2E Tienda $suffix"
    identificationTypeId = $identificationTypeId
    identificationNumber = "90$suffix"
    verificationDigit = "1"
    email = "e2e-$suffix@example.com"
}

Assert-Equal $company.status "ACTIVE" "Company must be active"
$companyId = $company.id

Write-Host "Creating active company license..."
$license = Invoke-Api -Method Post -Uri "$TenantUrl/api/v1/companies/$companyId/license" -Body @{
    planCode = "SMALL_BUSINESS"
    validFrom = "2026-01-01"
    validTo = "2027-12-31"
    maxUsers = 5
    maxMonthlyDocuments = 1000
}
Assert-Equal $license.status "ACTIVE" "Company license must be active"

$licenseValidation = Invoke-Api -Method Get -Uri "$TenantUrl/api/v1/companies/$companyId/license/validation?action=CREATE_TRANSACTION"
Assert-Equal $licenseValidation.allowed "True" "License must allow transactions"

Write-Host "Creating owner user and company membership..."
$owner = Invoke-Api -Method Post -Uri "$IdentityUrl/api/v1/users" -Body @{
    email = $ownerEmail
    fullName = "Owner E2E $suffix"
    password = $ownerPassword
}
$userId = $owner.id

$membership = Invoke-Api -Method Post -Uri "$IdentityUrl/api/v1/companies/$companyId/memberships" -Body @{
    userId = $userId
    roles = @("OWNER")
}
Assert-Equal $membership.companyId $companyId "Owner membership must belong to company"

$login = Invoke-Api -Method Post -Uri "$IdentityUrl/api/v1/auth/login" -Body @{
    email = $ownerEmail
    password = $ownerPassword
}
Assert-NotEmpty $login.accessToken "Login token is required"
$authHeaders = @{ "Authorization" = "Bearer $($login.accessToken)" }
$companies = @(Invoke-Api -Method Get -Uri "$IdentityUrl/api/v1/me/companies" -Headers $authHeaders)
if (@($companies | Where-Object { $_.companyId -eq $companyId }).Count -ne 1) {
    throw "Owner user must see the created company."
}

Write-Host "Creating second company for isolation checks..."
$otherCompany = Invoke-Api -Method Post -Uri "$TenantUrl/api/v1/companies" -Body @{
    legalName = "E2E Otra Empresa SAS $suffix"
    tradeName = "E2E Otra Tienda $suffix"
    identificationTypeId = ([guid]::NewGuid().ToString())
    identificationNumber = "91$suffix"
    verificationDigit = "2"
    email = "e2e-other-$suffix@example.com"
}
$otherCompanyId = $otherCompany.id

Write-Host "Creating customer and supplier with v1 third-party APIs..."
$companyHeaders = @{
    "X-Company-Id" = $companyId
    "X-User-Id" = $userId
}

$customer = Invoke-Api -Method Post -Uri "$ThirdpartyUrl/api/v1/customers" -Headers $companyHeaders -Body @{
    personType = "NATURAL"
    identificationTypeCode = "CC"
    identificationNumber = "10$suffix"
    fullName = "Cliente E2E $suffix"
    businessName = $null
    tradeName = $null
    email = "cliente-$suffix@example.com"
    phone = "3001234567"
    address = "Calle 1 # 2-3"
    municipalityCode = "11001"
    roles = @("CUSTOMER")
}
Assert-Equal $customer.companyId $companyId "Customer must belong to company"

$supplier = Invoke-Api -Method Post -Uri "$ThirdpartyUrl/api/v1/suppliers" -Headers $companyHeaders -Body @{
    personType = "JURIDICA"
    identificationTypeCode = "NIT"
    identificationNumber = "900123456"
    verificationDigit = 8
    fullName = $null
    businessName = "Proveedor E2E SAS $suffix"
    tradeName = "Proveedor E2E $suffix"
    email = "proveedor-$suffix@example.com"
    phone = "3007654321"
    address = "Carrera 4 # 5-6"
    municipalityCode = "11001"
    roles = @("SUPPLIER")
}
Assert-Equal $supplier.companyId $companyId "Supplier must belong to company"

Write-Host "Creating issuer and POS numbering resolution..."
$issuer = Invoke-Api -Method Post -Uri "$BillingUrl/api/v1/issuers" -Headers $companyHeaders -Body @{
    legalName = "E2E Empresa SAS $suffix"
    nit = "90$suffix"
    verificationDigit = "1"
    taxResponsibilities = @("O-13")
    municipalityCode = "11001"
    address = "Calle 1 # 2-3"
}
Assert-Equal $issuer.active "True" "Issuer profile must be active"

$resolutionFromNumber = [int64]("3" + $suffix.Substring($suffix.Length - 8))
$resolution = Invoke-Api -Method Post -Uri "$BillingUrl/api/v1/numbering-resolutions" -Headers $companyHeaders -Body @{
    documentType = "ELECTRONIC_POS"
    resolutionNumber = "1876$suffix"
    prefix = "POS"
    fromNumber = $resolutionFromNumber
    toNumber = ($resolutionFromNumber + 100)
    validFrom = "2026-01-01"
    validTo = "2026-12-31"
    environment = "TEST"
}
Assert-Equal $resolution.currentNumber ($resolutionFromNumber - 1) "Resolution must start before first authorized number"

$accountHeaders = @{ "X-Company-Id" = $companyId }

Write-Host "Creating basic PUC accounting setup..."
$accountingSetup = Invoke-Api -Method Post -Uri "$AccountingUrl/api/v1/accounting-setup/basic" -Headers $accountHeaders
if (@($accountingSetup.accounts).Count -lt 1) {
    throw "Basic accounting setup must create accounts."
}

Write-Host "Creating product with initial stock..."
$product = Invoke-Api -Method Post -Uri "$InventoryUrl/api/v1/products" -Headers ($companyHeaders + @{ "Idempotency-Key" = "product-$suffix" }) -Body @{
    sku = "E2E-SKU-$suffix"
    barcode = "770$suffix"
    name = "Cafe E2E $suffix"
    description = "Bolsa 500g"
    salePrice = 15000.00
    cost = 9000.00
    initialStock = 10.00
}

Assert-DecimalEqual $product.currentStock 10.00 "Initial stock must be 10"
$productId = $product.id

Write-Host "Creating and confirming POS sale..."
$sale = Invoke-Api -Method Post -Uri "$BillingUrl/api/v1/sales" -Headers ($companyHeaders + @{ "Idempotency-Key" = "sale-$suffix" }) -Body @{
    saleChannel = "POS"
    items = @(
        @{
            productId = $productId
            quantity = 2.00
            unitPrice = 15000.00
            discountAmount = 0.00
            taxCode = "IVA_19"
            taxRate = 19.00
        }
    )
}

Assert-Equal $sale.status "DRAFT" "Sale must start as DRAFT"
$saleId = $sale.id

$confirmedSale = Invoke-Api -Method Post -Uri "$BillingUrl/api/v1/sales/$saleId/confirm" -Headers ($companyHeaders + @{ "Idempotency-Key" = "confirm-$suffix" })

Assert-Equal $confirmedSale.status "CONFIRMED" "Sale must be confirmed"
Assert-Equal $confirmedSale.electronicDocument.status "VALIDATED" "Electronic document must be validated"
Assert-Equal $confirmedSale.electronicDocument.providerStatus "ACCEPTED" "Provider status must be accepted"
Assert-Equal $confirmedSale.electronicDocument.prefix "POS" "Electronic document must use configured POS prefix"
Assert-Equal $confirmedSale.electronicDocument.documentNumber $resolutionFromNumber "Electronic document must use configured first fiscal number"
Assert-NotEmpty $confirmedSale.electronicDocument.providerTrackingId "Provider tracking id is required"
Assert-NotEmpty $confirmedSale.electronicDocument.inventoryAppliedAt "Inventory application timestamp is required"
Assert-NotEmpty $confirmedSale.electronicDocument.accountingAppliedAt "Accounting application timestamp is required"

Write-Host "Verifying inventory..."
$updatedProduct = Invoke-Api -Method Get -Uri "$InventoryUrl/api/v1/products/$productId" -Headers $companyHeaders
Assert-DecimalEqual $updatedProduct.currentStock 8.00 "Final stock must be 8 after sale"

$kardex = Invoke-Api -Method Get -Uri "$InventoryUrl/api/v1/products/$productId/kardex" -Headers $companyHeaders
$saleOut = @($kardex | Where-Object { $_.movementType -eq "SALE_OUT" })
if ($saleOut.Count -ne 1) {
    throw "Expected exactly one SALE_OUT movement, got $($saleOut.Count)."
}
Assert-DecimalEqual $saleOut[0].resultingStock 8.00 "SALE_OUT resulting stock must be 8"

Write-Host "Verifying DIAN provider submission..."
$trackingId = $confirmedSale.electronicDocument.providerTrackingId
$submission = Invoke-Api -Method Get -Uri "$ProviderUrl/api/v1/provider/submissions/$trackingId" -Headers @{ "X-Company-Id" = $companyId }
Assert-Equal $submission.status "ACCEPTED" "Provider submission must be accepted"
Assert-Equal $submission.documentId $confirmedSale.electronicDocument.id "Provider document id must match billing document"

Write-Host "Verifying accounting journal..."
$entryDate = ([DateTimeOffset]::Parse($confirmedSale.confirmedAt)).UtcDateTime.ToString("yyyy-MM-dd")
$journal = Invoke-Api -Method Get -Uri "$AccountingUrl/api/v1/reports/journal?from=$entryDate&to=$entryDate" -Headers $accountHeaders
Assert-DecimalEqual $journal.debitTotal 35700.00 "Journal debit total must be 35700.00"
Assert-DecimalEqual $journal.creditTotal 35700.00 "Journal credit total must be 35700.00"
if (@($journal.entries).Count -lt 1) {
    throw "Expected at least one accounting entry in journal."
}

Write-Host "Verifying audit event..."
$auditEvents = @(Invoke-Api -Method Get -Uri "$AuditUrl/api/v1/audit-events?resourceType=SALE&resourceId=$saleId" -Headers $companyHeaders)
$saleAuditEvent = @($auditEvents | Where-Object { $_.action -eq "CONFIRM_SALE" -and $_.result -eq "SUCCESS" })
if ($saleAuditEvent.Count -lt 1) {
    throw "Expected at least one successful CONFIRM_SALE audit event, got $($saleAuditEvent.Count)."
}

Write-Host "Verifying tenant isolation..."
$otherHeaders = @{ "X-Company-Id" = $otherCompanyId }

try {
    Invoke-Api -Method Get -Uri "$InventoryUrl/api/v1/products/$productId" -Headers $otherHeaders | Out-Null
    throw "Inventory product was visible from another company."
}
catch {
    if ($_.ToString() -like "*Inventory product was visible*") {
        throw
    }
}

try {
    Invoke-Api -Method Get -Uri "$BillingUrl/api/v1/sales/$saleId" -Headers $otherHeaders | Out-Null
    throw "Sale was visible from another company."
}
catch {
    if ($_.ToString() -like "*Sale was visible*") {
        throw
    }
}

try {
    Invoke-Api -Method Get -Uri "$ProviderUrl/api/v1/provider/submissions/$trackingId" -Headers $otherHeaders | Out-Null
    throw "Provider submission was visible from another company."
}
catch {
    if ($_.ToString() -like "*Provider submission was visible*") {
        throw
    }
}

try {
    Invoke-Api -Method Get -Uri "$AccountingUrl/api/v1/accounts?code=1105" -Headers $otherHeaders | Out-Null
    throw "Accounting account was visible from another company."
}
catch {
    if ($_.ToString() -like "*Accounting account was visible*") {
        throw
    }
}

$otherAuditResponse = Invoke-Api -Method Get -Uri "$AuditUrl/api/v1/audit-events?resourceType=SALE&resourceId=$saleId" -Headers $otherHeaders
$otherAuditCount = if ($null -eq $otherAuditResponse) { 0 } else { @($otherAuditResponse).Count }
if ($otherAuditCount -ne 0) {
    throw "Audit event was visible from another company."
}

Write-Host ""
Write-Host "E2E flow completed successfully."
Write-Host "CompanyId: $companyId"
Write-Host "ProductId: $productId"
Write-Host "SaleId: $saleId"
Write-Host "DocumentId: $($confirmedSale.electronicDocument.id)"
Write-Host "ProviderTrackingId: $trackingId"
