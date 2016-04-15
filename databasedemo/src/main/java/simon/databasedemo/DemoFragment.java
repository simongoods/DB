package simon.databasedemo;

/**
 * Created by simongoods on 15/12/2.
 */
/*
 * Copyright (C) 2013 Maciej Górski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.PolylineOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;



public class DemoFragment extends BaseFragment {

    private static final double[] CLUSTER_SIZES = new double[]{180, 160, 144, 120, 96};
    private MutableData[] dataArray = {new MutableData(6, new LatLng(-50, 0)), new MutableData(28, new LatLng(-52, 1)),
            new MutableData(496, new LatLng(-51, -2)),};
    private Handler handler = new Handler();
    private Runnable dataUpdater = new Runnable() {

        @Override
        public void run() {
            for (MutableData data : dataArray) {
                data.value = 7 + 3 * data.value;
            }
            onDataUpdate();
            handler.postDelayed(this, 1000);
        }
    };

    private SeekBar clusterSizeSeekbar;
    private CheckBox clusterCheckbox;

    public DemoFragment(int InputMonth) {
        this.month =  InputMonth;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpClusteringViews(view);
    }

    @Override
    protected void setUpMap() {
        //addCircles();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();

            // Initializing
            Marker markero;
            Marker markerd;
            @Override
            public void onMapClick(LatLng point) {
                // Already two locations
                if (markerPoints.size() > 1) {

                    markerPoints.clear();
                    map.clear();
                    Calendar c = Calendar.getInstance();
                    int day;
                    day = c.get(Calendar.DAY_OF_MONTH);
                    MarkerGenerator.setUpMap(map, day, month);
                }

                // Adding new item to the ArrayList
                markerPoints.add(point);


                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);


                //起始及終點位置符號顏色

                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)); //起點符號顏色
                    //markero = map.addMarker(options);
                } else if (markerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)); //終點符號顏色
                    //markerd = map.addMarker(options);
                }

                // Add new marker to the Google Map Android API V2
                map.addMarker(options);

                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
//                    Uri gmmIntentUri = Uri.parse("geo:" + origin + "?q=" + dest);
//                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                    mapIntent.setPackage("com.google.android.apps.maps");
//                    startActivity(mapIntent);
                }

            }
        });

        updateClustering(clusterSizeSeekbar.getProgress(), clusterCheckbox.isChecked());

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            private TextView tv;

            {
                tv = new TextView(getActivity());
                tv.setTextColor(Color.BLACK);
            }

            private Collator collator = Collator.getInstance();
            private Comparator<Marker> comparator = new Comparator<Marker>() {
                public int compare(Marker lhs, Marker rhs) {
                    String leftTitle = lhs.getTitle();
                    String rightTitle = rhs.getTitle();
                    if (leftTitle == null && rightTitle == null) {
                        return 0;
                    }
                    if (leftTitle == null) {
                        return 1;
                    }
                    if (rightTitle == null) {
                        return -1;
                    }
                    return collator.compare(leftTitle, rightTitle);
                }
            };

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if (marker.isCluster()) {
                    List<Marker> markers = marker.getMarkers();
                    int i = 0;
                    String text = "";
                    while (i < 3 && markers.size() > 0) {
                        Marker m = Collections.min(markers, comparator);
                        String title = m.getTitle();
                        if (title == null) {
                            break;
                        }
                        text += title + "\n";
                        markers.remove(m);
                        i++;
                    }
                    if (text.length() == 0) {
                        text = "Markers with mutable data";
                    } else if (markers.size() > 0) {
                        text += "and " + markers.size() + " more...";
                    } else {
                        text = text.substring(0, text.length() - 1);
                    }
                    tv.setText(text);
                    return tv;
                } else {
                    if (marker.getData() instanceof MutableData) {
                        MutableData mutableData = marker.getData();
                        tv.setText("Value: " + mutableData.value);
                        return tv;
                    }
                }

                return null;
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.isCluster()) {
                    List<Marker> markers = marker.getMarkers();
                    LatLngBounds.Builder builder = LatLngBounds.builder();
                    for (Marker m : markers) {
                        builder.include(m.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, getResources().getDimensionPixelSize(R.dimen.padding)));
                }
            }
        });
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.6440, 120.9337), 7));
        Calendar c = Calendar.getInstance();
        int day;
        day = c.get(Calendar.DAY_OF_MONTH);
        MarkerGenerator.setUpMap(map, day, month);
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
//        for (MutableData data : dataArray) {
//            map.addMarker(new MarkerOptions().position(data.position).icon(icon).data(data));
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(dataUpdater);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(dataUpdater);
    }

    private void onDataUpdate() {
        Marker m = map.getMarkerShowingInfoWindow();
        if (m != null && !m.isCluster() && m.getData() instanceof MutableData) {
            m.showInfoWindow();
        }
    }



    private void setUpClusteringViews(View view) {
        clusterCheckbox = (CheckBox) view.findViewById(R.id.checkbox_cluster);
        clusterSizeSeekbar = (SeekBar) view.findViewById(R.id.seekbar_cluster_size);
        clusterCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clusterSizeSeekbar.setEnabled(isChecked);

                updateClustering(clusterSizeSeekbar.getProgress(), isChecked);
            }
        });
        clusterSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateClustering(progress, clusterCheckbox.isChecked());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    void updateClustering(int clusterSizeIndex, boolean enabled) {
        if (map == null) {
            return;
        }
        ClusteringSettings clusteringSettings = new ClusteringSettings();
        clusteringSettings.addMarkersDynamically(true);

        if (enabled) {
            clusteringSettings.clusterOptionsProvider(new DemoClusterOptionsProvider(getResources()));

            double clusterSize = CLUSTER_SIZES[clusterSizeIndex];
            clusteringSettings.clusterSize(clusterSize);
        } else {
            clusteringSettings.enabled(false);
        }
        map.setClustering(clusteringSettings);
    }

    private static class MutableData {

        private int value;

        private LatLng position;

        public MutableData(int value, LatLng position) {
            this.value = value;
            this.position = position;
        }
    }
    //-------------------------------ROAD-----------------------------------------------------------
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;

        return url;
    }

    /**從URL下載JSON資料的方法**/
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /** 解析JSON格式 **/
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            com.google.android.gms.maps.model.MarkerOptions markerOptions = new com.google.android.gms.maps.model.MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);  //導航路徑寬度
                lineOptions.color(Color.BLUE); //導航路徑顏色

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null)
                map.addPolyline(lineOptions);
            else{

            }
        }
    }
}
