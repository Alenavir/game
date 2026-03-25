# RTS Game

Серверная часть мобильной RTS игры. Игроки управляют юнитами на карте в реальном времени, выполняя команды создания, движения и атаки.

---

## Архитектура
- **PlayerService** — управление игроками и их данными.
- **UnitService** — логика создания и управления юнитами.
- **GameService** — управление матчами и состоянием игры.
- **EventService** — получает команды игроков и рассылает обновления всем участникам матча.

---

## Механика работы
1. Игрок отправляет команду (создать юнит, двигать юнит, атаковать) через Netty TCP сервер.
2. EventService получает команду и вызывает нужный микросервис через gRPC.
3. Микросервисы обрабатывают команду, изменяют состояние игры.
4. EventService рассылает всем игрокам обновления через Netty каналы (broadcast).
5. Все действия происходят в реальном времени, синхронизируя состояние у всех игроков.

---

## Примеры команд
СОЗДАНИЕ ИГРОКА
{"commandType":"PLAYER_CREATE","payload":{"name":"Alice"}}

СОЗДАНИЕ ИГРЫ
{"commandType":"GAME_CREATE","payload":{"playerId":1}}

ПРИСОЕД ИГРОКА
{"commandType":"PLAYER_JOIN","payload":{"playerId":3,"gameId":1}}

ВЫХОД ИГРОКА
{"commandType":"PLAYER_LEAVE","payload":{"playerId":3,"gameId":1}}

СТАРТ ИГРЫ
{"commandType":"GAME_START","payload":{"gameId":1}}

СОЗДАНИЕ ЮНИТА
{"commandType":"UNIT_CREATE","payload":{"type":"MAGICIAN","x":10,"y":20,"playerId":2,"gameId":1}}

ПЕРЕДВИЖЕНИЕ ЮНИТА
{"commandType":"UNIT_MOVE","payload":{"unitId":1,"playerId":1,"x":30,"y":40}}

АТАКОВАТЬ ЮНИТ
{"commandType":"UNIT_ATTACK","payload":{"playerId":1,"attackerId":1,"targetId":2}}
