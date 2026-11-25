# New Big | Gestão de sorveteria e ponto de venda

O New Big é um sistema web desenvolvido para a **New Big Sorvetes e Açaí**, em Foz do Iguaçu (PR). Ele reúne vendas de balcão, estoque por lote e controle de caixa. O objetivo é permitir que a equipe acompanhe, a partir de uma venda, quais produtos saíram, de quais lotes vieram e quanto dinheiro deve haver no caixa ao final do expediente.

O repositório contém a interface em **Angular e TypeScript**, a API REST em **Java 17 com Spring Boot** e a configuração de persistência em **PostgreSQL**. O projeto está em desenvolvimento.

## Como funciona uma venda

1. **Abertura do caixa.** O funcionário informa o valor inicial disponível para troco.
2. **Registro dos itens.** Na venda aberta, o produto é identificado pelo código de barras. A API verifica se há estoque suficiente e dá baixa nos lotes com vencimento mais próximo.
3. **Atualização da venda.** O sistema registra os itens e as movimentações de estoque e recalcula subtotal, desconto e total.
4. **Pagamento e conferência.** Ao concluir a venda, a API registra a forma de pagamento e atualiza o total vendido. No fechamento do caixa, compara o dinheiro contado com o valor esperado na gaveta.

Além do ponto de venda, o sistema mantém cadastros de **produtos, lotes, clientes e funcionários**. A API autentica funcionários por e-mail e senha, emite um token JWT e exige perfil de administrador nas rotas de funcionários.

## Duas regras de negócio no código

**Qual lote deve sair primeiro?** Imagine uma venda de cinco picolés. O lote que vence primeiro tem três unidades e o seguinte tem dez. Pela regra FEFO (*First Expired, First Out*), o sistema retira três do primeiro lote e duas do segundo. A consulta seleciona lotes com saldo e vencimento posterior ao dia da venda e usa bloqueio pessimista de escrita durante a baixa. Se o saldo disponível for insuficiente, a operação é recusada antes de descontar as unidades. [Consulta dos lotes](backend/src/main/java/com/newbig/web/repository/BatchRepository.java) · [Regra de consumo](backend/src/main/java/com/newbig/web/service/BatchService.java)

**Quanto deve haver na gaveta?** Se o caixa abre com R$ 100 e registra R$ 30 em dinheiro e R$ 40 em Pix, o total vendido é R$ 70 e o dinheiro esperado é R$ 130. O sistema aceita o fechamento quando o valor contado coincide com o esperado. [Implementação do caixa](backend/src/main/java/com/newbig/web/service/CashDailyService.java)

## Tecnologias

| Área | Tecnologias | Uso no projeto |
| --- | --- | --- |
| Interface | Angular 19, TypeScript, RxJS e SCSS | Telas de operação, formulários e comunicação com a API |
| API REST | Java 17, Spring Boot, Spring MVC e Jakarta Validation | Endpoints, validação de entradas e execução das regras de negócio |
| Persistência | PostgreSQL e Spring Data JPA | Armazenamento de vendas, itens, lotes e movimentações |
| Autenticação | Spring Security e JWT | Login de funcionários e controle de acesso às rotas |
| Testes e build | Maven Wrapper, JUnit, Mockito e JaCoCo | Execução da suíte do back-end e geração do relatório de cobertura |

## Por onde começar a leitura

O [componente do ponto de venda](frontend/src/app/features/vendas/pdv.component.ts) mostra as ações do operador. No back-end, o [SaleService](backend/src/main/java/com/newbig/web/service/SaleService.java) coordena a inclusão de itens, a baixa por lote e a atualização dos valores da venda. Os [testes de venda](backend/src/test/java/com/newbig/web/service/SaleServiceTest.java) mostram o comportamento esperado, inclusive a rejeição de novos itens em uma venda que já saiu do estado aberto.

**Desenvolvimento do full-stack:** Felipe Bocchi Bazzanela. [GitHub](https://github.com/FelipeBocchi) · [Portfólio](https://felipebocchi.com/)

## Execução local

Use JDK 17, PostgreSQL e Node.js com npm compatível com Angular 19. Os comandos abaixo partem da raiz do repositório e usam Bash.

**1. API.** Crie o banco `newbigweb` no PostgreSQL local, na porta 5432. No terminal, configure o usuário e a senha do banco e uma chave JWT de pelo menos 32 bytes. O exemplo usa OpenSSL para gerar a chave.

```bash
cd backend
export SPRING_DATASOURCE_USERNAME='seu_usuario'
export SPRING_DATASOURCE_PASSWORD='sua_senha'
export JWT_SECRET="$(openssl rand -hex 32)"
./mvnw spring-boot:run
```

A API escuta em `http://localhost:8081`. O primeiro acesso à interface exige um funcionário ativo previamente cadastrado na base local.

**2. Interface.** Em outro terminal, entre em `frontend`. Para usar o login e o ponto de venda localmente, direcione as chamadas de `/api` ao Spring Boot com um proxy de desenvolvimento.

```bash
cd frontend
npm ci
printf '%s\n' '{"/api":{"target":"http://localhost:8081","secure":false}}' > proxy.local.json
npm start -- --proxy-config proxy.local.json
```

Abra [localhost:4200](http://localhost:4200) no navegador.

**3. Testes do back-end.** Na pasta `backend`, execute a suíte completa ou apenas os testes da regra de lotes.

```bash
./mvnw clean verify
./mvnw -Dtest=BatchServiceTest test
```

Após uma execução bem-sucedida de `verify`, abra `backend/target/site/jacoco/index.html` para consultar o relatório de cobertura. Os testes estão organizados por [serviços, controllers e repositórios](backend/src/test/java/com/newbig/web).