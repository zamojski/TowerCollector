<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
您可以決定在收集測量結果時使用何種方法來取得蜂窩塔的詳細資訊。

*強烈建議使用最新的可用方法來獲取更可靠的數據。 在運行Android 4.1及更早版本的裝置上不可選擇。 *

* **自動偵測** - 此應用程式將自動偵測可使用的API版本，並選擇最佳選項。如果收集器在沒有訊號時啟動，它將使用安全選項。
* **Android 4.2 API** - 使用Android 4.2及更高版本上提供的方法。 這些方法提供最可靠的信號塔資訊。
  但是，某些製造商沒有正確實作新API，也可能無法在此類裝置上使用。 在Android 5.1上，可能會傳回來自多張SIM卡的較新的手機訊號塔資訊。
* **Android 1.0 API** -
  使用自第一版Android以來可用的一組方法。這些方法可能傳回LTE和UMTS網路的不可靠資料。根據裝置的不同，此方法可能要求螢幕在收集時保持開啟以保持正確更新基地台資訊。
