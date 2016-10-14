package com.baigiamasis.ed.geo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    final String TAG = "debug";
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
    private boolean mAddressRequested;
    private DistanceCount distanceCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        MultiDex.install(this);

        //distance
        mDistance = (TextView) findViewById(R.id.distance);
        //point
        mPointLatitudeText = (TextView) findViewById(R.id.pointLatitudeText);
        mPointLongitudeText = (TextView) findViewById(R.id.pointLongtitudeText);
        setPointCoordinates();
        //location
        mLatitudeText = (TextView) findViewById(R.id.latitudeText);
        mLongitudeText = (TextView) findViewById(R.id.longtitudeText);
        //time
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.lastUpdatedText);
        //address
        mAddressText = (TextView) findViewById(R.id.addressText);

        // Create an instance of GoogleAPIClient.
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mResultReceiver = new AddressResultReceiver(new Handler());
//        mResultReceiver.setReceiver(this);
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
            Log.i(TAG, "onResume: connect()");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

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

        createLocationRequest(1000, 1000, LocationRequest.PRIORITY_HIGH_ACCURACY);

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

        //Address
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (mAddressRequested) {
                startIntentService();
            }
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
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    //    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        updateUI(mCurrentLocation);
    }

    //    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateUI(Location location) {
        //Address and distance
        if (mGoogleApiClient.isConnected() && mLastUpdateTime != null) {
            startIntentService();
            mDistance.setText(distanceCount.returnDistance(location.getLatitude(), location.getLongitude()));
        }
        mAddressRequested = true;

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

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "onReceiveResult");
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.i(TAG, "mAddressOutput:" + mAddressOutput);

            //moves to main UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayAddressOutput(mAddressOutput);
                }
            });

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(MainActivity.this, R.string.address_found, Toast.LENGTH_LONG).show();
            }

        }
    }
}
