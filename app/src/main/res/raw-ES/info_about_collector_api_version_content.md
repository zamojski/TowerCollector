[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Puede decidir cual método se utilizará para obtener los detalles de la celda cuando se recolecten mediciones.

*Es sumamente recomendable utilizar la más reciente para obtener datos confiables. Esta selección está deshabilitada en dispositivos que corran Android 4.1 o anteriores.*

* **Auto detección** - La aplicación detectará automáticamente qué versión de la API puede ser utilizada y elegirá la mejor opción. Usará la versión más segura si no hay señal cuando se inicia la aplicación.
* **API 4.2 de Android** - Utiliza métodos disponibles en Android 4.2 y posteriores. Estos métodos proveen la más confiable información de celdas. Sin embargo, algunos fabricantes no implementaron correctamente la nueva API y podria no funcionar en algunos dispositivos. En Android 5.1 y posterior pueden ser obtenidas las celdas celulares de multiples tarjetas SIM.
* **API 1.0 de Android** - Utiliza un conjunto de funciones disponibles desde la primera versión de Android. Estas funciones pueden devolver datos erróneos en redes LTE y UMTS. Dependiendo del dispositivo se puede requerir mantener la pantalla encendida mientras se recolecta la información para mantener la información de celda correctamente actualizada.