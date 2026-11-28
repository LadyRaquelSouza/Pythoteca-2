# Próximos marcos do produto

Base 88379800ef95c2527d41fb7bd6cf89a81c32b230. Meta de uso pessoal para revisão da semana de provas, sem prazo garantido ou edição descartável. Manter roadmap original e agrupar mudanças coerentes; executar frentes independentes em paralelo. Consultar implementação original privada como referência de requisitos, sem publicar material privado. Pythoteca por dentro continua apêndice pedagógico separado do catálogo.

## Regra de prioridade e critérios de passagem

Esclarecimento da mantenedora: a urgência das provas não autoriza desvio da Pythoteca-2 planejada, redução de segurança ou soluções descartáveis que provoquem retrabalho. A meta temporal é subordinada à arquitetura e aos critérios de aceite. Se houver conflito, adiar a entrega, não os controles necessários ao uso pretendido.

- Antes de implementar cada marco, confrontar ADRs, threat model, roadmap e requisitos do original; registrar interfaces, modelo de dados, dependências, alternativas e reversão. Os novos identificadores são organização proposta, não substituição de decisões anteriores.
- Paralelizar apenas trabalho independente. Não considerar etapa dependente homologada antes de suas precondições. ARCH-002e3 e DISTRIBUTION-001 continuam abertas com os critérios originais; resolver ou registrar bloqueios sem reclassificar falhas como sucesso.
- Catálogo: assinatura/autenticidade e proteção contra rollback conforme ADR antes de usar conteúdo remoto como confiável no aparelho pessoal. Adaptador experimental não autenticado restrito a testes com dados não sensíveis; sem fallback em uso real. Fixar contrato e separar rede, domínio e persistência da UI.
- Distribuição: não instalar build experimental com dados reais sem procedência verificável, identidade de assinatura adequada ao uso e testes pertinentes. Não substituir instalação original nem migrar dados sem autorização. Licenças/avisos e homologação física são gates, não tarefas esquecidas.
- Dados e pedagogia: IDs estáveis, contratos versionados, progresso e rascunhos independentes da UI; preservar trilha intercalada, biblioteca livre, revisão e laboratório separado. Não projetar um leitor descartável limitado à revisão das provas.
- Cada PR deve registrar critérios, evidências, limitações e próximo passo. Preservar falhas intermitentes; repetir testes não prova sua causa. Atualizar diário operacional e apêndice pedagógico por marcos; não expor dados privados.
- A primeira entrega pode conter um subconjunto funcional definitivo, mas não atalhos de arquitetura ou segurança. Caso não seja possível cumprir os gates a tempo, manter a Pythoteca-2 em desenvolvimento e estudar por meios existentes.

## Sequência proposta
- BRAND-001 / DESIGN-001: identidade, símbolo sujeito a aprovação, paleta verde/preto/branco/cinza, glassmorphism econômico, contraste, escalas de texto e acessibilidade.
- UI-001: leitor Compose, navegação e biblioteca livre por disciplina, preservar diagnóstico IPC enquanto necessário.
- CATALOG-001 (identificador proposto): catálogo online v2 modules/index.json e adaptador legado v1 catalog/index.json; origem fixa HTTPS, sem redirecionamentos arbitrários, limites, tamanho, SHA256, esquema e IDs. Manifesto autenticado por assinatura e política de rollback antes de release; modo experimental explicitamente identificado, nunca fallback inseguro de release. Carregar aula sob demanda, guardar offline opcionalmente. Não incluir docs/desenvolvimento.
- STUDY-001 (proposto): leitura por capítulo, exemplos, atividades teóricas, feedback, gabarito sob demanda, questões essenciais e opcionais. Não exigir todo banco para avançar; não executar conteúdo automaticamente.
- TRAIL-001 (proposto): percurso intercalado guiado, biblioteca livre, revisão por disciplina e cumulativa, aproveitar intercâmbios curriculares.
- DATA-001 (proposto): progresso, tentativas, acertos, consulta ao gabarito e rascunhos separados; persistência local, IDs estáveis, backup e migração não destrutiva. Persistência mínima deve acompanhar STUDY/TRAIL, não esperar marco final.
- VALIDATION-001 (proposto): regressão, instalação/atualização da Suite, segurança, acessibilidade, Moto G05. Sem alegar homologação antes de evidências.

## Dependências e paralelismo
DISTRIBUTION-001: testes reais de instalação, cancelamento, assinatura divergente, lifecycle/concorrência e atualização. ARCH-002e3: licenças/avisos, versões nativas, compatibilidade e estabilidade CI; homologação física. Nenhuma mudança de critérios de segurança, assinatura de produção ou licença do projeto. GUI Python e execução arbitrária ainda precisam de etapa própria; não confundir probe 42 com laboratório completo.

## Conteúdo
Catálogo consultado nesta sessão: 43 módulos multidisciplinares (9 arquitetura, 5 Portugol, 11 Java, 3 SO, 5 TI, 12 modelagem) e 2 aulas Python no catálogo legado; demonstração excluída. Contagem é snapshot, não limite. Conteúdo online deve evoluir sem recompilar. Materiais acadêmicos privados não devem ser publicados.

## Próxima ação
Iniciar BRAND/DESIGN e UI em bloco coerente; em paralelo preparar testes Suite. Consolidar capítulos pedagógicos de inventário, ELF e distribuição após evidências. Não alterar arquivos de estudo ou instalações antigas sem autorização. Critérios específicos e rollback registrados em cada implementação.

## Investigação multilinguagem
[LANG-001](LANG-001.md): contrato comum, Portugol Studio, Java 8 e emulação pedagógica Swing (LANG-002–004), com escrita autônoma e auxílio gradual. Viabilidade não demonstrada; manter Library intacta até matriz de capacidades homologada. Preparar contratos antes de consolidar formatos de exercícios, sem reduzir segurança ou interromper frentes independentes.
