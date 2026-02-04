[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Vous pouvez décider de la méthode qui sera utilisée pour obtenir les données sur les antennes relais lors de la collecte des mesures.

*Il est fortement recommandé d\'utiliser la dernière version disponible pour obtenir des données les plus fiables. La sélection est désactivée sur les appareils sous Android 4.1 et antérieurs.*

*   **Détection automatique** - L\'application détectera automatiquement quelle version de l\'API peut être utilisée et sélectionnera la meilleure option. Elle utilisera l\'option "sécurité" si le Collecteur a démarré alors qu\'il n\'y avait pas de signal.
* **Android 4.2 API** - Utilise des méthodes disponibles sur Android 4.2 et plus récent. Ces méthodes fournissent les informations de cellule les plus fiables. Cependant, certains fabricants n'ont pas implémenté correctement la nouvelle API et elle peut ne pas être utilisée sur de tels appareils. Sur Android 5.1 et plus récents tours cellulaires de plusieurs cartes SIM peuvent être retournés.
* **Android 1.0 API** - Utilise un ensemble de méthodes disponibles depuis la première version d'Android. Ces méthodes peuvent produire des données non fiables pour les réseaux LTE et UMTS. Selon l'appareil, cette méthode peut nécessiter que l'écran reste allumé pendant la collecte pour que les informations sur les cellules soient correctement mises à jour.