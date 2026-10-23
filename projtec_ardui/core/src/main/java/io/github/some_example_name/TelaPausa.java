package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaPausa {
    private final FitViewport viewport;
    private final BitmapFont font;
    private final Vector2 touchPos;
    private final UiAssets uiAssets;

    private final Button botaoContinuar;
    private final Button botaoReiniciar;
    private final Button botaoRecalibrar;
    private final Button botaoMenu;
    private final Button botaoConfirmar;
    private final Button botaoCancelar;

    private boolean confirmando;
    private boolean continuarSolicitado;
    private boolean reiniciarSolicitado;
    private boolean recalibrarSolicitado;
    private boolean menuSolicitado;

    public TelaPausa(FitViewport viewport, BitmapFont font, UiAssets uiAssets) {
        this.viewport = viewport;
        this.font = font;
        this.uiAssets = uiAssets;
        this.touchPos = new Vector2();

        this.botaoContinuar = new Button(uiAssets, "continuar", 2.4f, 3.4f, 3.2f, 0.6f);
        this.botaoReiniciar = new Button(uiAssets, "reiniciar_partida", 2.4f, 2.6f, 3.2f, 0.6f);
        this.botaoRecalibrar = new Button(uiAssets, "recalibrar_sensor", 2.4f, 1.8f, 3.2f, 0.6f);
        this.botaoMenu = new Button(uiAssets, "voltar_ao_menu", 2.4f, 1.0f, 3.2f, 0.6f);

        this.botaoConfirmar = new Button(uiAssets, "sim_voltar_ao_menu", 2.4f, 2.0f, 3.2f, 0.6f);
        this.botaoCancelar = new Button(uiAssets, "cancelar", 2.4f, 1.2f, 3.2f, 0.6f);
    }

    public void abrir() {
        confirmando = false;
        continuarSolicitado = false;
        reiniciarSolicitado = false;
        recalibrarSolicitado = false;
        menuSolicitado = false;
    }

    public void fechar() {
        confirmando = false;
        continuarSolicitado = false;
        reiniciarSolicitado = false;
        recalibrarSolicitado = false;
        menuSolicitado = false;
    }

    public void input() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPos);
        atualizarEstadosBotoes();
        boolean teclaPausa = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.P);

        if (confirmando) {
            if (teclaPausa) {
                confirmando = false;
                return;
            }
            if (Gdx.input.justTouched()) {
                if (botaoConfirmar.foiClicado(touchPos.x, touchPos.y)) {
                    menuSolicitado = true;
                    confirmando = false;
                }
                if (botaoCancelar.foiClicado(touchPos.x, touchPos.y)) {
                    confirmando = false;
                }
            }
            return;
        }

        if (teclaPausa) {
            continuarSolicitado = true;
            return;
        }

        if (Gdx.input.justTouched()) {
            if (botaoContinuar.foiClicado(touchPos.x, touchPos.y)) {
                continuarSolicitado = true;
            }
            if (botaoReiniciar.foiClicado(touchPos.x, touchPos.y)) {
                reiniciarSolicitado = true;
            }
            if (botaoRecalibrar.foiClicado(touchPos.x, touchPos.y)) {
                recalibrarSolicitado = true;
            }
            if (botaoMenu.foiClicado(touchPos.x, touchPos.y)) {
                confirmando = true;
            }
        }
    }

    public boolean isContinuarSolicitado() {
        return continuarSolicitado;
    }

    public boolean isReiniciarSolicitado() {
        return reiniciarSolicitado;
    }

    public boolean isRecalibrarSolicitado() {
        return recalibrarSolicitado;
    }

    public boolean isMenuSolicitado() {
        return menuSolicitado;
    }

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.setColor(0f, 0f, 0f, 0.75f);
        batch.draw(uiAssets.whitePixel(), 2.0f, 0.55f, 4.0f, 4.1f);
        batch.setColor(Color.WHITE);

        if (confirmando) {
            font.draw(batch, "ABANDONAR PARTIDA?", 2.0f, 4.35f, 4.0f, Align.center, false);
            font.getData().setScale(0.016f);
            font.draw(batch, "A partida em andamento sera perdida.", 2.2f, 3.6f, 3.6f, Align.center, true);
            font.getData().setScale(0.02f);
            botaoConfirmar.draw(batch);
            botaoCancelar.draw(batch);
        } else {
            font.draw(batch, "JOGO PAUSADO", 2.0f, 4.35f, 4.0f, Align.center, false);
            botaoContinuar.draw(batch);
            botaoReiniciar.draw(batch);
            botaoRecalibrar.draw(batch);
            botaoMenu.draw(batch);
        }
        batch.end();
    }

    private void atualizarEstadosBotoes() {
        botaoContinuar.atualizarEstado(touchPos);
        botaoReiniciar.atualizarEstado(touchPos);
        botaoRecalibrar.atualizarEstado(touchPos);
        botaoMenu.atualizarEstado(touchPos);
        botaoConfirmar.atualizarEstado(touchPos);
        botaoCancelar.atualizarEstado(touchPos);
    }
}
