# ARCH-002e3.1 — Wrapper versionado

## Antes
Base cf29e09, plano ARCH-002e3. Manter Gradle 8.11.1, sem alterar dependências do aplicativo.

## Durante
- Run 46094319345, artefato 15242503078 recuperado; SHA256 do ZIP conferido: 9ade29509db21f19b07bfcde50ee0c6d43146e8d17cb3acd62b727c4eeec9a9e. TESTED_COMMIT contém SHA do merge sintético 64d33a4bc28788d66b6c24c2d10d363b79e6631b, não head do PR.
- JAR extraído: SHA256 2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046, igual ao publicado em https://gradle.org/release-checksums/ para 8.11.1. Distribuição bin: f397b287023acdba1e9f6fc5ea72d22dd63669d59ed4a289a29b1a76eee151c6.
- API textual rejeitou binário; referência direta a blob externo rejeitada. Solução: fetch_file com encoding base64 do repositório oficial gradle/gradle tag v8.11.1, create_blob no destino. SHA Git a4b76b9530d66f5e68d973ea569d8e19de379189 preservado. Scripts oficiais da mesma tag, não cópias do artefato (tamanhos diferem); gradlew modo 100755. Propriedades reproduzem as do artefato.
- CI passa a verificar JAR, URL, checksum da distribuição e bit executável antes de executar Wrapper. Remove download/descompactação/bootstrap provisórios. Nenhuma mudança no código Android.

## Depois
- PR #12, head f63d4528ad592c215af911310df6b46485b3b94f, [run 46095581452](https://github.com/LadyRaquelSouza/Pythoteca-2/actions/runs/46095581452).
- Tentativa 1: 10/12 jobs aprovados. Verificação do Wrapper, compilação, identidades/permissões, alinhamento e assinaturas aprovados. Runtime integrado passou no smoke test convencional e falhou em 16 KB; logs registram ADB offline e SIGSEGV em processo Google. Ping isolado 2048 MB abortou com INSTRUMENTATION_ABORTED: System has crashed. Controles e Python isolado passaram.
- Solicitada repetição dos jobs reprovados sem alterar head. Tentativa 2: 12/12 aprovados, todos com novos IDs na API latest; runtime 140828571508, ping 140828571626. Hipótese ambiental reforçada, causa raiz não demonstrada.
- Squash merge do head aprovado: 9a12903fec91344ce632b3c5457c213bf44fe7d3. Consolidação documental separada para preservar o head funcional testado.
- ARCH-002e3.1 concluída experimentalmente. Sem homologação física ARM64 ou Windows; sem requisito definitivo de RAM. Próximo passo: ARCH-002e3.2, inventário de dependências, licenças e bibliotecas nativas conforme plano.
