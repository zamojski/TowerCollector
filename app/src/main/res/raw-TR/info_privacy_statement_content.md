[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Gizlilik çok önemlidir, bu nedenle uygulama yalnızca hizmetleri sağlamak için gerekli bilgileri toplar ve iletir. Uygulama sizi, cihazınızı veya diğer kişisel bilgileri kişisel olarak tanımlayabilecek herhangi bir bilgiyi toplamaz, saklamaz veya göndermez.

**Bu gizlilik politikasını kabul etmiyorsanız, lütfen uygulamayı yüklemeyin veya kaldırmayın. Uygulamanın mobil cihazdan kalıcı olarak kaldırılması, uygulamanın kullanımının sonlandırılmasına eşdeğerdir.**

### Ana fonksiyonlar

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [OpenCellID.org gizlilik politikası](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

Uygulamanın çoğu özelliği **ön plan konum iznini** gerektirir, ancak cihaz başlangıcında ölçümleri toplamaya başlamak için **arka plan konum izninin** verilmesi gerekir. Arka plan konum izni gereklidir çünkü bu senaryoda, toplama sizin tarafınızdan değil (ön planda) sistem tarafından başlatılır (arka planda).

### İsteğe bağlı işlevler

**Harita** özelliği, OpenStreetMap tarafından [kullanım şartları](https://wiki.osmfoundation.org/wiki/Terms_of_Use) altında sağlanan hizmetleri kullanır.

**Otomatik güncelleme kontrolü** özelliği, uygulamanın daha yeni bir sürümü olup olmadığını kontrol etmek için sunucuya bağlanır. İstek, kurulu uygulamanın sürümünü içerir ve uygulamanın başlangıcında günde bir kez yürütülür.

**Geliştiriciyle iletişime geç** özelliği, seçilen e-posta istemcisinde yeni bir e-posta iletisi oluşturur. Mesaj, sorun gidermeye yardımcı olmak için cihazla ilgili aşağıdaki teknik bilgileri içerir:

- Uygulama sürümü
- Android sürümü
- Cihaz üreticisi ve modeli

**Tercihleri dışa aktar** veya **Veritabanını dışa aktar** özelliği, uygulama tercihlerini veya veritabanını cihazınızdaki ayrılmış klasöre kopyalar. Dosyalar, başka herhangi bir uygulamanın erişebildiği paylaşılan belleğe yerleştirilirse, hassas veriler (OpenCellID Erişim Belirteci gibi) korumasız olabilir.

**Hata Ayıklama düzeyinde dosya günlüğü** özelliği, cihazınızdaki ayrılmış klasörde çok ayrıntılı günlük dosyaları oluşturur. Günlük, geçerli konumunuz ve cihazınızın bağlı olduğu baz istasyonu gibi hassas bilgileri içerebilir. Bu seçenek yalnızca sorun giderme için geçici olarak kullanılmalıdır.

### Sorun giderme yardımcıları

Uygulama, **anonim kullanım istatistiklerini** toplar. Veriler, uygulamanın nasıl kullanıldığını, çeşitli işlevlerin çalıştırılma sürelerinin ve en popüler özelliklerin daha iyi anlaşılmasına yardımcı olmak için yalnızca geliştirici ile paylaşılır. Toplanan veriler, genel performansı, kullanılabilirliği ve kullanıcı deneyimini iyileştirmek için kullanılır. Veriler, [Google'ın gizlilik politikası](https://policies.google.com/privacy) kapsamında Google tarafından sağlanan Firebase için üçüncü taraf Google Analytics kitaplığı kullanılarak toplanır.

Uygulama, yerleşik **otomatik kilitlenme raporlama** sistemine sahiptir. Uygulama hatası raporları yalnızca geliştiriciyle paylaşılır ve çökme nedeni ve cihaz yapılandırması hakkında bilgiler içerir. Sizi veya cihazı tanımlamak için kullanılabilecek herhangi bir bilgi İÇERMEZ.