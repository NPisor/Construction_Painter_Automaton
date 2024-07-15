package com.example.line_painting_robot;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class KalmanFilter {
    private final int stateSize;
    private final int measurementSize;

    private RealMatrix stateVector;

    public int getStateSize() {
        return stateSize;
    }

    public int getMeasurementSize() {
        return measurementSize;
    }

    public RealMatrix getStateVector() {
        return stateVector;
    }

    public void setStateVector(RealMatrix stateVector) {
        this.stateVector = stateVector;
    }

    public RealMatrix getStateCovariance() {
        return stateCovariance;
    }

    public void setStateCovariance(RealMatrix stateCovariance) {
        this.stateCovariance = stateCovariance;
    }

    public RealMatrix getStateTransition() {
        return stateTransition;
    }

    public void setStateTransition(RealMatrix stateTransition) {
        this.stateTransition = stateTransition;
    }

    public RealMatrix getControlInput() {
        return controlInput;
    }

    public void setControlInput(RealMatrix controlInput) {
        this.controlInput = controlInput;
    }

    public RealMatrix getProcessNoise() {
        return processNoise;
    }

    public void setProcessNoise(RealMatrix processNoise) {
        this.processNoise = processNoise;
    }

    public RealMatrix getMeasurementMatrix() {
        return measurementMatrix;
    }

    public void setMeasurementMatrix(RealMatrix measurementMatrix) {
        this.measurementMatrix = measurementMatrix;
    }

    public RealMatrix getMeasurementNoise() {
        return measurementNoise;
    }

    public void setMeasurementNoise(RealMatrix measurementNoise) {
        this.measurementNoise = measurementNoise;
    }

    private RealMatrix stateCovariance;
    private RealMatrix stateTransition;
    private RealMatrix controlInput;
    private RealMatrix processNoise;
    private RealMatrix measurementMatrix;
    private RealMatrix measurementNoise;

    public KalmanFilter(int stateSize, int measurementSize, int controlSize) {
        this.stateSize = stateSize;
        this.measurementSize = measurementSize;

        stateVector = new Array2DRowRealMatrix(stateSize, 1);
        stateCovariance = MatrixUtils.createRealIdentityMatrix(stateSize);
        stateTransition = MatrixUtils.createRealIdentityMatrix(stateSize);
        controlInput = new Array2DRowRealMatrix(stateSize, 1);
        processNoise = MatrixUtils.createRealIdentityMatrix(stateSize);
        measurementMatrix = new Array2DRowRealMatrix(measurementSize, stateSize);
        measurementNoise = MatrixUtils.createRealIdentityMatrix(measurementSize);
    }

    public void predict(RealMatrix controlVector) {
        stateVector = stateTransition.multiply(stateVector).add(controlInput.multiply(controlVector));
        stateCovariance = stateTransition.multiply(stateCovariance).multiply(stateTransition.transpose()).add(processNoise);
    }

    public void update(RealMatrix measurement) {
        RealMatrix y = measurement.subtract(measurementMatrix.multiply(stateVector));
        RealMatrix S = measurementMatrix.multiply(stateCovariance).multiply(measurementMatrix.transpose()).add(measurementNoise);
        RealMatrix K = stateCovariance.multiply(measurementMatrix.transpose()).multiply(MatrixUtils.inverse(S));
        stateVector = stateVector.add(K.multiply(y));
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(stateSize);
        stateCovariance = (I.subtract(K.multiply(measurementMatrix))).multiply(stateCovariance);
    }

    public RealMatrix getState() {
        return stateVector;
    }
}
