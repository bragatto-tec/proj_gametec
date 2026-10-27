package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Sistema de 3 vias: define onde cada via fica na tela, sorteia o que nasce
 * (moeda ou cone) e em qual via, move os itens, remove os que saem da tela
 * e detecta colisao com o carro.
 *
 * As constantes de geometria foram medidas na arte da rua (assets/rua, mundo 8x5):
 * ponto de fuga em x = 4,0 / y = 3,6 e faixas a +-1,45 do centro na altura do carro.
 * Se trocar a arte da rua, ajuste so as constantes abaixo.
 */
public class GerenciadorPista {

    public static final int NUM_VIAS = 3;
    public static final int VIA_ESQUERDA = 0;
    public static final int VIA_CENTRO = 1;
    public static final int VIA_DIREITA = 2;

    // --- geometria da pista (perspectiva) ---
    private static final float X_FUGA = 4.0f;          // centro da pista / ponto de fuga
    private static final float Y_HORIZONTE = 3.6f;     // y do ponto de fuga
    private static final float Y_CHAO_CARRO = 0.35f;   // y da base do carro (escala 1)
    private static final float DESVIO_VIA = 1.45f;     // distancia do centro da via lateral, na escala 1

    // --- spawn ---
    private static final float Z_NASCIMENTO = 22f;     // longe, perto do horizonte (escala ~0,045)

    // --- dificuldade por fase (posicao 0 = fase 1, 1 = fase 2, 2 = fase 3) ---
    private static final float[] VELOCIDADES_Z = {4.5f, 5.5f, 6.5f};     // unidades de z por segundo
    private static final float[] SPAWN_MINIMOS = {0.70f, 0.55f, 0.45f};  // segundos entre itens
    private static final float[] SPAWN_MAXIMOS = {1.40f, 1.15f, 0.90f};
    private static final float[] CHANCES_MOEDA = {0.70f, 0.60f, 0.50f};  // o resto e cone

    private static final int QUADROS_MOEDA = 8;        // moeda.png = 8 quadros de 16x16

    private final Texture texturaCone;
    private final Texture texturaMoeda;
    private final TextureRegion[] regiaoCone;
    private final TextureRegion[] quadrosMoeda;

    private final Array<ItemPista> itens = new Array<ItemPista>();
    private float tempoParaProximo;
    private int moedas;
    private int batidas;
    private int moedasPerdidas;
    private float velocidadeZ;
    private float spawnMin;
    private float spawnMax;
    private float chanceMoeda;

    public GerenciadorPista() {
        texturaCone = new Texture("cone.png");
        texturaCone.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        // recorta so a parte visivel do cone (a imagem 500x500 tem muita margem transparente)
        regiaoCone = new TextureRegion[] { new TextureRegion(texturaCone, 142, 154, 219, 237) };

        texturaMoeda = new Texture("moeda.png");
        texturaMoeda.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest); // pixel art
        int lado = texturaMoeda.getHeight();
        quadrosMoeda = new TextureRegion[QUADROS_MOEDA];
        for (int i = 0; i < QUADROS_MOEDA; i++) {
            quadrosMoeda[i] = new TextureRegion(texturaMoeda, i * lado, 0, lado, lado);
        }
        reiniciar();
    }

    // ---------------------------------------------------------------- geometria

    /**
     * Centro X de uma via. escala = 1 na altura do carro; tende a 0 no horizonte,
     * onde as tres vias se encontram no ponto de fuga.
     */
    public static float xDaVia(int via, float escala) {
        return X_FUGA + (via - VIA_CENTRO) * DESVIO_VIA * escala;
    }

    /** Y do chao (base do objeto) para uma escala de perspectiva. */
    public static float yDoChao(float escala) {
        return Y_HORIZONTE + (Y_CHAO_CARRO - Y_HORIZONTE) * escala;
    }

    // ---------------------------------------------------------------- ciclo de vida

    public void reiniciar() {
        itens.clear();
        moedas = 0;
        batidas = 0;
        moedasPerdidas = 0;
        tempoParaProximo = 1.0f; // pequena folga antes do primeiro item
        setFase(1);
    }

    /** Ajusta velocidade, frequencia de itens e chance de cone conforme a fase (1 a 3). */
    public void setFase(int fase) {
        int i = Math.max(1, Math.min(fase, VELOCIDADES_Z.length)) - 1;
        velocidadeZ = VELOCIDADES_Z[i];
        spawnMin = SPAWN_MINIMOS[i];
        spawnMax = SPAWN_MAXIMOS[i];
        chanceMoeda = CHANCES_MOEDA[i];
    }

    public void update(float dt, Rectangle hitboxCarro) {
        tempoParaProximo -= dt;
        if (tempoParaProximo <= 0f) {
            spawnItem();
            tempoParaProximo = MathUtils.random(spawnMin, spawnMax);
        }

        for (int i = itens.size - 1; i >= 0; i--) {
            ItemPista item = itens.get(i);
            item.update(dt);

            if (item.getBounds().overlaps(hitboxCarro)) {
                if (item.getTipo() == ItemPista.Tipo.MOEDA) {
                    moedas++;
                } else {
                    batidas++;
                }
                itens.removeIndex(i);
            } else if (item.saiuDaTela()) {
                if (item.getTipo() == ItemPista.Tipo.MOEDA) {
                    moedasPerdidas++;
                }
                itens.removeIndex(i); // saiu pela parte de baixo: libera memoria
            }
        }
    }

    /** Sorteia a via (0, 1 ou 2) e o tipo (moeda ou cone, pela chanceMoeda da fase) e cria o item no horizonte. */
    public void spawnItem() {
        int via = MathUtils.random(0, NUM_VIAS - 1);
        ItemPista item;
        if (MathUtils.randomBoolean(chanceMoeda)) {
            item = new ItemPista(ItemPista.Tipo.MOEDA, via, quadrosMoeda,
                    0.45f, 0.12f, 0.10f, Z_NASCIMENTO, velocidadeZ);
        } else {
            item = new ItemPista(ItemPista.Tipo.CONE, via, regiaoCone,
                    0.55f, 0f, 0.12f, Z_NASCIMENTO, velocidadeZ);
        }
        itens.add(item);
    }

    /** Itens novos ficam no fim da lista (mais longe): desenha de tras para frente. */
    public void draw(SpriteBatch batch) {
        for (int i = itens.size - 1; i >= 0; i--) {
            itens.get(i).draw(batch);
        }
    }

    public int getMoedas() {
        return moedas;
    }

    public int getBatidas() {
        return batidas;
    }

    /** Moedas que passaram pelo carro sem serem pegas. */
    public int getMoedasPerdidas() {
        return moedasPerdidas;
    }

    public void dispose() {
        texturaCone.dispose();
        texturaMoeda.dispose();
    }
}