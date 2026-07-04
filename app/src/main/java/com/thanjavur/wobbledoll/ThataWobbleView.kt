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
 * Thata (grandfather) doll view.
 * The base body is drawn statically; only the head wobbles as a pendulum
 * rotating around the neck pivot (bottom of the head image).
 */
class ThataWobbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Head-only pendulum physics
    private var headAngle = 0f
    private var headVelocity = 0f
    private val stiffness = 18f   // slower period → natural pendulum swing
    private val damping = 0.2f

    // Raw bitmaps
    private var baseRaw: Bitmap? = null
    private var headRaw: Bitmap? = null

    // Pre-scaled bitmaps (set in onSizeChanged)
    private var baseScaled: Bitmap? = null
    private var headScaled: Bitmap? = null

    // Pre-computed layout
    private var cx = 0f
    private var baseLeft = 0f; private var baseTop = 0f
    private var headLeft = 0f; private var headTop = 0f
    private var headPivotY = 0f   // top of head = pendulum pivot (chin sweeps the arc)
    private val shadowOval = RectF()

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#50000000")
    }

    private var animating = false
    private var lastFrameNanos = 0L

    var onTouched: (() -> Unit)? = null

    init {
        baseRaw = loadBitmap("thata_base")
        headRaw = loadBitmap("thata_head")
    }

    private fun loadBitmap(name: String): Bitmap? {
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) BitmapFactory.decodeResource(resources, id) else null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return
        cx = w / 2f

        // Base: scale to 72% width, sit in the lower 60% of the view
        baseScaled = fitBitmap(baseRaw, (w * 0.72f).toInt(), (h * 0.62f).toInt())
        baseScaled?.let { bmp ->
            val baseBottom = h * 0.95f
            baseTop  = baseBottom - bmp.height
            baseLeft = cx - bmp.width / 2f

            // Shadow at the floor line
            shadowOval.set(cx - w * 0.20f, baseBottom - 8f, cx + w * 0.20f, baseBottom + 8f)
        }

        // Head: scale to 38% width, sit directly on top of the body
        headScaled = fitBitmap(headRaw, (w * 0.38f).toInt(), (h * 0.40f).toInt())
        headScaled?.let { bmp ->
            headLeft   = cx - bmp.width / 2f
            headTop    = baseTop - bmp.height   // head bottom rests on body top
            // Pendulum pivot at the TOP of the head — chin sweeps the arc, like a hanging weight
            headPivotY = headTop
        }
    }

    private fun fitBitmap(src: Bitmap?, maxW: Int, maxH: Int): Bitmap? {
        src ?: return null
        val scale = minOf(maxW.toFloat() / src.width, maxH.toFloat() / src.height)
        val tw = (src.width * scale).toInt().coerceAtLeast(1)
        val th = (src.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, tw, th, true)
    }

    // ── Physics ──────────────────────────────────────────────────────────

    private fun step(dt: Float) {
        val acc = -stiffness * headAngle - damping * headVelocity
        headVelocity += acc * dt
        headAngle += headVelocity * dt
        if (headAngle > 10f)  { headAngle = 10f;  headVelocity = -headVelocity * 0.25f }
        if (headAngle < -10f) { headAngle = -10f; headVelocity = -headVelocity * 0.25f }
    }

    private fun isAtRest() = abs(headAngle) < 0.01f && abs(headVelocity) < 0.01f

    fun applyImpulse(force: Float) {
        headVelocity += force
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

    // ── Drawing ───────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Base — static, no rotation
        baseScaled?.let { canvas.drawBitmap(it, baseLeft, baseTop, null) }

        // Head — rotates around neck pivot only
        headScaled?.let { bmp ->
            canvas.save()
            canvas.rotate(headAngle, cx, headPivotY)
            canvas.drawBitmap(bmp, headLeft, headTop, null)
            canvas.restore()
        }

        canvas.drawOval(shadowOval, shadowPaint)
    }
}