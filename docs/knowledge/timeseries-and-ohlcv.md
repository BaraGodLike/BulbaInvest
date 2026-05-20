---
title: Time series и OHLCV
tags:
  - timeseries
  - clickhouse
  - ohlcv
  - analytics
---

# Time series и OHLCV

Time series - данные, привязанные ко времени. Котировки акций являются классическим примером: каждый tick имеет ticker, price и timestamp.

## Почему нужна отдельная модель

Текущая цена и история цен - разные задачи. Текущую цену удобно хранить в Redis, а историю - в базе, оптимизированной под аналитические запросы.

История котировок быстро растет. Если писать каждый tick в обычную transactional database, запросы графиков могут начать мешать доменным операциям.

## ClickHouse

ClickHouse - column-oriented database для аналитики. Она хорошо подходит для queries по большим наборам данных: фильтрация по ticker, группировка по времени, расчет агрегатов.

Типовая таблица quotes:

- `ticker`;
- `price`;
- `available_quantity`;
- `volatility`;
- `ts`.

Для ClickHouse важны partition и order key. Для котировок часто используют partition by month и order by `(ticker, ts)`.

## Batch inserts

ClickHouse не любит слишком частые одиночные inserts. Поэтому events обычно накапливают в buffer и пишут batch.

Flush может быть:

- time-based: каждые N секунд;
- size-based: когда buffer достиг N строк;
- shutdown flush: перед остановкой сервиса.

Если insert упал, данные можно вернуть в buffer и повторить позже. Но нужно следить, чтобы buffer не рос бесконечно.

## OHLCV

OHLCV candle - агрегат цены за временной bucket:

- `Open` - первая цена;
- `High` - максимальная цена;
- `Low` - минимальная цена;
- `Close` - последняя цена;
- `Volume` - объем.

В учебных системах volume часто означает количество ticks в bucket. В реальной бирже volume обычно означает количество проторгованных единиц.

## Granularity

Granularity определяет размер candle:

- 1 second;
- 1 minute;
- 5 minutes;
- 1 hour;
- 1 day.

Для разных экранов нужны разные granularity. График за час может использовать minute candles, а график за месяц - daily candles.

## Latest price

Latest price можно получать из time-series database через сортировку по timestamp, но для real-time UI это не всегда оптимально. Часто latest quote хранят отдельно в Redis, а ClickHouse используют для истории.

## TTL

История тиков может быть дорогой. TTL позволяет автоматически удалять старые данные, например старше 12 месяцев.

Иногда используют downsampling: сырые тики хранят недолго, а агрегированные candles - дольше.

## Практические правила

Не смешивать transactional data и analytics workload без необходимости.

Писать в ClickHouse batch inserts.

Четко определить, что значит `volume`.

Хранить timestamps в UTC.

Для UI использовать подходящую granularity.

## Связанные темы

- [[data-storage-and-modeling|Хранение данных и моделирование]]
- [[redis-events-and-messaging|Redis, events и messaging]]
- [[market-simulation|Market simulation]]
