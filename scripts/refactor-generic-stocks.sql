-- Execute antes de iniciar a versão refatorada do backend.
-- Quantidades passam a ser consultadas em tb_item_unit; somente o mínimo permanece no saldo.
BEGIN;

LOCK TABLE tb_item, tb_item_unit, tb_stock_balance, tb_stock_movement IN SHARE ROW EXCLUSIVE MODE;

-- Compatibilidade com valores antigos. A situação específica não pertence ao estoque.
UPDATE tb_item_unit SET status = 'UNAVAILABLE' WHERE status IN ('RENTED', 'RESERVED');

ALTER TABLE tb_stock_movement ADD COLUMN IF NOT EXISTS item_unit_id BIGINT;
ALTER TABLE tb_stock_movement ADD COLUMN IF NOT EXISTS previous_status VARCHAR(30);
ALTER TABLE tb_stock_movement ADD COLUMN IF NOT EXISTS new_status VARCHAR(30);

UPDATE tb_stock_movement
SET previous_status = CASE WHEN type = 'MAINTENANCE' THEN 'AVAILABLE' ELSE 'MAINTENANCE' END,
    new_status = CASE WHEN type = 'MAINTENANCE' THEN 'MAINTENANCE' ELSE 'AVAILABLE' END,
    type = 'STATUS_CHANGE'
WHERE type IN ('MAINTENANCE', 'RELEASE');

-- Valores desconhecidos interrompem a migração, sem descartar registros antigos.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM tb_item_unit
        WHERE status NOT IN ('AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE', 'DAMAGED', 'LOST')
        OR condition_status NOT IN ('NEW', 'GOOD', 'FAIR', 'DAMAGED')) THEN
        RAISE EXCEPTION 'Existem status ou condições de unidades não reconhecidos. Revise os dados antes da migração.';
    END IF;
    IF EXISTS (SELECT 1 FROM tb_stock_movement
        WHERE type NOT IN ('ENTRY', 'EXIT', 'ADJUSTMENT', 'STATUS_CHANGE')
        OR quantity < 0 OR (type <> 'ADJUSTMENT' AND quantity = 0)) THEN
        RAISE EXCEPTION 'Existem tipos ou quantidades de movimentações não reconhecidos. Revise os dados antes da migração.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'tb_stock_movement'::regclass
        AND conname = 'fk_stock_movement_item_unit') THEN
        ALTER TABLE tb_stock_movement ADD CONSTRAINT fk_stock_movement_item_unit
            FOREIGN KEY (item_unit_id) REFERENCES tb_item_unit(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'tb_item_unit'::regclass
        AND conname = 'ck_item_unit_status') THEN
        ALTER TABLE tb_item_unit ADD CONSTRAINT ck_item_unit_status
            CHECK (status IN ('AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE', 'DAMAGED', 'LOST'));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'tb_item_unit'::regclass
        AND conname = 'ck_item_unit_condition') THEN
        ALTER TABLE tb_item_unit ADD CONSTRAINT ck_item_unit_condition
            CHECK (condition_status IN ('NEW', 'GOOD', 'FAIR', 'DAMAGED'));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'tb_stock_movement'::regclass
        AND conname = 'ck_stock_movement_type') THEN
        ALTER TABLE tb_stock_movement ADD CONSTRAINT ck_stock_movement_type
            CHECK (type IN ('ENTRY', 'EXIT', 'ADJUSTMENT', 'STATUS_CHANGE'));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'tb_stock_movement'::regclass
        AND conname = 'ck_stock_movement_quantity') THEN
        ALTER TABLE tb_stock_movement ADD CONSTRAINT ck_stock_movement_quantity
            CHECK (quantity >= 0 AND (type = 'ADJUSTMENT' OR quantity > 0));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'tb_stock_balance'::regclass
        AND conname = 'ck_stock_balance_minimum') THEN
        ALTER TABLE tb_stock_balance ADD CONSTRAINT ck_stock_balance_minimum CHECK (minimum_quantity >= 0);
    END IF;
END
$$;

-- O item pode ser controlado sem informação de preço.
ALTER TABLE tb_item ALTER COLUMN price DROP NOT NULL;

ALTER TABLE tb_stock_balance DROP COLUMN IF EXISTS total_quantity;
ALTER TABLE tb_stock_balance DROP COLUMN IF EXISTS unavailable_quantity;
ALTER TABLE tb_stock_balance DROP COLUMN IF EXISTS reserved_quantity;

INSERT INTO tb_stock_balance (version, item_id, minimum_quantity, created_at, created_by)
SELECT 0, item.id, 0, CURRENT_TIMESTAMP, item.created_by FROM tb_item item
WHERE NOT EXISTS (SELECT 1 FROM tb_stock_balance balance WHERE balance.item_id = item.id);

CREATE INDEX IF NOT EXISTS idx_item_unit_item_active_status ON tb_item_unit(item_id, active, status);
CREATE INDEX IF NOT EXISTS idx_stock_movement_item_unit ON tb_stock_movement(item_unit_id);

COMMIT;
