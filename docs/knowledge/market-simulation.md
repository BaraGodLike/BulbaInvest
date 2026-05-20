---
title: Market simulation
tags:
  - market
  - simulation
  - go
  - cgo
---

# Market simulation

Market simulation - искусственная модель рынка, которая генерирует котировки и доступное количество активов без подключения к реальной бирже. Она нужна для учебных систем, демо-приложений, нагрузочных сценариев и тестирования trading flow.

## Что симулируется

Минимальная модель акции:

- ticker;
- current price;
- available quantity;
- volatility;
- updated time.

На каждом tick цена немного меняется. Изменение может быть случайным, но должно учитывать volatility и нижнюю границу цены.

## Tick loop

Tick loop - background process, который через заданный interval обновляет состояние рынка.

Типовой цикл:

1. дождаться timer tick;
2. обновить цены в simulation engine;
3. получить snapshot;
4. сохранить current quotes;
5. опубликовать quote update event.

Tick loop должен корректно останавливаться при shutdown, чтобы не оставлять незавершенные операции.

## Inventory

Inventory показывает, сколько бумаг доступно у компании или market maker.

При покупке у компании inventory уменьшается. При продаже компании inventory увеличивается.

Inventory не должен смешиваться с portfolio пользователя. Это другой bounded context.

## Simulation engine

Simulation engine можно вынести в отдельный слой или библиотеку. Он должен заниматься только математикой и состоянием рынка:

- update price;
- apply buy;
- apply sell;
- return snapshot.

Он не должен знать про HTTP, Redis, users, wallets или orders. Такое разделение делает engine переиспользуемым и легче тестируемым.

## Go и C boundary

Go удобен для service orchestration: HTTP API, Redis IO, background loops, graceful shutdown. C может быть полезен для низкоуровневого simulation engine.

При использовании cgo важно явно определить memory ownership. Если C возвращает snapshot в heap memory, Go wrapper должен освободить его через C free-функцию после conversion.

## Publishing quotes

Симулятор может публиковать quotes двумя способами:

- сохранить последнюю quote в Redis key/hash для быстрых reads;
- отправить event в Pub/Sub или stream для live consumers.

Так разные consumers получают данные под свой сценарий: gateway стримит UI, graph service пишет историю, domain service читает последнюю цену.

## HTTP debug API

Даже если основной обмен идет через Redis, полезно иметь небольшой HTTP API:

- health check;
- list stocks;
- stock by ticker;
- current snapshot.

Такой API помогает отлаживать симулятор вручную и писать integration tests.

## Практические правила

Simulation не должна быть source of truth для денег пользователя.

Inventory должен иметь одного владельца.

Quote update можно распространять через events.

Commands на изменение inventory должны быть idempotent.

C boundary должен иметь ясные правила освобождения памяти.

## Связанные темы

- [[backend-service-stack|Backend service stack]]
- [[redis-events-and-messaging|Redis, events и messaging]]
- [[timeseries-and-ohlcv|Time series и OHLCV]]
