ALTER TABLE inventory.inventory_movement
    ADD COLUMN reason VARCHAR(300);

ALTER TABLE inventory.inventory_movement
    DROP CONSTRAINT ck_inventory_movement_type;

ALTER TABLE inventory.inventory_movement
    ADD CONSTRAINT ck_inventory_movement_type
    CHECK (movement_type IN (
        'PURCHASE_IN',
        'SALE_OUT',
        'RETURN_IN',
        'ADJUSTMENT_IN',
        'ADJUSTMENT_OUT',
        'CONSUMPTION_OUT',
        'WASTE_OUT'
    ));

ALTER TABLE inventory.inventory_movement
    DROP CONSTRAINT ck_inventory_source_type;

ALTER TABLE inventory.inventory_movement
    ADD CONSTRAINT ck_inventory_source_type
    CHECK (source_document_type IN (
        'PURCHASE',
        'SALE',
        'RETURN',
        'ADJUSTMENT',
        'INITIAL_STOCK',
        'MANUAL_SUPPLY_CONSUMPTION',
        'MANUAL_SUPPLY_WASTE'
    ));

ALTER TABLE inventory.inventory_movement
    ADD CONSTRAINT ck_inventory_movement_reason_required
    CHECK (
        movement_type NOT IN ('CONSUMPTION_OUT', 'WASTE_OUT')
        OR reason IS NOT NULL AND BTRIM(reason) <> ''
    );
