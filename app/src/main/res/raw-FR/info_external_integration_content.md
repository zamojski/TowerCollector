<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector propose des tâches qui peuvent être exécutées par d\'autres applications qui envoient des *intents* comme Tasker, Automate ou Llama. Vous devez utiliser le type *Broadcast* pour toutes les activités définies ci-dessous. Pour plus d\'information sur la façon de faire de telles intégrations, consultez l\'aide de ces applications.
*Vérifiez notamment qu\'il est possible de démarrer plus d\'une tâche à la fois !*

### Début Collector

Collector sera démarré en utilisant les paramètres définis dans les préférences. Action:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Arrêter Collector

Collector sera arrêté immédiatement. Action:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Commencer l\'envoi

Le téléversement va être démarré selon la configuration "Action :" définie dans les Préférences.

`info.zamojski.soft.towercollector.UPLOADER_START`

### Arrêter l'envoi

Le téléchargement sera bientôt annulé. Action:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Lancer l'exportation

L'exportation sera lancée en utilisant la configuration de la dernière exécution, les mesures seront conservées. Action:

`info.zamojski.soft.towercollector.EXPORT_START`

### Arrêter l'exportation

L'exportation sera bientôt annulée. Action:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

Sur Android Oreo et plus récent vous devez définir en outre le nom du paquet de Composant:

`info.zamojski.soft.towercollector`