<h1 align="center">🚗 RehabCar</h1>

<p align="center">
  <strong>Jogo sério de apoio à reabilitação ativa do tornozelo com ESP32 e libGDX</strong>
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-libGDX%201.14.2-orange">
  <img alt="Hardware" src="https://img.shields.io/badge/Hardware-ESP32%20%2B%20MPU6050-blue">
  <img alt="Conexão" src="https://img.shields.io/badge/Conex%C3%A3o-USB%20%7C%20Wi--Fi-green">
</p>

<p align="center">
  👥 <strong>Autores:</strong> Guilherme Miyamoto Bragatto e Eduardo Kenzo Okada
</p>

---

## 📑 Sumário

1. [Resumo](#-resumo)
2. [Descrição do problema e contexto da reabilitação](#-1-descrição-do-problema-e-contexto-da-reabilitação)
3. [Justificativa](#-2-justificativa)
4. [Descrição do jogo](#-3-descrição-do-jogo)
   - [3.1 Visão geral](#-31-visão-geral)
   - [3.2 Integração com o dispositivo IoT](#-32-integração-com-o-dispositivo-iot)
   - [3.3 Identificação do pé e calibração](#-33-identificação-do-pé-e-calibração)
   - [3.4 Fluxo de telas](#️-34-fluxo-de-telas)
   - [3.5 Mecânicas e regras](#️-35-mecânicas-e-regras)
   - [3.6 Fases e dificuldade progressiva](#-36-fases-e-dificuldade-progressiva)
   - [3.7 Pontuação e métricas](#-37-pontuação-e-métricas)
   - [3.8 Como jogar](#️-38-como-jogar)
   - [3.9 Tecnologias e organização técnica](#️-39-tecnologias-e-organização-técnica)
5. [Protocolos proprioceptivos e biofeedback](#-4-protocolos-proprioceptivos-e-biofeedback)
6. [Limitações e finalidade clínica](#-5-limitações-e-finalidade-clínica)
7. [Referências e fontes consultadas](#-6-referências-e-fontes-consultadas)

---

## 📝 Resumo

O **RehabCar** é um jogo sério desenvolvido em **Java**, com a biblioteca **libGDX**, como ferramenta complementar para exercícios ativos de mobilidade do pé no contexto da reabilitação do tornozelo. O jogador conduz um carro em uma pista de três vias utilizando movimentos laterais do pé, associados à inversão e à eversão. Esses movimentos são captados por um dispositivo IoT composto por um **ESP32** e um sensor inercial **MPU6050**.

O sistema recebe os dados de inclinação estimada pelo sensor, interpreta os movimentos e os transforma em comandos para o jogo. A aplicação oferece feedback visual durante a atividade e registra indicadores de desempenho, como moedas coletadas, colisões, fases alcançadas e maiores inclinações estimadas para cada direção.

O protótipo conta com calibração do sensor, três fases de dificuldade, pausa, tela de resultados e comunicação com o ESP32 por cabo USB ou Wi-Fi. Um único sensor pode ser utilizado alternadamente nos dois pés, desde que seja reposicionado e calibrado antes de cada utilização.

---

## 🩺 1. Descrição do problema e contexto da reabilitação

A reabilitação de entorses de tornozelo envolve diferentes etapas, que podem incluir controle dos sintomas, recuperação da mobilidade e restabelecimento do controle neuromuscular. Após a fase aguda e eventual período de imobilização, o paciente pode iniciar exercícios ativos de mobilidade quando houver liberação e orientação do fisioterapeuta.

Entre os movimentos trabalhados nessa etapa estão:

- **Inversão:** movimento em que a planta do pé se orienta medialmente, em direção à linha média do corpo.
- **Eversão:** movimento em que a planta do pé se orienta lateralmente, afastando-se da linha média do corpo.

Esses movimentos estão associados principalmente à articulação subtalar e ao complexo do retropé, embora sejam frequentemente abordados no contexto da reabilitação do tornozelo.

O RehabCar foi desenvolvido como uma ferramenta complementar para exercícios ativos de inversão e eversão. Um sensor MPU6050, conectado a um ESP32 e fixado ao pé, capta alterações de inclinação. O jogo utiliza essas informações para controlar o deslocamento de um carro em uma pista virtual, oferecendo feedback visual durante a execução dos movimentos.

O dispositivo também pode ser utilizado no membro não acometido. Nesse caso, o paciente realiza os movimentos com o pé contralateral, permitindo registrar uma referência funcional individual para posterior comparação com os registros do membro em recuperação. A utilização é alternada: o mesmo sensor é transferido de um pé para o outro, com novo posicionamento e calibração.

O objetivo é tornar a execução dos exercícios mais interativa e fornecer indicadores que possam auxiliar o acompanhamento profissional. A adequação dos movimentos, a amplitude permitida e a progressão do exercício devem ser determinadas pelo fisioterapeuta.

---

## 🎯 2. Justificativa

A gamificação pode tornar a realização de exercícios repetitivos mais interativa, além de oferecer informações visuais sobre o desempenho durante a atividade.

| | Elemento | Como contribui para o projeto |
|---|---|---|
| 🎯 | **Objetivo claro e imediato** | Coletar moedas e desviar de cones dá uma finalidade às mudanças de direção realizadas pelo jogador. |
| ⚡ | **Feedback visual** | O carro responde aos movimentos captados pelo sensor, permitindo observar a relação entre a inclinação do pé e o comando no jogo. |
| 📈 | **Progressão** | As fases apresentam diferentes níveis de dificuldade, configurando desafios progressivos dentro da experiência de jogo. |
| 📊 | **Registro de desempenho** | A tela de resultados apresenta dados da partida e os maiores valores de inclinação estimada para cada direção. |
| 🦶 | **Uso nos dois membros** | O mesmo sensor pode ser utilizado alternadamente no pé acometido e no não acometido, possibilitando registros individuais para comparação. |
| 🏆 | **Motivação** | Pontuação, recorde da sessão e contagem de vidas acrescentam elementos de desafio à atividade. |

---

## 🎮 3. Descrição do jogo

### 🏁 3.1 Visão geral

O jogador vê a pista pela traseira de um carro, o personagem principal. O carro ocupa uma de três vias (esquerda, centro ou direita). Moedas e cones descem pela pista em perspectiva: as **moedas** devem ser coletadas e os **cones**, evitados. O jogador troca de via inclinando o pé para um lado ou para o outro.

<p align="center">
  <img width="560" alt="Figura 1 - Tela de jogo" src="https://github.com/user-attachments/assets/0bf05c2e-bc09-47ed-b123-54cb0531f1c2" /><br>
  <em>Figura 1 – Tela de jogo: pista, carro, moedas, cones e placar.</em>
</p>

### 🔌 3.2 Integração com o dispositivo IoT

| | Item | Descrição |
|---|---|---|
| 🧩 | **Hardware** | Um ESP32 conectado a um sensor MPU6050 (acelerômetro e giroscópio), fixado ao pé do jogador. |
| 📐 | **Dados** | O sensor fornece dados inerciais utilizados pelo firmware para estimar a inclinação do dispositivo. O jogo utiliza a componente correspondente ao movimento lateral do pé. |
| 📡 | **Conexão** | O jogo se comunica com o ESP32 por **cabo USB (porta serial)** ou por **Wi-Fi (HTTP)**, com busca automática e uma tela para informar manualmente o endereço do ESP32. |
| 🧵 | **Processamento** | A leitura do sensor ocorre em uma thread separada do desenho do jogo, evitando bloquear a atualização da tela. |
| 🚦 | **Controle da via** | O carro muda para a via lateral quando a inclinação ultrapassa **5°** e retorna ao centro quando cai abaixo de **3°**. A diferença entre os limites evita oscilações próximas ao ponto de acionamento. O ângulo também é suavizado por um filtro para reduzir variações da leitura. |
| 🎛️ | **Controle alternativo** | Quando o sensor não está disponível, o teclado (setas ou `A` e `D`) pode ser utilizado como substituto para testes durante o desenvolvimento. |
| 🦶 | **Utilização nos dois pés** | O projeto utiliza apenas um MPU6050. O dispositivo pode ser fixado alternadamente no pé direito ou esquerdo, permitindo realizar sessões separadas em cada membro. |

### 🧭 3.3 Identificação do pé e calibração

Antes de iniciar a atividade, o jogador deve informar qual pé está utilizando: **direito ou esquerdo**. Essa identificação é necessária para interpretar corretamente a direção dos movimentos, pois a correspondência entre inclinação para a esquerda/direita e inversão/eversão muda conforme o pé utilizado.

| Pé utilizado | Inversão | Eversão |
|---|---|---|
| Direito | Inclinação medial (para a esquerda) | Inclinação lateral (para a direita) |
| Esquerdo | Inclinação medial (para a direita) | Inclinação lateral (para a esquerda) |

Antes da partida, o jogador apoia o pé em posição neutra e mantém o sensor imóvel durante o processo de calibração. A calibração estabelece a referência inicial de orientação para aquela colocação do dispositivo.

A tela apresenta:

- o estado da conexão com o ESP32 e da leitura do MPU6050;
- um indicador de nivelamento, com um ponto que se move conforme a inclinação;
- os valores lidos;
- o progresso da calibração.

A calibração pode ser refeita pela tela de pausa. Sempre que o sensor for transferido de um pé para o outro, deve ser reposicionado e calibrado novamente. Para que os registros sejam comparáveis, recomenda-se manter, tanto quanto possível, a mesma posição e orientação de fixação do sensor em cada pé.

<p align="center">
  <img width="560" alt="Figura 2 - Tela de calibração" src="https://github.com/user-attachments/assets/85dbfe59-b043-469e-a5c7-e6586f32697e" /><br>
  <em>Figura 2 – Tela de calibração do sensor.</em>
</p>

### 🗺️ 3.4 Fluxo de telas

| Tela | Função |
|---|---|
| 🏠 **Menu principal** | Jogar, calibrar sensor, configurar conexão Wi-Fi e sair. |
| 🧭 **Calibração** | Exibe o estado do ESP32 e do sensor, o progresso da calibração e o indicador de nivelamento. |
| ⏱️ **Duração da partida** | Permite escolher partidas de 1, 2 ou 3 minutos. |
| 🚦 **Preparação** | Apresenta o pé selecionado, o objetivo, os controles, o estado da conexão e da calibração e a contagem regressiva 3, 2, 1. |
| 🏎️ **Jogo** | Exibe pista, carro, moedas, cones e placar. |
| ⏸️ **Pausa** | Permite continuar, reiniciar a partida, recalibrar o sensor ou voltar ao menu, com confirmação. |
| 🏁 **Resultados** | Apresenta pontuação, recorde da sessão e indicadores de desempenho da partida. |

<table align="center">
  <tr>
    <td align="center">
      <img width="380" alt="Figura 3 - Menu principal" src="https://github.com/user-attachments/assets/de72ad61-91c2-4cd6-8623-7eed207632b1" /><br>
      <em>Figura 3 – Menu principal</em>
    </td>
    <td align="center">
      <img width="380" alt="Figura 4 - Duração da partida" src="https://github.com/user-attachments/assets/8099044b-2e70-49d8-a81a-1b5481b8af99" /><br>
      <em>Figura 4 – Duração da partida</em>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img width="380" alt="Figura 5 - Preparação" src="https://github.com/user-attachments/assets/f9291b4e-eabb-4ac7-8a95-a1679a39a839" /><br>
      <em>Figura 5 – Preparação da partida</em>
    </td>
    <td align="center">
      <img width="380" alt="Figura 6 - Pausa" src="https://github.com/user-attachments/assets/a01e7804-8a32-4c37-aef7-5326864feee5" /><br>
      <em>Figura 6 – Jogo pausado</em>
    </td>
  </tr>
</table>

### ⚙️ 3.5 Mecânicas e regras

| | Regra | Descrição |
|---|---|---|
| 🪙 | **Objetivo** | Coletar o máximo de moedas e evitar os cones até o fim do tempo escolhido. |
| ❤️ | **Vidas** | A partida termina quando o jogador bate três vezes nos cones ou quando o tempo acaba. |
| 💥 | **Colisão** | Moedas coletadas e batidas em cones são contabilizadas e exibidas no placar, junto com fase, pontos, tempo e vidas. |
| ⏯️ | **Pausa e reinício** | O jogador pode pausar a qualquer momento e reiniciar a partida, mantendo a mesma duração. |

### 📈 3.6 Fases e dificuldade progressiva

A partida é dividida em **três fases de igual duração** (cada uma corresponde a um terço do tempo escolhido). A cada mudança de fase, aparece um aviso na tela e a dificuldade aumenta.

| Fase | Velocidade dos itens | Intervalo entre itens | Chance de ser moeda |
|:---:|:---:|:---:|:---:|
| 🟢 **1** | 4,5 | 0,70 a 1,40 s | 70% |
| 🟡 **2** | 5,5 | 0,55 a 1,15 s | 60% |
| 🔴 **3** | 6,5 | 0,45 a 0,90 s | 50% |

Da fase 1 para a 3, os itens chegam mais rapidamente, em maior quantidade e com mais cones, exigindo mudanças de via mais frequentes.

A progressão descrita corresponde à dificuldade do jogo. Ela não representa uma recomendação de progressão terapêutica: a duração, a intensidade e a adequação dos exercícios devem ser definidas pelo fisioterapeuta.

### 🏆 3.7 Pontuação e métricas

A pontuação combina o desempenho do jogador durante a partida:

| | Componente | Regra |
|---|---|---|
| ➕ | **Pontos base** | +10 por moeda coletada e −5 por colisão (sem pontuação negativa). |
| 🎯 | **Bônus de consistência** | Até +100 pontos, proporcional ao aproveitamento de moedas coletadas em relação às que apareceram. |
| ⚡ | **Bônus de fase** | +20 pontos por fase alcançada. |

A tela de resultados apresenta a pontuação e sua composição, o melhor resultado da sessão, as moedas coletadas em relação às que apareceram, as colisões, o aproveitamento, a fase alcançada e o tempo jogado.

Também são apresentados os maiores valores de inclinação estimada associados aos movimentos de inversão e eversão, considerando o pé selecionado. Esses dados permitem comparar partidas realizadas em condições semelhantes e acompanhar os registros ao longo do tempo.

Quando o mesmo sensor é utilizado nos dois pés, os resultados podem ser registrados em sessões separadas e comparados posteriormente. Essa comparação é uma referência observacional e não corresponde, por si só, a uma avaliação clínica de simetria ou de recuperação funcional.

Os valores angulares exibidos pelo jogo devem ser identificados como **inclinação estimada pelo sensor**. Caso sejam apresentados percentuais em relação a valores de referência, eles devem ser descritos como percentuais da referência angular adotada, e não como percentual de recuperação.

<p align="center">
  <img width="560" alt="Figura 7 - Tela de resultados" src="https://github.com/user-attachments/assets/edb5726a-a30d-4219-b80c-32cb0c0e25fb" /><br>
  <em>Figura 7 – Tela de resultados da partida.</em>
</p>

### 🕹️ 3.8 Como jogar

| | Controle | Ação |
|---|---|---|
| 🦶 | **Sensor** | Incline o pé para a esquerda ou para a direita para trocar de via. A direção correspondente a inversão ou eversão depende do pé selecionado. |
| ⌨️ | **Teclado** | Setas esquerda e direita, ou as teclas `A` e `D`, somente para testes sem o sensor. |
| 🖱️ | **Mouse** | Navegar pelos menus e botões. |
| ⏸️ | **Pausa** | Tecla `ESC` ou `P` durante a partida. |

### 🛠️ 3.9 Tecnologias e organização técnica

| | Área | Detalhes |
|---|---|---|
| 💻 | **Linguagem e framework** | Java e libGDX 1.14.2, com a plataforma desktop LWJGL3. |
| 🔧 | **Hardware e comunicação** | ESP32, MPU6050, comunicação serial (jSerialComm) e HTTP. |
| 🧱 | **Estrutura do código** | Uma classe base para os objetos do jogo, da qual derivam o carro, os itens da pista, o fundo animado e os botões, além de uma classe para cada tela. A lógica segue o ciclo de entrada, atualização e desenho trabalhado nos laboratórios da disciplina. |
| 📋 | **Acompanhamento** | Repositório no GitHub com commits semanais dos integrantes e quadro Kanban no GitHub Projects. |

---

## 🧠 4. Protocolos proprioceptivos e biofeedback

### 4.1 Evidências sobre treinamento proprioceptivo

O treinamento proprioceptivo e os exercícios de equilíbrio são componentes importantes da reabilitação após entorse lateral do tornozelo e no manejo da instabilidade crônica. Revisões sistemáticas e ensaios clínicos apontam benefícios para o equilíbrio, a função e a redução de recorrências, especialmente em populações esportivas. Os resultados, entretanto, dependem da população, do histórico de lesão e das características do programa.

Um ensaio clínico randomizado de Hupperets et al. (2009), com 522 atletas acompanhados por um ano, avaliou um programa domiciliar de treinamento proprioceptivo de oito semanas associado ao cuidado habitual. Foram relatadas recorrências por 22% dos participantes do grupo de intervenção e por 33% do grupo controle, correspondendo a uma redução relativa de risco de 35% no estudo. Esses números dizem respeito à população e ao protocolo avaliados e não devem ser generalizados automaticamente para todos os pacientes.

Uma metanálise publicada em 2024, com 20 estudos e 682 participantes com instabilidade crônica do tornozelo, encontrou benefícios do treinamento de equilíbrio para função e equilíbrio dinâmico. Nas análises de subgrupos, programas com sessões de 20 a 30 minutos, três vezes por semana, durante quatro a seis semanas apresentaram resultados favoráveis. Essa faixa é uma referência encontrada na literatura, não uma prescrição universal.

### 4.2 Exercícios utilizados em programas proprioceptivos

Os protocolos descritos na literatura incluem combinações progressivas de tarefas como:

- **Apoio unipodal:** manter o equilíbrio sobre um pé, inicialmente em superfície estável e com os olhos abertos.
- **Transferência de peso:** deslocar o peso corporal em diferentes direções mantendo o controle postural.
- **Alcances multidirecionais:** tocar pontos à frente, atrás e nas diagonais sem perder o equilíbrio, como em tarefas inspiradas no Star Excursion Balance Test.
- **Superfícies instáveis:** realizar tarefas de equilíbrio em prancha proprioceptiva ou equipamento equivalente, quando apropriado.
- **Tarefas funcionais progressivas:** incluir mudanças de direção, aterrissagens e atividades específicas da rotina ou do esporte, somente quando houver liberação profissional.

A dificuldade deve progredir conforme a capacidade de execução e o controle do participante. A seleção dos exercícios, amplitude, duração, frequência e critérios de progressão cabe ao fisioterapeuta.

### 4.3 Biofeedback visual e sensores vestíveis

O biofeedback pode apresentar informações sobre o movimento durante a tarefa, ajudando o participante a compreender a relação entre a ação realizada e o resultado observado. Sensores vestíveis, como unidades de medição inercial (IMU), têm sido estudados para registrar orientação e movimento em contextos de avaliação e reabilitação musculoesquelética.

Um ensaio clínico randomizado publicado em 2026 investigou a adição de retreinamento da marcha com feedback visual a um programa de treinamento de resistência e equilíbrio em pessoas com instabilidade crônica do tornozelo. O estudo avaliou variáveis cinemáticas, centro de pressão e propriocepção. Os resultados são promissores para desfechos biomecânicos e proprioceptivos, mas não demonstram, por si só, que o biofeedback previna novas entorses em longo prazo.

Assim, a evidência para exercícios proprioceptivos e de equilíbrio na reabilitação é mais estabelecida do que a evidência para um dispositivo eletrônico específico ou para um protocolo de biofeedback padronizado. O biofeedback deve ser entendido como recurso complementar, e não como substituto do treinamento terapêutico supervisionado.

### 4.4 Aplicação possível no RehabCar

Considerando a arquitetura atual do RehabCar — ESP32, MPU6050 e aplicação Java/libGDX — o projeto pode explorar o feedback visual em exercícios ativos de inversão e eversão. A proposta abaixo é uma possibilidade de desenvolvimento e avaliação do protótipo; não corresponde a um protocolo clínico validado.

| Etapa proposta | Atividade no sistema | Informação que pode ser registrada |
|---|---|---|
| 1. Calibração | Estabelecer a posição neutra do sensor antes da sessão. | Referência inicial de orientação. |
| 2. Mobilidade controlada | Executar movimentos de inversão e eversão dentro da faixa orientada pelo fisioterapeuta. | Inclinação estimada e número de repetições. |
| 3. Alvos visuais | Tentar alcançar uma faixa ou alvo exibido na tela. | Diferença entre o alvo e a inclinação estimada. |
| 4. Controle do movimento | Repetir movimentos de forma controlada, evitando priorizar apenas velocidade. | Consistência e tempo de execução. |
| 5. Acompanhamento | Comparar sessões realizadas em condições semelhantes e, quando indicado, registros de cada pé. | Histórico de inclinação estimada por sessão e por lado. |

**Limites de medição:** o MPU6050 fixado ao pé registra dados inerciais e permite estimar a orientação do sensor. Essa medida não corresponde diretamente à amplitude articular medida por goniometria, à força muscular, ao centro de pressão ou ao equilíbrio corporal. Para medir esses outros parâmetros seriam necessários instrumentos e métodos específicos.

O jogo atual, que controla um carro em três vias por meio da inclinação do pé, não reproduz por si só exercícios de equilíbrio em apoio unipodal, tarefas em superfície instável ou retreinamento da marcha. Esses exercícios podem integrar um programa conduzido pelo fisioterapeuta, enquanto o RehabCar atua como ferramenta complementar de mobilidade, feedback e registro.

---

## ⚕️ 5. Limitações e finalidade clínica

O RehabCar é um protótipo de tecnologia voltado ao apoio de exercícios ativos de inversão e eversão do pé, no contexto da reabilitação do tornozelo. Sua utilização é considerada para uma etapa posterior à fase aguda e à imobilização, mediante liberação e orientação do fisioterapeuta.

O MPU6050 é uma unidade de medição inercial que fornece dados de aceleração e velocidade angular. A inclinação apresentada pelo jogo é estimada a partir desses dados e representa a orientação do sensor fixado ao pé. Portanto, essa leitura não equivale diretamente à amplitude articular medida por goniometria.

A posição de fixação, a orientação do dispositivo, a calibração e a execução do movimento podem influenciar os valores registrados. Por isso, a comparação entre sessões exige atenção à padronização do posicionamento e das condições de uso.

Os valores de referência angular eventualmente apresentados no jogo servem como parâmetros informativos de comparação, não como limites terapêuticos universais, metas obrigatórias ou critérios isolados de recuperação. A amplitude permitida, a intensidade, a duração e a progressão dos exercícios devem ser individualizadas pelo fisioterapeuta.

O dispositivo pode ser utilizado alternadamente nos dois pés para registrar os movimentos de cada membro. Essa funcionalidade permite uma comparação observacional entre os registros, mas não substitui avaliações clínicas específicas nem representa uma medição bilateral simultânea.

**O RehabCar é uma ferramenta complementar de interação, feedback visual e registro de desempenho. Não substitui a avaliação fisioterapêutica, a goniometria, o tratamento convencional ou a conduta de um profissional habilitado.**

---

## 📚 6. Referências e fontes consultadas

As informações utilizadas na contextualização da reabilitação do tornozelo, na justificativa dos exercícios e nas limitações de uso do RehabCar foram complementadas por diretrizes clínicas, revisões científicas e fontes institucionais. Essas referências fundamentam o uso de exercícios terapêuticos como parte da reabilitação, mas **não validam clinicamente o protótipo RehabCar**, seu sensor ou os valores angulares apresentados pelo jogo.

### 6.1 Consulta cadastral profissional – CREFITO-3

Também foi realizada consulta ao serviço oficial de consulta de profissionais do Conselho Regional de Fisioterapia e Terapia Ocupacional da 3ª Região (CREFITO-3):

- **Profissional exibido no resultado:** Mauricio Suner Gimenez.
- **Profissão:** Fisioterapeuta.
- **Registro informado:** CREFITO-3 nº 54838-F.
- **Situação apresentada na consulta:** exercício profissional ativo / profissional habilitado(a).
- **Localização informada:** São Paulo – SP.
- **Fonte:** [CREFITO-3 – Consulta de profissionais](https://www.crefito3.org.br/dsn/consultapf/detalhes.asp?tb=ni).

*Observação: os dados acima reproduzem o resultado apresentado na captura de tela fornecida para esta documentação. A consulta cadastral serve para registrar a verificação do profissional no conselho regional; não representa avaliação, aprovação ou validação técnica/científica do RehabCar.*

### 6.2 Artigos e diretrizes científicas

1. **MARTIN, RobRoy L. et al.** Ankle Stability and Movement Coordination Impairments: Lateral Ankle Ligament Sprains Revision 2021. *Journal of Orthopaedic & Sports Physical Therapy*, v. 51, n. 4, p. CPG1–CPG80, 2021. DOI: [10.2519/jospt.2021.0302](https://doi.org/10.2519/jospt.2021.0302). Disponível também em: [PubMed](https://pubmed.ncbi.nlm.nih.gov/33789434/).
   - **Contribuição para o trabalho:** diretriz de prática clínica sobre avaliação e manejo de entorse lateral e instabilidade crônica do tornozelo. Apresenta recomendações para exercícios terapêuticos, amplitude de movimento ativa protegida, treinamento neuromuscular, equilíbrio e propriocepção. A diretriz também ressalta que jogos e ambientes virtuais podem ter efeitos variados e, quando utilizados, devem atuar como recurso complementar.

2. **WAGEMANS, Jente et al.** Exercise-based rehabilitation reduces reinjury following acute lateral ankle sprain: A systematic review update with meta-analysis. *PLOS ONE*, v. 17, n. 2, e0262023, 2022. DOI: [10.1371/journal.pone.0262023](https://doi.org/10.1371/journal.pone.0262023). Disponível em: [PubMed](https://pubmed.ncbi.nlm.nih.gov/35134061/) | [Texto completo](https://pmc.ncbi.nlm.nih.gov/articles/PMC8824326/).
   - **Contribuição para o trabalho:** revisão sistemática com metanálise que investigou programas de reabilitação baseados em exercícios após entorse aguda do tornozelo. Os resultados apontaram redução da prevalência de novas entorses em 12 meses no grupo submetido a exercícios, embora os autores indiquem limitações e não estabeleçam um conteúdo ou volume ideal de treinamento.

3. **VASCONCELOS, Gabriela Souza de et al.** Effects of proprioceptive training on the incidence of ankle sprain in athletes: systematic review and meta-analysis. *Clinical Rehabilitation*, v. 32, n. 12, p. 1581–1590, 2018. DOI: [10.1177/0269215518788683](https://doi.org/10.1177/0269215518788683). Disponível em: [PubMed](https://pubmed.ncbi.nlm.nih.gov/29996668/).
   - **Contribuição para o trabalho:** revisão sistemática e metanálise sobre treinamento proprioceptivo e equilíbrio. O estudo encontrou redução na incidência de entorses entre atletas que realizaram treinamento de equilíbrio, oferecendo suporte à discussão sobre controle neuromuscular e propriocepção. Os resultados são específicos da população estudada e não devem ser generalizados automaticamente para todos os pacientes.

### 6.3 Treinamento proprioceptivo, equilíbrio e biofeedback

4. **HUPPERETS, Maarten D. W.; VERHAGEN, Evert A. L. M.; VAN MECHELEN, Willem.** Effect of unsupervised home based proprioceptive training on recurrences of ankle sprain: randomised controlled trial. *BMJ*, v. 339, b2684, 2009. DOI: [10.1136/bmj.b2684](https://doi.org/10.1136/bmj.b2684). Disponível em: [BMJ – texto completo](https://www.bmj.com/content/339/bmj.b2684.long).
   - **Contribuição para o trabalho:** ensaio clínico randomizado com 522 atletas e acompanhamento de um ano. O programa proprioceptivo domiciliar de oito semanas foi associado a menor proporção de recorrências autorrelatadas no grupo de intervenção (22%) em comparação ao controle (33%).

5. **TANG, Fang et al.** Meta-analysis of the dosage of balance training on ankle function and dynamic balance ability in patients with chronic ankle instability. *BMC Musculoskeletal Disorders*, v. 25, art. 689, 2024. DOI: [10.1186/s12891-024-07800-8](https://doi.org/10.1186/s12891-024-07800-8). Disponível em: [PubMed](https://pubmed.ncbi.nlm.nih.gov/39217316/) | [Texto completo](https://pmc.ncbi.nlm.nih.gov/articles/PMC11365157/).
   - **Contribuição para o trabalho:** metanálise de 20 estudos com 682 participantes com instabilidade crônica do tornozelo. Encontrou benefícios do treinamento de equilíbrio para função e equilíbrio dinâmico; as análises de subgrupos apontaram resultados favoráveis com sessões de 20–30 minutos, três vezes por semana, por quatro a seis semanas.

6. **ABBASI, Alireza; SHOJAEDIN, Seyed Sadredin; HOSSEINI, Seyed Mohammad.** Effects of adding gait retraining with visual feedback to resistance-balance training on kinematics, centers of pressure, and proprioception in individuals with chronic ankle instability: a randomized controlled trial. *BMC Sports Science, Medicine and Rehabilitation*, 2026. DOI: [10.1186/s13102-026-02061-5](https://doi.org/10.1186/s13102-026-02061-5). Disponível em: [Texto do artigo](https://link.springer.com/article/10.1186/s13102-026-02061-5).
   - **Contribuição para o trabalho:** ensaio randomizado com 45 participantes com instabilidade crônica do tornozelo, que investigou retreinamento da marcha com feedback visual associado ao treinamento de resistência e equilíbrio. Avaliou cinemática, centro de pressão e propriocepção. Não deve ser interpretado como comprovação de prevenção de recorrências em longo prazo.

7. **BROGNARA, L. et al.** Wearable Technology Applications and Methods to Assess Clinical Outcomes in Foot and Ankle Disorders: Achievements and Perspectives. *Sensors*, v. 24, n. 21, 7059, 2024. DOI: [10.3390/s24217059](https://doi.org/10.3390/s24217059).
   - **Contribuição para o trabalho:** revisão sobre aplicações de tecnologias vestíveis na avaliação de condições do pé e tornozelo. Contextualiza o potencial de sensores vestíveis e as limitações de validação e padronização das medidas.

### 6.4 Legislação e normas profissionais

8. **BRASIL.** Lei nº 6.316, de 17 de dezembro de 1975. Cria o Conselho Federal e os Conselhos Regionais de Fisioterapia e Terapia Ocupacional e dá outras providências. Disponível em: [Câmara dos Deputados – legislação](https://www2.camara.leg.br/legin/fed/lei/1970-1979/lei-6316-17-dezembro-1975-366429-normaatualizada-pl.html).
   - **Contribuição para o trabalho:** referência legal sobre a criação e as atribuições de fiscalização do COFFITO e dos CREFITOs no âmbito do exercício profissional.

9. **CONSELHO FEDERAL DE FISIOTERAPIA E TERAPIA OCUPACIONAL (COFFITO).** Resolução nº 424, de 8 de julho de 2013. Estabelece o Código de Ética e Deontologia da Fisioterapia. Disponível em: [COFFITO – Resolução nº 424/2013](https://www.coffito.gov.br/nsite/?p=3187).
   - **Contribuição para o trabalho:** norma de referência sobre deveres éticos e responsabilidades profissionais do fisioterapeuta.

### 6.5 Relação das fontes com o projeto

- **Contexto da reabilitação e exercícios ativos:** Martin et al. (2021) e Wagemans et al. (2022).
- **Propriocepção, equilíbrio e controle neuromuscular:** Martin et al. (2021) e Vasconcelos et al. (2018).
- **Uso do jogo como recurso complementar e limites da tecnologia:** Martin et al. (2021), em conjunto com as limitações descritas neste documento.
- **Consulta de habilitação profissional e contexto regulatório:** consulta cadastral do CREFITO-3, Lei nº 6.316/1975 e Resolução COFFITO nº 424/2013.

**Nota metodológica:** o RehabCar é apresentado como protótipo acadêmico e ferramenta complementar. As referências científicas não permitem afirmar que o jogo, isoladamente, trate lesões, determine a recuperação funcional ou substitua avaliação e prescrição individualizadas por fisioterapeuta.

