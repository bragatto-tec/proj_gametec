package io.github.some_example_name;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaCalibracao {
    private final FitViewport viewport;
    private final BitmapFont font;
    private final SensorClient sensorClient;
    private final ShapeRenderer shapeRenderer;
    private final Button botaoIniciar;
    private final Button botaoVoltar;
    private final Vector2 touchPos;

    private boolean voltarSolicitado;

    public TelaCalibracao(FitViewport viewport, BitmapFont font, UiAssets uiAssets, SensorClient sensorClient) {
        this.viewport = viewport;
        this.font = font;
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

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        font.draw(batch, "CALIBRACAO DO SENSOR", 0, 4.88f, viewport.getWorldWidth(), Align.center, false);
        desenharTextoAdaptado(batch, "Apoie o sensor em superfície plana e mantenha-o imóvel",
                0.3f, 4.57f, 7.4f, Align.center, 0.014f);
        batch.end();

        desenharStatus();
        desenharAlvo();
        desenharPainelMensagem();
        desenharProgresso();

        batch.begin();
        desenharStatusTexto(batch);
        desenharAlvoTexto(batch);
        desenharProgressoTexto(batch);
        botaoIniciar.draw(batch);
        botaoVoltar.draw(batch);
        batch.end();
    }

    private void desenharStatus() {
        Color corConexao = sensorClient.conectado() ? Color.GREEN : Color.RED;
        Color corSensor = sensorClient.sensorPronto() ? Color.GREEN : Color.YELLOW;

        if (!sensorClient.conectado()) {
            corSensor = Color.RED;
        }

        desenharCaixaStatus(0.4f, 3.48f, 3.45f, 0.82f, corConexao);
        desenharCaixaStatus(4.15f, 3.48f, 3.45f, 0.82f, corSensor);
    }

    private void desenharCaixaStatus(float x, float y, float width, float height, Color cor) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.15f, 0.15f, 0.2f, 1f));
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(cor);
        shapeRenderer.rect(x, y, 0.12f, height);
        shapeRenderer.end();
    }

    private void desenharStatusTexto(SpriteBatch batch) {
        font.draw(batch, "ESP32", 0.62f, 4.17f, 3.1f, Align.left, false);
        font.draw(batch, sensorClient.conectado() ? "Conectado" : "Desconectado",
                0.62f, 3.91f, 3.1f, Align.left, false);
        font.draw(batch, "MPU6050", 4.37f, 4.17f, 3.1f, Align.left, false);
        font.draw(batch, sensorClient.sensorPronto() ? "Leitura OK" : "Sem leitura",
                4.37f, 3.91f, 3.1f, Align.left, false);
        String url = sensorClient.getUrlAtual();
        desenharTextoAdaptado(batch, url.isEmpty() ? "Buscando endereco..." : url.replace("http://", ""),
                0.62f, 3.62f, 3.1f, Align.left, 0.009f);
    }

    private void desenharAlvo() {
        float centroX = 2.65f;
        float centroY = 2.48f;
        float raio = 0.72f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 1; i <= 4; i++) {
            shapeRenderer.circle(centroX, centroY, raio * i / 3f, 32);
        }
        shapeRenderer.end();

        float pontoX = centroX + (sensorClient.getAngY() / 45f) * raio * 0.85f;
        float pontoY = centroY + (sensorClient.getAngX() / 45f) * raio * 0.85f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.SKY);
        shapeRenderer.circle(pontoX, pontoY, 0.08f, 16);
        shapeRenderer.end();
    }

    private void desenharAlvoTexto(SpriteBatch batch) {
        font.draw(batch, "X: " + String.format(Locale.ROOT, "%.1f", sensorClient.getAngX()),
                4.55f, 3.0f, 2.9f, Align.left, false);
        font.draw(batch, "Y: " + String.format(Locale.ROOT, "%.1f", sensorClient.getAngY()),
                4.55f, 2.5f, 2.9f, Align.left, false);
        font.draw(batch, "Temp: " + String.format(Locale.ROOT, "%.1f C", sensorClient.getTemp()),
                4.55f, 2.0f, 2.9f, Align.left, false);
    }

    private void desenharPainelMensagem() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (sensorClient.calibracaoFalhou()) {
            shapeRenderer.setColor(new Color(0.32f, 0.10f, 0.12f, 1f));
        } else if (sensorClient.calibrando()) {
            shapeRenderer.setColor(new Color(0.10f, 0.22f, 0.32f, 1f));
        } else if (sensorClient.calibracaoConcluidaComSucesso()) {
            shapeRenderer.setColor(new Color(0.10f, 0.30f, 0.18f, 1f));
        } else {
            shapeRenderer.setColor(new Color(0.15f, 0.15f, 0.2f, 1f));
        }
        shapeRenderer.rect(0.4f, 1.05f, 7.2f, 0.46f);
        shapeRenderer.end();
    }

    private void desenharProgresso() {
        float x = 0.8f;
        float y = 0.82f;
        float largura = 6.4f;
        float altura = 0.14f;
        int progresso = sensorClient.getProgresso();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.18f, 0.18f, 0.22f, 1f));
        shapeRenderer.rect(x, y, largura, altura);
        if (sensorClient.calibrando()) {
            shapeRenderer.setColor(new Color(0.2f, 0.7f, 1f, 1f));
            shapeRenderer.rect(x, y, largura * (progresso / 100f), altura);
        } else if (sensorClient.calibracaoFalhou()) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(x, y, largura, altura);
        } else if (sensorClient.calibracaoConcluidaComSucesso()) {
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(x, y, largura, altura);
        }
        shapeRenderer.end();
    }

    private void desenharProgressoTexto(SpriteBatch batch) {
        float x = 0.8f;
        float largura = 7.0f;
        int progresso = sensorClient.getProgresso();
        if (sensorClient.calibrando()) {
            String mensagem = sensorClient.getMensagemErro();
            if (mensagem.isEmpty()) {
                mensagem = "Mantenha imovel... " + progresso + "%";
            }
            desenharTextoAdaptado(batch, mensagem, x, 1.22f, largura, Align.center, 0.009f);
        } else if (sensorClient.calibracaoFalhou()) {
            String mensagem = sensorClient.getMensagemErro();
            if (mensagem.isEmpty()) {
                mensagem = "Reposicione o sensor e tente novamente.";
            }
            desenharTextoAdaptado(batch, mensagem, x, 1.22f, largura, Align.center, 0.009f);
        } else if (sensorClient.calibracaoConcluidaComSucesso()) {
            desenharTextoAdaptado(batch, "Calibração concluída com sucesso!",
                    x, 1.22f, largura, Align.center, 0.011f);
        } else if (!sensorClient.sensorPronto() && !sensorClient.getMensagemErro().isEmpty()) {
            desenharTextoAdaptado(batch, sensorClient.getMensagemErro(),
                    x, 1.22f, largura, Align.center, 0.009f);
        } else {
            desenharTextoAdaptado(batch, "Pronto para iniciar",
                    x, 1.22f, largura, Align.center, 0.011f);
        }
    }

    private void desenharTextoAdaptado(SpriteBatch batch, String texto, float x, float y,
                                       float largura, int alinhamento, float escala) {
        float escalaX = font.getData().scaleX;
        float escalaY = font.getData().scaleY;
        font.getData().setScale(escala);
        try {
            font.draw(batch, texto, x, y, largura, alinhamento, true);
        } finally {
            font.getData().setScale(escalaX, escalaY);
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
