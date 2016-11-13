package com.baigiamasis.ed.geo;

import android.location.Location;
import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class Map {

    private static String mUrl;


     // @param location the location of the mobile
     // @param latDest the latitude of the poi selected
     // @param longDest the longitude of the poi selected

    String getMapsApiDirectionsUrl(Location location, double latDest, double longDest) {

        String origin = "&origins=" +location.getLatitude() + "," + location.getLongitude();
        String destination = "&destinations=" +latDest + ","+ longDest;
        String sensor = "sensor=false";
        String params = origin + destination + "&" + sensor;
        String travelmode = "walking";
        //Project : "TestMap"
        String apiKey ="AIzaSyC67keP_liIP0ZVy0Qknl6GIPGTCIo8heM";
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/distancematrix/"
                + output +"?"+ params+"&mode="+travelmode+ "&key="+apiKey;
        mUrl = url;
        return url;
    }


    static class MapAsyncTask extends AsyncTask<Void, Void, List> {


         //@param params parameters in background
         //@return list(distance and duration)

        @Override
        protected List doInBackground(Void... params) {
            List<Double> list = new ArrayList<>();

            URL url = null;
            try {
                url = new URL(mUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            /*
            JSONObject jsonResult = null;
            HttpClient httpClient = new DefaultHttpClient();
            Double distance ;
            Double duration ;
            List<Double> list = new ArrayList<>();
            HttpGet httpGet = new HttpGet(mUrl);
            try {

                HttpResponse response = httpClient.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200)
                {
                    StringBuilder builder = new StringBuilder();
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    jsonResult = new JSONObject(builder.toString());
                }
                if (jsonResult != null){

                    JSONArray rows = jsonResult.getJSONArray("rows");
                    JSONObject jsonRows = rows.getJSONObject(0);
                    JSONArray elements = jsonRows.getJSONArray("elements");
                    JSONObject jsonElements = elements.getJSONObject(0);
                    String status =  jsonElements.getString("status");
                    if (status.equals("OK")){

                        JSONObject jsonDistance = jsonElements.getJSONObject("distance");
                        distance = jsonDistance.getDouble("value");
                        JSONObject jsonDuration = jsonElements.getJSONObject("duration");
                        duration = jsonDuration.getDouble("value");
                        duration = duration/60/60;
                        list.add(distance);
                        list.add(duration);
                       }
                    else{
                        distance = 0.0;
                        duration = 0.0 ;
                        list.add(distance);
                        list.add(duration);
                    }





                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
*/
            return list;

        }


        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

        }

    }



}
