# Pythoteca

Ambiente multidisciplinar de aprendizagem tecnológica para Android, com laboratório Python integrado.

## Estado do projeto

Segunda geração em fase de fundação. As funcionalidades descritas abaixo são objetivos de desenvolvimento, não recursos já disponíveis neste repositório. Não há APK publicado nesta etapa.

## Proposta

- Biblioteca de conteúdos organizada por disciplinas, com acesso offline após obtenção dos materiais.
- Percurso de estudos intercalado, sem impedir a consulta livre à biblioteca.
- Exercícios essenciais e atividades opcionais de revisão.
- Laboratório Python com editor, console e execução local.
- Ferramentas de apoio, acompanhamento do progresso e backup.
- Experimentação futura com interfaces gráficas programáveis.

## Princípios de engenharia

Reconstrução seletiva, com revisão de cada componente antes de sua incorporação. Segurança, acessibilidade, manutenção e testes são requisitos desde a fundação.

O leitor e o executor deverão ter isolamento e permissões mínimas. A execução de código terá limites explícitos; o isolamento não será apresentado como garantia absoluta contra código hostil. A biblioteca online deverá validar origem, formato e integridade dos conteúdos. Credenciais, chaves de assinatura, dados pessoais e materiais acadêmicos privados não pertencem ao repositório público.

A integração contínua deverá utilizar permissões mínimas e não disponibilizar segredos a código não confiável. A assinatura de distribuição será mantida em ambiente separado.

## Direção visual

Glassmorphism com profundidade, iluminação difusa, superfícies translúcidas e bordas delicadas. A paleta-mater é composta por verde, preto, branco, cinza e suas variantes. A identidade da serpente será modernizada, explorando geometria e fluidez. Temas claro e escuro, contraste, legibilidade, adaptação a diferentes telas e desempenho serão considerados desde os protótipos.

## Roteiro inicial

1. FOUNDATION-001: fundação e documentação.
2. SECURITY-001: modelo de ameaças e requisitos de segurança.
3. ARCH-001: avaliação de arquitetura, Kotlin, Jetpack Compose e Chaquopy.
4. BRAND-001: identidade visual e propostas de marca.
5. DESIGN-001: design system e protótipos de alta fidelidade.
6. UI-001: interface navegável para avaliação em dispositivo.
7. Implementação incremental, testes e homologação.

As escolhas tecnológicas e a identidade visual definitiva dependem de avaliação. A compatibilidade com instalações existentes e com a biblioteca de conteúdos será investigada antes da distribuição.

## Licença

Licenciamento pendente de definição. A visibilidade pública do repositório não constitui, por si só, autorização para reutilização ou redistribuição do código.

### Prioridade de conectividade

Uso inicial pessoal, normalmente conectado. Acesso ao catálogo pela rede é o fluxo normal; download de aulas para leitura offline permanece disponível como objetivo secundário, sem impor arquitetura offline-first. A restrição de rede do executor continua independente desta prioridade. Não confundir leitura offline com instalação ou execução do laboratório.
