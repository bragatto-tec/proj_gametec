package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

/** Shared cache for button and mode-card textures. */
public final class UiAssets implements Disposable {
    private final String language;
    private final Map<String, Texture[]> buttons = new HashMap<String, Texture[]>();
    private final Map<String, Texture[]> cards = new HashMap<String, Texture[]>();
    private final Texture whitePixel;

    public UiAssets(String language) {
        if (!"pt".equals(language) && !"en".equals(language)) {
            throw new IllegalArgumentException("Idioma deve ser 'pt' ou 'en': " + language);
        }
        this.language = language;
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(1, 1, 1, 1);
        pixel.fill();
        whitePixel = new Texture(pixel);
        pixel.dispose();
    }

    public Texture button(String name, int state) {
        Texture[] textures = buttons.get(name);
        if (textures == null) {
            textures = loadSet(name, new String[]{"normal", "hover", "pressed", "disabled"}, "botoes");
            buttons.put(name, textures);
        }
        return textures[state];
    }

    public Texture card(String name, boolean selected) {
        Texture[] textures = cards.get(name);
        if (textures == null) {
            textures = loadSet(name, new String[]{"normal", "selecionado"}, "cards");
            cards.put(name, textures);
        }
        return textures[selected ? 1 : 0];
    }

    public Texture whitePixel() { return whitePixel; }

    private Texture[] loadSet(String name, String[] states, String folder) {
        Texture[] textures = new Texture[states.length];
        for (int i = 0; i < states.length; i++) {
            String filename = name + "_" + states[i] + ".png";
            String root = "pt".equals(language) ? "ui" : "ui_en";
            String path = root + "/" + folder + "/" + filename;
            if ("pt".equals(language) && !Gdx.files.internal(path).exists()) {
                // Support the Portuguese asset layout already present in this project.
                path = folder + "/" + filename;
            }
            if (!Gdx.files.internal(path).exists()) {
                throw new IllegalArgumentException("Asset de UI ausente: " + root + "/" + folder + "/" + filename);
            }
            Texture texture = new Texture(Gdx.files.internal(path), true);
            texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
            textures[i] = texture;
        }
        return textures;
    }

    @Override
    public void dispose() {
        for (Texture[] set : buttons.values()) for (Texture texture : set) texture.dispose();
        for (Texture[] set : cards.values()) for (Texture texture : set) texture.dispose();
        whitePixel.dispose();
    }
}
