<h1 align="center">🚗 RehabCar</h1>

<p align="center">
  <strong>Jogo sério de reabilitação de tornozelo com ESP32 e libGDX</strong>
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-libGDX%201.14.2-orange">
  <img alt="Hardware" src="https://img.shields.io/badge/Hardware-ESP32%20%2B%20MPU6050-blue">
  <img alt="Conexão" src="https://img.shields.io/badge/Conex%C3%A3o-USB%20%7C%20Wi--Fi-green">
</p>

<p align="center">
  👥 <strong>Autores:</strong> Guilherme Miyamoto Bragatto e Eduardo Okada
</p>

---

## 📑 Sumário

1. [Resumo](#-resumo)
2. [Descrição do problema e contexto da reabilitação](#-1-descrição-do-problema-e-contexto-da-reabilitação)
3. [Justificativa](#-2-justificativa)
4. [Descrição do jogo](#-3-descrição-do-jogo)
   - [3.1 Visão geral](#-31-visão-geral)
   - [3.2 Integração com o dispositivo IoT](#-32-integração-com-o-dispositivo-iot)
   - [3.3 Calibração](#-33-calibração)
   - [3.4 Fluxo de telas](#️-34-fluxo-de-telas)
   - [3.5 Mecânicas e regras](#️-35-mecânicas-e-regras)
   - [3.6 Fases e dificuldade progressiva](#-36-fases-e-dificuldade-progressiva)
   - [3.7 Pontuação e métricas](#-37-pontuação-e-métricas)
   - [3.8 Como jogar](#️-38-como-jogar)
   - [3.9 Tecnologias e organização técnica](#️-39-tecnologias-e-organização-técnica)

---

## 📝 Resumo

O **RehabCar** é um jogo sério desenvolvido em **Java** com a biblioteca **libGDX** para tornar mais motivador o exercício de reabilitação de tornozelo. O jogador conduz um carro em uma pista de três vias usando apenas a inclinação lateral do pé, medida por um dispositivo IoT composto por um **ESP32** e um sensor inercial **MPU6050**.

O jogo coleta o ângulo em tempo real, transforma o movimento em controle e converte o desempenho (moedas coletadas, batidas e progresso nas fases) em pontuação. O protótipo tem calibração do sensor, três fases de dificuldade crescente, pausa, tela de resultados e conexão por cabo USB ou Wi-Fi.

---

## 🩺 1. Descrição do problema e contexto da reabilitação

A reabilitação de tornozelo, por exemplo após uma entorse, depende da repetição de movimentos controlados e do ganho gradual de amplitude e de controle. Entre os movimentos trabalhados nessa região estão:

- **Inversão e eversão:** inclinar a planta do pé para dentro e para fora.
- **Dorsiflexão e flexão plantar:** levantar e abaixar a ponta do pé.

Esses exercícios são repetitivos e de difícil acompanhamento fora do consultório: o paciente nem sempre sabe se executa o movimento com a amplitude adequada, e o acompanhamento costuma depender de relato e observação. A repetição monótona pode reduzir a motivação e a adesão ao tratamento.

> ⚠️ **[COMPLETAR]** Incluir uma ou duas referências, ou a orientação de um profissional de fisioterapia, que sustentem este contexto. Evitem afirmações clínicas sem fonte.

O RehabCar foca no movimento de **inversão e eversão** do tornozelo. O dispositivo IoT é indispensável ao problema, porque é ele que mede a inclinação do pé e permite que o próprio exercício seja o controle do jogo, com uma medição objetiva do que o jogador realizou.

---

## 🎯 2. Justificativa

A gamificação oferece ao exercício elementos que o tornam mais atrativo e mensurável:

| | Elemento | Como ajuda |
|---|---|---|
| 🎯 | **Objetivo claro e imediato** | Coletar moedas e desviar de cones dá sentido a cada repetição do movimento. |
| ⚡ | **Feedback instantâneo** | O carro responde ao pé do jogador, que vê na tela se o movimento foi feito. |
| 📈 | **Progressão** | Três fases com velocidade e quantidade de obstáculos crescentes incentivam a evolução gradual. |
| 📊 | **Medição objetiva** | A tela de resultados mostra o aproveitamento de moedas, a fase alcançada e a maior inclinação atingida para cada lado, informações que o jogador e um profissional podem consultar. |
| 🏆 | **Motivação** | Pontuação, recorde da sessão e contagem de vidas criam um desafio de superação. |

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
| 🧩 | **Hardware** | Um ESP32 ligado a um sensor MPU6050 (acelerômetro e giroscópio), posicionado no pé do jogador. |
| 📐 | **Dados** | O sensor fornece os ângulos de inclinação do pé. O jogo usa o ângulo correspondente à inclinação lateral (inversão e eversão). |
| 📡 | **Conexão** | O jogo se comunica com o ESP32 por **cabo USB (porta serial)** ou por **Wi-Fi (HTTP)**, com busca automática e uma tela para informar o endereço do ESP32 manualmente. |
| 🧵 | **Processamento** | A leitura do sensor roda em uma thread separada do desenho do jogo, sem travar a tela. |
| 🚦 | **Controle da via** | O carro muda para a via lateral quando a inclinação passa de **5°** e volta ao centro quando ela cai abaixo de **3°**. Essa diferença entre os dois limites evita que o carro fique oscilando perto do limite. O ângulo também é suavizado por um filtro, para reduzir o tremor da leitura. |
| 🎛️ | **Controle pelo dispositivo** | Na demonstração o jogo é controlado só pelo sensor. Para o desenvolvimento, quando o sensor não está disponível, o teclado (setas ou `A` e `D`) funciona como substituto de teste. |

### 🧭 3.3 Calibração

Antes de jogar, o jogador apoia o sensor em posição neutra e o mantém imóvel enquanto uma barra de progresso é preenchida. A tela mostra:

- o estado da conexão com o ESP32 e da leitura do MPU6050;
- um indicador de nivelamento, com um ponto que se move conforme a inclinação;
- os valores lidos.

A calibração pode ser refeita pela tela de pausa.

<p align="center">
  <img width="560" alt="Figura 2 - Tela de calibração" src="https://github.com/user-attachments/assets/85dbfe59-b043-469e-a5c7-e6586f32697e" /><br>
  <em>Figura 2 – Tela de calibração do sensor.</em>
</p>

### 🗺️ 3.4 Fluxo de telas

| Tela | Função |
|---|---|
| 🏠 **Menu principal** | Jogar, Calibrar sensor, conexão Wi-Fi e Sair |
| 🧭 **Calibração** | Estado do ESP32 e do sensor, progresso e nivelamento |
| ⏱️ **Duração da partida** | Escolha de 1, 2 ou 3 minutos |
| 🚦 **Preparação** | Objetivo, controles, estado da conexão e da calibração, contagem regressiva 3, 2, 1 |
| 🏎️ **Jogo** | Pista, carro, moedas, cones e placar |
| ⏸️ **Pausa** | Continuar, reiniciar partida, recalibrar sensor e voltar ao menu (com confirmação) |
| 🏁 **Resultados** | Pontuação, recorde da sessão e desempenho da partida |

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
| ❤️ | **Vidas** | A partida termina quando o jogador **bate três vezes** nos cones ou quando o **tempo acaba**. |
| 💥 | **Colisão** | Moedas coletadas e batidas em cones são contadas e mostradas no placar (fase, pontos, tempo, vidas, moedas e batidas). |
| ⏯️ | **Pausa e reinício** | O jogador pode pausar a qualquer momento e reiniciar a partida, mantendo a mesma duração. |

### 📈 3.6 Fases e dificuldade progressiva

A partida é dividida em **três fases de igual duração** (cada uma é um terço do tempo escolhido). A cada mudança de fase aparece um aviso na tela e a dificuldade aumenta.

| Fase | Velocidade dos itens | Intervalo entre itens | Chance de ser moeda |
|:---:|:---:|:---:|:---:|
| 🟢 **1** | 4,5 | 0,70 a 1,40 s | 70% |
| 🟡 **2** | 5,5 | 0,55 a 1,15 s | 60% |
| 🔴 **3** | 6,5 | 0,45 a 0,90 s | 50% |

Da fase 1 para a 3 os itens chegam mais rápido, em maior quantidade e com mais cones, o que exige mudanças de via mais rápidas e frequentes.

### 🏆 3.7 Pontuação e métricas

A pontuação combina o desempenho do jogador e o indicador derivado do sensor:

| | Componente | Regra |
|---|---|---|
| ➕ | **Pontos base** | +10 por moeda coletada e −5 por batida (nunca abaixo de zero). |
| 🎯 | **Bônus de consistência** | Até +100 pontos, proporcional ao **aproveitamento**, ou seja, a porcentagem de moedas coletadas em relação às que apareceram. |
| ⚡ | **Bônus de velocidade** | +20 pontos por fase alcançada, porque cada fase acelera os itens e exige movimentos mais rápidos. |

A tela de resultados mostra a pontuação com a soma de cada parte, o melhor resultado da sessão, as moedas coletadas de quantas apareceram, as batidas, o aproveitamento, a fase alcançada, o tempo jogado e a **maior inclinação atingida para a esquerda e para a direita**. Esses dados permitem comparar partidas e acompanhar a evolução do jogador.

<p align="center">
  <img width="560" alt="Figura 7 - Tela de resultados" src="https://github.com/user-attachments/assets/edb5726a-a30d-4219-b80c-32cb0c0e25fb" /><br>
  <em>Figura 7 – Tela de resultados da partida.</em>
</p>

### 🕹️ 3.8 Como jogar

| | Controle | Ação |
|---|---|---|
| 🦶 | **Sensor** (controle do jogo) | Incline o pé para a esquerda ou para a direita para trocar de via. |
| ⌨️ | **Teclado** (somente teste, sem o sensor) | Setas esquerda e direita, ou as teclas `A` e `D`. |
| 🖱️ | **Mouse** | Navegar pelos menus e botões. |
| ⏸️ | **Pausa** | Tecla `ESC` ou `P` durante a partida. |

### 🛠️ 3.9 Tecnologias e organização técnica

| | Área | Detalhes |
|---|---|---|
| 💻 | **Linguagem e framework** | Java e libGDX 1.14.2, com a plataforma desktop LWJGL3. |
| 🔧 | **Hardware e comunicação** | ESP32, MPU6050, porta serial (jSerialComm) e HTTP. |
| 🧱 | **Estrutura do código** | Uma classe base para os objetos do jogo, da qual derivam o carro, os itens da pista, o fundo animado e os botões, e uma classe para cada tela. A lógica segue o ciclo de entrada, atualização e desenho trabalhado nos laboratórios da disciplina. |
| 📋 | **Acompanhamento** | Repositório no GitHub com commits semanais de todos os integrantes e quadro kanban no GitHub Projects. |

---
