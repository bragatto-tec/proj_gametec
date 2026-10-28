package io.github.some_example_name;

/** Clickable mode card with normal and selected artwork. */
public final class ModeCard extends GameObject {
    private final UiAssets assets;
    private final String name;
    private boolean selected;

    public ModeCard(UiAssets assets, String name, float x, float y, float width, float height) {
        super(assets.card(name, false), x, y, width, height);
        this.assets = assets;
        this.name = name;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        sprite.setTexture(assets.card(name, selected));
    }

    public boolean foiClicado(float x, float y) { return getBounds().contains(x, y); }
    @Override public void update(float dt) { }
}
