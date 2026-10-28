package io.github.some_example_name;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

/**
 * Tema visual do RehabCar: painéis, pílulas, barra, LEDs, fundo, fonte e HUD.
 * Chame Ui.criarFonte() e Ui.carregar() no create() do Main e Ui.dispose() no dispose().
 */
public final class Ui {
    /** A fonte e gerada a 60 px; as telas continuam usando as escalas antigas (0.02f etc., base de 15 px). */
    private static final float ESCALA_FONTE = 15f / 60f;
    public static final float ESCALA_BASE = 0.02f;

    public static final Color AMARELO = new Color(1f, 0.773f, 0.239f, 1f);
    public static final Color VERDE = new Color(0.30f, 0.85f, 0.50f, 1f);
    public static final Color VERMELHO = new Color(0.95f, 0.30f, 0.35f, 1f);
    public static final Color AZUL = new Color(0.25f, 0.70f, 1f, 1f);
    public static final Color MUTED = new Color(0.62f, 0.70f, 0.84f, 1f);

    public enum Estilo { NEUTRO, VERDE, VERMELHO, AZUL }

    public static final float BARRA_ALTURA = 0.22f;

    private static final GlyphLayout LAYOUT = new GlyphLayout();
    private static final List<Texture> texturas = new ArrayList<>();
    private static NinePatch pNeutro, pVerde, pVermelho, pAzul, pilulaNeutra, pilulaAmarela, barraFundo, barraFill;
    private static Texture fundo, led, anel, pixel, cone, moeda;
    private static Texture iconeModo;
    private static TextureRegion moedaIcone;

    private Ui() {
    }

    // ---------------------------------------------------------------- fonte

    private static class FonteHD extends BitmapFont.BitmapFontData {
        FonteHD(FileHandle arquivo) {
            super(arquivo, false);
        }

        @Override
        public void setScale(float scaleX, float scaleY) {
            super.setScale(scaleX * ESCALA_FONTE, scaleY * ESCALA_FONTE);
        }
    }

    public static BitmapFont criarFonte() {
        BitmapFont fonte = new BitmapFont(new FonteHD(Gdx.files.internal("fonts/fredoka.fnt")),
                (Array<TextureRegion>) null, true);
        for (TextureRegion regiao : fonte.getRegions()) {
            regiao.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        fonte.setUseIntegerPositions(false);
        return fonte;
    }

    // ------------------------------------------------------------- recursos

    private static Texture tex(String caminho) {
        Texture t = new Texture(Gdx.files.internal(caminho), true);
        t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        texturas.add(t);
        return t;
    }

    private static NinePatch patch(String caminho, int borda, float escala) {
        NinePatch p = new NinePatch(tex(caminho), borda, borda, borda, borda);
        p.scale(escala, escala);
        return p;
    }

    public static void carregar() {
        pNeutro = patch("ui/painel_neutro.png", 40, 0.005f);
        pVerde = patch("ui/painel_verde.png", 40, 0.005f);
        pVermelho = patch("ui/painel_vermelho.png", 40, 0.005f);
        pAzul = patch("ui/painel_azul.png", 40, 0.005f);
        pilulaNeutra = patch("ui/pilula_neutra.png", 48, 0.0035f);
        pilulaAmarela = patch("ui/pilula_amarela.png", 48, 0.0035f);
        barraFundo = patch("ui/barra_fundo.png", 16, BARRA_ALTURA / 36f);
        barraFill = patch("ui/barra_fill.png", 16, BARRA_ALTURA / 36f);
        fundo = tex("ui/fundo_ui.png");
        led = tex("ui/led.png");
        anel = tex("ui/anel.png");
        cone = tex("cone.png");
        moeda = tex("moeda.png");
        moedaIcone = new TextureRegion(moeda, 0, 0, 16, 16);
        iconeModo = tex("ui/icone_modo_horizontal.png");

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
        texturas.add(pixel);
    }

    public static void dispose() {
        for (Texture t : texturas) {
            t.dispose();
        }
        texturas.clear();
    }

    // -------------------------------------------------------------- desenho

    public static void fundo(SpriteBatch b, float largura, float altura) {
        b.draw(fundo, 0, 0, largura, altura);
    }

    public static void retangulo(SpriteBatch b, float x, float y, float w, float h, Color cor) {
        b.setColor(cor);
        b.draw(pixel, x, y, w, h);
        b.setColor(Color.WHITE);
    }

    public static void painel(SpriteBatch b, Estilo estilo, float x, float y, float w, float h) {
        NinePatch p = pNeutro;
        if (estilo == Estilo.VERDE) p = pVerde;
        else if (estilo == Estilo.VERMELHO) p = pVermelho;
        else if (estilo == Estilo.AZUL) p = pAzul;
        p.draw(b, x, y, w, h);
    }

    /** Altura minima recomendada: 0.34f. (texturas 9-patch precisam de um miolo de pelo menos 1 px, por isso 100x100 / 64x36) */
    public static void pilula(SpriteBatch b, boolean amarela, float x, float y, float w, float h) {
        (amarela ? pilulaAmarela : pilulaNeutra).draw(b, x, y, w, h);
    }

    public static void led(SpriteBatch b, Color cor, float centroX, float centroY, float tamanho) {
        b.setColor(cor);
        b.draw(led, centroX - tamanho / 2f, centroY - tamanho / 2f, tamanho, tamanho);
        b.setColor(Color.WHITE);
    }

    /** Barra de progresso (fracao 0..1). Altura fixa BARRA_ALTURA. */
    public static void barra(SpriteBatch b, float x, float y, float largura, float fracao, Color cor) {
        barraFundo.draw(b, x, y, largura, BARRA_ALTURA);
        if (fracao > 0f) {
            float w = Math.max(BARRA_ALTURA, largura * Math.min(1f, fracao));
            b.setColor(cor);
            barraFill.draw(b, x, y, w, BARRA_ALTURA);
            b.setColor(Color.WHITE);
        }
    }

    public static void anel(SpriteBatch b, Color cor, float centroX, float centroY, float tamanho) {
        b.setColor(cor);
        b.draw(anel, centroX - tamanho / 2f, centroY - tamanho / 2f, tamanho, tamanho);
        b.setColor(Color.WHITE);
    }

    public static Texture iconeModo() {
        return iconeModo;
    }

    public static Texture cone() {
        return cone;
    }

    public static TextureRegion moeda() {
        return moedaIcone;
    }

    // ---------------------------------------------------------------- texto

    /** Texto simples. y = topo do texto (como no BitmapFont). */
    public static void texto(SpriteBatch b, BitmapFont f, String s, float x, float y, float largura,
                             int alinhamento, float escala, Color cor, boolean quebra) {
        f.getData().setScale(escala);
        f.setColor(cor);
        f.draw(b, s, x, y, largura, alinhamento, quebra);
        f.setColor(Color.WHITE);
        f.getData().setScale(ESCALA_BASE);
    }

    /** Texto centralizado vertical e horizontalmente numa caixa; tenta cada escala em linha unica e, se nao couber, quebra. */
    public static void textoNaCaixa(SpriteBatch b, BitmapFont f, String s, float x, float y, float w, float h,
                                    Color cor, float... escalas) {
        float usar = escalas[escalas.length - 1];
        boolean quebra = true;
        for (float e : escalas) {
            f.getData().setScale(e);
            LAYOUT.setText(f, s);
            if (LAYOUT.width <= w) {
                usar = e;
                quebra = false;
                break;
            }
        }
        f.getData().setScale(usar);
        LAYOUT.setText(f, s, cor, w, Align.center, quebra);
        float topo = y + (h + LAYOUT.height) / 2f;
        f.setColor(cor);
        f.draw(b, s, x, topo, w, Align.center, quebra);
        f.setColor(Color.WHITE);
        f.getData().setScale(ESCALA_BASE);
    }

    /** Texto em uma linha, centralizado na vertical dentro de [y, y+h]. */
    public static void textoLinha(SpriteBatch b, BitmapFont f, String s, float x, float y, float w, float h,
                                  int alinhamento, float escala, Color cor) {
        f.getData().setScale(escala);
        float topo = y + (h + f.getCapHeight()) / 2f;
        f.setColor(cor);
        f.draw(b, s, x, topo, w, alinhamento, false);
        f.setColor(Color.WHITE);
        f.getData().setScale(ESCALA_BASE);
    }

    // ------------------------------------------------------------------ HUD

    /** HUD da partida: estado do sensor (esq.) e moedas/batidas (dir.), em pilulas. */
    public static void hud(SpriteBatch b, BitmapFont f, float larguraMundo, float alturaMundo,
                           boolean sensorPronto, int moedas, int batidas) {
        float h = 0.42f, y = alturaMundo - 0.15f - h, margem = 0.15f;

        // sensor
        float wSensor = sensorPronto ? 2.45f : 3.55f;
        pilula(b, false, margem, y, wSensor, h);
        led(b, sensorPronto ? VERDE : AMARELO, margem + 0.28f, y + h / 2f, 0.26f);
        textoLinha(b, f, sensorPronto ? "MPU6050 conectado" : "Aguardando MPU6050 (setas/A/D)",
                margem + 0.5f, y, wSensor - 0.6f, h, Align.left, 0.0135f, Color.WHITE);

        // batidas (cone)
        float wItem = 1.3f;
        float xBatidas = larguraMundo - margem - wItem;
        pilula(b, false, xBatidas, y, wItem, h);
        b.draw(cone, xBatidas + 0.16f, y + 0.06f, 0.30f, 0.30f);
        textoLinha(b, f, String.valueOf(batidas), xBatidas + 0.5f, y, wItem - 0.65f, h, Align.center, 0.019f, Color.WHITE);

        // moedas
        float xMoedas = xBatidas - 0.12f - wItem;
        pilula(b, true, xMoedas, y, wItem, h);
        b.draw(moedaIcone, xMoedas + 0.15f, y + 0.07f, 0.28f, 0.28f);
        textoLinha(b, f, String.valueOf(moedas), xMoedas + 0.5f, y, wItem - 0.65f, h, Align.center, 0.019f, AMARELO);
    }
}
