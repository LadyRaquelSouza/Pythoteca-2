# Estado atual e retomada

Atualização: 27/11/2026 (data local do projeto).

## Base

Pythoteca-2, main após PR #9: db1ef21cadbb8167ef72ee8e9a8d9fb7db04a9e5.
ARCH-002e2 aprovado experimentalmente no head 417dbf16d821c9b8842430b749a35f917caf002a, execução 46091383922, tentativa 2, 10/10 jobs considerando as duas tentativas.
https://github.com/LadyRaquelSouza/Pythoteca-2/actions/runs/46091383922

## Continuidade documental

PR #10 integrado em 17733b8fa87a0c40c9f3b022f40b1c371b427a35; head 5c41c49, run 46094319345, 12/12 jobs aprovados. Classificação conservadora de mudanças: README.md e docs/**/*.md dispensam testes funcionais quando exclusivos; workflow e demais arquivos exigem bateria completa. Caminho misto validado; este PR documental deve validar a dispensa de emuladores.

Library PR #33 integrado c8d61b34; run 46095204983 aprovado. Pythoteca-2 PR #11 integrado cf29e09; run 46095232924 classificação/documentação aprovadas, todos os jobs funcionais dispensados conforme previsto.

## Etapa atual: ARCH-002e3.2 (coleta inicial aprovada, inventário incompleto)

Library PR #34 integrado em 94c5b8c; Pythoteca-2 PR #13 integrado em 32533d9. Plano e inventário declarado em [ARCH-002e3.2](ARCH-002e3.2.md). PR #15 integrado em 9f1deef, run 46099960779 tentativa 2 12/12; artefato 15245023465 conferido. PR #16 integrado em 28ec9a6. FAILED classificados como configurações auxiliares/não resolvíveis; coleta runtime explícita implementada na branch arch/002e3-runtime-resolution. PR #17 integrado 16b1f54; run 46101776466 tentativa 2 12/12, seis classpaths sem falhas. Inspeção ampliada 138/138 ELF com p_align mínimo 16384; licenças preliminares. PR #18 integrado 1d0adc8. PR #19 integrado 12b1dba; run 46103424860 tentativas 7/12, 10/12, 12/12; log de build confirma 138 ELF, zero erros, cinco candidatos a avisos. Artefato JSON ainda a revisar. Versões nativas e avisos ainda incompletos; preparar homologação física. Sem novas dependências.

PR #12 integrado em 9a12903fec91344ce632b3c5457c213bf44fe7d3. ARCH-002e3.1 concluída experimentalmente: head f63d452, run 46095581452, tentativa 1: 10/12; tentativa 2: 12/12. Falhas intermitentes preservadas no [registro do Wrapper](ARCH-002e3.1.md).

Plano detalhado: [ARCH-002e3](ARCH-002e3.md).

1. PR #33 da Library concluído.
2. Gradle Wrapper versionado e verificado no CI: concluído.
3. Inventariar dependências, licenças e bibliotecas nativas, inclusive conteúdo empacotado internamente; revisar alinhamento ELF e demais requisitos aplicáveis.
4. Validar documentação oficial sobre 16 KB e propriedades de compatibilidade. Leitura das propriedades não comprova seu efeito.
5. Testar no Moto G05 real: confirmar Android, ABI, RAM e tamanho de página; instalar APKs experimentais e registrar resultados. Não alegar homologação física antes disso.
6. Revisar estabilidade do CI, remover intervenções desnecessárias no launcher e corrigir verificações de controle que não abortem corretamente. Manter evidências de falhas intermitentes.

## Restrições e decisões

Android 15/API 35 prioritário; API 29–34 secundário; API <=28 fora do escopo; API >=36 progressivo. Moto G05 única referência física ativa; Galaxy A06 adiado.
Leitor Kotlin/Compose Material3, executor Chaquopy em APK/UID separado, sem INTERNET; IPC autenticado por assinatura e UID. Não executar código arbitrário no leitor.
Suite experimental compilada e inspecionada; instalação multi-package ainda não homologada.
Catálogo assinado, cache offline e proteção contra rollback planejados; GUI Python ainda em investigação.
Identidade visual aprovada como direção: glassmorphism verde/preto/branco/cinza, contraste acessível e fallback de desempenho. Logo final não aprovado.
Licença do projeto ainda não decidida. Sem chaves privadas ou material acadêmico privado no repositório público.

## Retomada

Conferir main, PRs e CI antes de editar. Ler histórico e ADR-001. Abrir etapa com plano registrado antes da implementação. Não fazer merge funcional sem evidência do head exato e critérios de aceite. Registrar bloqueios e próximo passo antes de encerrar a sessão.

## Prioridade de produto esclarecida pela mantenedora
Uso inicialmente pessoal, conectado por padrão. Offline não é prioridade arquitetural; download de aulas para estudo sem conexão permanece requisito secundário. Não ampliar complexidade offline sem necessidade concreta. Executor sem INTERNET continua decisão independente de segurança. A distribuição Suite offline é decisão técnica anterior, não requisito de uso diário; reavaliar seu escopo separadamente, sem revogação automática.

## Frente paralela: DISTRIBUTION-001
PR #20 integrado em af823ee. Plano experimental em [DISTRIBUTION-001](DISTRIBUTION-001.md). Implementação pendente; não autoriza distribuição de produção nem encerra ARCH-002e3. Priorizar instalação pessoal verificável e preservar APKs independentes.

DISTRIBUTION-001: PR #21 integrado 53e54d1; implementação inicial na branch distribution/001-implementation. PR #22 integrado em 8837980, run 46106132067 tentativa 2 12/12; empacotamento aprovado. Implementar testes de instalação e tratar ciclo de vida, concorrência e callback antes de homologar. ARCH-002e3 permanece aberta; sem assinatura permanente.

## Próximos marcos do produto
[Sequência e frentes paralelas](PRODUCT-NEXT.md). Iniciar BRAND/DESIGN/UI; testes Suite e pendências ARCH em paralelo. Pythoteca por dentro ainda requer consolidação de inventário/ELF e distribuição.

A urgência das provas é meta de entrega, não requisito arquitetural. Preservar critérios originais, contratos evolutivos e gates de segurança; se houver conflito, adiar a entrega. Ver regra de prioridade em PRODUCT-NEXT.md.

## Investigação multilinguagem
[LANG-001](LANG-001.md): contrato comum, Portugol Studio, Java 8 e emulação pedagógica Swing (LANG-002–004), com escrita autônoma e auxílio gradual. Viabilidade não demonstrada; manter Library intacta até matriz de capacidades homologada. Preparar contratos antes de consolidar formatos de exercícios, sem reduzir segurança ou interromper frentes independentes.

LANG-001: PR #24 integrado 9c9e535. Contrato de domínio candidato e testes JVM na branch lang/001-contract; aguardar CI. Triagem documental de três motores Portugol, nenhum escolhido. Transporte e execução permanecem pendentes; Library intacta.

LANG-001 PR #25 integrado 475063b, run 46108917849 tentativa 3 12/12. LANG-002: [corpus e candidatos](LANG-002.md) na branch lang/002-conformance; referência oficial e execução isolada pendentes. Sem dependência ou alteração na Library.

LANG-002 PR #26 integrado cd49118, run 46111252518 12/12 tentativa 1. LANG-002.1 branch lang/002-harness prepara resolução sem scripts e harness; revisar lockfile/licenças e isolamento antes de executar motor. Nenhuma execução ainda.

LANG-002.2: PR #27 integrado d113d69; artefato de dependências conferido, lockfile versionado e execução offline em contêiner na branch lang/002-isolated-run. Aguardar evidências por caso; referência Studio, licenças e Android pendentes.

LANG-002.3: PR #28 integrado 897281c; LANG aprovado, ARCH tentativa 2 12/12. Candidato Portugol Mobile indicado pela mantenedora, ensaio comparativo isolado na branch lang/002-mobile-comparison, sem adoção GPL nem integração Android. Aguardar CI e revisar saídas brutas, eco, licenças e referência oficial.

LANG-002.4: PR #29 integrado 401ee7a; LANG primeira tentativa, ARCH quinta 12/12. Artefatos comparativos conferidos, digest Node fixado, corpus ampliado a 22 casos e harness oficial preparado (ainda não executado). Referência, GPL e Android pendentes.

LANG-002.5: PR #30 integrado dcded01; LANG primeira tentativa, ARCH terceira 12/12. Decisão aprovada: Mobile prioritário, Design Líquido reserva sob dispatch explícito. Branch lang/002-reference inspeciona distribuição oficial standalone sem executar; hash independente ausente, referência ainda não medida. Aguardar inventário e revisar antes de extração/execução. GPL, Android e Library pendentes.

LANG-002.6: PR #31 integrado fc6f3e3; inventário oficial e Mobile verificados localmente. Branch lang/002-reference-run fixa hash observado e prepara execução dos 22 casos em contêiner offline; CI pendente. ARCH terceira tentativa 12/12, causas das falhas não verificadas. Sem adoção de motor, app ou Library.
