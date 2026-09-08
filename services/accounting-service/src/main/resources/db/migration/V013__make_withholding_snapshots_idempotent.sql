WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY company_id, source_type, source_id, withholding_type
               ORDER BY created_at, id
           ) AS position
    FROM accounting.withholding_calculation_snapshot
)
DELETE FROM accounting.withholding_calculation_snapshot snapshot
USING ranked
WHERE snapshot.id = ranked.id
  AND ranked.position > 1;

CREATE UNIQUE INDEX uk_withholding_snapshot_document_type
    ON accounting.withholding_calculation_snapshot (company_id, source_type, source_id, withholding_type);
