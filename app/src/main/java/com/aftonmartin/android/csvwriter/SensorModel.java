package com.aftonmartin.android.csvwriter;

import java.util.ArrayList;

public class SensorModel {
    private ArrayList<Float>[] positionData = new ArrayList[3];
    private ArrayList<Long> time = new ArrayList<Long>();

    public SensorModel(){
        positionData[0] = new ArrayList<Float>();
        positionData[1] = new ArrayList<Float>();
        positionData[2] = new ArrayList<Float>();
    }

    public void pushX(float newData){
        positionData[0].add(newData);
    }
    public void pushY(float newData){
        positionData[1].add(newData);
    }
    public void pushZ(float newData){
        positionData[2].add(newData);
    }
    public void pushTime(long newTime){
        time.add(newTime);
    }
    public void pushAll(float x, float y, float z, long newTime){
        positionData[0].add(x);
        positionData[1].add(y);
        positionData[2].add(z);
        time.add(newTime);

    }

    public int getMin(){
        if(time.size() != 0) {
            return Math.min(positionData[0].size(), Math.min(positionData[1].size(), Math.min(positionData[2].size(), time.size())));
        } else{
            return Math.min(positionData[0].size(), Math.min(positionData[1].size(), positionData[2].size()));
        }
    }
    public ArrayList<Float>[] getSensorData(){
        return positionData;
    }
    public ArrayList<Long> getTimeData(){
        return time;
    }
}
