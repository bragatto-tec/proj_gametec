package io.github.some_example_name;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaCalibracao {
    private static final float TOLERANCIA_NIVEL = 3f; // graus considerados "nivelado"

    private final FitViewport viewport;
    private final BitmapFont font;
    private final UiAssets uiAssets;
    private final SensorClient sensorClient;
    private final ShapeRenderer shapeRenderer;
    private final Button botaoIniciar;
    private final Button botaoVoltar;
    private final Vector2 touchPos;

    private boolean voltarSolicitado;

    public TelaCalibracao(FitViewport viewport, BitmapFont font, UiAssets uiAssets, SensorClient sensorClient) {
        this.viewport = viewport;
        this.font = font;
        this.uiAssets = uiAssets;
        this.sensorClient = sensorClient;
        this.shapeRenderer = new ShapeRenderer();
        this.touchPos = new Vector2();

        this.botaoIniciar = new Button(uiAssets, "iniciar_calibracao", 1.2f, 0.15f, 2.8f, 0.6f);
        this.botaoVoltar = new Button(uiAssets, "voltar_calib", 4.3f, 0.15f, 2.0f, 0.6f);
        this.botaoIniciar.setHabilitado(false);
        this.botaoVoltar.setHabilitado(true);
    }

    public void abrir() {
        voltarSolicitado = false;
        sensorClient.limparResultado();
        atualizarBotoes();
    }

    public void fechar() {
        voltarSolicitado = false;
        atualizarBotoes();
    }

    public void update(float dt) {
        sensorClient.update(dt);
        atualizarBotoes();
    }

    public boolean input() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPos);
        botaoIniciar.atualizarEstado(touchPos);
        botaoVoltar.atualizarEstado(touchPos);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !sensorClient.calibrando()) {
            voltarSolicitado = true;
            return true;
        }

        if (Gdx.input.justTouched()) {
            if (botaoIniciar.foiClicado(touchPos.x, touchPos.y)) {
                if (sensorClient.sensorPronto() && !sensorClient.calibrando()) {
                    sensorClient.iniciarCalibracao();
                }
                return false;
            }

            if (botaoVoltar.foiClicado(touchPos.x, touchPos.y)) {
                if (!sensorClient.calibrando()) {
                    voltarSolicitado = true;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isVoltarSolicitado() {
        return voltarSolicitado;
    }

    // ------------------------------------------------------------------ desenho

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        float largura = viewport.getWorldWidth();

        // 1) fundo, titulo e paineis
        batch.begin();
        Ui.fundo(batch, largura, viewport.getWorldHeight());
        Ui.texto(batch, font, "CALIBRAÇÃO DO SENSOR", 0, 4.88f, largura, Align.center, 0.023f, Color.WHITE, false);
        Ui.texto(batch, font, "Apoie o sensor em superfície plana e mantenha-o imóvel",
                0.3f, 4.5f, 7.4f, Align.center, 0.0125f, Ui.MUTED, false);

        String url = sensorClient.getUrlAtual();
        Color corConexao = sensorClient.conectado() ? Ui.VERDE : Ui.VERMELHO;
        Color corSensor = !sensorClient.conectado() ? Ui.VERMELHO : (sensorClient.sensorPronto() ? Ui.VERDE : Ui.AMARELO);
        desenharCartaoStatus(batch, 0.4f, "ESP32", sensorClient.conectado() ? "Conectado" : "Desconectado",
                url.isEmpty() ? "Buscando USB / Wi-Fi..." : sensorClient.getEnderecoAmigavel(), corConexao);
        desenharCartaoStatus(batch, 4.1f, "MPU6050", sensorClient.sensorPronto() ? "Leitura OK" : "Sem leitura",
                sensorClient.sensorPronto() ? "Sensor respondendo" : "Aguardando dados", corSensor);

        Ui.painel(batch, Ui.Estilo.NEUTRO, 0.4f, 1.72f, 3.5f, 1.46f);
        Ui.painel(batch, Ui.Estilo.NEUTRO, 4.1f, 1.72f, 3.5f, 1.46f);
        Ui.texto(batch, font, "NIVELAMENTO", 0.62f, 3.08f, 3.0f, Align.left, 0.011f, Ui.MUTED, false);
        Ui.texto(batch, font, "LEITURAS", 4.32f, 3.08f, 3.0f, Align.left, 0.011f, Ui.MUTED, false);
        Ui.painel(batch, estiloMensagem(), 0.4f, 1.14f, 7.2f, 0.48f);
        batch.end();

        // 2) alvo (formas)
        desenharAlvo();

        // 3) textos e controles por cima
        batch.begin();
        desenharLeituras(batch);
        desenharMensagem(batch);
        desenharProgresso(batch);
        botaoIniciar.draw(batch);
        botaoVoltar.draw(batch);
        batch.end();
    }

    private void desenharCartaoStatus(SpriteBatch batch, float x, String rotulo, String estado, String detalhe, Color cor) {
        float y = 3.28f;
        Ui.painel(batch, Ui.Estilo.NEUTRO, x, y, 3.5f, 0.9f);
        Ui.led(batch, cor, x + 0.38f, y + 0.45f, 0.34f);
        Ui.texto(batch, font, rotulo, x + 0.72f, y + 0.8f, 2.6f, Align.left, 0.011f, Ui.MUTED, false);
        Ui.texto(batch, font, estado, x + 0.72f, y + 0.6f, 2.6f, Align.left, 0.019f, Color.WHITE, false);
        Ui.texto(batch, font, detalhe, x + 0.72f, y + 0.28f, 2.65f, Align.left, 0.0095f, Ui.MUTED, false);
    }

    private boolean nivelado() {
        return sensorClient.sensorPronto()
                && Math.abs(sensorClient.getAngY()) < TOLERANCIA_NIVEL;
    }

    private void desenharAlvo() {
        float centroX = 2.15f;
        float centroY = 2.4f;
        float raio = 0.62f;
        boolean ok = nivelado();
        Color corPonto = ok ? Ui.VERDE : Ui.AZUL;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.04f, 0.09f, 0.18f, 0.9f);
        shapeRenderer.circle(centroX, centroY, raio + 0.06f, 48);
        if (ok) {
            shapeRenderer.setColor(Ui.VERDE.r, Ui.VERDE.g, Ui.VERDE.b, 0.22f);
            shapeRenderer.circle(centroX, centroY, raio / 3f, 32);
        }
        shapeRenderer.end();

        Gdx.gl.glLineWidth(2f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.79f, 0.84f, 0.94f, 0.55f);
        for (int i = 1; i <= 3; i++) {
            shapeRenderer.circle(centroX, centroY, raio * i / 3f, 48);
        }
        shapeRenderer.setColor(0.79f, 0.84f, 0.94f, 0.30f);
        shapeRenderer.line(centroX - raio - 0.06f, centroY, centroX + raio + 0.06f, centroY);
        shapeRenderer.line(centroX, centroY - raio - 0.06f, centroX, centroY + raio + 0.06f);
        shapeRenderer.end();

        // ponto (limitado ao disco)
        float dx = (sensorClient.getAngY() / 45f) * raio * 0.85f;
        float dy = 0f;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float maximo = raio * 0.95f;
        if (dist > maximo) {
            dx *= maximo / dist;
            dy *= maximo / dist;
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(corPonto.r, corPonto.g, corPonto.b, 0.25f);
        shapeRenderer.circle(centroX + dx, centroY + dy, 0.15f, 24);
        shapeRenderer.setColor(corPonto);
        shapeRenderer.circle(centroX + dx, centroY + dy, 0.08f, 24);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1f);
    }

    private void desenharLeituras(SpriteBatch batch) {
        float x = 4.32f;
        float largura = 3.06f;
        float[] topos = {2.8f, 2.42f, 2.04f};
        String[] rotulos = {"Inclinação Y", "Temperatura"};
        String[] valores = {
                String.format(Locale.ROOT, "%.1f°", sensorClient.getAngY()),
                String.format(Locale.ROOT, "%.1f °C", sensorClient.getTemp())};
        for (int i = 0; i < 2; i++) {
            boolean angulo = i == 0;
            float ang = sensorClient.getAngY();
            Color corValor = angulo && sensorClient.sensorPronto() && Math.abs(ang) < TOLERANCIA_NIVEL ? Ui.VERDE : Color.WHITE;
            Ui.textoLinha(batch, font, rotulos[i], x, topos[i] - 0.36f, largura, 0.4f, Align.left, 0.0135f, Ui.MUTED);
            Ui.textoLinha(batch, font, valores[i], x, topos[i] - 0.36f, largura, 0.4f, Align.right, 0.02f, corValor);
            if (i == 0) {
                Ui.retangulo(batch, x, topos[i] - 0.35f, largura, 0.012f, new Color(0.79f, 0.84f, 0.94f, 0.18f));
            }
        }
    }

    private Ui.Estilo estiloMensagem() {
        if (sensorClient.calibracaoFalhou()) return Ui.Estilo.VERMELHO;
        if (sensorClient.calibrando()) return Ui.Estilo.AZUL;
        if (sensorClient.calibracaoConcluidaComSucesso()) return Ui.Estilo.VERDE;
        return Ui.Estilo.NEUTRO;
    }

    private void desenharMensagem(SpriteBatch batch) {
        int progresso = sensorClient.getProgresso();
        String mensagem;
        if (sensorClient.calibrando()) {
            mensagem = sensorClient.getMensagemErro();
            if (mensagem.isEmpty()) {
                mensagem = "Mantenha imóvel... " + progresso + "%";
            }
        } else if (sensorClient.calibracaoFalhou()) {
            mensagem = sensorClient.getMensagemErro();
            if (mensagem.isEmpty()) {
                mensagem = "Reposicione o sensor e tente novamente.";
            }
        } else if (sensorClient.calibracaoConcluidaComSucesso()) {
            mensagem = "Calibração concluída com sucesso!";
        } else if (!sensorClient.sensorPronto() && !sensorClient.getMensagemErro().isEmpty()) {
            mensagem = sensorClient.getMensagemErro();
        } else {
            mensagem = "Pronto para iniciar";
        }
        Ui.textoNaCaixa(batch, font, mensagem, 0.6f, 1.14f, 6.8f, 0.48f, Color.WHITE, 0.0135f, 0.0105f, 0.0085f);
    }

    private void desenharProgresso(SpriteBatch batch) {
        float x = 0.4f;
        float y = 0.84f;
        float largura = 6.5f;
        float fracao = 0f;
        Color cor = Ui.AZUL;
        if (sensorClient.calibrando()) {
            fracao = sensorClient.getProgresso() / 100f;
        } else if (sensorClient.calibracaoFalhou()) {
            fracao = 1f;
            cor = Ui.VERMELHO;
        } else if (sensorClient.calibracaoConcluidaComSucesso()) {
            fracao = 1f;
            cor = Ui.VERDE;
        }
        Ui.barra(batch, x, y, largura, fracao, cor);
        if (sensorClient.calibrando()) {
            Ui.textoLinha(batch, font, sensorClient.getProgresso() + "%", x + largura + 0.1f, y, 0.7f,
                    Ui.BARRA_ALTURA, Align.left, 0.0135f, Color.WHITE);
        }
    }

    private void atualizarBotoes() {
        botaoIniciar.setHabilitado(sensorClient.sensorPronto() && !sensorClient.calibrando());
        botaoVoltar.setHabilitado(!sensorClient.calibrando());
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
