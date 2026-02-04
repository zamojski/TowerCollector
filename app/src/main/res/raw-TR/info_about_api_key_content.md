[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Erişim tokeni (API anahtarı) kullanıcıya toplanan ölçümleri OpenCellID veri tabanına yükleme yetkisi vermek için kullanılan özel bir dizedir.

Doğru anahtar 0 ile 9 arası rakamlardan ve a ve f arası harflerden oluşmalı. Kayıt tarihine bağlı olarak anahtar biçimi bunun gibi olabilir:

* `pk.9743a66f914cc249efca164485a19c5c` - başında `pk.` bulunan 32 karakter, toplam 35 karakter
* `9743a66f914cc249efca164485a19c5c` - 32 karakter
* `9743a66f-914c-c249-efca-164485a19c5c` - tire ile ayrılan 5 gruptan oluşan 32 karakter, toplamda 36 karakter
* `9743a66f914cc2` - 14 karakter