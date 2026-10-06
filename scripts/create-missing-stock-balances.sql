-- Execute após scripts/refactor-generic-stocks.sql em bases já atualizadas.
-- Cria somente a configuração do mínimo; as quantidades são calculadas pelas unidades.
BEGIN;

INSERT INTO tb_stock_balance (version, item_id, minimum_quantity, created_at, created_by)
SELECT 0, item.id, 0, CURRENT_TIMESTAMP, item.created_by
FROM tb_item item
WHERE NOT EXISTS (SELECT 1 FROM tb_stock_balance balance WHERE balance.item_id = item.id);

COMMIT;
