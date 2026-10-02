# Padronização de financial_reports

## Escopo e referência

Foram lidos os 24 arquivos de implementação de `modules/organization/customers` e seus dois arquivos de testes antes das alterações. Também foram lidos os 12 arquivos originais de `modules/reports/financial_reports`.

O módulo solicitado é de relatórios financeiros. Matrícula, salário, cargo, setor, contratação, admissão, desligamento, foto e anexos de funcionários não pertencem a ele. Esses cadastros não foram alterados nem reproduzidos em relatórios.

Os padrões copiados de customers são pastas por responsabilidade, interfaces de service, implementação com injeção pelo construtor, dependências `private final`, `@Transactional(readOnly = true)` nas leituras, DTOs sem conversão de entidades, mapper explícito, constantes agrupadas e repositories anotados com SQL nativo. A lógica usa variáveis nomeadas, condicionais e loops simples. Os testes usam JUnit 5, Mockito, `@ExtendWith`, `@Mock`, `@InjectMocks`, `@BeforeEach`, objetos inicializados explicitamente, `when`, `verify` e assertions.

## Correspondência arquivo por arquivo

Caminhos da primeira coluna são relativos a `src/main/java/com/locadora_rdt_backend/modules/reports/financial_reports`.

| Arquivo final | Referência em customers | Adaptação |
| --- | --- | --- |
| `constants/FinancialReportConstants.java` | `constants/CustomerConstants.java` | Constantes por assunto, mensagens de validação e construtor privado; listas com `List.of`, sem factory auxiliar. |
| `controller/FinancialReportController.java` | `controller/CustomerController.java`, `controller/CustomerFileController.java` | Construtor, anotações, DTO local, `ResponseEntity`, `ContentDisposition` e headers explícitos. |
| `dto/FinancialReportDTO.java` | `dto/CustomerDTO.java` | Único DTO de leitura; construtor vazio, campos e acessores; sem conversão de entidades. |
| `dto/FinancialReportFileDTO.java` | `dto/CustomerFileViewDTO.java` | Nome do arquivo, tipo de conteúdo e bytes; metadados separados do corpo HTTP binário. |
| `dto/FinancialReportFilterDTO.java` | `dto/CustomerInsertDTO.java`, `dto/CustomerUpdateDTO.java` | DTO de entrada independente, com acessores e construtor vazio; preserva filtros e conversão de datas. Não foram adicionadas restrições de cadastro. |
| `mapper/FinancialReportMapper.java` | `mapper/CustomerMapper.java` | `@Component`, construtor vazio, criação explícita do DTO e setters no `toDTO`. |
| `model/FinancialReport.java` | `model/Customer.java` | Modelo passivo com dados do comparativo, construtor vazio e acessores. Não é uma entidade persistida. |
| `model/FinancialReportMonth.java` | `model/Address.java` | Objeto de dados aninhado, com campos e acessores, separado do DTO. |
| `model/FinancialReportSummary.java` | `model/Address.java` | Dados dos agrupamentos, sem métodos de cálculo, factories ou classe interna. |
| `model/FinancialReportTable.java` | `model/Address.java` | Dados de apresentação: título, colunas e linhas. As linhas são mapas porque o gerador Excel existente exige esse formato. |
| `model/FinancialReportType.java` | Organização de `model/Customer.java` | Enum específico dos sete relatórios; normalização e validação foram retiradas do model e colocadas no service. Não existe enum de tipos de relatório em customers. |
| `repository/FinancialReportReceivableRepository.java` | `repository/CustomerRepository.java` | `@Repository`, `JpaRepository`, `@Query(nativeQuery = true)` e `@Param`; consulta nomeada `find`. |
| `repository/FinancialReportPayableRepository.java` | `repository/CustomerRepository.java` | Mesmo padrão; preserva filtros de fornecedor e funcionário. |
| `service/FinancialReportService.java` | `service/CustomerService.java` | Interface com apenas as operações existentes e DTOs do módulo. |
| `service/FinancialReportServiceImpl.java` | `service/CustomerServiceImpl.java`, `service/CustomerFileServiceImpl.java` | Repositories e mapper injetados diretamente; dois métodos públicos; métodos privados simples para consultas, cálculos, agrupamentos e arquivos. |
| `src/test/java/com/locadora_rdt_backend/modules/financial_reports/service/FinancialReportServiceTests.java` | `CustomerServiceTests.java`, `CustomerFileServiceTests.java` | Mesma organização de mocks, setup, nomes e assertions, com exatamente quatro testes. |

## Inventário

### Alterados

- `constants/FinancialReportConstants.java`.
- `controller/FinancialReportController.java`.
- `dto/FinancialReportFilterDTO.java`.
- `model/FinancialReportType.java`.
- `repository/FinancialReportPayableRepository.java`.
- `repository/FinancialReportReceivableRepository.java`.
- `service/FinancialReportService.java`.
- `service/FinancialReportServiceImpl.java`.
- `src/main/java/com/locadora_rdt_backend/shared/constants/PermissionConstants.java`: declara a expressão `FINANCIALREPORTS_READ` que os endpoints já exigiam. Não cria permissões ou atribuições no banco.

### Criados

- `dto/FinancialReportDTO.java`.
- `dto/FinancialReportFileDTO.java`.
- `mapper/FinancialReportMapper.java`.
- `model/FinancialReport.java`.
- `model/FinancialReportMonth.java`.
- `model/FinancialReportSummary.java`.
- `model/FinancialReportTable.java`.
- `src/test/java/com/locadora_rdt_backend/modules/financial_reports/service/FinancialReportServiceTests.java`.
- Este documento.

### Removidos

- `dto/FinancialReportComparisonDTO.java`, incluindo seu DTO mensal interno.
- `service/FinancialReportQueryService.java`.
- `service/FinancialReportCalculationService.java`, incluindo a classe interna `SummaryValues`.
- `service/FinancialReportTableService.java`.

O módulo original estava sem rastreamento no Git. A classificação acima compara a implementação final com a cópia original preservada antes da edição.

## Estruturas antigas eliminadas

- Encadeamento de services auxiliares para consulta, cálculo e montagem.
- Imports de `ReportData`, `ReportFileDTO`, `ReportFormat`, `ReportTableSupport` e do antigo caminho de `JasperReportGenerator`, inexistentes no projeto atual.
- Dependência de um bean `Clock` que não está definido neste projeto.
- DTO de leitura com construtor agregador e DTO mensal interno.
- Classe de resumo interna com métodos de acumulação.
- `computeIfAbsent` e lambda para criar grupos.
- Factory genérica `immutableList`.
- Validação do tipo de relatório dentro do enum.
- Helper privado do controller para montar uma resposta que pode ser escrita diretamente, conforme `CustomerFileController`.

Não há `toDetailsDTO`, streams, `Function`, referências de métodos, builders ou herança nova no módulo.

## Diferenças necessárias e contratos preservados

- O módulo tem somente consultas e exportações. O mapper possui `toDTO`; `toEntity` e `updateEntity` não foram criados porque não existe inserção ou atualização de relatórios. A referência `CustomerFileMapper` também possui somente `toDTO`.
- Os models de relatório não são entidades JPA. Os repositories consultam as entidades existentes de contas a receber e a pagar. Não há tabelas novas.
- `FinancialReportDTO` é a única resposta de leitura estruturada. `FinancialReportFileDTO` é o transporte de arquivo, equivalente a `CustomerFileViewDTO`, e não uma segunda visão de detalhes.
- Meses, tabelas e resumos têm objetos passivos para os dados necessários aos relatórios. As diferenças de campos são próprias deste domínio.
- Excel usa `shared.reports.generator.JasperReportGenerator.generateExcel`, que já existe. PDF usa OpenPDF, já utilizada pelo projeto e disponível nas dependências. Nenhum framework ou biblioteca foi adicionado. O gerador compartilhado não foi modificado.
- Permanecem os endpoints `GET /reports/financial-reports/{reportType}/{format}` e `GET /reports/financial-reports/comparison`, os nomes dos arquivos, a permissão exigida e os campos JSON do comparativo.
- Permanecem os sete tipos: contas a receber, contas a pagar, financeiro, sintético por cliente, sintético por fornecedor, sintético por funcionário e balanço anual.
- Permanecem os totais calculados sobre `amount`, a classificação de status, os agrupamentos por nome, os 12 meses, a data de pagamento do balanço anual, o período selecionado no comparativo e a conversão de `createdAt` em UTC nesse comparativo. Esta refatoração não alterou os critérios financeiros existentes.
- As duas consultas declaradas continuam 100% nativas. Foram preservados os joins, filtros, valores desabilitadores, busca sem distinguir maiúsculas/minúsculas e `ORDER BY r.id DESC`. Não havia paginação nesses endpoints e ela não foi adicionada.
- A busca usa `CAST(:search AS TEXT)` para manter a comparação de texto e permitir o parâmetro nulo no PostgreSQL.

## Métodos públicos e testes

Há somente um `ServiceImpl` no módulo. Seu construtor não entra na contagem.

| ServiceImpl | Método público | Teste de sucesso | Teste de falha |
| --- | --- | --- | --- |
| `FinancialReportServiceImpl` | `generate(String, String, FinancialReportFilterDTO)` | `generateShouldReturnAnnualBalanceFile` | `generateShouldThrowExceptionWhenReportTypeIsInvalid` |
| `FinancialReportServiceImpl` | `comparison(FinancialReportFilterDTO)` | `comparisonShouldReturnFinancialReport` | `comparisonShouldThrowExceptionWhenRepositoryFails` |

`FinancialReportServiceTests`: **4 testes**. Não há testes parametrizados, loops de cenários, testes diretos de privados nem novas classes de testes para mapper, validator, controller ou repository.

O cenário de sucesso de `generate` produz um XLSX real por meio do método real do gerador já existente, verifica arquivo e metadados e captura as 13 linhas do balanço anual. O cenário de sucesso de `comparison` captura o modelo entregue ao mapper e verifica totais, contagens e meses usando um único conjunto de filtros.

## Validação

- Comparação de hashes: os 26 arquivos de customers, incluindo seus testes, permaneceram idênticos.
- Auditoria dos services: dois métodos públicos e dois testes por método.
- Auditoria dos repositories: uma consulta nativa em cada repository, sem métodos derivados declarados.
- Revisão de todos os arquivos finais contra as referências da tabela acima.
- `git diff --check`: aprovado.
- Build limpo e suíte completa: **344 testes, zero falhas, zero erros, zero testes ignorados**, incluindo os quatro novos testes e `LocadoraRdtBackendApplicationTests`.
- Artefato: `target/locadora_rdt_backend-0.0.1-SNAPSHOT.jar`.

Comando de validação:

```bash
EMAIL_USERNAME=rdt-test EMAIL_PASSWORD=rdt-test \
OAUTH_CLIENT_ID=rdt-test OAUTH_CLIENT_SECRET=rdt-test JWT_SECRET=rdt-test \
mvn -Dspring.jpa.hibernate.ddl-auto=none \
    -Dspring.datasource.initialization-mode=never \
    -Dspring.datasource.hikari.read-only=true clean package
```

Os valores de email, OAuth e JWT acima são fictícios e utilizados somente na execução local. O datasource utiliza a configuração local já existente, com DDL e inicialização SQL desativados e conexões somente leitura. Nenhum arquivo de configuração foi editado, nenhum dado foi inserido/atualizado/excluído e nenhuma atualização automática de esquema foi executada.

Limitações: não foi realizada navegação autenticada pelo frontend. A quantidade definida de testes cobre um cenário de sucesso e um de falha por método; não exercita todos os tipos de relatório, todos os filtros, a exportação PDF ou a execução das consultas dos novos repositories contra dados reais. O teste de contexto confirma a inicialização da aplicação, mas não substitui essas verificações ponta a ponta.
