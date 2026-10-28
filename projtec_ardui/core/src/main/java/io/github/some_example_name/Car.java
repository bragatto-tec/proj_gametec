package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Car extends GameObject {

    public static final float ANGULO_ENTRADA_FAIXA = 5f;
    private static final float ANGULO_SAIDA_FAIXA = 3f;
    private static final float FILTRO_INCLINACAO_SEGUNDOS = 0.12f;
    private static final float RESPOSTA_DIRECAO = 12f;
    private static final float QUADROS_POR_SEGUNDO = 8f;

    private final TextureRegion[][] quadros;
    private final Rectangle hitbox = new Rectangle();
    private final float largura;
    private final float altura;
    private final float y;

    private float centroX;
    private int via;
    private float tempoAnimacao;
    private float inclinacaoFiltrada;
    private boolean inclinacaoInicializada;

    public Car(Texture folha, float y, float largura) {
        super(folha, 0, y, largura, largura);
        // split() entrega as linhas na ordem visual, de cima para baixo.
        quadros = TextureRegion.split(folha, folha.getWidth() / 4, folha.getHeight() / 5);
        this.largura = largura;
        this.altura = largura * quadros[0][0].getRegionHeight() / quadros[0][0].getRegionWidth();
        this.y = y;
        sprite.setRegion(quadros[0][0]);
        sprite.setSize(largura, altura);
        reiniciar();
    }

    public void reiniciar() {
        via = GerenciadorPista.VIA_CENTRO;
        inclinacaoInicializada = false;
        tempoAnimacao = 0f;
        centroX = GerenciadorPista.xDaVia(via, 1f);
        sprite.setRegion(quadros[0][0]);
        sprite.setPosition(centroX - largura / 2f, y);
    }

    /** Teclado (teste sem o ESP32): uma via por toque. */
    public void moverEsquerda() {
        via = Math.max(GerenciadorPista.VIA_ESQUERDA, via - 1);
    }

    public void moverDireita() {
        via = Math.min(GerenciadorPista.VIA_DIREITA, via + 1);
    }

    /** Sensor: suaviza o angulo lido e escolhe a via pela inclinacao. */
    public void seguirInclinacao(float angulo, float dt) {
        if (!inclinacaoInicializada) {
            inclinacaoFiltrada = angulo;
            inclinacaoInicializada = true;
        } else {
            // Suaviza ruido do MPU entre leituras sem atrasar demais a resposta.
            float peso = 1f - (float) Math.exp(-dt / FILTRO_INCLINACAO_SEGUNDOS);
            inclinacaoFiltrada += (angulo - inclinacaoFiltrada) * peso;
        }
        via = viaPelaInclinacao(inclinacaoFiltrada);
    }

    /** Inclina para a faixa lateral; ao voltar quase ao nivel, retorna ao centro (histerese). */
    private int viaPelaInclinacao(float angulo) {
        if (angulo >= ANGULO_ENTRADA_FAIXA) return GerenciadorPista.VIA_DIREITA;
        if (angulo <= -ANGULO_ENTRADA_FAIXA) return GerenciadorPista.VIA_ESQUERDA;

        if (via == GerenciadorPista.VIA_DIREITA && angulo > ANGULO_SAIDA_FAIXA) {
            return GerenciadorPista.VIA_DIREITA;
        }
        if (via == GerenciadorPista.VIA_ESQUERDA && angulo < -ANGULO_SAIDA_FAIXA) {
            return GerenciadorPista.VIA_ESQUERDA;
        }
        return GerenciadorPista.VIA_CENTRO;
    }

    @Override
    public void update(float dt) {
        tempoAnimacao += dt;

        int linha = 0;
        if (via < GerenciadorPista.VIA_CENTRO) {
            linha = 1;
        } else if (via > GerenciadorPista.VIA_CENTRO) {
            linha = 2;
        }
        int coluna = (int) (tempoAnimacao * QUADROS_POR_SEGUNDO) % quadros[linha].length;
        sprite.setRegion(quadros[linha][coluna]);

        // Desliza ate o centro da via escolhida.
        float alvoX = GerenciadorPista.xDaVia(via, 1f);
        float peso = 1f - (float) Math.exp(-RESPOSTA_DIRECAO * dt);
        centroX += (alvoX - centroX) * peso;
        sprite.setPosition(centroX - largura / 2f, y);
    }

    /** Hitbox um pouco menor que o sprite, para a colisao nao parecer injusta. */
    @Override
    public Rectangle getBounds() {
        float mx = largura * 0.15f;
        float my = altura * 0.12f;
        hitbox.set(centroX - largura / 2f + mx, y + my, largura - 2f * mx, altura - 2f * my);
        return hitbox;
    }
}