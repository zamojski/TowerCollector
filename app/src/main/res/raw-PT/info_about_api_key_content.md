<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
O token de acesso (chave da API) é uma sequência especial usada para autorizar o
usuário a enviar as medições coletadas para o banco de dados OpenCellID.

A chave correta deve ser composta pelos dígitos 0 a 9 e pelas letras a a f.
Dependendo da data de registro, o formato da chave pode ser semelhante a este:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 caracteres, precedido por `pk.`,
  totalizando 35 caracteres
* `9743a66f914cc249efca164485a19c5c` - 32 caracteres
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 caracteres em cinco grupos
  separados por hífen, totalizando 36 caracteres
* `9743a66f914cc2` - 14 caracteres
