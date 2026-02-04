[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Klucz dostępu (klucz API) to specjalny ciąg znaków używany do autoryzacji użytkownika w celu przesłania próbek do bazy OpenCellID

Poprawny klucz musi składać się z cyfr 0-9 i liter a-f. W zależności od daty rejestracji format klucza może wyglądać następująco:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 znaki poprzedzone `pk.`, łącznie 35 znaków
* `9743a66f914cc249efca164485a19c5c` - 32 znaki
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 znaki w pięciu grupach oddzielonych myślnikiem, łącznie 36 znaków
* `9743a66f914cc2` - 14 znaków