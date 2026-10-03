package com.example.decisioncoin

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.random.Random

// Colors
val CoolBlueGrayLight = Color(0xFFE0E5EC)
val DeepNavyInkLight = Color(0xFF0A192F)
val BrassAccentLight = Color(0xFFC5A059) 
val CoolBlueGrayDark = Color(0xFF1A1C20)
val DeepNavyInkDark = Color(0xFFE2E8F0)
val BrassAccentDark = Color(0xFFD4AF37)

val OptionColors = listOf(
    Color(0xFF2196F3), // Blue
    Color(0xFFE91E63), // Pink
    Color(0xFF4CAF50), // Green
    Color(0xFFFFC107)  // Amber
)

private val LightColorScheme = lightColorScheme(
    background = CoolBlueGrayLight,
    surface = Color(0xFFF1F5F9),
    onBackground = DeepNavyInkLight,
    onSurface = DeepNavyInkLight,
    primary = DeepNavyInkLight,
    onPrimary = Color.White,
    secondary = BrassAccentLight,
    onSecondary = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    background = CoolBlueGrayDark,
    surface = Color(0xFF22252A),
    onBackground = DeepNavyInkDark,
    onSurface = DeepNavyInkDark,
    primary = DeepNavyInkDark,
    onPrimary = Color.Black,
    secondary = BrassAccentDark,
    onSecondary = Color.Black
)

@Composable
fun DecisionTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            headlineLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            displayMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 45.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp
            )
        ),
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DecisionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DecisionScreen()
                }
            }
        }
    }
}

@Composable
fun CoinFlip(
    options: List<String>,
    winnerIndex: Int,
    triggerSpin: Int,
    onAnimationEnd: () -> Unit
) {
    val context = LocalContext.current
    val durationScale = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }
    val rotation = remember { Animatable(0f) }
    val hop = remember { Animatable(0f) }
    
    val option1 = options.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "Option 1"
    val option2 = options.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "Option 2"
    val winnerName = options.getOrNull(winnerIndex)?.takeIf { it.isNotBlank() } ?: "Option ${winnerIndex + 1}"

    LaunchedEffect(triggerSpin) {
        if (triggerSpin > 0) {
            val targetRotation = rotation.value + (5 * 360f) + if (winnerIndex == 1) 180f else 0f
            val rem = targetRotation % 360
            val correction = if (winnerIndex == 0) -rem else (180f - rem)
            val finalRotation = targetRotation + correction

            val duration = (1800 * durationScale).toInt()
            if (duration > 0) {
                launch {
                    rotation.animateTo(finalRotation, tween(duration, easing = FastOutSlowInEasing))
                }
                launch {
                    hop.animateTo(-200f, tween(duration / 2, easing = FastOutSlowInEasing))
                    hop.animateTo(0f, tween(duration / 2, easing = FastOutLinearInEasing))
                }
            } else {
                rotation.snapTo(finalRotation)
            }
            onAnimationEnd()
        }
    }

    val rot = rotation.value
    val normalizedRot = abs(rot % 360f)
    val isFront = normalizedRot <= 90f || normalizedRot >= 270f
    
    val currentText = if (isFront) option1 else option2

    Box(
        modifier = Modifier
            .size(160.dp)
            .graphicsLayer {
                translationY = hop.value
                rotationY = rot
                cameraDistance = 80f * density
            }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFDF00), Color(0xFFDAA520))
                ),
                shape = CircleShape
            )
            .drawBehind {
                drawCircle(
                    color = Color(0xFFB8860B),
                    radius = size.width / 2f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                )
            }
            .semantics {
                if (triggerSpin > 0 && hop.value == 0f) {
                    contentDescription = "Coin landed on $winnerName"
                } else {
                    contentDescription = "Coin"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                if (!isFront) rotationY = 180f
            }.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5C4033)
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WheelSpin(
    options: List<String>,
    winnerIndex: Int,
    triggerSpin: Int,
    onAnimationEnd: () -> Unit
) {
    val context = LocalContext.current
    val durationScale = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }
    val rotation = remember { Animatable(0f) }
    
    val sweepAngle = 360f / options.size
    val winnerName = options.getOrNull(winnerIndex)?.takeIf { it.isNotBlank() } ?: "Option ${winnerIndex + 1}"

    LaunchedEffect(triggerSpin) {
        if (triggerSpin > 0) {
            val centerAngle = winnerIndex * sweepAngle + sweepAngle / 2f
            val jitter = Random.nextDouble(-(sweepAngle / 2.0 - 5.0), sweepAngle / 2.0 - 5.0).toFloat()
            val targetModulo = (270f - centerAngle + jitter) % 360f
            val currentModulo = rotation.value % 360f
            var diff = targetModulo - currentModulo
            if (diff < 0) diff += 360f
            
            val totalRotation = rotation.value + (5 * 360f) + diff
            val duration = (4000 * durationScale).toInt()
            if (duration > 0) {
                rotation.animateTo(totalRotation, tween(duration, easing = FastOutSlowInEasing))
            } else {
                rotation.snapTo(totalRotation)
            }
            onAnimationEnd()
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = Modifier
            .size(240.dp)
            .semantics {
                if (triggerSpin > 0 && !rotation.isRunning) {
                    contentDescription = "Wheel landed on $winnerName"
                } else {
                    contentDescription = "Decision Wheel"
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .graphicsLayer {
                    rotationZ = rotation.value
                }
        ) {
            val radius = size.width / 2f
            options.forEachIndexed { index, option ->
                val startAngle = index * sweepAngle
                drawArc(
                    color = OptionColors[index % OptionColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = size
                )
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = size,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
                
                val label = option.takeIf { it.isNotBlank() } ?: "Option ${index + 1}"
                val ellipsized = if (label.length > 12) label.take(10) + "..." else label
                
                rotate(degrees = startAngle + sweepAngle / 2f, pivot = center) {
                    val textLayoutResult = textMeasurer.measure(
                        text = ellipsized,
                        style = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    translate(
                        left = center.x + radius * 0.2f,
                        top = center.y - textLayoutResult.size.height / 2f
                    ) {
                        drawText(textLayoutResult)
                    }
                }
            }
        }
        
        Canvas(
            modifier = Modifier
                .size(24.dp)
                .offset(y = (-4).dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path = path, color = Color.DarkGray)
        }
    }
}

@Composable
fun DecisionScreen(viewModel: DecisionViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val logic = remember { DecisionLogic() }
    val haptics = LocalHapticFeedback.current

    var spinTrigger by rememberSaveable { mutableIntStateOf(0) }
    var showResultDetails by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.phase) {
        if (state.phase is DecisionPhase.Result) {
            showResultDetails = false
            spinTrigger++
        } else if (state.phase is DecisionPhase.Editing) {
            showResultDetails = false
            spinTrigger = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Decision Coin+",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        val isDeciding = state.phase is DecisionPhase.Deciding || (state.phase is DecisionPhase.Result && !showResultDetails)
        val isEditing = state.phase is DecisionPhase.Editing

        AnimatedVisibility(
            visible = isEditing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(OptionColors[index % OptionColors.size], CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedTextField(
                            value = option,
                            onValueChange = { viewModel.updateOption(index, it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Option ${index + 1}") },
                            enabled = isEditing,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (state.options.size > 2) {
                            IconButton(
                                onClick = { viewModel.removeOption(index) },
                                enabled = isEditing,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Text("X", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                if (state.options.size < 4) {
                    TextButton(
                        onClick = { viewModel.addOption() },
                        enabled = isEditing,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Add option", color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val isCoinEnabled = state.options.size == 2 && isEditing
                    val isWheelEnabled = isEditing

                    FilterChip(
                        selected = state.mode == DecisionMode.COIN,
                        onClick = { viewModel.setMode(DecisionMode.COIN) },
                        label = { Text("Coin") },
                        enabled = isCoinEnabled,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = state.mode == DecisionMode.WHEEL,
                        onClick = { viewModel.setMode(DecisionMode.WHEEL) },
                        label = { Text("Wheel") },
                        enabled = isWheelEnabled,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }

                val buttonText = if (state.mode == DecisionMode.COIN) "Flip the coin" else "Spin the wheel"
                Button(
                    onClick = { viewModel.decide() },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(buttonText)
                }
            }
        }

        AnimatedVisibility(
            visible = state.phase !is DecisionPhase.Editing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.mode == DecisionMode.COIN) {
                    CoinFlip(
                        options = state.options,
                        winnerIndex = state.winnerIndex ?: 0,
                        triggerSpin = spinTrigger,
                        onAnimationEnd = {
                            if (!showResultDetails) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showResultDetails = true
                            }
                        }
                    )
                } else {
                    WheelSpin(
                        options = state.options,
                        winnerIndex = state.winnerIndex ?: 0,
                        triggerSpin = spinTrigger,
                        onAnimationEnd = {
                            if (!showResultDetails) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showResultDetails = true
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showResultDetails,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val winnerIndex = state.winnerIndex ?: 0
                val winnerName = state.options.getOrNull(winnerIndex)?.takeIf { it.isNotBlank() } ?: "Option ${winnerIndex + 1}"

                Text(
                    text = winnerName,
                    style = MaterialTheme.typography.displayMedium,
                    color = OptionColors[winnerIndex % OptionColors.size],
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (state.feeling == null) {
                    Text("How do you feel?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { 
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onFeeling(Feeling.RELIEVED) 
                            },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Relieved")
                        }
                        Button(
                            onClick = { 
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onFeeling(Feeling.DISAPPOINTED) 
                            },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Disappointed")
                        }
                    }
                } else if (state.feeling == Feeling.DISAPPOINTED && state.options.size > 2 && state.hopedForIndex == null) {
                    Text("Which option were you hoping for?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.options.forEachIndexed { index, option ->
                            if (index != winnerIndex) {
                                val optionName = option.takeIf { it.isNotBlank() } ?: "Option ${index + 1}"
                                OutlinedButton(
                                    onClick = { viewModel.setHopedFor(index) },
                                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text(optionName, color = OptionColors[index % OptionColors.size])
                                }
                            }
                        }
                    }
                } else if (state.phase is DecisionPhase.Feedback) {
                    val message = when {
                        state.feeling == Feeling.RELIEVED -> {
                            logic.getRelievedMessage(winnerIndex, state.options)
                        }
                        state.feeling == Feeling.DISAPPOINTED && state.options.size == 2 -> {
                            logic.getDisappointed2OptionsMessage(state.hopedForIndex ?: 0, state.options)
                        }
                        state.feeling == Feeling.DISAPPOINTED && state.options.size > 2 -> {
                            logic.getDisappointedMoreOptionsMessage(state.hopedForIndex ?: 0, state.options)
                        }
                        else -> ""
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.editOptions() },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Edit options")
                        }
                        Button(
                            onClick = { viewModel.decideAgain() },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Decide again")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DecisionScreenPreviewLight() {
    DecisionTheme(darkTheme = false) {
        DecisionScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DecisionScreenPreviewDark() {
    DecisionTheme(darkTheme = true) {
        DecisionScreen()
    }
}