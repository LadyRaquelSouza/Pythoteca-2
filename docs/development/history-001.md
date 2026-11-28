# Histórico reconstruído: fundação até ARCH-002e2

Registro retrospectivo a partir da continuidade do projeto e das evidências de CI já consultadas. Não representa transcrição integral das conversas. Links e commits permitem auditoria posterior.

| Etapa | Resultado | PR / merge |
|---|---|---|
| FOUNDATION-001 | Fundação do repositório público separado do legado privado | d063687 |
| SECURITY-001 | Modelo de ameaças, isolamento, catálogo e cadeia de confiança | #1 / 466208b |
| ARCH-001 | ADR: Kotlin/Compose, executor Chaquopy separado, protocolo compartilhado | #2 / 0e12469 |
| ARCH-002a | Compilação dos APKs experimentais | #3 / 10abd53 |
| ARCH-002b | IPC autenticado, rejeição de UID e mensagens inválidas | #4 / 25d171b |
| ARCH-002c | Python retorna 42 via IPC | #5 / 705253b |
| ARCH-002d1 | Exceções recuperáveis e rede negada | #6 / 364a179 |
| ARCH-002d2 | Busy, watchdog de execução infinita e recuperação de processo | #7 / f608a59 |
| ARCH-002e1 | Alinhamento ELF/ZIP 16 KB e assinaturas | #8 / c2f1286 |
| ARCH-002e2 | Execução em emulador Android 15 de 16 KB | #9 / db1ef21 |

## ARCH-002e2: antes

Objetivo: verificar execução real em páginas de 16 KB, além do alinhamento estático. Manter testes 4 KB, autenticação, exceções, rede, watchdog e recuperação. Não confundir emulação x86_64 com ARM64 físico. Propriedades de compatibilidade foram configuradas e lidas de volta, mas sua efetividade não está comprovada.

## Durante

- Run 46052484486: resolução do plugin Chaquopy falhou. Bootstrap do Wrapper separado do projeto.
- Run 46052761182: setprop falhou; preparação passou a usar adb root.
- Run 46053691043: shell permaneceu root; correção com unroot e verificação UID 2000.
- Run 46054530338: executor desconectado durante Python.
- Run 46056187781: falha de UIAutomator e XML antigo; captura passou a exigir arquivo novo.
- Run 46057957998: SIGSEGV nativo em system_server após instalações; causa não estabelecida.
- Run 46061198658: controles sem APKs passaram; bateria completa falhou novamente em system_server.
- Run 46089539845: bateria completa passou em 4/16 KB, controles e B1–B4 passaram; B5 falhou antes de instalar por perda do ADB. Reexecução B5 instalou APKs, mas instrumentação morreu; outros processos também morreram e houve eventos de pressão de memória. Hipótese ambiental, não diagnóstico conclusivo.
- Commits 588b994, 5356bd8, 712eb63: estabilização de 120 s, monitoramento de memória/processos, marcadores Python, logs e comparação 2048/4096 MB.
- Run 46091105654: plugin não resolvido, seis jobs falharam por ausência do artefato. Deficiência do pipeline: dependentes iniciavam mesmo sem compilação.
- Commit 417dbf1: separação build/runtime, dependência de artefato, até três tentativas limitadas por erros de resolução, logs detalhados.
- Run 46091383922 tentativa 1: 9/10; Python isolado passou em ambas as RAMs, 24 amostras cada. B4 falhou antes de instalar APKs durante estabilização, com erro gráfico e sinais de reinicialização do sistema.
- Tentativa 2: B4 passou, instrumentação -1, PING_OK, 24 amostras, PID 545 estável. Total 10/10 para mesmo head em duas tentativas.

## Depois

Merge squash PR #9: db1ef21cadbb8167ef72ee8e9a8d9fb7db04a9e5. Head testado 417dbf16d821c9b8842430b749a35f917caf002a.
Artefato experimental: https://github.com/LadyRaquelSouza/Pythoteca-2/actions/runs/46091383922/artifacts/15241821230
Aprovação experimental, não release. Instabilidade ambiental não eliminada. 2 GB funcionou; não estabelece requisito mínimo nem exclui pressão de memória. Não houve teste físico ARM64. Próxima etapa e3.

## Ideias e decisões futuras

Suite offline com instalação atômica e consentimento; catálogo assinado independente; GUI Python real sem editor visual, toolkit ainda não escolhido; trilha interdisciplinar e biblioteca por disciplina; revisão das aulas Python sem obrigação de preservar IDs/progresso legado. Direção visual glassmorphism acessível. Nenhuma dessas ideias equivale a implementação concluída.
