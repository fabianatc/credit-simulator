# 💳 Credit Simulator

Projeto desenvolvido para simulação de crédito individual ou em lote (batch) com diferentes taxas de juros, utilizando
arquitetura limpa e escalável.

---

## ⚙️ Instruções de Setup

### ✅ Pré-requisitos

- Java 21
- Docker (para executar PostgreSQL e RabbitMQ localmente)
- [Opcional] Postman ou cURL para testes manuais

### 🐘 Banco de Dados e RabbitMQ

Entre na pasta `docker` e execute o comando para executar os containers do PostgreSQL e RabbitMQ localmente, conforme
abaixo:

```bash
docker-compose up -d
``` 

### 🚀 Rodando o projeto

Após levantar o banco de dados e o RabbitMQ, inicie o backend com o comando Maven:

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em http://localhost:8081

### 📘 Documentação Swagger

Acesse a documentação interativa da API em:  
[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

### 📡 Endpoints - Exemplos de Requisições

🔹 Simulação Individual

POST `/simulations`

Body:

```json
{
  "creditAmount": 10000,
  "termInMonths": 24,
  "birthDate": "1990-05-20",
  "taxType": "AGE_BASED",
  "currency": "USD"
}
```

Resposta:

```json
{
  "totalAmount": 12345.67,
  "monthlyPayment": 514.40,
  "feePaid": 345.67,
  "currency": "USD"
}
```

🔸 Upload de Simulações em Lote

POST `/simulations/batch/upload`

Requisição `multipart/form-data`

Parâmetros

    file: arquivo .csv

    requesterName: nome do solicitante

    requesterEmail: email para envio do resultado

Exemplo de CSV

```
creditAmount,termInMonths,birthDate,taxType,fixedTax,currency
10000,24,1990-05-20,AGE_BASED,,BRL
15000,36,1985-07-11,FIXED,0.035,USD
```

Resposta

```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-1234567890ef",
  "message": "Your batch is being processed. Results will be sent to your email when ready.",
  "success": true,
  "csvExportUrl": "/simulations/batch/a1b2c3d4-e5f6-7890-abcd-1234567890ef/csv"
}
```

📤 Exportar Resultados em CSV

GET `/simulations/batch/{jobId}/csv`

    Retorna o resultado da simulação em formato .csv

    Retorna 400 caso o job ainda esteja em processamento

## 🧱 Estrutura do Projeto e Arquitetura

📦 Organização Hexagonal (Ports and Adapters)

```
└── com.creditsimulator
├── application        # Controllers, serviços de entrada, workers
├── domain             # Modelos de domínio, interfaces (UseCases e Ports)
├── infra              # JPA, RabbitMQ, Mail, adapters externos
└── shared             # Mappers, Enums e exceções comuns
```

## 🔄 Decisões técnicas

- Arquitetura hexagonal, focada em desacoplamento entre domínio e infraestrutura

- Uso de mensageria com RabbitMQ para processar grandes volumes de dados

- Testes automatizados com JUnit 5, Mockito, Testcontainers

- Versionamento de banco com Liquibase

- Integração com Spring Boot 3.4.5 e uso de record em requests/responses para imutabilidade

- Envio de resultados em lote por e-mail em formato CSV

## 🧪 Testes

- Testes unitários para principais serviços e workers

- Testes de integração com containers reais (PostgreSQL)

- Cobertura de exceções, casos de erro, CSV inválido e envio de email

## Autora

- [Fabiana Casagrande Costa](https://www.linkedin.com/in/fabiana-casagrande-costa/)
- [GitHub do projeto](https://github.com/fabianatc/credit-simulator)
- [E-mail](mailto:fabianatc@gmail.com)