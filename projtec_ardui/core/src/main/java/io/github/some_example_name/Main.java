package io.github.some_example_name;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.Locale;

public class Main implements ApplicationListener {
    private final String sensorBaseUrl;

    private enum Estado {
        MENU,
        CALIBRANDO,
        SELECAO_DURACAO,
        PREPARACAO,
        PAUSA,
        JOGANDO,
        RESULTADOS
    }

    // Carro
    private static final float CARRO_LARGURA = 0.72f;
    private static final float CARRO_Y = 0.35f;

    // Regras da partida
    private static final int MAX_BATIDAS = 3;
    private static final int PONTOS_MOEDA = 10;
    private static final int PONTOS_BATIDA = 5;
    private static final int FASES = 3;
    private static final float DURACAO_AVISO_FASE = 2f;
    // Bonus de velocidade: quanto mais fases o jogador acompanha, mais pontos (a pista acelera a cada fase).
    private static final int BONUS_VELOCIDADE_POR_FASE = 20;
    // Velocidade da animacao da rua em cada fase (1 = 24 quadros por segundo).
    // Acompanha a velocidade dos itens: 4,5 / 5,5 / 6,5 no GerenciadorPista.
    private static final float[] VELOCIDADES_RUA = {1.0f, 1.22f, 1.44f};

    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private BitmapFont font;
    private UiAssets uiAssets;
    private Texture carroTexture;
    private Car carro;
    private Vector2 touchPos;
    private Rua rua;
    private GerenciadorPista pista;

    private SensorClient sensorClient;
    private TelaCalibracao telaCalibracao;
    private TelaSelecaoDuracao telaSelecaoDuracao;
    private TelaPreparacao telaPreparacao;
    private TelaPausa telaPausa;
    private TelaResultados telaResultados;
    private boolean recalibrandoDaPausa;
    private int duracaoPartida = 60;
    private float tempoRestante;
    private int fase = 1;
    private float tempoAviso;
    private String motivoFim = "";
    private boolean usouSensor;
    private float maiorInclinacaoEsquerda;
    private float maiorInclinacaoDireita;
    private Estado estadoAtual;

    private Button botaoJogar;
    private Button botaoCalibrar;
    private Button botaoSair;

    public Main() {
        this(null);
    }

    public Main(String sensorBaseUrl) {
        this.sensorBaseUrl = sensorBaseUrl;
    }

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        // As telas novas usam os recursos do tema Ui; carregue-os antes do primeiro draw.
        Ui.carregar();
        font = Ui.criarFonte();
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.02f);

        uiAssets = new UiAssets(System.getProperty("rehabcar.language", "pt"));
        carroTexture = new Texture("carros/car_hatch_sem-placa_e63946_4x5.png");
        carro = new Car(carroTexture, CARRO_Y, CARRO_LARGURA);
        touchPos = new Vector2();
        rua = new Rua(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight(), 24f);
        pista = new GerenciadorPista();

        sensorClient = sensorBaseUrl == null ? new SensorClient() : new SensorClient(sensorBaseUrl);
        telaCalibracao = new TelaCalibracao(viewport, font, uiAssets, sensorClient);
        telaSelecaoDuracao = new TelaSelecaoDuracao(viewport, font, uiAssets);
        telaPreparacao = new TelaPreparacao(viewport, font, uiAssets, sensorClient);
        telaPausa = new TelaPausa(viewport, font, uiAssets);
        telaResultados = new TelaResultados(viewport, font, uiAssets);
        estadoAtual = Estado.MENU;

        botaoJogar = new Button(uiAssets, "jogar", 2.5f, 3.0f, 3, 0.7f);
        botaoCalibrar = new Button(uiAssets, "calibrar_sensor", 2.5f, 2.0f, 3, 0.7f);
        botaoSair = new Button(uiAssets, "sair", 2.5f, 1.0f, 3, 0.7f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        if (estadoAtual == Estado.MENU) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            botaoJogar.atualizarEstado(touchPos);
            botaoCalibrar.atualizarEstado(touchPos);
            botaoSair.atualizarEstado(touchPos);
        }
        if (estadoAtual == Estado.CALIBRANDO) {
            if (telaCalibracao.input()) {
                sairDaCalibracao();
            }
            return;
        }

        if (estadoAtual == Estado.SELECAO_DURACAO) {
            telaSelecaoDuracao.input();
            if (telaSelecaoDuracao.isVoltarSolicitado()) {
                estadoAtual = Estado.MENU;
                telaSelecaoDuracao.fechar();
            } else if (telaSelecaoDuracao.isSelecionarSolicitado()) {
                duracaoPartida = telaSelecaoDuracao.getDuracaoSegundos();
                telaPreparacao.abrir(duracaoPartida);
                estadoAtual = Estado.PREPARACAO;
                telaSelecaoDuracao.fechar();
            }
            return;
        }

        if (estadoAtual == Estado.PREPARACAO) {
            telaPreparacao.input();
            if (telaPreparacao.isVoltarSolicitado()) {
                telaSelecaoDuracao.abrir();
                estadoAtual = Estado.SELECAO_DURACAO;
                telaPreparacao.fechar();
            }
            return;
        }

        if (estadoAtual == Estado.JOGANDO) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                telaPausa.abrir();
                estadoAtual = Estado.PAUSA;
            } else if (!sensorClient.sensorPronto()) {
                // sem MPU6050: troca de via pelo teclado (setas ou A/D)
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                    carro.moverEsquerda();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                    carro.moverDireita();
                }
            }
            return;
        }

        if (estadoAtual == Estado.PAUSA) {
            telaPausa.input();
            if (telaPausa.isContinuarSolicitado()) {
                estadoAtual = Estado.JOGANDO;
                telaPausa.fechar();
            } else if (telaPausa.isReiniciarSolicitado()) {
                reiniciarPartida();
                estadoAtual = Estado.JOGANDO;
                telaPausa.fechar();
            } else if (telaPausa.isRecalibrarSolicitado()) {
                recalibrandoDaPausa = true;
                telaCalibracao.abrir();
                estadoAtual = Estado.CALIBRANDO;
                telaPausa.fechar();
            } else if (telaPausa.isMenuSolicitado()) {
                estadoAtual = Estado.MENU;
                telaPausa.fechar();
            }
            return;
        }

        if (estadoAtual == Estado.RESULTADOS) {
            telaResultados.input();
            if (telaResultados.isJogarNovamenteSolicitado()) {
                // mesmo modo e mesma duracao, passando pela contagem 3, 2, 1
                telaPreparacao.abrir(duracaoPartida);
                estadoAtual = Estado.PREPARACAO;
                telaResultados.fechar();
            } else if (telaResultados.isMenuSolicitado()) {
                estadoAtual = Estado.MENU;
                telaResultados.fechar();
            }
            return;
        }

        if (Gdx.input.justTouched()) {
            if (botaoJogar.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Jogar");
                telaSelecaoDuracao.abrir();
                estadoAtual = Estado.SELECAO_DURACAO;
                return;
            }
            if (botaoCalibrar.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Calibrar sensor");
                telaCalibracao.abrir();
                estadoAtual = Estado.CALIBRANDO;
            }
            if (botaoSair.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.exit();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && estadoAtual == Estado.MENU) {
            Gdx.app.exit();
        }
    }

    private void reiniciarPartida() {
        tempoRestante = duracaoPartida;
        fase = 1;
        tempoAviso = DURACAO_AVISO_FASE;
        motivoFim = "";
        carro.reiniciar();
        rua.reiniciar();
        rua.setVelocidade(VELOCIDADES_RUA[0]);
        pista.reiniciar();
        usouSensor = false;
        maiorInclinacaoEsquerda = 0f;
        maiorInclinacaoDireita = 0f;
    }

    private void encerrarPartida(String motivo) {
        motivoFim = motivo;
        int base = calcularPontos();
        int bonusConsistencia = calcularAproveitamento();
        int bonusVelocidade = fase * BONUS_VELOCIDADE_POR_FASE;
        int total = base + bonusConsistencia + bonusVelocidade;
        telaResultados.abrir(motivoFim, "Horizontal", duracaoPartida, total, base, bonusConsistencia,
                bonusVelocidade, pista.getMoedas(), pista.getMoedasPerdidas(), pista.getBatidas(), MAX_BATIDAS,
                fase, FASES, duracaoPartida - tempoRestante, usouSensor,
                maiorInclinacaoEsquerda, maiorInclinacaoDireita);
        estadoAtual = Estado.RESULTADOS;
    }

    private int calcularAproveitamento() {
        int total = pista.getMoedas() + pista.getMoedasPerdidas();
        if (total == 0) {
            return 0;
        }
        return Math.round(100f * pista.getMoedas() / total);
    }

    private int calcularPontos() {
        int pontos = pista.getMoedas() * PONTOS_MOEDA - pista.getBatidas() * PONTOS_BATIDA;
        return Math.max(0, pontos);
    }

    private void desenharPlacar() {
        float largura = 1.9f;
        float altura = 0.42f;
        float xEsq = 0.15f;
        float xDir = viewport.getWorldWidth() - 0.15f - largura;
        float linha1 = viewport.getWorldHeight() - 0.15f - altura - 0.12f - altura;
        float linha2 = linha1 - 0.12f - altura;

        int totalSegundos = (int) Math.ceil(tempoRestante);
        String tempo = String.format(Locale.ROOT, "Tempo  %02d:%02d", totalSegundos / 60, totalSegundos % 60);
        int vidas = Math.max(0, MAX_BATIDAS - pista.getBatidas());

        desenharPilula(xEsq, linha1, largura, altura, "Fase  " + fase + "/" + FASES, Color.WHITE);
        desenharPilula(xEsq, linha2, largura, altura, "Pontos  " + calcularPontos(), Ui.AMARELO);
        desenharPilula(xDir, linha1, largura, altura, tempo, tempoRestante <= 10f ? Ui.VERMELHO : Color.WHITE);
        desenharPilula(xDir, linha2, largura, altura, "Vidas  " + vidas, vidas <= 1 ? Ui.VERMELHO : Color.WHITE);

        if (tempoAviso > 0f) {
            Ui.texto(spriteBatch, font, "FASE " + fase, 0, 3.0f, viewport.getWorldWidth(), Align.center,
                    0.04f, Ui.AMARELO, false);
        }
    }

    private void desenharPilula(float x, float y, float largura, float altura, String texto, Color cor) {
        Ui.pilula(spriteBatch, false, x, y, largura, altura);
        Ui.textoLinha(spriteBatch, font, texto, x, y, largura, altura, Align.center, 0.0165f, cor);
    }

    private void sairDaCalibracao() {
        telaCalibracao.fechar();
        if (recalibrandoDaPausa) {
            recalibrandoDaPausa = false;
            telaPausa.abrir();
            estadoAtual = Estado.PAUSA;
        } else {
            estadoAtual = Estado.MENU;
        }
    }

    private void logic() {
        sensorClient.update(Gdx.graphics.getDeltaTime());
        float dt = Gdx.graphics.getDeltaTime();

        if (estadoAtual == Estado.JOGANDO) {
            rua.update(dt);
            if (sensorClient.sensorPronto()) {
                float angulo = sensorClient.getAngX();
                carro.seguirInclinacao(angulo, dt);
                usouSensor = true;
                maiorInclinacaoDireita = Math.max(maiorInclinacaoDireita, angulo);
                maiorInclinacaoEsquerda = Math.max(maiorInclinacaoEsquerda, -angulo);
            }
            carro.update(dt);
            pista.update(dt, carro.getBounds());

            tempoRestante -= dt;
            if (tempoAviso > 0f) {
                tempoAviso -= dt;
            }

            float decorrido = duracaoPartida - tempoRestante;
            int novaFase = 1 + (int) (FASES * decorrido / duracaoPartida);
            if (novaFase > FASES) {
                novaFase = FASES;
            }
            if (novaFase != fase) {
                fase = novaFase;
                pista.setFase(fase);
                rua.setVelocidade(VELOCIDADES_RUA[fase - 1]);
                tempoAviso = DURACAO_AVISO_FASE;
            }

            if (pista.getBatidas() >= MAX_BATIDAS) {
                encerrarPartida("Você bateu " + MAX_BATIDAS + " vezes");
            } else if (tempoRestante <= 0f) {
                tempoRestante = 0f;
                encerrarPartida("Tempo esgotado");
            }
        }

        if (estadoAtual == Estado.PREPARACAO) {
            telaPreparacao.update(Gdx.graphics.getDeltaTime());
            if (telaPreparacao.isIniciarSolicitado()) {
                reiniciarPartida();
                estadoAtual = Estado.JOGANDO;
                telaPreparacao.fechar();
            }
        }

        if (estadoAtual == Estado.CALIBRANDO) {
            telaCalibracao.update(Gdx.graphics.getDeltaTime());
            if (telaCalibracao.isVoltarSolicitado()) {
                sairDaCalibracao();
            }
        }
    }

    private void draw() {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        if (estadoAtual == Estado.CALIBRANDO) {
            telaCalibracao.draw(spriteBatch);
            return;
        }

        if (estadoAtual == Estado.SELECAO_DURACAO) {
            telaSelecaoDuracao.draw(spriteBatch);
            return;
        }

        if (estadoAtual == Estado.PREPARACAO) {
            telaPreparacao.draw(spriteBatch);
            return;
        }

        if (estadoAtual == Estado.JOGANDO || estadoAtual == Estado.PAUSA) {
            spriteBatch.begin();
            rua.draw(spriteBatch);
            pista.draw(spriteBatch);
            carro.draw(spriteBatch);
            Ui.hud(spriteBatch, font, viewport.getWorldWidth(), viewport.getWorldHeight(),
                    sensorClient.sensorPronto(), pista.getMoedas(), pista.getBatidas());
            desenharPlacar();
            spriteBatch.end();
            if (estadoAtual == Estado.PAUSA) {
                telaPausa.draw(spriteBatch);
            }
            return;
        }

        if (estadoAtual == Estado.RESULTADOS) {
            telaResultados.draw(spriteBatch);
            return;
        }

        spriteBatch.begin();
        font.draw(spriteBatch, "RehabCar", 0, 4.8f, viewport.getWorldWidth(), Align.center, false);
        botaoJogar.draw(spriteBatch);
        botaoCalibrar.draw(spriteBatch);
        botaoSair.draw(spriteBatch);
        spriteBatch.end();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        sensorClient.parar();
        telaCalibracao.dispose();
        rua.dispose();
        pista.dispose();
        spriteBatch.dispose();
        font.dispose();
        uiAssets.dispose();
        Ui.dispose();
        carroTexture.dispose();
    }
}