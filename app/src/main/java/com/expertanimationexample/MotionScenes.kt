package com.expertanimationexample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.*

/** Semi-implicit spring integration with substeps keeps high velocities stable. */
@Composable
private fun springValue(target: Float, playing: Boolean, speed: Float, stiffness: Float = 150f, damping: Float = 15f): Float {
    var position by remember { mutableFloatStateOf(target) }
    val latestTarget by rememberUpdatedState(target)
    val latestSpeed by rememberUpdatedState(speed)
    var velocity by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(playing, stiffness, damping) {
        if (playing) {
            var previous = 0L
            while (true) withFrameNanos { now ->
                if (previous != 0L) {
                    val dt = ((now - previous) / 1_000_000_000f).coerceAtMost(.04f) * latestSpeed / 4
                    repeat(4) {
                        velocity += ((latestTarget - position) * stiffness - damping * velocity) * dt
                        position += velocity * dt
                    }
                }
                previous = now
            }
        }
    }
    return position
}

@Composable
internal fun ElasticStack(playing: Boolean, speed: Float, time: Float) {
    var drag by remember { mutableStateOf(Offset.Zero) }
    val live by rememberUpdatedState(playing)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GridBackground()
        for (layer in 2 downTo 0) {
            val x = springValue(drag.x * (1f - layer * .22f), playing, speed, 180f - layer * 45, 12f + layer * 2)
            val y = springValue(drag.y * (1f - layer * .22f), playing, speed, 180f - layer * 45, 12f + layer * 2)
            val float = sin(time * 1.8f + layer * .5f) * 4
            val color = listOf(Mint, Violet, Color(0xFF5997E6))[layer]
            Box(Modifier.size(212.dp, 144.dp).graphicsLayer {
                translationX = x
                translationY = y + layer * 22.dp.toPx() + float
                rotationZ = x / 18 + layer * -5f
                scaleX = 1f - layer * .06f
                scaleY = scaleX
                shadowElevation = 20.dp.toPx()
                shape = RoundedCornerShape(22.dp)
                clip = true
            }.background(color).then(if (layer == 0) Modifier.semantics { contentDescription = "Упругая карточка: перетащите" }.pointerInput(Unit) {
                detectDragGestures(onDragEnd = { drag = Offset.Zero }, onDragCancel = { drag = Offset.Zero }) { change, amount ->
                    if (live) { change.consume(); drag = Offset((drag.x + amount.x).coerceIn(-250f, 250f), (drag.y + amount.y).coerceIn(-220f, 220f)) }
                }
            } else Modifier).padding(20.dp)) {
                Column {
                    Text("MOTION / ${layer + 1}", color = Ink.copy(alpha = .6f), fontSize = 10.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.weight(1f))
                    Text(if (layer == 0) "Feel the physics." else "Follow the lead.", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("DRAG • RELEASE • REPEAT", color = Ink.copy(alpha = .6f), fontSize = 8.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
internal fun SpatialCarousel(playing: Boolean, speed: Float) {
    var target by remember { mutableFloatStateOf(0f) }
    val phase = springValue(target, playing, speed, 110f, 18f)
    val live by rememberUpdatedState(playing)
    val density = LocalDensity.current.density
    val tracker = remember { VelocityTracker() }
    val colors = listOf(Mint, Violet, Color(0xFF78D9EE), Color(0xFFFFB98F), Color(0xFFE998CA))
    Box(Modifier.fillMaxSize().semantics { contentDescription = "3D-карусель: листайте горизонтально" }.pointerInput(Unit) {
        detectDragGestures(onDragStart = { tracker.resetTracking() }, onDragEnd = {
            target = (target - tracker.calculateVelocity().x / (density * 1200)).roundToInt().toFloat()
        }, onDragCancel = { target = target.roundToInt().toFloat() }) { change, amount ->
            if (live) { change.consume(); tracker.addPosition(change.uptimeMillis, change.position); target -= amount.x / (density * 170) }
        }
    }, contentAlignment = Alignment.Center) {
        GridBackground()
        colors.forEachIndexed { index, color ->
            val angle = (index - phase) * (2 * PI / 5).toFloat()
            val depth = cos(angle)
            Box(Modifier.zIndex(depth).size(128.dp, 176.dp).graphicsLayer {
                translationX = sin(angle) * 114.dp.toPx()
                scaleX = .72f + .28f * (depth + 1) / 2
                scaleY = scaleX
                rotationY = -sin(angle) * 48
                cameraDistance = 14 * density
                alpha = .4f + .6f * (depth + 1) / 2
                shape = RoundedCornerShape(20.dp)
                clip = true
                shadowElevation = 12.dp.toPx()
            }.background(Brush.verticalGradient(listOf(color, color.copy(alpha = .6f)))).padding(16.dp)) {
                Column {
                    Text("0${index + 1}", color = Ink, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    Text(listOf("FORM", "SPACE", "LIGHT", "DEPTH", "FLOW")[index], color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("PERSPECTIVE", color = Ink.copy(alpha = .6f), fontSize = 8.sp)
                }
            }
        }
        Text("←  SWIPE TO EXPLORE  →", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
    }
}

@Composable
internal fun ShapeLaboratory(playing: Boolean, speed: Float, time: Float) {
    var selected by remember { mutableIntStateOf(0) }
    val star = springValue(if (selected == 1) 1f else 0f, playing, speed, 90f, 16f)
    val organic = springValue(if (selected == 2) 1f else 0f, playing, speed, 90f, 16f)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.weight(1f).fillMaxWidth().semantics { contentDescription = "Морфинг круга, звезды и органического контура" }) {
            val center = center
            val base = min(size.width, size.height) * .32f
            val path = Path()
            val count = 240
            for (i in 0..count) {
                val angle = i * 2f * PI.toFloat() / count
                val triangle = acos(cos(angle * 5)) / PI.toFloat()
                val starRadius = 1f - triangle * .48f
                val blobRadius = 1f + .16f * sin(angle * 3 + time) + .09f * cos(angle * 5 - time * .7f)
                val radius = base * (1f + star * (starRadius - 1f) + organic * (blobRadius - 1f))
                val rotated = angle + time * .22f
                val point = center + Offset(cos(rotated), sin(rotated)) * radius
                if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            path.close()
            drawCircle(Mint.copy(alpha = .04f), base * 1.35f)
            drawPath(path, Brush.linearGradient(listOf(Mint, Color(0xFF67DCCB), Violet)))
            drawPath(path, Color.White.copy(alpha = .3f), style = Stroke(1.5.dp.toPx()))
            drawCircle(Ink, 5.dp.toPx())
        }
        Row(Modifier.padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Круг", "Звезда", "Органика").forEachIndexed { index, name ->
                Surface(onClick = { if (playing) selected = index }, color = if (selected == index) Mint else Ink, shape = RoundedCornerShape(12.dp)) {
                    Text(name, color = if (selected == index) Ink else Muted, fontSize = 11.sp, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}

@Composable
internal fun OrbitalField(playing: Boolean, speed: Float, time: Float) {
    var target by remember { mutableStateOf(Offset(.5f, .5f)) }
    val x = springValue(target.x, playing, speed, 70f, 10f)
    val y = springValue(target.y, playing, speed, 70f, 10f)
    val live by rememberUpdatedState(playing)
    Canvas(Modifier.fillMaxSize().semantics { contentDescription = "Орбитальные частицы: коснитесь, чтобы переместить центр" }.pointerInput(Unit) {
        detectTapGestures { if (live) target = Offset(it.x / size.width, it.y / size.height) }
    }) {
        val origin = Offset(size.width * x, size.height * y)
        val radius = min(size.width, size.height) * .4f
        drawCircle(Brush.radialGradient(listOf(Violet.copy(alpha = .18f), Color.Transparent), origin, radius), radius, origin)
        repeat(96) { i ->
            val ring = i % 6
            val r = radius * (.2f + ring * .135f)
            val phase = i * 2.39996f
            val rate = .32f + ring * .09f
            val color = if (i % 3 == 0) Mint else if (i % 3 == 1) Violet else Color(0xFF6CDDE5)
            fun point(t: Float): Offset {
                val angle = phase + t * rate
                return origin + Offset(cos(angle) * r, sin(angle) * r * .68f + sin(t * .6f + phase) * 10.dp.toPx())
            }
            repeat(6) { trail ->
                val p = point(time - trail * .09f)
                drawCircle(color.copy(alpha = (1f - trail / 6f) * .55f), (2.6f - trail * .3f).dp.toPx(), p)
            }
            drawCircle(color.copy(alpha = .07f), 7.dp.toPx(), point(time))
        }
        drawCircle(Mint, 4.dp.toPx(), origin)
        drawCircle(Mint.copy(alpha = .25f), (12 + 3 * sin(time * 2)).dp.toPx(), origin, style = Stroke(1.dp.toPx()))
    }
}

@Composable
internal fun RadialBloom(playing: Boolean, speed: Float, time: Float) {
    var open by remember { mutableStateOf(false) }
    var chosen by remember { mutableIntStateOf(0) }
    val colors = listOf(Mint, Violet, Color(0xFF78D9EE), Color(0xFFFFB98F), Color(0xFFE998CA))
    val names = listOf("Идея", "Слои", "Свет", "Ритм", "Цвет")
    val progress = springValue(if (open) 1f else 0f, playing, speed, 100f, 14f)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(3) { index ->
                val pulse = ((time * .25f + index / 3f) % 1f)
                drawCircle(colors[chosen].copy(alpha = (1 - pulse) * .14f * progress.coerceIn(0f, 1f)), (35 + pulse * 130).dp.toPx(), style = Stroke(1.dp.toPx()))
            }
        }
        names.forEachIndexed { index, name ->
            // A travelling activation threshold creates stagger without wall-clock delays.
            val localTarget = ((progress - index * .075f) / (1f - index * .075f)).coerceIn(0f, 1f)
            val p = springValue(localTarget, playing, speed, 230f, 12f)
            val angle = (-90 + index * 72) * PI.toFloat() / 180
            Surface(onClick = { if (playing && open) chosen = index }, enabled = open && playing && p > .8f,
                shape = CircleShape, color = if (chosen == index) colors[index] else Color(0xFF2A3445),
                modifier = Modifier.size(58.dp).graphicsLayer {
                    translationX = cos(angle) * 104.dp.toPx() * p
                    translationY = sin(angle) * 104.dp.toPx() * p
                    scaleX = p.coerceAtLeast(.01f); scaleY = scaleX
                    alpha = p.coerceIn(0f, 1f)
                    rotationZ = (1 - p) * -90
                }) {
                Box(contentAlignment = Alignment.Center) { Text(name, color = if (chosen == index) Ink else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Surface(onClick = { if (playing) open = !open }, shape = CircleShape, color = colors[chosen],
            modifier = Modifier.size(70.dp).graphicsLayer { rotationZ = progress * 135; shadowElevation = 12.dp.toPx() }) {
            Box(contentAlignment = Alignment.Center) { Text("+", color = Ink, fontSize = 36.sp, fontWeight = FontWeight.Light) }
        }
        Text(if (open) "ВЫБРАНО: ${names[chosen].uppercase()}" else "TAP TO BLOOM", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp))
    }
}

@Composable
private fun GridBackground() {
    Canvas(Modifier.fillMaxSize()) {
        val step = 24.dp.toPx()
        var x = step
        while (x < size.width) {
            var y = step
            while (y < size.height) { drawCircle(Muted.copy(alpha = .13f), 1.dp.toPx(), Offset(x, y)); y += step }
            x += step
        }
    }
}
