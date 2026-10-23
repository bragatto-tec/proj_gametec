package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaSelecaoModo {
    public static final int NENHUM = 0;
    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 2;
    public static final int COMBINADO = 3;

    private final FitViewport viewport;
    private final BitmapFont font;
    private final Vector2 touchPos;

    private final ModeCard botaoVertical;
    private final ModeCard botaoHorizontal;
    private final ModeCard botaoCombinado;
    private final Button botaoVoltar;
    private final Button botaoSelecionar;

    private int modoEscolhido;
    private boolean voltarSolicitado;
    private boolean selecionarSolicitado;

    public TelaSelecaoModo(FitViewport viewport, BitmapFont font, UiAssets uiAssets) {
        this.viewport = viewport;
        this.font = font;
        this.touchPos = new Vector2();

        this.botaoVertical = new ModeCard(uiAssets, "vertical", 0.44f, 1.0f, 2.24f, 2.5f);
        this.botaoHorizontal = new ModeCard(uiAssets, "horizontal", 2.88f, 1.0f, 2.24f, 2.5f);
        this.botaoCombinado = new ModeCard(uiAssets, "combinado", 5.32f, 1.0f, 2.24f, 2.5f);
        this.botaoVoltar = new Button(uiAssets, "voltar", 1.0f, 0.2f, 2.5f, 0.6f);
        this.botaoSelecionar = new Button(uiAssets, "selecionar", 4.5f, 0.2f, 2.5f, 0.6f);

        this.modoEscolhido = NENHUM;
        this.botaoSelecionar.setHabilitado(false);
    }

    public void abrir() {
        modoEscolhido = NENHUM;
        atualizarSelecaoCards();
        voltarSolicitado = false;
        selecionarSolicitado = false;
        botaoSelecionar.setHabilitado(false);
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
            if (botaoVertical.foiClicado(touchPos.x, touchPos.y)) {
                modoEscolhido = VERTICAL;
                atualizarSelecaoCards();
                botaoSelecionar.setHabilitado(true);
            }
            if (botaoHorizontal.foiClicado(touchPos.x, touchPos.y)) {
                modoEscolhido = HORIZONTAL;
                atualizarSelecaoCards();
                botaoSelecionar.setHabilitado(true);
            }
            if (botaoCombinado.foiClicado(touchPos.x, touchPos.y)) {
                modoEscolhido = COMBINADO;
                atualizarSelecaoCards();
                botaoSelecionar.setHabilitado(true);
            }
            if (botaoVoltar.foiClicado(touchPos.x, touchPos.y)) {
                voltarSolicitado = true;
            }
            if (botaoSelecionar.foiClicado(touchPos.x, touchPos.y)) {
                selecionarSolicitado = true;
            }
        }
    }

    public boolean isVoltarSolicitado() {
        return voltarSolicitado;
    }

    public boolean isSelecionarSolicitado() {
        return selecionarSolicitado;
    }

    public int getModoEscolhido() {
        return modoEscolhido;
    }

    public String getNomeModo() {
        if (modoEscolhido == VERTICAL) {
            return "Vertical";
        }
        if (modoEscolhido == HORIZONTAL) {
            return "Horizontal";
        }
        if (modoEscolhido == COMBINADO) {
            return "Combinado";
        }
        return "nenhum";
    }

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        font.draw(batch, "ESCOLHA O MODO DE JOGO", 0, 4.8f, viewport.getWorldWidth(), Align.center, false);

        botaoVertical.draw(batch);
        botaoHorizontal.draw(batch);
        botaoCombinado.draw(batch);
        botaoVoltar.draw(batch);
        botaoSelecionar.draw(batch);

        batch.end();
    }

    private void atualizarSelecaoCards() {
        botaoVertical.setSelected(modoEscolhido == VERTICAL);
        botaoHorizontal.setSelected(modoEscolhido == HORIZONTAL);
        botaoCombinado.setSelected(modoEscolhido == COMBINADO);
    }
}
