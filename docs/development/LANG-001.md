# LANG-001 — Investigação de execução multilinguagem

## Antes
Base 80342b431a1d725d1a2b8c5e0127eebe70e762a9. Proposta da mantenedora: ampliar a Pythoteca para exercícios práticos de Portugol e Java, além do laboratório Python. Não substituir IDEs completas. Viabilidade técnica ainda não homologada. A Library poderá ser enriquecida após os resultados; não modificar seu esquema ou conteúdo nesta etapa. Preservar ADR-001, T01–T13 e os gates de PRODUCT-NEXT.

## Requisitos pedagógicos
- Portugol: dialeto Portugol Studio (não VisuAlg); incentivar escrita de programas completos pela estudante, incluindo leia/escreva, variáveis, condições, laços e funções conforme currículo. Oferecer exemplos, estruturas parciais e desafios sem código pronto; dicas e gabarito apenas sob demanda.
- Java: foco em Java 8 conforme curso, pequenos programas, classes, métodos e eventos. Exercícios Swing com formulários previamente descritos, componentes virtuais e edição do código relevante, sem exigir digitação repetitiva de código gerado pelo NetBeans. Não chamar a emulação de Swing real nem prometer compatibilidade integral. Métodos e semântica suportados devem ser fiéis ou diferenças explícitas.
- Python permanece primeiro motor previsto. Separar tipo pedagógico de exercício, linguagem, versão do dialeto, capacidades do motor, código inicial e tentativas. Preservar IDs e progresso. Ações explícitas para executar, nunca autorun; casos de teste e feedback sem revelar solução automaticamente.

## Experimentos propostos
1. LANG-001: contrato independente de UI para execução, entrada, saída, diagnóstico, sessão, limites, cancelamento e capacidades negociadas. Investigar processo/UID por motor versus executor compartilhado; não decidir sem evidência de isolamento e recursos. Nenhuma alteração IPC de produção antes de ADR e testes de compatibilidade.
2. LANG-002: comparar núcleos existentes do Portugol Studio e alternativas compatíveis; verificar licença, dependências, semântica e Android. Preferir reaproveitamento revisado a interpretador próprio. Prova de conceito com código inteiro, entrada/saída e erros; confrontar resultados com Portugol Studio.
3. LANG-003: comparar compilação Java local (compilador compatível, transformação DEX, carregamento restrito) e outras abordagens. Investigar Java 8, APIs disponíveis, diagnósticos, memória, armazenamento e requisitos Android. Não pressupor que D8/compilador desktop funcione no aparelho. Não executar código arbitrário no leitor.
4. LANG-004: representar JFrame/JLabel/JTextField/JButton/JComboBox/JCheckBox/JRadioButton/JOptionPane em uma interface pedagógica; eventos e propriedades por protocolo declarativo fechado, limitado e versionado. Sem acesso arbitrário a Views, intents ou objetos do leitor. Validar transferência de aprendizado para Swing/NetBeans, delimitar incompatibilidades. Não inventar resultados de execução.

## Aceite e segurança
Para cada motor, demonstrar exemplos positivos e erros de sintaxe/execução, entrada/saída, fidelidade semântica do subconjunto declarado, limites de tamanho/tempo/saída/concorrência, cancelamento e recuperação, autenticação IPC, ausência de rede no executor e ausência de acesso a dados privados do leitor. Testar casos hostis e mensagens inválidas. Medir APK, RAM e latência no Moto G05; avaliar licenças e dependências antes de distribuição. Não usar execução remota ou enviar código/progresso a terceiros como atalho. GUI exige capacidades temporárias e validação conforme T07/T12. Não prometer sandbox absoluta.

## Critério de integração editorial
Após cada prova aprovada, registrar matriz de capacidades, limitações, versão de contrato e exemplos verificados; só então propor esquema extensível retrocompatível e exercícios práticos na Library. Aulas existentes continuam válidas e exercícios de motor indisponível devem informar indisponibilidade, não simular execução nem bloquear leitura. Não alterar conteúdo sem decisão editorial da mantenedora.

## Reversão e continuidade
Experimentos separados, IDs experimentais, sem dados reais ou chaves permanentes. Reverter ramo/módulo sem afetar leitor, Python ou aulas existentes. Registrar falhas, alternativas descartadas e resultados em diário e Pythoteca por dentro. BRAND/DESIGN/UI podem avançar independentemente, considerando capacidade multilinguagem sem assumir motores já disponíveis. ARCH-002e3 e DISTRIBUTION-001 permanecem abertas. Urgência das provas não reduz critérios.

## Durante
Planejamento registrado. Nenhum motor novo implementado ou homologado; nenhuma mudança na Library.

## Contrato candidato e triagem de motores
Base 9c9e5355a728eabac9133e2f1a1f1ea22675ebd3 (PR #24 integrado). ExecutionContract é modelo Java puro, independente do IPC v1 existente. Linguagens, capacidades imutáveis, UUID de sessão, limites candidatos UTF-8 (código 64 KiB, entrada inicial 16 KiB, saída 64 KiB como constante ainda não aplicada), prazo máximo 120 s e transições explícitas de estado. Testes JVM sem dependência adicional verificam incompatibilidade de capacidades, cópia defensiva, limites e estados terminais. Não implementa transporte, negociação remota, eventos de saída/entrada incremental, cancelamento efetivo, diagnósticos estruturados, orçamento cumulativo, autenticação ou isolamento; ainda necessários antes de uso. Não confundir validação de modelo com contenção de execução. Reversão: remover classe, testes e invocação CI, sem alterar IPC existente.
Triagem documental 28/11/2026: núcleo oficial https://github.com/UNIVALI-LITE/Portugol-Studio (LGPL-3.0 indicada no repositório; compilador gera Java, investigar dependências desktop e geração dinâmica); https://github.com/dgadelha/Portugol-Webstudio (runtime transpila para JavaScript, revisar GPL e dependências); https://github.com/DesignLiquido/portugol-studio (implementação TypeScript sobre Delégua, confirmar licença específica e transitivas). Nenhum candidato selecionado, instalado ou executado. Próximo: fixar revisões, auditar licenças e dependências, montar corpus sintético de conformidade, comparar execução desktop antes de avaliar Android. Não introduzir WebView com ponte privilegiada ou execução no leitor. Aguardar CI do contrato.
