# RTS Game — серверная часть

Серверная часть мобильной RTS-игры с real-time управлением юнитами. Игроки подключаются через TCP, отдают команды (создание юнитов, движение, атака), и все участники матча получают обновления синхронно.

## Стек технологий

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-244c5a?style=for-the-badge&logo=grpc&logoColor=white)
![Netty](https://img.shields.io/badge/Netty-004088?style=for-the-badge&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![H2](https://img.shields.io/badge/H2-0000CC?style=for-the-badge&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)

---

## Архитектура

Проект построен на микросервисной архитектуре. Каждый сервис отвечает за свою область и общается с остальными через gRPC. Kafka используется для асинхронного обновления данных игрока при старте матча.

```
┌─────────────────────────────────────────────────────┐
│                  Мобильный клиент                    │
└──────────────────────┬──────────────────────────────┘
          Netty TCP    │    ▲ broadcast
                       ▼    │
┌─────────────────────────────────────────────────────┐
│                   EventService                       │
│        маршрутизация команд · broadcast              │
└──────────┬──────────────┬──────────────┬────────────┘
        gRPC            gRPC           gRPC
           ▼              ▼              ▼
    ┌────────────┐ ┌────────────┐ ┌────────────┐
    │GameService │ │UnitService │ │PlayerService│
    │ матчи,     │ │ юниты,     │ │ игроки,    │
    │ состояние  │ │ движение,  │ │ активная   │
    │            │ │ атака      │ │ игра       │
    └─────┬──────┘ └────────────┘ └─────▲──────┘
          │                             │
          │           Kafka             │
          └──── game-started ──────────►│
                                    consume
```

### Сервисы

**EventService** — единая точка входа. Принимает TCP-соединения от клиентов через Netty, десериализует JSON-команды, маршрутизирует их к нужному микросервису по gRPC и рассылает ответы всем участникам матча через Netty-каналы.

**GameService** — управляет матчами: создание, присоединение игроков, старт, завершение. При старте игры публикует событие в Kafka.

**PlayerService** — хранит данные игроков. Подписан на Kafka-топик `game-started` и обновляет поле `activeGameId` у каждого игрока асинхронно, без прямого gRPC-вызова из GameService.

**UnitService** — обрабатывает всю логику юнитов: создание, передвижение по координатам, атака.

---

## Механика работы

1. Игрок подключается к Netty TCP серверу и отправляет JSON-команду.
2. EventService десериализует команду и вызывает нужный микросервис через gRPC.
3. Микросервис обрабатывает команду и изменяет состояние.
4. EventService рассылает обновление всем участникам матча (broadcast по Netty-каналам).
5. Состояние синхронизируется у всех игроков в реальном времени.

При старте игры GameService публикует событие в Kafka вместо прямого вызова PlayerService — это развязывает сервисы и исключает синхронное ожидание.

При обрыве соединения игрок может переподключиться командой `RECONNECT`: сервер проверяет, что игрок действительно участвовал в матче, добавляет канал обратно и возвращает текущее состояние игры.

---

## Команды (формат JSON)

Все команды отправляются через TCP-соединение в формате:

```json
{"commandType": "...", "payload": {...}}
```

| Команда | Пример |
|---|---|
| Создать игрока | `{"commandType":"PLAYER_CREATE","payload":{"name":"Alice"}}` |
| Создать игру | `{"commandType":"GAME_CREATE","payload":{"playerId":1}}` |
| Присоединиться | `{"commandType":"PLAYER_JOIN","payload":{"playerId":3,"gameId":1}}` |
| Покинуть игру | `{"commandType":"PLAYER_LEAVE","payload":{"playerId":3,"gameId":1}}` |
| Старт игры | `{"commandType":"GAME_START","payload":{"gameId":1}}` |
| Создать юнит | `{"commandType":"UNIT_CREATE","payload":{"type":"MAGICIAN","x":10,"y":20,"playerId":2,"gameId":1}}` |
| Передвинуть юнит | `{"commandType":"UNIT_MOVE","payload":{"unitId":1,"playerId":1,"x":30,"y":40}}` |
| Атаковать | `{"commandType":"UNIT_ATTACK","payload":{"playerId":1,"attackerId":1,"targetId":2}}` |
| Переподключиться | `{"commandType":"RECONNECT","payload":{"playerId":1,"gameId":1}}` |

---

## Запуск

```bash
docker compose up --build
```

Каждый сервис поднимается в отдельном контейнере. В качестве БД используется H2 (in-memory) — для упрощения dev-окружения. В production-окружении предполагается замена на PostgreSQL.

## Подключение и отправка команд

Клиент подключается к EventService через TCP на порт 8081. Для тестирования можно использовать `telnet`:

```bash
telnet localhost 8081
```

После подключения вводить JSON-команды и нажимать Enter:

```json
{"commandType":"PLAYER_CREATE","payload":{"name":"Alice"}}
```

---

## Структура проекта

```
rts-game/
├── event-service/     # TCP-сервер, маршрутизация команд
├── game-service/      # Управление матчами
├── unit-service/      # Логика юнитов
├── player-service/    # Данные игроков, Kafka consumer
└── docker-compose.yml
```
