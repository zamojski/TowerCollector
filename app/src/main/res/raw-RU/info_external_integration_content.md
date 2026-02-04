[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Tower Collector выставляет задачи, которые будут запускаться другими приложениями, посылающими *события*, например Tasker, Automate или Llama. Вы должны использовать тип *Широковещательный* для всех активностей, определённых ниже. Для более подробной информации об интеграции с другими приложениями, смотрите раздел Помощь этих приложений.
*Как и в самом Tower Collector вы не можете запустить более одной задачи одновременно!*

### Запустить Сборщик

Сборщик будет запущен с использованием параметров указанных в Настройках. Действие:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Остановить Сборщик

Сборщик будет остановлен немедленно. Действие:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Запустить Выгрузку

Загрузка будет запущена используя конфигурацию из Настроек. Действие:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Остановить выгрузку

Выгрузка скоро будет остановлена. Действие:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Начать экспорт

Экспорт будет запущен с использованием конфигурации из последнего запуска, измерения будут сохранены. Действие:

`info.zamojski.soft.towercollector.EXPORT_START`

### Остановить экспорт

Экспорт скоро будет отменен. Действие:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

В Android Oreo и новее вам необходимо дополнительно определить имя пакета компонента:

`info.zamojski.soft.towercollector`