<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
La privacidad es muy importante, por lo que la aplicación recopila y transmite
solo la información requerida para brindar los servicios. La aplicación NO
recopila, almacena ni envía ninguna información que pueda identificarlo
personalmente a usted, su dispositivo o cualquier otra información personal.

**Si no está de acuerdo con esta política de privacidad, no instale ni
desinstale la aplicación. La eliminación permanente de la aplicación del
dispositivo móvil equivale a cancelar el uso de la aplicación.**

### Las funciones principales

La función principal de la aplicación es recopilar identificadores de torres de
telefonía celular, intensidad de la señal y la ubicación del dispositivo
(coordenadas). Esta información se almacena localmente en su dispositivo y nunca
se transmitirá sin su consentimiento explícito. Los datos recopilados se pueden
exportar a un archivo en su dispositivo o compartir con los proyectos
OpenCellID.org y BeaconDB. Para garantizar la protección de la privacidad, la
aplicación NO utiliza ningún servidor proxy que pueda acceder a sus datos
durante la transmisión. Todo el tráfico a servicios externos se cifra mediante
el protocolo HTTPS.

**Para contribuir al proyecto OpenCellID.org** se requiere un token de acceso
personal (clave API) que debe ingresarse en las preferencias de la aplicación.
Esta clave se utilizará únicamente para cargar las mediciones a la base de datos
del proyecto y nunca saldrá del dispositivo. Quien conozca el token de acceso
tendrá control total sobre los datos cargados, incluyendo la posibilidad de
consultarlos y eliminarlos. ¡Es su responsabilidad proteger la confidencialidad
de su token de acceso!

- [La política de privacidad de
  OpenCellID.org](https://community.opencellid.org/privacy)
- [Aviso de privacidad de BeaconDB](https://beacondb.net/privacy/)

La mayoría de las funciones de la aplicación requieren el **permiso de ubicación
en primer plano**; sin embargo, para comenzar a recopilar mediciones al iniciar
el dispositivo, se debe otorgar el **permiso de ubicación en segundo plano**. El
permiso de ubicación en segundo plano es necesario porque, en este escenario, la
recopilación la inicia el sistema (en segundo plano), no usted (en primer
plano).

### Las funciones opcionales

La función **mapa** utiliza los servicios proporcionados por OpenStreetMap en
[los términos de uso](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

La función de **verificación automática de actualizaciones** se conecta al
servidor para verificar si hay disponible una versión más reciente de la
aplicación. La solicitud contiene la versión de la aplicación instalada y se
ejecuta al inicio de la aplicación una vez al día.

La función **Contactar al desarrollador** creará un nuevo mensaje de correo
electrónico en el cliente de correo electrónico seleccionado. El mensaje incluye
la siguiente información técnica sobre el dispositivo para ayudar en la
resolución de problemas:

- La versión de la aplicación
- La versión de Android
- El fabricante y el modelo del dispositivo

La función **Exportar preferencias** o **Exportar base de datos** copiará las
preferencias de la aplicación o la base de datos en la carpeta dedicada de su
dispositivo. Si los archivos se colocan en la memoria compartida, a la que
cualquier otra aplicación tiene acceso, entonces los datos confidenciales (como
el token de acceso OpenCellID) pueden estar desprotegidos.

La función **registro de archivos en el nivel de depuración** creará archivos de
registro muy detallados en la carpeta dedicada de su dispositivo. El registro
puede contener información confidencial, como su ubicación actual y la torre
celular a la que está conectado su dispositivo. Esta opción solo debe usarse
temporalmente para solucionar problemas.

### Los ayudantes de resolución de problemas

La aplicación recopila **las estadísticas de uso anónimas**. Los datos se
comparten solo con el desarrollador, para ayudar a comprender mejor cómo se
utiliza la aplicación, cuáles son los tiempos de ejecución de varias funciones y
cuáles son las características más populares. Los datos recopilados se utilizan
para mejorar el rendimiento general, la usabilidad y la experiencia del usuario.
Los datos se recopilan utilizando la biblioteca de Google Analytics para
Firebase de terceros proporcionada por Google en [la política de privacidad de
Google](https://policies.google.com/privacy).

La aplicación tiene incorporado el sistema de **informe automático de fallos**.
Los informes de fallas de la aplicación se comparten solo con el desarrollador y
contienen información sobre la causa del bloqueo y la configuración del
dispositivo. NO incluye ninguna información que pueda usarse para identificarlo
a usted o al dispositivo.
