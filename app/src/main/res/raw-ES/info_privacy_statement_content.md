<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
La privacidad es muy importante, por lo que la aplicación recopila y transmite solo la información requerida para brindar los servicios. La aplicación NO recopila, almacena ni envía ninguna información que pueda identificarlo personalmente a usted, su dispositivo o cualquier otra información personal.

**Si no está de acuerdo con esta política de privacidad, no instale ni desinstale la aplicación. La eliminación permanente de la aplicación del dispositivo móvil equivale a cancelar el uso de la aplicación.**

### Las funciones principales

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [La política de privacidad de OpenCellID.org](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

La mayoría de las funciones de la aplicación requieren el **permiso de ubicación en primer plano**; sin embargo, para comenzar a recopilar mediciones al iniciar el dispositivo, se debe otorgar el **permiso de ubicación en segundo plano**. El permiso de ubicación en segundo plano es necesario porque, en este escenario, la recopilación la inicia el sistema (en segundo plano), no usted (en primer plano).

### Las funciones opcionales

La función **mapa** utiliza los servicios proporcionados por OpenStreetMap en [los términos de uso](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

La función de **verificación automática de actualizaciones** se conecta al servidor para verificar si hay disponible una versión más reciente de la aplicación. La solicitud contiene la versión de la aplicación instalada y se ejecuta al inicio de la aplicación una vez al día.

La función **Contactar al desarrollador** creará un nuevo mensaje de correo electrónico en el cliente de correo electrónico seleccionado. El mensaje incluye la siguiente información técnica sobre el dispositivo para ayudar en la resolución de problemas:

- La versión de la aplicación
- La versión de Android
- El fabricante y el modelo del dispositivo

La función **Exportar preferencias** o **Exportar base de datos** copiará las preferencias de la aplicación o la base de datos en la carpeta dedicada de su dispositivo. Si los archivos se colocan en la memoria compartida, a la que cualquier otra aplicación tiene acceso, entonces los datos confidenciales (como el token de acceso OpenCellID) pueden estar desprotegidos.

La función **registro de archivos en el nivel de depuración** creará archivos de registro muy detallados en la carpeta dedicada de su dispositivo. El registro puede contener información confidencial, como su ubicación actual y la torre celular a la que está conectado su dispositivo. Esta opción solo debe usarse temporalmente para solucionar problemas.

### Los ayudantes de resolución de problemas

La aplicación recopila **las estadísticas de uso anónimas**. Los datos se comparten solo con el desarrollador, para ayudar a comprender mejor cómo se utiliza la aplicación, cuáles son los tiempos de ejecución de varias funciones y cuáles son las características más populares. Los datos recopilados se utilizan para mejorar el rendimiento general, la usabilidad y la experiencia del usuario. Los datos se recopilan utilizando la biblioteca de Google Analytics para Firebase de terceros proporcionada por Google en [la política de privacidad de Google](https://policies.google.com/privacy).

La aplicación tiene incorporado el sistema de **informe automático de fallos**. Los informes de fallas de la aplicación se comparten solo con el desarrollador y contienen información sobre la causa del bloqueo y la configuración del dispositivo. NO incluye ninguna información que pueda usarse para identificarlo a usted o al dispositivo.