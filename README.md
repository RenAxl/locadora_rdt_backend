# Locadora RDT Backend

## Execução local

O perfil padrão é `dev`. Ele carrega o arquivo `application-local.properties`
na raiz do backend, se o arquivo existir. Esse arquivo é ignorado pelo Git.

Em uma nova instalação, copie `application-local.properties.example` para
`application-local.properties` e preencha as configurações:

- `OAUTH_CLIENT_ID` e `OAUTH_CLIENT_SECRET`: o mesmo cliente OAuth usado pelo frontend.
- `JWT_SECRET`: uma chave aleatória para assinar os tokens.
- `EMAIL_USERNAME` e `EMAIL_PASSWORD`: credenciais SMTP para enviar e-mails.

As variáveis de ambiente têm prioridade sobre os valores do arquivo local.
Credenciais de e-mail em branco permitem iniciar a aplicação, mas precisam ser
preenchidas para o envio de e-mails funcionar.

No perfil `dev`, os links enviados por e-mail usam `http://localhost:4200`.
Defina `FRONTEND_BASE_URL` se o frontend estiver em outro endereço.

Com o PostgreSQL disponível, execute na raiz do backend:

```bash
mvn spring-boot:run
```

No IntelliJ, execute `LocadoraRdtBackendApplication` com o diretório de trabalho
definido como a raiz de `locadora_rdt_backend`.
