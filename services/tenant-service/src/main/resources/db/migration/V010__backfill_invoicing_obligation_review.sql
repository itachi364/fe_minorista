INSERT INTO tenant.company_invoicing_obligation_snapshot (
    id,
    company_id,
    version,
    current_snapshot,
    status,
    decision_code,
    uvt_value,
    normative_rule_set_version,
    evaluated_at
)
SELECT
    md5(c.id::text || ':invoicing-obligation-v1')::uuid,
    c.id,
    1,
    TRUE,
    'REVIEW_REQUIRED',
    'INITIAL_REVIEW_REQUIRED',
    52374.00,
    'CO-INVOICE-2026-01',
    CURRENT_TIMESTAMP
FROM tenant.company c
WHERE NOT EXISTS (
    SELECT 1
    FROM tenant.company_invoicing_obligation_snapshot s
    WHERE s.company_id = c.id
);

INSERT INTO tenant.company_invoicing_obligation_reason (snapshot_id, reason_order, reason_code)
SELECT s.id, 0, 'RUT_EVIDENCE_OR_IDENTITY_MISSING'
FROM tenant.company_invoicing_obligation_snapshot s
WHERE s.decision_code = 'INITIAL_REVIEW_REQUIRED'
  AND NOT EXISTS (
      SELECT 1 FROM tenant.company_invoicing_obligation_reason r WHERE r.snapshot_id = s.id
  );
