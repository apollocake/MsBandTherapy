package com.aftonmartin.android.csvwriter;


import java.util.ArrayList;

public class Algorithm {
    private Algorithm() {
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

//    public long getDelay(){
//        if(oldTime == 0){
//            oldTime = event.getTimestamp();
//        }else{
//            newTime = event.getTimestamp();
//            delay = newTime - oldTime;
//            oldTime = newTime;
//        }
//    }


}
