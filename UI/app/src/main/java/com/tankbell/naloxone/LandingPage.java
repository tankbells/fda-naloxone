/*
 *  Copyright 2016 TankBell Technologies.
 *  All Rights Reserved.
 */

package com.tankbell.naloxone;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.provider.Settings;
import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.lambdainvoker.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

public class LandingPage extends AppCompatActivity {
    private final String TAG="MainActivity";

    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private ListView attributesList;

    // Cognito user objects
    private CognitoUser user;
    private CognitoUserSession session;
    private CognitoUserDetails details;

    // User details
    private String username;

    // To track changes to user details
    private final List<String> attributesToDelete = new ArrayList<>();

    //TankBell Technologies
    Button submitButton;
    //GPSTracker gps;
    private static final int HANDLER_DELAY = 1000*60*5;

    private UserDetailsCache cache;

    GPSTracker gps;

    public static AmazonClientManager clientManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(com.tankbell.naloxone.R.id.main_toolbar);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(com.tankbell.naloxone.R.id.main_toolbar_title);
        main_title.setText("Naloxone");
        setSupportActionBar(toolbar);

        // Set navigation drawer for this screen
        mDrawer = (DrawerLayout) findViewById(com.tankbell.naloxone.R.id.landing_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer, toolbar, com.tankbell.naloxone.R.string.nav_drawer_open, com.tankbell.naloxone.R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        nDrawer = (NavigationView) findViewById(com.tankbell.naloxone.R.id.nav_view);
        setNavDrawer();
        init();
        View navigationHeader = nDrawer.getHeaderView(0);
        TextView navHeaderSubTitle = (TextView) navigationHeader.findViewById(com.tankbell.naloxone.R.id.textViewNavUserSub);
        navHeaderSubTitle.setText(username);

        clientManager = new AmazonClientManager(this);
        startGPSService(GPSTracker.TAG);

        submitButton = (Button) findViewById(R.id.submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backendLambda();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.tankbell.naloxone.R.menu.activity_user_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                performAction(item);
                return true;
            }
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
        // Close the navigation drawer
        mDrawer.closeDrawers();

        // Find which item was selected
        switch(item.getItemId()) {
            case R.id.nav_user_update_quantity:
                updateQuantity();
                break;
            case R.id.nav_user_update_emergency_contact:
                updateEmergencyContact();
                break;
            case com.tankbell.naloxone.R.id.nav_user_change_password:
                // Change password
                changePassword();
                break;
            case com.tankbell.naloxone.R.id.nav_user_sign_out:
                // Sign out from this account
                signOut();
                break;
            case com.tankbell.naloxone.R.id.nav_user_about:
                // For the inquisitive
                Intent aboutAppActivity = new Intent(this, AboutApp.class);
                startActivity(aboutAppActivity);
                break;
        }
    }

    private void updateEmergencyContact() {
        Intent updateEmergencyContact = new Intent(this, UpdateEmergencyContact.class);
        startActivity(updateEmergencyContact);
    }

    private void updateQuantity() {
        Intent updateQuant = new Intent(this, UpdateQuantity.class);
        startActivity(updateQuant);
    }

    // Change user password
    private void changePassword() {
        Intent changePssActivity = new Intent(this, ChangePasswordActivity.class);
        startActivity(changePssActivity);
    }

    // Sign out user
    private void signOut() {
        stopGPSService(GPSTracker.TAG);
        user.signOut();
        exit();
    }

    // Initialize this activity
    private void init() {
        // Get the user name
        Bundle extras = getIntent().getExtras();
        username = AppHelper.getCurrUser();
        user = AppHelper.getPool().getUser(username);
    }

    private void exit () {
        Intent intent = new Intent();
        if(username == null)
            username = "";
        intent.putExtra("name",username);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void startGPSService(final String tag) {
        Intent i =new Intent(getApplicationContext(),GPSTracker.class);
        i.addCategory(tag);
        startService(i);
    }

    private void stopGPSService(final String tag) {
        Intent i =new Intent(getApplicationContext(),GPSTracker.class);
        i.addCategory(tag);
        stopService(i);
    }

    private void backendLambda() {
        // Create an instance of CognitoCachingCredentialsProvider
        CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
                this.getApplicationContext(), "your_key_here", Regions.US_EAST_1);

        // Create LambdaInvokerFactory, to be used to instantiate the Lambda proxy.
        LambdaInvokerFactory factory = new LambdaInvokerFactory(this.getApplicationContext(),
                Regions.US_EAST_1, cognitoProvider);

        final MyInterface myInterface = factory.build(MyInterface.class);

        //RequestClass request = new RequestClass("John", "Doe");
        RequestClass request = new RequestClass(AppHelper.getCurrUser());

        // The Lambda function invocation results in a network call.
        // Make sure it is not called from the main thread.
        new AsyncTask<RequestClass, Void, String>() {
        //new AsyncTask<Void, Void, String>() {
            //new AsyncTask<RequestClass, Void, String>() {
            @Override
            protected String doInBackground(RequestClass... params) {
                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    return myInterface.AndroidBackendLambdaFunction(params[0]);
                    //return myInterface.AndroidBackendLambdaFunction(params[0]);
                } catch (LambdaFunctionException lfe) {
                    Log.e("Tag", "Failed to invoke echo", lfe);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    return;
                }

                // Do a toast
                Toast.makeText(LandingPage.this, result, Toast.LENGTH_LONG).show();
            }
            //}.execute(request);
        }.execute(request);

    }

}
