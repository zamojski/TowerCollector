<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
El token de acceso (clave API) es una cadena especial que se utiliza para
autorizar al usuario a cargar las mediciones recopiladas en la base de datos de
OpenCellID.

La clave correcta debe constar de dígitos del 0 al 9 y letras de la a a la f.
Dependiendo de la fecha de registro, el formato de la clave puede ser similar a
este:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 caracteres, precedidos por `pk.`,
  total 35 caracteres
* `9743a66f914cc249efca164485a19c5c` - 32 caracteres
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 caracteres en cinco grupos
  separados por un guion, total 36 caracteres
* `9743a66f914cc2` - 14 caracteres
