# ARCH-002e3: homologação complementar

Estado: planejado; implementação não iniciada. Base: main após PR #10, 17733b8. Escopo: consolidar infraestrutura e evidências antes da distribuição Suite. Não alterar arquitetura ou versão de dependência sem justificativa registrada.

## Plano e critérios de aceite

1. Wrapper: recuperar arquivos gerados por CI confiável ou gerar com Gradle 8.11.1 verificado; verificar JAR e distribuição contra checksums oficiais; versionar gradlew, gradlew.bat, propriedades e JAR; preservar bit executável; CI usa Wrapper versionado sem bootstrap improvisado.
2. Dependências: inventário de versões diretas/transitivas, licenças, avisos de redistribuição, runtime e bibliotecas empacotadas. Distinguir licença do projeto (decisão pendente) das obrigações de terceiros. Não escolher licença em nome da mantenedora.
3. Nativo: inspecionar APKs e arquivos internos pertinentes; revisar critérios ELF/ZIP, congruência e requisitos aplicáveis em documentação oficial. Não alegar que RELRO é requisito de alinhamento sem evidência.
4. Ambiente: verificar suporte real às propriedades de compatibilidade na imagem API35; avaliar imagem complementar se necessário. Revisar intervenções no launcher, controles e diagnóstico de reinicialização; manter falhas anteriores registradas.
5. Físico: Moto G05, confirmar versão Android, ABI, RAM e PAGE_SIZE; instalar par de APKs do mesmo build e certificado, verificar IPC e Python 42; documentar passos, resultados e limitações. Testes destrutivos ou mudanças no aparelho exigem autorização. Se indisponível, marcar bloqueado, não aprovado.

## Hipóteses e alternativas

Falhas anteriores indicam instabilidade ambiental, mas causa raiz não demonstrada. Não aumentar RAM mínima com base em um único erro. Preferir correções de CI e medições controladas a alterações no runtime. Se recuperação de binário Wrapper via conector não for possível, usar procedimento verificável alternativo, sem fabricar JAR.

## Durante

Nenhuma alteração funcional nesta etapa até o presente registro. PR documental serve também como teste da classificação de CI: documentação deve passar; build, runtime, controles e isolados devem ser ignorados.

## Depois

Preencher evidências, commits, execuções, limitações e decisão de merge. DISTRIBUTION-001 permanece planejado. Atualizar STATUS.md antes de qualquer pausa.
