package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Item que desce pela pista: uma moeda (coletavel) ou um cone (obstaculo).
 *
 * A rua e desenhada em perspectiva, entao o item nao cai em linha reta:
 * ele nasce pequeno no horizonte e cresce/desliza ao longo da sua via
 * ate passar pelo carro. Para isso guardamos uma "profundidade" z:
 *   z grande = longe (horizonte) | z = 1 = na altura do carro | z < 1 = passou do carro
 * e a escala na tela e simplesmente s = 1 / z. Com z diminuindo a velocidade
 * constante, o item acelera visualmente, como numa estrada real.
 */
public class ItemPista extends GameObject {

    public enum Tipo { MOEDA, CONE }

    private static final float TEMPO_QUADRO_MOEDA = 0.08f; // 8 quadros -> giro completo em ~0.64s

    private final Tipo tipo;
    private final int via;
    private final float larguraBase;   // tamanho em unidades do mundo quando s = 1
    private final float alturaBase;
    private final float elevacaoBase;  // moedas "flutuam" um pouco acima do asfalto
    private final float margemHitbox;  // fracao encolhida de cada lado na colisao
    private final float velocidade;    // unidades de z por segundo
    private final TextureRegion[] quadros;
    private float z;
    private float tempoAnimacao;

    public ItemPista(Tipo tipo, int via, TextureRegion[] quadros, float larguraBase,
                     float elevacaoBase, float margemHitbox, float zInicial, float velocidade) {
        super(quadros[0].getTexture(), 0f, 0f, 1f, 1f);
        this.tipo = tipo;
        this.via = via;
        this.quadros = quadros;
        this.larguraBase = larguraBase;
        this.alturaBase = larguraBase * quadros[0].getRegionHeight() / quadros[0].getRegionWidth();
        this.elevacaoBase = elevacaoBase;
        this.margemHitbox = margemHitbox;
        this.z = zInicial;
        this.velocidade = velocidade;
        this.sprite.setRegion(quadros[0]);
        posicionar();
    }

    @Override
    public void update(float dt) {
        z -= velocidade * dt;
        if (z < 0.05f) z = 0.05f; // protege a divisao; o item ja foi removido antes disso

        if (quadros.length > 1) {
            tempoAnimacao += dt;
            int quadro = (int) (tempoAnimacao / TEMPO_QUADRO_MOEDA) % quadros.length;
            sprite.setRegion(quadros[quadro]);
        }
        posicionar();
    }

    /** Converte (via, z) em posicao e tamanho na tela. */
    private void posicionar() {
        float escala = 1f / z;
        float w = larguraBase * escala;
        float h = alturaBase * escala;
        float cx = GerenciadorPista.xDaVia(via, escala);
        float y = GerenciadorPista.yDoChao(escala) + elevacaoBase * escala;
        sprite.setSize(w, h);
        sprite.setPosition(cx - w / 2f, y);
    }

    /** Hitbox atual (muda de tamanho conforme o item se aproxima). */
    @Override
    public Rectangle getBounds() {
        float mx = sprite.getWidth() * margemHitbox;
        float my = sprite.getHeight() * margemHitbox;
        bounds.set(sprite.getX() + mx, sprite.getY() + my,
                sprite.getWidth() - 2f * mx, sprite.getHeight() - 2f * my);
        return bounds;
    }

    /** true quando o item ja saiu inteiro pela parte de baixo da tela. */
    public boolean saiuDaTela() {
        return sprite.getY() + sprite.getHeight() < 0f;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getVia() {
        return via;
    }
}
