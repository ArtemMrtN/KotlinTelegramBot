Учебный бот-викторина, который помогает запоминать иностранные слова.  
Первая версия: октябрь 2024

## 📋 Описание проекта
* Бот присылает слово и 4 варианта перевода; пользователь выбирает правильный ответ кликом по кнопке.  
* Прогресс хранится в локальном файле CSV: команда `/stats` показывает «выучено / осталось».  
* Словари (любой языковой пары)

## 🚀 Установка и запуск

Создать виртуальный сервер (Ubuntu), получить: ip-адрес, пароль для root пользователя
Подключиться к серверу по SSH используя команду ssh root@100.100.100.100 и введя пароль
Обновить установленные пакеты командами apt update и apt upgrade
Устанавливаем JDK коммандой apt install default-jdk
Убедиться что JDK установлена командой java --version
Публикация и запуск

Соберем shadowJar командой ./gradlew shadowJar
./gradlew clean build
Копируем jar на наш VPS переименуя его одновременно в bot.jar: scp build/libs/WordsTelegramBot-1.0-SNAPSHOT.jar root@100.100.100.100:/root/bot.jar
Копируем words.txt на VPS: scp words.txt root@100.100.100.100:/root/words.txt
Подключиться к серверу по SSH используя команду ssh root@100.100.100.100 и введя пароль
Запустить бота в фоне командой nohup java -jar bot.jar <ТОКЕН ТЕЛЕГРАМ> &
Проверить работу бота

## Контакты
Автор — Мартынов Артем
✉️ artemmrt@gmail.com · 🖇 [LinkedIn](https://www.linkedin.com/in/artem-n-martynov/)
