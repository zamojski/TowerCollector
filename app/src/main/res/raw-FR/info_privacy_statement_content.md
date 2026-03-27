<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
La confidentialité est très importante, de sorte que l\'application ne collecte
et ne transmet que les informations nécessaires pour fournir les services.
L\'application ne collecte, ne stocke ni n\'envoie aucune information qui
pourrait vous identifier personnellement, votre appareil ou toute autre
information personnelle.

**Si vous n\'acceptez pas cette politique de confidentialité, veuillez ne pas
installer ni désinstaller l\'application. Supprimer définitivement
l\'application de l\'appareil mobile équivaut à mettre fin à l\'utilisation de
l\'application.**

### Les principales fonctions

La principale fonctionnalité de l'application est de collecter les identifiants
des antennes-relais, la puissance du signal et la géolocalisation de votre
appareil (coordonnées). Ces informations sont stockées localement sur votre
appareil et ne seront jamais transmises sans votre consentement explicite. Les
données collectées peuvent être exportées vers un fichier enregistré sur votre
appareil ou partagées avec les projets OpenCellID.org et BeaconDB. Afin de
garantir la protection de votre vie privée, l'application n'utilise aucun
serveur proxy susceptible d'accéder à vos données lors de leur transmission.
L'ensemble du trafic vers les services externes est chiffré à l'aide du
protocole HTTPS.

**La contribution au projet OpenCellID.org** requiert un jeton d'accès personnel
(clé API) à saisir dans les préférences de l'application. Cette clé servira
uniquement à télécharger les mesures dans la base de données du projet et ne
quittera jamais l'appareil. Toute personne connaissant le jeton d'accès dispose
d'un contrôle total sur les données téléchargées, y compris la possibilité de
les consulter et de les supprimer. Il est de votre responsabilité de protéger la
confidentialité de votre jeton d'accès !

- [La politique de confidentialité
  d\'OpenCellID.org](https://community.opencellid.org/privacy)
- [Avis de confidentialité de BeaconDB](https://beacondb.net/privacy/)

La plupart des fonctionnalités de l\'application nécessitent l \'**autorisation
de localisation au premier plan**, cependant, pour commencer à collecter les
mesures au démarrage de l\'appareil, l \' **autorisation de localisation en
arrière-plan** doit être accordée. L\'autorisation d\'emplacement en
arrière-plan est requise car, dans ce scénario, la collecte est initiée par le
système (en arrière-plan) et non par vous (au premier plan).

### Les fonctions optionnelles

La fonctionnalité **carte** utilise les services fournis par OpenStreetMap sous
[les conditions
d\'utilisation](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

La fonctionnalité de **vérification automatique des mises à jour** se connecte
au serveur pour vérifier si une version plus récente de l'application est
disponible. La requête contient la version de l'application installée et elle
est exécutée au démarrage de l'application une fois par jour.

La fonctionnalité **Contacter le développeur** créera un nouvel e-mail dans le
client de messagerie sélectionné. Le message comprend les informations
techniques suivantes sur l'appareil afin d'aider au dépannage :

- La version de l'application
- La version Android
- Le fabricant et le modèle de l'appareil

La fonction **Exporter les préférences** ou **Exporter la base de données**
copiera les préférences de l'application ou la base de données dans le dossier
dédié de votre appareil. Si les fichiers sont placés sur la mémoire partagée, à
laquelle toute autre application a accès, les données sensibles (comme le jeton
d'accès OpenCellID) peuvent ne pas être protégées.

La fonction **journalisation des fichiers au niveau du débogage** créera des
fichiers journaux très détaillés dans le dossier dédié de votre appareil. Le
journal peut contenir des informations sensibles, telles que votre emplacement
actuel et la tour cellulaire à laquelle votre appareil est connecté. Cette
option ne doit être utilisée que temporairement pour le dépannage.

### Les aides au dépannage

L'application collecte **les statistiques d'utilisation anonymes**. Les données
sont partagées uniquement avec le développeur, afin d'aider à mieux comprendre
comment l'application est utilisée, quels sont les temps d'exécution des
différentes fonctions et quelles sont les fonctionnalités les plus populaires.
Les données collectées sont utilisées pour améliorer les performances globales,
la convivialité et l'expérience utilisateur. Les données sont collectées à
l'aide de la bibliothèque tierce Google Analytics pour Firebase fournie par
Google sous [la politique de confidentialité de
Google](https://policies.google.com/privacy).

L'application intègre le système de **rapport automatisé des plantages**. Les
rapports d'échec de l'application sont partagés uniquement avec le développeur
et contiennent des informations sur la cause de l'incident et la configuration
de l'appareil. Il n'inclut aucune information pouvant être utilisée pour vous
identifier ou identifier l'appareil.
