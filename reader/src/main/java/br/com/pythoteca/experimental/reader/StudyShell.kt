package br.com.pythoteca.experimental.reader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Night = darkColorScheme(
    primary = Color(0xFF83E2AC), onPrimary = Color(0xFF10251A),
    background = Color(0xFF101816), onBackground = Color(0xFFF1F6F3),
    surface = Color(0xFF1B2925), onSurface = Color(0xFFF1F6F3),
    surfaceVariant = Color(0xFF263832), onSurfaceVariant = Color(0xFFD1DED7),
    outline = Color(0xFF8DA69A)
)
private val Day = lightColorScheme(
    primary = Color(0xFF14663F), onPrimary = Color.White,
    background = Color(0xFFF3F7F4), onBackground = Color(0xFF14251D),
    surface = Color.White, onSurface = Color(0xFF14251D),
    surfaceVariant = Color(0xFFE1ECE5), onSurfaceVariant = Color(0xFF31483B),
    outline = Color(0xFF627B6C)
)
private enum class Destination(val label: String) {
    HOME("Início"), TRAIL("Trilha de estudos"), LIBRARY("Biblioteca"),
    REVIEW("Revisão"), LAB("Laboratórios"), DATA("Dados e preferências")
}
private val disciplines = listOf(
    "Arquitetura de Computadores", "Lógica de Programação", "Java",
    "Sistemas Operacionais", "Tecnologia da Informação", "Modelagem de Dados", "Python"
)

@Composable
internal fun StudyShell(status: String, onPing: () -> Unit, onPython: () -> Unit) {
    var screen by rememberSaveable { mutableStateOf(Destination.HOME.name) }
    var dark by rememberSaveable { mutableStateOf(true) }
    var discipline by rememberSaveable { mutableStateOf("") }
    val destination = Destination.valueOf(screen)
    fun go(to: Destination) { discipline = ""; screen = to.name }
    BackHandler(destination != Destination.HOME) {
        if (discipline.isNotEmpty()) discipline = "" else go(Destination.HOME)
    }
    MaterialTheme(colorScheme = if (dark) Night else Day) {
        val colors = MaterialTheme.colorScheme
        Surface(Modifier.fillMaxSize(), color = colors.background) {
            Column(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(colors.surface, colors.background))
                ).safeDrawingPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("PYTHOTECA", style = MaterialTheme.typography.titleLarge, color = colors.primary)
                if (destination != Destination.HOME) {
                    TextButton(onClick = {
                        if (discipline.isNotEmpty()) discipline = "" else go(Destination.HOME)
                    }) { Text(if (discipline.isNotEmpty()) "Voltar à biblioteca" else "Voltar ao início") }
                }
                key(screen, discipline) {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        when (destination) {
                            Destination.HOME -> {
                                Text("Seu espaço de estudos", style = MaterialTheme.typography.headlineSmall)
                                Text("Versão experimental · conteúdo ainda não conectado",
                                    style = MaterialTheme.typography.bodyMedium)
                                StudyPanel {
                                    Text("Trilha de estudos", style = MaterialTheme.typography.titleMedium)
                                    Text("Percurso guiado entre disciplinas.")
                                    Button(onClick = { go(Destination.TRAIL) }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Abrir trilha")
                                    }
                                }
                                OutlinedButton(onClick = { go(Destination.LIBRARY) }, modifier = Modifier.fillMaxWidth()) { Text("Biblioteca") }
                                OutlinedButton(onClick = { go(Destination.REVIEW) }, modifier = Modifier.fillMaxWidth()) { Text("Revisão") }
                                OutlinedButton(onClick = { go(Destination.LAB) }, modifier = Modifier.fillMaxWidth()) { Text("Laboratórios") }
                                OutlinedButton(onClick = { go(Destination.DATA) }, modifier = Modifier.fillMaxWidth()) { Text("Dados e preferências") }
                            }
                            Destination.LIBRARY -> {
                                Text(if (discipline.isEmpty()) "Biblioteca por disciplina" else discipline,
                                    style = MaterialTheme.typography.headlineSmall)
                                Text("Catálogo ainda não conectado. Nenhuma aula disponível nesta versão.")
                                if (discipline.isEmpty()) disciplines.forEach { name ->
                                    OutlinedButton(onClick = { discipline = name }, modifier = Modifier.fillMaxWidth()) { Text(name) }
                                }
                            }
                            Destination.TRAIL -> PendingPanel("Trilha de estudos", "O percurso será calculado a partir das aulas verificadas e do seu progresso. Nenhuma sessão disponível ainda.")
                            Destination.REVIEW -> PendingPanel("Revisão", "As atividades de revisão serão disponibilizadas após a integração do catálogo e do progresso.")
                            Destination.LAB -> PendingPanel("Laboratórios", "Os motores estão em validação. A execução de código do estudante ainda não está habilitada.")
                            Destination.DATA -> {
                                Text("Dados e preferências", style = MaterialTheme.typography.headlineSmall)
                                StudyPanel {
                                    Text(if (dark) "Tema escuro" else "Tema claro")
                                    OutlinedButton(onClick = { dark = !dark }) { Text(if (dark) "Usar tema claro" else "Usar tema escuro") }
                                    Text("Sem progresso ou dados de estudos armazenados nesta versão.")
                                }
                            }
                        }
                    }
                }
                if (destination == Destination.HOME) {
                    HorizontalDivider()
                    Text("Diagnóstico experimental", style = MaterialTheme.typography.labelMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onPing, modifier = Modifier.weight(1f)) { Text(status) }
                        Button(onClick = onPython, modifier = Modifier.weight(1f)) { Text("Testar Python") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyPanel(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    Column(Modifier.fillMaxWidth().background(colors.surface.copy(alpha = .88f), shape)
        .border(1.dp, colors.outline.copy(alpha = .55f), shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun PendingPanel(title: String, description: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall)
    StudyPanel { Text(description) }
}
