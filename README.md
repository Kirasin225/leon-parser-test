# Leonbets Async Parser

Тестовое задание на разработку парсера прематч данных.

## Стек технологий
* **Java 21** (Core)
* **Jackson** (JSON Processing)
* **ExecutorService** (Custom Thread Pool)
* **java.net.http.HttpClient**
* **Lombok**

## Особенности реализации
1. **Асинхронность:** Используется кастомный пул на 3 потока (`Executors.newFixedThreadPool(3)`), как требовалось в ТЗ.
2. **Anti-Bot:**
   - Ротация `User-Agent`.
   - Задержки между запросами (имитация поведения человека).

## Запуск
Поскольку основной домен может быть недоступен, рекомендуется запускать через VPN.

```bash
mvn clean package
java -jar target/parser.jar
