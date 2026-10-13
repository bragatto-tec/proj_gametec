## 📅 Cronograma do Projeto

**Última atualização:** 13/10/2026  
**Prazo de entrega:** 04/11/2026

O cronograma apresenta as etapas de desenvolvimento do RehabCar, incluindo hardware/IoT, gameplay, funcionalidades de reabilitação, métricas e entrega final.

> **Observação:** as datas das atividades detalhadas são estimativas. O status deve ser atualizado conforme o progresso real da equipe.

### 📊 Cronograma Gantt

```mermaid
gantt
    title Cronograma do Projeto RehabCar
    dateFormat YYYY-MM-DD
    axisFormat %d/%m
    tickInterval 1week

    section Hardware e IoT
    Leitura do MPU6050 no firmware :done, hw1, 2026-10-01, 2026-10-08
    Cliente HTTP SensorClient :done, hw2, 2026-10-09, 2026-10-12
    Validacao do sensor com o jogo :active, hw3, 2026-10-13, 2026-10-23

    section Gameplay
    Menu de calibracao e controles :done, gp1, 2026-09-28, 2026-10-12
    Mecanicas principais e menus :active, gp2, 2026-10-13, 2026-10-20
    Desenvolvimento das fases e obstaculos :gp3, 2026-10-20, 2026-10-27
    Colisoes e validacao :gp4, 2026-10-20, 2026-10-27
    Polimento e balanceamento :gp5, 2026-10-28, 2026-10-30

    section Reabilitacao
    Metas de amplitude por exercicio :reh1, 2026-10-20, 2026-10-27
    Feedback dos movimentos :reh2, 2026-10-27, 2026-10-30

    section Metricas
    Pontuacao por desempenho :met1, 2026-10-20, 2026-10-27
    Integracao e correcao de erros :met2, 2026-10-28, 2026-10-31

    section Documentacao e Entrega
    Short paper e documentacao :doc1, 2026-10-27, 2026-11-03
    Testes finais e validacao do executavel :test1, 2026-11-01, 2026-11-03
    Entrega final :milestone, entrega, 2026-11-04, 0d
```

### 📌 Etapas e prazos

| Etapa | Prazo | Status |
|---|---|---|
| Ideação, viabilidade e planejamento inicial | 02/10/2026 | ✅ Concluído |
| Configuração da base em libGDX e cronograma | 06/10/2026 | ✅ Concluído |
| Hardware, menus e integração com o MPU6050 | 13/10/2026 | ✅ Concluído |
| Mecânicas principais, menus e início das fases | 20/10/2026 | 🔄 Em andamento |
| Fases, obstáculos, colisões e metas de reabilitação | 27/10/2026 | ⬜ Planejado |
| Short paper e documentação | 03/11/2026 | ⬜ Planejado |
| Testes finais e entrega do executável | 04/11/2026 | ⬜ Planejado |

### 🎯 Marcos do projeto

- **13/10:** base técnica, menus e integração inicial do sensor.
- **20/10:** mecânicas principais e controles direcionais.
- **27/10:** fases, obstáculos, colisões e funcionalidades de reabilitação.
- **03/11:** documentação, short paper e preparação do executável.
- **04/11:** entrega final do projeto.

### 📝 Observações

- Validar a comunicação entre o ESP32, o MPU6050 e o jogo.
- Garantir que a calibração e os controles direcionais funcionem corretamente.
- Integrar as metas de movimento e a pontuação às mecânicas do jogo.
- Realizar testes integrados antes da entrega.
- Atualizar os status e as datas conforme o progresso real da equipe.

**Legenda:** `done` = concluído · `active` = em andamento · sem marcador = planejado.