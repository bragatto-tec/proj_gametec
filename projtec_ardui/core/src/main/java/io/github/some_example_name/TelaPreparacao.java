package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaPreparacao {
    private static final float DURACAO_CONTAGEM = 3f;

    private final FitViewport viewport;
    private final BitmapFont font;
    private final Vector2 touchPos;
    private final SensorClient sensorClient;

    private final Button botaoIniciar;
    private final Button botaoVoltar;

    private int modo;
    private String nomeModo;
    private boolean contando;
    private float tempoContagem;
    private boolean voltarSolicitado;
    private boolean iniciarSolicitado;

    public TelaPreparacao(FitViewport viewport, BitmapFont font, UiAssets uiAssets, SensorClient sensorClient) {
        this.viewport = viewport;
        this.font = font;
        this.sensorClient = sensorClient;
        this.touchPos = new Vector2();

        this.botaoVoltar = new Button(uiAssets, "voltar", 1.0f, 0.2f, 2.5f, 0.6f);
        this.botaoIniciar = new Button(uiAssets, "iniciar", 4.5f, 0.2f, 2.5f, 0.6f);

        this.nomeModo = "nenhum";
    }

    public void abrir(int modo, String nomeModo) {
        this.modo = modo;
        this.nomeModo = nomeModo;
        contando = false;
        tempoContagem = DURACAO_CONTAGEM;
        voltarSolicitado = false;
        iniciarSolicitado = false;
        botaoIniciar.setHabilitado(true);
        botaoVoltar.setHabilitado(true);
    }

    public void fechar() {
        contando = false;
        voltarSolicitado = false;
        iniciarSolicitado = false;
    }

    public void input() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPos);
        botaoVoltar.atualizarEstado(touchPos);
        botaoIniciar.atualizarEstado(touchPos);
        if (contando) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            voltarSolicitado = true;
            return;
        }

        if (Gdx.input.justTouched()) {
            if (botaoVoltar.foiClicado(touchPos.x, touchPos.y)) {
                voltarSolicitado = true;
            }
            if (botaoIniciar.foiClicado(touchPos.x, touchPos.y)) {
                contando = true;
                tempoContagem = DURACAO_CONTAGEM;
                botaoIniciar.setHabilitado(false);
                botaoVoltar.setHabilitado(false);
            }
        }
    }

    public void update(float dt) {
        if (contando) {
            tempoContagem -= dt;
            if (tempoContagem <= 0) {
                contando = false;
                iniciarSolicitado = true;
            }
        }
    }

    public boolean isVoltarSolicitado() {
        return voltarSolicitado;
    }

    public boolean isIniciarSolicitado() {
        return iniciarSolicitado;
    }

    private String textoControles() {
        if (modo == TelaSelecaoModo.VERTICAL) {
            return "Dorsiflexao e flexao plantar: o carro sobe e desce na pista.";
        }
        if (modo == TelaSelecaoModo.HORIZONTAL) {
            return "Inversao e eversao: o carro troca de faixa (esquerda e direita).";
        }
        return "Os dois eixos ao mesmo tempo, com mais controle.";
    }

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        font.draw(batch, "PREPARAR PARTIDA", 0, 4.8f, viewport.getWorldWidth(), Align.center, false);
        font.draw(batch, "Modo escolhido: " + nomeModo, 0, 4.25f, viewport.getWorldWidth(), Align.center, false);

        font.getData().setScale(0.016f);
        font.draw(batch, "Objetivo: leve o carro pela pista, desvie dos cones e colete as moedas ate o fim do tempo.",
                0.6f, 3.65f, 6.8f, Align.left, true);
        font.draw(batch, "Controles: " + textoControles(), 0.6f, 2.85f, 6.8f, Align.left, true);

        String conexao = sensorClient.conectado() ? "ESP32 conectado" : "ESP32 nao conectado";
        String calibracao = sensorClient.calibracaoConcluidaComSucesso() ? "Calibracao concluida" : "Calibracao necessaria";
        font.draw(batch, conexao + "   |   " + calibracao, 0.6f, 2.0f, 6.8f, Align.left, true);
        font.getData().setScale(0.02f);

        if (contando) {
            int numero = (int) Math.ceil(tempoContagem);
            font.getData().setScale(0.05f);
            font.draw(batch, String.valueOf(numero), 0, 1.5f, viewport.getWorldWidth(), Align.center, false);
            font.getData().setScale(0.02f);
        }

        botaoVoltar.draw(batch);
        botaoIniciar.draw(batch);
        batch.end();
    }
}
