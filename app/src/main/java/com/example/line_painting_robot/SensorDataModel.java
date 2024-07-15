package com.example.line_painting_robot;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SensorDataModel implements Parcelable {
    private long timestamp;
    private String sensorType;
    private float x;
    private float y;
    private float z;

    public SensorDataModel(long timestamp, String sensorType, float x, float y, float z) {
        this.timestamp = timestamp;
        this.sensorType = sensorType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected SensorDataModel(Parcel in) {
        timestamp = in.readLong();
        sensorType = in.readString();
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
    }

    public static final Creator<SensorDataModel> CREATOR = new Creator<SensorDataModel>() {
        @Override
        public SensorDataModel createFromParcel(Parcel in) {
            return new SensorDataModel(in);
        }

        @Override
        public SensorDataModel[] newArray(int size) {
            return new SensorDataModel[size];
        }
    };

    public long getTimestamp() { return timestamp; }
    public String getSensorType() { return sensorType; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeString(sensorType);
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeFloat(z);
    }
}
