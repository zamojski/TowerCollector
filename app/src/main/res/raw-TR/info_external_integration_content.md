<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector; Tasker, Automate veya Llama gibi *intent* gönderen diğer uygulamalar tarafından çalıştırılacak görevleri ortaya çıkarır. Aşağıda tanımlanan tüm etkinlikler için *Broadcast* türünü kullanmanız gerekir. Böyle bir entegrasyonun nasıl yapılacağı hakkında daha fazla bilgi için uygulamaların yardım bölümlerini kontrol edin.
*Uygulama içinde olduğu gibi, aynı anda birden fazla göreve başlayamazsınız!*

### Toplayıcıyı başlatma

Toplayıcı, Tercihler\'de tanımlanan ayarlar kullanılarak başlatılacaktır. Eylem:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Toplayıcıyı durdur

Toplayıcı anında durdurulacak Eylem:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Yüklemeye başla

Yükleme, Tercihlerdeki yapılandırma kullanılarak başlatılacaktır. Eylem:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Yüklemeyi durdur

Yükleme kısa bir süre sonra durdurulacak. Eylem:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Dışa aktarmayı başlat

Dışa aktarma, son çalıştırmadan itibaren yapılandırma kullanılarak başlatılacak, ölçümler tutulacaktır. Eylem:

`info.zamojski.soft.towercollector.EXPORT_START`

### Dışa Aktarmayı Durdur

İhracat yakında iptal edilecektir. Eylem:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8 üzeri

Android oreo ve üzerinde ek olarak bileşen paketinin ismini tanımlamanız gerekir.

`info.zamojski.soft.towercollector`