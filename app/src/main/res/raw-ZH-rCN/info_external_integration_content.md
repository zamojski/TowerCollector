<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector公开由发送 *intents* 的其他应用程序运行的任务，如Tasker，Automate或Llama。 您需要为下面定义的所有活动使用*Broadcast* 类型。 有关如何进行此类集成的更多信息，请检查应用程序是否有帮助。
*在应用中，您不能同时启动多个任务！*

### 启动收集器

收集器将使用设置中的选项启动。操作：

`info.zamojski.soft.towercollector.COLLECTOR_START`

### 停止收集器

收集器将立即停止。操作：

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### 开始上传

将使用"设置"中的配置启动上传。 操作：

`info.zamojski.soft.towercollector.UPLOADER_START`

### 停止上传

立即停止上传。操作：

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### 开始导出数据

使用上次运行的配置开始导出，将保留测量值。 操作：

`info.zamojski.soft.towercollector.EXPORT_START`

### 停止导出数据

取消导出数据。 操作：

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

在Android Oreo和更新的版本上，您还需要另外定义组件包名称：

`info.zamojski.soft.towercollector`