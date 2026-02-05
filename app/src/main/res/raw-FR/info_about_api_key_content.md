<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Le jeton d\'accès (la clé API) est une chaîne de caractères spéciale utilisée pour autoriser l\'utilisateur à uploader des mesures collectées dans la base de données OpenCellID.

La clé valide doit être composée des chiffres 0-9 et des lettres a-f. Selon la date d\'enregistrement, le format de la clé peut avoir la forme suivante :

* `pk.9743a66f914cc249efca164485a19c5c` - 32 caractères, précédés de `pk.`, 35 caractères au total
* `9743a66f914cc249efca164485a19c5c` - 32 caractères
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 caractères en groupe de 5 séparés par des tirets, donc 36 caractères au total
* `9743a66f914cc2` - 14 caractères