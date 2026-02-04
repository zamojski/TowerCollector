[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
The privacy is very important, so the application collect and transmit only the required information to provide the services. The application does NOT collect, store or send any information that could personally identify you, your device or any other personal information.

**If you do not agree to this privacy policy, please do not install or uninstall the application. Permanently removing the application from the mobile device is equivalent to terminating the use of the application.**

### The main functions

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [The OpenCellID.org privacy policy](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

Most of the features of the application require the **foreground location permission**, however, to start collecting measurements at device startup, the **background location permission** must be granted. The background location permission is required because, in this scenario, collecting is initiated by the system (in background), not by you (in foreground).

### The optional functions

The **map** feature uses the services provided by OpenStreetMap under [the terms of use](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

The **automatic update check** feature connects to the server to check if there is available a newer version of the application. The request contains the version of installed application and it is executed on the start of the application once a day.

The **Contact developer** feature will create a new email message in the selected email client. The message includes the following technical information about the device in order to help in troubleshooting:

- The application version
- The Android version
- The device manufacturer and model

The **Export preferences** or **Export database** feature will copy the application preferences or database to the dedicated folder on your device. If the files are placed on the shared memory, that any other application has access to, then the sensitive data (like the OpenCellID Access Token) may be unprotected.

The **file logging at Debug level** feature will create very detailed log files in the dedicated folder on your device. The log may contain the sensitive information, like your current location and the cell tower your device is connected to. This option should only be used temporarily for troubleshooting.

### The troubleshooting helpers

The application collects **the anonymous usage statistics**. The data is shared only with the developer, in order to help to better understand how application is being used, what are the execution times of various functions and what are the most popular features. The collected data is used to improve overall performance, usability and user experience. The data is collected using third party Google Analytics for Firebase library provided by Google under [the Google's privacy policy](https://policies.google.com/privacy).

The application has built-in the **automated crash reporting** system. The application failure reports are shared only with the developer and contain information about crash cause and device configuration. It does NOT include any information that can use used to identify you or the device.