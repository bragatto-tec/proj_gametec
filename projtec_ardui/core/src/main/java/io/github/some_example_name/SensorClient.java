package io.github.some_example_name;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.fazecast.jSerialComm.SerialPort;

/**
 * Cliente do ESP32 + MPU6050, com transporte USB serial ou HTTP.
 *
 * Toda leitura, calibração e escrita serial/HTTP acontece na thread de trabalho.
 * Os métodos chamados pela renderização somente consultam snapshots ou enfileiram
 * operações, sem aguardar operações de entrada/saída.
 */
public class SensorClient {

    public enum ConnectionMode {
        AUTO,
        SERIAL,
        WIFI
    }

    /** Snapshot imutável da última telemetria recebida; consultar não bloqueia. */
    public static final class SensorData {
        public final float angX;
        public final float angY;
        public final float temp;
        public final boolean ok;

        private SensorData(float angX, float angY, float temp, boolean ok) {
            this.angX = angX;
            this.angY = angY;
            this.temp = temp;
            this.ok = ok;
        }
    }

    private static final String PREFIXO_SERIAL = "serial:";
    private static final String SERIAL_AUTO = "serial:auto";
    private static final String[] PADROES = {
        SERIAL_AUTO,
        "http://192.168.4.1",
        "http://rehabcar.local",
        "http://esp32.local"
    };

    private static final int BAUD_SERIAL = 115200;
    private static final int TIMEOUT_SERIAL_MS = 1200;
    private static final int TIMEOUT_DESCOBERTA_SERIAL_MS = 5000;
    private static final long INTERVALO_BUSCA_SERIAL_MS = 4000L;
    private static final int TIMEOUT_DESCOBERTA_MS = 1500;
    private static final int TIMEOUT_NORMAL_MS = 2500;
    private static final long INTERVALO_DADOS_MS = 250L;
    private static final long INTERVALO_CALIBRACAO_MS = 250L;
    private static final int FALHAS_PARA_DESCONECTAR = 4;

    private final List<String> candidatos = new ArrayList<String>();
    private final ConnectionMode connectionMode;
    private final AtomicReference<WifiRequest> pedidoWifiPendente =
            new AtomicReference<WifiRequest>();
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
    private volatile String statusWifi = "";
    private int falhasProgressoCalibracao;
    private SerialPort portaSerial;
    private long ultimaBuscaSerial;
    private boolean serialIndisponivel;

    /** Descoberta automática (aceita override por propriedade/variável de ambiente). */
    public SensorClient() {
        this(ConnectionMode.AUTO, urlBaseConfiguravel());
    }

    /** Endereços a tentar, em ordem. null/vazio são ignorados; os padrões entram no fim. */
    public SensorClient(String... bases) {
        this(ConnectionMode.AUTO, bases);
    }

    /**
     * Seleciona o transporte. Em SERIAL, informe opcionalmente "serial:auto",
     * "COM5" ou "/dev/ttyUSB0"; em WIFI, informe IP, hostname ou URL.
     * AUTO mantém a descoberta serial seguida dos endereços Wi-Fi padrão.
     */
    public SensorClient(ConnectionMode mode, String... bases) {
        if (mode == null) throw new IllegalArgumentException("O modo de conexão não pode ser nulo.");
        this.connectionMode = mode;
        if (bases != null) {
            for (String b : bases) {
                adicionarCandidato(b);
            }
        }
        for (String p : PADROES) {
            if (mode == ConnectionMode.AUTO
                    || (mode == ConnectionMode.SERIAL && ehSerial(p))
                    || (mode == ConnectionMode.WIFI && !ehSerial(p))) {
                adicionarCandidato(p);
            }
        }
        if (mode == ConnectionMode.SERIAL && candidatos.isEmpty()) {
            adicionarCandidato(SERIAL_AUTO);
        }
    }

    private void adicionarCandidato(String base) {
        if (base == null) return;
        String url = base.trim();
        if (url.isEmpty()) return;
        if (ehNomePortaSerial(url)) url = PREFIXO_SERIAL + url;
        if (url.regionMatches(true, 0, PREFIXO_SERIAL, 0, PREFIXO_SERIAL.length())) {
            String porta = url.substring(PREFIXO_SERIAL.length()).trim();
            url = porta.isEmpty() || "auto".equalsIgnoreCase(porta)
                    ? SERIAL_AUTO : PREFIXO_SERIAL + porta;
        } else {
            if (connectionMode == ConnectionMode.SERIAL) {
                throw new IllegalArgumentException("Endereço incompatível com SERIAL: " + url);
            }
            if (!url.contains("://")) url = "http://" + url;
            while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        }
        if (connectionMode == ConnectionMode.WIFI && ehSerial(url)) {
            throw new IllegalArgumentException("Endereço serial incompatível com WIFI: " + url);
        }
        if (!candidatos.contains(url)) candidatos.add(url);
    }

    private static boolean ehNomePortaSerial(String valor) {
        String lower = valor.toLowerCase(Locale.ROOT);
        return lower.matches("com\\d+") || lower.startsWith("/dev/tty");
    }

    private static boolean ehSerial(String base) {
        return base != null && base.startsWith(PREFIXO_SERIAL);
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
    public int obterProgressoCalibracao() { return progresso; }
    public float getAngX()      { return angX; }
    public float getAngY()      { return angY; }
    public float getTemp()      { return temp; }
    public String getMensagemErro() { return mensagemErro; }
    public String getStatusWifi() { return statusWifi; }

    /**
     * Retorna imediatamente a última telemetria completa recebida pela thread
     * de comunicação. Antes da primeira resposta, {@code ok} é false.
     */
    public SensorData obterDados() {
        return new SensorData(angX, angY, temp, sensorPronto);
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public boolean viaCabo() {
        return ehSerial(baseAtiva);
    }

    /** Endereço do ESP32 que está respondendo (ou "" se ainda não achou). */
    public String getUrlAtual() {
        String b = baseAtiva;
        return b == null ? "" : b;
    }

    /** Endereço curto para a interface, por exemplo USB (COM5) ou 192.168.4.1. */
    public String getEnderecoAmigavel() {
        String base = baseAtiva;
        if (base == null) return "";
        return ehSerial(base) ? "USB (" + base.substring(PREFIXO_SERIAL.length()) + ")"
                : base.replace("http://", "");
    }

    /**
     * Enfileira SETWIFI e completa o future quando o ESP32 responder.
     * O future não bloqueia o chamador; aguardar seu resultado deve ser feito
     * fora da thread de renderização.
     */
    public CompletableFuture<Boolean> setWifiCredentials(String ssid, String password) {
        if (ssid == null || ssid.trim().isEmpty() || password == null || password.isEmpty()) {
            return rejeitarPedidoWifi("Informe o SSID e a senha da rede Wi-Fi.");
        }
        if (campoWifiInvalido(ssid) || campoWifiInvalido(password)) {
            return rejeitarPedidoWifi("SSID e senha não podem conter ':' ou quebra de linha.");
        }
        if (!rodando) return rejeitarPedidoWifi("O cliente do sensor foi encerrado.");
        if (!viaCabo()) {
            return rejeitarPedidoWifi("Conecte o ESP32 por USB antes de alterar o Wi-Fi.");
        }

        WifiRequest request = new WifiRequest(ssid, password);
        if (!pedidoWifiPendente.compareAndSet(null, request)) {
            return rejeitarPedidoWifi("Já existe uma alteração de Wi-Fi em andamento.");
        }
        statusWifi = "Enviando credenciais ao ESP32...";
        return request.resultado;
    }

    private CompletableFuture<Boolean> rejeitarPedidoWifi(String motivo) {
        statusWifi = motivo;
        CompletableFuture<Boolean> resultado = new CompletableFuture<Boolean>();
        resultado.complete(Boolean.FALSE);
        return resultado;
    }

    private static boolean campoWifiInvalido(String valor) {
        return valor.indexOf(':') >= 0 || valor.indexOf('\n') >= 0 || valor.indexOf('\r') >= 0;
    }

    public void iniciarCalibracao() {
        solicitarCalibracao();
    }

    /** Agenda a calibração pelo transporte ativo, sem bloquear a interface. */
    public boolean solicitarCalibracao() {
        if (!sensorPronto || emCalibracao) return false;
        calibracaoFalhou = false;
        calibracaoConcluida = false;
        progresso = 0;
        pedidoCalibracaoEnviado = false;
        falhasProgressoCalibracao = 0;
        mensagemErro = "";
        emCalibracao = true;
        return true;
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
        try {
            while (rodando) {
                long inicio = System.currentTimeMillis();
                try {
                    processarPedidoWifi();
                    if (baseAtiva == null) {
                        descobrir();
                    } else if (emCalibracao) {
                        atualizarCalibracao();
                    } else {
                        atualizarDados();
                    }
                } catch (RuntimeException e) {
                    mensagemErro = "Erro inesperado na comunicação com o ESP32: " + e.getMessage();
                }

                long intervalo = emCalibracao ? INTERVALO_CALIBRACAO_MS : INTERVALO_DADOS_MS;
                long espera = intervalo - (System.currentTimeMillis() - inicio);
                if (baseAtiva == null) espera = Math.max(espera, 300L);
                try {
                    if (espera > 0) Thread.sleep(espera);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    rodando = false;
                }
            }
        } finally {
            fecharSerial();
            WifiRequest pendente = pedidoWifiPendente.getAndSet(null);
            if (pendente != null) {
                statusWifi = "Cliente encerrado antes do envio das credenciais.";
                pendente.resultado.complete(Boolean.FALSE);
            }
        }
    }

    /** Testa os endereços configurados até o ESP32 responder com telemetria válida. */
    private void descobrir() {
        StringBuilder tentados = new StringBuilder();
        for (String base : candidatos) {
            if (!rodando) return;
            if (ehSerial(base)) {
                String porta = descobrirSerial(base);
                if (porta != null) {
                    String payload = lerSerial("DADOS", TIMEOUT_SERIAL_MS);
                    if (payload != null && processarDados(payload)) {
                        baseAtiva = porta;
                        falhasSeguidas = 0;
                        conectado = true;
                        return;
                    }
                    fecharSerial();
                }
                if (tentados.length() > 0) tentados.append(", ");
                tentados.append("USB");
                continue;
            }
            String payload = lerPayload(base, "/api/dados", TIMEOUT_DESCOBERTA_MS);
            if (payload != null && processarDados(payload)) {
                baseAtiva = base;
                falhasSeguidas = 0;
                conectado = true;
                return;
            }
            if (tentados.length() > 0) tentados.append(", ");
            tentados.append(base);
        }
        conectado = false;
        sensorPronto = false;
        mensagemErro = "ESP32 não encontrado (tentativas: " + tentados + "). "
                + (connectionMode == ConnectionMode.SERIAL
                ? "Verifique o cabo USB e a porta serial."
                : "Conecte ao Wi-Fi \"RehabCar\" ou informe --sensor-url=IP.");
    }

    private void atualizarDados() {
        String payload = viaCabo()
                ? lerSerial("DADOS", TIMEOUT_SERIAL_MS)
                : lerPayload(baseAtiva, "/api/dados", TIMEOUT_NORMAL_MS);
        if (payload == null || !processarDados(payload)) {
            if (++falhasSeguidas >= FALHAS_PARA_DESCONECTAR) {
                conectado = false;
                sensorPronto = false;
                baseAtiva = null;
                fecharSerial();
            }
            return;
        }
        falhasSeguidas = 0;
        conectado = true;
    }

    private boolean processarDados(String payload) {
        JsonValue dados = interpretarJson(payload);
        if (dados == null) {
            sensorPronto = false;
            return false;
        }

        Float leituraX = extrairNumero(dados, "angX");
        Float leituraY = extrairNumero(dados, "angY");
        Float temperatura = extrairNumero(dados, "temp");
        JsonValue estadoSensor = dados.get("ok");
        if (leituraX == null || leituraY == null || temperatura == null
                || estadoSensor == null || !estadoSensor.isBoolean()) {
            mensagemErro = "A resposta do ESP32 precisa conter angX, angY, temp e ok válidos.";
            sensorPronto = false;
            return false;
        }
        if (!estadoSensor.asBoolean()) {
            mensagemErro = "ESP32 conectado, mas o MPU6050 não está fornecendo leituras.";
            sensorPronto = false;
            return false;
        }
        angX = leituraX;
        angY = leituraY;
        temp = temperatura;
        sensorPronto = true;
        if (!calibracaoFalhou) mensagemErro = "";
        return true;
    }

    private void atualizarCalibracao() {
        if (!pedidoCalibracaoEnviado) {
            String resposta = consultar("/api/calibrar");
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

        String payload = consultar("/api/progresso");
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

    private String consultar(String endpoint) {
        return viaCabo() ? lerSerial(comandoParaEndpoint(endpoint), TIMEOUT_SERIAL_MS)
                : lerPayload(baseAtiva, endpoint, TIMEOUT_NORMAL_MS);
    }

    private static String comandoParaEndpoint(String endpoint) {
        if ("/api/calibrar".equals(endpoint)) return "CALIBRAR";
        if ("/api/progresso".equals(endpoint)) return "PROGRESSO";
        return "DADOS";
    }

    private void processarPedidoWifi() {
        WifiRequest request = pedidoWifiPendente.get();
        if (request == null) return;

        boolean sucesso = false;
        try {
            if (!viaCabo() || portaSerial == null || !portaSerial.isOpen()) {
                statusWifi = "Falha: a porta serial do ESP32 não está aberta.";
            } else {
                String comando = "SETWIFI:" + request.ssid + ":" + request.password;
                String resposta = enviarComandoSerial(comando, TIMEOUT_SERIAL_MS);
                if (resposta == null) {
                    statusWifi = "Falha ao configurar o Wi-Fi: " + mensagemErro;
                } else if ("OK WIFI ATUALIZADO".equalsIgnoreCase(resposta.trim())) {
                    statusWifi = "Wi-Fi atualizado com sucesso.";
                    sucesso = true;
                } else {
                    statusWifi = "ESP32 recusou a configuração: " + resposta.trim();
                }
            }
        } catch (RuntimeException e) {
            statusWifi = "Erro de comunicação ao configurar o Wi-Fi: " + e.getMessage();
        } finally {
            pedidoWifiPendente.compareAndSet(request, null);
            request.resultado.complete(Boolean.valueOf(sucesso));
        }
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

    // ------------------------------------------------------------------
    // USB serial
    // ------------------------------------------------------------------
    private String descobrirSerial(String candidato) {
        if (serialIndisponivel) return null;
        long agora = System.currentTimeMillis();
        if (agora - ultimaBuscaSerial < INTERVALO_BUSCA_SERIAL_MS) return null;
        ultimaBuscaSerial = agora;

        String alvo = SERIAL_AUTO.equalsIgnoreCase(candidato)
                ? null : candidato.substring(PREFIXO_SERIAL.length());
        try {
            for (SerialPort porta : SerialPort.getCommPorts()) {
                if (!rodando) return null;
                String nome = porta.getSystemPortName();
                if (alvo != null ? !alvo.equalsIgnoreCase(nome) : !portaProvavel(porta)) continue;
                if (!abrirSerial(porta)) continue;

                String resposta = enviarComandoSerial("DADOS", TIMEOUT_DESCOBERTA_SERIAL_MS);
                if (resposta != null && interpretarJson(resposta) != null) {
                    return PREFIXO_SERIAL + nome;
                }
                fecharSerial();
            }
        } catch (LinkageError e) {
            serialIndisponivel = true;
            mensagemErro = "Biblioteca serial indisponível nesta plataforma: " + e.getMessage();
            fecharSerial();
        } catch (RuntimeException e) {
            mensagemErro = "Erro ao procurar a porta serial: " + e.getMessage();
            fecharSerial();
        }
        return null;
    }

    private static boolean portaProvavel(SerialPort porta) {
        String descricao = (porta.getSystemPortName() + " " + porta.getDescriptivePortName() + " "
                + porta.getPortDescription()).toLowerCase(Locale.ROOT);
        if (descricao.contains("bluetooth")) return false;
        String[] identificadores = {"usb", "uart", "cp210", "ch340", "ch341", "ch910",
                "ftdi", "silicon labs", "wch", "acm", "slab"};
        for (String identificador : identificadores) {
            if (descricao.contains(identificador)) return true;
        }
        return false;
    }

    private boolean abrirSerial(SerialPort porta) {
        fecharSerial();
        porta.setComPortParameters(BAUD_SERIAL, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        porta.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        if (!porta.openPort()) {
            mensagemErro = "Não foi possível abrir " + porta.getSystemPortName()
                    + "; verifique se outra aplicação está usando a porta.";
            return false;
        }
        porta.clearDTR();
        porta.clearRTS();
        portaSerial = porta;
        return true;
    }

    private void fecharSerial() {
        SerialPort porta = portaSerial;
        portaSerial = null;
        if (porta != null) {
            try {
                if (porta.isOpen()) porta.closePort();
            } catch (RuntimeException e) {
                mensagemErro = "Erro ao fechar a porta serial: " + e.getMessage();
            }
        }
    }

    private String lerSerial(String comando, int timeoutMs) {
        return enviarComandoSerial(comando, timeoutMs);
    }

    private String enviarComandoSerial(String comando, int timeoutMs) {
        SerialPort porta = portaSerial;
        if (porta == null || !porta.isOpen()) {
            mensagemErro = "Porta serial do ESP32 fechada.";
            return null;
        }
        try {
            byte[] descarte = new byte[256];
            while (porta.bytesAvailable() > 0) {
                int disponiveis = Math.min(porta.bytesAvailable(), descarte.length);
                if (porta.readBytes(descarte, disponiveis) < 0) {
                    throw new java.io.IOException("Falha ao limpar dados antigos da porta serial.");
                }
            }

            byte[] bytes = (comando + "\n").getBytes("UTF-8");
            if (porta.writeBytes(bytes, bytes.length) != bytes.length) {
                throw new java.io.IOException("Falha ao escrever o comando na porta serial.");
            }

            long limite = System.currentTimeMillis() + timeoutMs;
            StringBuilder linha = new StringBuilder();
            byte[] buffer = new byte[128];
            while (rodando && System.currentTimeMillis() < limite) {
                int lidos = porta.readBytes(buffer, buffer.length);
                if (lidos < 0) throw new java.io.IOException("A porta serial foi desconectada.");
                for (int i = 0; i < lidos; i++) {
                    char caractere = (char) (buffer[i] & 0xFF);
                    if (caractere == '\n') {
                        String resposta = linha.toString().trim();
                        linha.setLength(0);
                        if (resposta.startsWith("ERR")) return resposta;
                        if (respostaSerialValida(resposta, comando)) return resposta;
                    } else if (caractere != '\r' && linha.length() < 1024) {
                        linha.append(caractere);
                    }
                }
            }
            mensagemErro = "Tempo esgotado aguardando resposta ao comando " + comando + " pela serial.";
        } catch (Exception e) {
            mensagemErro = "Erro de comunicação serial: "
                    + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            fecharSerial();
        }
        return null;
    }

    private static boolean respostaSerialValida(String resposta, String comando) {
        if ("CALIBRAR".equals(comando)) return "OK".equalsIgnoreCase(resposta);
        if ("SETWIFI".equals(comando) || comando.startsWith("SETWIFI:")) {
            return "OK WIFI ATUALIZADO".equalsIgnoreCase(resposta);
        }
        if (!resposta.startsWith("{")) return false;
        try {
            JsonValue json = new JsonReader().parse(resposta);
            if ("PROGRESSO".equals(comando)) {
                return json.isObject() && json.get("progresso") != null;
            }
            return json.isObject() && json.get("angX") != null && json.get("angY") != null;
        } catch (RuntimeException e) {
            return false;
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

    private static final class WifiRequest {
        private final String ssid;
        private final String password;
        private final CompletableFuture<Boolean> resultado = new CompletableFuture<Boolean>();

        private WifiRequest(String ssid, String password) {
            this.ssid = ssid;
            this.password = password;
        }
    }
}
