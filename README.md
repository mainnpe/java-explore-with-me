# java-explore-with-me

Template repository for ExploreWithMe project.
___

### Дополнительная функциональность

#### Добавление конкретных локаций, поиск событий по локации.

1. ***POST* /admin/cities** - добавление городов админом
2. ***POST* /admin/locations** - добавление конкретной локации админом
3. ***POST* /users/{userId}/locations** - добавление конкретной локации пользователем
4. ***GET* /cities** - получения списка городов (public)
5. ***GET* /locations** - получения списка локаций (public)
6. ***GET* /events** - добавлена возможность поиска событий по городам, по конкретной локации, а также событий рядом с
   текущей
   локацией (по координатам и диапазону). Параметры строки запроса:
   * *cities* - список id городов
   * *locations* - список id конкретных локаций
   * *x* - текущая широта
   * *y* - текущая долгота
   * *r* - радиус для поиска локаций (км)

**После добавление фичи тесты Postman, рассчитанные на основной функционал не выполняются, в силу того, что в поле
сущности Event добавился идентификатор конкретной локации, к которой привязано событие. Так как данного поля нет в
существующей коллекции, то все тесты, завязанные на создание события не выполняются.**
