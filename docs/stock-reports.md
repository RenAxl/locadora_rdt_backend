# Relatórios de estoque

O módulo foi criado em `modules/reports/stock_reports`, seguindo o padrão de
`financial_reports`: controller, DTOs, mapper, models de leitura, repositories
com SQL nativo, interface de service e implementação com injeção pelo construtor.
No frontend fica em `features/reports/stock-reports`.

## Relatórios

| Tipo | Conteúdo | Filtros específicos |
| --- | --- | --- |
| `balances` | Quantidades atuais por item, mínimo e alerta | Itens ativos/inativos/todos |
| `low-stock` | Itens com quantidade disponível menor que o mínimo | Itens ativos/inativos/todos |
| `item-units` | Unidade, código patrimonial, situação, conservação, compra e atividade | Unidades ativas/com baixa/todas, situação e conservação |
| `movements` | Data, item, unidade quando existente, tipo, quantidade, motivo, responsável e situações anterior/nova | Tipo de movimentação e período |

Todos permitem busca textual, categoria e item, além de PDF e Excel (`xlsx`).
A busca de saldos considera item/categoria; a de unidades também considera
código patrimonial; a de movimentações também considera código e motivo.

Os saldos são calculados pelas unidades ativas, sem persistir novas quantidades.
Unidades AVAILABLE de itens/categorias inativos são consideradas indisponíveis,
conforme o cálculo do módulo de estoque. Itens sem unidades aparecem com saldo
zero. Na ausência de configuração de saldo, o mínimo é zero.

O relatório de unidades permite consultar unidades com baixa. O histórico
mantém movimentações de unidades com baixa e movimentações coletivas sem uma
unidade associada. Para ADJUSTMENT, a quantidade corresponde ao total ativo final
informado na operação existente, identificado no cabeçalho e no tipo exibido.

Datas e horários das movimentações são apresentados em UTC. O período inclui
todo o dia final selecionado; a consulta compara instantes, com o limite final
exclusivo no início do dia seguinte.

Relatórios vazios mantêm título, cabeçalhos e a mensagem “Nenhum registro
encontrado.”.

## Endpoints

Todos exigem `STOCK_REPORTS_READ`.

| Método | Endpoint | Resposta |
| --- | --- | --- |
| GET | `/reports/stock-reports/{reportType}/{format}` | Arquivo PDF/XLSX com Content-Type e Content-Disposition |
| GET | `/reports/stock-reports/summary` | Contagem de itens, quantidades por situação e itens abaixo do mínimo |
| GET | `/reports/stock-reports/options` | Categorias com itens cadastrados e itens para os filtros |

Parâmetros opcionais: `search`, `categoryId`, `itemId`, `active`, `status`,
`conditionStatus`, `movementType`, `startDate` e `endDate`. Datas usam
`yyyy-MM-dd`; enums usam os valores genéricos existentes no estoque. `ALL` ou
ausência desabilita os filtros de enums; ausência de `active` inclui ambos.
IDs inválidos, enums inválidos, tipos/formatos inválidos e períodos invertidos
retornam erro de validação HTTP 400.

## Frontend

Acesso: **Relatórios → Relatórios de Estoque**, rota
`/reports/stock-reports`. O menu e a rota respeitam a nova permissão.

Os filtros específicos aparecem conforme o relatório selecionado. A tela possui
geração de PDF, download de Excel, limpeza dos filtros e resumo com barras de
disponibilidade atual. O resumo considera a categoria e o item selecionados,
independentemente do período histórico ou dos filtros específicos da exportação.
O botão “Atualizar resumo” aplica a seleção ao resumo.

Os filtros são carregados pelo endpoint próprio do relatório, permitindo acesso
com `STOCK_REPORTS_READ` sem exigir permissões de cadastro de itens/categorias.

## Banco e acesso

Não foram criadas tabelas nem alterado o esquema do estoque.

O script `scripts/add-stock-reports-permission.sql` cadastra a permissão e a
associa a `ROLE_ADMINISTRADOR`. Pode ser executado novamente sem duplicações.
Outros perfis podem receber a permissão pela tela de perfis existente.

Esse script foi aplicado à base local `loc_rdt`. As contagens de categorias,
itens, unidades, saldos e movimentações foram verificadas antes/depois e
permaneceram iguais. Ao atualizar a aplicação, reinicie o backend e faça login
novamente para carregar a nova permissão.

## Exportação e compatibilidade

PDF utiliza a biblioteca já existente e Excel utiliza o gerador Jasper
compartilhado. Foi adicionada uma sobrecarga simples de `generateExcel` com
`keepFullText`, utilizada pelo estoque para preservar nomes e motivos longos.
O método existente de três parâmetros conserva o comportamento anterior.
Não foram adicionadas dependências nem modificados endpoints financeiros.

Referência da configuração de texto completo:
[JasperReports 6.21.3](https://jasperreports.sourceforge.net/6.21.3/sample.reference/stretch/index.html).

## Verificação

- Backend: `mvn clean test`, **452 testes**, sem falhas, erros ou ignorados.
- Novos testes de relatórios no backend: **29 cenários** em service, controller
  e repository, incluindo consultas reais em PostgreSQL isolado, autorização,
  validações, limites de datas e arquivos PDF/Excel dos quatro relatórios.
- Frontend: `npm test -- --watch=false --browsers=ChromeHeadless`, **86 testes**
  aprovados, incluindo **18 novos cenários** de mapper, service e componente.
- Builds: `mvn -DskipTests package` e `npm run build` aprovados.
- Script de permissão validado em banco isolado, incluindo execução repetida.
- `git diff --check` aprovado nos dois projetos.

Os testes de componentes renderizam o template. Não foi realizada navegação
manual autenticada no navegador.
