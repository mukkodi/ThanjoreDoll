package com.thanjavur.wobbledoll

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class ThanjavurDollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    @Inject lateinit var physics: WobblePhysics
    private var animating = false
    private var lastFrameTimeNanos: Long = 0

    // Raw bitmaps decoded once from resources
    private var headBitmap: Bitmap? = null
    private var torsoBitmap: Bitmap? = null
    private var baseBitmap: Bitmap? = null

    // Bitmaps pre-scaled to display size in onSizeChanged — no per-frame scaling
    private var headScaled: Bitmap? = null
    private var torsoScaled: Bitmap? = null
    private var baseScaled: Bitmap? = null

    // Layout geometry pre-computed in onSizeChanged — no per-frame allocation
    private var cx = 0f
    private var headPivotY = 0f;  private var headLeft = 0f;  private var headTop = 0f
    private var torsoPivotY = 0f; private var torsoLeft = 0f; private var torsoTop = 0f
    private var basePivotY = 0f;  private var baseLeft = 0f;  private var baseTop = 0f
    private val shadowOval = RectF()
    private val placeholderRect = RectF()

    init {
        loadResources()
    }

    private fun loadResources() {
        headBitmap  = loadBitmap("doll_head")
        torsoBitmap = loadBitmap("doll_torso")
        baseBitmap  = loadBitmap("doll_base")
    }

    private fun loadBitmap(name: String): Bitmap? {
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) BitmapFactory.decodeResource(resources, id) else null
    }

    // BlurMaskFilter is NOT hardware-accelerated — removed to keep GPU rendering.
    // Plain semi-transparent oval gives a clean shadow without forcing software fallback.
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40000000")
    }
    private val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80000000")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    var onTouched: (() -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        cx = w / 2f
        val cy = h / 2f

        // Pre-scale bitmaps exactly once to their display dimensions
        headScaled  = scaleBitmap(headBitmap,  (w * 0.35f).toInt())
        torsoScaled = scaleBitmap(torsoBitmap, (w * 0.80f).toInt())
        baseScaled  = scaleBitmap(baseBitmap,  (w * 0.65f).toInt())

        // Pre-compute pivot and top-left positions (pivotFractionY = 0 → top of image at pivot)
        headPivotY  = cy - h * 0.54f
        torsoPivotY = cy - h * 0.28f
        basePivotY  = cy - h * 0.06f

        headScaled?.let  { headLeft  = cx - it.width / 2f; headTop  = headPivotY  }
        torsoScaled?.let { torsoLeft = cx - it.width / 2f; torsoTop = torsoPivotY }
        baseScaled?.let  { baseLeft  = cx - it.width / 2f; baseTop  = basePivotY  }

        val shadowY = cy + h * 0.41f
        shadowOval.set(cx - w * 0.28f, shadowY - 12f, cx + w * 0.28f, shadowY + 12f)
    }

    private fun scaleBitmap(src: Bitmap?, targetWidth: Int): Bitmap? {
        src ?: return null
        if (targetWidth <= 0) return src
        val targetHeight = (targetWidth * src.height.toFloat() / src.width).toInt()
        return Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (lastFrameTimeNanos == 0L) {
                lastFrameTimeNanos = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
                return
            }
            val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceAtMost(0.033f)
            lastFrameTimeNanos = frameTimeNanos
            physics.update(dt)
            invalidate()
            if (!physics.isAtRest()) {
                Choreographer.getInstance().postFrameCallback(this)
            } else {
                animating = false
                lastFrameTimeNanos = 0
            }
        }
    }

    fun startWobble(impulse: Float = 18f) {
        physics.applyGlobalImpulse(impulse)
        if (!animating) {
            animating = true
            lastFrameTimeNanos = 0
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val force = if (event.x < width / 2f) -22f else 22f
            when {
                event.y < height * 0.35f -> physics.applyHeadImpulse(force)
                event.y < height * 0.65f -> physics.applyTorsoImpulse(force)
                else                     -> physics.applyBaseImpulse(force)
            }
            if (!animating) {
                animating = true
                lastFrameTimeNanos = 0
                Choreographer.getInstance().postFrameCallback(frameCallback)
            }
            onTouched?.invoke()
            performClick()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean { super.performClick(); return true }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBase(canvas)
        drawTorso(canvas)
        drawHead(canvas)
        canvas.drawOval(shadowOval, shadowPaint)
    }

    private fun drawBase(canvas: Canvas) {
        canvas.save()
        canvas.rotate(physics.base.angleX, cx, basePivotY)
        canvas.scale(1f, 1f - abs(physics.base.angleY) / 100f, cx, basePivotY)
        baseScaled?.let { canvas.drawBitmap(it, baseLeft, baseTop, null) }
            ?: run {
                placeholderRect.set(cx - width * 0.25f, basePivotY + height * 0.02f, cx + width * 0.25f, basePivotY + height * 0.08f)
                canvas.drawOval(placeholderRect, placeholderPaint)
            }
        canvas.restore()
    }

    private fun drawTorso(canvas: Canvas) {
        canvas.save()
        canvas.rotate(physics.torso.angleX, cx, torsoPivotY)
        canvas.scale(1f, 1f - abs(physics.torso.angleY) / 100f, cx, torsoPivotY)
        torsoScaled?.let { canvas.drawBitmap(it, torsoLeft, torsoTop, null) }
            ?: run {
                placeholderRect.set(cx - width * 0.2f, torsoPivotY + height * 0.02f, cx + width * 0.2f, torsoPivotY + height * 0.28f)
                canvas.drawOval(placeholderRect, placeholderPaint)
            }
        canvas.restore()
    }

    private fun drawHead(canvas: Canvas) {
        canvas.save()
        canvas.rotate(physics.head.angleX, cx, headPivotY)
        canvas.scale(1f, 1f - abs(physics.head.angleY) / 100f, cx, headPivotY)
        headScaled?.let { canvas.drawBitmap(it, headLeft, headTop, null) }
            ?: run {
                placeholderRect.set(cx - width * 0.15f, headPivotY + height * 0.06f, cx + width * 0.15f, headPivotY + height * 0.18f)
                canvas.drawOval(placeholderRect, placeholderPaint)
            }
        canvas.restore()
    }
}
