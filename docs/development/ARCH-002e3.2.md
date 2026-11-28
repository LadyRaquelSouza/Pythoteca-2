# ARCH-002e3.2: inventário de dependências

## Antes
Base 32533d90a3a03ef47be13bac7dc47df8ca43268c. Library PR #34 integrado em 94c5b8cbbd9d74c06281c0ba7fad50853a40e3ce. Objetivo: inventário verificável de dependências de build, runtime e conteúdo efetivamente empacotado, com licenças e avisos. Não alterar versões, arquitetura ou licença do projeto.

Hipótese: relatórios de resolução Gradle mais inspeção dos APKs e arquivos internos permitem cruzar componentes declarados, resolvidos e distribuídos. Alternativas: inventário manual (útil como ponto inicial, insuficiente para transitivas); plugin SBOM externo (avaliar depois, evitar nova dependência nesta etapa). Preferir tarefas Gradle nativas e inspeção Python da biblioteca padrão.

Aceite: registrar commit e hashes dos APKs; listar configurações de build/runtime/teste separadamente, versões resolvidas e falhas de resolução; identificar bibliotecas por ABI e arquivos internos pertinentes; registrar origem e evidência de licença/avisos, marcar desconhecidos sem presumir; não confundir POM com prova de conteúdo empacotado. Revisão de ELF/ZIP em etapa complementar. Reversão: remover coleta diagnóstica sem alterar módulos ou dependências.

## Durante: inventário declarado inicial

| Componente | Versão declarada | Origem |
|---|---|---|
| Gradle | 8.11.1 | gradle/wrapper/gradle-wrapper.properties |
| AGP | 8.9.2 | build.gradle.kts |
| Kotlin Android e Compose plugin | 2.1.20 | build.gradle.kts |
| Chaquopy | 17.2.0 | build.gradle.kts |
| Compose BOM | 2025.04.01 | reader/build.gradle.kts |
| Activity Compose | 1.10.1 | reader/build.gradle.kts |
| Material3 | via BOM | reader/build.gradle.kts |
| Python | 3.13 (patch não apurado) | executor/build.gradle.kts |
| Java | 17 (reader, executor, protocol) | respectivos build.gradle.kts |

Módulos reader, executor e ipc-test dependem de protocol. Executor declara arm64-v8a e x86_64; não declara requisitos pip explícitos. Isso não implica ausência de bibliotecas Python internas. SDK 35, minSdk 29, targetSdk 35 nos três módulos Android. Nenhuma licença de terceiros foi validada nesta coleta inicial.

## Depois
Pendente: implementar coleta diagnóstica e executar CI; verificar artefatos, licenças e limites; atualizar Pythoteca por dentro após evidências. Não declarar inventário completo nesta fase.

## Coleta experimental
Adicionada coleta no job build após inspeção dos APKs: buildEnvironment e dependencies por módulo em modo offline, inventário JSON de entradas APK e ZIP internos até profundidade 2, SHA256 e identificação por magic ELF. Artefato dependency-inventory com retenção de 14 dias. Relatórios podem conter FAILED em configurações não resolvidas; revisar antes de aceitar. Limites: não é SBOM completo, não identifica automaticamente licenças nem formatos internos não ZIP; coleta offline pode falhar. Bateria funcional mantida. Referência histórica privada consultada para requisitos, sem copiar conteúdo privado. Aguardar CI e verificar artefato antes de merge.

## Evidências da primeira coleta
PR #15 integrado em 9f1deefa696efe39f0b647268c61e17877766908, head 0fbe5846eb3417bd3a056d1bca32582eefdcdea4. Run [46099960779](https://github.com/LadyRaquelSouza/Pythoteca-2/actions/runs/46099960779): tentativa 1 quatro sucessos, controle default falhou, sete cancelamentos; tentativa 2 12/12 aprovados. Causa raiz do controle não demonstrada.
Artefato [15245023465](https://github.com/LadyRaquelSouza/Pythoteca-2/actions/runs/46099960779/artifacts/15245023465), ZIP SHA256 conferido 70be8cd71f437be667c7aed9471f2eb9fb3e8380cd8cfe674cbb86237b39a939; TESTED_COMMIT sintético 7770aac04e38ee164862978a0cf958e05583e814. Cinco APKs: executor 134 entradas ELF (18 lib, 24 bootstrap-native, 92 stdlib interna); reader 4 (AndroidX Graphics Path); três APKs de teste/auxiliares sem ELF. Material3 1.3.2, graphics-path 1.0.1 no debugRuntimeClasspath do reader. Hash APK executor 85e816b90812fee53cd45598c47160092aa2afd1df56b9f53d6e20ffebd029dd; reader e41d860e6a0d19b4d8dc328914385430292ec0a655b254b7b5808fc0e9f2a52d. Relatórios contêm 14 linhas FAILED em executor, 14 ipc-test, 17 reader, zero protocol/buildEnvironment. Não equivalem a número de dependências únicas; investigar configurações auxiliares e distinguir runtime. Coleta funcional aprovada, inventário/licenças ainda incompletos; próxima coleta deve tratar resolução e preservar evidências, sem promover hipótese de versão de bibliotecas nativas a fato.

## Refinamento da resolução
Revisão do ZIP 15245023465: 42 linhas FAILED são 14 configurações internas UTP repetidas nos três módulos Android; no reader há também duas ocorrências atomicfu na configuração implementation (não resolvível) e uma kotlin-klib-commonizer-embeddable em kotlinCompilerPluginClasspathReleaseUnitTest. Nenhuma linha FAILED nos relatórios debugRuntimeClasspath. Não concluir que faltam bibliotecas nos APKs. O controle default da tentativa 1 registrou seis amostras PID 546 e SIGSEGV na thread SettingsProvide de system_server, sem APKs instalados (job 140839962557). Causa raiz do SIGSEGV desconhecida.
Novo init script Gradle seleciona classpaths runtime debug e androidTest utilizados, separa módulos, componentes e artefatos, registra falhas e reprova a coleta se houver resolução incompleta. Permite rede para resolução; relatórios amplos offline mantidos como contexto, não como gate de todas as configurações auxiliares. Não altera dependências declaradas. Limites: artefato resolvido não comprova inclusão no APK; release e licenças ainda não validados. Aguardar CI.

## Evidências da resolução direcionada e inspeção ampliada
PR #17 integrado em 16b1f546e1fa9dca39f5622d7ccc452156f59708, head 90e1482b48e13198e82848ef265824b88de127f2. Run 46101776466 tentativa 1 11/12, executor isolado falhou com ADB offline e Broken pipe no serviço input (job 140846417996); tentativa 2 12/12. Causa raiz não estabelecida.
Artefato 15246595189 SHA256 236960a7111e6991d514ff5988c43c08a9b606a9521bd55976713dc1328525e4 conferido; commit sintético d03667b09d30b18586d1335b0ddb450ecd698ce4. resolved-runtime.json: reader runtime 85 componentes/59 artefatos, reader test 1/0, executor 2/2, ipc-test 2/1, ipc-test test 1/0, protocol 1/0; zero failures. Compose 1.8.0, Material3 1.3.2, lifecycle 2.8.7, coroutines 1.7.3. Chaquopy JAR aparece como artefato local, não como componente Maven no grafo.
Artefato APK testado 15245742000 SHA256 f4278c318dd583efba9ba9dfe47b2f2711c1dae067b523e6112c088bb96d3082 conferido; mesmo TESTED_COMMIT; hashes APK coincidem com inventário: executor fa5f436eda944a18189c314a0df63f8f723fec68b03d7cf6176b54d3e8dcbbf8, reader 5fd3c4b7ca7d97c85576a6c02504b933034e137eddb0630944ae441158cc55ed. Inspeção local por Python struct/zipfile dos cabeçalhos ELF e PT_LOAD, inclusive ZIP internos: mínimo p_align 16384 em 138/138 entradas (executor 67 ARM64, 67 x86_64; reader 4 ABIs). Não é ainda verificação de congruência offset/vaddr nem garantia de alinhamento ZIP interno; zipalign externo já aprovado pelo CI. Sem exigência RELRO inferida. Referência https://developer.android.com/guide/practices/page-sizes.
Licenças preliminares: Chaquopy Java 17.2.0 MIT (https://central.sonatype.com/artifact/com.chaquo.python.runtime/chaquopy_java/17.2.0); CPython PSF e avisos adicionais (https://docs.python.org/3.13/license.html); SQLite núcleo domínio público (https://www.sqlite.org/copyright.html); OpenSSL depende da versão (https://www.openssl-library.org/source/license/). APK executor inclui textos setuptools 68.2.2 MIT e pyelftools 0.26 domínio público com avisos adicionais. APK reader inclui três textos Apache 2.0 em META-INF/androidx. Versões exatas de bibliotecas nativas e inventário individual de licenças/avisos ainda pendentes. Não declarar liberação de redistribuição. Próximo passo: automatizar verificação ELF interna e coleta de evidência de licença, confirmar versões nativas; homologação física e distribuição continuam pendentes.
