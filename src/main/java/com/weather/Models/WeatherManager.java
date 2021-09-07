package com.weather.Models;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WeatherManager {
    private String city;
    private String day;
    private Number temperature, tempMon, tempTue, tempWed, tempThu, tempFri, tempSat, tempSun;
    private String icon;
    private String description;
    private String windSpeed;
    private String cloudiness;
    private String pressure;
    private String humidity;

    public WeatherManager(String city) {
        this.city = city;
    }

    //Build a String from the read Json file
    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    //Reads and returns the JsonObject
    public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    //method to get the weather of the selected city
    public void getWeather(){
        int d = 0;

        String unitGroup = "metric";
        String apiKey = "2AEXYVBSPZFGHQPDMK9EGKQTY";
        String apiEndPoint="https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";

        JSONObject json, timelineResponse, json_specific; //get specific data in jsonobject variable

        SimpleDateFormat df2 = new SimpleDateFormat("EEEE", Locale.ENGLISH); //Entire word/day as output
        Calendar c = Calendar.getInstance();
        HttpGet http;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response;
        String rawResult = null;

        StringBuilder requestBuilder=new StringBuilder(apiEndPoint);
        requestBuilder.append(city.replace(' ', '+'));

        //connects and asks the api to send the json file
        try {
            json = readJsonFromUrl("http://api.openweathermap.org/data/2.5/weather?q="+city.toUpperCase()+"&cnt=7&appid=4e99899cfc7011ecb212636c606eecf1&lang=eng&units=metric");

            URIBuilder builder = new URIBuilder(requestBuilder.toString());
            builder.setParameter("unitGroup", unitGroup)
                    .setParameter("key", apiKey);

            http = new HttpGet(builder.toString());
            response = httpclient.execute(http);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                System.out.printf("Bad response status code:%d%n",
                       response.getStatusLine().getStatusCode());
                return;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                rawResult= EntityUtils.toString(entity, Charset.forName("utf-8"));
            }

            response.close();
        }
        catch(java.net.URISyntaxException ue){
            System.err.println(ue.fillInStackTrace());
            return;
        }
        catch (IOException e) {
            System.err.println(e.fillInStackTrace());
            return;
        }

        //receives the particular data in the read Json File
        Map<String, Number> tempValues = new HashMap<String, Number>();
        json_specific = json.getJSONObject("main");
        try{
            timelineResponse = new JSONObject(rawResult);
            ZoneId zoneId= ZoneId.of(timelineResponse.getString("timezone"));

            JSONArray values=timelineResponse.getJSONArray("days");

            for (int i = 0; i < 7; i++) {
                JSONObject dayValue = values.getJSONObject(i);

                ZonedDateTime datetime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(dayValue.getLong("datetimeEpoch")), zoneId);

                tempValues.put(datetime.getDayOfWeek().toString(), dayValue.getDouble("temp"));
            }
        }
        catch(Exception ex){
            System.err.println(ex.fillInStackTrace());
        }

        for(String key : tempValues.keySet()){
            switch(key){
                case "MONDAY":
                    this.tempMon = tempValues.get(key);
                    break;
                case "TUESDAY":
                    this.tempTue = tempValues.get(key);
                    break;
                case "WEDNESDAY":
                    this.tempWed = tempValues.get(key);
                    break;
                case "THURSDAY":
                    this.tempThu = tempValues.get(key);
                    break;
                case "FRIDAY":
                    this.tempFri = tempValues.get(key);
                    break;
                case "SATURDAY":
                    this.tempSat = tempValues.get(key);
                    break;
                case "SUNDAY":
                    this.tempSun = tempValues.get(key);
                    break;
            }
        }

        this.pressure = json_specific.get("pressure").toString();
        this.humidity = json_specific.get("humidity").toString();

        c.add(Calendar.DATE, d);
        this.day = df2.format(c.getTime());

        for(String key : tempValues.keySet()){
            if(key.equals(this.day.toUpperCase())) {
                this.temperature = tempValues.get(key);
                break;
            }
            else
                this.temperature = 0;
        }

        json_specific = json.getJSONObject("wind");
        this.windSpeed = json_specific.get("speed").toString();
        json_specific = json.getJSONObject("clouds");
        this.cloudiness = json_specific.get("all").toString();

        json_specific = json.getJSONArray("weather").getJSONObject(0);
        this.description = json_specific.get("description").toString();
        this.icon = json_specific.get("icon").toString();
    }


    //Setters for all the private fields
    public String getCity() {
        return city;
    }

    public String getDay() {
        return day;
    }

    public Number getTemperature() {
        return temperature;
    }

    public Number getTempMon(){return tempMon; }

    public Number getTempTue(){return tempTue; }

    public Number getTempWed(){return tempWed; }

    public Number getTempThu(){return tempThu; }

    public Number getTempFri(){return tempFri; }

    public Number getTempSat(){return tempSat; }

    public Number getTempSun(){return tempSun; }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getCloudiness() {
        return cloudiness;
    }

    public String getPressure() {
        return pressure;
    }

    public String getHumidity() {
        return humidity;
    }
}
