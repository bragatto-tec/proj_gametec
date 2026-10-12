package io.github.some_example_name;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    SpriteBatch spriteBatch;
    FitViewport viewport;
    BitmapFont font;
    Texture buttonTexture;
    Vector2 touchPos;

    Button botaoJogar;
    Button botaoCalibrar;
    Button botaoConfig;
    Button botaoSair;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.02f);

        buttonTexture = new Texture("button.png");
        touchPos = new Vector2();

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
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            if (botaoJogar.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Jogar");
            }
            if (botaoCalibrar.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Calibrar sensor");
            }
            if (botaoConfig.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.log("RehabCar", "Configuracoes");
            }
            if (botaoSair.foiClicado(touchPos.x, touchPos.y)) {
                Gdx.app.exit();
            }
        }
    }

    private void logic() {
    }

    private void draw() {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

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
        spriteBatch.dispose();
        font.dispose();
        buttonTexture.dispose();
    }
}