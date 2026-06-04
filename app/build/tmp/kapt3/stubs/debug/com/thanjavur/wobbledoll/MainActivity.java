package com.thanjavur.wobbledoll;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0014J\b\u0010\u0013\u001a\u00020\u0010H\u0014J\b\u0010\u0014\u001a\u00020\u0010H\u0014R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0007\u001a\u00020\b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/thanjavur/wobbledoll/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "accelerometer", "Landroid/hardware/Sensor;", "dollView", "Lcom/thanjavur/wobbledoll/ThanjavurDollView;", "sensorManager", "Landroid/hardware/SensorManager;", "getSensorManager", "()Landroid/hardware/SensorManager;", "setSensorManager", "(Landroid/hardware/SensorManager;)V", "shakeDetector", "Lcom/thanjavur/wobbledoll/ShakeDetector;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onPause", "onResume", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.thanjavur.wobbledoll.ThanjavurDollView dollView;
    @javax.inject.Inject()
    public android.hardware.SensorManager sensorManager;
    private com.thanjavur.wobbledoll.ShakeDetector shakeDetector;
    @org.jetbrains.annotations.Nullable()
    private android.hardware.Sensor accelerometer;
    
    public MainActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.hardware.SensorManager getSensorManager() {
        return null;
    }
    
    public final void setSensorManager(@org.jetbrains.annotations.NotNull()
    android.hardware.SensorManager p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    @java.lang.Override()
    protected void onPause() {
    }
}