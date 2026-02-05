<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
El token de acceso (clave API) es una cadena especial que se utiliza para autorizar al usuario a cargar las mediciones recogidas en la base de datos OpenCellID.

Una llave correcta consiste en dígitos del 0-9 y letras de la a-f. Dependiendo de la fecha de registro el formato de la llave podría lucir así:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 caracteres, precedidos de `pk.`, total 35 caracteres
* `9743a66f914cc249efca164485a19c5c` - 32 caracteres
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 caracteres en 5 grupos separados por un guión, 36 caracteres en total
* `9743a66f914cc2` - 14 caracteres