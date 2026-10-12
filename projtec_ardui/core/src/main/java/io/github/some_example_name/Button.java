package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

public class Button extends GameObject {

    private String text;
    private BitmapFont font;

    public Button(Texture texture, BitmapFont font, String text, float x, float y, float width, float height) {
        super(texture, x, y, width, height);
        this.font = font;
        this.text = text;
    }

    @Override
    public void update(float dt) {
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        font.draw(batch, text, sprite.getX(), sprite.getY() + sprite.getHeight() / 2f + 0.12f, sprite.getWidth(), Align.center, false);
    }

    public boolean foiClicado(float x, float y) {
        return getBounds().contains(x, y);
    }
}