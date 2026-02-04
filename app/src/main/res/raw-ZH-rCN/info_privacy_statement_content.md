[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
隐私非常重要，因此应用程序仅收集和传输所需的信息以提供服务。该应用程序不会收集，存储或发送任何可个人识别您，您的设备或任何其他个人信息。

**如果您不同意此隐私政策，请不要安装或卸载该应用程序。从移动设备中永久删除该应用程序等同于终止该应用程序的使用。**

### 主要功能

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [OpenCellID.org隐私权政策](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

该应用程序的大多数功能都需要**前台位置权限**，但是，要在设备启动时开始收集测量值，必须授予**后台位置权限**。需要后台位置权限，是因为在这种情况下，收集是由系统（在后台）而不是您（在前台）启动的。

### 可选功能

**地图**功能使用OpenStreetMap在[使用条款](https://wiki.osmfoundation.org/wiki/Terms_of_Use)下提供的服务。

**自动更新检查**功能连接到服务器，以检查是否有可用的较新版本的应用程序。 该请求包含已安装应用程序的版本，并且每天在应用程序启动时执行一次。

**联系开发人员**功能将在选定的电子邮件客户端中创建新的电子邮件。该消息包括有关设备的以下技术信息，以帮助进行故障排除：

- 应用版本
- 安卓版
- 设备制造商和型号

**导出首选项**或**导出数据库**功能会将应用程序首选项或数据库复制到设备上的专用文件夹中。如果将文件放置在任何其他应用程序都可以访问的共享内存上，则敏感数据（如OpenCellID访问令牌）可能不受保护。

**导出首选项**或**导出数据库**功能会将应用程序首选项或数据库复制到设备上的专用文件夹中。如果将文件放置在任何其他应用程序都可以访问的共享内存上，则敏感数据（如OpenCellID访问令牌）可能不受保护。

### 故障排除助手

该应用程序收集**匿名使用统计信息**。数据仅与开发人员共享，以帮助更好地了解应用程序的使用方式，各种功能的执行时间以及最受欢迎的功能。收集的数据用于改善整体性能，可用性和用户体验。这些数据是由Google[ Google的隐私政策](https://policies.google.com/privacy)提供的第三方Google Analytics for Firebase库收集的。

该应用程序内置了**自动崩溃报告**系统。应用程序故障报告仅与开发人员共享，并且包含有关崩溃原因和设备配置的信息。它不包括任何可用于识别您或设备的信息。