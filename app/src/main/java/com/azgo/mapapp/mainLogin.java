package com.azgo.mapapp;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class mainLogin extends AppCompatActivity implements
        View.OnClickListener {

    //General
    private String TAG = "Login Activity";
    private static final int RC_SIGN_IN = 9001;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private CallbackManager mCallbackManager;

    //google
    private GoogleApiClient mGoogleApiClient;

    //server
    private TCPClient mTcpClient;
    private static String Message;
    private static boolean messageReceived;
    private static boolean errorLogin = false;
    private static String sessionID;
    private static AtomicBoolean asyncEnable = new AtomicBoolean(false);

    //telefone
    String mPhoneNumber;

    //Async Task
    public AsyncTask login;
    public AsyncTask reception;

    private Object lock1 = new Object();
    private Object lockmess = new Object();

    private SharedPreferences myPrefs;
    private SharedPreferences.Editor myPrefsEditor;
    //MISC
    private ProgressBar mProgress;

    //Facebook
    CallbackManager callbackManager = null;

    Button fb = null;
    LoginButton loginButton = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

         /* Load the view and display it */
        super.onCreate(savedInstanceState);
        Log.e("mainLogin", "STARTING");

        //Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);


        //get permission for contacts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            getPermissionToReadUserContacts();
        }



        //get permission for contacts
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
            getPermissionToReadPhoneState();
*/

        myPrefs = this.getSharedPreferences(
                "com.azgo.mapapp", Context.MODE_PRIVATE);


        //get phone number from preferences
        if ((mPhoneNumber = myPrefs.getString("number", "ERROR")).equals("ERROR")) {
            //get phone number
            showDialogGetPhoneNumber();
        }
        Log.e("PhoneNumber: ", mPhoneNumber);

/*
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainLogin.this);
        builder.setMessage("Phone Number: ");
        final EditText input = new EditText(mainLogin.this);
        builder.setView(input);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        mPhoneNumber = input.getText().toString();
                    }
                });
*/
        //if (mPhoneNumber == )
        //can return null


        //SERVER
        /*
        if (mTcpClient == null) {
            Log.e("mainLogin", "Connecting to Server");

            //AsyncTask<String,String,TCPClient> connectTask = new connectTask();
            //AsyncTaskCompat.executeParallel(connectTask, "");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                Log.e("mainLogin", "Async Task: connectTask - if");
                reception = new connectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                Log.e("mainLogin", "Async Task: connectTask - else");
                reception = new connectTask().execute("");
            }
        }
        */

        //***///
        //findViewById(R.id.button_facebook_login).setOnClickListener(this);


/* TOGET: Hash code
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo("com.azgo.mapapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
*/

        /**
         * FOR GOOGLE
         */
        findViewById(R.id.gmail_sign_in_button).setOnClickListener(this);
        findViewById(R.id.button_facebook_login).setOnClickListener(this);

        //new connectTask().execute(""); //AQUI?
        /*********************************
         *       FIREBASE
         ***************************/
        Log.d("mainLogin", "Starting Facebook");
        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    if(!isNetworkConnected()) displayAlert("Disconnected, please connect");
                    Log.e(TAG, "onAuthStateChanged(): signed_in: userID: " + user.getUid());
                    goMainScreen();
                } else {
                    // User is signed out
                    Log.e(TAG, "onAuthStateChanged(): signed_out");
                }
                // ...
            }
        };

        /**************
         * FACEBOOK
         ************/
        Log.d("mainLogin", "Starting Facebook");
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "facebook:onCancel");
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "facebook:onError", error);
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        });


        /**
         *  GMAIL
         */

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null) //TODO: Ver como resolver isto
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppIndex.API).build();


    }


    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "FACEBOOK: signInWithCredential:onComplete:STARTING");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "FACEBOOK: signInWithCredential:onComplete:"
                                + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(mainLogin.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        } else goMainScreen();
                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_facebook]

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.e(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.e(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(mainLogin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Log.e(TAG, "LOGINGOGGLE: DONE");
                            goMainScreen();
                        }
                        // ...
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e(TAG, "onActivityResult() "+requestCode);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.e(TAG, "LOGINGOGGLE: toCheck");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.e(TAG, "LOGINGOGGLE: checking");
            if (result.isSuccess()) {
                Log.e(TAG, "LOGINGOGGLE: True");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Log.e(TAG, "ACCOUNT: " + account.toString());
            } else {
                Log.e(TAG, "LOGINGOOGLE: False");
                Toast.makeText(mainLogin.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();

            }
        } else {
            //If not request code is RC_SIGN_IN it must be facebook
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void signIn() {
        Log.e(TAG, "signIn(): GOOGLE");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.e("mainLogin ", "signIn() END");
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d("mainLogin", "onStart()");
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("mainLogin", "onStop()");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        mGoogleApiClient.disconnect();
    }


    public void onClick(View v) {
        if(!isNetworkConnected()) displayAlert("Disconnected, please connect");
        int i = v.getId();
        Log.d(TAG, "onClick" + i);
        if (i == R.id.gmail_sign_in_button) {
            signIn();
        } else if (i == R.id.button_facebook_login){
            loginButton.performClick();
        }
    }

    private void goMainScreen() {

        Log.e("mainLogin", "goMainScreen()");

        /*
        if(mTcpClient == null)  {
            Log.e("mainLogin", "goMainScreen(): mTcpClient == null");
            AsyncTask<String,String,TCPClient> connectTask = new connectTask();
            AsyncTaskCompat.executeParallel(connectTask, "");
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new connectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                new connectTask().execute("");
            }
            */
        //}

        if (mTcpClient == null) {
            Log.e("mainLogin", "Connecting to Server");

            //AsyncTask<String,String,TCPClient> connectTask = new connectTask();
            //AsyncTaskCompat.executeParallel(connectTask, "");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                Log.e("mainLogin", "Async Task: connectTask - if");
                reception = new connectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                Log.e("mainLogin", "Async Task: connectTask - else");
                reception = new connectTask().execute("");
            }
        }

        Log.e("mainLogin", "Async Task - login");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Log.e("mainLogin", "Async Task: login - if");
            login = new login().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
            Log.e("mainLogin", "Async Task: login - else");
            login = new login().execute("");

        }


    }


    public class connectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            Thread.currentThread().setName("Login-connectTask-doInBackground");

            //we create a TCPClient object and
            Log.e("AsyncTask [" + Thread.currentThread().getId() + "]",
                    "connectTask: Creating mTcpClient");
            synchronized (lock1) {
                mTcpClient = TCPClient.getInstance();
            }
            Log.e("AsyncTask [" + Thread.currentThread().getId() + "]",
                    "connectTask: returned mTcpClient");


            while (!mTcpClient.connected) { //Best way to do?
                if (mTcpClient.socketTimeout) {
                    Log.e("connectTask [" + Thread.currentThread().getId() + "]", "return");
                    mTcpClient = null;
                    return mTcpClient;
                }
            }

            Log.e("AsyncTask [" + Thread.currentThread().getId() + "]",
                    "connectTask: mTcpClient is now Connected");
            //espera enquanto nao recebe nada

            synchronized (lock1) {
                while (!mTcpClient.loginReceived) ;


                mTcpClient.loginReceived = false;

                Log.e("AsyncTask [" + Thread.currentThread().getId() + "]",
                        "connectTask: mTcpClient.array= " + TCPClient.loginArray.peek());

                try {
                    publishProgress(TCPClient.loginArray.remove());
                } catch (NoSuchElementException e) {
                    Log.e("AsyncTask [" + Thread.currentThread().getId() + "]",
                            "Easy Fix");
                }
                mTcpClient.loginReceived = false;
            }


            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            Log.d("AsyncTask", values[0]);

            synchronized (lockmess) {
                Message = values[0];
                messageReceived = true;
            }
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list


        }

        @Override
        protected void onPostExecute(TCPClient tcpClient) {
            super.onPostExecute(tcpClient);
            Log.e("connectTask-AsyncTask  [" + Thread.currentThread().getId() + "| " +
                    Thread.currentThread().getName() + "]", "Cenas " + mTcpClient);
            if (mTcpClient == null) {
                errorLogin = true;
                displayAlert("Connection problems");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e("connectTask-AsyncTask  [ " + Thread.currentThread().getId() + " | " +
                    Thread.currentThread().getName() + " ]", "Canceled");
            errorLogin = true;
            if (mTcpClient == null) displayAlert("Connection error, check internet connection");
        }
    }

    public class login extends AsyncTask<String, String, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Thread.currentThread().setName("Login-async");
            pDialog = new ProgressDialog(mainLogin.this);
            pDialog.setMessage("Authenticating user...");

            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            Log.e("AsyncTask", "Processing created");
        }

        @Override
        protected String doInBackground(String... message) {

            if (asyncEnable.get()) {
                Log.e("Asinc Login" + Thread.currentThread().getId(), "Returning");
                login.cancel(true);
                Thread.currentThread().interrupt();
                return null;
            } else {
                asyncEnable.set(true);
            }

            Thread.currentThread().setName("Login-doInBackground");
            Log.e("login-AsyncTask [ " + Thread.currentThread().getId() + " | " +
                    Thread.currentThread().getName() + " ]", "login(): Entering while");


            while (true) {
                Log.d("login-AsyncTask  [ " + Thread.currentThread().getId() + " | " +
                        Thread.currentThread().getName() + " ]", "checking mTCPClient: "
                        + mTcpClient);




                while (mTcpClient == null) if (errorLogin) return "False";

                Log.d("login-AsyncTask  [ " + Thread.currentThread().getId() + " | " +
                        Thread.currentThread().getName() + " ]", "Checking status: " + mTcpClient.checkForSocketStatus());

                Log.d("login-AsyncTask  [ " + Thread.currentThread().getId() + " | " +
                        Thread.currentThread().getName() + " ]", "Checking: " + !(mTcpClient != null && mTcpClient.checkForSocketStatus()));

                while (!(mTcpClient != null && mTcpClient.checkForSocketStatus())) {
                    if (errorLogin) return "False";
                }


                //Wait for phoneNumber...
                while (Objects.equals(mPhoneNumber, "ERROR") || mPhoneNumber == null) ;

                Log.d("login-AsyncTask  [ " + Thread.currentThread().getId() + " | " +
                        Thread.currentThread().getName() + " ]", "Sending login to server "
                );
                mTcpClient.sendMessage("Login$" + mAuth.getCurrentUser().getDisplayName()
                        + "$" + mAuth.getCurrentUser().getEmail() + "$" + mPhoneNumber);

                // Waits for the server response
                while (!messageReceived) ;
                Log.e("login-AsyncTask [ " + Thread.currentThread().getId() + " | " +
                        Thread.currentThread().getName() + " ]", "Message Received");
                messageReceived = false;


                String[] items = Message.split("\\$");
                if (items[0].equals("Login")) {
                    //To save..
                    sessionID = items[1];
                    break;
                } else {
                    //Unexpected else
                    Toast.makeText(mainLogin.this, "Problems with authentication, try again later",
                            Toast.LENGTH_SHORT).show();
                    return null;
                }

            }

            return "True";

        }


        @Override
        protected void onPostExecute(String value) {
            super.onPostExecute(value);

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (value == null) {
                Log.e("login-AsyncTask", "Returning without entering");
                return;
            }
            Log.e("AsyncTask-login", "onPostExecute");
            if (value.equals("True")) {
                reception.cancel(true);
                Log.e("AsyncTask", "onPostExecute");
                Intent intent = new Intent(mainLogin.this, MainActivity.class);
                intent.putExtra("sessionId", sessionID);
                asyncEnable.set(false);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (this.isCancelled()) cancel(true);
            } else {

                Log.e("AsyncTask", "login() mTcpClient == null");
            }
        }
    }


    private void displayAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setCancelable(
                false).setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        mainLogin.this.finish();
                        dialog.cancel();
                        System.exit(1);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    void showDialogGetPhoneNumber() {


        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Please enter your number");
        final EditText input = new EditText(this);
        b.setView(input);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // SHOULD NOW WORK
                Log.e("ALERT: ", input.getText().toString());
                myPrefsEditor = myPrefs.edit();
                mPhoneNumber = input.getText().toString();
                myPrefsEditor.putString("number", mPhoneNumber);
                myPrefsEditor.commit();
            }
        });
        b.create().show();
    }


    //For Preferences


    // Identifier for the permission request
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;

    // Called when the user is performing an action which requires the app to read the
    // user's contacts
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadUserContacts() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (shouldShowRequestPermissionRationale(
                    android.Manifest.permission.READ_CONTACTS)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    // Called when the user is performing an action which requires the app to read the
    // user's contacts

    // Identifier for the permission request
    private static final int READ_PHONE_STATE_PERMISSIONS_REQUEST = 1;

    // Called when the user is performing an action which requires the app to read the
    // user's contacts
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadPhoneState() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (shouldShowRequestPermissionRationale(
                    android.Manifest.permission.READ_PHONE_STATE)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    READ_PHONE_STATE_PERMISSIONS_REQUEST);
        }
    }




}
