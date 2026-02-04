[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
개인 정보는 매우 중요하므로 앱은 서비스 제공에 필요한 정보만 수집하고 전송합니다. 앱은 귀하, 귀하의 장치 또는 기타 개인 정보를 식별할 수 있는 데이터를 수집, 저장, 전송하지 않습니다.

**만약 개인 정보 보호 정책에 동의하지 않는 경우 앱을 설치하지 말고 삭제하십시오. 장치에서 앱을 영구적으로 제거하는 것은 해당 앱의 사용을 종료하는 것과 같습니다.**

### 주요 기능

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [OpenCellID.org 개인 정보 보호 정책](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

앱의 대부분의 기능에는 **포그라운드 위치 권한**이 필요하지만 기기 시작 시 데이터 수집 기능을 이용하려면 **백그라운드 위치 권한**이 필요합니다. 이 시나리오에서 데이터 수집은 사용자(포그라운드)가 아니라 시스템(백그라운드)에서 시작되기 때문에 백그라운드 위치 권한이 필요합니다.

### 옵션 기능

**지도** 기능은 [이용 약관](https://wiki.osmfoundation.org/wiki/Terms_of_Use)에 따라 OpenStreetMap에서 제공하는 서비스를 사용합니다.

**자동 업데이트 확인** 기능은 서버에 연결하여 사용 가능한 최신 버전의 앱이 있는지 확인합니다. 요청에는 설치된 앱 버전이 포함되어 있으며 하루에 한 번 앱 시작시 확인합니다.

**개발자에게 문의** 기능은 선택한 이메일 클라이언트에서 새 이메일 메시지를 생성합니다. 메시지에는 문제 해결에 도움이 되도록 장치에 대한 다음 기술 정보가 포함되어 있습니다.

- 앱 버전
- Android 버전
- 장치 제조사와 모델

**설정 내보내기** 또는 **데이터베이스 내보내기** 기능은 앱 환경설정 또는 데이터베이스를 기기의 특정 폴더에 복사합니다. 다른 앱이 액세스할 수 있는 공유 저장소에 파일이 있는 경우 민감한 데이터(예: OpenCellID 액세스 토큰)가 보호되지 않을 수 있습니다.

**Debug 수준의 파일 로깅** 기능은 기기의 전용 폴더에 매우 상세한 로그 파일을 생성합니다. 로그에는 현재 위치 및 장치가 연결된 기지국과 같은 민감한 정보가 포함될 수 있습니다. 이 옵션은 문제 해결을 위해 일시적으로만 사용해야 합니다.

### 문제 해결 도우미

앱은 **익명의 사용 통계**를 수집합니다. 앱이 어떻게 사용되는지, 다양한 기능의 실행 시간과 가장 인기 있는 기능을 더 잘 이해할 수 있도록 데이터는 개발자에게만 공유됩니다. 수집된 데이터는 전반적인 성능, 사용성 및 사용자 경험을 개선하는 데 사용됩니다. 데이터는 [Google의 개인정보취급방침](https://policies.google.com/privacy)에 따라 Google에서 제공하는 Firebase용 Google 애널리틱스 라이브러리를 사용하여 수집됩니다.

앱에는 **자동 충돌 보고** 시스템이 내장되어 있습니다. 앱 오류 보고서는 개발자와만 공유되며 충돌 원인 및 장치 구성에 대한 정보를 포함합니다. 귀하 또는 장치를 식별하는 데 사용할 수 있는 정보는 포함되지 않습니다.