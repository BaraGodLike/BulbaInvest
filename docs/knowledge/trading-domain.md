---
title: Trading domain
tags:
  - trading
  - domain
  - order-book
  - consistency
---

# Trading domain

Trading domain описывает пользователей, деньги, портфели, заявки, сделки и рыночные операции. Даже в учебной системе эти понятия нужно моделировать аккуратно, потому что ошибки быстро приводят к отрицательным балансам, двойной продаже или расхождению истории.

## Основные сущности

`User` - участник системы.

`Wallet` - денежный счет пользователя. Обычно содержит currency, amount и reserved amount.

`Portfolio position` - количество бумаг по конкретному ticker.

`Order` - заявка на покупку или продажу.

`Trade` - исполненная сделка.

`Quote` - текущая рыночная цена или snapshot рынка.

`Inventory` - количество бумаг, доступных у компании или market maker.

## Company trades и user trades

Сделки можно разделить на два типа.

`Company trade` - пользователь покупает у компании или продает компании. Здесь участвует внешний market context: нужно проверить или изменить inventory.

`User trade` - пользователь покупает у другого пользователя. В упрощенной системе это можно выполнить целиком внутри domain database.

Разница важна для consistency. User trade может быть атомарной SQL transaction. Company trade часто становится distributed workflow, потому что затрагивает несколько сервисов.

## Order book

Order book - список заявок. В полной биржевой системе есть buy side и sell side. В упрощенной модели может быть только sell side: пользователи выставляют sell orders, а покупатели выбирают конкретный order или покупают из стакана по максимальной цене.

Order status обычно включает:

- `OPEN`;
- `PARTIALLY_FILLED`;
- `MATCHED` или `FILLED`;
- `CANCELLED`;
- `EXPIRED`.

## Matching

Matching - процесс подбора встречных заявок. Простая стратегия - price-time priority:

- сначала лучшая цена;
- при одинаковой цене - более ранняя заявка.

Для покупки из sell book выбираются самые дешевые open sell orders, пока не будет набрано нужное quantity или пока цена не превысит max price.

## Reservation

Reservation защищает от двойного расходования.

Если пользователь выставляет sell order, система резервирует quantity в portfolio. Эти бумаги нельзя продать повторно до отмены или исполнения order.

Если пользователь создает buy order, можно резервировать деньги в wallet. Это предотвращает ситуацию, где пользователь создал несколько заявок на одну и ту же сумму.

## Trade consistency

Сделка должна сохранять инварианты:

- balance не уходит ниже нуля;
- reserved amount не превышает amount;
- reserved quantity не превышает quantity;
- trade history соответствует изменениям wallet и portfolio;
- order status соответствует фактическому исполнению.

Для локальных операций помогает SQL transaction. Для distributed операций нужны idempotency, retries, compensation и reconciliation.

## Average buy price

`average_buy_price` показывает среднюю цену покупки позиции. При докупке бумаг пересчитывается weighted average:

```text
newAverage = (oldQty * oldAvg + buyQty * buyPrice) / (oldQty + buyQty)
```

При продаже средняя цена обычно не меняется, уменьшается только quantity.

## Практические правила

Деньги считать только decimal types.

Все изменения wallet, portfolio, order и trade history выполнять атомарно, если они находятся в одной базе.

Не позволять пользователю купить собственный order.

Не исполнять closed order.

При distributed trade использовать idempotency key.

Отдельно хранить текущее состояние и историю событий.

## Связанные темы

- [[data-storage-and-modeling|Хранение данных и моделирование]]
- [[redis-events-and-messaging|Redis, events и messaging]]
- [[market-simulation|Market simulation]]
