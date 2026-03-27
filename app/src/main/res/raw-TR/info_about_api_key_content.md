<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Erişim belirteci (API anahtarı), kullanıcının toplanan ölçümleri OpenCellID
veritabanına yüklemesine izin vermek için kullanılan özel bir dizedir.

Doğru anahtar 0-9 rakamlarından ve a-f harflerinden oluşmalıdır. Kayıt tarihine
bağlı olarak anahtar formatı şu şekilde olabilir:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 karakter, `pk.` ile başlıyor,
  toplam 35 karakter
* `9743a66f914cc249efca164485a19c5c` - 32 karakter
* `9743a66f-914c-c249-efca-164485a19c5c` - tire ile ayrılmış beş grupta 32
  karakter, toplam 36 karakter
* `9743a66f914cc2` - 14 karakter
