# Tower Collector
This repository contains the source code of the Tower Collector Android app.

The app is designed to help map the extent of mobile phone network coverage of GSM/UMTS/LTE/CDMA cell towers using GPS locations. This task is accomplished by reading GPS coordinates and identifiers of mobile network transmitters in regular intervals. Collected information is saved in your phone memory and can be uploaded to [OpenCellID](http://opencellid.org) database. It is also possible to collect measurements for private purposes and export them to CSV or GPX files.


## Building
Project is build using Android Studio 2.0.

To build your own signed apk go to "[app/properties](app/properties)" and:

 1. Place your keystore file inside the folder.
 2. Copy "[example.properties](app/properties/example.properties)" as "private.properties" and adjust it accordingly.
 3. These files will be ignored so don't worry about committing them by mistake.

## Bug reports and feature requests
Please report through Issues tab on this page or email me directly.

## Translations
Please help me translate it to other languages, visit http://i18n.zamojski.info/

## License
The source code is available under Mozilla Public License 2.0. See [LICENSE.txt](LICENSE.txt) for more info.
