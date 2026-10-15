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
| Безопасность | Spring Security, OAuth2 Resource Server, Keycloak (OAuth2 / OIDC) |
| Кеш | Redis |
| Инфраструктура | Docker, docker-compose |
| Прочее | Lombok, Bean Validation, springdoc-openapi |

В следующих фазах добавятся: Spring Cloud Gateway, Eureka, Apache Kafka, Resilience4j, Testcontainers.

## Архитектура

**Целевая :** набор микросервисов за единым API Gateway.

- `api-gateway` — единая точка входа, маршрутизация, проверка JWT
- `discovery-server` (Eureka) — реестр сервисов
- `Keycloak` — сервер авторизации (OAuth2 / OIDC): логин, регистрация, выдача JWT, роли. Сервисы только проверяют токен
- `product-service` — каталог товаров, кеш в Redis
- `order-service` — заказы, транзакции, события в Kafka
- `notification-service` — асинхронная обработка событий из Kafka

**Текущая:** монолит-каталог (resource server) + Keycloak (authorization server). Слоистая архитектура:

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
| 0 | Скелет монолита: каталог, JPA/Hibernate, Liquibase | ✅ готово |
| 1 | Безопасность: Spring Security + JWT (свой, HS256) | ✅ готово |
| 1.5 | Keycloak: OAuth2/OIDC, resource server, RS256 | ✅ готово |
| 2 | Кеширование: Redis | ✅ готово |
| 3 | Контейнеризация: Docker, docker-compose | 🔄 частично (Keycloak в compose) |
| 4 | Распил на микросервисы: Gateway, Eureka, OpenFeign | ⬜ не начато |
| 5 | Асинхронность: Apache Kafka | ⬜ не начато |
| 6 | Устойчивость и тесты: Resilience4j, Testcontainers | ⬜ не начато |
| 7 | Наблюдаемость (бонус): Config Server, трейсинг | ⬜ не начато |

## Запуск

### Требования

- JDK 21
- Maven 3.9+
- PostgreSQL (локально; в Фазе 3 переедет в Docker)
- Docker Desktop

### Keycloak

```bash
docker compose up -d
```

Keycloak поднимается на `http://localhost:8180` (админка: admin / admin).
Realm `marketplace` импортируется при старте из `keycloak/marketplace-realm.json`:
роли `USER` / `ADMIN`, клиенты и тестовые пользователи.

| Пользователь | Пароль | Роли |
|---|---|---|
| nikita | nikita | USER |
| admin2 | admin2 | ADMIN |

**Swagger:** `http://localhost:8070/swagger-ui.html` → Authorize → `keycloak`
(Authorization Code + PKCE, логин на странице Keycloak).

**Токен из терминала** (учебный Password grant, клиент `marketplace-cli`):

```bash
curl.exe -s -X POST http://localhost:8180/realms/marketplace/protocol/openid-connect/token -d grant_type=password -d client_id=marketplace-cli -d username=admin2 -d password=admin2
```

> Пароли и realm с пользователями лежат в репо только потому, что это учебный проект.
> Таблица `users` в БД остаётся ради ветки `security-own-jwt` (своя выдача JWT на HS256).

## Управление схемой

Схему БД ведёт **Liquibase**, а не Hibernate. Любая новая entity сопровождается
миграцией (changeset) в `src/main/resources/db/changelog/`. Hibernate только
проверяет соответствие — это осознанный выбор в пользу контролируемых миграций.

## Прогресс

README обновляется по мере закрытия фаз.