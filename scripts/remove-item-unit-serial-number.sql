-- Execute junto da atualização do backend para retirar a coluna legada de número de série.
-- O código patrimonial gerado e os registros de unidades e movimentações são preservados.
BEGIN;

ALTER TABLE tb_item_unit DROP COLUMN IF EXISTS serial_number;

COMMIT;
