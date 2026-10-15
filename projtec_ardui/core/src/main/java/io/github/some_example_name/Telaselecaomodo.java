package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
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

    private final Button botaoVertical;
    private final Button botaoHorizontal;
    private final Button botaoCombinado;
    private final Button botaoVoltar;
    private final Button botaoSelecionar;

    private int modoEscolhido;
    private boolean voltarSolicitado;
    private boolean selecionarSolicitado;

    public TelaSelecaoModo(FitViewport viewport, BitmapFont font, Texture buttonTexture) {
        this.viewport = viewport;
        this.font = font;
        this.touchPos = new Vector2();

        this.botaoVertical = new Button(buttonTexture, font, "Vertical", 0.5f, 3.4f, 3.0f, 0.7f);
        this.botaoHorizontal = new Button(buttonTexture, font, "Horizontal", 0.5f, 2.5f, 3.0f, 0.7f);
        this.botaoCombinado = new Button(buttonTexture, font, "Combinado", 0.5f, 1.6f, 3.0f, 0.7f);
        this.botaoVoltar = new Button(buttonTexture, font, "Voltar", 1.0f, 0.2f, 2.5f, 0.6f);
        this.botaoSelecionar = new Button(buttonTexture, font, "Selecionar", 4.5f, 0.2f, 2.5f, 0.6f);

        this.modoEscolhido = NENHUM;
        this.botaoSelecionar.setHabilitado(false);
    }

    public void abrir() {
        modoEscolhido = NENHUM;
        voltarSolicitado = false;
        selecionarSolicitado = false;
        botaoSelecionar.setHabilitado(false);
    }

    public void fechar() {
        voltarSolicitado = false;
        selecionarSolicitado = false;
    }

    public void input() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            voltarSolicitado = true;
            return;
        }

        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            if (botaoVertical.foiClicado(touchPos.x, touchPos.y)) {
                modoEscolhido = VERTICAL;
                botaoSelecionar.setHabilitado(true);
            }
            if (botaoHorizontal.foiClicado(touchPos.x, touchPos.y)) {
                modoEscolhido = HORIZONTAL;
                botaoSelecionar.setHabilitado(true);
            }
            if (botaoCombinado.foiClicado(touchPos.x, touchPos.y)) {
                modoEscolhido = COMBINADO;
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

        font.getData().setScale(0.014f);
        font.draw(batch, "Dorsiflexao e flexao plantar: o carro sobe e desce na pista.", 3.9f, 3.95f, 3.8f, Align.left, true);
        font.draw(batch, "Inversao e eversao: o carro troca de faixa (esquerda e direita).", 3.9f, 3.05f, 3.8f, Align.left, true);
        font.draw(batch, "Os dois eixos ao mesmo tempo, com mais controle.", 3.9f, 2.15f, 3.8f, Align.left, true);
        font.getData().setScale(0.02f);

        font.draw(batch, "Modo selecionado: " + getNomeModo(), 0, 1.3f, viewport.getWorldWidth(), Align.center, false);
        batch.end();
    }
}