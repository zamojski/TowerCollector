<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector은 Tasker, Automate, MacroDroid 또는 Llama와 같은 *Intent*를 보내는 외부 앱과 연동할 수 있습니다. 아래에 정의된 모든 활동에 대해 *Broadcast* 유형을 이용합니다. 연동을 하기 위해서 해당 앱의 도움말을 참조하세요.
*앱과 마찬가지로 동시에 두 개 이상의 작업을 시작할 수 없습니다.*

### 수집 시작

설정에 정의된 대로 데이터 수집을 시작합니다. Action:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### 수집 중단

데이터 수집을 즉시 중단합니다. Action:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### 업로드 시작

설정에 정의된대로 업로드를 시작합니다. Action:

`info.zamojski.soft.towercollector.UPLOADER_START`

### 업로드 중단

업로드가 중단됩니다. Action:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### 내보내기

마지막에 설정한 설정으로 데이터 내보내기를 시작합니다. 측정값은 유지됩니다. Action:

`info.zamojski.soft.towercollector.EXPORT_START`

### 내보내기 중단

내보내기를 중단합니다. Action:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

Android Oreo 이상에서는 패키지 이름을 추가로 입력해야합니다.

`info.zamojski.soft.towercollector`