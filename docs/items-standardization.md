# Padronização de items com referência em customers

## Escopo e leitura antes das alterações

Foram lidos os 24 arquivos de produção de `modules/organization/customers` e seus dois arquivos de testes, além dos 14 arquivos originais de `modules/stocks/items`, antes de editar qualquer arquivo. Não havia testes de items no projeto atual. A comparação final utilizou cópias dos arquivos originais e hashes SHA-256 dos 26 arquivos de customers; a pasta items já estava não rastreada pelo Git ao iniciar o trabalho.

O alvo seguido foi o módulo `items`, explicitamente indicado no pedido. Seus dados são de estoque: categoria, preço, imagem, unidades físicas, patrimônio, número de série, condição, compra, disponibilidade, versão, auditoria e status ativo. Matrícula, salário, cargo, setor, admissão, desligamento, tipo de contratação e anexos de funcionários não existem nesses arquivos. Não foram acrescentadas regras ou estruturas de employees.

Os padrões da referência são pastas por responsabilidade; nomes Customer/CustomerDTO/CustomerInsertDTO/CustomerUpdateDTO; DTOs independentes com acessores em pares; mappers explícitos; services com interface, injeção por construtor e dependências `private final`; variáveis explícitas; `Optional` com `if`; listas com `ArrayList` e `for`; constantes qualificadas e agrupadas; transações somente leitura nas consultas; tradução direta de exceções; controllers com `ControllerResponseBuilder`, permissões e `ResponseEntity.ok().body(...)`.

Nos testes, a referência utiliza JUnit 5 e Mockito, `@ExtendWith`, `@Mock`, `@InjectMocks`, `@BeforeEach`, setup explícito, nomes `metodoShould...`, `when`, `verify`, `assertThrows` e assertions simples. A quantidade obrigatória do pedido foi aplicada aos métodos finais de items, sem copiar os cenários extras de CustomerFileServiceTests.

## Correspondência de todos os arquivos originais

Caminhos de items relativos a `src/main/java/com/locadora_rdt_backend/modules/stocks/items/`. Os equivalentes de produção pertencem a `modules/organization/customers/`.

| Arquivo original de items | Equivalente ou mais próximo em customers | Resultado |
| --- | --- | --- |
| `constants/ItemConstants.java` | `constants/CustomerConstants.java` | Mesma organização; validações de imagem locais e nomes de constantes padronizados. |
| `controller/ItemController.java` | `controller/CustomerController.java` | Mesmo estilo, organização dos métodos e tratamento da resposta binária. |
| `dto/ItemDTO.java` | `dto/CustomerDTO.java` | Único DTO de leitura com versão, categoria, preço, imagem e auditoria. |
| `dto/ItemDetailsDTO.java` | `dto/CustomerDTO.java` | Campos incorporados em ItemDTO; arquivo removido. |
| `dto/ItemInsertDTO.java` | `dto/CustomerInsertDTO.java` | Validações, construtor e acessores no mesmo estilo; restrições de itens preservadas. |
| `dto/ItemUpdateDTO.java` | `dto/CustomerUpdateDTO.java` | Mesmo estilo; sem campos ou operações novos. |
| `mapper/ItemMapper.java` | `mapper/CustomerMapper.java` | Apenas `toDTO`, `toEntity` e `updateEntity`; mapeamento explícito. |
| `model/Item.java` | `model/Customer.java` | Acessores, callbacks, equals/hashCode e construtor no estilo da referência; schema preservado. |
| `model/ItemUnit.java` | `model/CustomerFile.java` e `model/Customer.java` | Entidade subordinada, com associação, acessores, callbacks e auditoria; regras de unidade preservadas. |
| `repository/ItemRepository.java` | `repository/CustomerRepository.java` | Quatro consultas SQL nativas, countQuery explícita e métodos de escrita com `@Modifying`. |
| `repository/ItemUnitRepository.java` | `repository/CustomerFileRepository.java` e `repository/CustomerRepository.java` | Doze consultas SQL nativas; filtros, ordenação, paginação e bloqueios preservados. |
| `service/ItemService.java` | `service/CustomerService.java` | Interface reorganizada; leitura binária com DTO; consulta de entidade sem consumidor removida. |
| `service/ItemServiceImpl.java` | `service/CustomerServiceImpl.java` | Fluxos diretos, transações e organização da referência; resolução de categoria com repository existente. |
| `service/ItemUnitReservationChecker.java` | Sem equivalente nem consumidor no projeto atual | Interface da integração antiga sem implementação removida. |

O novo `dto/ItemImageDTO.java` corresponde a `dto/CustomerPhotoDTO.java`. O novo `src/test/java/com/locadora_rdt_backend/modules/items/service/ItemServiceTests.java` corresponde a `modules/customers/service/CustomerServiceTests.java`.

Não foram criados controller ou service de arquivos, validators de duplicidade, testes de mapper/controller/repository/validator ou operações novas. As constraints existentes dos DTOs e do banco continuam responsáveis pelas validações aplicáveis. A unicidade do nome do item e dos códigos de patrimônio/números de série permanece nas entidades.

## Arquivos alterados, criados e removidos

Alterados em `modules/stocks/items/`:

- `constants/ItemConstants.java`.
- `controller/ItemController.java`.
- `dto/ItemDTO.java`.
- `dto/ItemInsertDTO.java`.
- `dto/ItemUpdateDTO.java`.
- `mapper/ItemMapper.java`.
- `model/Item.java`.
- `model/ItemUnit.java`.
- `repository/ItemRepository.java`.
- `repository/ItemUnitRepository.java`.
- `service/ItemService.java`.
- `service/ItemServiceImpl.java`.

Também alterado: `src/main/java/com/locadora_rdt_backend/shared/constants/PermissionConstants.java`, somente para declarar as quatro expressões de autorização que ItemController já utilizava e que estavam ausentes no projeto atual. Foram preservadas as autoridades `ITEMS_READ`, `CUSTOMERS_ITEMS_READ`, `ITEMS_WRITE` e `ITEMS_DELETE`, sem alterar registros de permissões no banco.

Criados:

- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/dto/ItemImageDTO.java`.
- `src/test/java/com/locadora_rdt_backend/modules/items/service/ItemServiceTests.java`.
- `docs/items-standardization.md`.

Removidos:

- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/dto/ItemDetailsDTO.java`.
- `src/main/java/com/locadora_rdt_backend/modules/stocks/items/service/ItemUnitReservationChecker.java`.

Também removidos `toDetailsDTO`, `copyToEntity`, `toCategoryDTO`, `normalizeName`, `findEntityById` de ItemService/ItemServiceImpl, construtores com parâmetros sem uso, imports e constantes sem uso. Todas as referências afetadas foram revisadas.

## Estruturas antigas eliminadas e diferenças justificadas

Eliminados a separação entre leitura resumida/detalhada, mapper duplicado, guardas de nulos sem uso no fluxo do mapper, helper de categoria, conversões compactadas, stream em deleteAll, referências de método no service, `orElseThrow` e imports do projeto antigo. ItemServiceImpl agora usa `shared.security.AuthenticationFacade`; não depende de `infrastructure.security.AuthenticationFacade`, `ImageUploadSupport` ou `BinaryResponseBuilder.media`, ausentes no projeto atual.

A leitura de imagem já existente em `GET /inventory/items/{id}/image` agora acontece dentro da transação do service e retorna ItemImageDTO. Nenhum endpoint foi acrescentado. Foram mantidas as URIs, permissões, paginação padrão de dez registros, filtro de nome com trim, leitura sem imagem com 204, upload JPEG/PNG/WEBP até 2 MB e mensagens anteriores de upload.

Diferenças restantes em relação a customers:

- Categoria, preço, versão otimista e ItemUnit são dados e regras próprios de estoque. CategoryRepository substitui a chamada antiga a `CategoryService.findEntityById`, que não existe na interface atual. Não foi ampliado o módulo categories.
- A atualização altera nome, descrição, categoria e preço, preservando o status ativo. Alterar o status continua sendo uma operação separada, como já era em items.
- A exclusão conserva `flush` e tradução de `DataIntegrityViolationException` para DatabaseException, para manter o tratamento anterior de vínculos e restrições.
- Consultas textuais mantêm `LOWER` e concatenação com `||`, inclusive propagação de nulo; consultas anteriormente derivadas mantêm comparação de nulos com `IS NOT DISTINCT FROM`. O valor `-1` continua significando todas as categorias no catálogo.
- As consultas paginadas de ItemRepository expõem aliases SQL para os nomes Java das colunas de auditoria e imagem e fazem join com categoria, permitindo a ordenação pelos campos correspondentes. A geração de SQL do Spring Data 2.4.2 foi inspecionada para `name`, `createdAt` e `category.name`.
- As consultas de reserva conservam exclusão de vínculos `RESERVED`/`DELIVERED`, ordenação por id e bloqueio pessimista com `FOR UPDATE OF unit`. Nos dois métodos paginados com bloqueio, Pageable foi substituído por `limit` e `offset` explícitos no SQL. A ordenação automática de Pageable nesta versão do Spring Data seria acrescentada depois de FOR UPDATE, gerando SQL inválido. A ordem original por id único não depende dos critérios adicionais de Sort. `limit = null` representa consulta sem limite. Não há consumidores desses dois métodos no projeto atual para atualizar.
- A tabela `tb_rental_item_unit` permanece como dependência das regras de disponibilidade existentes. O nome da tabela, FK `item_unit_id` e armazenamento dos status como texto foram confirmados no mapeamento original de RentalItemUnit. As referências Java ao pacote de locações antigo foram eliminadas; não foi portado outro módulo.

Não foram mantidos dois DTOs de leitura, métodos alternativos de mapper, abstrações genéricas ou divergências justificadas somente por compatibilidade com código antigo.

## Métodos públicos finais e exatamente dois testes por método

Existe somente um ServiceImpl em items: ItemServiceImpl, com nove métodos públicos, excluindo o construtor.

| Método público | Teste de sucesso | Teste de falha |
| --- | --- | --- |
| `findAllPaged` | `findAllPagedShouldReturnPageOfItems` | `findAllPagedShouldThrowExceptionWhenRepositoryFails` |
| `findById` | `findByIdShouldReturnItem` | `findByIdShouldThrowExceptionWhenItemDoesNotExist` |
| `insert` | `insertShouldSaveItem` | `insertShouldThrowExceptionWhenNameAlreadyExists` |
| `update` | `updateShouldUpdateItem` | `updateShouldThrowExceptionWhenItemDoesNotExist` |
| `delete` | `deleteShouldDeleteItem` | `deleteShouldThrowExceptionWhenItemHasRelatedRecords` |
| `deleteAll` | `deleteAllShouldDeleteAllItems` | `deleteAllShouldThrowExceptionWhenIdListIsEmpty` |
| `changeActiveStatus` | `changeActiveStatusShouldChangeStatus` | `changeActiveStatusShouldThrowExceptionWhenDatabaseFails` |
| `getItemImageById` | `getItemImageByIdShouldReturnImage` | `getItemImageByIdShouldThrowExceptionWhenItemDoesNotExist` |
| `updateImage` | `updateImageShouldSaveImage` | `updateImageShouldThrowExceptionWhenFileIsEmpty` |

ItemServiceTests: **18 testes**, exatamente um sucesso e uma falha por método. Cada teste chama apenas seu cenário; não há testes parametrizados, loops de cenários ou testes diretos de métodos privados. Não existe ServiceImpl de arquivos em items.

## Validação e configuração local

A comparação final revisou todos os arquivos remanescentes e suas correspondências. Os 26 arquivos de customers continuaram idênticos aos hashes anteriores. As 16 consultas declaradas nos dois repositories possuem `nativeQuery = true`; métodos herdados de JpaRepository não foram redeclarados.

**Resultado final:** `clean package` concluiu com **BUILD SUCCESS**. Foram executados **379 testes**, com **zero falhas, zero erros e zero testes ignorados**, incluindo os **18 testes de ItemServiceTests** e o teste de contexto. O JAR executável foi gerado em `target/locadora_rdt_backend-0.0.1-SNAPSHOT.jar`.

Execução final com Java 11.0.32.1 e Maven 3.8.7:

```bash
EMAIL_USERNAME=rdt-test EMAIL_PASSWORD=rdt-test \
OAUTH_CLIENT_ID=rdt-test OAUTH_CLIENT_SECRET=rdt-test JWT_SECRET=rdt-test \
APP_MAIL_ENABLED=true \
mvn -Dspring.jpa.hibernate.ddl-auto=none \
    -Dspring.datasource.initialization-mode=never \
    -Dspring.datasource.hikari.read-only=true clean package
```

Os valores de email, OAuth e JWT são fictícios. `APP_MAIL_ENABLED=true` mantém disponível o bean de EmailService exigido pelos componentes existentes; desativá-lo causou falha no carregamento do contexto durante uma tentativa anterior. A inicialização dos testes não envia emails. Nenhum arquivo de configuração foi editado.

O teste do contexto usa o datasource de desenvolvimento já configurado, com DDL e inicialização SQL desativados e conexões somente leitura. Foi feita apenas consulta de metadados com PostgreSQL em modo `default_transaction_read_only=on`, que confirmou a ausência de `tb_item`, `tb_item_unit` e `tb_rental_item_unit` no banco local. Nenhuma tabela foi criada, nenhum dado foi alterado e não foi habilitada atualização automática do banco.

Limitações: as consultas de items não foram executadas contra essas tabelas, pois elas não existem no banco local. O teste de contexto valida a criação dos componentes, mas não substitui a execução de SQL nativo, paginação, bloqueios concorrentes e restrições em um schema de estoque existente. Não foi realizada navegação autenticada pelo frontend. A quantidade obrigatória de testes cobre os cenários escolhidos, sem exercitar todas as falhas possíveis.
