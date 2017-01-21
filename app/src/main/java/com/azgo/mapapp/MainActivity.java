package com.azgo.mapapp;

import android.Manifest;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ImageView;

import com.azgo.mapapp.fragments.AboutActivity;
import com.azgo.mapapp.fragments.FavouritesActivity;
import com.azgo.mapapp.fragments.HistoryActivity;
import com.azgo.mapapp.fragments.MapActivity;
import com.azgo.mapapp.fragments.SettingsActivity;
import com.facebook.login.LoginManager;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.PolyUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    PolylineOptions linePath = new PolylineOptions();
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;

    private LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Location mCurrentLocation;
    Marker mCurrentLocationMarker;
    String location;
    Location sendLastLocation;
    Polyline mPolyLine;
    private LocationManager locationManager;
    Location location_nav;
    private CameraPosition mCameraPosition;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private int navigation_on = 0;
    int start_nav = 0;
    float mDeclination;
    SensorManager mSensorManager;
    private Sensor mRotVectorSensor;
    private final float[] mRotationMatrix = new float[16];
    double angle;

    static final float ALPHA = 0.5f; //variável global
    TextToSpeech ttobj;
    private EditText write;

    //prepare graph
    static Graph grafo = new Graph();
    static List<Graph.Node> nodes = grafo.insertNodes();
    static boolean[][] adj = grafo.fillMatrix();
    LinkedList<Graph.Node> caminho = new LinkedList<>();
    List<Graph.Node> caminho_dois = new ArrayList<>(75);

    //design
    NavigationView navigationView = null;
    NavigationView navigationViewRight = null;
    Menu menuRight = null;
    Toolbar toolbar = null;
    DrawerLayout drawer = null;
    ActionBarDrawerToggle toggle = null;
    Button testButton = null;
    RadioGroup radio = null;

    String roomMeet, emailMeet;


    //Communicação
    public TCPClient mTcpClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //private static boolean messageReceived;
    //private static String Message;
    private static String[][] FriendsMessage;
    boolean logoutPressed = false;
    private AsyncTask senAsync;
    private AsyncTask recAsync;
    private AsyncTask friAsync;
    private AsyncTask logAsync;
    private AsyncTask waitConnection;
    private AsyncTask meetTask;
    //static Queue<String> numbersArray = new LinkedList<>();
    static String friends = "Friends";
    public static String sessionID;
    List<LatLng> points = new ArrayList<>();
    Instructions.Instruction[] instrucoes = new Instructions.Instruction[999];
    boolean friendsready = false;

    static List<FriendsData<String, String, String>> FriendsDataList = new ArrayList<>();


    private Object lockmessa = new Object();
    private Object lockfriends = new Object();
    private Object lockReception = new Object();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setName("MainActivity");
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


        // Create the LocationRequest object
        /*mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)        // 1 second, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
                //.setSmallestDisplacement(1); //1 meter
        /*createBuilder();
        createLocationRequest();*/
        //mGoogleMap = null;


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nvView);
        navigationView.setNavigationItemSelectedListener(this);

        navigationViewRight = (NavigationView) findViewById(R.id.nvView_right);
        menuRight = navigationViewRight.getMenu();
        //menuRight.add(0,1,0,"ASD0");

        navigationViewRight.setNavigationItemSelectedListener(this);


        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                ttobj.setLanguage(Locale.US);
                if (status == TextToSpeech.SUCCESS) {
                    ttobj.speak("Welcome", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        //Communication Stuff

        mAuth = FirebaseAuth.getInstance();



        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header);
        final TextView username = (TextView) headerLayout.findViewById(R.id.username);


        if (mAuth.getCurrentUser().getDisplayName() != null) {
            Log.e("sim", mAuth.getCurrentUser().getDisplayName());
            username.setText(mAuth.getCurrentUser().getDisplayName().toUpperCase());
        } else {
            Log.e("nao", mAuth.getCurrentUser().getDisplayName());
            username.setText(R.string.noName);
        }
        sessionID = getIntent().getStringExtra("sessionId");

        Log.d("onCreate: ", "SeccionID is:" + sessionID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            recAsync = new backgroundReception().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            senAsync = new backgroundSending().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            friAsync = new backgroundSendFriends().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
            recAsync = new backgroundReception().execute();
            senAsync = new backgroundSending().execute();
            friAsync = new backgroundSendFriends().execute();
        }
        //till were

        /*
        if (googleServicesAvailable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            }
            Toast.makeText(this, "Connected!!", Toast.LENGTH_LONG).show();
            mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
            //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //mGoogleMap.setIndoorEnabled(true);
        } else {
            //No Google Maps Layout
        }
        //Location location = locationManager.getLastKnownLocation(provider);*/
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        //Location location = locationManager.getLastKnownLocation(provider);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //getnumbers
        Cursor phones = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String separator = "$";
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //String phonemails = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
            phoneNumber = phoneNumber.replaceAll("\\s+", ""); //tirar espaços;
//            phoneNumber = phoneNumber.substring(phoneNumber.length() - 9); //buscar ultimos 9 numeros
            String oldfriends = friends + separator + phoneNumber;
            friends = oldfriends;
        }
        friendsready = true;
        phones.close();// close cursor
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
        //low to avoid flickering
        mSensorManager.registerListener(this, mRotVectorSensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        updateMarkers();
        //TODO: be tested
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            recAsync = new backgroundReception().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            senAsync = new backgroundSending().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
            recAsync = new backgroundReception().execute();
            senAsync = new backgroundSending().execute();
        }
*/
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister listener
        mSensorManager.unregisterListener(this);
        //mGoogleMap=null;

        //Stop asyncTask
        //TODO: be tested
        /*
        senAsync.cancel(true);
        recAsync.cancel(true);
        */

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }


    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot connect to Google play services", Toast.LENGTH_LONG).show();
        }
        return false;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        updateLocationUI();
        // Add markers for nearby places.
        updateMarkers();
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setIndoorEnabled(true);
        //set markers on all the rooms
        for (Graph.Node no : nodes) {
            if (no.getIndex() <= 35)
                MainActivity.this.setMarker(no.getLabel(), no.getLatitude(), no.getLongitude());
            else
                break;
        }
        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.info_window, null);

                TextView tvLocality = (TextView) infoWindow.findViewById(R.id.tv_locality);
                TextView tvLat = (TextView) infoWindow.findViewById(R.id.tv_lat);
                TextView tvLng = (TextView) infoWindow.findViewById(R.id.tv_lng);
                TextView tvSnippet = (TextView) infoWindow.findViewById(R.id.tv_snippet);

                LatLng ll = marker.getPosition();
                tvLocality.setText(marker.getTitle());
                tvLat.setText("Latitude: " + ll.latitude);
                tvLng.setText("Longitude: " + ll.longitude);
                tvSnippet.setText(marker.getSnippet());

                //TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                //snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
    /*
     * Set the map's camera position to the current location of the device.
     * If the previous state was saved, set the position to the saved state.
     * If the current location is unknown, use a default position and zoom value.
     */
        if (mCameraPosition != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), (float) 19.08));
        } else {

        }

        radio = (RadioGroup) findViewById(R.id.radio_group_list_selector);
        testButton = (Button) findViewById(R.id.startActivityButton);

        testButton.setTag(1);
        //\testButton.setText("Navigate Here");
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int status = (Integer) v.getTag();
                if (status == 1) {
                    navigation(v);
                    testButton.setBackgroundResource(R.drawable.cancelnavigation);
                    navigation_on = 1;
                    v.setTag(0);
                    radio.setVisibility(View.VISIBLE);
                } else {
                    testButton.setBackgroundResource(R.drawable.navigate);
                    stopNavigation(mCurrentLocation);
                    v.setTag(1);
                    navigation_on = 0;
                    radio.setVisibility(View.INVISIBLE);

                }
            }
        });

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        createLocationRequest();


    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getDeviceLocation() {

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
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);


    }

    Marker marker;

    public void geoLocate(View view) throws IOException {
        EditText et = (EditText) findViewById(R.id.editText);
        String location = et.getText().toString();
        Graph.Node searchNode = null;
        //converts string to lat and lng
        //devolve a localização
        //String locality = address.getLocality();
        //set markers on all the rooms
        for (Graph.Node no : nodes) {
            if (no.getLabel().equals(location)) {
                searchNode = no;
                break;
            }
        }


        Toast.makeText(this, location, Toast.LENGTH_LONG).show();

        double lat = searchNode.getLatitude();
        double lng = searchNode.getLongitude();
        goToLocationZoom(lat, lng, (float) 21);
        //setMarker(location, lat, lng);

    }

    public void navigation(View view) {
        final EditText et = (EditText) findViewById(R.id.editText);
        location = et.getText().toString();
        PolylineOptions linePath = new PolylineOptions();
        //mLastLocation = location;
        //Get map on navigation mode
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        // Construct a CameraPosition focusing on current position and animate the camera to that position.
        //change camera view on current user's location to start navigation
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(21)                   // Sets the zoom
                .tilt(60)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        Graph.Node searchNode = null;
        for (Graph.Node no : nodes) {
            if (no.getLabel().equals(location.toUpperCase())) {
                searchNode = no;
                break;
            }
        }
        startNavigationTo(searchNode, mCurrentLocation);


        //Intent i = new Intent(this, Navigation.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //i.putExtra("location", locationMessage);
        //send String to navigate to room number
       /* if(location!=null && !location.isEmpty()){
            Toast.makeText(this, "Starting navigation to "+location, Toast.LENGTH_LONG).show();
        }*/
        //startActivity(i);
        //finish();
        //location.equals(null);

    }

    public void startNavigationTo(Graph.Node searchNode, Location mCurrentLocation) {

        //calculate closest Node to mLastLocation
        //mGoogleMap.UiSettings.setMapToolbarEnabled(false);
        List<Graph.Node> nos = grafo.getListNodes();
        Graph.Node closestNode;
        Graph.Node indexSource;
        List<Graph.Node> nos_entradas = new ArrayList<>(4);
        nos_entradas.add(0, grafo.getNode(36));
        nos_entradas.add(1, grafo.getNode(37));
        nos_entradas.add(2, grafo.getNode(38));
        nos_entradas.add(3, grafo.getNode(39));
        //ver se a posição atual está dentro do edifício B
        if(mCurrentLocation.getLatitude()< 41.177142 || mCurrentLocation.getLatitude()>41.177940 || mCurrentLocation.getLongitude()>-8.594958 || mCurrentLocation.getLongitude() < -8.597510){
            closestNode = findClosestNodeEntrada(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), nos_entradas);
            indexSource = grafo.getNode(closestNode.getIndex());
        }
        else {
            closestNode = findClosestNode(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), nos);
            indexSource = grafo.getNode(closestNode.getIndex());
        }

        //calculate shortest path from firstNode to searchNode
        Graph.Node indexDest = grafo.getNode(searchNode.getIndex());
        double result = MatrixGraphAlgorithms.shortestPath(adj, grafo, indexSource, indexDest, caminho);
        //shortest path is on caminho
        //draw path on Google Maps
        // Instantiates a new Polyline object and adds points to define the navigation path
        PolylineOptions linePath = new PolylineOptions();
        linePath.add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        /*listIter = myList.listIterator(myList.size());
        while (listIter.hasPrevious()) {
            String prev = listIter.previous();
            // Do something with prev here
        }*/

        for (Graph.Node no : caminho) {
            linePath.add(new LatLng(no.getLatitude(), no.getLongitude()));
        }

        //add extra options
        linePath.width(25)
                .geodesic(false)
                .color(Color.GREEN);

        // Get back the mutable Polyline
        mPolyLine = mGoogleMap.addPolyline(linePath);


    }

    public static Graph.Node findClosestNodeEntrada(double latitude, double longitude, List<Graph.Node> nodes) {
        Graph.Node closestNode = null;
        double[][] points = new double[4][2];
        double shortestDistance = 0;
        double distance = 0;
        int i=0;
        //enter x,y coords into the 69x2 table points[][]
        for (Graph.Node no : nodes) {
            points[i][0] = no.getLatitude();
            points[i][1] = no.getLongitude();
            i++;
        }
        i=0;
        //get the distance between the point in the ith row and the (m+1)th row
        //and check if it's shorter than the distance between 0th and 1st
        for (Graph.Node no: nodes) {
            //use m=i rather than 0 to avoid duplicate computations

                double dx = points[i][0] - latitude;
                double dy = points[i][1] - longitude;
                distance = Math.sqrt(dx * dx + dy * dy);

                //set shortestDistance and closestPoints to the first iteration
                if (i == 0) {
                    shortestDistance = distance;
                    closestNode = no;
                }
                //then check if any further iterations have shorter distances
                else if (distance < shortestDistance) {
                    shortestDistance = distance;
                    closestNode = no;
                }
            
            i++;
        }
        //search the closest Node on shortest path

        return closestNode;
    }


    public static Graph.Node findClosestNode(double latitude, double longitude, List<Graph.Node> nodes) {
        Graph.Node closestNode = null;
        double[][] points = new double[69][2];
        double shortestDistance = 0;
        double distance = 0;

        //enter x,y coords into the 69x2 table points[][]
        for (Graph.Node no : nodes) {
            points[no.getIndex()][0] = no.getLatitude();
            points[no.getIndex()][1] = no.getLongitude();
        }

        //get the distance between the point in the ith row and the (m+1)th row
        //and check if it's shorter than the distance between 0th and 1st
        for (Graph.Node no : nodes) {
            //use m=i rather than 0 to avoid duplicate computations
            for (int m = no.getIndex(); m < 69 - 1; m++) {
                double dx = points[no.getIndex()][0] - latitude;
                double dy = points[no.getIndex()][1] - longitude;
                distance = Math.sqrt(dx * dx + dy * dy);

                //set shortestDistance and closestPoints to the first iteration
                if (m == 0 && no.getIndex() == 0) {
                    shortestDistance = distance;
                    closestNode = no;
                }
                //then check if any further iterations have shorter distances
                else if (distance < shortestDistance) {
                    shortestDistance = distance;
                    closestNode = no;
                }
            }
        }
        //search the closest Node on shortest path

        return closestNode;
    }

    public void stopNavigation(Location mLastLocation) {
        String name = "I am here";
        //Place current location marker
        mPolyLine.remove();
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        //setMarker(name, mLastLocation.getLatitude(), mLastLocation.getLongitude());


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom((float) 19.08)                   // Sets the zoom
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, (float) 19.08);
        //move map camera
        //mGoogleMap.moveCamera(cameraUpdate);
    }

    Circle circle;

    private void setMarker(final String locality, double lat, double lng) {
        BitmapDescriptor icon;
        switch (locality) {
            case "B001":
            case "B002":
            case "B003":
                icon = BitmapDescriptorFactory.fromResource(R.drawable.auditorio);
                break;
            default:
                if (locality.charAt(0) == 'B') // Temporário até adicionar novos edificios ao mapa
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.sala);
                else
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.posicao);
        }
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                //.draggable(true)
                //to specify a custom marker, use .icon(BitmapDescriptorFactory.fromResource(id_Resource))...
                .icon(icon)
                .position(new LatLng(lat, lng))
                .snippet("I am here"); //something added to add more info


        marker = mGoogleMap.addMarker(options);

    }

    private void removeEverything() {
        marker.remove();
        marker = null;
        circle.remove();
        circle = null;
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.logout:
                signOut();
                break;
            case R.id.info:
                goUserInfoPage();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);

    }
*/
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_openRight) {
            drawer.openDrawer(GravityCompat.END); /*Opens the Right Drawer
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onConnected(Bundle bundle) {
        getDeviceLocation();
        mSensorManager.registerListener(this, mRotVectorSensor, SensorManager.SENSOR_STATUS_ACCURACY_LOW);

        // mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
        // mSensorManager.registerListener(this, mGeomagnetic, SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);

        mapFrag = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    }

    private void handleNewLocation(Location location) {
        Log.d("localização", location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mGoogleMap.addMarker(options);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, (float) 19.08);
        //move map camera
        mGoogleMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        /*Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());*/
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView distance_view = (TextView) findViewById(R.id.distance);
        TextView text_view = (TextView) findViewById(R.id.instruction);
        ImageView imagem_view = (ImageView) findViewById(R.id.arrow_image);

        //test if navigation mode is on
        if (navigation_on == 1) {

            if (start_nav == 0) {//first time since navigation started

                int y = 0;
                //Log.e("valor de y ", " "+y);
                for (Graph.Node nos_t : caminho) {
                    caminho_dois.add(y, nos_t);
                    //Log.e("INDICE CAMINHO: "+y, " "+nos_t.getIndex());
                    y++;
                }
                text_view.setText("Welcome to AZGO");
                imagem_view.setImageResource(R.drawable.forward);
                //add all nodes of navigation path to variable points
                if (!caminho.isEmpty()) {
                    points.add(new LatLng(location.getLatitude(), location.getLongitude()));
                    for (Graph.Node no : caminho) {
                        points.add(new LatLng(no.getLatitude(), no.getLongitude()));
                    }
                }
                //Graph nod = new Graph().Node(-1,"null",0,0);
                //Instructions.Instruction auxiliar = new Instructions().new Instruction("nada",caminho.getFirst());

                instrucoes = Instructions.calculateInstructions(caminho);
                start_nav = 1;
            }
            if (start_nav == 1) {//instructions are loaded, path is done
                int i = 0;
                if (instrucoes == null) {
                    Log.e("ESTA VAZIA.....", "cenas");
                } else {
                    for (i = 0; i < instrucoes.length; i++) {
                        if (instrucoes[i] == null) break;
                        Log.e("INSTRUCAO nº" + i, instrucoes[i].text);
                    }
                }
                LatLng testPoint = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng nearestPoint = findNearestPoint(testPoint, points);
                mCurrentLocation.setLatitude(nearestPoint.latitude);
                mCurrentLocation.setLongitude(nearestPoint.longitude);
                Graph.Node atual_cenas;

                //Log.e("TAMANHO : ", " "+caminho_dois.size());
                /*for(Graph.Node no: caminho_dois){
                    Log.e("CAMINHO DOIS", "no "+no.getIndex());
                }*/
                //atual constains the closest node to the graph of the current location
                atual_cenas = findClosestNodeNOW(nearestPoint.latitude, nearestPoint.longitude, caminho_dois);
                //Log.e("ATUAL_CENAS", " "+atual_cenas.getIndex());
                //calcular a distância até ao destino
                //results[0] contains the computed distance
                float[] results = new float[5];
                Location localizacao_atual = new Location("localizacao atual");
                localizacao_atual.setLatitude(mCurrentLocation.getLatitude());
                localizacao_atual.setLongitude(mCurrentLocation.getLongitude());
                localizacao_atual.distanceBetween(localizacao_atual.getLatitude(), localizacao_atual.getLongitude(), atual_cenas.getLatitude(), atual_cenas.getLongitude(), results);
                //redundante....
                float distancia = 0;
                distancia = results[0];
                int start = 0;
                Graph.Node now = null;
                Graph.Node next = null;
                for (Graph.Node no : caminho) {
                    if (start == 0) {
                        if ((no.getLatitude() == atual_cenas.getLatitude()) && (no.getLongitude() == atual_cenas.getLongitude())) {
                            start = 1;
                            //now.setIndex(atual_cenas.getIndex());
                            now = atual_cenas;
                        }
                    } else {
                        //next.setIndex(atual_cenas.getIndex());
                        next = no;
                        localizacao_atual.setLatitude(now.getLatitude());
                        localizacao_atual.setLongitude(now.getLongitude());
                        localizacao_atual.distanceBetween(localizacao_atual.getLatitude(), localizacao_atual.getLongitude(), next.getLatitude(), next.getLongitude(), results);
                        distancia += results[0];
                        //now.setIndex(no.getIndex());
                        now = no;
                    }
                }


                if (instrucoes == null) {
                    Log.e("ESTA VAZIA.....", "cenas");
                } else {
                    for (i = 0; i < instrucoes.length; i++) {
                        if (instrucoes[i] == null || instrucoes[i].node == null) break;
                       /* Log.e("Instrucoes[i].text ", " "+instrucoes[i].text);
                        Log.e("Instrucoes[i].latitude ", " "+instrucoes[i].node.getLatitude());
                        Log.e("Instrucoes[i].longitude", " "+instrucoes[i].node.getLongitude());
                        Log.e("Atual_cenas.latitude ", " "+atual_cenas.getLatitude());
                        Log.e("Atual_cenas.longitude", " "+atual_cenas.getLongitude());*/

                        if ((instrucoes[i].node.getLatitude() == atual_cenas.getLatitude()) && (instrucoes[i].node.getLongitude() == atual_cenas.getLongitude())) {

                            //TextView text = (TextView)findViewById(R.id.instruction);
                            final String texto_ler = instrucoes[i].text;
                            text_view.setText(instrucoes[i].text);
                            if (i > 0) {
                                if ((instrucoes[i].text != instrucoes[i - 1].text) && i != 0)
                                    ttobj.speak(texto_ler, TextToSpeech.QUEUE_FLUSH, null);
                                //ImageView imagem = (ImageView) findViewById(R.id.arrow_image);
                            }
                            if (instrucoes[i].text.equals("Go out of the room through the corridor")) {
                                imagem_view.setImageResource(R.drawable.forward);
                            } else if (instrucoes[i].text.equals("Get in the room")) {
                                imagem_view.setImageResource(R.drawable.forward);
                            } else if (instrucoes[i].text.equals("Follow the exit")) {
                                imagem_view.setImageResource(R.drawable.forward);
                            } else if (instrucoes[i].text.equals("Get in the building")) {
                                imagem_view.setImageResource(R.drawable.forward);
                            } else if (instrucoes[i].text.equals("Follow the corridor")) {
                                imagem_view.setImageResource(R.drawable.forward);
                            } else if (instrucoes[i].text.equals("Go forward")) {
                                imagem_view.setImageResource(R.drawable.forward);
                            } else if (instrucoes[i].text.equals("Turn right")) {
                                imagem_view.setImageResource(R.drawable.right);
                            } else if (instrucoes[i].text.equals("Turn left")) {
                                imagem_view.setImageResource(R.drawable.left);
                            } else if (instrucoes[i].text.equals("Go back")) {
                                imagem_view.setImageResource(R.drawable.back);
                            } else if (instrucoes[i].text.equals("You have arrived to your destination")) {
                                imagem_view.setImageResource(R.drawable.forward);

                            }
                        }

                    }
                }
                int nova_distancia = (int) Math.round(distancia);
                float tempo;
                tempo = nova_distancia / ((float) (1.4));
                int novo_tempo = (int) Math.round(tempo);

                distance_view.setText("Distance: " + nova_distancia + "m   ETA: " + novo_tempo + "s");
                atual_cenas = null;
            }

        } else {
            if (start_nav == 1) {
                start_nav = 0;
                points.clear();
                caminho.clear();
                //clear the array of instructions for future navigation
                Arrays.fill(instrucoes, null);
                distance_view.setText(" ");
            }
            mCurrentLocation = location;
        }

        //updateMarkers();
        GeomagneticField field = new GeomagneticField(
                (float) mCurrentLocation.getLatitude(),
                (float) mCurrentLocation.getLongitude(),
                (float) mCurrentLocation.getAltitude(),
                System.currentTimeMillis()
        );
        //getDeclination returns degrees
        mDeclination = field.getDeclination();

        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        if (navigation_on == 1) {
            marker.remove();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    //.bearing(mCurrentLocation.getBearing())// Sets the center of the map to Mountain View
                    .zoom(21)                   // Sets the zoom
                    .tilt(60)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            setMarker("local", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        } else {
            marker.remove();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    //.bearing(mCurrentLocation.getBearing())// Sets the center of the map to Mountain View
                    .zoom((float) 19.08)                   // Sets the zoom
                    .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            setMarker("local", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //TODO:
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                //(just doing it here for now, note that with this code, no explanation is shown)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    private Graph.Node findClosestNodeNOW(double latitude, double longitude, List<Graph.Node> caminho_dois) {
        Graph.Node closestNode = new Graph().new Node(5, "mais perto", 0, 0);
        //double[][] points = new double[caminho_dois.size()][2];
        double shortestDistance = 200;
        double distance = 0;
        //enter x,y coords into the 69x2 table points[][]
        /*for (Graph.Node no : caminho_dois) {
            points[i][0] = no.getLatitude();
            points[i][1] = no.getLongitude();
            i++;
        }*/

        //get the distance between the point in the ith row and the (m+1)th row
        //and check if it's shorter than the distance between 0th and 1st

        for (Graph.Node no : caminho_dois) {
            //use m=i rather than 0 to avoid duplicate computations
            // for (int m = i; m < caminho_dois.size() - 1; m++) {
            double dx = no.getLatitude() - latitude;
            double dy = no.getLongitude() - longitude;
            distance = Math.sqrt(dx * dx + dy * dy);

            //set shortestDistance and closestPoints to the first iteration
                /*if (m == 0 && i == 0) {
                    shortestDistance = distance;
                    closestNode = no;
                }*/
            //then check if any further iterations have shorter distances
            if (distance < shortestDistance) {
                shortestDistance = distance;
                closestNode.setIndex(no.getIndex());
                closestNode.setLabel("mais proximo");
                closestNode.setLatitude(no.getLatitude());
                closestNode.setLongitude(no.getLongitude());
            }


        }
        //search the closest Node on shortest path

        return closestNode;
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
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

    private void updateMarkers() {
        if (mGoogleMap == null) {
            return;
        }

       /* if (mLocationPermissionGranted) {
            // Get the businesses and other points of interest located
            // nearest to the device's current location.
            @SuppressWarnings("MissingPermission")
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        // Add a marker for each place near the device's current location, with an
                        // info window showing place information.
                        String attributions = (String) placeLikelihood.getPlace().getAttributions();
                        String snippet = (String) placeLikelihood.getPlace().getAddress();
                        if (attributions != null) {
                            snippet = snippet + "\n" + attributions;
                        }
                        mGoogleMap.addMarker(new MarkerOptions()
                                .position(placeLikelihood.getPlace().getLatLng())
                                .title((String) placeLikelihood.getPlace().getName())
                                .snippet(snippet));
                    }
                    // Release the place likelihood buffer.
                    likelyPlaces.release();
                }
            });
        } else {
        }*/
    }

    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (mGoogleMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            mGoogleMap.setMyLocationEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mGoogleMap.getUiSettings().setCompassEnabled(false);
            mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        } else {
            mGoogleMap.setMyLocationEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mGoogleMap.getUiSettings().setCompassEnabled(false);
            mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
            mCurrentLocation = null;
        }
    }


    public void signOut() {

        logoutPressed = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Log.e("SignOut", "Logout - if");
            logAsync = new backgroundLogout().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
            Log.e("SignOut", "Logout - if");
            logAsync = new backgroundLogout().execute();
        }
    }

    private void goLoginScreen() {
        Intent intent = new Intent(this, mainLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goUserInfoPage() {
        Intent intent = new Intent(this, infoPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

       /* if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            //instance variable that holds the last set of smoothed values
            accelVals = lowPass(event.values.clone(), accelVals);
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            //instance variable that holds the last set of smoothed values
            compassVals = lowPass(event.values.clone(), compassVals);*/
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //rotationVals = lowPass(event.values.clone(), event.values);
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            if (Math.abs(Math.toDegrees(orientation[0] - angle)) > 0.8) {
                float bearing = (float) Math.toDegrees(orientation[0]) + mDeclination;
                updateCamera(bearing);
            }
            angle = Math.toDegrees(orientation[0]);
        }
        //event.values is an array of sensor values for different axes
      /*  if(accelVals != null && compassVals != null){
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R,I,accelVals,compassVals);
            if(success){
                float orientation[] = new float[3];
                SensorManager.getOrientation(R,orientation);
                //at this point orientation contains azimuth, pitch and roll values
                if(Math.abs(Math.toDegrees(orientation[0] - angle))>0.8){
                    float bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
                    if(mGoogleMap != null)
                        updateCamera(bearing);
                }
                angle = Math.toDegrees(orientation[0]);
            }
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateCamera(float bearing) {
        CameraPosition oldPos = mGoogleMap.getCameraPosition();
        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {  /*Closes the Appropriate Drawer*/
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
            System.exit(0);
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.history) {
            final TextView textViewToChange = (TextView) findViewById(R.id.toolbar_title);
            textViewToChange.setText("HISTORY");
            HistoryActivity historyActivityFragment = new HistoryActivity();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content_frame,
                    historyActivityFragment,
                    historyActivityFragment.getTag()).commit();

        } else if (id == R.id.favourites) {
            final TextView textViewToChange = (TextView) findViewById(R.id.toolbar_title);
            textViewToChange.setText("FAVOURITES");
            FavouritesActivity FavouritesActivityFragment = new FavouritesActivity();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content_frame,
                    FavouritesActivityFragment,
                    FavouritesActivityFragment.getTag()).commit();


        } else if (id == R.id.about) {
            final TextView textViewToChange = (TextView) findViewById(R.id.toolbar_title);
            textViewToChange.setText("ABOUT");
            AboutActivity AboutActivityFragment = new AboutActivity();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content_frame,
                    AboutActivityFragment,
                    AboutActivityFragment.getTag()).commit();

        } else if (id == R.id.settings) {
            final TextView textViewToChange = (TextView) findViewById(R.id.toolbar_title);
            textViewToChange.setText("SETTINGS");
            SettingsActivity SettingsActivityFragment = new SettingsActivity();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content_frame,
                    SettingsActivityFragment,
                    SettingsActivityFragment.getTag()).commit();

        } else if (id == R.id.map) {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            /*
            final TextView textViewToChange = (TextView) findViewById(R.id.toolbar_title);
            textViewToChange.setText("MapApp");
            FragmentManager manager = getSupportFragmentManager();
            MapActivity MapActivityFragment = new MapActivity();
            manager.beginTransaction().replace(R.id.content_frame,
                    MapActivityFragment,
                    MapActivityFragment.getTag()).commit();*/


        } else if (id == R.id.logout) {
            signOut();
            Intent intent = new Intent(this, mainLogin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        int i = 0;

        for (FriendsData friendsdata : FriendsDataList) {
            Log.e("DEBUG", "IM HERE");
            i++;
            if (id == i) {
                emailMeet = getEmailByName(friendsdata.getName().toString());


                android.support.v7.app.AlertDialog.Builder b = new android.support.v7.app.AlertDialog.Builder(this);
                b.setTitle("Room to meet:");
                final EditText input = new EditText(this);
                b.setView(input);
                b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        roomMeet = input.getText().toString();

                        Log.e("ROOM: ", roomMeet);
                        Log.e("EMAIL: ", emailMeet);
                        meetSend(emailMeet, roomMeet);
                    }
                });
                b.create().show();

            }
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void navigationMeet(String room) {
        location = room;
        PolylineOptions linePath = new PolylineOptions();
        //mLastLocation = location;
        //Get map on navigation mode
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        // Construct a CameraPosition focusing on current position and animate the camera to that position.
        //change camera view on current user's location to start navigation
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(21)                   // Sets the zoom
                .tilt(60)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        Graph.Node searchNode = null;
        for (Graph.Node no : nodes) {
            if (no.getLabel().equals(location.toUpperCase())) {
                searchNode = no;
                break;
            }
        }
        startNavigationTo(searchNode, mCurrentLocation);


        //Intent i = new Intent(this, Navigation.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //i.putExtra("location", locationMessage);
        //send String to navigate to room number
       /* if(location!=null && !location.isEmpty()){
            Toast.makeText(this, "Starting navigation to "+location, Toast.LENGTH_LONG).show();
        }*/
        //startActivity(i);
        //finish();
        //location.equals(null);

    }

    /**
     * Api time method to help communication
     *
     * @param room String with name of room
     */
    public void navigateToRoom(String room) {


        final int status = (Integer) testButton.getTag();
        if (status == 1) {
            navigationMeet(room);
            testButton.setBackgroundResource(R.drawable.cancelnavigation);
            navigation_on = 1;
            testButton.setTag(0);
            radio.setVisibility(View.VISIBLE);
        } else {
            testButton.setBackgroundResource(R.drawable.navigate);
            stopNavigation(mCurrentLocation);
            testButton.setTag(1);
            navigation_on = 0;
            radio.setVisibility(View.INVISIBLE);
        }

        /*
        Graph.Node searchNode = null;
        for (Graph.Node no : nodes) {
            if (no.getLabel().equals(room)) {
                searchNode = no;
                break;
            }
        }
        if (searchNode != null)
            //TODO: something
            startNavigationTo(searchNode, mCurrentLocation);
        */
    }

    //Communication

    //To meet Functions:

    /**
     * Method that receives email and room to meet and creates thread that sends a meet request
     * <p>
     * TODO: Implement it to be used by design people
     */
    public void meetSend(String mail, String room) {

        //String mail;
        //String room;
        //mail = "azgosetec@gmail.com";
        //room = "b001";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Log.e("meetSend", "Meet - if");
            meetTask = new sendMeetRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mail, room);
        } else {
            Log.e("meetSend", "Meet - else");
            meetTask = new sendMeetRequest().execute(mail, room);
        }
    }


    /**
     * Method that receives a the reply of a meet request and creats a asyncTask to send the
     * and starts the navigation
     *
     * @param replyStatus
     */
    public void meetReply(String replyStatus) {
        String[] items = TCPClient.meetRArray.peek().split("\\$");

        if (replyStatus.equals("OK")) {
            Toast.makeText(MainActivity.this, "Starting Navigation to " + items[2], Toast.LENGTH_LONG).show();
            Log.e("Meet", "Start navigation to: " + items[2]);
            navigateToRoom(items[2].toUpperCase());
        }

        String reply = items[1] + "$" + items[2] + "$" + replyStatus;
        TCPClient.meetRArray.remove();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Log.e("meetReply", "Meet - if");
            meetTask = new sendMeetReply().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, reply);
        } else {
            Log.e("meetReply", "Meet - else");
            meetTask = new sendMeetReply().execute(reply);
        }
    }


    /**
     * Async task that listens to server information
     */
    public class backgroundReception extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = TCPClient.getInstance();

            while (mTcpClient != null) {

                // Loop while doesn't receive
                while ((!mTcpClient.comunicationReceived) && (!mTcpClient.meetRStatus)
                        && (!mTcpClient.meetStatus) && !TCPClient.killme.get()) ;

                //if killme received
                if (TCPClient.killme.get()) {
                    Log.e("AsyncTask Reception:", "LoginOut");
                    signOut();
                    recAsync.cancel(true);
                }


                synchronized (lockReception) {
                    if (!TCPClient.comunicationArray.isEmpty()) {
                        Log.d("AsyncTask Reception:", "ComunicationArray: " + TCPClient.comunicationArray.peek());
                        publishProgress(TCPClient.comunicationArray.remove());
                        mTcpClient.comunicationReceived = false;
                    } else if (!TCPClient.meetRArray.isEmpty()) {
                        Log.d("AsyncTask Reception:", "meetRArray: " + TCPClient.meetRArray.peek());
                        publishProgress(TCPClient.meetRArray.peek());
                        //TCPClient.meetRArray.remove();  //will be needed to the reply
                        mTcpClient.meetRStatus = false;
                    } else if (!TCPClient.meetArray.isEmpty()) {
                        Log.d("AsyncTask Reception:", "meetArray: " + TCPClient.meetArray.peek());
                        publishProgress(TCPClient.meetArray.peek());
                        //TCPClient.meetRArray.remove();  //will be needed to the reply
                        mTcpClient.meetStatus = false;
                    } else {
                        Log.e("AsyncTask Reception:", "Unexpected Message");
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            String[] message = values[0].split("\\$");

            //Receiver cellphone:
            if (message[0].equals("MeetRequest")) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                meetReply("OK");
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                meetReply("FAIL");
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Meet with " + message[1] + " in " + message[2] + "?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                //Sending Cellphone:
            } else if (message[0].equals("Meet")) {       // Meet$email2$OK/FAIL$LAT$LON
                if (message[3].equals("FAIL")) {
                    Toast.makeText(MainActivity.this, "Meet Rejected", Toast.LENGTH_LONG).show();
                } else if (message[3].equals("OK")) {
                    Toast.makeText(MainActivity.this, "Starting Navigation to " + message[2], Toast.LENGTH_LONG).show();
                    Log.d("MainActivity", "Start Meet: " + message[1] + " in " + message[2]);
                    navigateToRoom(message[2].toUpperCase());
                }

            } else {
                Log.e("AsyncTask Reception:", "(OnProgress): Unexpected Message");
            }
        }
    }

    /**
     * Async task used to send regular Coordinates information
     */
    public class backgroundSending extends AsyncTask<String, String, String> {

        private AtomicBoolean connection = new AtomicBoolean(false);

        @Override
        protected String doInBackground(String... message) {
            //TODO: NOT sending Coordinates...
            String stringCoordinates = "";
            while (mTcpClient == null) ;
            connection.set(true);

            while (!logoutPressed) {

                try {
                    //Log.e("ASYNC", "Sending Coordinates$: " + mCurrentLocation);
                    if (mCurrentLocation != null) {
                        Double latitude_enviar = mCurrentLocation.getLatitude();

                        Double longitude_enviar = mCurrentLocation.getLongitude();
                        stringCoordinates = Double.toString(latitude_enviar) + "$" + Double.toString(longitude_enviar);
                    }

                    if (stringCoordinates != "$" && !stringCoordinates.equals("")) {
                        if (!TCPClient.connected && connection.compareAndSet(true, false)) {
                            publishProgress("");
                        } else if (TCPClient.connected) {
                            connection.set(true);
                            //mTcpClient.sendMessage("Coordinates$" + mAuth.getCurrentUser().getEmail() + "$" + stringCoordinates);
                            //Log.e("ASYNC", "Sending Coordinates$: " + stringCoordinates);
                        } else {
                            Log.e("backgroundSending", "Waiting for connection ");
                        }

                    } else {
                        Log.e("backgroundSending", "AsyncTask Sending: Wrong Coordinates");
                    }
                    Thread.sleep(2000);

                    if (logoutPressed) cancel(true);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logoutPressed = false;
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                Log.d("backgroundSending", "waitConnection - if");
                waitConnection = new lostConnection().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                Log.d("backgroundSending", "waitConnection - else");
                waitConnection = new lostConnection().execute();
            }


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            connection.set(false);
            if (mTcpClient != null) mTcpClient.stopClient();

            Log.e("backgroundSending", "CANCELED");
            return;
        }
    }

    public class backgroundLogout extends AsyncTask<String, String, String> {

//        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected String doInBackground(String... values) {


            FirebaseAuth.getInstance().signOut(); //for gmail
            LoginManager.getInstance().logOut(); //for facebook

            Log.e("SignOut", "Firebase");

            recAsync.cancel(true);
            senAsync.cancel(true);
            friAsync.cancel(true);

            Log.e("SignOut", "Async Cancel " + logoutPressed);

            if (mTcpClient != null) {
                mTcpClient.stopClient();
                mTcpClient = null;
            }

            //while(mTcpClient != null)


            Log.e("SignOut", "GoingTo login" + logoutPressed);
            return null;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Thread.currentThread().setName("Logout-async");
            //this.dialog.setMessage("Login out...");
            //this.dialog.show();
            Log.e("AsyncTask", "Processing created");
        }

        @Override
        protected void onPostExecute(String value) {
            super.onPostExecute(value);

            Log.e("AsyncTask", "onPostExecute");

            //this.dialog.dismiss();
            goLoginScreen();
            if (this.isCancelled()) cancel(true);

        }
    }

    public class lostConnection extends AsyncTask<String, String, String> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected String doInBackground(String... values) {


            Log.e("AsyncTask", "Login out");

            signOut();

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Thread.currentThread().setName("Logout-async");
            //this.dialog.setMessage("Reconnecting...");
            //this.dialog.show();
            Log.e("AsyncTask", "Processing created");
        }

        @Override
        protected void onPostExecute(String value) {
            super.onPostExecute(value);
            Toast.makeText(MainActivity.this, "Lost connection, LoginOut",
                    Toast.LENGTH_SHORT).show();
            //this.dialog.dismiss();

        }
    }

    /**
     * Send Meet request to another cellphone
     * toSend[0] - email
     * toSend[1] - room to meet
     * <p>
     * There is no need to make a message to wait,
     * Maybe to much information
     */
    public class sendMeetRequest extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... toSend) {
            while (mTcpClient == null) ;

            Log.e("ASYNC", "Sending MeetRequest to: " + toSend[0] + toSend[1]);
            mTcpClient.meetStatus = false;
            mTcpClient.sendMessage("Meet$" + toSend[0] + "$" + toSend[1]);

            //Message for user is not needed
            //while (!mTcpClient.meetStatus) publishProgress("Waiting");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            Log.e("ASYNC", "CANCELED: ");
            return;
        }
    }


    public class sendMeetReply extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... reply) {
            while (mTcpClient == null) ;

            Log.e("ASYNC", "Sending MeetReply to: " + reply[0]);
            //mTcpClient.meetStatus = true;
            mTcpClient.sendMessage("MeetRequest$" + reply[0]);

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            Log.e("ASYNC", "CANCELED: ");
            return;
        }
    }

    public class backgroundSendFriends extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... message) {
            while (true) {
                while (mTcpClient == null) ;
                while (friendsready == false) ;

                if (friends != "Friends") {
                    mTcpClient.sendMessage(friends);
                    Log.e("AsyncFriends", "Sending Friends: " + friends);
                } else {
                    Log.e("AsyncFriends", "Error sending Friends");
                }

                while (!mTcpClient.friendsReceived) ;

                if (!TCPClient.friendsArray.isEmpty()) {

                    publishProgress(TCPClient.friendsArray.peek());
                    TCPClient.friendsArray.remove();
                    mTcpClient.friendsReceived = false;
                } else {
                    Log.e("AsyncFriends", "Reception Friends: Error");
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            synchronized (lockfriends) {


                String[] items = values[0].split("\\$"); //values[0] está a mensagem toda do server

                //ToDebug:

                /*
                String[] items = new String[6];
                for (int i = 0; i < 6; i++) {
                    items[i] = "ola" + i + "#ole" + i + "#oli" + i;
                    Log.d("FrindsAsinc", items[i]);
                }
                */

                int i = 0;
                menuRight.clear();
                menuRight.add(0,0,0,"FRIENDS");
                FriendsDataList.clear();
                for (String item : items) {

                    if (i != 0) { //ignore first message
                        String[] data = item.split("\\#");
                        FriendsData<String, String, String> trio = new FriendsData<>(data[0], data[1], data[2]);

                        menuRight.add(0, i, 0, "   " + data[0]);
                        FriendsDataList.add(trio);
                        Log.e("DEBUG", "i actual: "+i);




                        /*
                        boolean contains = false;
                        for (int x = 0; x < menuRight.size(); x++) {
                            if (trio.getName().equals(menuRight.getItem(x).getTitle())) {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            menuRight.add(0, i, 0, data[0]);
                            FriendsDataList.add(trio);
                            contains = false;
                        }
                        */

                    }
                    i++;
                }


                //Message = FriendsDataList; //required: java.lang.string <-> found: java.util.list

                //messageReceived = true;

            }


            //to match a name to a email:
            //newName - name to meet
            /*
            for (FriendsData friendsdata : FriendsDataList) {
                if (newName == friendsdata.getName())
                    return friendsdata.getEmail();
            }
            */


            for (FriendsData friendsdata : FriendsDataList) {

                Log.e("AsyncFriends", "onProgressUpdate: " + friendsdata.getName() + friendsdata.getEmail() + friendsdata.getNumber());

            }


        }

    }

    public String getEmailByName(String newName) {
        for (FriendsData friendsdata : FriendsDataList) {
            if (newName == friendsdata.getName())
                return friendsdata.getEmail().toString();
        }
        return null;
    }


    //if on navigation mode
    private LatLng findNearestPoint(LatLng test, List<LatLng> target) {
        double distance = -1;
        LatLng minimumDistancePoint = test;
        if (test == null || target == null) {
            return minimumDistancePoint;
        }
        for (int i = 0; i < target.size(); i++) {
            LatLng point = target.get(i);
            int segmentPoint = i + 1;
            if (segmentPoint >= target.size()) {
                segmentPoint = 0;
            }
            double currentDistance = PolyUtil.distanceToLine(test, point, target.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                minimumDistancePoint = findNearestPoint(test, point, target.get(segmentPoint));
            }
        }
        return minimumDistancePoint;
    }


    private LatLng findNearestPoint(final LatLng p, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return start;
        }
        final double s0lat = Math.toRadians(p.latitude);
        final double s0lng = Math.toRadians(p.longitude);
        final double s1lat = Math.toRadians(start.latitude);
        final double s1lng = Math.toRadians(start.longitude);
        final double s2lat = Math.toRadians(end.latitude);
        final double s2lng = Math.toRadians(end.longitude);
        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = (((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng) / (s2s1lat * s2s1lat + s2s1lng * s2s1lng));
        if (u <= 0) {
            return start;
        }
        if (u >= 1) {
            return end;
        }
        return new LatLng(start.latitude + (u * (end.latitude - start.latitude)), start.longitude + (u * (end.longitude - start.longitude)));
    } //the solution will give a point on a segment of a Polygon that is the closest point to the test point


}