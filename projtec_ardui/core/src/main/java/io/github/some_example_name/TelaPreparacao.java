package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
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

    private int duracaoSegundos;
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

    }

    public void abrir(int duracaoSegundos) {
        this.duracaoSegundos = duracaoSegundos;
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
        return "Inversão e eversão: incline o pé para os lados e o carro troca de faixa.";
    }

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float largura = viewport.getWorldWidth();

        batch.begin();
        Ui.fundo(batch, largura, viewport.getWorldHeight());
        Ui.texto(batch, font, "PREPARAR PARTIDA", 0, 4.88f, largura, Align.center, 0.023f, Color.WHITE, false);

        Ui.pilula(batch, true, 2.1f, 4.0f, 3.8f, 0.42f);
        Ui.textoLinha(batch, font, "Modo: Horizontal - " + (duracaoSegundos / 60) + " min",
                2.1f, 4.0f, 3.8f, 0.42f, Align.center, 0.0165f, Ui.AMARELO);

        // objetivo
        Ui.painel(batch, Ui.Estilo.NEUTRO, 0.5f, 2.72f, 7.0f, 1.1f);
        batch.draw(Ui.moeda(), 0.78f, 2.98f, 0.5f, 0.5f);
        batch.draw(Ui.cone(), 1.32f, 2.96f, 0.54f, 0.54f);
        Ui.texto(batch, font, "OBJETIVO", 2.1f, 3.68f, 5.2f, Align.left, 0.011f, Ui.AMARELO, false);
        Ui.texto(batch, font, "Leve o carro pela pista, desvie dos cones e colete as moedas até o fim do tempo.",
                2.1f, 3.46f, 5.2f, Align.left, 0.0135f, Color.WHITE, true);

        // controles
        Ui.painel(batch, Ui.Estilo.NEUTRO, 0.5f, 1.48f, 7.0f, 1.1f);
        batch.draw(Ui.iconeModo(), 0.82f, 1.76f, 0.54f, 0.54f);
        Ui.texto(batch, font, "CONTROLES", 2.1f, 2.44f, 5.2f, Align.left, 0.011f, Ui.AMARELO, false);
        Ui.texto(batch, font, textoControles(), 2.1f, 2.22f, 5.2f, Align.left, 0.0135f, Color.WHITE, true);

        // status
        boolean esp = sensorClient.conectado();
        boolean cal = sensorClient.calibracaoConcluidaComSucesso();
        desenharChip(batch, 0.5f, esp ? "ESP32 conectado" : "ESP32 não conectado", esp ? Ui.VERDE : Ui.VERMELHO);
        desenharChip(batch, 4.1f, cal ? "Calibração concluída" : "Calibração necessária", cal ? Ui.VERDE : Ui.AMARELO);

        botaoVoltar.draw(batch);
        botaoIniciar.draw(batch);

        if (contando) {
            desenharContagem(batch, largura);
        }
        batch.end();
    }

    private void desenharChip(SpriteBatch batch, float x, String texto, Color cor) {
        Ui.pilula(batch, false, x, 0.92f, 3.4f, 0.42f);
        Ui.led(batch, cor, x + 0.3f, 1.13f, 0.26f);
        Ui.textoLinha(batch, font, texto, x + 0.55f, 0.92f, 2.75f, 0.42f, Align.left, 0.0135f, Color.WHITE);
    }

    private void desenharContagem(SpriteBatch batch, float largura) {
        Ui.retangulo(batch, 0, 0, largura, viewport.getWorldHeight(), new Color(0f, 0.02f, 0.06f, 0.78f));
        float fracao = tempoContagem - (float) Math.floor(tempoContagem); // 1 -> 0 em cada segundo
        float pulso = 1f + 0.12f * fracao;
        int numero = Math.max(1, (int) Math.ceil(tempoContagem));

        Ui.texto(batch, font, "PREPARE-SE!", 0, 4.2f, largura, Align.center, 0.02f, Color.WHITE, false);
        Ui.anel(batch, Ui.AMARELO, largura / 2f, 2.5f, 2.3f * pulso);
        Ui.textoLinha(batch, font, String.valueOf(numero), 0, 2.5f - 1f, largura, 2f, Align.center, 0.07f * pulso, Ui.AMARELO);
        Ui.texto(batch, font, "Mantenha-se parado e olhe para a pista", 0, 1.0f, largura, Align.center, 0.0135f, Ui.MUTED, false);
    }
}
