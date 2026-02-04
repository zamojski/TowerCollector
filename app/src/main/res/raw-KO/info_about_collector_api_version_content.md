[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
데이터를 수집 할 때 기지국 세부 정보를 얻는 데 사용할 방법을 결정할 수 있습니다.

*더 신뢰할 수 있는 데이터를 얻으려면 사용 가능한 최신 방법을 사용하는 것이 좋습니다. Android 4.1 이하를 실행하는 기기에서는 선택이 비활성화 됩니다.*

*   **자동 감지** - 앱이 사용할 수 있는 최상의 API 버전을 자동으로 감지합니다. 신호가 없을 때 수집이 시작되면 안전 옵션을 사용합니다.
* **Android 4.2 API** - Android 4.2 이상에서 사용 가능한 방법을 이용합니다. 이 방법은 가장 신뢰할 수 있는 기지국 정보를 얻을 수 있습니다. 그러나 일부 제조업체는 새 API를 올바르게 구현하지 않았으며 이러한 장치에서는 사용할 수 없을 수 있습니다. Android 5.1 이상에서 여러 SIM 카드의 기지국이 반환될 수 있습니다.
* **Android 1.0 API** - Android의 첫 버전부터 사용 가능한 방법입니다. 해당 방법은 LTE/UMTS 네트워크에 대해 신뢰할 수 없는 데이터가 반환될 수 있습니다. 장치에 따라 이 방법을 사용하려면 올바른 셀 정보를 얻기 위해 수집하는 동안 화면이 켜져 있어야 할 수 있습니다.