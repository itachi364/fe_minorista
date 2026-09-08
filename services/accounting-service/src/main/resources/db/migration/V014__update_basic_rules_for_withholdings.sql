WITH basic_companies AS (
    SELECT DISTINCT company_id
    FROM accounting_rule
    WHERE name IN (
        'Factura de compra - plantilla basica',
        'Gasto operativo - plantilla basica',
        'Compra de activo - plantilla basica'
    )
), retention_accounts(code, name) AS (
    VALUES
        ('2365', 'Retencion en la fuente'),
        ('2367', 'Impuesto a las ventas retenido'),
        ('2368', 'Impuesto de industria y comercio retenido')
)
INSERT INTO accounting_account (id, company_id, code, name, category, level, nature, parent_account_id, active)
SELECT gen_random_uuid(), company.company_id, account.code, account.name, 'LIABILITY', 'ACCOUNT', 'CREDIT', NULL, true
FROM basic_companies company
CROSS JOIN retention_accounts account
ON CONFLICT (company_id, code) DO NOTHING;

UPDATE accounting_rule_line line
SET amount_type = 'NET_PAYABLE'
FROM accounting_rule rule
WHERE line.rule_id = rule.id
  AND rule.active = true
  AND rule.name IN (
      'Factura de compra - plantilla basica',
      'Gasto operativo - plantilla basica',
      'Compra de activo - plantilla basica'
  )
  AND line.account_code = '2205'
  AND line.amount_type = 'TOTAL';

WITH basic_rules AS (
    SELECT id
    FROM accounting_rule
    WHERE active = true
      AND name IN (
          'Factura de compra - plantilla basica',
          'Gasto operativo - plantilla basica',
          'Compra de activo - plantilla basica'
      )
), retention_lines(account_code, amount_type, description, offset_order) AS (
    VALUES
        ('2365', 'RETEFUENTE', 'Retencion en la fuente por pagar', 1),
        ('2367', 'RETEIVA', 'ReteIVA por pagar', 2),
        ('2368', 'RETEICA', 'ReteICA por pagar', 3)
)
INSERT INTO accounting_rule_line (id, rule_id, line_order, account_code, side, amount_type, description)
SELECT gen_random_uuid(), rule.id,
       (SELECT COALESCE(MAX(existing.line_order), 0) FROM accounting_rule_line existing WHERE existing.rule_id = rule.id)
           + retention.offset_order,
       retention.account_code, 'CREDIT', retention.amount_type, retention.description
FROM basic_rules rule
CROSS JOIN retention_lines retention
WHERE NOT EXISTS (
    SELECT 1
    FROM accounting_rule_line existing
    WHERE existing.rule_id = rule.id
      AND existing.amount_type = retention.amount_type
);
