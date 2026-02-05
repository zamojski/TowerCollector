<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Você pode decidir qual método será utilizado para obter detalhes das torres celulares quando coletando medições.

*É altamente recomendado que se use a última versão disponível para obter dados mais confiáveis. A seleção é desabilitada em dispositivos rodando Android 4.1 e anteriores.*

* **Autodetecção** - O aplicativo irá detectar automaticamente qual versão da API pode ser usada e selecionar a melhor opção. Ele usará a opção mais segura se o Coletor iniciar quando não há sinal.
* **API Android 4.2** - Usa métodos disponíveis no Android 4.2 e posteriores. Esses métodos proveem as informações de céulas mais confiáveis. No entanto alguns fabricantes não implementam a nova API corretamente e ela pode não ser utilizada nesses dispositivos. No Android 5.1 e posteriores as torres celulares de múltiplos SIM cards podem ser retornadas.
* **API Android 1.0** - Usa um conjunto de métodos disponíveis desde a primeira versão do Android. Esses métodos podem retornar dados não confiáveis para redes LTE e UMTS. Dependendo do dispositivo, este método pode exigir que a tela seja mantida ligada enquanto se está coletando para manter as informações das células atualizadas corretamente.