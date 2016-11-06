package com.tankbell.naloxone;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class UpdateQuantity extends AppCompatActivity {

    private EditText age;
    private EditText qt;

    private String userAge;
    private String userQt;

    private Button updateButton;

    public static AmazonClientManager clientManager = null;

    private UserDetailsCache cache;

    private DynamoDBManager.UserDetails userDetails;
    private DynamoDBManager.UserLocation userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        clientManager = new AmazonClientManager(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_quantity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarUpdateQuant);
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

        TextView main_title = (TextView) findViewById(com.tankbell.naloxone.R.id.update_quant_toolbar_title);
        main_title.setText("Update");

        init();
    }

    private void init() {

        age = (EditText) findViewById(R.id.editTextAge);
        age.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewAgeLabel);
                    label.setText(age.getHint());
                    age.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewAgeMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewAgeLabel);
                    label.setText("");
                }
            }
        });


        qt = (EditText) findViewById(R.id.editTextQt);
        qt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewQtLabel);
                    label.setText(qt.getHint());
                    qt.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewQtMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewQtLabel);
                    label.setText("");
                }
            }
        });


        updateButton = (Button) findViewById(R.id.update_button);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userAge = age.getText().toString();
                Log.d("Age", "UserAge : " + userAge);

                userQt = qt.getText().toString();
                Log.d("Quant","Qt : " + userQt);
                //Toast.makeText(UpdateQuantity.this,"DAWG", Toast.LENGTH_SHORT).show();
                cache.setUserName(AppHelper.getCurrUser());
                cache.setAge(Integer.parseInt(userAge));
                cache.setQuantity(Integer.parseInt(userQt));
                new DynamoDBManagerTask()
                        .execute(DynamoDBManagerType.READ_LOCATION);

            }
        });

    }

    private class DynamoDBManagerTask extends
            AsyncTask<DynamoDBManagerType, Void, DynamoDBManagerTaskResult> {

        protected DynamoDBManagerTaskResult doInBackground(
                DynamoDBManagerType... types) {

            DynamoDBManagerTaskResult result = new DynamoDBManagerTaskResult();

            if (types[0] == DynamoDBManagerType.UPDATE_USER) {
                String tableStatus = DynamoDBManager.getTableStatusUpdateQt();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DynamoDBManager.updateUserQuant(cache);
                }
            } else if (types[0] == DynamoDBManagerType.READ_LOCATION) {
                String tableStatus = DynamoDBManager.getNLXTableStatusFromUpdate();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    userLocation = DynamoDBManager.getUserLocationFromUpdate(AppHelper.getCurrUser());
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
                String tableStatus = DynamoDBManager.getNLXUserDetailTableStatusFromUpdate();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    userDetails = DynamoDBManager.getUserDetailsFromUpdate(AppHelper.getCurrUser());
                }
            } else if (types[0] == DynamoDBManagerType.UPDATE_NLX) {
                String tableStatus = DynamoDBManager.getNLXTableStatusFromUpdate();
                result.setTableStatus(tableStatus);
                result.setTaskType(types[0]);
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DynamoDBManager.updateUserDetailsQuantity(cache);
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
