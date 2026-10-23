package io.github.some_example_name;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

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
    private static final float INCLINACAO_MAX = 30f;      // graus que o sensor considera "maximo"
    private static final float LIMIAR_TROCA = 0.35f;      // fracao da inclinacao para sair do centro
    private static final float LIMIAR_VOLTA = 0.20f;      // fracao para voltar ao centro (histerese)

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

    private SensorClient sensorClient;
    private TelaCalibracao telaCalibracao;
    private TelaSelecaoModo telaSelecaoModo;
    private TelaPreparacao telaPreparacao;
    private TelaPausa telaPausa;
    private boolean recalibrandoDaPausa;
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
                telaPreparacao.abrir(telaSelecaoModo.getModoEscolhido(), telaSelecaoModo.getNomeModo());
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
        tempoAnimacaoCarro = 0f;
        carroSprite = quadrosCarro[0][0];
        carroX = GerenciadorPista.xDaVia(viaAtual, 1f);
        rua.reiniciar();
        pista.reiniciar();
    }

    /**
     * Converte a inclinacao do sensor (graus, negativo = esquerda) em uma via.
     * Usa histerese: precisa inclinar mais para sair do centro do que para voltar,
     * assim o carro nao fica "tremendo" entre vias quando o angulo oscila perto do limite.
     */
    private int viaPelaInclinacao(int via, float angX) {
        float n = Math.max(-INCLINACAO_MAX, Math.min(INCLINACAO_MAX, angX)) / INCLINACAO_MAX;
        if (via == GerenciadorPista.VIA_ESQUERDA && n > -LIMIAR_VOLTA) {
            via = GerenciadorPista.VIA_CENTRO;
        } else if (via == GerenciadorPista.VIA_DIREITA && n < LIMIAR_VOLTA) {
            via = GerenciadorPista.VIA_CENTRO;
        }
        if (via == GerenciadorPista.VIA_CENTRO) {
            if (n < -LIMIAR_TROCA) via = GerenciadorPista.VIA_ESQUERDA;
            else if (n > LIMIAR_TROCA) via = GerenciadorPista.VIA_DIREITA;
        }
        return via;
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
                viaAtual = viaPelaInclinacao(viaAtual, sensorClient.getAngX());
            }
            // As tres primeiras linhas mostram o carro reto ou inclinado.
            // Alternar as quatro colunas anima as rodas e a carroceria.
            int linha = viaAtual < GerenciadorPista.VIA_CENTRO ? 1
                    : viaAtual > GerenciadorPista.VIA_CENTRO ? 2 : 0;
            int coluna = (int) (tempoAnimacaoCarro * 8f) % quadrosCarro[linha].length;
            carroSprite = quadrosCarro[linha][coluna];
            // o carro desliza suavemente ate o centro da via escolhida
            float alvoX = GerenciadorPista.xDaVia(viaAtual, 1f);
            carroX += (alvoX - carroX) * Math.min(1f, dt * 8f);

            pista.update(dt, carroX, CARRO_Y, CARRO_LARGURA, alturaDoCarro());
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
            spriteBatch.end();
            if (estadoAtual == Estado.PAUSA) {
                telaPausa.draw(spriteBatch);
            }
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
