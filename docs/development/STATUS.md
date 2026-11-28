# Estado atual e retomada

Atualização: 27/11/2026 (data local do projeto).

## Base

Pythoteca-2, main após PR #9: db1ef21cadbb8167ef72ee8e9a8d9fb7db04a9e5.
ARCH-002e2 aprovado experimentalmente no head 417dbf16d821c9b8842430b749a35f917caf002a, execução 46091383922, tentativa 2, 10/10 jobs considerando as duas tentativas.
https://github.com/LadyRaquelSouza/Pythoteca-2/actions/runs/46091383922

## Próxima etapa: ARCH-002e3 (planejada, não iniciada)

1. Atualizar diário pedagógico na Library sobre e1/e2.
2. Consolidar Gradle Wrapper versionado e verificar checksum e procedência.
3. Inventariar dependências, licenças e bibliotecas nativas, inclusive conteúdo empacotado internamente; revisar alinhamento ELF e demais requisitos aplicáveis.
4. Validar documentação oficial sobre 16 KB e propriedades de compatibilidade. Leitura das propriedades não comprova seu efeito.
5. Testar no Moto G05 real: confirmar Android, ABI, RAM e tamanho de página; instalar APKs experimentais e registrar resultados. Não alegar homologação física antes disso.
6. Revisar estabilidade do CI, remover intervenções desnecessárias no launcher e corrigir verificações de controle que não abortem corretamente. Manter evidências de falhas intermitentes.

## Restrições e decisões

Android 15/API 35 prioritário; API 29–34 secundário; API <=28 fora do escopo; API >=36 progressivo. Moto G05 única referência física ativa; Galaxy A06 adiado.
Leitor Kotlin/Compose Material3, executor Chaquopy em APK/UID separado, sem INTERNET; IPC autenticado por assinatura e UID. Não executar código arbitrário no leitor.
Distribuição futura Suite APK offline com APKs assinados embutidos, PackageInstaller multi-package, consentimento explícito, validação de identidade, assinatura, hash, versão e ABI. Não implementado.
Catálogo assinado, cache offline e proteção contra rollback planejados; GUI Python ainda em investigação.
Identidade visual aprovada como direção: glassmorphism verde/preto/branco/cinza, contraste acessível e fallback de desempenho. Logo final não aprovado.
Licença do projeto ainda não decidida. Sem chaves privadas ou material acadêmico privado no repositório público.

## Retomada

Conferir main, PRs e CI antes de editar. Ler histórico e ADR-001. Abrir etapa com plano registrado antes da implementação. Não fazer merge funcional sem evidência do head exato e critérios de aceite. Registrar bloqueios e próximo passo antes de encerrar a sessão.
