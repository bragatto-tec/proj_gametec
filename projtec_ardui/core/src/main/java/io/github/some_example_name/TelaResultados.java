package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.Locale;

public class TelaResultados {
    private final FitViewport viewport;
    private final BitmapFont font;
    private final Vector2 touchPos;

    private final Button botaoJogarNovamente;
    private final Button botaoMenu;

    private String motivo = "";
    private int duracaoSegundos;
    private int pontos;
    private int moedas;
    private int moedasPerdidas;
    private int batidas;
    private int maxBatidas;
    private int fase;
    private int fases;
    private float tempoJogado;
    private int melhorPontuacao;
    private boolean novoRecorde;

    private boolean jogarNovamenteSolicitado;
    private boolean menuSolicitado;

    public TelaResultados(FitViewport viewport, BitmapFont font, UiAssets uiAssets) {
        this.viewport = viewport;
        this.font = font;
        this.touchPos = new Vector2();

        this.botaoJogarNovamente = new Button(uiAssets, "reiniciar_partida", 0.8f, 0.25f, 3.0f, 0.6f);
        this.botaoMenu = new Button(uiAssets, "voltar_ao_menu", 4.2f, 0.25f, 3.0f, 0.6f);
    }

    public void abrir(String motivo, int duracaoSegundos, int pontos, int moedas,
                      int moedasPerdidas, int batidas, int maxBatidas, int fase, int fases, float tempoJogado) {
        this.motivo = motivo;
        this.duracaoSegundos = duracaoSegundos;
        this.pontos = pontos;
        this.moedas = moedas;
        this.moedasPerdidas = moedasPerdidas;
        this.batidas = batidas;
        this.maxBatidas = maxBatidas;
        this.fase = fase;
        this.fases = fases;
        this.tempoJogado = tempoJogado;

        novoRecorde = pontos > melhorPontuacao;
        if (novoRecorde) {
            melhorPontuacao = pontos;
        }

        jogarNovamenteSolicitado = false;
        menuSolicitado = false;
    }

    public void fechar() {
        jogarNovamenteSolicitado = false;
        menuSolicitado = false;
    }

    public void input() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPos);
        botaoJogarNovamente.atualizarEstado(touchPos);
        botaoMenu.atualizarEstado(touchPos);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            menuSolicitado = true;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            jogarNovamenteSolicitado = true;
            return;
        }

        if (Gdx.input.justTouched()) {
            if (botaoJogarNovamente.foiClicado(touchPos.x, touchPos.y)) {
                jogarNovamenteSolicitado = true;
            }
            if (botaoMenu.foiClicado(touchPos.x, touchPos.y)) {
                menuSolicitado = true;
            }
        }
    }

    public boolean isJogarNovamenteSolicitado() {
        return jogarNovamenteSolicitado;
    }

    public boolean isMenuSolicitado() {
        return menuSolicitado;
    }

    private int aproveitamento() {
        int total = moedas + moedasPerdidas;
        if (total == 0) {
            return 0;
        }
        return Math.round(100f * moedas / total);
    }

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float largura = viewport.getWorldWidth();

        batch.begin();
        Ui.fundo(batch, largura, viewport.getWorldHeight());
        Ui.texto(batch, font, "FIM DE PARTIDA", 0, 4.85f, largura, Align.center, 0.025f, Color.WHITE, false);
        Ui.texto(batch, font, motivo + "  -  Modo Horizontal, " + (duracaoSegundos / 60) + " min",
                0, 4.25f, largura, Align.center, 0.0150f, Ui.MUTED, false);

        Ui.painel(batch, Ui.Estilo.AZUL, 0.5f, 1.1f, 3.3f, 2.6f);
        Ui.texto(batch, font, "PONTUAÇÃO", 0.5f, 3.45f, 3.3f, Align.center, 0.011f, Ui.AMARELO, false);
        Ui.textoLinha(batch, font, String.valueOf(pontos), 0.5f, 2.05f, 3.3f, 1.0f, Align.center, 0.06f, Ui.AMARELO);
        if (novoRecorde && pontos > 0) {
            Ui.pilula(batch, true, 0.95f, 1.35f, 2.4f, 0.42f);
            Ui.textoLinha(batch, font, "NOVO RECORDE!", 0.95f, 1.35f, 2.4f, 0.42f, Align.center, 0.0150f, Ui.AMARELO);
        } else {
            Ui.textoLinha(batch, font, "Melhor: " + melhorPontuacao, 0.5f, 1.35f, 3.3f, 0.42f, Align.center, 0.0150f, Ui.MUTED);
        }

        float x = 4.0f;
        Ui.painel(batch, Ui.Estilo.NEUTRO, x, 1.1f, 3.5f, 2.6f);
        batch.draw(Ui.moeda(), x + 0.25f, 3.08f, 0.34f, 0.34f);
        linha(batch, x, 3.05f, "Moedas: " + moedas + " de " + (moedas + moedasPerdidas), Color.WHITE);
        batch.draw(Ui.cone(), x + 0.22f, 2.6f, 0.4f, 0.4f);
        linha(batch, x, 2.6f, "Batidas: " + batidas + " de " + maxBatidas, batidas >= maxBatidas ? Ui.VERMELHO : Color.WHITE);
        linha(batch, x, 2.15f, "Aproveitamento: " + aproveitamento() + "%", Color.WHITE);
        linha(batch, x, 1.7f, "Fase alcançada: " + fase + "/" + fases, Color.WHITE);
        int segundos = (int) tempoJogado;
        linha(batch, x, 1.25f, String.format(Locale.ROOT, "Tempo jogado: %02d:%02d", segundos / 60, segundos % 60),
                Color.WHITE);

        botaoJogarNovamente.draw(batch);
        botaoMenu.draw(batch);
        batch.end();
    }

    private void linha(SpriteBatch batch, float xPainel, float y, String texto, Color cor) {
        Ui.textoLinha(batch, font, texto, xPainel + 0.8f, y, 2.6f, 0.4f, Align.left, 0.0150f, cor);
    }
}
