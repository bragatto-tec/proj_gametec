package io.github.some_example_name;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Cliente HTTP do ESP32 + MPU6050.
 *
 * Roda numa thread própria (nunca trava o render). Se nenhum endereço for
 * informado, descobre sozinho o ESP32 testando, em ordem:
 *   1) o endereço configurado (--sensor-url / -Dsensor.baseUrl / SENSOR_BASE_URL)
 *   2) http://192.168.4.1     -> Wi-Fi próprio do ESP32 ("RehabCar"), IP sempre fixo
 *   3) http://rehabcar.local  -> mDNS, quando o ESP32 está na mesma rede do PC
 *   4) http://esp32.local     -> nome antigo
 */
public class SensorClient {

    private static final String[] PADROES = {
        "http://192.168.4.1",
        "http://rehabcar.local",
        "http://esp32.local"
    };

    private static final int TIMEOUT_DESCOBERTA_MS = 1500;
    private static final int TIMEOUT_NORMAL_MS = 2500;
    private static final long INTERVALO_DADOS_MS = 250L;       // 4 leituras/s para evitar sobrecarregar o WebServer
    private static final long INTERVALO_CALIBRACAO_MS = 250L;
    private static final int FALHAS_PARA_DESCONECTAR = 4;

    private final List<String> candidatos = new ArrayList<String>();
    private volatile String baseAtiva;           // null = ainda procurando
    private int falhasSeguidas = 0;              // só usada na thread de rede

    private volatile boolean conectado;
    private volatile boolean sensorPronto;
    private volatile boolean emCalibracao;
    private volatile boolean calibracaoFalhou;
    private volatile boolean calibracaoConcluida;
    private volatile boolean pedidoCalibracaoEnviado;
    private volatile boolean workerIniciado;
    private volatile boolean rodando = true;
    private volatile int progresso;
    private volatile float angX;
    private volatile float angY;
    private volatile float temp;
    private volatile String mensagemErro = "";
    private int falhasProgressoCalibracao;

    /** Descoberta automática (aceita override por propriedade/variável de ambiente). */
    public SensorClient() {
        this(urlBaseConfiguravel());
    }

    /** Endereços a tentar, em ordem. null/vazio são ignorados; os padrões entram no fim. */
    public SensorClient(String... bases) {
        if (bases != null) {
            for (String b : bases) {
                adicionarCandidato(b);
            }
        }
        for (String p : PADROES) {
            adicionarCandidato(p);
        }
    }

    private void adicionarCandidato(String base) {
        if (base == null) return;
        String url = base.trim();
        if (url.isEmpty()) return;
        if (!url.contains("://")) url = "http://" + url;   // "192.168.1.50" -> "http://192.168.1.50"
        while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        if (!candidatos.contains(url)) candidatos.add(url);
    }

    private static String urlBaseConfiguravel() {
        String url = System.getProperty("sensor.baseUrl");
        if (url == null || url.trim().isEmpty()) url = System.getenv("SENSOR_BASE_URL");
        return url == null || url.trim().isEmpty() ? null : url.trim();
    }

    // ------------------------------------------------------------------
    // API usada pelo jogo (inalterada)
    // ------------------------------------------------------------------
    /** Pode ser chamado todo frame; só inicia a thread na primeira vez. */
    public void update(float dt) {
        if (workerIniciado) return;
        synchronized (this) {
            if (workerIniciado) return;
            workerIniciado = true;
        }
        Thread thread = new Thread(new Runnable() {
            @Override public void run() { executarComunicacao(); }
        }, "sensor-http");
        thread.setDaemon(true);
        thread.start();
    }

    public boolean conectado()  { return conectado; }
    public boolean sensorPronto() { return sensorPronto; }
    public boolean calibrando() { return emCalibracao; }
    public boolean calibracaoFalhou() { return calibracaoFalhou; }
    public boolean calibracaoConcluidaComSucesso() { return calibracaoConcluida; }
    public int getProgresso()   { return progresso; }
    public float getAngX()      { return angX; }
    public float getAngY()      { return angY; }
    public float getTemp()      { return temp; }
    public String getMensagemErro() { return mensagemErro; }

    /** Endereço do ESP32 que está respondendo (ou "" se ainda não achou). */
    public String getUrlAtual() {
        String b = baseAtiva;
        return b == null ? "" : b;
    }

    public void iniciarCalibracao() {
        if (!sensorPronto || emCalibracao) return;
        calibracaoFalhou = false;
        calibracaoConcluida = false;
        progresso = 0;
        pedidoCalibracaoEnviado = false;
        falhasProgressoCalibracao = 0;
        mensagemErro = "";
        emCalibracao = true;
    }

    /** Apaga o resultado da última calibração (chamar ao abrir a tela). */
    public void limparResultado() {
        if (emCalibracao) return;
        calibracaoFalhou = false;
        calibracaoConcluida = false;
        progresso = 0;
    }

    /** Encerra a thread de rede (chamar no dispose do jogo). */
    public void parar() {
        rodando = false;
    }

    // ------------------------------------------------------------------
    // Thread de rede
    // ------------------------------------------------------------------
    private void executarComunicacao() {
        while (rodando) {
            long inicio = System.currentTimeMillis();
            try {
                if (baseAtiva == null) {
                    descobrir();
                } else if (emCalibracao) {
                    atualizarCalibracao();
                } else {
                    atualizarDados();
                }
            } catch (RuntimeException e) {
                mensagemErro = "Erro inesperado: " + e;
            }

            long intervalo = emCalibracao ? INTERVALO_CALIBRACAO_MS : INTERVALO_DADOS_MS;
            long espera = intervalo - (System.currentTimeMillis() - inicio);
            if (baseAtiva == null) espera = Math.max(espera, 300L);  // não martelar a rede enquanto procura
            try {
                if (espera > 0) Thread.sleep(espera);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                rodando = false;
            }
        }
    }

    /** Testa cada endereço candidato até algum responder /api/dados com JSON válido. */
    private void descobrir() {
        StringBuilder tentados = new StringBuilder();
        for (String base : candidatos) {
            if (!rodando) return;
            String payload = lerPayload(base, "/api/dados", TIMEOUT_DESCOBERTA_MS);
            if (payload != null && interpretarJson(payload) != null) {
                baseAtiva = base;
                falhasSeguidas = 0;
                conectado = true;
                processarDados(payload);
                return;
            }
            if (tentados.length() > 0) tentados.append(", ");
            tentados.append(base);
        }
        conectado = false;
        sensorPronto = false;
        mensagemErro = "ESP32 nao encontrado (tentei " + tentados + "). "
            + "Conecte o PC ao Wi-Fi \"RehabCar\" ou use --sensor-url=IP";
    }

    private void atualizarDados() {
        String payload = lerPayload(baseAtiva, "/api/dados", TIMEOUT_NORMAL_MS);
        if (payload == null) {
            if (++falhasSeguidas >= FALHAS_PARA_DESCONECTAR) {
                conectado = false;
                sensorPronto = false;
                baseAtiva = null;          // volta a procurar (o IP pode ter mudado)
            }
            return;
        }
        falhasSeguidas = 0;
        conectado = true;
        processarDados(payload);
    }

    private void processarDados(String payload) {
        if (!calibracaoFalhou) mensagemErro = "";
        JsonValue dados = interpretarJson(payload);
        if (dados == null) { sensorPronto = false; return; }

        Float leituraX = extrairNumero(dados, "angX");
        Float leituraY = extrairNumero(dados, "angY");
        if (leituraX == null || leituraY == null) {
            mensagemErro = "A resposta do ESP32 precisa conter angX e angY numericos.";
            sensorPronto = false;
            return;
        }
        JsonValue estadoSensor = dados.get("ok");
        if (estadoSensor != null && (!estadoSensor.isBoolean() || !estadoSensor.asBoolean())) {
            mensagemErro = "ESP32 conectado, mas o MPU6050 nao esta fornecendo leituras.";
            sensorPronto = false;
            return;
        }
        angX = -leituraY;
        angY = leituraX;
        if (dados.has("temp")) {
            Float t = extrairNumero(dados, "temp");
            if (t != null) temp = t;
        }
        sensorPronto = true;
    }

    private void atualizarCalibracao() {
        if (!pedidoCalibracaoEnviado) {
            String resposta = lerPayload(baseAtiva, "/api/calibrar", TIMEOUT_NORMAL_MS);
            if (resposta == null) {
                falharCalibracao("Nao foi possivel iniciar a calibracao: " + mensagemErro);
                return;
            }
            if (!"OK".equalsIgnoreCase(resposta.trim())) {
                falharCalibracao("Resposta inesperada de /api/calibrar: " + resposta.trim());
                return;
            }
            pedidoCalibracaoEnviado = true;
            falhasProgressoCalibracao = 0;
        }

        String payload = lerPayload(baseAtiva, "/api/progresso", TIMEOUT_NORMAL_MS);
        if (payload == null || payload.trim().isEmpty()) {
            falhaTemporariaProgresso(payload == null ? mensagemErro : "resposta vazia");
            return;
        }
        JsonValue status = interpretarJson(payload);
        if (status == null) {
            falhaTemporariaProgresso("resposta JSON inválida");
            return;
        }
        Float percentual = extrairNumero(status, "progresso");
        if (percentual == null) {
            falhaTemporariaProgresso("campo progresso ausente ou inválido");
            return;
        }
        falhasProgressoCalibracao = 0;
        conectado = true;
        mensagemErro = "";
        progresso = Math.max(0, Math.min(100, Math.round(percentual)));

        if (progresso >= 100) {
            calibracaoConcluida = true;
            sensorPronto = true;
            emCalibracao = false;
            pedidoCalibracaoEnviado = false;
        }
    }

    private void falhaTemporariaProgresso(String detalhe) {
        falhasProgressoCalibracao++;
        if (falhasProgressoCalibracao >= 3) {
            falharCalibracao("Sem resposta válida de /api/progresso após "
                    + falhasProgressoCalibracao + " tentativas: " + detalhe);
            return;
        }
        mensagemErro = "Falha temporária lendo o progresso ("
                + falhasProgressoCalibracao + "/3). Tentando novamente: " + detalhe;
    }

    private void falharCalibracao(String mensagem) {
        mensagemErro = mensagem;
        calibracaoFalhou = true;
        emCalibracao = false;
        pedidoCalibracaoEnviado = false;
        // volta a validar a conexao no proximo ciclo (atualizarDados decide conectado/sensorPronto)
        sensorPronto = false;
    }

    // ------------------------------------------------------------------
    // HTTP / JSON
    // ------------------------------------------------------------------
    private JsonValue interpretarJson(String payload) {
        try {
            JsonValue json = new JsonReader().parse(payload);
            if (json == null || !json.isObject()) {
                mensagemErro = "A API retornou JSON que nao e um objeto.";
                return null;
            }
            return json;
        } catch (RuntimeException e) {
            mensagemErro = "JSON invalido recebido do ESP32.";
            return null;
        }
    }

    private Float extrairNumero(JsonValue json, String chave) {
        JsonValue valor = json.get(chave);
        if (valor == null || !valor.isNumber()) return null;
        float numero = valor.asFloat();
        return Float.isNaN(numero) || Float.isInfinite(numero) ? null : numero;
    }

    /** GET simples. Devolve o corpo, ou null (com mensagemErro preenchida) em qualquer falha. */
    private String lerPayload(String base, String endpoint, int timeoutMs) {
        HttpURLConnection conexao = null;
        try {
            conexao = (HttpURLConnection) new URL(base + endpoint).openConnection();
            conexao.setRequestMethod("GET");
            conexao.setConnectTimeout(timeoutMs);
            conexao.setReadTimeout(timeoutMs);
            conexao.setUseCaches(false);
            conexao.setRequestProperty("Connection", "close");

            int status = conexao.getResponseCode();
            if (status < 200 || status >= 300) {
                String detalhe = lerCorpo(conexao.getErrorStream());
                mensagemErro = "GET " + endpoint + " retornou HTTP " + status
                    + (detalhe.isEmpty() ? "" : ": " + detalhe);
                return null;
            }
            return lerCorpo(conexao.getInputStream());
        } catch (SocketTimeoutException e) {
            mensagemErro = "Tempo esgotado ao consultar " + endpoint + " em " + base
                    + ". Verifique o Wi-Fi e se o ESP32 responde a essa rota.";
            return null;
        } catch (Exception e) {
            String detalhe = e.getMessage();
            mensagemErro = base + endpoint + ": " + (detalhe == null || detalhe.trim().isEmpty()
                ? e.getClass().getSimpleName() : detalhe);
            return null;
        } finally {
            if (conexao != null) conexao.disconnect();
        }
    }

    private String lerCorpo(InputStream stream) throws Exception {
        if (stream == null) return "";
        BufferedReader leitor = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        try {
            StringBuilder resposta = new StringBuilder();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (resposta.length() > 0) resposta.append('\n');
                resposta.append(linha);
            }
            return resposta.toString();
        } finally {
            leitor.close();
        }
    }
}
