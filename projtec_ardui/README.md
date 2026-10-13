# projtec_ardui

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## 📊 Acompanhamento do Projeto

O desenvolvimento do projeto é acompanhado através do [GitHub Projects](https://github.com/bragatto-tec/proj_gametec/projects). O gerenciamento visual, as milestones, as issues e as tarefas são mantidos no GitHub Projects oficial.

- 🎮 Gameplay
- 🔌 Integração IoT
- 🦵 Reabilitação do tornozelo
- 📊 Métricas e pontuação
- 🧪 Testes e documentação

**Project:** [Projeto Tec — Reabilitação do Tornozelo](https://github.com/bragatto-tec/proj_gametec/projects)

## 📅 Cronograma

Consulte o [cronograma do projeto](../CRONOGRAMA.md), com as etapas, prazos e status estimados.

## Conexão com o sensor

Sem endereço configurado, o aplicativo procura o ESP32 em `192.168.4.1`, `rehabcar.local` e `esp32.local`. Como o firmware imprime no Serial Monitor o IP recebido pelo Wi-Fi, informar esse IP é o método mais confiável. Computador e placa precisam estar na mesma rede Wi-Fi:

```powershell
$env:SENSOR_BASE_URL = "http://192.168.1.50"
.\gradlew.bat lwjgl3:run
```

Também são aceitos o argumento `--sensor-url=http://192.168.1.50` e a propriedade Java `sensor.baseUrl`. Na tela `TelaCalibracao`, erros de conexão e respostas incompatíveis da API do ESP32 são exibidos junto ao progresso.

A API do ESP32 deve fornecer `GET /api/dados` como JSON com `angX`, `angY`, `temp` e `ok`; iniciar a calibração por `GET /api/calibrar` com resposta `OK`; e responder a `GET /api/progresso` como JSON com `progresso` numérico de 0 a 100. O app consulta leituras quatro vezes por segundo para reduzir a carga no servidor HTTP do ESP32 e tenta novamente falhas temporárias de progresso antes de encerrar a calibração.
