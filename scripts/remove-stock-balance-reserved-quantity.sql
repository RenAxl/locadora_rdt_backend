-- Script antigo mantido por compatibilidade. A migração completa está em refactor-generic-stocks.sql.
BEGIN;
ALTER TABLE tb_stock_balance DROP COLUMN IF EXISTS reserved_quantity;
COMMIT;
