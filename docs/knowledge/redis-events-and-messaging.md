---
title: Redis, events и messaging
tags:
  - redis
  - messaging
  - pubsub
  - streams
  - events
---

# Redis, events и messaging

Redis может быть cache, key-value storage, Pub/Sub broker и lightweight stream platform. В backend-системах его часто используют для live updates, временных значений, locks, rate limits и межсервисных сообщений.

## Redis как key-value storage

Redis хорошо подходит для быстрых данных, которые часто читаются и не требуют сложных relational queries.

Примеры:

- последняя цена ticker;
- auth code с TTL;
- idempotency key;
- session metadata;
- feature flags;
- counters.

TTL особенно полезен для временных значений: кодов подтверждения, short-lived tokens, temporary locks.

## Pub/Sub

Redis Pub/Sub - механизм публикации сообщений в канал. Producer делает publish, subscribers получают message.

Подходит для:

- live quotes;
- UI updates;
- notification fan-out;
- internal real-time signals.

Ограничение Pub/Sub: он не durable. Если subscriber был выключен, он пропустит сообщение. Поэтому Pub/Sub нельзя использовать как единственный транспорт для критичной финансовой операции.

## Redis Streams

Redis Streams - append-only log сообщений. Streams поддерживают consumer groups, acknowledgements и чтение истории.

Streams лучше подходят для workflows, где важно не потерять command или result:

- domain command;
- market command;
- payment event;
- reconciliation task;
- asynchronous processing.

Consumer group позволяет нескольким consumers разделять нагрузку. Pending messages можно дочитывать после сбоя.

## Events и commands

Важно различать semantic type сообщения.

`Event` - факт, который уже произошел: quote updated, order matched, user created.

`Command` - просьба выполнить действие: reserve quantity, decrement inventory, create payout.

Для event часто достаточно fire-and-forget. Для command почти всегда нужен result, timeout, retry и idempotency.

## Message contract

Сообщение в Redis - это тоже contract. Нужно документировать:

- channel или stream name;
- schema payload;
- обязательные поля;
- timestamps и units;
- idempotency key;
- error shape;
- retry rules.

JSON удобен для старта, но schema должна быть стабильной. При изменениях лучше добавлять новые поля, а не ломать старые.

## Idempotency

Idempotency означает, что повтор одной и той же операции не меняет результат повторно.

Это критично для commands. Если сервис отправил command, получил timeout, но downstream успел применить изменение, повтор command не должен применить его второй раз.

Типовая схема:

- request содержит `idempotencyKey`;
- receiver проверяет, видел ли этот key;
- если key новый, операция применяется и результат сохраняется;
- если key старый, возвращается сохраненный результат.

## Практические правила

Pub/Sub - для live updates, где допустима потеря отдельного сообщения.

Streams - для задач, где сообщение нужно обработать надежнее.

Критичные commands должны иметь idempotency key.

Schema сообщений должна быть документирована.

Consumers должны уметь переживать повторную доставку.

## Связанные темы

- [[architecture-and-boundaries|Архитектура и границы сервисов]]
- [[trading-domain|Trading domain]]
- [[operations-and-testing|Operations и testing]]
