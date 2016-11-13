package com.baigiamasis.ed.geo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

//TODO fix: Location permission window crash (app crashes only first time after permission is granted)
//TODO fix: after permission is allowed it doesn't crash.

    final String TAG = "debug";
    public List mList;
    //    private final double latitude = 56.458155, longitude = 16.582279;
    private final double latitude = 55.675165, longitude = 21.175710;
    public TextView mLatitudeText, mLongitudeText, mLastUpdateTimeTextView, mAddressText,
            mPointLatitudeText, mPointLongitudeText, mDistance;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation, mCurrentLocation, mLatitude, mLongitude;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = true;
    private String mLastUpdateTime;
    private AddressResultReceiver mResultReceiver;
    private DistanceCount distanceCount;
    public Button buttonMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

//        MultiDex.install(this);

        // Distance
        mDistance = (TextView) findViewById(R.id.distance);
        // Point location
        mPointLatitudeText = (TextView) findViewById(R.id.pointLatitudeText);
        mPointLongitudeText = (TextView) findViewById(R.id.pointLongtitudeText);
        setPointCoordinates();
        // Location
        mLatitudeText = (TextView) findViewById(R.id.latitudeText);
        mLongitudeText = (TextView) findViewById(R.id.longtitudeText);
        // Time
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.lastUpdatedText);
        // Address
        mAddressText = (TextView) findViewById(R.id.addressText);

        buttonMapSetUp();

        // Creates GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mResultReceiver = new AddressResultReceiver(null);
        distanceCount = new DistanceCount(latitude, longitude);
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Log.i(TAG, "onStart: connect()");
        }
        super.onStart();
    }

    // B U T T O N    S E T    U P
    public void buttonMapSetUp(){
        final int MODE_SHOW = 1;
        buttonMap = (Button) findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                i.putExtra("extra_map_mod", MODE_SHOW);
                startActivity(i);
            }
        });

    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
//        MultiDex.install(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
            Log.i(TAG, "onResume: connect()");
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();
        stopLocationUpdates();
    }

    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    //calls to find current or last known address
    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        if (mCurrentLocation != null) {
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
        } else {
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        }
        startService(intent);
        Log.i(TAG, "startIntentService");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");

        //change speed for checking location changes
        createLocationRequest(5000, 10000, LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Checking Permissions
        checkPermission();

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        if (mLastLocation != null) {
            updateUI(mLastLocation);
        } else {
            Log.i(TAG, "mLastLocation is NULL");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    protected void createLocationRequest(int interval, int fastestInterval, int priority) {
        Log.i(TAG, "createLocationRequest: ON");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(priority);
    }

    protected void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates: ON");
        checkPermission();

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());


        //
        // google maps distance matrix api
        //gauna

        Map map = new Map();
        String url = map.getMapsApiDirectionsUrl(location, latitude, longitude);
        Log.w(TAG, url);
        Map.MapAsyncTask mapAsyncTask = new Map.MapAsyncTask();
        mapAsyncTask.execute();
        try {
            mList = mapAsyncTask.get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        updateUI(mCurrentLocation);
    }

    private void updateUI(Location location) {
        //Address and distance
        if (mGoogleApiClient.isConnected() && mLastUpdateTime != null) {
            startIntentService();
            mDistance.setText(distanceCount.returnDistance(location.getLatitude(), location.getLongitude()));

            TextView mDistance2 = (TextView) findViewById(R.id.distance2);
            TextView mTime2 = (TextView) findViewById(R.id.time2);
            try{
                mDistance2.setText(String.valueOf(mList.get(0)));
                mTime2.setText(String.valueOf(mList.get(1)));
            }catch ( IndexOutOfBoundsException e) {
                e.printStackTrace();
                Toast.makeText(this.getApplicationContext(), "IndexOutOfBoundsException", Toast.LENGTH_LONG).show();
            }
        }

        //updateUI
        Log.i(TAG, "updateUI:" + mLastUpdateTime);
        mLatitudeText.setText(String.valueOf(location.getLatitude()));
        mLongitudeText.setText(String.valueOf(location.getLongitude()));
        mLastUpdateTimeTextView.setText(mLastUpdateTime);
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }
    }

    public void displayAddressOutput(String mAddressOutput) {
        mAddressText.setText(mAddressOutput);
    }

    public void setPointCoordinates() {
        mPointLatitudeText.setText(String.valueOf(latitude));
        mPointLongitudeText.setText(String.valueOf(longitude));
    }

    class AddressResultReceiver extends ResultReceiver {
        private String mAddressOutput;

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "onReceiveResult");
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.i(TAG, "mAddressOutput:" + mAddressOutput);

            // Moves to main UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayAddressOutput(mAddressOutput);
                }
            });
        }
    }
}
