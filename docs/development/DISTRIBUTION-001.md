# DISTRIBUTION-001 — Pythoteca Suite

## Antes
Base af823ee2baeeee693ea8455076b57d1d0045b266. ADR-001 e T13 orientam a etapa. ARCH-002e3 continua aberta para licenças, compatibilidade ambiental e dispositivo físico; iniciar a distribuição experimental em paralelo não equivale a homologar release.

Objetivo: instalar leitor e executor independentes por uma interface única, com consentimento Android e sem conceder ao leitor ou executor permissões de instalação.

## Implementação incremental
1. Criar módulo installer API 29+, sem INTERNET, com REQUEST_INSTALL_PACKAGES exclusivamente nele. Usar IDs experimentais, sem substituir instalações existentes da primeira geração.
2. Empacotar os APKs experimentais previamente assinados do mesmo build; manifesto gerado com nomes, versões, hashes, certificados e ABIs. Não aceitar arquivos externos. Separar a futura assinatura permanente da CI pública.
3. Validar entradas, assinaturas esperadas, identidade, versão, ABI e instalações existentes antes de abrir sessões; verificar protocolo compatível. Não confiar apenas em hashes declarados pelo próprio pacote. A assinatura do instalador deve ser conferida por canal confiável.
4. PackageInstaller: sessão pai multi-package com sessões filhas, copiar e fsync, commit com callback explícito, tratar STATUS_PENDING_USER_ACTION e confirmação do usuário. Abandonar sessões em falha, limpar temporários, tratar recriação da Activity. Sem instalação silenciosa.
5. Testar instalação limpa, cancelamento, adulteração, versão incompatível, assinatura divergente, atualização, falha de sessão filha e preservação de dados. Manter APKs individuais para diagnóstico. Testes em emulador e Moto G05; não modificar instalação antiga sem autorização.
6. Só promover distribuição após revisão de licenças/avisos, assinatura permanente e homologação. Não publicar chave privada ou APK de produção na CI.

## Alternativas
Instalação manual dos dois APKs: preservada para diagnóstico. APK monolítico: rejeitado por comprometer separação de UID. Download pelo instalador: desnecessário para esta etapa, aumenta superfície de confiança.

## Aceite e reversão
Compilação e inspeção dos três APKs; instalador sem rede; identidade e integridade verificadas; consentimento obrigatório; testes de sucesso e falha; evidência de preservação dos dados. Reversão: retirar módulo e tarefas de empacotamento, mantendo leitor/executor independentes. Nenhuma migração destrutiva.

## Durante
Plano registrado; implementação e testes ainda pendentes. Homologação física da etapa ARCH-002e3 e inventário de licenças continuam abertos.

## Implementação inicial
PR #21 integrado em 53e54d178135718bf009031a969d77ad3b56a792. Módulo installer Java/API29+, sem INTERNET, permissão REQUEST_INSTALL_PACKAGES exclusiva. tests/prepare-suite.py valida identidade/versão/ABI e certificados dos APKs debug, gera assets e propriedades, verifica conteúdo e assinatura da Suite. CI compila Suite após componentes e preserva junto aos artefatos experimentais. MainActivity valida hash, identidade de pacote, versão, ABI e certificado igual ao do instalador; rejeita downgrade e assinatura existente divergente. Sessão pai e filhas, fsync, callback explícito e consentimento. Não aceita arquivos externos. Limpeza de cache e abandono de sessões pré-commit em falhas.
Limites: assinatura debug não é identidade permanente confiável; conferir procedência externamente. Protocolo implícito pela versão experimental fixa e build conjunto, não negociação em instalador. Testes de instalação real, cancelamento, atualização, recriação, concorrência, callback Android 15 e recuperação pós-commit ainda pendentes. Artefato Suite compilado/inspecionado não deve ser chamado de Suite funcionalmente testado até teste de instalação. Reversão: remover módulo, script e etapas CI sem alterar leitor/executor. Aguardar CI.
