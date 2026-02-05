<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
A privacidade é muito importante, por isso o aplicativo coleta e transmite apenas as informações necessárias para a prestação dos serviços. O aplicativo NÃO coleta, armazena ou envia quaisquer informações que possam identificá-lo pessoalmente, seu dispositivo ou qualquer outra informação pessoal.

**Se você não concorda com esta política de privacidade, não instale ou desinstale o aplicativo. Remover permanentemente o aplicativo do dispositivo móvel é equivalente a encerrar o uso do aplicativo.**

### As principais funções

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [A política de privacidade do OpenCellID.org](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

A maioria dos recursos do aplicativo requer a **permissão de localização em primeiro plano**, no entanto, para iniciar a coleta de medições na inicialização do dispositivo, a **permissão de localização em segundo plano** deve ser concedida. A permissão de localização em segundo plano é necessária porque, neste cenário, a coleta é iniciada pelo sistema (em segundo plano), não por você (em primeiro plano).

### As funções opcionais

O recurso **mapa** usa os serviços fornecidos pelo OpenStreetMap de acordo com [os termos de uso](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

O recurso **verificação automática de atualização** se conecta ao servidor para verificar se há uma versão mais recente do aplicativo disponível. A solicitação contém a versão do aplicativo instalado e é executada no início do aplicativo uma vez por dia.

O recurso **Contactar o desenvolvedor** criará uma nova mensagem de e-mail no cliente de e-mail selecionado. A mensagem inclui as seguintes informações técnicas sobre o dispositivo para ajudar na solução de problemas:

- A versão do aplicativo
- A versão Android
- O fabricante e o modelo do dispositivo

O recurso **Exportar preferências** ou **Exportar banco de dados** irá copiar as preferências do aplicativo ou banco de dados para a pasta dedicada em seu dispositivo. Se os arquivos forem colocados na memória compartilhada, à qual qualquer outro aplicativo tem acesso, os dados confidenciais (como o token de acesso OpenCellID) podem estar desprotegidos.

O recurso **arquivo de registro no nível de depuração** criará arquivos de registro muito detalhados na pasta dedicada em seu dispositivo. O registro pode conter informações confidenciais, como sua localização atual e a torre de celular à qual o dispositivo está conectado. Esta opção deve ser usada apenas temporariamente para solução de problemas.

### Os ajudantes de solução de problemas

O aplicativo coleta **as estatísticas de uso anônimas**. Os dados são compartilhados apenas com o desenvolvedor, a fim de ajudar a entender melhor como o aplicativo está sendo utilizado, quais são os tempos de execução das diversas funções e quais são os recursos mais populares. Os dados coletados são usados ​​para melhorar o desempenho geral, a usabilidade e a experiência do usuário. Os dados são coletados usando a biblioteca de terceiros do Google Analytics para Firebase fornecida pelo Google de acordo com [a política de privacidade do Google](https://policies.google.com/privacy).

O aplicativo possui o sistema integrado de **relatórios automatizados de falhas**. Os relatórios de falha do aplicativo são compartilhados apenas com o desenvolvedor e contêm informações sobre a causa da falha e a configuração do dispositivo. NÃO inclui nenhuma informação que possa ser usada para identificar você ou o dispositivo.