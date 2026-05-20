---
title: Хранение данных и моделирование
tags:
  - data
  - postgres
  - flyway
  - modeling
  - money
---

# Хранение данных и моделирование

Data storage выбирают под workload. В backend-системе с trading domain обычно нужны разные типы хранения: relational database для транзакций, Redis для быстрых значений и сообщений, column-oriented database для аналитики.

## PostgreSQL как source of truth

PostgreSQL хорошо подходит для доменной модели, где важны транзакции, constraints и consistency. Пользователи, кошельки, портфели, ордера и сделки требуют атомарных изменений.

Пример: при покупке order нужно:

- списать деньги покупателя;
- начислить деньги продавцу;
- изменить портфель продавца;
- изменить портфель покупателя;
- обновить status order;
- записать trade history.

Все эти шаги должны быть выполнены в одной transaction. Если один шаг падает, остальные должны откатиться.

## Flyway и миграции

Миграции делают схему базы частью кода. Flyway применяет SQL-файлы в заданном порядке и хранит историю примененных миграций.

Преимущества миграций:

- схема воспроизводится в новом окружении;
- изменения БД проходят code review;
- можно понять, какая версия схемы нужна приложению;
- проще запускать локальные и тестовые окружения.

Первая миграция обычно создает базовые таблицы. Последующие миграции добавляют columns, indexes, constraints или новые таблицы.

## Exposed и SQL abstraction

Exposed - Kotlin SQL library. Она позволяет описывать таблицы и писать типизированные queries. Важно помнить, что ORM/DSL не отменяет понимания SQL.

Даже при использовании Exposed нужно понимать:

- где открывается transaction;
- какие queries выполняются;
- какие indexes нужны;
- как работает isolation;
- где может возникнуть race condition.

## Wallet model

Wallet хранит деньги пользователя. Минимальная модель:

- `id`;
- `user_id`;
- `currency`;
- `amount`;
- `reserved_amount`;
- `is_default`;
- timestamps.

`reserved_amount` нужен для операций, где деньги блокируются заранее. Например, если buy order создается до исполнения, сумму можно зарезервировать, чтобы пользователь не потратил ее дважды.

## Portfolio model

Portfolio position хранит количество бумаг по ticker:

- `user_id`;
- `ticker`;
- `quantity`;
- `reserved_quantity`;
- `average_buy_price`.

`reserved_quantity` нужна для sell orders. Если пользователь выставил акции на продажу, он не должен продать эти же акции повторно до отмены или исполнения order.

## Trade history

Trade history - журнал исполненных сделок. Он не заменяет текущее состояние wallet или portfolio, но нужен для истории пользователя, аудита, аналитики и сверок.

Trade обычно содержит:

- buyer;
- seller;
- ticker;
- quantity;
- execution price;
- total amount;
- trade type;
- created time.

## Деньги и Decimal

Деньги нельзя считать через floating point. Для финансовой логики нужны decimal types: `BigDecimal` в приложении и `NUMERIC` или `DECIMAL` в базе.

Floating point допустим для симуляции графиков или неофициальных вычислений, но не для source of truth по балансу.

Практическое правило: все значения, которые влияют на wallet, portfolio и settlement, должны проходить через decimal.

## Indexes и constraints

Constraints защищают данные:

- unique email;
- unique `(user_id, ticker)` для portfolio position;
- positive quantity;
- valid status;
- foreign keys.

Indexes ускоряют частые queries:

- orders by ticker and status;
- trades by user;
- wallets by user;
- quotes by ticker and timestamp.

## Связанные темы

- [[trading-domain|Trading domain]]
- [[timeseries-and-ohlcv|Time series и OHLCV]]
- [[operations-and-testing|Operations и testing]]
