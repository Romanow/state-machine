# Calculation State Machine

Implements state machine for [calculation process](images/Calculation%20State%20Machine.png)
using [Spring State Machine](https://docs.spring.io/spring-statemachine/docs/current/reference/).

Requirements:

* persists and load from state;
* make `@Transactional` actions: add new record to `CalculationStatus`.