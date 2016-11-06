package com.tankbell.naloxone;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UpdateEmergencyContact extends AppCompatActivity {

    private EditText contactA;
    private EditText contactB;
    private EditText contactC;

    private String emergencyA;
    private String emergencyB;
    private String emergencyC;

    private Button updateEmergencyContactButton;

    public static AmazonClientManager clientManager = null;

    private UserDetailsCache cache;

    private DynamoDBManager.UserDetails userDetails;
    private DynamoDBManager.UserLocation userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        clientManager = new AmazonClientManager(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_emergency_contact);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarUpdateEmergencyContact);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView main_title = (TextView) findViewById(R.id.update_emer_contact_toolbar_title);
        main_title.setText("Update Emergency Contacts");

        init();
    }

    private void init() {

        contactA = (EditText) findViewById(R.id.editTextContactA);
        contactA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewContactALabel);
                    label.setText(contactA.getHint());
                    contactA.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewContactAMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewContactALabel);
                    label.setText("");
                }
            }
        });

        contactB = (EditText) findViewById(R.id.editTextContactB);
        contactB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewContactBLabel);
                    label.setText(contactB.getHint());
                    contactB.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewContactBMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewContactBLabel);
                    label.setText("");
                }
            }
        });

        contactC = (EditText) findViewById(R.id.editTextContactC);
        contactC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewContactCLabel);
                    label.setText(contactC.getHint());
                    contactC.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewContactCMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewContactCLabel);
                    label.setText("");
                }
            }
        });

        updateEmergencyContactButton = (Button) findViewById(R.id.update_emergency_button);

        updateEmergencyContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emergencyA = contactA.getText().toString();
                emergencyB = contactB.getText().toString();
                emergencyC = contactC.getText().toString();
                cache.setUserName(AppHelper.getCurrUser());
                cache.setContactA(emergencyA);
                cache.setContactB(emergencyB);
                cache.setContactC(emergencyC);
                new DynamoDBManagerTask()
                        .execute(DynamoDBManagerType.READ_LOCATION);

            }
        });
    } // End init()

    private class DynamoDBManagerTask extends
            AsyncTask<DynamoDBManagerType, Void, DynamoDBManagerTaskResult> {

        protected DynamoDBManagerTaskResult doInBackground(
                DynamoDBManagerType... types) {

            DynamoDBManagerTaskResult result = new DynamoDBManagerTaskResult();

            if (types[0] == DynamoDBManagerType.UPDATE_USER) {
                String tableStatus = DynamoDBManager.getTableStatusEmergencyContacts();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DynamoDBManager.updateUserEmergencyContact(cache);
                }
            } else if (types[0] == DynamoDBManagerType.READ_LOCATION) {
                String tableStatus = DynamoDBManager.getNLXTableStatusFromEmergency();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    userLocation = DynamoDBManager.getUserLocationFromEmergency(AppHelper.getCurrUser());
                    // If for some reason , the table NLX does not exist for the user.
                    // This may happen if for example the user has some GPS problems.
                    // Handle this gracefully. Set the latitude and longitude to 0 and
                    // update the 'NLX' table.
                    // Later , when the GPS is working , the tables will come to a consistent
                    // state from GPSTracker.java.
                    if (userLocation == null) {
                        userLocation = new DynamoDBManager.UserLocation();
                        userLocation.setLatitude(new Double(0));
                        userLocation.setLongitude(new Double(0));
                    }
                }
            } else if (types[0] == DynamoDBManagerType.READ_USR_DETAIL) {
                String tableStatus = DynamoDBManager.getNLXUserDetailTableStatusFromEmergency();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    userDetails = DynamoDBManager.getUserDetailsFromEmergency(AppHelper.getCurrUser());
                }
            } else if (types[0] == DynamoDBManagerType.UPDATE_NLX) {
                String tableStatus = DynamoDBManager.getNLXTableStatusFromEmergency();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DynamoDBManager.updateUserDetailsEmergency(cache);
                }
            }

            return result;
        }

        protected void onPostExecute(DynamoDBManagerTaskResult result) {

            if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == DynamoDBManagerType.UPDATE_USER) {
                //Toast.makeText(GPSTracker.this,
                //        "Users inserted successfully!", Toast.LENGTH_SHORT).show();
                cache.setLatitude(userLocation.getLatitude());
                cache.setLongitude(userLocation.getLongitude());
                cache.setGivenname(userDetails.getGivenname());
                cache.setPhoneNo(userDetails.getPhoneNumber());
                cache.setEmailAddr(userDetails.getEmailId());
                new DynamoDBManagerTask().execute(DynamoDBManagerType.UPDATE_NLX);
            } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == DynamoDBManagerType.READ_LOCATION) {
                new DynamoDBManagerTask().execute(DynamoDBManagerType.READ_USR_DETAIL);
            } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == DynamoDBManagerType.READ_USR_DETAIL) {
                new DynamoDBManagerTask().execute(DynamoDBManagerType.UPDATE_USER);
            }
        }
    }

    private enum DynamoDBManagerType {
        UPDATE_USER,READ_LOCATION,READ_USR_DETAIL,UPDATE_NLX
    }

    private class DynamoDBManagerTaskResult {
        private DynamoDBManagerType taskType;
        private String tableStatus;

        public DynamoDBManagerType getTaskType() {
            return taskType;
        }

        public void setTaskType(DynamoDBManagerType taskType) {
            this.taskType = taskType;
        }

        public String getTableStatus() {
            return tableStatus;
        }

        public void setTableStatus(String tableStatus) {
            this.tableStatus = tableStatus;
        }
    }

}
