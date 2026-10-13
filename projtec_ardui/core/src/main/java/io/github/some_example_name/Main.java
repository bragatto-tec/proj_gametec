package io.github.some_example_name;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    private final String sensorBaseUrl;

    private enum Estado {
        MENU,
        CALIBRANDO,
        JOGANDO,
        FIM
    }

    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private BitmapFont font;
    private Texture buttonTexture;
    private Vector2 touchPos;

    private SensorClient sensorClient;
    private TelaCalibracao telaCalibracao;
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

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.02f);

        buttonTexture = new Texture("button.png");
        touchPos = new Vector2();
        sensorClient = sensorBaseUrl == null ? new SensorClient() : new SensorClient(sensorBaseUrl);
        telaCalibracao = new TelaCalibracao(viewport, font, buttonTexture, sensorClient);
        estadoAtual = Estado.MENU;

        botaoJogar = new Button(buttonTexture, font, "Jogar", 2.5f, 3.0f, 3, 0.7f);
        botaoCalibrar = new Button(buttonTexture, font, "Calibrar sensor", 2.5f, 2.1f, 3, 0.7f);
        botaoConfig = new Button(buttonTexture, font, "Configuracoes", 2.5f, 1.2f, 3, 0.7f);
        botaoSair = new Button(buttonTexture, font, "Sair", 2.5f, 0.3f, 3, 0.7f);
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
        if (estadoAtual == Estado.CALIBRANDO) {
            if (telaCalibracao.input()) {
                estadoAtual = Estado.MENU;
                telaCalibracao.fechar();
            }
            return;
        }

        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            if (botaoJogar.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Jogar");
                estadoAtual = Estado.JOGANDO;
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

    private void logic() {
        sensorClient.update(Gdx.graphics.getDeltaTime());

        if (estadoAtual == Estado.CALIBRANDO) {
            telaCalibracao.update(Gdx.graphics.getDeltaTime());
            if (telaCalibracao.isVoltarSolicitado()) {
                estadoAtual = Estado.MENU;
                telaCalibracao.fechar();
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
        spriteBatch.dispose();
        font.dispose();
        buttonTexture.dispose();
    }
}
