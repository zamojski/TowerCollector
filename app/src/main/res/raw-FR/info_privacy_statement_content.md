<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
La confidentialité est très importante, de sorte que l\'application ne collecte et ne transmet que les informations nécessaires pour fournir les services. L\'application ne collecte, ne stocke ni n\'envoie aucune information qui pourrait vous identifier personnellement, votre appareil ou toute autre information personnelle.

**Si vous n\'acceptez pas cette politique de confidentialité, veuillez ne pas installer ni désinstaller l\'application. Supprimer définitivement l\'application de l\'appareil mobile équivaut à mettre fin à l\'utilisation de l\'application.**

### Les principales fonctions

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [La politique de confidentialité d\'OpenCellID.org](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

La plupart des fonctionnalités de l\'application nécessitent l \'**autorisation de localisation au premier plan**, cependant, pour commencer à collecter les mesures au démarrage de l\'appareil, l \' **autorisation de localisation en arrière-plan** doit être accordée. L\'autorisation d\'emplacement en arrière-plan est requise car, dans ce scénario, la collecte est initiée par le système (en arrière-plan) et non par vous (au premier plan).

### Les fonctions optionnelles

La fonctionnalité **carte** utilise les services fournis par OpenStreetMap sous [les conditions d\'utilisation](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

La fonctionnalité de **vérification automatique des mises à jour** se connecte au serveur pour vérifier si une version plus récente de l'application est disponible. La requête contient la version de l'application installée et elle est exécutée au démarrage de l'application une fois par jour.

La fonctionnalité **Contacter le développeur** créera un nouvel e-mail dans le client de messagerie sélectionné. Le message comprend les informations techniques suivantes sur l'appareil afin d'aider au dépannage:

- La version de l'application
- La version Android
- Le fabricant et le modèle de l'appareil

La fonction **Exporter les préférences** ou **Exporter la base de données** copiera les préférences de l'application ou la base de données dans le dossier dédié de votre appareil. Si les fichiers sont placés sur la mémoire partagée, à laquelle toute autre application a accès, les données sensibles (comme le jeton d'accès OpenCellID) peuvent ne pas être protégées.

La fonction **journalisation des fichiers au niveau du débogage** créera des fichiers journaux très détaillés dans le dossier dédié de votre appareil. Le journal peut contenir des informations sensibles, telles que votre emplacement actuel et la tour cellulaire à laquelle votre appareil est connecté. Cette option ne doit être utilisée que temporairement pour le dépannage.

### Les aides au dépannage

L'application collecte **les statistiques d'utilisation anonymes**. Les données sont partagées uniquement avec le développeur, afin d'aider à mieux comprendre comment l'application est utilisée, quels sont les temps d'exécution des différentes fonctions et quelles sont les fonctionnalités les plus populaires. Les données collectées sont utilisées pour améliorer les performances globales, la convivialité et l'expérience utilisateur. Les données sont collectées à l'aide de la bibliothèque tierce Google Analytics pour Firebase fournie par Google sous [la politique de confidentialité de Google](https://policies.google.com/privacy).

L'application intègre le système de **rapport automatisé des plantages**. Les rapports d'échec de l'application sont partagés uniquement avec le développeur et contiennent des informations sur la cause de l'incident et la configuration de l'appareil. Il n'inclut aucune information pouvant être utilisée pour vous identifier ou identifier l'appareil.