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
