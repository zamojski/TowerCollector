<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Колектор сот виставляє завдання для запуску інших програм, які надсилають *intents*, як Tasker, Automate або Llama. Вам потрібно використовувати тип *Broadcast* для всіх дій, визначених нижче. Для отримання додаткової інформації про те, як зробити таку інтеграцію перевірити інструкцію програми
*Як у додатку, ви не можете розпочати більше одного завдання одночасно!*

### Запуск колектора

Колектор буде запущений з використанням параметрів, визначених у Параметрах. Дія:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Зупинити колектор

Колекціонер буде зупинений негайно. Дія:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Почніть вивантажувати

Завантаження буде розпочато за допомогою конфігурації з Налаштування. Акція:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Припинити завантаження

Завантаження буде скасовано найближчим часом. Дії:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Почати експорт

Експорт розпочнеться з використанням конфігурації з останнього запуску, виміри будуть збережені. Дія:

`info.zamojski.soft.towercollector.EXPORT_START`

### Зупинити експорт

Незабаром експорт буде скасовано. Дія:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

У Android Oreo і новіші ви повинні додатково визначити назву компонента пакета:

`info.zamojski.soft.towercollector`