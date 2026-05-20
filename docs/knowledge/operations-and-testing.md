---
title: Operations и testing
tags:
  - operations
  - testing
  - docker
  - observability
---

# Operations и testing

Operations и testing отвечают за то, чтобы система не только работала на локальной машине, но и была понятной, проверяемой и диагностируемой.

## Docker Compose

Docker Compose удобен для локального окружения. Он позволяет поднять сервисы и инфраструктуру одной командой:

- application services;
- PostgreSQL;
- Redis;
- ClickHouse;
- MailHog или другой SMTP simulator.

Compose описывает networks, ports, volumes, environment variables и dependencies.

Важно понимать ограничение `depends_on`: он задает порядок запуска containers, но не всегда гарантирует readiness приложения. Для баз данных и аналитических систем полезны healthchecks.

## Configuration

Сервис не должен зашивать адреса баз, пароли, ports и intervals прямо в код. Эти значения должны приходить из config files или environment variables.

Типовые параметры:

- server port;
- database URL;
- Redis host/port;
- JWT secret;
- SMTP host;
- downstream service URLs;
- batch size;
- flush interval;
- retry settings.

Один и тот же image должен запускаться в разных окружениях за счет конфигурации, а не пересборки.

## Health checks

Health endpoint отвечает на вопрос: жив ли процесс. Readiness отвечает на вопрос: готов ли сервис принимать traffic.

Простая проверка `/health` может вернуть:

```json
{ "status": "ok" }
```

Для production полезно разделять:

- liveness: процесс не завис;
- readiness: зависимости доступны;
- startup: сервис завершил initialization.

## Observability

Observability строится на logs, metrics и traces.

Logs должны помогать понять:

- какой request пришел;
- какой user или trade id участвовал;
- какой downstream был вызван;
- какой status code вернулся;
- сколько заняла операция;
- где произошла ошибка.

Metrics показывают состояние системы в числах: request rate, latency, error rate, queue lag, batch size, memory usage.

Tracing помогает увидеть цепочку запроса через несколько сервисов.

## Testing strategy

Testing strategy зависит от риска.

Unit tests подходят для pure logic:

- matching algorithm;
- ticker normalization;
- money calculations;
- validation;
- JSON parsing.

Integration tests нужны для работы с реальными зависимостями:

- PostgreSQL transactions;
- Redis messages;
- ClickHouse inserts and queries;
- HTTP downstream calls.

Contract tests фиксируют API между сервисами:

- request/response schema;
- message payload;
- status codes;
- idempotency behavior.

End-to-end tests проверяют пользовательский сценарий целиком, но они дороже и медленнее. Их должно быть меньше, чем unit и integration tests.

## Reliability checks

Для distributed workflows важно тестировать не только happy path:

- downstream timeout;
- duplicate command;
- message redelivery;
- partial failure;
- invalid payload;
- closed order;
- insufficient funds;
- insufficient quantity.

## Практические правила

Все сервисы должны иметь health endpoint.

Конфигурация должна приходить извне.

Logs должны содержать correlation id или request id.

Критичная бизнес-логика должна иметь unit tests.

Интеграционные границы должны покрываться integration или contract tests.

Distributed operations нужно проверять на retries и idempotency.

## Связанные темы

- [[architecture-and-boundaries|Архитектура и границы сервисов]]
- [[backend-service-stack|Backend service stack]]
- [[trading-domain|Trading domain]]
