# SECURITY-001 — Modelo de ameaças

Estado: requisitos propostos para revisão. Nenhuma proteção descrita constitui implementação já homologada.

## Escopo e ativos

Aplicativo Android, leitor, executor Python, biblioteca remota, armazenamento local, backups, integração contínua e distribuição. Proteger código e progresso do estudante, dados do dispositivo, integridade das aulas e APKs, credenciais e disponibilidade do aplicativo.

## Fronteiras de confiança

- Leitor: interface, catálogo e progresso; não executa diretamente código Python fornecido pelo estudante.
- Executor: aplicativo com UID distinto, sem INTERNET e sem acesso aos dados privados do leitor; tratar todo código executado como não confiável, inclusive soluções de aulas.
- IPC: fronteira autenticada por identidade do chamador e assinatura; validar protocolo, tamanho, sessão, estado e respostas em ambas as direções.
- Biblioteca e backups: entradas externas não confiáveis, mesmo quando obtidas de origem conhecida.
- CI público: contribuições, dependências, logs e artefatos não são automaticamente confiáveis.
- Assinatura de distribuição: ambiente privado separado, sem execução de código de PRs ou scripts provenientes de artefatos não confiáveis.

## Ameaças e controles requeridos

| ID | Ameaça | Controle requerido | Evidência de aceitação |
|---|---|---|---|
| T01 | Acesso indevido ao executor | UID distinto, permissão signature, validação do UID e pacote; componentes não exportados por padrão | Cliente com assinatura diferente rejeitado; inspeção dos manifests finais |
| T02 | Exfiltração por código Python | Ausência de INTERNET no executor, sem permissões desnecessárias, sem proxy de rede pelo leitor | Testes de rede negada e revisão de IPC, intents e permissões |
| T03 | Bloqueio e consumo excessivo | Limites de código, entrada, saída, tempo e concorrência; cancelamento e recuperação por processo | Testes de loop infinito, saída excessiva, espera por entrada e reinício |
| T04 | IPC inválido ou sessão obsoleta | Validação de remetente, versão, campos, tamanhos, identificadores e transições; rejeição de mensagens inesperadas | Testes negativos, duplicação, sessão encerrada e desconexão |
| T05 | Conteúdo adulterado | HTTPS, origem permitida, validação de esquema, limites de tamanho, integridade e atualização atômica | Rejeitar origem indevida, hash divergente, conteúdo truncado e redirecionamento não autorizado |
| T06 | Backup malicioso ou exposição | Validação antes de importar, limites, restauração transacional, aviso de privacidade e compartilhamento explícito | Importação inválida não altera progresso; exportação sem credenciais |
| T07 | Abuso de interface gráfica | Sessão única, autorização temporária, capability de uso único, Activity não exportada, encerramento e expiração verificada no destino | Testes de replay, expiração, retorno, cancelamento, recriação e timeout |
| T08 | Cadeia de suprimentos comprometida | Dependências revisadas, versões fixadas, ações externas fixadas por SHA, permissões mínimas, revisão de atualizações | Inventário, análise de dependências e revisão de workflows |
| T09 | Vazamento de segredos | Nenhuma chave ou material privado em commits, logs, fixtures ou artefatos públicos; assinatura separada | Revisão pré-publicação e verificação de artefatos |
| T10 | APK adulterado ou origem incorreta | Verificar origem, commit, execução CI, hash do artefato e certificado antes da distribuição | Verificação independente da assinatura e dos hashes |
| T11 | Perda ou corrupção de dados | Escrita atômica ou transacional, migrações testadas, recuperação de falhas | Testes de interrupção, migração e restauração |
| T12 | Injeção em conteúdo de aula | Renderização como dados, sem HTML ativo ou execução automática; ação explícita para executar exemplos | Testes com conteúdo malformado, links e código embutido |

## Limites e riscos residuais

Separação por UID e ausência de INTERNET reduzem a superfície de ataque, mas não constituem sandbox absoluta. Código Python com acesso a APIs Java pode interagir com recursos disponíveis ao UID do executor e tentar iniciar intents ou comprometer o próprio processo. Avaliar essas rotas, a exposição de componentes e o acesso a armazenamento compartilhado. Não confiar exclusivamente em filtros de imports ou em watchdogs executados dentro do processo que recebe código não confiável.

Limites de memória e CPU precisam de avaliação experimental; não prometer quotas rígidas sem mecanismo comprovado. Hashes de conteúdo obtidos junto ao catálogo não autenticam independentemente um catálogo comprometido. Avaliar assinatura de manifesto, proteção contra rollback e rotação de chaves antes de estabelecer garantias de autenticidade.

O sistema operacional, dependências e dispositivo podem conter vulnerabilidades. Não declarar proteção contra dispositivo comprometido ou root. Backups exportados podem conter informações pessoais; não declarar criptografia sem implementação validada.

## Regras de implementação e CI

- Revisar arquivos antes de publicar, inclusive metadados e exemplos. Não importar histórico privado.
- Usar permissões de workflow explícitas e mínimas; evitar pull_request_target para executar código de contribuições.
- Desabilitar persistência de credenciais no checkout quando desnecessária; não fornecer segredos a builds de PR.
- Separar APKs de teste de artefatos de distribuição. O assinador privado deve tratar APKs recebidos como dados e não executar scripts recebidos com eles.
- Revisar manifests mesclados de todas as variantes, dependências transitivas e componentes exportados.
- Não registrar código do estudante, conteúdo de backups ou credenciais em logs de produção.
- Registrar riscos, controles, testes e exceções por etapa; manter funcionalidades experimentais desativadas em releases até homologação.

## Portões de integração

1. Documentação: revisão de ameaças e requisitos aplicáveis.
2. Implementação: testes unitários e negativos; revisão de permissões e entradas externas.
3. Integração Android: testes em versões suportadas, incluindo falhas e recuperação.
4. Distribuição: verificação de artefatos, certificados, dependências e riscos residuais; aprovação explícita.

Falha de controle obrigatório impede integração ou distribuição da funcionalidade afetada. Exceções exigem decisão documentada, escopo e mitigação. A arquitetura definitiva, versões Android suportadas, política de assinatura de catálogo e mecanismos de contenção permanecem pendentes de ARCH-001 e experimentos técnicos.

## Segurança da biblioteca online — Pythoteca-Library

A biblioteca pública é uma fronteira de confiança independente do aplicativo. A leitura de conteúdo público não requer token GitHub, senha ou credencial embutida no APK. Somente o leitor acessará a rede; o executor não será intermediário de downloads.

### Transporte e origem

- HTTPS obrigatório, validação normal de certificados e nomes de host, sem desabilitar verificações TLS ou aceitar certificados arbitrários.
- Permitir somente origens e caminhos explicitamente configurados para a biblioteca oficial; validar URLs normalizadas e cada redirecionamento antes de segui-lo. Rejeitar esquemas alternativos, credenciais na URL e destinos locais ou privados.
- Não aceitar URLs arbitrárias fornecidas por aulas ou backups como destinos de download. Definir timeouts, limites de bytes efetivamente recebidos e descompactados, concorrência e política de repetição.
- Não enviar código, progresso, identificadores pessoais ou backups à biblioteca. Reconhecer que o provedor de hospedagem pode observar IP e metadados de acesso.

### Autenticidade e publicação

- Requerer manifesto autenticado para distribuição de produção, com assinatura digital verificável por chave pública confiável incorporada ao leitor. Definir formato canônico, algoritmo, versionamento, rotação e revogação em decisão arquitetural posterior.
- A chave privada de publicação não deve estar no repositório público nem disponível a workflows de PR. Separar aprovação de conteúdo, validação e assinatura; proteger branches e revisar alterações no catálogo e nos workflows.
- Manifesto autenticado deve vincular identificadores, versões, caminhos, tamanhos e hashes SHA-256 das aulas. Verificar assinatura antes de confiar nesses campos e verificar cada aula antes de instalá-la.
- Definir proteção contra replay e rollback, inclusive reinstalação e perda de estado local; documentar limites da verificação offline e da expiração. Não confundir HTTPS ou hashes isolados com autenticidade editorial.
- Durante desenvolvimento, conteúdo não assinado somente em variante de teste explicitamente identificada; nenhuma opção de contornar verificação em produção.

### Validação e disponibilidade

- Validar esquema, versões suportadas, IDs únicos, limites de profundidade, campos, quantidade e tamanho; rejeitar caminhos relativos perigosos, colisões e conteúdo inesperado.
- Renderizar aulas como dados, sem HTML ativo, JavaScript ou execução automática. Código de exemplo e soluções continuam não confiáveis, executáveis apenas mediante ação explícita no executor.
- Instalar atualizações de forma atômica após todas as verificações; preservar última versão válida e progresso em caso de falha. Nunca substituir cache válido por resposta inválida ou parcial.
- Manter acesso offline ao conteúdo previamente validado; informar falhas de atualização sem ocultar estado de confiança. Não exigir conexão permanente para estudar.

### Critérios de homologação adicionais

Testar certificado inválido, HTTP, redirecionamento para origem não permitida, manifesto com assinatura ausente ou inválida, chave desconhecida, hash ou tamanho divergente, versão antiga, conteúdo excessivo ou malformado, interrupção de download, falha de atualização, cache offline e ausência de execução automática. Verificar também que o executor não consegue acessar a biblioteca diretamente nem usar o leitor como proxy arbitrário.

A escolha do endpoint, formato de assinatura e processo de publicação exige coordenação com Pythoteca-Library. A migração do catálogo existente deverá ser planejada sem declarar que o formato atual já satisfaz esses requisitos. Nenhuma alteração na biblioteca é autorizada por este documento.
