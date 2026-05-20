---
title: Архитектура и границы сервисов
tags:
  - architecture
  - microservices
  - bounded-context
  - api-gateway
---

# Архитектура и границы сервисов

Архитектура backend-системы описывает не только список сервисов, но и границы ответственности между ними. Хорошая архитектура отвечает на вопросы: кто владеет данными, кто принимает решения, как сервисы общаются, что происходит при ошибке и где проходит внешний API.

## Microservices

Microservice - это самостоятельный сервис с понятной ответственностью, собственным lifecycle, конфигурацией, контрактами и часто собственной базой данных. Его размер менее важен, чем ясность границ.

Плохой microservice - это просто часть монолита, вынесенная в отдельный процесс, но продолжающая делить таблицы, бизнес-логику и внутренние модели с другими частями системы. Хороший microservice владеет своим состоянием и предоставляет наружу явный contract.

В trading-системах естественно выделяются разные зоны ответственности:

- пользовательская доменная модель: users, wallets, portfolios, orders, trades;
- рыночная модель: quotes, available inventory, volatility, ticks;
- аналитическая модель: historical quotes, candles, aggregates;
- edge layer: внешний API, WebSocket, auth boundary, routing.

Такое разделение помогает не смешивать финансовые операции, симуляцию рынка и отображение графиков.

## Bounded context

`Bounded context` - это граница, внутри которой термины имеют устойчивый смысл. Например, `price` в market context может быть текущей simulated quote, а `price` в trade history - уже зафиксированной ценой исполненной сделки. Это разные факты, хотя название одинаковое.

Главное правило bounded context: у каждого факта должен быть один владелец. Если два сервиса считают себя владельцами одного значения, возникает shared ownership. Это почти всегда приводит к расхождениям.

Пример: количество акций, доступных у компании, не должно одновременно изменяться и сервисом рынка, и сервисом домена. Один сервис должен владеть inventory, другой должен обращаться к нему через contract.

## API Gateway

`API Gateway` - единая входная точка для клиентов. Он скрывает внутреннюю топологию сервисов, маршрутизирует запросы, может проверять auth, добавлять request id, логировать edge-запросы, применять rate limiting и держать WebSocket endpoint.

Gateway полезен, когда клиенту не нужно знать адрес каждого сервиса. Клиент обращается к одному public API, а gateway проксирует запросы во внутреннюю сеть.

Но gateway не должен становиться вторым domain service. Его задача - edge-логика, а не правила сделок. Если gateway начинает решать, можно ли пользователю купить акцию, это признак утечки business logic.

## Service contracts

Contract - это обещание сервиса. Он описывает endpoints, payloads, status codes, message schemas, retry behavior и idempotency rules.

Контракты бывают:

- HTTP contracts: REST endpoints, OpenAPI, status codes;
- message contracts: JSON payloads в broker или stream;
- WebSocket contracts: формат сообщений и правила подписки;
- operational contracts: health checks, readiness, timeouts.

В distributed system нельзя полагаться на “мы знаем, как оно внутри устроено”. Внутреннее устройство сервиса может меняться, но контракт должен оставаться стабильным или версионироваться.

## Event-driven architecture

Event-driven architecture строится вокруг событий. Producer публикует факт, consumer реагирует. Например, market service публикует `quotes updated`, gateway отправляет обновление клиентам, graph service сохраняет историю.

Важно различать events и commands.

`Event` сообщает, что что-то уже произошло: цена обновилась, order matched, user created. Event может читать много consumers.

`Command` просит выполнить действие: уменьшить inventory, создать order, списать деньги. Command требует результата, обработки ошибок и часто idempotency.

Для live data подходят Pub/Sub-механизмы. Для критичных операций лучше использовать durable streams, message queues или HTTP-команды с idempotency key.

## Практические правила

Сначала определяют owners данных, потом выбирают протоколы взаимодействия.

Сервис не должен напрямую писать в базу другого сервиса.

Gateway не должен содержать core business logic.

Contracts нужно документировать отдельно от внутренней реализации.

Events подходят для распространения фактов, commands - для действий с результатом.

Если операция затрагивает несколько сервисов, нужно заранее продумать idempotency, retries и compensation.

## Связанные темы

- [[trading-domain|Trading domain]]
- [[redis-events-and-messaging|Redis, events и messaging]]
- [[operations-and-testing|Operations и testing]]
