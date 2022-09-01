# Calculation Finite State Machine

![Build Workflow](../../workflows/Build%20project/badge.svg?branch=master)

Реализация State Machine на
базе [Spring State Machine](https://docs.spring.io/spring-statemachine/docs/current/reference/).

* [Cash Flow](images/Cash%20Flow%20State%20Machine.png)
* [VSSDV](images/VSSDV%20State%20Machine.png)

### Требования

* Сохранение и восстановление FSM из состояния в таблице `CalculationStatus`.
* Для ВССДВ используется State Machine с параллельными состояниями, в таблицу CalculationStatus записывается кортеж
  состояний.
* Использование транзакционных действий при записи в `CalculationStatus`.

### Тестирование

Подготовка:

```shell
$ docker compose up postrges -d

$ ./gradlew clean build
$ ./gradlew bootRun --args='--server.port=8081'
$ ./gradlew bootRun --args='--server.port=8082'

$ psql -h localhost -p 5432 -U program services 

services=> INSERT INTO calculation (uid, name, type, created_date, modified_date)
           VALUES ('0df50a2c-45ef-45ff-b2c3-9f5c58e3e814', 'Calculation CF 1', 'CASH_FLOW', NOW(), NOW()),
                  ('ef8ac5ac-77dd-48f8-ad9c-5b4496f05dc3', 'Calculation CF 2', 'CASH_FLOW', NOW(), NOW()),
                  ('ba012596-4be8-4c96-9721-07b7f9902a6a', 'Calculation VSSDV 1', 'VSSDV', NOW(), NOW()),;
```

Тестирование:

```shell
$ curl http://localhost:8081/api/v1/calculation/cash-flow/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
[DATA_PREPARED] # 8081

$ curl http://localhost:8081/api/v1/calculation/cash-flow/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
[ETL_START] # 8081

$ curl http://localhost:8082/api/v1/calculation/cash-flow/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
[ETL_SENT_TO_DRP] # 8082

$ curl http://localhost:8081/api/v1/calculation/cash-flow/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
[ETL_ACCEPTED] # 8081

$ curl http://localhost:8082/api/v1/calculation/cash-flow/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
[ETL_COMPLETED] # 8082
```