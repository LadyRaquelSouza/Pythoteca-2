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
