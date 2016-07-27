package com.aftonmartin.android.msbandtherapy;


import java.lang.reflect.Array;
import java.util.ArrayList;

public class Algorithm {
    private static Algorithm.MOVEMENT_STATE currentState = MOVEMENT_STATE.IDLE;
    final static double OMEGA_LIMIT = 30; //threshold for peaks in velocity
    final static double MOVEMENT_BEGIN_LIMIT = 75; //threshold for minimal movement that should count
    final static int SUM_QUEUE_LIMIT = 20; //increase if waves are fat enough and sampling rate is high enough
    final static double OMEGA_SUM_LIMIT = OMEGA_LIMIT * SUM_QUEUE_LIMIT;
    private static int startCounter = 0;
    private static double sumFloats = 0;
    static LimitedQueue<Float>sumQueue = new LimitedQueue<Float>(SUM_QUEUE_LIMIT); //live queue for threshold summing
    private Algorithm() {}

    public static SensorModel lowPassFilter(SensorModel rawInput){
        SensorModel processedData = new SensorModel();
        ArrayList<Float>[] rawSignal = rawInput.getSensorData();
        ArrayList<Float>[] processedAcceleration = processedData.getSensorData();
        //find alpha and compensate for phase/bias accumulation
        final double ALPHA = 0.8;
        int min = rawInput.getMin() - 1; // in case of interruptions slowest x,y,z wins, rest of data discarded
        float[] lowPassValue = {0,0,0};
        for (int i = 1; i < min; i++) {
            lowPassValue[0] = (float) (ALPHA * lowPassValue[0] + (1-ALPHA) * rawSignal[0].get(i));
            processedAcceleration[0].add(lowPassValue[0]);
            lowPassValue[1] = (float) (ALPHA * lowPassValue[1] + (1-ALPHA) * rawSignal[1].get(i));
            processedAcceleration[1].add(lowPassValue[1]);
            lowPassValue[2] = (float) (ALPHA * lowPassValue[2] + (1-ALPHA) * rawSignal[2].get(i));
            processedAcceleration[2].add(lowPassValue[2]);



            FileUtils.getInstance().getLowPassWriter().println(String.format("%.6f,%.6f,%.6f", lowPassValue[0], lowPassValue[1], lowPassValue[2]));

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
            FileUtils.getInstance().getSubtractGravWriter().println(String.format("%.6f,%.6f,%.6f", rawAcceleration[0].get(i + 1) - rawAcceleration[0].get(i),
                    rawAcceleration[1].get(i + 1) - rawAcceleration[1].get(i), rawAcceleration[2].get(i + 1) - rawAcceleration[2].get(i)));

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
        ArrayList<Long> timeData = velocityInput.getTimeData();
        ArrayList<Float>[] processedPositions = calculatedPosition.getSensorData();
        int min = velocityInput.getMin(); // in case of interruptions slowest x,y,z wins, rest of data discarded
        float positionX = 0;
        float positionY = 0;
        float positionZ = 0;
        long timeDelay = 0;
        float currentPosition = 0;


        for (int i = 1; i < min ; i++) {
            timeDelay = timeData.get(i) - timeData.get(i-1);
            //throw away data if no delay between last event
            if(timeDelay != 0){
                positionX +=  0.001f * (((velocityData[0].get(i-1)+ velocityData[0].get(i)) * timeDelay) / 2);
                processedPositions[0].add(positionX);
                positionY += 0.001f * (((velocityData[1].get(i-1)+ velocityData[1].get(i)) * timeDelay) / 2);
                processedPositions[1].add(positionY);
                positionZ += 0.001f * (((velocityData[2].get(i-1)+ velocityData[2].get(i)) * timeDelay) / 2);
                processedPositions[2].add(positionZ);
                FileUtils.getInstance().getPositionWriter().println(String.format("%.6f,%.6f,%.6f", positionX, positionY, positionZ));

            }
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


        public enum MOVEMENT_STATE {
            IDLE, DETECT_INCREASE, FIND_MIN, AT_MIN, FIND_MAX, AT_MAX
        }

        public static Algorithm.MOVEMENT_STATE detectStatus(float pitchVelocity){
            sumQueue.add(pitchVelocity);
            sumFloats = sumFloats();
            switch(currentState){
                case IDLE:
                    UIAsyncUtils.getInstance().appendToUI(R.id.angleState, "Idle");
                    if(Math.abs(pitchVelocity) > MOVEMENT_BEGIN_LIMIT) { //must happen four consecutive times or reset
                        startCounter += 1;
                        if(startCounter > 4){
                            currentState = MOVEMENT_STATE.FIND_MIN;
                            startCounter = 0;
                        }
                    } else {
                        startCounter = 0;
                    }
                    break;

                case FIND_MIN:
                    UIAsyncUtils.getInstance().appendToUI(R.id.angleState, "FIND_MIN");
                    if(pitchVelocity < -OMEGA_LIMIT){
                        if(sumFloats < -OMEGA_SUM_LIMIT) {
                            currentState = MOVEMENT_STATE.AT_MIN;
                            //forwardPeak = j; better for logging later
                        }
                    }
                    break;
                case AT_MIN:
                    UIAsyncUtils.getInstance().appendToUI(R.id.angleState, "AT_MIN");
                    currentState = MOVEMENT_STATE.FIND_MAX;
                    break;
                case FIND_MAX:
                    UIAsyncUtils.getInstance().appendToUI(R.id.angleState, "FIND_MAX");
                    if((pitchVelocity > OMEGA_LIMIT) && sumFloats > OMEGA_SUM_LIMIT){
                        currentState = MOVEMENT_STATE.AT_MAX;
                        //backPeak = j; better for logging- implement in caller
                        //motionCounter += 1 "..."
                        //log forward_back((i-1)*20+motion_counter,:)=[i,motion_counter,forward_peak,back_peak];
                    }
                    break;
                case AT_MAX:
                    UIAsyncUtils.getInstance().appendToUI(R.id.angleState, "AT_MAX");
                    currentState = MOVEMENT_STATE.DETECT_INCREASE;
                    break;
                case DETECT_INCREASE:
                    UIAsyncUtils.getInstance().appendToUI(R.id.angleState, "DETECT_INCREASE");
                    if (Math.abs(pitchVelocity) < MOVEMENT_BEGIN_LIMIT){
                        currentState = MOVEMENT_STATE.IDLE;
                    }
                    break;
                default:
                    UIAsyncUtils.getInstance().appendToUI(R.id.angleState, "default error");
                    break;


            }
            return  currentState;
        }

        private static float sumFloats(){
            float sum = 0;
            for(int i=0; i < sumQueue.size(); i++){
                sum += sumQueue.get(i);
            }
            return sum;
        }


}
