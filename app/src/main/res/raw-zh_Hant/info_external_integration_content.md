<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector 可向其他應用程式（如 Tasker、Automate 或 Llama）提供可被觸發的工作，這些應用程式需透過發送 Intent
來執行。\
針對下列所有活動，請一律使用 Broadcast 類型。\
若需了解如何進行此類整合，請參閱對應應用程式的說明文件。\
注意：與在應用程式內相同，同一時間無法啟動超過一個工作！

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

Export will be started using configuration from last run, measurements will be
kept. Action:

`info.zamojski.soft.towercollector.EXPORT_START`

### Stop Export

Export will be cancelled soon. Action:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

On Android Oreo and newer you need to additionally define component package
name:

`info.zamojski.soft.towercollector`
