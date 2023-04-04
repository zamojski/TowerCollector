# Tower Collector FAQ

## Known issues

### Low number of cells collected when screen is off
**Problem:** When screen is off only a very small number of cell towers is collected comparing to collecting with screen on. The problem is known to appear on every Android version. For instance for 10 km trip:
- Screen on: 150 measurements of 20 cells collected.
- Screen off: 125 measurements of 1 cells collected.

**Cause:** The problem is related to specific chipset/hardware manufacturer and it was observed on older Qualcomm Snapdragon but not on Broadcom based devices. The low level chipset internal software stops reporting cell and signal strength changes.<br>This problem has been partially mitigated by new API methods introduced in Android 5.1, <a href="#MultipleSIMCards">see more</a>.

**Solution:** The workaround is to keep screen on while collecting and such option is available in app's Preferences. A side effect is that battery life will be considerably shorter.

### Notification sound and vibrations on Android Oreo and newer
**Problem:** On Android 8.0+ when starting collector or upload the device vibrates and plays notification sound but it wasn't on older Android versions.

**Cause:** Tower Collector services like collector and uploader are started as foreground services to ensure continuous operation when device is getting low on memory. In version 8.0 Google changed the way to display notifications (by introducing notification channels) and their priorities. Every foreground service needs to show a notification but Android [ignores its priority](https://developer.android.com/reference/android/app/NotificationManager#IMPORTANCE_MIN) which results in vibration and sound being played unintentionally (even if channel importance is set to low). Additionally it's no longer possible to change channel importance once it's created nor start foreground service with hidden notification, so this option has been removed from app's Preferences on Oreo and newer.

**Solution:** You can manually disable sound and vibration if you want by long pressing on posted notification and going to settings.

## Frequent questions

### Is there support for multi-SIM phones?
Currently Tower Collector provides support for multiple SIM cards, check details below.
- **Android 5.1 and newer** - In version 5.1 Google introduced official support for multi-SIM devices. Unfortunately number of phones running on this and newer versions is very low currently, especially in low-end devices segment. Upgrading operating system version to 5.1+ doesn't guarantee that new methods will be correctly implemented and supported which makes testing more complicated. The more changes device manufacturer introduces comparing to stock Android version the less is the chance that new methods will be fully implemented as intended by Google.
- **Older Android versions** - The behavior and available methods providing necessary data strictly depend of hardware manufacturer. Many different implementations exist of the market. Because of this it's impossible to correctly support every multi-SIM device available on the market. Moreover some of the phones are available only in very limited regions and thus it's not possible to test the app on all off them.

### How to enable diagnostic logging?
The diagnostic logging helps software developers to track down and fix errors. In order to enable logging follow below steps:
1. Open Tower Collector Preferences.
2. Go to 'Advanced' section.
3. Select 'File logging level'.
4. Select 'Debug' option.
5. Use the function you want to troubleshoot.

The log file will be saved in 'TowerCollector' folder on the external memory (the same folder as exported files).

**Remember to disable logging when you don't need it to avoid using all available space by the log files.**

### Is Tower Collector an Open Source project?
Since 2016 Tower Collector is an open source project developed and maintained mainly by me in my spare time. Source code is available on [GitHub](https://github.com/zamojski/TowerCollector/).