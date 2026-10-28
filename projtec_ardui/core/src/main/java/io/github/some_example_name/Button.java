package io.github.some_example_name;

import com.badlogic.gdx.math.Vector2;

public class Button extends GameObject {

    private boolean habilitado;
    private final UiAssets uiAssets;
    private final String assetName;
    private boolean pointerOver;

    public Button(UiAssets uiAssets, String name, float x, float y, float width, float height) {
        super(uiAssets.button(name, 0), x, y, width, height);
        this.uiAssets = uiAssets;
        this.assetName = name;
        this.habilitado = true;
    }

    @Override
    public void update(float dt) {
    }

    public void atualizarEstado(Vector2 ponteiroMundo) {
        pointerOver = ponteiroMundo != null && getBounds().contains(ponteiroMundo);
        int state = !habilitado ? 3 : pointerOver && com.badlogic.gdx.Gdx.input.isTouched() ? 2 : pointerOver ? 1 : 0;
        sprite.setTexture(uiAssets.button(assetName, state));
    }

    public boolean foiClicado(float x, float y) {
        return habilitado && getBounds().contains(x, y);
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }
}