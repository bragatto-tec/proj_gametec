package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;

/**
 * Fundo animado da rua: percorre os 32 frames (assets/rua/rua01.png ... rua32.png)
 * em loop infinito. Depois do ultimo frame volta para o primeiro.
 */
public class Rua extends GameObject {

    private static final int TOTAL_FRAMES = 32;

    private final Texture[] frames = new Texture[TOTAL_FRAMES];
    private final float duracaoFrame;   // segundos por frame
    private float tempo = 0f;
    private float velocidade = 1f;      // 1 = normal, 2 = dobro, 0 = parada
    private int frameAtual = 0;

    /**
     * @param fps quantos frames da rua passam por segundo (ex.: 24)
     */
    public Rua(float x, float y, float width, float height, float fps) {
        super(carregar(1), x, y, width, height);
        this.duracaoFrame = 1f / fps;

        frames[0] = sprite.getTexture();
        for (int i = 1; i < TOTAL_FRAMES; i++) {
            frames[i] = carregar(i + 1);
        }
    }

    private static Texture carregar(int numero) {
        Texture t = new Texture(String.format("rua/rua%02d.png", numero));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    @Override
    public void update(float dt) {
        tempo += dt * velocidade;

        int novoFrame = ((int) (tempo / duracaoFrame)) % TOTAL_FRAMES;
        if (novoFrame != frameAtual) {
            frameAtual = novoFrame;
            sprite.setTexture(frames[frameAtual]);
        }

        // evita o float crescer indefinidamente
        float duracaoCiclo = duracaoFrame * TOTAL_FRAMES;
        if (tempo >= duracaoCiclo) {
            tempo -= duracaoCiclo;
        }
    }

    /** Multiplicador de velocidade da animacao (pode ligar a velocidade do carro). */
    public void setVelocidade(float velocidade) {
        this.velocidade = Math.max(0f, velocidade);
    }

    public float getVelocidade() {
        return velocidade;
    }

    /** Volta para o primeiro frame. */
    public void reiniciar() {
        tempo = 0f;
        frameAtual = 0;
        sprite.setTexture(frames[0]);
    }

    public void dispose() {
        for (Texture t : frames) {
            if (t != null) t.dispose();
        }
    }
}
