package ust.exchange.hackust;


import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bq.markerseekbar.MarkerSeekBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, LocationSource{

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private SupportMapFragment mapFragment;

    private Circle circle;
    private Marker prevMarker;
    private long dayofweek;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    private LocationRequest mLocationRequest;

    // Default zoom to use when location permission is
    // not granted.
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private String destination;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    public boolean isMapTouched = false;
    public ArrayList<DatabaseTester.myObject> list;
    private MarkerSeekBar bar;
    public ArrayList<Circle> circleList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        circleList = new ArrayList<Circle>();
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        Button monday = (Button) findViewById(R.id.monButton);

        bar = (MarkerSeekBar) findViewById(R.id.markerSeekBar);
        assert bar != null;
        bar.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @Override
            public String toText(int progress) {
                int min;
                int hr;
                min = progress % 60;
                hr = progress / 60;
                String am_pm;
                if (hr > 12) {
                    hr = hr - 12;
                    am_pm = "PM";
                } else {
                    am_pm = "AM";
                }

                return " " + hr + " : " + String.format("%02d", min) + am_pm;
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mapFragment = (TouchableMapFragment) getSupportFragmentManager().findFragmentById(R.id.touchableMap);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("From...");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (prevMarker != null) {
                    prevMarker.remove();
                }
                prevMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));

                destination = place.getLatLng().latitude + "," + place.getLatLng().longitude;
                new GetDirection().execute();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /*Begin STATE change listeners*/
    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mLocationRequest != null && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;
    }
    @Override
    public void deactivate() {
        onLocationChangedListener = null;
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.touchableMap);
        mapFragment.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(300);
            mLocationRequest.setFastestInterval(300);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            if (mGoogleApiClient == null){
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this /* FragmentActivity */,
                                this /* OnConnectionFailedListener */)
                        .addConnectionCallbacks(this)
                        .addApi(LocationServices.API)
                        .addApi(Places.GEO_DATA_API)
                        .addApi(Places.PLACE_DETECTION_API)
                        .build();
                mGoogleApiClient.connect();
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }
*/
    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        return true;
    }
*/
    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        new MapStateListener(mMap, (TouchableMapFragment) mapFragment, this) {

            @Override
            public void onMapTouched() {
                isMapTouched = true;
            }

            @Override
            public void onMapReleased() {

            }

            @Override
            public void onMapUnsettled() {

            }

            @Override
            public void onMapSettled() {

            }
        };

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

            @Override
            public boolean onMyLocationButtonClick() {
                isMapTouched = false;
                return false;
            }
        });

//        for (DatabaseTester.myObject testing: list) {
//            circle = mMap.addCircle(new CircleOptions()
//                .center(new LatLng(testing.getBegintrip_lat(), testing.getBegintrip_long()))
//                .radius(50)
//                .strokeWidth(10)
//                .strokeColor(Color.argb((int) testing.getSurge() * 25, 203, 45, 45))
//                .fillColor(Color.argb((int) testing.getSurge() * 25, 203, 45, 45)));
//        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLastKnownLocation == null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } else if (!isMapTouched) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        mLastKnownLocation = location;
    }

    public void calculate(View view) {
            Processor proc = new Processor(dayofweek, bar.getProgress());
            list = proc.sort();
            if (circleList.size() != 0) {
                mMap.clear();
            }
            for (DatabaseTester.myObject testing: list) {
                circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(testing.getBegintrip_lat(), testing.getBegintrip_long()))
                .radius(50)
                .strokeWidth(10)
                .strokeColor(Color.argb((int) testing.getSurge() * 25, 203, 45, 45))
                .fillColor(Color.argb((int) testing.getSurge() * 25, 203, 45, 45)));
                circleList.add(circle);
            }
    }

    public void buttonNum0(View view) {
        dayofweek = 0;
    }
    public void buttonNum1(View view) {
        dayofweek = 1;
    }
    public void buttonNum2(View view) {
        dayofweek = 2;
    }
    public void buttonNum3(View view) {
        dayofweek = 3;
    }
    public void buttonNum4(View view) {
        dayofweek = 4;
    }
    public void buttonNum5(View view) {
        dayofweek = 5;
    }
    public void buttonNum6(View view) {
        dayofweek = 6;
    }




    public class GetDirection extends AsyncTask<String, String, String> {
        public String carTime;
        public String publicTime;
        public int carNum;
        public int publicNum;

        @Override
        protected String doInBackground(String ... params) {
            String result = "";
            URL carUrl = null;
            URL transitUrl = null;
            try {
                carUrl = new URL("http://maps.googleapis.com/maps/api/directions/json?origin=" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + "&destination=" + destination + "&sensor=false");
                transitUrl = new URL("http://maps.googleapis.com/maps/api/directions/json?origin=" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + "&destination=" + destination + "&mode=transit&sensor=false");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (estimateTime(carUrl, result, true) == "true" && estimateTime(transitUrl, result, false) == "true") {
                return "true";
            } else {
                return "false";
            }
        }

        private String estimateTime(URL url, String result, Boolean isCar) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = convertInputStreamToString(in);

                if (!(result == null) && !result.equals("")) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (!jsonObject.getString("status").equals("ZERO_RESULTS")) {
                        // routesArray contains ALL routes
                        JSONArray routesArray = null;
                        try {
                            routesArray = jsonObject.getJSONArray("routes");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Grab the first route
                        JSONObject route = null;
                        route = routesArray.getJSONObject(0);

                        JSONArray duration = null;
                        duration = route.getJSONArray("legs");

                        JSONObject text = null;
                        text = duration.getJSONObject(0);

                        if (isCar) {
                            carTime = text.getJSONObject("duration").getString("text");
                            carNum = text.getJSONObject("duration").getInt("value");
                            System.out.println(carTime);
                            System.out.println(carNum);
                        } else {
                            publicTime = text.getJSONObject("duration").getString("text");
                            publicNum = text.getJSONObject("duration").getInt("value");
                            System.out.println(publicTime);
                            System.out.println(publicNum);
                        }
                    }
                    return "true";
                } else {
                    return "false";
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }
        protected void onPostExecute(String file_url) {
            if (publicNum != 0 && carNum != 0) {
                if (publicNum <= carNum + 350 || publicNum <= carNum * 1.2) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Public transit is only " + (publicNum - carNum) / 60 + " min slower. Suggest to use public transit.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }
}
