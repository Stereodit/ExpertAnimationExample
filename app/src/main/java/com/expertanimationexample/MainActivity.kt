package com.expertanimationexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val Ink = Color(0xFF0B1018)
internal val Panel = Color(0xFF151D2A)
internal val Mint = Color(0xFFB8F36D)
internal val Muted = Color(0xFF96A3B6)
internal val Violet = Color(0xFFA99CFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Mint, background = Ink, surface = Panel)) { MotionLab() } }
    }
}

private data class Experiment(val name: String, val category: String, val hint: String, val detail: String)
private val experiments = listOf(
    Experiment("Elastic stack", "01 / PHYSICS", "Потяните верхнюю карточку и отпустите", "Независимые пружины связывают три слоя. Жест управляет смещением и наклоном, а после отпускания каждый слой возвращается с собственной жёсткостью и затуханием."),
    Experiment("Spatial carousel", "02 / PERSPECTIVE", "Листайте карточки горизонтально", "Положение на окружности задаёт глубину, масштаб и поворот по оси Y. Скорость жеста и пружинная фиксация создают инерционную 3D-карусель."),
    Experiment("Shape laboratory", "03 / GEOMETRY", "Выберите форму для плавного превращения", "Контуры имеют одинаковое число точек. Интерполяция радиуса каждой точки превращает круг в звезду и органическую форму; градиент и вращение следуют единому времени."),
    Experiment("Orbital field", "04 / CANVAS", "Коснитесь поля, чтобы переместить его центр", "96 частиц движутся по параметрическим орбитам с индивидуальными фазами. Следы, свечение и пружинное перемещение центра рисуются в Canvas без отдельных элементов разметки."),
    Experiment("Radial bloom", "05 / CHOREOGRAPHY", "Раскройте меню и выберите действие", "Пять кнопок появляются с задержкой, двигаются по дуге и пружинят независимо. Центральный переключатель поворачивается, фон получает пульсирующие кольца, выбор меняет акцент.")
)

@Composable
internal fun MotionLab(initialPlaying: Boolean = true) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var playing by rememberSaveable { mutableStateOf(initialPlaying) }
    var speed by rememberSaveable { mutableFloatStateOf(1f) }
    var replay by remember { mutableIntStateOf(0) }
    val item = experiments[selected]
    Column(Modifier.fillMaxSize().background(Ink).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("M /", color = Mint, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("COMPOSE • INTERACTIVE", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(28.dp))
        Text("Motion Lab", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-1).sp)
        Text("Пять экспериментов. Безграничное движение.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        Row(Modifier.padding(top = 26.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            experiments.forEachIndexed { index, experiment ->
                Surface(onClick = { selected = index; replay++ }, color = if (selected == index) Mint else Panel, shape = RoundedCornerShape(14.dp)) {
                    Text("0${index + 1}  ${experiment.name}", Modifier.padding(horizontal = 16.dp, vertical = 13.dp), color = if (index == selected) Ink else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        AnimatedContent(selected, label = "experiment title") { index ->
            Column {
                Text(experiments[index].category, color = Mint, fontSize = 10.sp, letterSpacing = 2.sp)
                Text(experiments[index].name, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(28.dp), color = Panel, modifier = Modifier.fillMaxWidth().height(330.dp)) {
            key(selected, replay) {
                val time = sceneTime(playing, speed)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (selected) {
                        0 -> ElasticStack(playing, speed, time)
                        1 -> SpatialCarousel(playing, speed)
                        2 -> ShapeLaboratory(playing, speed, time)
                        3 -> OrbitalField(playing, speed, time)
                        else -> RadialBloom(playing, speed, time)
                    }
                }
            }
        }
        Text(item.hint, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = { playing = !playing }, contentPadding = PaddingValues(horizontal = 12.dp)) { Text(if (playing) "Пауза" else "Продолжить", fontSize = 12.sp) }
            OutlinedButton(onClick = { replay++ }, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("Сброс", fontSize = 12.sp) }
            Spacer(Modifier.weight(1f))
            Text("${"%.2f".format(speed)}×", color = Mint, fontSize = 12.sp)
        }
        Slider(value = speed, onValueChange = { speed = it }, valueRange = .25f..1.5f)
        Text("СКОРОСТЬ ДВИЖЕНИЯ", color = Muted, fontSize = 9.sp, letterSpacing = 1.8.sp)
        HorizontalDivider(Modifier.padding(vertical = 22.dp), color = Color(0xFF283241))
        Text("ПОД КАПОТОМ", color = Mint, fontSize = 10.sp, letterSpacing = 2.sp)
        Text(item.detail, color = Muted, fontSize = 13.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 10.dp))
        Text("BUILT WITH JETPACK COMPOSE", color = Muted.copy(alpha = .5f), fontSize = 9.sp, letterSpacing = 2.sp, modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
    }
}

/** A shared frame clock that preserves elapsed time across pause and resume. */
@Composable
internal fun sceneTime(playing: Boolean, speed: Float): Float {
    var elapsed by remember { mutableFloatStateOf(0f) }
    val currentSpeed by rememberUpdatedState(speed)
    LaunchedEffect(playing) {
        if (playing) {
            var previous = 0L
            while (true) withFrameNanos { now ->
                if (previous != 0L) elapsed += ((now - previous) / 1_000_000_000f).coerceAtMost(.05f) * currentSpeed
                previous = now
            }
        }
    }
    return elapsed
}
