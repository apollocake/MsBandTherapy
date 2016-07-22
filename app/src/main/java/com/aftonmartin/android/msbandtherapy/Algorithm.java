package com.aftonmartin.android.msbandtherapy;


import java.lang.reflect.Array;
import java.util.ArrayList;

public class Algorithm {
    private Algorithm() {
    }

    public static SensorModel lowPassFilter(SensorModel rawInput){
        SensorModel processedData = new SensorModel();
        ArrayList<Float>[] rawAcceleration = rawInput.getSensorData();
        ArrayList<Float>[] processedAcceleration = processedData.getSensorData();
        //find alpha and compensate for phase/bias accumulation
        final double ALPHA = 0.2;
        int min = rawInput.getMin() - 1; // in case of interruptions slowest x,y,z wins, rest of data discarded
        float[] gravity = {0,0,0};
        for (int i = 1; i < min; i++) {
            gravity[0] = (float) (ALPHA * gravity[0] + (1-ALPHA) * rawAcceleration[0].get(i));
            processedAcceleration[0].add(gravity[0]);
            gravity[1] = (float) (ALPHA * gravity[1] + (1-ALPHA) * rawAcceleration[1].get(i));
            processedAcceleration[1].add(gravity[1]);
            gravity[2] = (float) (ALPHA * gravity[2] + (1-ALPHA) * rawAcceleration[2].get(i));
            processedAcceleration[2].add(gravity[2]);
        }

        return processedData;
    }

    public static SensorModel subtractGravity(SensorModel rawInput) { //data is a container-model for positions
        SensorModel processedData = new SensorModel();
        ArrayList<Float>[] rawAcceleration = rawInput.getSensorData();
        ArrayList<Float>[] processedAcceleration = processedData.getSensorData();
        int min = rawInput.getMin() - 1; // in case of interruptions slowest x,y,z wins, rest of data discarded
        for (int i = 0; i < min; i++) {
            processedAcceleration[0].add(rawAcceleration[0].get(i + 1) - rawAcceleration[0].get(i));
            processedAcceleration[1].add(rawAcceleration[1].get(i + 1) - rawAcceleration[1].get(i));
            processedAcceleration[2].add(rawAcceleration[2].get(i + 1) - rawAcceleration[2].get(i));
        }
        return processedData;
    }

    public static SensorModel getVelocity(SensorModel accelInput){
        SensorModel calculatedVelocity = new SensorModel();
        ArrayList<Float>[] accelData = accelInput.getSensorData();
        ArrayList<Float>[] processedVelocities = calculatedVelocity.getSensorData();
        int min = accelInput.getMin(); // in case of interruptions slowest x,y,z wins, rest of data discarded
        float velocityX = 0;
        float velocityY = 0;
        float velocityZ = 0;

        for (int i = 0; i < min; i++) {
            velocityX = velocityX + accelData[0].get(i);
            processedVelocities[0].add(velocityX);
            velocityY = velocityY + accelData[1].get(i);
            processedVelocities[1].add(velocityY);
            velocityZ = velocityZ + accelData[2].get(i);
            processedVelocities[2].add(velocityZ);
        }
        return calculatedVelocity;
    }

    public static SensorModel getPosition(SensorModel velocityInput){
        SensorModel calculatedPosition = new SensorModel();
        ArrayList<Float>[] velocityData = velocityInput.getSensorData();
        ArrayList<Float>[] processedPositions = calculatedPosition.getSensorData();
        int min = velocityInput.getMin(); // in case of interruptions slowest x,y,z wins, rest of data discarded
        float positionX = 0;
        float positionY = 0;
        float positionZ = 0;

        for (int i = 0; i < min; i++) {
            positionX = positionX + velocityData[0].get(i);
            processedPositions[0].add(positionX);
            positionY = positionY + velocityData[1].get(i);
            processedPositions[1].add(positionY);
            positionZ = positionZ + velocityData[2].get(i);
            processedPositions[2].add(positionZ);
        }
        return calculatedPosition;
    }

    public ArrayList<Long> getTimeDelays(SensorModel sensorModel){
        ArrayList<Long> processedDelays = new ArrayList<Long>();
        for (int i = 0; i < processedDelays.size() - 1; i++) {
            processedDelays.add(sensorModel.getTimeData().get(i+1) - sensorModel.getTimeData().get(i));
        }
        return processedDelays;
    }


}
