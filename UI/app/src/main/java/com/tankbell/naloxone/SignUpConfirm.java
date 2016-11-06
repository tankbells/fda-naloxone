/*
 *  Copyright 2013-2016 Amazon.com,
 *  Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Amazon Software License (the "License").
 *  You may not use this file except in compliance with the
 *  License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 *  or in the "license" file accompanying this file. This file is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, express or implied. See the License
 *  for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tankbell.naloxone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import android.util.Log;
import android.widget.Toast;

public class SignUpConfirm extends AppCompatActivity {
    private EditText username;
    private EditText confCode;

    private Button confirm;
    private TextView reqCode;
    private String userName;
    private String phoneNumber;
    private String emailAddress;
    private String patientName;
    private AlertDialog userDialog;

    private UserDetailsCache cache;

    public static AmazonClientManager clientManager = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.tankbell.naloxone.R.layout.activity_sign_up_confirm);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(com.tankbell.naloxone.R.id.toolbar);
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

        TextView main_title = (TextView) findViewById(com.tankbell.naloxone.R.id.confirm_toolbar_title);
        main_title.setText("Confirm");

        clientManager = new AmazonClientManager(this);

        init();
    }

    private void init() {

        Bundle extras = getIntent().getExtras();
        if (extras !=null) {
            if (extras.containsKey("ph")) {
                phoneNumber = extras.getString("ph");
                Log.d("SignUpConfirm","PhoneNumber: " + phoneNumber.toString());
            }
            if (extras.containsKey("eM")) {
                emailAddress = extras.getString("eM");
                Log.d("SignUpConfirm","Email: " + emailAddress.toString());
            }
            if (extras.containsKey("gN")) {
                patientName = extras.getString("gN");
                Log.d("SignUpConfirm","Given Name: " + patientName.toString());
            }
            if(extras.containsKey("name")) {
                userName = extras.getString("name");
                username = (EditText) findViewById(com.tankbell.naloxone.R.id.editTextConfirmUserId);
                username.setText(userName);

                confCode = (EditText) findViewById(com.tankbell.naloxone.R.id.editTextConfirmCode);
                confCode.requestFocus();

                if(extras.containsKey("destination")) {
                    String dest = extras.getString("destination");
                    String delMed = extras.getString("deliveryMed");

                    TextView screenSubtext = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmSubtext_1);
                    if(dest != null && delMed != null && dest.length() > 0 && delMed.length() > 0) {
                        screenSubtext.setText("A confirmation code was sent to "+dest+" via "+delMed);
                    }
                    else {
                        screenSubtext.setText("A confirmation code was sent");
                    }
                }
            }
            else {
                TextView screenSubtext = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmSubtext_1);
                screenSubtext.setText("Request for a confirmation code or confirm with the code you already have.");
            }

        }

        username = (EditText) findViewById(com.tankbell.naloxone.R.id.editTextConfirmUserId);
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmUserIdLabel);
                    label.setText(username.getHint());
                    username.setBackground(getDrawable(com.tankbell.naloxone.R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmUserIdMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmUserIdLabel);
                    label.setText("");
                }
            }
        });

        confCode = (EditText) findViewById(com.tankbell.naloxone.R.id.editTextConfirmCode);
        confCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmCodeLabel);
                    label.setText(confCode.getHint());
                    confCode.setBackground(getDrawable(com.tankbell.naloxone.R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmCodeMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmCodeLabel);
                    label.setText("");
                }
            }
        });

        confirm = (Button) findViewById(com.tankbell.naloxone.R.id.confirm_button);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendConfCode();
            }
        });

        reqCode = (TextView) findViewById(com.tankbell.naloxone.R.id.resend_confirm_req);
        reqCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reqConfCode();
            }
        });
    }


    private void sendConfCode() {
        userName = username.getText().toString();
        String confirmCode = confCode.getText().toString();

        if(userName == null || userName.length() < 1) {
            TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmUserIdMessage);
            label.setText(username.getHint()+" cannot be empty");
            username.setBackground(getDrawable(com.tankbell.naloxone.R.drawable.text_border_error));
            return;
        }

        if(confirmCode == null || confirmCode.length() < 1) {
            TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmCodeMessage);
            label.setText(confCode.getHint()+" cannot be empty");
            confCode.setBackground(getDrawable(com.tankbell.naloxone.R.drawable.text_border_error));
            return;
        }

        AppHelper.getPool().getUser(userName).confirmSignUpInBackground(confirmCode, true, confHandler);
    }

    private void reqConfCode() {
        userName = username.getText().toString();
        if(userName == null || userName.length() < 1) {
            TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmUserIdMessage);
            label.setText(username.getHint()+" cannot be empty");
            username.setBackground(getDrawable(com.tankbell.naloxone.R.drawable.text_border_error));
            return;
        }
        AppHelper.getPool().getUser(userName).resendConfirmationCodeInBackground(resendConfCodeHandler);

    }

    GenericHandler confHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            showDialogMessage("Success!",userName+" has been confirmed!", true);
            cache.setUserName(userName);
            cache.setPhoneNo(phoneNumber);
            cache.setEmailAddr(emailAddress);
            cache.setGivenname(patientName);
            new DynamoDBManagerTask()
                    .execute(DynamoDBManagerType.INSERT_USER);
        }

        @Override
        public void onFailure(Exception exception) {
            TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmUserIdMessage);
            label.setText("Confirmation failed!");
            username.setBackground(getDrawable(com.tankbell.naloxone.R.drawable.text_border_error));

            label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmCodeMessage);
            label.setText("Confirmation failed!");
            confCode.setBackground(getDrawable(com.tankbell.naloxone.R.drawable.text_border_error));

            showDialogMessage("Confirmation failed", AppHelper.formatException(exception), false);
        }
    };

    VerificationHandler resendConfCodeHandler = new VerificationHandler() {
        @Override
        public void onSuccess(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
            TextView mainTitle = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmTitle);
            mainTitle.setText("Confirm your account");
            confCode = (EditText) findViewById(com.tankbell.naloxone.R.id.editTextConfirmCode);
            confCode.requestFocus();
            showDialogMessage("Confirmation code sent.","Code sent to "+cognitoUserCodeDeliveryDetails.getDestination()+" via "+cognitoUserCodeDeliveryDetails.getDeliveryMedium()+".", false);
        }

        @Override
        public void onFailure(Exception exception) {
            TextView label = (TextView) findViewById(com.tankbell.naloxone.R.id.textViewConfirmUserIdMessage);
            label.setText("Confirmation code resend failed");
            username.setBackground(getDrawable(com.tankbell.naloxone.R.drawable.text_border_error));
            showDialogMessage("Confirmation code request has failed", AppHelper.formatException(exception), false);
        }
    };

    private void showDialogMessage(String title, String body, final boolean exitActivity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exitActivity) {
                        exit();
                    }
                } catch (Exception e) {
                    exit();
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void exit() {
        Intent intent = new Intent();
        if(userName == null)
            userName = "";
        intent.putExtra("name",userName);
        setResult(RESULT_OK, intent);
        finish();
    }


    private class DynamoDBManagerTask extends
            AsyncTask<DynamoDBManagerType, Void, DynamoDBManagerTaskResult> {

        protected DynamoDBManagerTaskResult doInBackground(
                DynamoDBManagerType... types) {

            String tableStatus = DynamoDBManager.getTableStatusSignUpConfirm();

            DynamoDBManagerTaskResult result = new DynamoDBManagerTaskResult();
            result.setTableStatus(tableStatus);
            result.setTaskType(types[0]);

            if (types[0] == DynamoDBManagerType.INSERT_USER) {
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DynamoDBManager.insertUserDetailsSignUpConfirm(cache);
                }
            }

            return result;
        }

        protected void onPostExecute(DynamoDBManagerTaskResult result) {

            if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == DynamoDBManagerType.INSERT_USER) {
                //Toast.makeText(LandingPage.this,
                //        "Users inserted successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private enum DynamoDBManagerType {
        INSERT_USER
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
