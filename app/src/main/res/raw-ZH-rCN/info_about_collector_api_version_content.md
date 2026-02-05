<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
您可以决定在收集测量结果时使用何种方法获取蜂窝塔的详细信息。

*强烈建议使用最新的可用方法来获取更可靠的数据。 在运行Android 4.1及更早版本的设备上不可选择。*

*   **自动检测** - 此应用将自动检测可以使用的API版本，并选择最佳选项。如果收集器在没有信号时启动，它将使用安全选项。
* **Android 4.2 API**  - 使用Android 4.2及更高版本上提供的方法。 这些方法提供最可靠的信号塔信息。 但是，某些制造商没有正确实施新API，也可能无法在此类设备上使用。 在Android 5.1上，可能会返回来自多张SIM卡的较新的手机信号塔信息。
* **Android 1.0 API**  - 使用自第一版Android以来可用的一组方法。这些方法可能返回LTE和UMTS网络的不可靠数据。根据设备的不同，此方法可能要求屏幕在收集时保持打开以保持正确更新基站信息。