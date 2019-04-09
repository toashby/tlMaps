package com.toashby.tlmap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;

/**
 * Created by tom on 27/09/16.
 */
public class Stations {

    boolean north;

    public boolean isNorth() {
        return north;
    }

    public void setNorth(boolean north) {
        this.north = north;
    }

    float xpos = 0f, ypos = 0f;
    float xdifference, ydifference;

    Long timeToDepart, timeToArrive, timeStep, nextDeparture, nextArrive, nextTimeStep;

    boolean canBeChecked;

    public void setcanBeChecked(boolean canBeChecked){
        this.canBeChecked = canBeChecked;
    }

    public boolean getcanBeChecked(){
        return canBeChecked;
    }

    float getXpos(){
        return xpos;
    }

    public float getYpos() {
        return ypos;
    }

    public void setXpos(float pos){
        xpos = pos;
    }

    public void setYpos(float pos) {
        ypos = pos;
    }


    public void setXdifference(float xdifference){
        this.xdifference = xdifference;
    }

    public void setYdifference(float ydifference) {
        this.ydifference = ydifference;
    }

    public float getXdifference() {
        return xdifference;
    }

    public float getYdifference() {
        return ydifference;
    }

    public Long getTimeToDepart() {
        return timeToDepart;
    }
    public Long getTimeToArrive() {
        return timeToArrive;
    }

    public void setTimeToArrive(long timeToArrive) {
        this.timeToArrive = timeToArrive;
    }

    public void setTimeToDepart(long timeToDepart) {

        this.timeToDepart = timeToDepart;
    }


    public long getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(long timeStep) {
        this.timeStep = timeStep;
    }

    //NEXT STUFF
    public void setNextArrive(Long nextArrive){
        this.nextArrive = nextArrive;
    }
    public Long getNextArrive() {
        return nextArrive;
    }

    public void setNextDeparture(Long nextDeparture){
        this.nextDeparture = nextDeparture;
    }
    public Long getNextDeparture() {
        return nextDeparture;
    }

    public Long getNextTimeStep(){
        return nextTimeStep;
    }

    public void setNextTimeStep(Long nextTimeStep) {
        this.nextTimeStep = nextTimeStep;
    }


    public void sendRequest(String method, String from, String to) {

        final Json json = new Json();

        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);


        //String requestJson = json.toJson(requestObject); // this is just an example
        //System.out.println("" + requestJson);

        Net.HttpRequest request = new Net.HttpRequest(method);
        final String url = "http://transport.opendata.ch/v1/connections?from=" + from +"&to=" + to + "&limit=1&transportations=tramway_underground";
        System.out.println(url);
        request.setUrl(url);

        //request.setContent(requestJson);

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {

            public void handleHttpResponse(Net.HttpResponse httpResponse) {

                int statusCode = httpResponse.getStatus().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    System.out.println("Request Failed");
                    return;
                }

                String responseJson = httpResponse.getResultAsString();
                try {
                    //System.out.println("" + json.prettyPrint(responseJson));
                    //System.out.println("" + responseJson);
                    //DO some stuff with the response string
                    getAPIFields(responseJson);
                    return;
                }
                catch(Exception exception) {
                    exception.printStackTrace();
                }
            }

            public void failed(Throwable t) {
                System.out.println("Request Failed Completely");
            }

            @Override
            public void cancelled() {
                System.out.println("request cancelled");

            }

        });

    }

    public void getAPIFields(String jsoon) throws FileNotFoundException, JSONException {
        String jsonData = jsoon;

        // System.out.println("File Content: \n" + jsonData);
        JSONObject obj = new JSONObject(jsonData);
        JSONArray jsonarr = obj.getJSONArray("connections");
        JSONObject location = jsonarr.getJSONObject(0);
        JSONObject fromObj = location.getJSONObject("from");
        JSONObject toObj = location.getJSONObject("to");

        //get xy
        JSONObject fromstationObj = fromObj.getJSONObject("station");
        System.out.println("stationinfo" + fromstationObj);
        JSONObject xcoordinateObj = fromstationObj.getJSONObject("coordinate");
        float y1 = Float.parseFloat(xcoordinateObj.getString("x"));
        float x1 = Float.parseFloat(xcoordinateObj.getString("y"));
        //System.out.println("XX " + x1);
        //System.out.println("YY " + y1);

        JSONObject tostationObj = toObj.getJSONObject("station");
        System.out.println("stationinfo" + tostationObj);
        JSONObject ycoordinateObj = tostationObj.getJSONObject("coordinate");
        float y2 = Float.parseFloat(ycoordinateObj.getString("x"));
        float x2 = Float.parseFloat(ycoordinateObj.getString("y"));

        String depart = fromObj.getString("departure");
        Long departureTimeStamp = Long.parseLong(fromObj.getString("departureTimestamp"));

        String arrive = toObj.getString("arrival");
        Long arrivalTimestamp = Long.parseLong(toObj.getString("arrivalTimestamp"));

        String latitude = location.getString("duration");

        //System.out.println("Duration" + latitude);
        //System.out.println("leaves at!" + depart);
        //System.out.println("leaves at! ts " + departureTimeStamp);
        //System.out.println("arrives at!" + arrive);
        //System.out.println("arrives at! ts " + arrivalTimestamp);

        Long tsLong = System.currentTimeMillis()/1000;
        //System.out.println("CURRENT SYSTEM TS " + tsLong);
        timeToDepart = departureTimeStamp - tsLong;
        timeToArrive = arrivalTimestamp - departureTimeStamp;

        timeStep = timeToArrive;

        setXpos(x1);
        setYpos(y1);

        setTimeToDepart(timeToDepart);

        setTimeToArrive(timeToArrive);
        setTimeStep(timeStep);

        setXdifference(x2 - x1);
        setYdifference(y2 - y1);
        //stationno??

        if(timeToDepart < 0){
            timeToArrive = timeToArrive + timeToDepart;
            for(timeToDepart = timeToDepart; timeToDepart < 0; timeToDepart++){
                setXpos(getXpos() + getXdifference() / getTimeStep());
                setYpos(getYpos() + getYdifference() / getTimeStep());
            }

        }
    }

}
