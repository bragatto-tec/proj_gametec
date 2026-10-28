# RehabCar

**Autores:** Guilherme Miyamoto Bragatto, Eduardo Okada 
---

## Resumo

O RehabCar é um jogo sério desenvolvido em Java com a biblioteca libGDX para tornar mais motivador o exercício de reabilitação de tornozelo. O jogador conduz um carro em uma pista de três vias usando apenas a inclinação lateral do pé, medida por um dispositivo IoT composto por um ESP32 e um sensor inercial MPU6050. O jogo coleta o ângulo em tempo real, transforma o movimento em controle e converte o desempenho (moedas coletadas, batidas e progresso nas fases) em pontuação. O protótipo tem calibração do sensor, três fases de dificuldade crescente, pausa, tela de resultados e conexão por cabo USB ou Wi-Fi.

## 1. Descrição do problema e contexto da reabilitação

A reabilitação de tornozelo, por exemplo após uma entorse, depende da repetição de movimentos controlados e do ganho gradual de amplitude e de controle. Entre os movimentos trabalhados nessa região estão a **inversão e a eversão** (inclinar a planta do pé para dentro e para fora) e a **dorsiflexão e a flexão plantar** (levantar e abaixar a ponta do pé).

Esses exercícios são repetitivos e de difícil acompanhamento fora do consultório: o paciente nem sempre sabe se executa o movimento com a amplitude adequada, e o acompanhamento costuma depender de relato e observação. A repetição monótona pode reduzir a motivação e a adesão ao tratamento. [COMPLETAR: incluir uma ou duas referências, ou a orientação de um profissional de fisioterapia, que sustentem este contexto. Evitem afirmações clínicas sem fonte.]

O RehabCar foca no movimento de **inversão e eversão** do tornozelo. O dispositivo IoT é indispensável ao problema, porque é ele que mede a inclinação do pé e permite que o próprio exercício seja o controle do jogo, com uma medição objetiva do que o jogador realizou.

## 2. Justificativa

A gamificação oferece ao exercício elementos que o tornam mais atrativo e mensurável:

- **Objetivo claro e imediato:** coletar moedas e desviar de cones dá sentido a cada repetição do movimento.
- **Feedback instantâneo:** o carro responde ao pé do jogador, que vê na tela se o movimento foi feito.
- **Progressão:** três fases com velocidade e quantidade de obstáculos crescentes incentivam a evolução gradual.
- **Medição objetiva:** a tela de resultados mostra o aproveitamento de moedas, a fase alcançada e a maior inclinação atingida para cada lado, informações que o jogador e um profissional podem consultar.
- **Motivação:** pontuação, recorde da sessão e contagem de vidas criam um desafio de superação.

## 3. Descrição do jogo

### 3.1 Visão geral

O jogador vê a pista pela traseira de um carro, o personagem principal. O carro ocupa uma de três vias (esquerda, centro ou direita). Moedas e cones descem pela pista em perspectiva: as moedas devem ser coletadas e os cones, evitados. O jogador troca de via inclinando o pé para um lado ou para o outro.

[COMPLETAR: Figura 1, tela de jogo]

### 3.2 Integração com o dispositivo IoT

- **Hardware:** um ESP32 ligado a um sensor MPU6050 (acelerômetro e giroscópio), posicionado no pé do jogador.
- **Dados:** o sensor fornece os ângulos de inclinação do pé. O jogo usa o ângulo correspondente à inclinação lateral (inversão e eversão).
- **Conexão:** o jogo se comunica com o ESP32 por **cabo USB (porta serial)** ou por **Wi-Fi (HTTP)**, com busca automática e uma tela para informar o endereço do ESP32 manualmente.
- **Processamento sem travar a tela:** a leitura do sensor roda em uma thread separada do desenho do jogo.
- **Controle da via:** o carro muda para a via lateral quando a inclinação passa de 5 graus e volta ao centro quando ela cai abaixo de 3 graus. Essa diferença entre os dois limites evita que o carro fique oscilando perto do limite. O ângulo também é suavizado por um filtro, para reduzir o tremor da leitura.
- **Controle exclusivamente pelo dispositivo:** na demonstração o jogo é controlado só pelo sensor. Para o desenvolvimento, quando o sensor não está disponível, o teclado (setas ou A e D) funciona como substituto de teste.

### 3.3 Calibração

Antes de jogar, o jogador apoia o sensor em posição neutra e o mantém imóvel enquanto uma barra de progresso é preenchida. A tela mostra o estado da conexão com o ESP32 e da leitura do MPU6050, um indicador de nivelamento com um ponto que se move conforme a inclinação, e os valores lidos. A calibração pode ser refeita pela tela de pausa.

[COMPLETAR: Figura 2, tela de calibração]

### 3.4 Fluxo de telas

| Tela | Função |
|---|---|
| Menu principal | Jogar, Calibrar sensor, conexão Wi-Fi e Sair |
| Calibração | Estado do ESP32 e do sensor, progresso e nivelamento |
| Duração da partida | Escolha de 1, 2 ou 3 minutos |
| Preparação | Objetivo, controles, estado da conexão e da calibração, contagem regressiva 3, 2, 1 |
| Jogo | Pista, carro, moedas, cones e placar |
| Pausa | Continuar, reiniciar partida, recalibrar sensor e voltar ao menu (com confirmação) |
| Resultados | Pontuação, recorde da sessão e desempenho da partida |

[COMPLETAR: Figuras 3 a 6, telas de menu, duração, preparação e pausa]

### 3.5 Mecânicas e regras

- **Objetivo:** coletar o máximo de moedas e evitar os cones até o fim do tempo escolhido.
- **Vidas:** a partida termina quando o jogador **bate três vezes** nos cones ou quando o **tempo acaba**.
- **Colisão:** moedas coletadas e batidas em cones são contadas e mostradas no placar (fase, pontos, tempo, vidas, moedas e batidas).
- **Pausa e reinício:** o jogador pode pausar a qualquer momento e reiniciar a partida, mantendo a mesma duração.

### 3.6 Fases e dificuldade progressiva

A partida é dividida em **três fases de igual duração** (cada uma é um terço do tempo escolhido). A cada mudança de fase aparece um aviso na tela e a dificuldade aumenta.

| Fase | Velocidade dos itens | Intervalo entre itens | Chance de ser moeda |
|---|---|---|---|
| 1 | 4,5 | 0,70 a 1,40 s | 70% |
| 2 | 5,5 | 0,55 a 1,15 s | 60% |
| 3 | 6,5 | 0,45 a 0,90 s | 50% |

Da fase 1 para a 3 os itens chegam mais rápido, em maior quantidade e com mais cones, o que exige mudanças de via mais rápidas e frequentes. [COMPLETAR: informar se os valores foram ajustados após testes com o sensor.]

### 3.7 Pontuação e métricas

A pontuação combina o desempenho do jogador e o indicador derivado do sensor:

- **Pontos base:** +10 por moeda coletada e −5 por batida (nunca abaixo de zero).
- **Bônus de consistência:** até +100 pontos, proporcional ao **aproveitamento**, ou seja, a porcentagem de moedas coletadas em relação às que apareceram.
- **Bônus de velocidade:** +20 pontos por fase alcançada, porque cada fase acelera os itens e exige movimentos mais rápidos.

A tela de resultados mostra a pontuação com a soma de cada parte, o melhor resultado da sessão, as moedas coletadas de quantas apareceram, as batidas, o aproveitamento, a fase alcançada, o tempo jogado e a **maior inclinação atingida para a esquerda e para a direita**. Esses dados permitem comparar partidas e acompanhar a evolução do jogador.

[COMPLETAR: Figura 7, tela de resultados]

### 3.8 Organização técnica

- **Linguagem e framework:** Java e libGDX 1.14.2, com a plataforma desktop LWJGL3.
- **Hardware e comunicação:** ESP32, MPU6050, porta serial (jSerialComm) e HTTP.
- **Estrutura do código:** uma classe base para os objetos do jogo, da qual derivam o carro, os itens da pista, o fundo animado e os botões, e uma classe para cada tela. A lógica segue o ciclo de entrada, atualização e desenho trabalhado nos laboratórios da disciplina.
- **Acompanhamento:** repositório no GitHub com commits semanais de todos os integrantes e quadro kanban no GitHub Projects.
