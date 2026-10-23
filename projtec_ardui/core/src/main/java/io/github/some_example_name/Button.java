package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.math.Vector2;

public class Button extends GameObject {

    private String text;
    private BitmapFont font;
    private boolean habilitado;
    private final UiAssets uiAssets;
    private final String assetName;
    private boolean legacyText;
    private boolean pointerOver;

    public Button(Texture texture, BitmapFont font, String text, float x, float y, float width, float height) {
        super(texture, x, y, width, height);
        this.uiAssets = null;
        this.assetName = null;
        this.font = font;
        this.text = text;
        this.legacyText = true;
        this.habilitado = true;
        sprite.setColor(Color.WHITE);
    }

    public Button(UiAssets uiAssets, String name, float x, float y, float width, float height) {
        super(uiAssets.button(name, 0), x, y, width, height);
        this.uiAssets = uiAssets;
        this.assetName = name;
        this.habilitado = true;
        this.text = null;
        this.font = null;
    }

    @Override
    public void update(float dt) {
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        if (legacyText) {
            font.draw(batch, text, sprite.getX(), sprite.getY() + sprite.getHeight() / 2f + 0.12f, sprite.getWidth(), Align.center, false);
        }
    }

    public void atualizarEstado(Vector2 ponteiroMundo) {
        pointerOver = ponteiroMundo != null && getBounds().contains(ponteiroMundo);
        if (uiAssets == null) return;
        int state = !habilitado ? 3 : pointerOver && com.badlogic.gdx.Gdx.input.isTouched() ? 2 : pointerOver ? 1 : 0;
        sprite.setTexture(uiAssets.button(findName(), state));
    }

    private String findName() { return assetName; }

    public boolean foiClicado(float x, float y) {
        return habilitado && getBounds().contains(x, y);
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
        sprite.setColor(uiAssets == null && !habilitado ? Color.GRAY : Color.WHITE);
    }
}
