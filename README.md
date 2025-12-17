# Tower Collector
Join the OpenCellID community and collect cell towers' locations from your area!

Tower Collector gives you opportunity to contribute to [OpenCellID.org](https://opencellid.org/) and [BeaconDB](https://beacondb.net/) projects by uploading GPS locations of GSM/UMTS/LTE/CDMA cell towers from your area. The measurements help map the extent of mobile phone network coverage. You can use the app to collect data for personal purposes and export them to CSV or GPX files.

# How to Get

Tower Collector is available for free on the Google Play store:

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=info.zamojski.soft.towercollector)

and on official F-Droid catalog:

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/info.zamojski.soft.towercollector/)

# Give thanks!
Consider supporting the project:

[<img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" height="60" padding="10">](https://www.buymeacoffee.com/zamojski)

## Building
This project is build using Android Studio and Gradle.

To build your own signed apk go to "[app/properties](app/properties)" and:

 1. Place your keystore file inside the folder.
 2. Copy "[example.properties](app/properties/example.properties)" as "private.properties" in the same directory and adjust it accordingly.
 3. Copy "[google-services-example.json](app/google-services-example.json)" as "google-services.json" in the same directory and adjust it accordingly or place your own generated file there.
 4. These files will be ignored so don't worry about committing them by mistake.

### Signing key fingerprint for Google Play and GitHub release (Official)
```
SHA1: E8:1C:C8:FC:65:50:87:9D:E1:AC:93:DB:04:8C:A5:F3:69:72:63:25
SHA-256: A4:D7:6D:2E:27:D8:25:BD:1B:2C:E9:75:2F:5A:7A:7B:77:B5:A8:AC:6A:6B:18:7A:E8:52:54:81:C8:E4:37:31
```

## Bug reports and feature requests
Please report any bugs or submit feature requests through the *Issues* tab on this page or email me directly.

## Translations
~If you would like to help out with translation, please visit https://i18n.zamojski.feedback/~

OneSky has suspended their translation service, so as of now the only way to contribute is to create a pull request to this repository.

## License
Tower Collector is licensed under the Mozilla Public License 2.0. A copy of this license is included in [LICENSE.txt](LICENSE.txt).
