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



import android.util.Log;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class MarkerGenerator {
    public static void setUpMap(final GoogleMap map,final int day,final int month) {
        map.setMyLocationEnabled(true);
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.6440, 120.9337), 7));
        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                //new LatLng(23.6440, 120.9337), 7));
        String date=String.valueOf(day);
        if(day < 10)
            date="0"+date+"日";
        else
            date=date+"日";
        String db;
        switch(month){
            case 4:
                db="june";
                break;
            case 3:
                db="july";
                break;
            case 2:
                db="august";
                break;
            case 1:
                db="september";
                break;
            case 0:
                db="october";
                break;
            default :
                db="october";
                break;

        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery(db);
        query.setLimit(1000);
        query.whereEqualTo("day",date);  //  onlt show today accident
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> locationList, ParseException e) {
                if (e == null) {


                    for (int i = 1; i < locationList.size(); i++) {
                        double lat = locationList.get(i).getDouble("latitude");
                        double lng = locationList.get(i).getDouble("longitude");
                        int injury = locationList.get(i).getInt("injury");
                        String Kind = locationList.get(i).getString("kind");
                        String smonth = "02";
                        switch(month){
                            case 0:{
                                smonth = "02";
                                break;
                            }
                            case 1:{
                                smonth = "01";
                                break;
                            }
                            case 2:{
                                smonth = "12";
                                break;
                            }
                            case 3:{
                                smonth = "11";
                                break;
                            }
                            case 4:{
                                smonth = "10";
                                break;
                            }
                        }
                        String sday = locationList.get(i).getString("day");
                        map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(Kind).snippet(smonth+"月"+sday+",傷亡人數:" + injury));
                        //Log.d("test", "lat"+lat+" lng"+lng);
                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }
}
