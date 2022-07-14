# Calculation Finite State Machine

![Build Workflow](../../workflows/Build%20project/badge.svg?branch=master)

Реализуем State Machine [calculation process](images/Calculation%20State%20Machine.png)
c
использованием [Spring State Machine](https://docs.spring.io/spring-statemachine/docs/current/reference/)
.

![Calculation FSM](images/Calculation%20State%20Machine.png)

### Требования

* Сохранение и восстановление FSM из состояния в таблице `CalculationStatus`;
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
           VALUES ('0df50a2c-45ef-45ff-b2c3-9f5c58e3e814', 'Calculation 1', 'CASH_FLOW', NOW(), NOW()),
                  ('ef8ac5ac-77dd-48f8-ad9c-5b4496f05dc3', 'Calculation 2', 'CASH_FLOW', NOW(), NOW());
```

Тестирование:
```shell
$ curl http://localhost:8081/api/v1/calculation/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
DATA_PREPARED # 8081

$ curl http://localhost:8081/api/v1/calculation/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
ETL_START # 8081

$ curl http://localhost:8082/api/v1/calculation/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
ETL_SEND_TO_DRP # 8082

$ curl http://localhost:8081/api/v1/calculation/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
ETL_ACCEPTED # 8081

$ curl http://localhost:8082/api/v1/calculation/next-state/0df50a2c-45ef-45ff-b2c3-9f5c58e3e814
ETL_COMPLETED # 8082
```