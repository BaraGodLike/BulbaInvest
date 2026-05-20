---
title: Backend service stack
tags:
  - backend
  - kotlin
  - ktor
  - go
  - dependency-injection
---

# Backend service stack

Backend service stack - это набор технологий и практик, из которых собирается сервис: HTTP framework, dependency injection, serialization, database access, external clients, concurrency model, logging и testing.

## Ktor

Ktor - легковесный Kotlin framework для HTTP-сервисов. Он хорошо подходит для microservices, потому что дает простой контроль над routing, plugins, serialization, authentication и WebSockets.

Типовой Ktor service состоит из:

- application module;
- plugins: serialization, status pages, authentication, CORS, WebSockets;
- routes: HTTP endpoints;
- service layer: use cases и business logic;
- infrastructure layer: database clients, Redis clients, HTTP clients.

Routes не должны содержать много бизнес-логики. Их задача - прочитать request, вызвать use case и вернуть response. Чем тоньше route layer, тем проще тестировать систему.

## Dependency Injection

Dependency Injection помогает отделить создание объектов от их использования. Service layer не должен сам создавать HTTP client, Redis pool или database connection. Он должен получать зависимости через constructor.

DI дает несколько преимуществ:

- проще тестировать use cases через fake dependencies;
- проще менять implementation без переписывания business logic;
- конфигурация собирается в одном месте;
- меньше hidden coupling.

Koin - популярный DI framework для Kotlin. Его mental model прост: в модуле объявляются singleton или factory dependencies, а приложение получает уже собранный dependency graph.

## Kotlin Coroutines и Flow

Coroutines позволяют писать asynchronous code в последовательном стиле. Это важно для HTTP, Redis, WebSocket и background tasks.

`Flow` подходит для потоков данных во времени: live quotes, events, updates, subscriptions. Например, WebSocket endpoint может получать `Flow<List<Quote>>`, фильтровать его по tickers и отправлять клиенту только нужные updates.

Важное свойство Flow - cancellation. Если клиент закрывает WebSocket, coroutine должна остановить collection и освободить ресурсы.

## StatusPages и ошибки

HTTP API должен возвращать предсказуемые ошибки. В Ktor `StatusPages` позволяет централизованно превращать domain exceptions в HTTP responses.

Типовое соответствие:

- validation error -> `400 Bad Request`;
- entity not found -> `404 Not Found`;
- business conflict -> `409 Conflict`;
- unexpected failure -> `500 Internal Server Error`.

Минимальный error response может выглядеть так:

```json
{ "error": "Insufficient funds" }
```

Более развитый формат включает `code`, `message`, `requestId`, `details`.

## HTTP clients: timeouts и retries

Любой HTTP client в service-to-service communication должен иметь timeouts:

- connect timeout;
- request timeout;
- socket timeout.

Без timeouts зависший downstream может удерживать ресурсы и постепенно положить upstream-сервис.

Retries полезны только для безопасных операций. Если повторить non-idempotent command, можно дважды применить изменение. Поэтому команды, меняющие состояние, должны иметь `idempotencyKey`.

## Go service structure

Go хорошо подходит для сервисов с background loops, network IO и простым concurrency model. Типовая структура:

- `cmd/<service>` - entrypoint;
- `internal/<domain>` - core logic;
- `internal/<transport>` - HTTP, Redis, messaging;
- `internal/<config>` - configuration;
- interfaces вокруг внешних зависимостей.

В Go удобно отделять core logic от infrastructure через interfaces. Например, service может зависеть от `Publisher`, `Driver`, `Repository`, а не от конкретного Redis или C binding.

## cgo и C boundary

cgo позволяет Go-коду вызывать C-библиотеки. Это полезно, когда simulation engine или низкоуровневая библиотека написаны на C.

Главный риск cgo - memory ownership. Если C выделяет память, должен быть понятный способ освободить ее. Go wrapper обязан конвертировать C structs в Go models и корректно вызвать free-функцию.

Хорошая граница с C:

- C layer не знает про HTTP, Redis и пользователей;
- Go layer отвечает за orchestration и integration;
- memory ownership явно задокументирован;
- C tests и Go tests запускаются отдельно.

## Связанные темы

- [[architecture-and-boundaries|Архитектура и границы сервисов]]
- [[market-simulation|Market simulation]]
- [[operations-and-testing|Operations и testing]]
