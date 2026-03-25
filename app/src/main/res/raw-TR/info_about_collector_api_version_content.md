<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Ölçümler toplandığı sırada baz istasyonu bilgilerini elde etmek için hangi
yöntemin kullanılacağını seçebilirsiniz.

*Daha güvenilir veriler elde etmek için en son sürümü kullanmanız şiddetle
tavsiye edilir. Android 4.1 ve önceki sürümleri çalıştıran cihazlarda bu seçim
devre dışıdır.*

* **Otomatik algılama** - Uygulama, hangi API sürümünün kullanılabileceğini
  otomatik olarak algılar ve en iyi seçeneği belirler. Sinyal yokken Collector
  başlatılırsa güvenli seçeneği kullanır.
* **Otomatik algılama** - Kullanılabilir API versiyonları otomatik olarak
  bulunur ve uygulama tarafından en iyi opsiyon seçilir. Eğer toplayıcı sinyal
  yokken çalıştırılırsa güvenli seçenek kullanılır.
* **Android 4.2 API** - Android 4.2 ve daha yeni sürümlerde bulunan yöntemleri
  kullanır. Bu yöntemler en güvenilir hücre bilgisini sağlar. Ancak bazı
  üreticiler yeni API'yi doğru bir şekilde uygulamadı ve bu tür cihazlarda
  kullanılamayabilir. Android 5.1 ve üzerinde farklı sim kartlardan farklı baz
  istasyonu bilgileri elde edilebilir.
