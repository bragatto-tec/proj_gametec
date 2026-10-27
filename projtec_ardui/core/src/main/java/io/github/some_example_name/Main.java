package io.github.some_example_name;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
        SELECAO_MODO,
        PREPARACAO,
        PAUSA,
        JOGANDO,
        FIM
    }

    // Carro e controle por vias
    private static final float CARRO_LARGURA = 0.72f;
    private static final float CARRO_Y = 0.35f;
    private static final float ANGULO_ENTRADA_FAIXA = 5f;
    private static final float ANGULO_SAIDA_FAIXA = 3f;
    private static final float FILTRO_INCLINACAO_SEGUNDOS = 0.12f;
    private static final float RESPOSTA_DIRECAO = 12f;

    // Regras da partida
    private static final int MAX_BATIDAS = 3;
    private static final int PONTOS_MOEDA = 10;
    private static final int PONTOS_BATIDA = 5;
    private static final int FASES = 3;
    private static final float DURACAO_AVISO_FASE = 2f;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private BitmapFont font;
    private UiAssets uiAssets;
    private Texture carroTexture;
    private TextureRegion carroSprite;
    private TextureRegion[][] quadrosCarro;
    private float tempoAnimacaoCarro;
    private Vector2 touchPos;
    private Rua rua;
    private GerenciadorPista pista;
    private float carroX;
    private int viaAtual = GerenciadorPista.VIA_CENTRO;
    private float inclinacaoFiltrada;
    private boolean inclinacaoInicializada;

    private SensorClient sensorClient;
    private TelaCalibracao telaCalibracao;
    private TelaSelecaoModo telaSelecaoModo;
    private TelaPreparacao telaPreparacao;
    private TelaPausa telaPausa;
    private boolean recalibrandoDaPausa;
    private int duracaoPartida = 60;
    private float tempoRestante;
    private int fase = 1;
    private float tempoAviso;
    private String motivoFim = "";
    private Estado estadoAtual;

    private Button botaoJogar;
    private Button botaoCalibrar;
    private Button botaoConfig;
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
        // A ultima linha da folha e o efeito de fogo, nao o carro normal.
        // split() entrega as linhas na ordem visual, de cima para baixo.
        quadrosCarro = TextureRegion.split(carroTexture,
                carroTexture.getWidth() / 4, carroTexture.getHeight() / 5);
        carroSprite = quadrosCarro[0][0];
        touchPos = new Vector2();
        rua = new Rua(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight(), 24f);
        pista = new GerenciadorPista();
        carroX = GerenciadorPista.xDaVia(viaAtual, 1f);

        sensorClient = sensorBaseUrl == null ? new SensorClient() : new SensorClient(sensorBaseUrl);
        telaCalibracao = new TelaCalibracao(viewport, font, uiAssets, sensorClient);
        telaSelecaoModo = new TelaSelecaoModo(viewport, font, uiAssets);
        telaPreparacao = new TelaPreparacao(viewport, font, uiAssets, sensorClient);
        telaPausa = new TelaPausa(viewport, font, uiAssets);
        estadoAtual = Estado.MENU;

        botaoJogar = new Button(uiAssets, "jogar", 2.5f, 3.0f, 3, 0.7f);
        botaoCalibrar = new Button(uiAssets, "calibrar_sensor", 2.5f, 2.1f, 3, 0.7f);
        botaoConfig = new Button(uiAssets, "configuracoes", 2.5f, 1.2f, 3, 0.7f);
        botaoSair = new Button(uiAssets, "sair", 2.5f, 0.3f, 3, 0.7f);
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
            botaoConfig.atualizarEstado(touchPos);
            botaoSair.atualizarEstado(touchPos);
        }
        if (estadoAtual == Estado.CALIBRANDO) {
            if (telaCalibracao.input()) {
                sairDaCalibracao();
            }
            return;
        }

        if (estadoAtual == Estado.SELECAO_MODO) {
            telaSelecaoModo.input();
            if (telaSelecaoModo.isVoltarSolicitado()) {
                estadoAtual = Estado.MENU;
                telaSelecaoModo.fechar();
            } else if (telaSelecaoModo.isSelecionarSolicitado()) {
                Gdx.app.log("RehabCar", "Modo escolhido: " + telaSelecaoModo.getNomeModo());
                duracaoPartida = telaSelecaoModo.getDuracaoSegundos();
                telaPreparacao.abrir(telaSelecaoModo.getModoEscolhido(), telaSelecaoModo.getNomeModo(), duracaoPartida);
                estadoAtual = Estado.PREPARACAO;
                telaSelecaoModo.fechar();
            }
            return;
        }

        if (estadoAtual == Estado.PREPARACAO) {
            telaPreparacao.input();
            if (telaPreparacao.isVoltarSolicitado()) {
                telaSelecaoModo.abrir();
                estadoAtual = Estado.SELECAO_MODO;
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
                    viaAtual = Math.max(GerenciadorPista.VIA_ESQUERDA, viaAtual - 1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                    viaAtual = Math.min(GerenciadorPista.VIA_DIREITA, viaAtual + 1);
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

        if (estadoAtual == Estado.FIM) {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                estadoAtual = Estado.MENU;
            }
            return;
        }

        if (Gdx.input.justTouched()) {
            if (botaoJogar.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Jogar");
                telaSelecaoModo.abrir();
                estadoAtual = Estado.SELECAO_MODO;
                return;
            }
            if (botaoCalibrar.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Calibrar sensor");
                telaCalibracao.abrir();
                estadoAtual = Estado.CALIBRANDO;
            }
            if (botaoConfig.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Configuracoes");
            }
            if (botaoSair.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.exit();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && estadoAtual == Estado.MENU) {
            Gdx.app.exit();
        }
    }

    private float alturaDoCarro() {
        return CARRO_LARGURA * carroSprite.getRegionHeight() / carroSprite.getRegionWidth();
    }

    private void reiniciarPartida() {
        viaAtual = GerenciadorPista.VIA_CENTRO;
        inclinacaoInicializada = false;
        tempoRestante = duracaoPartida;
        fase = 1;
        tempoAviso = DURACAO_AVISO_FASE;
        motivoFim = "";
        tempoAnimacaoCarro = 0f;
        carroSprite = quadrosCarro[0][0];
        carroX = GerenciadorPista.xDaVia(viaAtual, 1f);
        rua.reiniciar();
        pista.reiniciar();
    }

    /** Inclina para a faixa lateral; ao voltar quase ao nivel, retorna ao centro. */
    private int viaPelaInclinacao(int via, float angX) {
        if (angX >= ANGULO_ENTRADA_FAIXA) return GerenciadorPista.VIA_DIREITA;
        if (angX <= -ANGULO_ENTRADA_FAIXA) return GerenciadorPista.VIA_ESQUERDA;

        if (via == GerenciadorPista.VIA_DIREITA && angX > ANGULO_SAIDA_FAIXA) {
            return GerenciadorPista.VIA_DIREITA;
        }
        if (via == GerenciadorPista.VIA_ESQUERDA && angX < -ANGULO_SAIDA_FAIXA) {
            return GerenciadorPista.VIA_ESQUERDA;
        }
        return GerenciadorPista.VIA_CENTRO;
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
            tempoAnimacaoCarro += dt;
            rua.update(dt);
            if (sensorClient.sensorPronto()) {
                float leitura = sensorClient.getAngX();
                if (!inclinacaoInicializada) {
                    inclinacaoFiltrada = leitura;
                    inclinacaoInicializada = true;
                } else {
                    // Suaviza ruído do MPU entre leituras sem atrasar demais a resposta.
                    float peso = 1f - (float) Math.exp(-dt / FILTRO_INCLINACAO_SEGUNDOS);
                    inclinacaoFiltrada += (leitura - inclinacaoFiltrada) * peso;
                }
                viaAtual = viaPelaInclinacao(viaAtual, inclinacaoFiltrada);
            }
            // As tres primeiras linhas mostram o carro reto ou inclinado.
            // Alternar as quatro colunas anima as rodas e a carroceria.
            int linha = viaAtual < GerenciadorPista.VIA_CENTRO ? 1
                    : viaAtual > GerenciadorPista.VIA_CENTRO ? 2 : 0;
            int coluna = (int) (tempoAnimacaoCarro * 8f) % quadrosCarro[linha].length;
            carroSprite = quadrosCarro[linha][coluna];
            // Cada inclinacao seleciona exatamente uma das tres faixas.
            float alvoX = GerenciadorPista.xDaVia(viaAtual, 1f);
            float pesoDirecao = 1f - (float) Math.exp(-RESPOSTA_DIRECAO * dt);
            carroX += (alvoX - carroX) * pesoDirecao;

            pista.update(dt, carroX, CARRO_Y, CARRO_LARGURA, alturaDoCarro());

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
                tempoAviso = DURACAO_AVISO_FASE;
            }

            if (pista.getBatidas() >= MAX_BATIDAS) {
                motivoFim = "Você bateu " + MAX_BATIDAS + " vezes";
                estadoAtual = Estado.FIM;
            } else if (tempoRestante <= 0f) {
                tempoRestante = 0f;
                motivoFim = "Tempo esgotado";
                estadoAtual = Estado.FIM;
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

        if (estadoAtual == Estado.SELECAO_MODO) {
            telaSelecaoModo.draw(spriteBatch);
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
            spriteBatch.draw(carroSprite, carroX - CARRO_LARGURA / 2f, CARRO_Y,
                    CARRO_LARGURA, alturaDoCarro());
            Ui.hud(spriteBatch, font, viewport.getWorldWidth(), viewport.getWorldHeight(),
                    sensorClient.sensorPronto(), pista.getMoedas(), pista.getBatidas());
            desenharPlacar();
            spriteBatch.end();
            if (estadoAtual == Estado.PAUSA) {
                telaPausa.draw(spriteBatch);
            }
            return;
        }

        if (estadoAtual == Estado.FIM) {
            float largura = viewport.getWorldWidth();
            spriteBatch.begin();
            Ui.fundo(spriteBatch, largura, viewport.getWorldHeight());
            Ui.texto(spriteBatch, font, "FIM DE PARTIDA", 0, 4.6f, largura, Align.center, 0.025f, Color.WHITE, false);
            Ui.texto(spriteBatch, font, motivoFim, 0, 4.0f, largura, Align.center, 0.0165f, Ui.MUTED, false);
            Ui.painel(spriteBatch, Ui.Estilo.NEUTRO, 2.0f, 1.65f, 4.0f, 1.9f);
            Ui.texto(spriteBatch, font, "Pontos: " + calcularPontos(), 2.5f, 3.3f, 3.2f, Align.left, 0.02f, Ui.AMARELO, false);
            spriteBatch.draw(Ui.moeda(), 2.5f, 2.45f, 0.5f, 0.5f);
            Ui.texto(spriteBatch, font, "Moedas: " + pista.getMoedas(), 3.2f, 2.9f, 2.5f, Align.left, 0.0165f, Color.WHITE, false);
            spriteBatch.draw(Ui.cone(), 2.5f, 1.85f, 0.5f, 0.5f);
            Ui.texto(spriteBatch, font, "Batidas: " + pista.getBatidas(), 3.2f, 2.3f, 2.5f, Align.left, 0.0165f, Color.WHITE, false);
            Ui.texto(spriteBatch, font, "Clique para voltar ao menu", 0, 1.1f, largura, Align.center, 0.0135f, Ui.MUTED, false);
            spriteBatch.end();
            return;
        }

        spriteBatch.begin();
        font.draw(spriteBatch, "RehabCar", 0, 4.8f, viewport.getWorldWidth(), Align.center, false);
        botaoJogar.draw(spriteBatch);
        botaoCalibrar.draw(spriteBatch);
        botaoConfig.draw(spriteBatch);
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