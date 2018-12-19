/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.walkmyandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final static int REQUEST_LOCATION_PERMISSION = 0;
    Location mLastLocation;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button mBtLocation = findViewById(R.id.button_location);
        TextView mLocationTextView = findViewById(R.id.textview_location);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }


    private void getLocation() {
        final TextView mLocationTextView = findViewById(R.id.textview_location);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
               ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
        }else{
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        mLastLocation = location;
                        mLocationTextView.setText(getString(R.string.location_text,mLastLocation.getLatitude(),mLastLocation.getLongitude(),mLastLocation.getTime()));
                    }else{
                        mLocationTextView.setText(R.string.no_location);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission,@NonNull int[] grantResults){
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                }
                else{
                    Toast.makeText(this,R.string.location_permission_denied,Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private class FetchAddressTask extends AsyncTask<Location,Void,String>{

        interface OnTaskCompleted{
            void onTaskCompleted(String result);
        }
        
        private final String TAG = FetchAddressTask.class.getSimpleName();
        private Context mContext;

        FetchAddressTask(Context applicationContext){
            mContext = applicationContext;


        }

        @Override
        protected String doInBackground(Location... locations) {
            Geocoder geocoder = new Geocoder(mContext,Locale.getDefault());
            Location location = locations[0];
            List<Address> addresses = null;
            String resultMessage = "";
            try{
                addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                if(addresses == null || addresses.size() == 0){
                    if(resultMessage.isEmpty()){
                        resultMessage = mContext.getString(R.string.no_address_found);
                        Log.e(TAG,resultMessage);
                    }
                }
                else{
                    Address address = addresses.get(0);
                    ArrayList<String> addressParts = new ArrayList<>();

                    for(int i=0;i<=address.getMaxAddressLineIndex();i++){
                        addressParts.add((address.getAddressLine(i)))
                    }

                    resultMessage = TextUtils.join("\n",addressParts);
                }
            }catch(IOException ioException){
                resultMessage = mContext.getString(R.string.service_not_available);
                Log.e(TAG,resultMessage,ioException);
            }catch (IllegalArgumentException illegalArgumentException){
                resultMessage = mContext.getString(R.string.invalid_lat_long_used);
                Log.e(TAG,resultMessage + "." + "Location = " + location.getLatitude() + ", Longitude = " + location.getLongitude(),illegalArgumentException);
            }
            return resultMessage;
        }

        @Override
        protected void onPostExecute(String address){
            super.onPostExecute(address);
        }
    }
}
