package com.thanjavur.wobbledoll

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Single-image wobble view for the rocking horse.
 * The whole image rocks around its base (bottom-center pivot), like a real
 * rocking horse pivoting on its curved rockers. Tapping pushes it and it rocks
 * back and forth until it settles.
 *
 * Two rock modes (set via the `rockMode` XML attribute):
 *  - [Mode.SIDE]: side-to-side tilt (Z rotation). Tap left/right half to push.
 *  - [Mode.FRONT_BACK]: pitch toward/away from the viewer (X rotation, with
 *    perspective). Tap top/bottom half to push.
 *
 * The image is chosen with the `rockImage` attribute (a drawable name);
 * it defaults to "rocking_horse1".
 */
class RockingHorseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class Mode { SIDE, FRONT_BACK }

    private var imageName = "rocking_horse1"
    private var mode = Mode.SIDE
    private var rockEnabled = true

    // Single-pendulum physics — rocks around the bottom-center pivot.
    private var angle = 0f
    private var velocity = 0f
    private val stiffness = 22f    // springy return — a horse rocks faster than the heavy Patti doll
    private val damping = 0.2f     // gentle decay so the rocking lingers

    // Reused for the FRONT_BACK perspective pitch.
    private val camera = Camera()
    private val tiltMatrix = Matrix()
    // Scales the visible camera pitch relative to the physics angle (which can
    // reach ~30°). 0.5 → a gentler front/back lean of about 15° at full swing.
    private val frontBackTiltScale = 0.5f

    // Bitmaps
    private var rawBitmap: Bitmap? = null
    private var scaledBitmap: Bitmap? = null

    // Pre-computed layout (set once in onSizeChanged)
    private var cx = 0f
    private var pivotY = 0f       // bottom of image = rotation pivot (rockers)
    private var bitmapLeft = 0f
    private var bitmapTop = 0f
    private val shadowOval = RectF()

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#50000000")
    }

    private var animating = false
    private var lastFrameNanos = 0L

    var onTouched: (() -> Unit)? = null

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.RockingHorseView)
            a.getString(R.styleable.RockingHorseView_rockImage)?.let { name -> imageName = name }
            mode = if (a.getInt(R.styleable.RockingHorseView_rockMode, 0) == 1) Mode.FRONT_BACK else Mode.SIDE
            rockEnabled = a.getBoolean(R.styleable.RockingHorseView_rockEnabled, true)
            a.recycle()
        }
        rawBitmap = loadBitmap(imageName)
    }

    private fun loadBitmap(name: String): Bitmap? {
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) BitmapFactory.decodeResource(resources, id) else null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        cx = w / 2f

        // Scale image to fill up to 80% width while fitting in 85% height
        val maxW = (w * 0.80f).toInt()
        val maxH = (h * 0.85f).toInt()
        scaledBitmap = fitBitmap(rawBitmap, maxW, maxH)

        scaledBitmap?.let { bmp ->
            bitmapLeft = cx - bmp.width / 2f
            bitmapTop = h * 0.20f                    // small top padding
            pivotY = bitmapTop + bmp.height          // base of horse = pivot
        }

        shadowOval.set(cx - w * 0.24f, pivotY - 7f, cx + w * 0.24f, pivotY + 7f)
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
        if (angle > 30f)  { angle = 30f;  velocity = -velocity * 0.25f }
        if (angle < -30f) { angle = -30f; velocity = -velocity * 0.25f }
    }

    private fun isAtRest() = abs(angle) < 0.01f && abs(velocity) < 0.01f

    fun applyImpulse(force: Float) {
        velocity += force
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
            invalidate()
            if (!isAtRest()) {
                Choreographer.getInstance().postFrameCallback(this)
            } else {
                animating = false
                lastFrameNanos = 0L
            }
        }
    }

    // ── Touch ─────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!rockEnabled) return false
        if (event.action == MotionEvent.ACTION_DOWN) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            // SIDE: tap left/right half rocks that way.
            // FRONT_BACK: tap upper half tips it back, lower half tips it forward.
            val force = if (mode == Mode.FRONT_BACK) {
                if (event.y < height / 2f) -22f else 22f
            } else {
                if (event.x < width / 2f) -22f else 22f
            }
            applyImpulse(force)
            onTouched?.invoke()
            performClick()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean { super.performClick(); return true }

    // ── Drawing ───────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        scaledBitmap?.let { bmp ->
            canvas.save()
            if (mode == Mode.FRONT_BACK) {
                // Pitch the image toward/away from the viewer around its base,
                // so it appears to rock to the front and back.
                camera.save()
                camera.rotateX(angle * frontBackTiltScale)
                camera.getMatrix(tiltMatrix)
                camera.restore()
                tiltMatrix.preTranslate(-cx, -pivotY)
                tiltMatrix.postTranslate(cx, pivotY)
                canvas.concat(tiltMatrix)
            } else {
                canvas.rotate(angle, cx, pivotY)  // rock around base of horse
            }
            canvas.drawBitmap(bmp, bitmapLeft, bitmapTop, null)
            canvas.restore()
        }
        canvas.drawOval(shadowOval, shadowPaint)
    }
}