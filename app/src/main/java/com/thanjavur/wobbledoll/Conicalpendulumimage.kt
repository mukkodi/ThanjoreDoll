package com.thanjavur.wobbledoll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Displays [painter] hanging from a pivot on a virtual string.
 * Idle: perfectly still.
 * Touch: inverse wobble — tap LEFT and the head first flies RIGHT, then oscillates back
 * through centre with a decaying spring (tap RIGHT → head flies LEFT first).
 *
 * @param imageSize    rendered size of the head image.
 * @param stringLength length of the virtual string above the image (0 = hinge at image top).
 * @param coneAngleDeg peak swing angle on touch (degrees); also controls lean.
 * @param periodMillis reserved for API compatibility — unused; spring controls timing.
 * @param foreshorten  Y-compression of pendulum arc to fake 3-D depth.
 * @param depthScale   scale boost when the head swings toward the viewer.
 * @param leanFactor   how strongly the head tilts into the swing direction.
 * @param tiltFactor   how strongly the head pitches toward/away from the viewer (rotationX),
 *                      giving the swing real 3-D depth instead of a faked scale change.
 * @param perspectiveDistance camera distance for the rotationX perspective; smaller values
 *                      exaggerate the 3-D effect, larger values flatten it.
 * @param showString   whether to draw the pivot dot and connecting string line.
 * @param stringColor  colour of the string and pivot dot.
 */
@Composable
fun ConicalPendulumImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    imageSize: Dp = 150.dp,
    stringLength: Dp = 0.dp,
    coneAngleDeg: Float = 22f,
    periodMillis: Int = 2600,
    foreshorten: Float = 0.34f,
    depthScale: Float = 0.06f,
    leanFactor: Float = 40.7f,
    tiltFactor: Float = 15f,
    perspectiveDistance: Float = 50f,
    showString: Boolean = false,
    stringColor: Color = Color(0xFF8A8A8A),
) {
    val scope   = rememberCoroutineScope()
    val density = LocalDensity.current

    // Swing angle in degrees: 0 = resting vertically, + = right, − = left
    val swingAngle = remember { Animatable(0f) }
    // Nod angle in degrees: 0 = upright, + = tilting forward (yes-nod)
    val nodAngle   = remember { Animatable(0f) }

    val pivotPad = with(density) { 16.dp.toPx() }
    val stringPx = with(density) { stringLength.toPx() }
    val imagePx  = with(density) { imageSize.toPx() }

    val sceneHeight = with(density) {
        (pivotPad + stringPx + imagePx + 16f).toDp()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sceneHeight)
            .pointerInput(coneAngleDeg) {
                detectTapGestures(
                    onPress = { offset ->
                        val w = size.width
                        if (offset.x > w / 3f && offset.x < w * 2f / 3f) {
                            // Centre tap → yes-nod (rotationX forward/back)
                            scope.launch {
                                nodAngle.stop()
                                nodAngle.snapTo(25f)
                                nodAngle.animateTo(
                                    targetValue  = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy * 0.01f,
                                        stiffness    = Spring.StiffnessLow * 0.9f,
                                    )
                                )
                            }
                        } else {
                            // Left/right tap → side swing
                            scope.launch {
                                swingAngle.stop()
                                val peak = if (offset.x < w / 2f) -coneAngleDeg else coneAngleDeg
                                swingAngle.snapTo(peak)
                                swingAngle.animateTo(
                                    targetValue  = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy * 0.03f,
                                        stiffness    = Spring.StiffnessLow * 0.9f,
                                    )
                                )
                            }
                        }
                    }
                )
            }
    ) {

        // ── Pivot dot + string ────────────────────────────────────────────
        if (showString) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val rad = Math.toRadians(swingAngle.value.toDouble()).toFloat()
                val cx  = size.width / 2f
                val bx  = cx + stringPx * sin(rad)
                val by  = pivotPad + stringPx * cos(rad)
                drawCircle(stringColor, radius = 5f, center = Offset(cx, pivotPad))
                drawLine(
                    color       = stringColor,
                    start       = Offset(cx, pivotPad),
                    end         = Offset(bx, by),
                    strokeWidth = 3f,
                    cap         = StrokeCap.Round,
                )
            }
        }

        // ── Head image ────────────────────────────────────────────────────
        Image(
            painter            = painter,
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .align(Alignment.TopCenter)
                .size(imageSize)
                .graphicsLayer {
                    val rad  = Math.toRadians(swingAngle.value.toDouble()).toFloat()
                    val sinR = sin(rad)
                    val cosR = cos(rad)

                    translationX = stringPx * sinR
                    translationY = pivotPad + stringPx * cosR

                    rotationZ = swingAngle.value * leanFactor
                    rotationX = swingAngle.value * tiltFactor + nodAngle.value
                    cameraDistance = perspectiveDistance * density.density

                    val s = 1f + depthScale * sinR * foreshorten
                    scaleX = s
                    scaleY = s

                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                },
        )
    }
}