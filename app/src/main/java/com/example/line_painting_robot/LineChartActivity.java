package com.example.line_painting_robot;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private List<Entry> entries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);
        lineChart = findViewById(R.id.lineChart);
        setupChart();

        // Retrieve the sensor data list from the intent
        List<SensorDataModel> sensorDataList = getIntent().getParcelableArrayListExtra("sensorDataList");
        if (sensorDataList != null) {
            plotSensorData(sensorDataList);
        }
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-10f); // Set minimum value for X axis
        xAxis.setAxisMaximum(10f); // Set maximum value for X axis

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(-10f); // Set minimum value for Y axis
        leftAxis.setAxisMaximum(10f); // Set maximum value for Y axis

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.setData(new LineData());
    }

    private void plotSensorData(List<SensorDataModel> sensorDataList) {
        // Normalize the data
        List<Entry> normalizedEntries = normalizeData(sensorDataList);

        // Apply moving average
        List<Entry> smoothedEntries = applyMovingAverage(normalizedEntries, 5);

        LineDataSet dataSet = new LineDataSet(smoothedEntries, "Path");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart
    }

    private List<Entry> applyMovingAverage(List<Entry> data, int windowSize) {
        List<Entry> smoothedEntries = new ArrayList<>();
        float[] yWindow = new float[windowSize];
        int windowIndex = 0;
        int dataIndex = 0;

        for (Entry point : data) {
            yWindow[windowIndex] = point.getY();
            windowIndex = (windowIndex + 1) % windowSize;

            if (dataIndex >= windowSize) {
                float avgY = 0;
                for (int i = 0; i < windowSize; i++) {
                    avgY += yWindow[i];
                }
                avgY /= windowSize;

                smoothedEntries.add(new Entry(point.getX(), avgY));
            }

            dataIndex++;
        }

        return smoothedEntries;
    }

    private List<Entry> normalizeData(List<SensorDataModel> data) {
        List<Entry> normalizedEntries = new ArrayList<>();
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float maxZ = Float.MIN_VALUE;

        // Find the maximum values for normalization
        for (SensorDataModel point : data) {
            if (point.getX() > maxX) maxX = point.getX();
            if (point.getY() > maxY) maxY = point.getY();
            if (point.getZ() > maxZ) maxZ = point.getZ();
        }

        // Normalize the data
        for (SensorDataModel point : data) {
            float normalizedX = point.getX() / maxX;
            float normalizedY = point.getY() / maxY;
            float normalizedZ = point.getZ() / maxZ;
            normalizedEntries.add(new Entry(normalizedX, normalizedY)); // Change as needed for different axes
        }

        return normalizedEntries;
    }
}
