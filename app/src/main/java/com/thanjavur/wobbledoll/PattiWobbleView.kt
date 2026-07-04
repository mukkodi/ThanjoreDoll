package com.thanjavur.wobbledoll

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import android.view.Choreographer
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Single-image wobble view for the Patti (grandmother) Tanjore doll.
 * The whole image rotates around its base (bottom-center pivot), like a real
 * weighted ceramic doll on a rounded stand.
 */
class PattiWobbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Simple single-pendulum physics (no Hilt needed — no shared state)
    private var angle = 0f
    private var velocity = 0f
    private val stiffness = 18f    // soft spring → slow, heavy-doll oscillation
    private val damping = 0.2f    // gradual decay, realistic ceramic weight

    // Bitmaps
    private var rawBitmap: Bitmap? = null
    private var scaledBitmap: Bitmap? = null

    // Pre-computed layout (set once in onSizeChanged)
    private var cx = 0f
    private var pivotY = 0f       // bottom of image = rotation pivot (doll's base)
    private var bitmapLeft = 0f
    private var bitmapTop = 0f
    private val shadowOval = RectF()

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#50000000")
    }

    private var animating = false
    private var lastFrameNanos = 0L

    private var mediaPlayer: MediaPlayer? = null
    private var currentSpeed = 1f

    // Energy thresholds driving wobble-sound speed (see wobbleEnergy())
    private val restEnergy = 1f     // below this → settled, stop sound
    private val lowEnergy = 25f     // gentle leftover sway → slow toll
    private val midEnergy = 90f     // normal swing → 1x

    var onTouched: (() -> Unit)? = null

    init {
        rawBitmap = loadBitmap("patti_wobble")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mediaPlayer = MediaPlayer.create(context, R.raw.wobble_sound)?.apply {
            isLooping = true
        }
    }

    private fun loadBitmap(name: String): Bitmap? {
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) BitmapFactory.decodeResource(resources, id) else null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        cx = w / 2f

        // Scale image to fill up to 72% width while fitting in 90% height
        val maxW = (w * 0.72f).toInt()
        val maxH = (h * 0.90f).toInt()
        scaledBitmap = fitBitmap(rawBitmap, maxW, maxH)

        scaledBitmap?.let { bmp ->
            bitmapLeft = cx - bmp.width / 2f
            bitmapTop = h * 0.20f                    // small top padding
            pivotY = bitmapTop + bmp.height          // base of doll = pivot
        }

        shadowOval.set(cx - w * 0.20f, pivotY - 7f, cx + w * 0.20f, pivotY + 7f)
    }

    private fun fitBitmap(src: Bitmap?, maxW: Int, maxH: Int): Bitmap? {
        src ?: return null
        val scaleW = maxW.toFloat() / src.width
        val scaleH = maxH.toFloat() / src.height
        val scale = minOf(scaleW, scaleH)
        val tw = (src.width * scale).toInt().coerceAtLeast(1)
        val th = (src.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, tw, th, true)
    }

    // ── Physics ──────────────────────────────────────────────────────────

    private fun step(dt: Float) {
        val acc = -stiffness * angle - damping * velocity
        velocity += acc * dt
        angle += velocity * dt
        if (angle > 32f)  { angle = 32f;  velocity = -velocity * 0.25f }
        if (angle < -32f) { angle = -32f; velocity = -velocity * 0.25f }
    }

    private fun isAtRest() = abs(angle) < 0.01f && abs(velocity) < 0.01f

    /**
     * Total mechanical energy (potential + kinetic) of the pendulum.
     * Raw angle hits zero at the centre of every swing and raw velocity hits
     * zero at every peak — using either alone would flicker the audio speed
     * twice per cycle. Energy decays smoothly under damping and tracks how
     * much wobble is actually left, independent of where in the cycle we are.
     */
    private fun wobbleEnergy(): Float =
        0.5f * stiffness * angle * angle + 0.5f * velocity * velocity

    /**
     * Bell rings fast while the doll swings hard, normal speed in the mid-swing,
     * and a slow toll as energy bleeds off toward rest.
     */
    private fun updatePlaybackSpeed() {
        val player = mediaPlayer ?: return
        val energy = wobbleEnergy()
        Log.d("wobble", "---wobble energy--$energy")

        // Energy has bled off — cut the sound here instead of waiting for
        // the stricter isAtRest() physics threshold.
        if (energy < restEnergy) {
            if (player.isPlaying) player.pause()
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (!player.isPlaying) return

        val targetSpeed = when {
            energy <= lowEnergy -> 0.50f   // gentle leftover sway
            energy <= midEnergy -> 1f      // normal swing
            else                -> 2f      // vigorous swing
        }

        if (targetSpeed != currentSpeed) {
            currentSpeed = targetSpeed
            try {
                player.playbackParams = player.playbackParams.setSpeed(targetSpeed)
            } catch (_: IllegalStateException) {}
        }
    }

    fun applyImpulse(force: Float) {
        velocity += force
        if (mediaPlayer?.isPlaying == false) mediaPlayer?.start()
        if (!animating) {
            animating = true
            lastFrameNanos = 0L
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (lastFrameNanos == 0L) {
                lastFrameNanos = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
                return
            }
            val dt = ((frameTimeNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(0.033f)
            lastFrameNanos = frameTimeNanos
            step(dt)
            updatePlaybackSpeed()
            invalidate()
            if (!isAtRest()) {
                Choreographer.getInstance().postFrameCallback(this)
            } else {
                animating = false
                lastFrameNanos = 0L
                mediaPlayer?.pause()
            }
        }
    }

    // ── Touch ─────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val force = if (event.x < width / 2f) -20f else 20f
            applyImpulse(force)
            onTouched?.invoke()
            performClick()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean { super.performClick(); return true }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // ── Drawing ───────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        scaledBitmap?.let { bmp ->
            canvas.save()
            canvas.rotate(angle, cx, pivotY)  // rotate around base of doll
            canvas.drawBitmap(bmp, bitmapLeft, bitmapTop, null)
            canvas.restore()
        }
        canvas.drawOval(shadowOval, shadowPaint)
    }
}