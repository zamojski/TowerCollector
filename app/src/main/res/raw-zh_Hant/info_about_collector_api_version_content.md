<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
在蒐集量測資料時，你可以自行決定要使用哪一種方式來取得基地台的詳細資訊。

強烈建議使用目前可用的最新版本，以取得更可靠的資料。在 Android 4.1（含）以下版本的裝置上，此選項將無法使用。

* **Auto detect** - The app will automatically detect which version of API can
  be used and select the best option. It will use safe option if Collector
  started when there is no signal.
* **Android 4.2 API** - Uses methods available on Android 4.2 and newer. These
  methods provide the most reliable cell info. However some manufacturers did
  not implement new API correctly and it may not be used on such devices. On
  Android 5.1 and newer cell towers from multiple SIM cards may be returned.
* **Android 1.0 API** - Uses a set of methods available since first version of
  Android. These methods may return unreliable data for LTE and UMTS networks.
  Depending on the device, this method may require the screen to remain on while
  collecting to keep cell info updated correctly.
