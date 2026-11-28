# LANG-002 — Conformidade Portugol

## Antes
Base 475063ba4f366aa6408df97cdd483be510136992. Objetivo: avaliar motores existentes usando programas sintéticos, sem dependência Android ou alteração na Library. Preservar LANG-001, ADR-001 e T01–T13. Alternativas: núcleo oficial, transpilação JS e interpretador TypeScript; não construir motor próprio sem justificar.

## Triagem 28/11/2026
Revisões consultadas pela API GitHub (não equivalem a versões publicadas):
- UNIVALI-LITE/Portugol-Studio, master 5640a2e95de1c5d63c11f453c324812b4d6a8f6c, LGPL-3.0 declarada.
- dgadelha/Portugol-Webstudio, main eb014d979893ef0f384d5ed0afdfc608f3eef08b, GPL-3.0 declarada.
- DesignLiquido/portugol-studio, principal 2fae99d7dea4d8176d80f0c8c173233f3f4255f6, MIT declarada; package.json 0.11.21, lodash ^4.17.21, node-fetch 2, xml2js ^0.6.2, peer @designliquido/delegua ^1.28.6. Conferir pacote publicado, lockfile, licenças transitivas, APIs de rede/arquivos e integridade antes de executar. Candidato inicial de investigação, não decisão de adoção. Sem dependências novas no app.

## Corpus
tests/lang/portugol/corpus.json contém P01–P10. Resultados esperados são hipóteses semânticas, não medições do Studio. Comparar revisão identificada do motor oficial, registrar entrada, saída bruta, erros, versão, duração, normalizações justificadas. P10 é loop infinito: executar apenas em subprocesso descartável com watchdog externo, limite de recursos e sem rede ou dados privados. Não executar scripts externos não revisados em CI com credenciais. Preferir ambiente isolado e lockfile com integridade. P09 aceita categoria de erro, não exige texto idêntico. P01 pode exigir normalização explícita de representação decimal; não esconder divergências. 

## Aceite
Referência oficial medida ou marcada pendente, resultados comparáveis por caso, documentação de diferenças, revisão de licenças e superfícies externas; só depois experimentar Android em UID separado. Nenhuma alegação de conformidade nesta etapa. Reversão: remover corpus e adaptadores experimentais sem afetar IPC ou Library.

## Durante
Corpus criado e candidatos fixados; nenhum motor executado ainda. Próximo: revisar API e pacote publicado do candidato MIT, fixar dependências transitivas e executar harness isolado; obter referência oficial. Não alterar catálogo editorial antes de matriz de capacidades validada.

## LANG-002.1 — Preparação verificável
PR #26 integrado cd4911809879036aff8dcadff425da95d5221b6d; run 46111252518 12/12 primeira tentativa. Revisão da API do candidato 2fae99d: lexador.mapear, avaliador.analisar, interpretador.interpretar, callbacks de saída e question para entrada. comum.ts acrescenta espaço por argumento de escreva, delega Arquivos/Internet/Util ao delegua-node (não incluir); não é prova de contenção. Yarn lock upstream blob 5b78e37a2ad4aec6003167efd8a099ee69c04856 fixa Delégua 1.28.6 com integridade sha512. Release GitHub 0.11.21 publicada 23/11/2026; equivalência do tarball com fonte não verificada.
Harness run-case.cjs e run-corpus.py preparados, ainda não executados. Captura saída bruta e diagnósticos, entrada sintética, limites externos CPU/tempo/arquivo e heap candidato; não constitui sandbox. Executar somente em contêiner sem rede, sem credenciais, não privilegiado, filesystem restrito, limite de RAM/PIDs, com diretório de resultados separado. Workflow separado LANG-002 resolve dependências exatas com scripts desabilitados e registra lockfile, árvore, metadados/avisos; ainda não executa motor. Revisar artefato, fixar lockfile e imagem de execução antes de rodar corpus. Não confundir resolução inicial com build reproduzível. P10 requer revisão especial. Sem alterações no aplicativo ou Library. Reversão: retirar harness e workflow.

## LANG-002.2 — Execução isolada
PR #27 integrado d113d693932ae5a170eb6a19c136d5b6c55a7c0d. Run LANG 46112298703 aprovado primeira tentativa; ARCH 46112298647 11/12 e 12/12; executor 2048 MB job 140879111510 ADB offline, causa raiz não demonstrada. Artefato 15250595428 SHA256 8ca43ed574f6c3ed72f2ded5a71cffa136e2b9c2ea5255864167345dd6644c07 conferido. 14 pacotes, 9 MIT, 3 BSD-2, 1 BSD-3, 1 BlueOak; avisos de topo ausentes em delegua, portugol-studio e tr46. Lockfile versionado a partir do artefato; npm ci sem scripts, sem nova resolução. Revisão de licenças e vulnerabilidades ainda parcial.
Workflow executa corpus sintético em contêiner Node 22 bookworm, digest resolvido no CI e registrado (tag ainda mutável entre execuções; fixar digest após evidência), sem rede, UID 1000, filesystem somente leitura, diretório de resultados separado, 512 MiB RAM/swap, 1 CPU, 64 PIDs, sem capabilities, no-new-privileges. Limites por subprocesso mantidos. Não montar checkout inteiro nem credenciais. Resultados exatos e divergências são evidência, não gate de conformidade neste primeiro ensaio; revisar todos. Disponibilidade de Python3 na imagem e funcionamento do harness ainda não testados. Não constitui sandbox Android nem auditoria completa. Sem alterações na Library. Aguardar CI.
