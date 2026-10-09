-- Cadastre a permissão e atribua-a aos administradores.
-- Outros perfis podem recebê-la pela tela de perfis do sistema.
BEGIN;

INSERT INTO tb_permission (name, group_name)
VALUES ('RENTAL_REPORTS_READ', 'Relatórios')
ON CONFLICT (name) DO NOTHING;

INSERT INTO tb_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM tb_role role
JOIN tb_permission permission ON permission.name = 'RENTAL_REPORTS_READ'
WHERE role.authority = 'ROLE_ADMINISTRADOR'
AND NOT EXISTS (
    SELECT 1 FROM tb_role_permission existing
    WHERE existing.role_id = role.id AND existing.permission_id = permission.id
);

COMMIT;
