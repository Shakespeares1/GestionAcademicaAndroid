package com.gestionacademica.android.presentation.sensores;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.gestionacademica.android.R;
import java.util.Locale;

public final class SensoresActivity extends AppCompatActivity implements SensorEventListener {
    private static final long UI_UPDATE_INTERVAL_MS = 120L;
    private static final float MOVEMENT_THRESHOLD = 1.2f;
    private static final float LEVEL_TOLERANCE_DEGREES = 8f;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximity;

    private TextView accelerometerStatus;
    private TextView accelerationValues;
    private TextView movementStatus;
    private TextView tiltStatus;
    private TextView proximityStatus;
    private TextView proximityDistance;
    private TextView proximityState;
    private TextView proximityRange;

    private long lastAccelerometerUiUpdate;
    private long lastProximityUiUpdate;
    private boolean hasPreviousAcceleration;
    private float previousX;
    private float previousY;
    private float previousZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensores);

        accelerometerStatus = findViewById(R.id.accelerometerAvailability);
        accelerationValues = findViewById(R.id.accelerometerValues);
        movementStatus = findViewById(R.id.accelerometerMovement);
        tiltStatus = findViewById(R.id.accelerometerTilt);
        proximityStatus = findViewById(R.id.proximityAvailability);
        proximityDistance = findViewById(R.id.proximityDistance);
        proximityState = findViewById(R.id.proximityState);
        proximityRange = findViewById(R.id.proximityRange);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        showSensorAvailability();

        findViewById(R.id.sensorsBackButton).setOnClickListener(v -> finish());
    }

    private void showSensorAvailability() {
        if (accelerometer == null) {
            accelerometerStatus.setText(R.string.sensor_unavailable);
            accelerationValues.setText(R.string.sensor_no_reading);
            movementStatus.setText(R.string.sensor_no_reading);
            tiltStatus.setText(R.string.sensor_no_reading);
        } else {
            accelerometerStatus.setText(getString(R.string.sensor_available_with_resolution,
                    accelerometer.getResolution()));
            accelerationValues.setText(R.string.sensor_waiting);
            movementStatus.setText(R.string.sensor_waiting);
            tiltStatus.setText(R.string.sensor_waiting);
        }

        if (proximity == null) {
            proximityStatus.setText(R.string.sensor_unavailable);
            proximityDistance.setText(R.string.sensor_no_reading);
            proximityState.setText(R.string.sensor_no_reading);
            proximityRange.setText(R.string.proximity_range_unavailable);
        } else {
            proximityStatus.setText(R.string.sensor_available);
            proximityDistance.setText(R.string.sensor_waiting);
            proximityState.setText(R.string.sensor_waiting);
            proximityRange.setText(getString(R.string.proximity_range_value,
                    proximity.getMaximumRange()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager == null) return;
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (proximity != null) {
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        hasPreviousAcceleration = false;
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.values == null) return;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && event.values.length >= 3) {
            updateAccelerometer(event.values, SystemClock.elapsedRealtime());
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY && event.values.length >= 1) {
            updateProximity(event.values[0], SystemClock.elapsedRealtime());
        }
    }

    private void updateAccelerometer(float[] values, long now) {
        if (now - lastAccelerometerUiUpdate < UI_UPDATE_INTERVAL_MS) return;
        lastAccelerometerUiUpdate = now;

        float x = values[0];
        float y = values[1];
        float z = values[2];
        accelerationValues.setText(getString(R.string.accelerometer_xyz,
                format(x), format(y), format(z)));

        if (hasPreviousAcceleration) {
            float dx = x - previousX;
            float dy = y - previousY;
            float dz = z - previousZ;
            float delta = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            movementStatus.setText(delta >= MOVEMENT_THRESHOLD
                    ? R.string.accelerometer_moving : R.string.accelerometer_still);
        }
        previousX = x;
        previousY = y;
        previousZ = z;
        hasPreviousAcceleration = true;

        double pitch = Math.toDegrees(Math.atan2(-x, Math.sqrt(y * y + z * z)));
        double roll = Math.toDegrees(Math.atan2(y, z));
        double dominantTilt = Math.max(Math.abs(pitch), Math.abs(roll));
        int tiltMessage;
        if (dominantTilt < LEVEL_TOLERANCE_DEGREES) {
            tiltMessage = R.string.accelerometer_level;
        } else if (Math.abs(pitch) >= Math.abs(roll)) {
            tiltMessage = R.string.accelerometer_tilt_x;
        } else {
            tiltMessage = R.string.accelerometer_tilt_y;
        }
        tiltStatus.setText(getString(R.string.accelerometer_tilt_value,
                getString(tiltMessage), format((float) pitch), format((float) roll)));
    }

    private void updateProximity(float distance, long now) {
        if (now - lastProximityUiUpdate < UI_UPDATE_INTERVAL_MS) return;
        lastProximityUiUpdate = now;
        proximityDistance.setText(getString(R.string.proximity_distance_value, format(distance)));
        boolean near = distance < proximity.getMaximumRange();
        proximityState.setText(near ? R.string.proximity_near : R.string.proximity_far);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // La precisión puede variar; los valores recibidos siguen siendo lecturas del sensor real.
    }

    private static String format(float value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }
}