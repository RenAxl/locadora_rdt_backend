-- Dados de teste para o banco local loc_rdt.
-- Os CPFs são fictícios; não representam documentos dos usuários.
-- Execução manual: psql -h localhost -U postgres -d loc_rdt -v ON_ERROR_STOP=1 -f scripts/populate-customers.sql
BEGIN;

-- Cinco clientes baseados em usuários já cadastrados.
INSERT INTO tb_customer (
    name, cpf, email, phone, active,
    street, number, complement, neighborhood, city, state, zip_code,
    photo_data, photo_content_type, created_at, created_by
)
SELECT
    u.name, dados.cpf, u.email, u.telephone, u.active,
    u.street, u.number, u.complement, u.neighborhood, u.city, u.state, u.zip_code,
    u.photo_data, u.photo_content_type, CURRENT_TIMESTAMP, 'Carga de teste - CPF fictício'
FROM tb_user u
JOIN (VALUES
    (4, '90000000175'),
    (6, '90000000256'),
    (7, '90000000337'),
    (9, '90000000418'),
    (10, '90000000507')
) AS dados(user_id, cpf) ON dados.user_id = u.id
ON CONFLICT (cpf) DO NOTHING;

-- Cinco clientes fictícios adicionais.
INSERT INTO tb_customer (
    name, cpf, email, phone, active,
    street, number, complement, neighborhood, city, state, zip_code,
    created_at, created_by
)
VALUES
    ('Ana Clara Oliveira', '90000000680', 'ana.clara.cliente@example.com', '31990001001', true,
     'Rua de Teste 1', '100', NULL, 'Centro', 'Belo Horizonte', 'MG', '30110000',
     CURRENT_TIMESTAMP, 'Carga de teste - CPF fictício'),
    ('Bruno Ferreira Costa', '90000000760', 'bruno.costa.cliente@example.com', '31990001002', true,
     'Rua de Teste 2', '200', NULL, 'Centro', 'Belo Horizonte', 'MG', '30110000',
     CURRENT_TIMESTAMP, 'Carga de teste - CPF fictício'),
    ('Camila Rodrigues Souza', '90000000841', 'camila.souza.cliente@example.com', '31990001003', true,
     'Rua de Teste 3', '300', NULL, 'Centro', 'Belo Horizonte', 'MG', '30110000',
     CURRENT_TIMESTAMP, 'Carga de teste - CPF fictício'),
    ('Lucas Henrique Almeida', '90000000922', 'lucas.almeida.cliente@example.com', '31990001004', true,
     'Rua de Teste 4', '400', NULL, 'Centro', 'Belo Horizonte', 'MG', '30110000',
     CURRENT_TIMESTAMP, 'Carga de teste - CPF fictício'),
    ('Mariana Pereira Rocha', '90000001066', 'mariana.rocha.cliente@example.com', '31990001005', true,
     'Rua de Teste 5', '500', NULL, 'Centro', 'Belo Horizonte', 'MG', '30110000',
     CURRENT_TIMESTAMP, 'Carga de teste - CPF fictício')
ON CONFLICT (cpf) DO NOTHING;

COMMIT;
