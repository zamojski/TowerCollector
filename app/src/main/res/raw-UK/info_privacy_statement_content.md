<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Конфіденційність дуже важлива, тому програма збирає та передає лише необхідну інформацію для надання послуг. Додаток НЕ збирає, не зберігає та не надсилає будь-яку інформацію, яка могла б ідентифікувати Вас, Ваш пристрій чи будь-яку іншу особисту інформацію.

**Якщо ви не згодні з цією політикою конфіденційності, будь ласка, не встановлюйте та не видаляйте програму. Постійне видалення програми з мобільного пристрою еквівалентно припиненню використання програми.**

### Основні функції

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [Політика конфіденційності OpenCellID.org](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

Для більшості функцій програми потрібен **дозвіл розташування на передньому плані**, однак, щоб розпочати збір вимірювань під час запуску пристрою, потрібно надати **дозвіл на розташування у фоновому режимі**. Дозвіл на фонове розташування потрібен, оскільки в цьому випадку збір ініціюється системою (у фоновому режимі), а не вами (на передньому плані).

### Додаткові функції

Функція **map** використовує послуги, що надаються OpenStreetMap згідно з [умовами використання](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

Функція **автоматична перевірка оновлення** підключається до сервера, щоб перевірити, чи доступна новіша версія програми. Запит містить версію встановленої програми, і вона виконується під час запуску програми раз на день.

Функція **Зв'язок з розробником** створить нове повідомлення електронної пошти у вибраному поштовому клієнті. Повідомлення містить наступну технічну інформацію про пристрій, щоб допомогти у вирішенні несправностей:

- Версія програми
- Версія для Android
- Виробник та модель пристрою

Функція **Експорт налаштувань** або **Експорт бази даних** скопіює налаштування програми або базу даних у спеціальну папку на вашому пристрої. Якщо файли розміщені у спільній пам'яті, до якої має доступ будь-яка інша програма, то конфіденційні дані (наприклад, маркер доступу OpenCellID) можуть бути незахищеними.

Функція **реєстрація файлів на рівні налагодження** створить дуже докладні файли журналів у спеціальній папці на вашому пристрої. Журнал може містити конфіденційну інформацію, як-от ваше поточне місцезнаходження та вишки стільникового зв'язку, до яких підключений ваш пристрій. Цей параметр слід використовувати лише тимчасово для усунення несправностей.

### Помічники з усунення несправностей

Додаток збирає **анонімну статистику використання**. Дані передаються лише розробнику, щоб допомогти краще зрозуміти, як використовується програма, який час виконання різних функцій та які найпопулярніші функції. Зібрані дані використовуються для поліпшення загальної продуктивності, зручності використання та зручності користування. Дані збираються за допомогою сторонньої бібліотеки Google Analytics для Firebase, наданої Google відповідно до [Політики конфіденційності Google](https://policies.google.com/privacy).

Додаток має вбудовану **автоматизовану систему повідомлення про аварії**. Звіти про помилки програми надаються лише розробнику та містять інформацію про причину збою та конфігурацію пристрою. Він НЕ включає жодної інформації, яка може використовуватися для ідентифікації вас або пристрою.