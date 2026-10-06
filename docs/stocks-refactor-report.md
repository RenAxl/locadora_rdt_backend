# Refatoração do módulo stocks

## Arquivos criados

- `scripts/refactor-generic-stocks.sql`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/dto/ItemUnitStatusUpdateDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/enums/ItemUnitCondition.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/enums/ItemUnitStatus.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/repository/StockQuantitySummary.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_movements/enums/StockMovementType.java`
- `src/test/java/com/locadora_rdt_backend/modules/stocks/repository/StockRepositoryTests.java`
- `docs/stocks-refactor-report.md`

## Arquivos alterados

- `scripts/create-missing-stock-balances.sql`
- `scripts/remove-stock-balance-reserved-quantity.sql`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/categories/controller/CategoryController.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/categories/repository/CategoryRepository.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/categories/service/CategoryServiceImpl.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/constants/ItemUnitConstants.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/controller/ItemUnitController.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/dto/ItemUnitDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/dto/ItemUnitInsertDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/dto/ItemUnitUpdateDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/mapper/ItemUnitMapper.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/model/ItemUnit.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/repository/ItemUnitRepository.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/service/ItemUnitService.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/item_units/service/ItemUnitServiceImpl.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/constants/ItemConstants.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/dto/ItemInsertDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/dto/ItemUpdateDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/model/Item.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/repository/ItemRepository.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/service/ItemServiceImpl.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_balances/constants/StockBalanceConstants.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_balances/dto/StockBalanceDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_balances/mapper/StockBalanceMapper.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_balances/model/StockBalance.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_balances/repository/StockBalanceRepository.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_balances/service/StockBalanceService.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_balances/service/StockBalanceServiceImpl.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_movements/constants/StockMovementConstants.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_movements/dto/StockMovementDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_movements/dto/StockMovementInsertDTO.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_movements/mapper/StockMovementMapper.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_movements/model/StockMovement.java`
- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_movements/service/StockMovementServiceImpl.java`
- `src/test/java/com/locadora_rdt_backend/modules/stocks/categories/service/CategoryServiceTests.java`
- `src/test/java/com/locadora_rdt_backend/modules/stocks/item_units/service/ItemUnitServiceTests.java`
- `src/test/java/com/locadora_rdt_backend/modules/stocks/items/service/ItemServiceTests.java`
- `src/test/java/com/locadora_rdt_backend/modules/stocks/stock_balances/service/StockBalanceServiceTests.java`
- `src/test/java/com/locadora_rdt_backend/modules/stocks/stock_movements/controller/StockRequestValidationTests.java`
- `src/test/java/com/locadora_rdt_backend/modules/stocks/stock_movements/service/StockMovementServiceTests.java`

## Arquivos removidos

- `src/main/java/com/locadora_rdt_backend/modules/stocks/stock_balances/dto/StockBalanceUpdateDTO.java`

O inventário acima compara o estado ao início desta refatoração. Alterações anteriores no workspace foram preservadas. Nenhum arquivo do frontend, de configuração compartilhada ou de outro módulo de negócio foi alterado nesta tarefa.

## Alterações realizadas

- Status operacionais: `AVAILABLE`, `UNAVAILABLE`, `MAINTENANCE`, `DAMAGED`, `LOST`.
- Condições físicas independentes: `NEW`, `GOOD`, `FAIR`, `DAMAGED`.
- Movimentações: `ENTRY`, `EXIT`, `ADJUSTMENT`, `STATUS_CHANGE`.
- Total, disponibilidade, manutenção, danos e perdas são calculados pelas unidades ativas em uma consulta de contagem por item. A entidade do saldo não armazena essas quantidades.
- A edição manual de quantidades pelo saldo foi removida da interface, implementação, mapper e DTO. Permanece apenas a atualização do mínimo.
- Entrada cria unidades; saída desativa unidades; ajuste corrige o total físico. Nenhuma dessas operações grava quantidades derivadas no saldo.
- Alterações de status e condição geram histórico vinculado à unidade. Status antigo, status novo, motivo, data e responsável são registrados.
- DELETE de unidade e DELETE em lote fazem baixa lógica, preservando unidades e histórico. Repetir uma baixa não cria outra saída.
- Reativar uma unidade registra entrada. Uma operação temporária deve alterar o status, mantendo `active = true`.
- A operação em lote valida os IDs antes de alterar unidades e bloqueia os itens em ordem crescente. Operações físicas usam a ordem de bloqueio item → unidade.
- Entrada, saída, ajuste, alterações de unidade e seus históricos são transacionais. Foi testada a reversão de uma unidade já persistida quando a gravação do histórico falha.
- Ativar/desativar itens e categorias atualiza versão, data e responsável, evitando que uma edição concorrente ignore a mudança de ativação.
- O preço foi preservado como informação opcional e não negativa. Não participa de disponibilidade, movimentações ou saldos.

## Arquitetura

| Estrutura | Responsabilidade |
| --- | --- |
| Category | Classificar os itens. |
| Item | Identificar o recurso, material, produto ou equipamento. Preserva descrição, categoria, imagem, ativação e preço opcional. |
| ItemUnit | Identificar cada unidade física. Status, condição, patrimônio gerado automaticamente, compra, observações e ativação permanecem separados. |
| StockBalance | Armazenar a configuração do mínimo e apresentar contagens calculadas pelas unidades. |
| StockMovement | Registrar alterações físicas e operacionais. A referência à unidade é opcional para operações agregadas e registros antigos. |

O fluxo de dados é `Category → Item → ItemUnit`, com `StockBalance` resumindo as unidades e `StockMovement` registrando alterações. Os serviços de unidades e movimentações não dependem do serviço ou repositório de saldos para alterar quantidades. Foram mantidas apenas dependências comuns do projeto para autenticação, permissões, respostas HTTP e exceptions.

### Contagens

As contagens são separadas, como no exemplo solicitado:

```text
Total = Disponíveis + Indisponíveis + Manutenção + Danificados + Perdidos
```

- Unidades inativas não entram em nenhuma contagem.
- `unavailableQuantity` representa `UNAVAILABLE`. Uma unidade com status `AVAILABLE` também entra nessa contagem se seu item ou categoria estiver inativo, preservando o bloqueio de disponibilidade já autorizado.
- `maintenanceQuantity`, `damagedQuantity` e `lostQuantity` são categorias separadas, sem duplicação.
- `damagedQuantity` conta o status operacional `DAMAGED`. A condição física `DAMAGED` é independente e não muda automaticamente o status.
- `LOST` permanece no total de registros ativos até a baixa definitiva.
- `lowStock = availableQuantity < minimumQuantity`. Igualdade com o mínimo não gera alerta.
- Versão e datas do saldo representam mudanças de configuração. Alterações físicas aparecem no histórico.

### Operações

- `ENTRY`: quantidade positiva; item e categoria precisam estar ativos. Gera códigos `ITEM-<id>-<código aleatório>`, unidades ativas, status `AVAILABLE` e condição `GOOD`.
- `EXIT`: quantidade positiva; seleciona unidades efetivamente disponíveis e faz baixa definitiva. Pode indicar `itemUnitId` para uma única unidade disponível.
- `ADJUSTMENT`: quantidade é o total ativo desejado, inclusive zero. Aumentos criam unidades; reduções retiram apenas unidades disponíveis. Uma redução insuficiente é rejeitada integralmente.
- `STATUS_CHANGE`: exige `itemUnitId`, quantidade igual a um e status de destino. Altera apenas uma unidade ativa, sem entrada ou saída física. No POST, repetir o mesmo status é rejeitado; no PATCH da unidade, é uma operação sem efeito e sem histórico duplicado.
- Baixa explícita da unidade, por DELETE ou alteração de `active`, também permite encerrar unidades danificadas ou perdidas, sem exigir um status temporário artificialmente disponível.
- Para alterações temporárias, use `UNAVAILABLE` e depois `AVAILABLE`, com motivo livre. O estoque registra o texto sem interpretar o processo externo.

## Banco de dados

Execute `scripts/refactor-generic-stocks.sql` antes de iniciar o backend atualizado. `ddl-auto: update` não substitui essa migração.

A migração:

1. Converte status antigos `RENTED` e `RESERVED` para `UNAVAILABLE`.
2. Converte movimentações antigas `MAINTENANCE` e `RELEASE` para `STATUS_CHANGE`, preenchendo o status anterior e o novo.
3. Acrescenta `item_unit_id` nullable, `previous_status` e `new_status` em `tb_stock_movement`, com FK para a unidade e sem exclusão em cascata.
4. Cria constraints de status, condição, tipo, quantidade e mínimo, além de índices das unidades e do histórico.
5. Remove `total_quantity`, `unavailable_quantity` e a coluna legada `reserved_quantity` do saldo. As unidades permanecem intactas.
6. Permite preço nulo em `tb_item`.
7. Cria a configuração de mínimo zero para itens antigos sem saldo.

Valores antigos desconhecidos interrompem a migração e revertem a transação, em vez de apagar ou adivinhar informações. IDs, códigos, condições, mínimos existentes, responsáveis, motivos e datas do histórico são preservados. Não foi inventado um vínculo de unidade para históricos antigos que não possuíam essa informação.

Os scripts de saldos faltantes e remoção da coluna antiga foram adequados para não depender das quantidades removidas. A migração foi testada com PostgreSQL 16, incluindo repetição e rollback. **A base `loc_rdt` não foi alterada durante esta refatoração.**

## Frontend

Nenhum arquivo do frontend foi alterado. Impactos identificados:

| Endpoint/DTO | Alteração | Ajuste necessário no frontend |
| --- | --- | --- |
| `/inventory/categories` | Novo caminho genérico; `/rental/categories` permanece como alias de compatibilidade. | Preferir o novo caminho na configuração da API. O caminho antigo continua funcionando. |
| ItemInsertDTO / ItemUpdateDTO / ItemDTO | `price` é opcional e aceita zero. | Tornar o campo opcional, permitir zero e tratar preço ausente quando necessário. Campos existentes foram preservados. |
| ItemUnitDTO / ItemUnitInsertDTO / ItemUnitUpdateDTO | JSON mantém strings, agora validadas por enums. Status genéricos e condição `FAIR`. | Atualizar rótulos e opções de condição; remover rótulos de status específicos do negócio. |
| `PATCH /inventory/item-units/{id}/status` | Novo endpoint; body `{ "status": "UNAVAILABLE", "reason": "Uso temporário" }`. Retorna ItemUnitDTO. | Acrescentar serviço e ações para os cinco status quando essas ações forem expostas na interface. |
| `PATCH /inventory/item-units/{id}/maintenance` | Mantido por compatibilidade. Agora registra `STATUS_CHANGE`. | Serviço atual continua funcionando; atualizar a interpretação do histórico. |
| `DELETE /inventory/item-units/{id}` e `/all` | Mantêm 204, porém fazem baixa lógica. A unidade aparece inativa em consultas posteriores. | Ajustar texto da ação para baixa; considerar filtro de ativos na listagem. |
| StockBalanceDTO | Adicionados `maintenanceQuantity`, `damagedQuantity`, `lostQuantity`. Demais campos existentes foram preservados no JSON. | Acrescentar campos ao DTO, model, mapper, tabela e exportação. |
| `GET /inventory/stock-balances` | `unavailableQuantity` passa a ser uma categoria separada. Quantidades são calculadas. `orderBy` aceita dados do item/categoria, mínimo e metadados, e rejeita quantidades calculadas. | Mostrar as contagens separadamente e evitar ordenação por quantidades. O frontend atual não habilita ordenação dessas colunas. |
| StockBalanceDTO.lowStock | Agora usa comparação estrita abaixo do mínimo. | Igualdade com o mínimo deixa de exibir alerta. |
| StockBalanceUpdateDTO | Removido somente do backend; não possuía endpoint público de edição. | Nenhuma chamada pública existente precisa ser removida. A edição do mínimo continua no mesmo endpoint. |
| StockMovementInsertDTO | `type` exige o nome exato do enum. Adicionados `itemUnitId` e `status`. | Continuar enviando tipos em maiúsculas; adicionar campos para saída unitária e alteração de status, se expostas. |
| StockMovementDTO | Adicionados `itemUnitId`, `assetCode`, `previousStatus`, `newStatus`; `STATUS_CHANGE` substitui os tipos antigos de manutenção. | Atualizar DTO/model/mapper, rótulos e histórico. Tratar campos nullable em movimentos agregados e antigos. |

Exemplo de alteração temporária, sem movimentação de saída:

```http
PATCH /inventory/item-units/10/status
Content-Type: application/json

{"status":"UNAVAILABLE","reason":"Uso temporário da unidade"}
```

Todos os demais endpoints de cadastro, consulta, imagem, mínimo e ativação foram preservados. Não há endpoints de venda, aluguel, pedido ou reserva comercial no núcleo do estoque.

## Testes

Resultado final: **481 testes no projeto, 138 de stocks, zero falhas, zero erros e zero testes ignorados**.

- CategoryServiceTests: 18.
- ItemServiceTests: 20.
- ItemUnitServiceTests: 32.
- StockBalanceServiceTests: 12.
- StockMovementServiceTests: 25.
- StockRequestValidationTests: 19.
- StockRepositoryTests: 12.

JUnit e Mockito cobrem sucesso e falha das operações, quantidade inteira, mínimo não negativo, código patrimonial automático, enums, baixa lógica, histórico, ativação, manutenção e ajustes. Os testes com PostgreSQL verificam contagens, consultas paginadas, enums persistidos, relacionamento do histórico, ausência de preço, versão/auditoria e rollback após falha no histórico.

Foi executado `mvn test` completo, incluindo compilação e `contextLoads`, em uma instância PostgreSQL temporária. A configuração de datasource foi substituída apenas na execução de testes; não houve alteração no ambiente ou na configuração do projeto. Credenciais de autenticação e e-mail utilizadas nessa execução eram fictícias.

## Problemas encontrados

Fora de stocks, sem alterações:

- `app.mail.enabled=false` remove o bean EmailService, mas serviços de identidade ainda o exigem na construção. Isso impede o carregamento completo da aplicação nessa configuração. Para verificar o contexto nesta tarefa, o bean foi habilitado com configuração de teste e destino SMTP local.
- O handler compartilhado de JSON inválido apresenta orientação sobre quantidades inteiras também quando o erro é um enum inválido. O status HTTP permanece 400; a mensagem compartilhada precisa de revisão em tarefa própria.

Os impactos de interface estão descritos acima e não foram corrigidos automaticamente no frontend.

## Melhorias futuras

- Adaptar o frontend aos status e contagens genéricos.
- Acrescentar paginação/filtros específicos do histórico e de unidades ativas quando houver requisito.
- Avaliar controle por quantidade sem identificação individual e quantidades fracionárias em uma tarefa própria. Nesta refatoração foi preservado o controle unitário com quantidades inteiras.
- Implementar ordenação de contagens calculadas se a interface passar a exigir esse comportamento.

Não foram implementados depósitos, múltiplos estoques, localização, fornecedores, inventário, manutenção completa, integração financeira ou processos comerciais.

**O módulo `stocks` permaneceu genérico e não possui dependência de regras específicas de outros módulos de negócio.** O único caminho de categoria com nomenclatura antiga é um alias HTTP para preservar compatibilidade, e os valores antigos aparecem somente na migração de dados.


## Atualização do cadastro manual de unidades físicas

O cadastro manual também gera o código patrimonial no backend, no padrão `ITEM-<id do item>-<8 caracteres iniciais de UUID>`. O código é preservado na edição. `assetCode` saiu dos DTOs de cadastro e edição e continua nos DTOs de consulta e histórico. `serialNumber` foi removido da entidade, dos DTOs e do mapper, e a consulta nativa deixou de selecionar seu alias.

A retirada da coluna legada está em `scripts/remove-item-unit-serial-number.sql`, para execução junto da atualização do backend. A aplicação atualizada também funciona enquanto a coluna nullable legada existir. Os códigos e as referências do histórico são preservados pelo script.

Validação desta atualização: **486 testes aprovados**, incluindo **143 de stocks**. A migração foi verificada em PostgreSQL isolado, incluindo sua repetição e a preservação dos códigos e do relacionamento com o histórico.


## Consulta de unidades ativas e com baixa

O endpoint `GET /inventory/item-units` aceita o parâmetro opcional `active`: `true` retorna unidades ativas; `false` retorna unidades com baixa; sem o parâmetro retorna todas, preservando o contrato anterior. O filtro é aplicado tanto na consulta paginada como na contagem de registros, junto dos filtros de nome e item.

A baixa continua preservando a unidade e seu histórico. Não houve alteração de schema nem exclusão física de dados. Os testes cobrem os três filtros, a paginação, a baixa e a reentrada. Resultado: 492 testes aprovados no backend, incluindo 149 de stocks, em PostgreSQL isolado.
