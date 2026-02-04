[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
액세스 토큰(API Key)은 사용자가 수집한 데이터를 OpenCellID에 업로드할 수 있는 권한을 확인할 때 사용하는 특수한 문자열입니다.

올바른 키 형식은 숫자 0~9, 문자 a~f로 구성되어야 합니다. 등록한 날짜에 다라 키 형식은 다음과 같을 수 있습니다.

* `pk.9743a66f914cc249efca164485a19c5c` - `pk.`으로 시작하는 32자리 문자열로 총 35자리 문자열
* `9743a66f914cc249efca164485a19c5c` - 32자리 문자열
* `9743a66f-914c-c249-efca-164485a19c5c` - '-'로 5개의 그룹으로 나뉘어진 32자리 문자열로, '-'를 포함하여 총 36자리 문자열
* `9743a66f914cc2` - 14자리 문자열