<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
O Tower Collector expõe tarefas a serem executadas por outras aplicações que enviam *intents* como o Tasker, o Automate ou o Llama. Você precisa usar o tipo *Broadcast* para todas as atividades definidas abaixo. Para mais informações de como fazer essa integração verifique a ajuda dessas aplicações.
*Como dentro do app você não pode iniciar mais de uma tarefa ao mesmo tempo!*

### Iniciar o Coletor

O Coletor será inicializado usando as configurações definidas nas Preferências. Action:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Parar o Coletor

O Coletor será parado imediatamente. Action:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Iniciar o upload

O upload será iniciado usando a configuração das Preferências. Action:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Parar o upload

Upload será cancelado em breve. Ação:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Comece a exportar

A exportação será iniciada usando a configuração da última execução, as medições serão mantidas. Açao:

`info.zamojski.soft.towercollector.EXPORT_START`

### Parar de exportar

A exportação será cancelada em breve. Açao:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

No Android Oreo e mais recente, você precisa definir adicionalmente o nome do pacote do componente:

`info.zamojski.soft.towercollector`