<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Конфиденциальность очень важна, поэтому приложение собирает и передает только необходимую информацию для предоставления услуг. Приложение НЕ собирает, не хранит и не отправляет какую-либо информацию, которая может идентифицировать вас, ваше устройство или любую другую личную информацию.

**Если вы не согласны с этой политикой конфиденциальности, не устанавливайте и не удаляйте приложение. Окончательное удаление приложения с мобильного устройства равносильно прекращению использования приложения.**

### Основные функции

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [Политика конфиденциальности OpenCellID.org](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

Для большинства функций приложения требуется **разрешение на определение местоположения на переднем плане**, однако, чтобы начать сбор измерений при запуске устройства, необходимо предоставить **разрешение на определение местоположения в фоновом режиме**. Разрешение фонового расположения требуется, потому что в этом сценарии сбор инициируется системой (в фоновом режиме), а не вами (на переднем плане).

### Дополнительные функции

Функция **карта** использует услуги, предоставляемые OpenStreetMap в соответствии с [условиями использования](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

Функция **автоматической проверки обновлений** подключается к серверу, чтобы проверить, доступна ли более новая версия приложения. Запрос содержит версию установленного приложения и выполняется один раз в сутки при запуске приложения.

Функция **Связаться с разработчиком** создаст новое сообщение электронной почты в выбранном почтовом клиенте. Сообщение содержит следующую техническую информацию об устройстве, которая поможет в устранении неполадок:

- Версия приложения
- Версия Android
- Производитель и модель устройства

Функция **Экспорт настроек** или **Экспорт базы данных** скопирует настройки приложения или базу данных в специальную папку на вашем устройстве. Если файлы размещаются в общей памяти, к которой имеет доступ любое другое приложение, конфиденциальные данные (например, токен доступа OpenCellID) могут быть незащищенными.

Функция **ведения журнала на уровне отладки** создает очень подробные файлы журнала в специальной папке на вашем устройстве. Журнал может содержать конфиденциальную информацию, такую как ваше текущее местоположение и вышка сотовой связи, к которой подключено ваше устройство. Этот параметр следует использовать только временно для устранения неполадок.

### Разрешение проблем

Приложение собирает **анонимную статистику использования**. Данные передаются только разработчику, чтобы помочь лучше понять, как используется приложение, каково время выполнения различных функций и какие функции являются наиболее популярными. Собранные данные используются для повышения общей производительности, удобства использования и удобства использования. Данные собираются с использованием сторонней библиотеки Google Analytics для Firebase, предоставленной Google в соответствии с [политикой конфиденциальности Google](https://policies.google.com/privacy).

В приложение встроена система **автоматических отчетов о сбоях**. Отчеты о сбоях приложений предоставляются только разработчику и содержат информацию о причине сбоя и конфигурации устройства. Он НЕ включает какую-либо информацию, которая может быть использована для идентификации вас или устройства.