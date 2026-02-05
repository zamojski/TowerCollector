<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector expone las tareas que deben ejecutar otras aplicaciones que envían *intents* como Tasker, Automate o Llama. Debe usar el tipo *Broadcast* para todas las actividades definidas a continuación. Para obtener más información sobre cómo realizar dicha comprobación de integración las aplicaciones ayudan.
*Al igual que dentro de la aplicación, ¡no puedes iniciar más de una tarea al mismo tiempo!*

### Iniciar Collector

Collector iniciará usando la configuración definida en Preferencias. Acción:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Detener Collector

Collector sera detenido inmediatamente. Acción:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Iniciar carga

La carga se iniciará utilizando la configuración de Preferencias. Acción:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Detener carga

La subida se cancelará pronto. Acción:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Iniciar exportación

La exportación se iniciará utilizando la configuración de la última ejecución, las mediciones se mantendrán. Acción:

`info.zamojski.soft.towercollector.EXPORT_START`

### Detener exportación

La exportación se cancelará pronto. Acción:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

En Android Oreo y versiones posteriores, debe definir adicionalmente el nombre del paquete del componente:

`info.zamojski.soft.towercollector`