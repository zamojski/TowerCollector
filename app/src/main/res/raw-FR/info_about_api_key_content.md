<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Le jeton d'accès (clé API) est une chaîne de caractères spéciale utilisée pour
autoriser l'utilisateur à télécharger les mesures collectées dans la base de
données OpenCellID.

La clé correcte doit comporter des chiffres de 0 à 9 et des lettres de a à f.
Selon la date d'enregistrement, le format de la clé peut ressembler à ceci :

* `pk.9743a66f914cc249efca164485a19c5c` - 32 caractères, précédé de `pk.`, total
  35 caractères
* `9743a66f914cc249efca164485a19c5c` - 32 caractères
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 caractères répartis en cinq
  groupes séparés par un tiret, soit 36 caractères au total
* `9743a66f914cc2` - 14 caractères
