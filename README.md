# Marketplace

Учебный проект — backend маркетплейса (системы заказов)

## О проекте

Проект развивается **по фазам**: сначала один сервис, проработанный вглубь (монолит-каталог), затем постепенный распил на микросервисную архитектуру с API Gateway, service discovery и асинхронным обменом через Kafka.

## Технологии

| Категория | Стек |
|---|---|
| Язык / платформа | Java 21 |
| Фреймворк | Spring Boot 4 (Spring MVC) |
| Доступ к данным | Spring Data JPA, Hibernate |
| База данных | PostgreSQL |
| Миграции схемы | Liquibase |
| Сборка | Maven |
| Прочее | Lombok, Bean Validation |

В следующих фазах добавятся: Spring Security + JWT, Redis, Docker, Spring Cloud Gateway, Eureka, Apache Kafka, Resilience4j, Testcontainers.

## Архитектура

**Целевая :** набор микросервисов за единым API Gateway.

- `api-gateway` — единая точка входа, маршрутизация, проверка JWT
- `discovery-server` (Eureka) — реестр сервисов
- `auth-service` — регистрация, логин, выдача JWT
- `product-service` — каталог товаров, кеш в Redis
- `order-service` — заказы, транзакции, события в Kafka
- `notification-service` — асинхронная обработка событий из Kafka
  **Текущая (Фаза 0):** один сервис — каталог товаров. Слоистая архитектура:

```
controller  -> принимает/отдаёт HTTP, без бизнес-логики
service     -> бизнес-логика и транзакции
repository  -> доступ к БД (Spring Data JPA)
entity      -> @Entity, модель БД
dto         -> объекты для общения с внешним миром
mapper      -> entity <-> dto
exception   -> свои исключения + глобальный обработчик
```

## Дорожная карта

| Фаза | Тема | Статус |
|---|---|---|
| 0 | Скелет монолита: каталог, JPA/Hibernate, Liquibase | 🔄 в работе |
| 1 | Безопасность: Spring Security + JWT | ⬜ не начато |
| 2 | Кеширование: Redis | ⬜ не начато |
| 3 | Контейнеризация: Docker, docker-compose | ⬜ не начато |
| 4 | Распил на микросервисы: Gateway, Eureka, OpenFeign | ⬜ не начато |
| 5 | Асинхронность: Apache Kafka | ⬜ не начато |
| 6 | Устойчивость и тесты: Resilience4j, Testcontainers | ⬜ не начато |
| 7 | Наблюдаемость (бонус): Config Server, трейсинг | ⬜ не начато |

## Запуск

### Требования

- JDK 21
- Maven 3.9+
- PostgreSQL (локально; в Фазе 3 переедет в Docker)

## Управление схемой

Схему БД ведёт **Liquibase**, а не Hibernate. Любая новая entity сопровождается
миграцией (changeset) в `src/main/resources/db/changelog/`. Hibernate только
проверяет соответствие — это осознанный выбор в пользу контролируемых миграций.

## Прогресс

README обновляется по мере закрытия фаз.