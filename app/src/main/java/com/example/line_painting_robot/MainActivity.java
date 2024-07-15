package com.example.line_painting_robot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button sensorDataButton, stopGatheringDataButton;
    private TextView headerText;

    private static final float ALPHA = 0.98f; // Complementary filter constant
    private float[] gravity = new float[3]; // Accelerometer data after gravity removal
    private float[] linearAcceleration = new float[3]; // Linear acceleration data
    private float[] gyroscopeData = new float[3]; // Gyroscope data
    private float[] orientation = new float[3]; // Orientation (pitch, roll, yaw)
    private long lastTimestamp = 0; // Last sensor event timestamp
    private SensorEventListener sensorEventListener;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;

    private OutputStream outputStream;

    private SensorDataModelAdapter sensorDataAdapter;

    private RecyclerView activeSensorData;

    private List<SensorDataModel> sensorDataList;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorDataList = new ArrayList<>();
        sensorDataAdapter = new SensorDataModelAdapter(sensorDataList);
        activeSensorData = findViewById(R.id.activeSensorData);
        activeSensorData.setLayoutManager(new LinearLayoutManager(this));
        activeSensorData.setAdapter(sensorDataAdapter);
        sensorDataButton = findViewById(R.id.gatherSensorBtn);
        stopGatheringDataButton = findViewById(R.id.stopGatheringDataBtn);
        headerText = findViewById(R.id.headerText);
        setupSensors();
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (isRecording) {
                    recordSensorData(event);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        stopGatheringDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });
    }

    private void setupSensors() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        Toast.makeText(this, "Accelerometer: " + accelerometer.getName() + "Gyroscope: " + gyroscope.getName() + " set up.", Toast.LENGTH_SHORT).show();

    }

    private void startRecording() {
        if (isRecording) return;

        isRecording = true;
        headerText.setText("Status: Recording...");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "sensor_data.txt");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SensorData");

        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);

        try {
            outputStream = getContentResolver().openOutputStream(uri);
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (!isRecording) return;

        isRecording = false;
        headerText.setText("Status: Stopped");
        sensorManager.unregisterListener(sensorEventListener);

        try {
            outputStream.close();

            // Start LineChartActivity and pass the sensor data list
            Intent intent = new Intent(this, LineChartActivity.class);
            intent.putParcelableArrayListExtra("sensorDataList", (ArrayList<? extends Parcelable>) sensorDataList);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordSensorData(SensorEvent event) {
        long timestamp = event.timestamp;
        String sensorType = event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ? "ACC" : "GYR";

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            processAccelerometerData(event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            processGyroscopeData(event);
        }

        SensorDataModel sensorData = new SensorDataModel(timestamp, sensorType, (float) Math.toDegrees(orientation[0]), (float) Math.toDegrees(orientation[1]), (float) Math.toDegrees(orientation[2]));
        sensorDataList.add(sensorData);
        sensorDataAdapter.notifyItemInserted(sensorDataList.size() - 1);
        activeSensorData.smoothScrollToPosition(sensorDataList.size() - 1);

        String data = String.format("%s, %d, %f, %f, %f\n", sensorType, timestamp, Math.toDegrees(orientation[0]), Math.toDegrees(orientation[1]), Math.toDegrees(orientation[2]));

        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processAccelerometerData(SensorEvent event) {
        final float alpha = 0.8f;

        // Isolate the force of gravity with a low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with a high-pass filter.
        linearAcceleration[0] = event.values[0] - gravity[0];
        linearAcceleration[1] = event.values[1] - gravity[1];
        linearAcceleration[2] = event.values[2] - gravity[2];
    }

    private void processGyroscopeData(SensorEvent event) {
        if (lastTimestamp != 0) {
            final float dT = (event.timestamp - lastTimestamp) * 1.0f / 1000000000.0f; // Convert nanoseconds to seconds

            // Integrate the gyroscope data -> angle change
            orientation[0] += event.values[0] * dT;
            orientation[1] += event.values[1] * dT;
            orientation[2] += event.values[2] * dT;
        }

        lastTimestamp = event.timestamp;

        // Apply complementary filter to combine accelerometer and gyroscope data
        float pitch = (float) Math.atan2(linearAcceleration[1], linearAcceleration[2]);
        float roll = (float) Math.atan2(linearAcceleration[0], linearAcceleration[2]);

        orientation[0] = ALPHA * orientation[0] + (1 - ALPHA) * pitch;
        orientation[1] = ALPHA * orientation[1] + (1 - ALPHA) * roll;

        Log.d("Orientation", "Pitch: " + Math.toDegrees(orientation[0]) + ", Roll: " + Math.toDegrees(orientation[1]) + ", Yaw: " + Math.toDegrees(orientation[2]));
    }
}