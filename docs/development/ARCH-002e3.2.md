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
