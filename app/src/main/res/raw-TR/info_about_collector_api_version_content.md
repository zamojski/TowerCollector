<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Ölçümler toplandığı sırada baz istasyonu bilgilerini elde etmek için hangi yöntemin kullanılacağını seçebilirsiniz.

* Daha güvenilir veriler elde etmek için en yeni yöntemi kullanmanız şiddetle tavsiye edilir. Seçim, Android 4.1 ve öncesi cihazlarda devre dışıdır.*

*   **Otomatik algılama** - Kullanılabilir API versiyonları otomatik olarak bulunur ve uygulama tarafından en iyi opsiyon seçilir.  Eğer toplayıcı sinyal yokken çalıştırılırsa güvenli seçenek kullanılır.
* **Android 4.2 API** - Android 4.2 ve daha yeni sürümlerde bulunan yöntemleri kullanır. Bu yöntemler en güvenilir hücre bilgisini sağlar. Ancak bazı üreticiler yeni API'yi doğru bir şekilde uygulamadı ve bu tür cihazlarda kullanılamayabilir. Android 5.1 ve üzerinde farklı sim kartlardan farklı baz istasyonu bilgileri elde edilebilir.
* **Android 1.0 API** - Anroidin ilk sürümünden beri mevcut olan bir dizi yöntem kullanır. Bulutu metodlar LTE ve UMTS ağları için güvenilir olmayan veriler sağlayabilir. Cihaza bağlı olarak bu metod hücre bilgilerinin doğru bir şekilde güncellenmesini sağlamak için toplama sırasında ekranın açık kalmasını gerektirebilir.