package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaSelecaoDuracao {
    private final FitViewport viewport;
    private final BitmapFont font;
    private final Vector2 touchPos;
    private final Button botaoVoltar;
    private final Button botaoSelecionar;
    private final Rectangle area1Min;
    private final Rectangle area2Min;
    private final Rectangle area3Min;

    private int duracaoSegundos;
    private boolean voltarSolicitado;
    private boolean selecionarSolicitado;

    public TelaSelecaoDuracao(FitViewport viewport, BitmapFont font, UiAssets uiAssets) {
        this.viewport = viewport;
        this.font = font;
        this.touchPos = new Vector2();
        this.botaoVoltar = new Button(uiAssets, "voltar", 1.0f, 0.2f, 2.5f, 0.6f);
        this.botaoSelecionar = new Button(uiAssets, "selecionar", 4.5f, 0.2f, 2.5f, 0.6f);
        this.area1Min = new Rectangle(1.0f, 2.3f, 1.8f, 0.9f);
        this.area2Min = new Rectangle(3.1f, 2.3f, 1.8f, 0.9f);
        this.area3Min = new Rectangle(5.2f, 2.3f, 1.8f, 0.9f);
        abrir();
    }

    public void abrir() {
        duracaoSegundos = 60;
        voltarSolicitado = false;
        selecionarSolicitado = false;
    }

    public void fechar() {
        voltarSolicitado = false;
        selecionarSolicitado = false;
    }

    public void input() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPos);
        botaoVoltar.atualizarEstado(touchPos);
        botaoSelecionar.atualizarEstado(touchPos);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            voltarSolicitado = true;
            return;
        }

        if (Gdx.input.justTouched()) {
            if (area1Min.contains(touchPos.x, touchPos.y)) duracaoSegundos = 60;
            if (area2Min.contains(touchPos.x, touchPos.y)) duracaoSegundos = 120;
            if (area3Min.contains(touchPos.x, touchPos.y)) duracaoSegundos = 180;
            if (botaoVoltar.foiClicado(touchPos.x, touchPos.y)) voltarSolicitado = true;
            if (botaoSelecionar.foiClicado(touchPos.x, touchPos.y)) selecionarSolicitado = true;
        }
    }

    public boolean isVoltarSolicitado() {
        return voltarSolicitado;
    }

    public boolean isSelecionarSolicitado() {
        return selecionarSolicitado;
    }

    public int getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float largura = viewport.getWorldWidth();

        batch.begin();
        Ui.fundo(batch, largura, viewport.getWorldHeight());
        Ui.texto(batch, font, "DURAÇÃO DA PARTIDA", 0, 4.85f, largura, Align.center, 0.023f, Color.WHITE, false);
        Ui.texto(batch, font, "Incline o pé para os lados para trocar de faixa.",
                0, 4.1f, largura, Align.center, 0.0150f, Ui.MUTED, false);
        desenharDuracao(batch, area1Min, 60);
        desenharDuracao(batch, area2Min, 120);
        desenharDuracao(batch, area3Min, 180);
        Ui.texto(batch, font, "A partida tem 3 fases: a cada terço do tempo a pista fica mais rápida.",
                0, 1.75f, largura, Align.center, 0.0135f, Ui.MUTED, false);
        botaoVoltar.draw(batch);
        botaoSelecionar.draw(batch);
        batch.end();
    }

    private void desenharDuracao(SpriteBatch batch, Rectangle area, int segundos) {
        boolean escolhida = duracaoSegundos == segundos;
        Ui.pilula(batch, escolhida, area.x, area.y, area.width, area.height);
        Ui.textoLinha(batch, font, (segundos / 60) + " min", area.x, area.y, area.width, area.height,
                Align.center, 0.025f, escolhida ? Ui.AMARELO : Color.WHITE);
    }
}
