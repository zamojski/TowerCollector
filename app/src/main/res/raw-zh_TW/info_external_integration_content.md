<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector公開由發送 *intents* 的其他應用程式執行的任務，如Tasker，Automate或Llama。
您需要為下面定義的所有活動使用*Broadcast* 類型。 有關如何進行此類整合的更多信息，請檢查應用程式是否有幫助。
*在應用程式中，您不能同時啟動多個任務！ *

### 启动收集器

收集器將使用設定中的選項啟動。操作：

`info.zamojski.soft.towercollector.COLLECTOR_START`

### 停止收集器

收集器將立即停止。操作：

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### 開始上傳

將使用"設定"中的配置啟動上傳。 操作：

`info.zamojski.soft.towercollector.UPLOADER_START`

### 停止上傳

立即停止上傳。操作：

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### 開始匯出數據

使用上次執行的配置開始匯出，將保留測量值。 操作：

`info.zamojski.soft.towercollector.EXPORT_START`

### 停止導出數據

取消匯出數據。 操作：

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

在Android Oreo和更新的版本上，您還需要另外定義元件包名稱：

`info.zamojski.soft.towercollector`
