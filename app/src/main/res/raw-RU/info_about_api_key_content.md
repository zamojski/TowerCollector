<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Токен доступа (API ключ) - это специальная строка, используемая для авторизации пользователя, чтобы загрузить измерения в базу OpenCellID.

Корректный ключ должен состоять из цифр 0-9 и латинских букв a-f. Ключ может выглядеть так:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 символа, перед которым стоит `pk.`, всего 35 символов
* `9743a66f914cc249efca164485a19c5c` - 32 символа
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 символа, разбитых на 5 групп и разделённых дефисом, всего 36 символов
* `9743a66f914cc2` - 14 символов