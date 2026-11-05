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
5. [Limitações e finalidade clínica](#-4-limitações-e-finalidade-clínica)

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

## ⚕️ 4. Limitações e finalidade clínica

O RehabCar é um protótipo de tecnologia voltado ao apoio de exercícios ativos de inversão e eversão do pé, no contexto da reabilitação do tornozelo. Sua utilização é considerada para uma etapa posterior à fase aguda e à imobilização, mediante liberação e orientação do fisioterapeuta.

O MPU6050 é uma unidade de medição inercial que fornece dados de aceleração e velocidade angular. A inclinação apresentada pelo jogo é estimada a partir desses dados e representa a orientação do sensor fixado ao pé. Portanto, essa leitura não equivale diretamente à amplitude articular medida por goniometria.

A posição de fixação, a orientação do dispositivo, a calibração e a execução do movimento podem influenciar os valores registrados. Por isso, a comparação entre sessões exige atenção à padronização do posicionamento e das condições de uso.

Os valores de referência angular eventualmente apresentados no jogo servem como parâmetros informativos de comparação, não como limites terapêuticos universais, metas obrigatórias ou critérios isolados de recuperação. A amplitude permitida, a intensidade, a duração e a progressão dos exercícios devem ser individualizadas pelo fisioterapeuta.

O dispositivo pode ser utilizado alternadamente nos dois pés para registrar os movimentos de cada membro. Essa funcionalidade permite uma comparação observacional entre os registros, mas não substitui avaliações clínicas específicas nem representa uma medição bilateral simultânea.

**O RehabCar é uma ferramenta complementar de interação, feedback visual e registro de desempenho. Não substitui a avaliação fisioterapêutica, a goniometria, o tratamento convencional ou a conduta de um profissional habilitado.**
