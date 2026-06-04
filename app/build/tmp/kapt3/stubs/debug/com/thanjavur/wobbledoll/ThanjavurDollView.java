package com.thanjavur.wobbledoll;

/**
 * Custom View that draws the Thanjavur Thalayatti Bommai (wobble doll)
 * using image resources for head, torso, and base to allow independent rotation.
 *
 * To use images, add "doll_head", "doll_torso", and "doll_base" to your res/drawable folder.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\r\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u001b\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J \u0010\u001e\u001a\u00020\u00102\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"H\u0002J \u0010$\u001a\u00020\u00102\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"H\u0002J8\u0010%\u001a\u00020\u00102\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010&\u001a\u00020\n2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"2\u0006\u0010\'\u001a\u00020\"2\u0006\u0010(\u001a\u00020\"H\u0002J0\u0010)\u001a\u00020\u00102\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"2\u0006\u0010*\u001a\u00020\"2\u0006\u0010+\u001a\u00020\"H\u0002J \u0010,\u001a\u00020\u00102\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"H\u0002J \u0010-\u001a\u00020\u00102\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\"H\u0002J\u0012\u0010.\u001a\u0004\u0018\u00010\n2\u0006\u0010/\u001a\u000200H\u0002J\b\u00101\u001a\u00020\u0010H\u0002J\u0010\u00102\u001a\u00020\u00102\u0006\u0010\u001f\u001a\u00020 H\u0014J\u0010\u00103\u001a\u00020\b2\u0006\u00104\u001a\u000205H\u0016J\b\u00106\u001a\u00020\bH\u0016J\u0010\u00107\u001a\u00020\u00102\b\b\u0002\u00108\u001a\u00020\"R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\"\u0010\u000e\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0015\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u000e\u0010\u0019\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00069"}, d2 = {"Lcom/thanjavur/wobbledoll/ThanjavurDollView;", "Landroid/view/View;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "animating", "", "baseBitmap", "Landroid/graphics/Bitmap;", "glowPaint", "Landroid/graphics/Paint;", "headBitmap", "onTouched", "Lkotlin/Function0;", "", "getOnTouched", "()Lkotlin/jvm/functions/Function0;", "setOnTouched", "(Lkotlin/jvm/functions/Function0;)V", "physics", "Lcom/thanjavur/wobbledoll/WobblePhysics;", "getPhysics", "()Lcom/thanjavur/wobbledoll/WobblePhysics;", "placeholderPaint", "shadowPaint", "torsoBitmap", "updateRunnable", "Ljava/lang/Runnable;", "drawBase", "canvas", "Landroid/graphics/Canvas;", "cx", "", "cy", "drawHead", "drawPart", "bitmap", "targetWidth", "pivotFractionY", "drawPlaceholder", "w", "h", "drawShadow", "drawTorso", "loadBitmap", "name", "", "loadResources", "onDraw", "onTouchEvent", "event", "Landroid/view/MotionEvent;", "performClick", "startWobble", "impulse", "app_debug"})
public final class ThanjavurDollView extends android.view.View {
    @org.jetbrains.annotations.NotNull()
    private final com.thanjavur.wobbledoll.WobblePhysics physics = null;
    private boolean animating = false;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap headBitmap;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap torsoBitmap;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap baseBitmap;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint shadowPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint glowPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint placeholderPaint = null;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function0<kotlin.Unit> onTouched;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable updateRunnable = null;
    
    @kotlin.jvm.JvmOverloads()
    public ThanjavurDollView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.thanjavur.wobbledoll.WobblePhysics getPhysics() {
        return null;
    }
    
    private final void loadResources() {
    }
    
    private final android.graphics.Bitmap loadBitmap(java.lang.String name) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnTouched() {
        return null;
    }
    
    public final void setOnTouched(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> p0) {
    }
    
    public final void startWobble(float impulse) {
    }
    
    @java.lang.Override()
    public boolean onTouchEvent(@org.jetbrains.annotations.NotNull()
    android.view.MotionEvent event) {
        return false;
    }
    
    @java.lang.Override()
    public boolean performClick() {
        return false;
    }
    
    @java.lang.Override()
    protected void onDraw(@org.jetbrains.annotations.NotNull()
    android.graphics.Canvas canvas) {
    }
    
    private final void drawBase(android.graphics.Canvas canvas, float cx, float cy) {
    }
    
    private final void drawTorso(android.graphics.Canvas canvas, float cx, float cy) {
    }
    
    private final void drawHead(android.graphics.Canvas canvas, float cx, float cy) {
    }
    
    /**
     * Draws a bitmap centered horizontally on cx, and anchored vertically based on pivotFractionY.
     * pivotFractionY: 0.0 = top of image is at cy, 1.0 = bottom of image is at cy.
     */
    private final void drawPart(android.graphics.Canvas canvas, android.graphics.Bitmap bitmap, float cx, float cy, float targetWidth, float pivotFractionY) {
    }
    
    private final void drawPlaceholder(android.graphics.Canvas canvas, float cx, float cy, float w, float h) {
    }
    
    private final void drawShadow(android.graphics.Canvas canvas, float cx, float cy) {
    }
    
    @kotlin.jvm.JvmOverloads()
    public ThanjavurDollView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
}