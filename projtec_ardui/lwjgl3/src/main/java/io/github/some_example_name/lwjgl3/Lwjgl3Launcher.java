package io.github.some_example_name.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.some_example_name.Main;

import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;
import java.net.URI;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication(obterEnderecoSensor(args));
    }

    private static Lwjgl3Application createApplication(String sensorBaseUrl) {
        return new Lwjgl3Application(new Main(sensorBaseUrl), getDefaultConfiguration());
    }

    /**
     * Endereço opcional do ESP32. Ordem: --sensor-url=..., -Dsensor.baseUrl, SENSOR_BASE_URL.
     * Se nada for informado devolve null e o SensorClient descobre sozinho
     * (Wi-Fi "RehabCar" em 192.168.4.1, rehabcar.local, esp32.local).
     */
    private static String obterEnderecoSensor(String[] args) {
        String endereco = null;
        for (String arg : args) {
            if (arg.startsWith("--sensor-url=")) {
                endereco = arg.substring("--sensor-url=".length());
                break;
            }
        }
        if (endereco == null || endereco.trim().isEmpty()) {
            endereco = System.getProperty("sensor.baseUrl");
        }
        if (endereco == null || endereco.trim().isEmpty()) {
            endereco = System.getenv("SENSOR_BASE_URL");
        }
        if (endereco == null || endereco.trim().isEmpty()) {
            endereco = solicitarEnderecoSensor();
        }
        if (endereco == null || endereco.trim().isEmpty()) {
            return null;
        }
        try {
            return normalizarEndereco(endereco.trim());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage() + " - usando descoberta automatica.");
            return null;
        }
    }

    private static String solicitarEnderecoSensor() {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        while (true) {
            Object informado = JOptionPane.showInputDialog(
                    null,
                    "Digite o IP mostrado no Serial Monitor do ESP32.\n"
                            + "Deixe vazio ou cancele para tentar a busca automatica.",
                    "Conectar ao sensor",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    ""
            );
            if (informado == null || informado.toString().trim().isEmpty()) {
                return null;
            }
            try {
                return normalizarEndereco(informado.toString().trim());
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Endereco invalido. Informe um IP, por exemplo: 192.168.1.50",
                        "Endereco invalido",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private static String normalizarEndereco(String endereco) {
        if (!endereco.contains("://")) {
            endereco = "http://" + endereco;
        }
        URI uri;
        try {
            uri = URI.create(endereco);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Endereço do sensor inválido: " + endereco);
        }
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
            || uri.getHost() == null) {
            throw new IllegalArgumentException("Endereço do sensor inválido: " + endereco);
        }
        String normalizado = uri.toString();
        return normalizado.endsWith("/")
            ? normalizado.substring(0, normalizado.length() - 1)
            : normalizado;
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("projtec_ardui");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(640, 480);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        //// This could improve compatibility with Windows machines with buggy OpenGL drivers, Macs
        //// with Apple Silicon that have to emulate compatibility with OpenGL anyway, and more.
        //// This uses the dependency `com.badlogicgames.gdx:gdx-lwjgl3-angle` to function.
        //// You would need to add this line to lwjgl3/build.gradle , below the dependency on `gdx-backend-lwjgl3`:
        ////     implementation "com.badlogicgames.gdx:gdx-lwjgl3-angle:$gdxVersion"
        //// You can choose to add the following line and the mentioned dependency if you want; they
        //// are not intended for games that use GL30 (which is compatibility with OpenGL ES 3.0).
        //// Know that it might not work well in some cases.
//        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        return configuration;
    }
}
