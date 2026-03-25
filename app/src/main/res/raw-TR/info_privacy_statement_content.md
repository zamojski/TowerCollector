<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Gizlilik çok önemlidir, bu nedenle uygulama yalnızca hizmetleri sağlamak için
gerekli bilgileri toplar ve iletir. Uygulama sizi, cihazınızı veya diğer kişisel
bilgileri kişisel olarak tanımlayabilecek herhangi bir bilgiyi toplamaz,
saklamaz veya göndermez.

**Bu gizlilik politikasını kabul etmiyorsanız, lütfen uygulamayı yüklemeyin veya
kaldırmayın. Uygulamanın mobil cihazdan kalıcı olarak kaldırılması, uygulamanın
kullanımının sonlandırılmasına eşdeğerdir.**

### Ana fonksiyonlar

Uygulamanın temel işlevi, baz istasyonu tanımlayıcılarını, sinyal gücünü ve
cihaz konumunu (koordinatlarını) toplamaktır. Bu bilgiler cihazınızda yerel
olarak saklanır ve açık izniniz ve işleminiz olmadan asla iletilmez. Toplanan
veriler, cihazınızda kalıcı olarak saklanan bir dosyaya aktarılabilir veya
OpenCellID.org ve BeaconDB projeleriyle paylaşılabilir. Gizliliğin korunmasını
sağlamak için, uygulama, verilerinize iletim sırasında erişebilecek herhangi bir
proxy sunucusu kullanmaz. Harici hizmetlere giden tüm trafik HTTPS protokolü
kullanılarak şifrelenir.

**OpenCellID.org** projesine katkıda bulunmak için, uygulama tercihlerine
girilmesi gereken kişisel bir Erişim Belirteci (API Anahtarı) gereklidir. Bu
anahtar yalnızca ölçümlerin proje veritabanına yüklenmesi için kullanılacak ve
başka hiçbir durumda cihazdan dışarı çıkarılmayacaktır. Erişim Belirtecini bilen
herkes, yüklenen veriler üzerinde tam kontrole sahiptir; bu kontrol, verilere
göz atmayı ve silmeyi de içerir. Erişim Belirtecinizin gizliliğini korumak sizin
sorumluluğunuzdadır!

- [OpenCellID.org gizlilik politikası](https://community.opencellid.org/privacy)
- [BeaconDB gizlilik bildirimi](https://beacondb.net/privacy/)

Uygulamanın çoğu özelliği **ön plan konum iznini** gerektirir, ancak cihaz
başlangıcında ölçümleri toplamaya başlamak için **arka plan konum izninin**
verilmesi gerekir. Arka plan konum izni gereklidir çünkü bu senaryoda, toplama
sizin tarafınızdan değil (ön planda) sistem tarafından başlatılır (arka planda).

### İsteğe bağlı işlevler

**Harita** özelliği, OpenStreetMap tarafından [kullanım
şartları](https://wiki.osmfoundation.org/wiki/Terms_of_Use) altında sağlanan
hizmetleri kullanır.

**Otomatik güncelleme kontrolü** özelliği, uygulamanın daha yeni bir sürümü olup
olmadığını kontrol etmek için sunucuya bağlanır. İstek, kurulu uygulamanın
sürümünü içerir ve uygulamanın başlangıcında günde bir kez yürütülür.

**Geliştiriciyle iletişime geç** özelliği, seçilen e-posta istemcisinde yeni bir
e-posta iletisi oluşturur. Mesaj, sorun gidermeye yardımcı olmak için cihazla
ilgili aşağıdaki teknik bilgileri içerir:

- Uygulama sürümü
- Android sürümü
- Cihaz üreticisi ve modeli

**Tercihleri dışa aktar** veya **Veritabanını dışa aktar** özelliği, uygulama
tercihlerini veya veritabanını cihazınızdaki ayrılmış klasöre kopyalar.
Dosyalar, başka herhangi bir uygulamanın erişebildiği paylaşılan belleğe
yerleştirilirse, hassas veriler (OpenCellID Erişim Belirteci gibi) korumasız
olabilir.

**Hata Ayıklama düzeyinde dosya günlüğü** özelliği, cihazınızdaki ayrılmış
klasörde çok ayrıntılı günlük dosyaları oluşturur. Günlük, geçerli konumunuz ve
cihazınızın bağlı olduğu baz istasyonu gibi hassas bilgileri içerebilir. Bu
seçenek yalnızca sorun giderme için geçici olarak kullanılmalıdır.

### Sorun giderme yardımcıları

Uygulama, **anonim kullanım istatistiklerini** toplar. Veriler, uygulamanın
nasıl kullanıldığını, çeşitli işlevlerin çalıştırılma sürelerinin ve en popüler
özelliklerin daha iyi anlaşılmasına yardımcı olmak için yalnızca geliştirici ile
paylaşılır. Toplanan veriler, genel performansı, kullanılabilirliği ve kullanıcı
deneyimini iyileştirmek için kullanılır. Veriler, [Google'ın gizlilik
politikası](https://policies.google.com/privacy) kapsamında Google tarafından
sağlanan Firebase için üçüncü taraf Google Analytics kitaplığı kullanılarak
toplanır.

Uygulama, yerleşik **otomatik kilitlenme raporlama** sistemine sahiptir.
Uygulama hatası raporları yalnızca geliştiriciyle paylaşılır ve çökme nedeni ve
cihaz yapılandırması hakkında bilgiler içerir. Sizi veya cihazı tanımlamak için
kullanılabilecek herhangi bir bilgi İÇERMEZ.
