<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector exposes tasks to be run by other applications that send *intents* like Tasker, Automate or Llama. You need to use *Broadcast* type for all activities defined below. For more information how to do such integration check that\'s applications help.
*Like within the app you can\'t start more than one task at the same time!*

### Start Collector

Collector will be started using settings defined in Preferences. Action:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Stop Collector

Collector will be stopped immediately. Action:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Start Upload

Upload will be started using configuration from Preferences. Action:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Stop Upload

Upload will be cancelled soon. Action:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Start Export

Export will be started using configuration from last run, measurements will be kept. Action:

`info.zamojski.soft.towercollector.EXPORT_START`

### Stop Export

Export will be cancelled soon. Action:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

On Android Oreo and newer you need to additionally define component package name:

`info.zamojski.soft.towercollector`