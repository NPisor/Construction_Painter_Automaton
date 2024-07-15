package com.example.line_painting_robot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SensorDataModelAdapter extends RecyclerView.Adapter<SensorDataModelAdapter.ViewHolder>{

    private List<SensorDataModel> sensorDataList;

    public SensorDataModelAdapter(List<SensorDataModel> sensorDataList) {
        this.sensorDataList = sensorDataList;
    }

    @NonNull
    @Override
    public SensorDataModelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sensor_data_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorDataModelAdapter.ViewHolder holder, int position) {
        SensorDataModel sensorData = sensorDataList.get(position);
        holder.timestampTextView.setText(String.valueOf(sensorData.getTimestamp()));
        holder.sensorTypeTextView.setText(sensorData.getSensorType());
        holder.valuesTextView.setText(String.format("x: %f, y: %f, z: %f", sensorData.getX(), sensorData.getY(), sensorData.getZ()));
    }

    @Override
    public int getItemCount() {
        return sensorDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timestampTextView;
        public TextView sensorTypeTextView;
        public TextView valuesTextView;

        public ViewHolder(View view) {
            super(view);
            timestampTextView = view.findViewById(R.id.timestampTextView);
            sensorTypeTextView = view.findViewById(R.id.sensorTypeTextView);
            valuesTextView = view.findViewById(R.id.valuesTextView);
        }
    }
}
