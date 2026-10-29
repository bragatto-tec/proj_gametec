package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.concurrent.CompletableFuture;

/** Tela para enviar as credenciais Wi-Fi ao ESP32 conectado por USB. */
public class TelaConfiguracaoWifi {
    private final FitViewport viewport;
    private final BitmapFont font;
    private final SensorClient sensorClient;
    private final Vector2 touchPos = new Vector2();
    private String ssid = "";
    private String senha = "";
    private int campoEditando;
    private boolean voltarSolicitado;
    private boolean conexaoWifiSolicitada;
    private boolean resultadoPronto;
    private boolean resultadoOk;
    private CompletableFuture<Boolean> pedidoWifi;

    public TelaConfiguracaoWifi(FitViewport viewport, BitmapFont font, SensorClient sensorClient) {
        this.viewport = viewport;
        this.font = font;
        this.sensorClient = sensorClient;
    }

    public void abrir() {
        voltarSolicitado = false;
        conexaoWifiSolicitada = false;
        resultadoPronto = false;
        resultadoOk = false;
        pedidoWifi = null;
    }

    public void fechar() {
        voltarSolicitado = false;
        conexaoWifiSolicitada = false;
    }

    public void input() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPos);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            voltarSolicitado = true;
            return;
        }
        if (pedidoWifi != null && pedidoWifi.isDone()) {
            try {
                resultadoOk = pedidoWifi.get();
            } catch (Exception e) {
                resultadoOk = false;
            }
            resultadoPronto = true;
            pedidoWifi = null;
        }
        processarTeclado();
        if (!Gdx.input.justTouched()) return;
        float x = touchPos.x, y = touchPos.y;
        if (y >= 3.25f && y <= 3.9f) {
            campoEditando = 0;
        } else if (y >= 2.35f && y < 3.0f) {
            campoEditando = 1;
        } else if (x >= 4.2f && y >= 0.2f && y <= 0.9f) {
            if (pedidoWifi == null && !conexaoWifiSolicitada) {
                if (resultadoPronto && resultadoOk) {
                    conexaoWifiSolicitada = true;
                } else if (!ssid.trim().isEmpty()) {
                    resultadoPronto = false;
                    resultadoOk = false;
                    pedidoWifi = sensorClient.setWifiCredentials(ssid.trim(), senha);
                }
            }
        } else if (x >= 1.0f && x <= 3.7f && y >= 0.2f && y <= 0.9f) {
            voltarSolicitado = true;
        }
    }

    private void processarTeclado() {
        String valor = valorCampo();
        int limite = campoEditando == 1 ? 63 : 32;
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && !valor.isEmpty()) {
            valor = valor.substring(0, valor.length() - 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            campoEditando = (campoEditando + 1) % 2;
            return;
        } else if (valor.length() < limite) {
            boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
            for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
                if (Gdx.input.isKeyJustPressed(key)) {
                    char c = (char) ((shift ? 'A' : 'a') + key - Input.Keys.A);
                    valor += c;
                    break;
                }
            }
            for (int key = Input.Keys.NUM_0; key <= Input.Keys.NUM_9 && valor.length() < limite; key++) {
                if (Gdx.input.isKeyJustPressed(key)) {
                    char c = (char) ('0' + key - Input.Keys.NUM_0);
                    valor += shift ? ")!@#$%^&*(".charAt(c - '0') : c;
                    break;
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) valor += ' ';
            if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) valor += shift ? '_' : '-';
            if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) valor += '.';
            if (Gdx.input.isKeyJustPressed(Input.Keys.COMMA)) valor += shift ? '<' : ',';
            if (Gdx.input.isKeyJustPressed(Input.Keys.SLASH)) valor += shift ? '?' : '/';
            if (Gdx.input.isKeyJustPressed(Input.Keys.SEMICOLON)) valor += shift ? ':' : ';';
            if (Gdx.input.isKeyJustPressed(Input.Keys.APOSTROPHE)) valor += shift ? '"' : '\'';
            if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) valor += shift ? '+' : '=';
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) valor += shift ? '{' : '[';
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) valor += shift ? '}' : ']';
            if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSLASH)) valor += shift ? '|' : '\\';
            if (Gdx.input.isKeyJustPressed(Input.Keys.GRAVE)) valor += shift ? '~' : '`';
        }
        definirValorCampo(valor);
    }

    private String valorCampo() {
        return campoEditando == 1 ? senha : ssid;
    }

    private void definirValorCampo(String valor) {
        if (campoEditando == 1) senha = valor;
        else ssid = valor;
    }

    public boolean isVoltarSolicitado() { return voltarSolicitado; }
    public boolean isConexaoWifiSolicitada() { return conexaoWifiSolicitada; }

    public void draw(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float largura = viewport.getWorldWidth();
        batch.begin();
        Ui.fundo(batch, largura, viewport.getWorldHeight());
        Ui.texto(batch, font, "CONFIGURAR WI-FI", 0, 4.85f, largura, Align.center, 0.023f, Color.WHITE, false);
        Ui.texto(batch, font, "Informe a rede Wi-Fi para o ESP32.", 0, 4.35f, largura, Align.center, 0.014f, Ui.MUTED, false);
        Ui.painel(batch, Ui.Estilo.NEUTRO, 1.0f, 3.25f, 6.0f, 0.65f);
        Ui.painel(batch, Ui.Estilo.NEUTRO, 1.0f, 2.35f, 6.0f, 0.65f);
        Ui.texto(batch, font, campoEditando == 0 ? "REDE (SSID)  •  EDITANDO" : "REDE (SSID)", 1.2f, 3.8f, 5.6f, Align.left, 0.011f, campoEditando == 0 ? Ui.AZUL : Ui.MUTED, false);
        Ui.texto(batch, font, ssid.isEmpty() ? "Digite o nome da rede" : ssid, 1.2f, 3.52f, 5.6f, Align.left, 0.015f, Color.WHITE, false);
        Ui.texto(batch, font, campoEditando == 1 ? "SENHA  •  EDITANDO" : "SENHA", 1.2f, 2.9f, 5.6f, Align.left, 0.011f, campoEditando == 1 ? Ui.AZUL : Ui.MUTED, false);
        Ui.texto(batch, font, senha.isEmpty() ? "Digite a senha" : mascararSenha(), 1.2f, 2.62f, 5.6f, Align.left, 0.015f, Color.WHITE, false);
        String status = sensorClient.getStatusWifi();
        if (status == null || status.isEmpty()) status = "Aguardando envio das credenciais.";
        Ui.Estilo estiloStatus = pedidoWifi != null ? Ui.Estilo.AZUL
                : resultadoPronto ? (resultadoOk ? Ui.Estilo.VERDE : Ui.Estilo.VERMELHO) : Ui.Estilo.NEUTRO;
        if (resultadoPronto && resultadoOk) {
            status = "Wi-Fi configurado com sucesso. Toque em CONECTAR para usar.";
        } else if (pedidoWifi != null) {
            status = "Enviando credenciais e aguardando resposta do ESP32...";
        }
        Ui.painel(batch, estiloStatus, 1.0f, 0.95f, 6.0f, 0.42f);
        Ui.textoNaCaixa(batch, font, status, 1.1f, 0.95f, 5.8f, 0.42f, Color.WHITE, 0.012f, 0.009f, 0.008f);
        Ui.texto(batch, font, "VOLTAR", 1.0f, 0.55f, 2.7f, Align.center, 0.016f, Color.WHITE, false);
        String textoAcao = pedidoWifi != null ? "ENVIANDO..."
                : resultadoPronto && resultadoOk ? "CONECTAR"
                : resultadoPronto ? "TENTAR NOVAMENTE" : "ENVIAR";
        Ui.texto(batch, font, textoAcao, 4.2f, 0.55f, 2.8f, Align.center, 0.016f, Color.WHITE, false);
        batch.end();
    }

    private String mascararSenha() {
        StringBuilder mascara = new StringBuilder();
        for (int i = 0; i < senha.length(); i++) mascara.append('*');
        return mascara.toString();
    }

}
