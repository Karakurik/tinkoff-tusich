# Сервис для мероприятий и ачивок
Как мы знаем в тинькофф любят тусить, и на тусах разработчики постоянно выполняют какие-либо задания и получают за это ачивки.
Одну ачивку можно получить несколько раз, в таком случае просто увеличивается его уровень на 1.
## Как запустить
* Выполнить docker-compose up
* Зайти в контейнер постгреса через docker exec -it psql с кредами и параметрами из docker-compose
* Выполнить команды sql скрипты из `src/main/resources/db`
* После этого можно посылать запросы к серверу
* Можно запустить `demo/tinkoff-tisich.sh` скрипт для демонстрации запросов, в скрипте используется https://stedolan.github.io/jq/download/, возможно надо будет его установить

* GET /user - получение всех юзеров
* GET /user/{user-id} - получение юзера по id
* GET /user?first_name={}&last_name={} - получение юзера по иф
* DELETE /user/{user-id} - удаление юзера
* POST /user - создание юзера
* GET /user/{user-id}/achievements - получение всех ачивок юзера

* GET /tusich - получение всех тус
* GET /tusich/{tusich-id} - получение тусы по id
* DELETE /tusich/{tusich-id} - удаление тусы
* POST /tusich - создание тусы

* GET /achievement - получение всех ачивок
* GET /achievement/{achievement-id} - получение ачивки по id
* GET /achievement/{tusich-id} - получение ачивок конкретной тусы
* GET /achievement/{name} - получение ачивки по названию
* DELETE /achievement/{achievement-id} - удаление ачивки
* POST /achievement - создание ачивки