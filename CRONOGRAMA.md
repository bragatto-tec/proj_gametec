# Cronograma do projeto

> **Premissa (estimada):** como a data final e as datas intermediárias não foram informadas, este cronograma considera a entrega em **15/12/2026**. Todas as datas, inclusive os períodos atribuídos às etapas já concluídas, são estimadas. As etapas ainda não descritas em detalhe também são estimativas e devem ser confirmadas.

```mermaid
gantt
    title Cronograma do RehabCar / Projeto Tec
    dateFormat YYYY-MM-DD
    axisFormat %d/%m

    section Hardware/IoT
    Leitura do MPU6050 no firmware (período estimado) :done, hw_mpu6050, 2026-10-01, 2026-10-08
    Cliente HTTP SensorClient (período estimado) :done, hw_sensorclient, 2026-10-09, 2026-10-12
    Validação ponta a ponta do sensor (estimado) :active, hw_validacao, 2026-10-13, 2026-10-23

    section Gameplay
    Menu calibração e controle por direções (período estimado) :done, game_base, 2026-09-28, 2026-10-12
    Fases e obstáculos (estimado) :active, game_fases, 2026-10-13, 2026-11-06
    Polimento e balanceamento (estimado) :game_polimento, 2026-11-09, 2026-11-20

    section Reabilitação
    Metas de amplitude por exercício (estimado) :rehab_metas, 2026-10-26, 2026-11-13
    Feedback do movimento e ajustes (estimado) :rehab_feedback, 2026-11-16, 2026-11-27

    section Métricas
    Pontuação por desempenho (estimado) :metricas_pontuacao, 2026-11-09, 2026-11-20
    Histórico e resumo da sessão (estimado) :metricas_historico, 2026-11-23, 2026-12-04

    section Entrega
    Testes integrados e correções (estimado) :entrega_testes, 2026-11-30, 2026-12-06
    Documentação final e pacote demonstrável (estimado) :entrega_docs, 2026-12-07, 2026-12-15
    Entrega final (estimada) :milestone, entrega_final, 2026-12-15, 0d
```

| Fase | Prazo | Status |
|---|---|---|
| Ideação, viabilidade e planejamento inicial | 02/10/2026 | ✅ |
| Setup Base (libGDX) e cronograma | 06/10/2026 | ✅ |
| Hardware, Menus e integração MPU6050 | 13/10/2026 | ✅ |
| Mecânicas Core, Finalizar Menus, Iniciar fases| 20/10/2026 | 🔄 |
| Fases, obstáculos, colisões e validação  e metas de reabilitação | 27/10/2026 | ⬜ |
| Short Paper e Documentação | 03/11/2026 | ⬜ |
| Testes finais e Entrega do Executável | 04/11/2026 | ⬜ |

✅ indica concluído · 🔄 indica ativo · ⬜ indica futuro.
Última atualização: 13/10/2026
