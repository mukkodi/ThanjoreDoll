package com.thanjavur.wobbledoll

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Custom View that draws the Thanjavur Thalayatti Bommai (wobble doll)
 * using image resources for head, torso, and base to allow independent rotation.
 * 
 * To use images, add "doll_head", "doll_torso", and "doll_base" to your res/drawable folder.
 */
class ThanjavurDollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ── Physics ──────────────────────────────────────────────────────────
    val physics = WobblePhysics()
    private var animating = false

    // ── Resources ────────────────────────────────────────────────────────
    private var headBitmap: Bitmap? = null
    private var torsoBitmap: Bitmap? = null
    private var baseBitmap: Bitmap? = null

    init {
        loadResources()
    }

    private fun loadResources() {
        // Using getIdentifier so the project still compiles even if images are missing
        headBitmap = loadBitmap("doll_head")
        torsoBitmap = loadBitmap("doll_torso")
        baseBitmap = loadBitmap("doll_base")
    }

    private fun loadBitmap(name: String): Bitmap? {
        val id = resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) BitmapFactory.decodeResource(resources, id) else null
    }

    // ── Paints (Background & Shadow) ─────────────────────────────────────
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40000000")
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#30FFD700")
        maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
    }
    private val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80000000")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    // ── Touch callback ───────────────────────────────────────────────────
    var onTouched: (() -> Unit)? = null

    // ── Runnable loop ────────────────────────────────────────────────────
    private val updateRunnable = object : Runnable {
        override fun run() {
            physics.update()
            invalidate()
            if (!physics.isAtRest()) {
                postDelayed(this, 16)
            } else {
                animating = false
            }
        }
    }

    fun startWobble(impulse: Float = 18f) {
        physics.applyGlobalImpulse(impulse)
        if (!animating) {
            animating = true
            post(updateRunnable)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val force = if (event.x < width / 2f) -22f else 22f
            
            // Determine which part to animate based on touch height
            val touchY = event.y
            when {
                touchY < height * 0.35f -> physics.applyHeadImpulse(force)
                touchY < height * 0.65f -> physics.applyTorsoImpulse(force)
                else -> physics.applyBaseImpulse(force)
            }

            if (!animating) {
                animating = true
                post(updateRunnable)
            }

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
        val cx = width / 2f
        val cy = height / 2f

        // Draw articulated segments
        drawBase(canvas, cx, cy)
        drawTorso(canvas, cx, cy)
        drawHead(canvas, cx, cy)

        drawShadow(canvas, cx, cy)
    }

    private fun drawBase(canvas: Canvas, cx: Float, cy: Float) {
        canvas.save()
        val pivotY = cy + height * -0.06f
        canvas.rotate(physics.base.angleX, cx, pivotY)
        val scaleY = 1.0f - (abs(physics.base.angleY) / 100f)
        canvas.scale(1.0f, scaleY, cx, pivotY)

        baseBitmap?.let {
            drawPart(canvas, it, cx, pivotY, width * 0.65f, 0.0f)
        } ?: drawPlaceholder(canvas, cx, pivotY + height * 0.05f, width * 0.5f, height * 0.03f)

        canvas.restore()
    }

    private fun drawTorso(canvas: Canvas, cx: Float, cy: Float) {
        canvas.save()
        val pivotY = cy - height * 0.28f
        canvas.rotate(physics.torso.angleX, cx, pivotY)
        val scaleY = 1.0f - (abs(physics.torso.angleY) / 100f)
        canvas.scale(1.0f, scaleY, cx, pivotY)
        
        torsoBitmap?.let {
            drawPart(canvas, it, cx, pivotY, width * 0.8f, 0.0f)
        } ?: drawPlaceholder(canvas, cx, pivotY + height * 0.15f, width * 0.4f, height * 0.25f)
        
        canvas.restore()
    }

    private fun drawHead(canvas: Canvas, cx: Float, cy: Float) {
        canvas.save()
        val pivotY = cy - height * 0.54f
        canvas.rotate(physics.head.angleX, cx, pivotY)
        val scaleY = 1.0f - (abs(physics.head.angleY) / 100f)
        canvas.scale(1.0f, scaleY, cx, pivotY)

        headBitmap?.let {
            drawPart(canvas, it, cx, pivotY, width * 0.35f, 0.0f)
        } ?: drawPlaceholder(canvas, cx, pivotY + height * 0.12f, width * 0.3f, height * 0.12f)

        canvas.restore()
    }

    /**
     * Draws a bitmap centered horizontally on cx, and anchored vertically based on pivotFractionY.
     * pivotFractionY: 0.0 = top of image is at cy, 1.0 = bottom of image is at cy.
     */
    private fun drawPart(canvas: Canvas, bitmap: Bitmap, cx: Float, cy: Float, targetWidth: Float, pivotFractionY: Float) {
        val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
        val targetHeight = targetWidth * aspectRatio
        val left = cx - targetWidth / 2f
        val top = cy - targetHeight * pivotFractionY
        val dest = RectF(left, top, left + targetWidth, top + targetHeight)
        canvas.drawBitmap(bitmap, null, dest, null)
    }

    private fun drawPlaceholder(canvas: Canvas, cx: Float, cy: Float, w: Float, h: Float) {
        val rect = RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
        canvas.drawOval(rect, placeholderPaint)
    }

    private fun drawShadow(canvas: Canvas, cx: Float, cy: Float) {
        val shadowY = cy + height * 0.41f
        canvas.drawOval(cx - width * 0.28f, shadowY - 12f, cx + width * 0.28f, shadowY + 12f, shadowPaint)
    }
}
