package com.tankbell.naloxone;

/**
 * Copyright 2016.TankBell Technologies.
 * All Rights Reserved.
 */
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import java.nio.DoubleBuffer;
import java.util.ArrayList;

import android.util.Log;

public class DynamoDBManager {
    private static final String TAG = "DynamoDBManager";

    /*
     * TODO : The following methods needs to be consolidated.
     * For now , I am just calling them from each activity.
     * In the future consolidate several methods into one
     * and pass the activity as an argument.
     */
    public static String getTableStatusUpdateQt() {

        try {
            AmazonDynamoDBClient ddb = UpdateQuantity.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_QUANT);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            UpdateQuantity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static String getTableStatusEmergencyContacts() {

        try {
            AmazonDynamoDBClient ddb = UpdateEmergencyContact.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_USR_EMER_CONTACTS);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            UpdateEmergencyContact.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static String getTableStatusGPSService() {

        try {
            AmazonDynamoDBClient ddb = GPSTracker.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            GPSTracker.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static String getTableStatusSignUpConfirm() {

        try {
            AmazonDynamoDBClient ddb = SignUpConfirm.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_USER_DETAILS);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            SignUpConfirm.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static String getTableStatusRegisterUser() {

        try {
            AmazonDynamoDBClient ddb = RegisterUser.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_USER_DETAILS);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            RegisterUser.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static void insertUserDetailsSignUpConfirm(UserDetailsCache cache) {
        Log.d("DynamoDBManager","Insert User Details" + cache.getUserName().toString());
        AmazonDynamoDBClient ddb = SignUpConfirm.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserDetails details = new UserDetails();
            details.setUserName(cache.getUserName());
            details.setPhoneNumber(cache.getPhoneNo());
            details.setGivenname(cache.getGivenname());
            details.setEmailId(cache.getEmailAddr());
            Log.d(TAG, "Inserting user");
            mapper.save(details);
            Log.d(TAG, "User inserted");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting user");
            SignUpConfirm.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    public static void insertUserDetailsRegister(UserDetailsCache cache) {
        Log.d("DynamoDBManager","Insert User Details" + cache.getUserName().toString());
        AmazonDynamoDBClient ddb = RegisterUser.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserDetails details = new UserDetails();
            details.setUserName(cache.getUserName());
            details.setPhoneNumber(cache.getPhoneNo());
            details.setGivenname(cache.getGivenname());
            details.setEmailId(cache.getEmailAddr());
            Log.d(TAG, "Inserting user");
            mapper.save(details);
            Log.d(TAG, "User inserted");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting user");
            RegisterUser.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    public static void updateUserDetailsGPS(UserDetailsCache cache) {
        AmazonDynamoDBClient ddb = GPSTracker.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserLocation loc = new UserLocation();
            loc.setUserName(cache.getUserName());
            loc.setLatitude(cache.getLatitude());
            loc.setLongitude(cache.getLongitude());
            loc.setGivenName(cache.getGivenname());
            loc.setAge(cache.getAge());
            loc.setQuantity(cache.getQuantity());
            loc.setPhoneNo(cache.getPhoneNo());
            loc.setEmailAddr(cache.getEmailAddr());
            loc.setContactA(cache.getContactA());
            loc.setContactB(cache.getContactB());
            loc.setContactC(cache.getContactC());
            Log.d(TAG, "Updating user");
            mapper.save(loc);
            Log.d(TAG, "User updated");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error updating user");
            GPSTracker.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    public static void updateUserDetailsQuantity(UserDetailsCache cache) {
        AmazonDynamoDBClient ddb = UpdateQuantity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserLocation loc = new UserLocation();
            loc.setUserName(cache.getUserName());
            loc.setLatitude(cache.getLatitude());
            loc.setLongitude(cache.getLongitude());
            loc.setGivenName(cache.getGivenname());
            loc.setAge(cache.getAge());
            loc.setQuantity(cache.getQuantity());
            loc.setPhoneNo(cache.getPhoneNo());
            loc.setEmailAddr(cache.getEmailAddr());
            Log.d(TAG, "Updating user");
            mapper.save(loc);
            Log.d(TAG, "User updated");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error updating user");
            UpdateQuantity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    public static void updateUserDetailsEmergency(UserDetailsCache cache) {
        AmazonDynamoDBClient ddb = UpdateEmergencyContact.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserLocation loc = new UserLocation();
            loc.setUserName(cache.getUserName());
            loc.setLatitude(cache.getLatitude());
            loc.setLongitude(cache.getLongitude());
            loc.setGivenName(cache.getGivenname());
            loc.setAge(cache.getAge());
            loc.setQuantity(cache.getQuantity());
            loc.setPhoneNo(cache.getPhoneNo());
            loc.setEmailAddr(cache.getEmailAddr());
            loc.setContactA(cache.getContactA());
            loc.setContactB(cache.getContactB());
            loc.setContactC(cache.getContactC());
            Log.d(TAG, "Updating user");
            mapper.save(loc);
            Log.d(TAG, "User updated");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error updating user");
            UpdateEmergencyContact.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    public static void updateUserQuant(UserDetailsCache cache) {
        AmazonDynamoDBClient ddb = UpdateQuantity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserQuant q = new UserQuant();
            q.setUserName(cache.getUserName());
            q.setAge(cache.getAge());
            q.setQuantity(cache.getQuantity());
            Log.d(TAG, "Updating user");
            mapper.save(q);
            Log.d(TAG, "User updated");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error updating user");
            UpdateQuantity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    public static void updateUserEmergencyContact(UserDetailsCache cache) {
        AmazonDynamoDBClient ddb = UpdateEmergencyContact.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            EmergencyContacts q = new EmergencyContacts();
            q.setUserName(cache.getUserName());
            q.setContactA(cache.getContactA());
            q.setContactB(cache.getContactB());
            q.setContactC(cache.getContactC());
            Log.d(TAG, "Updating user");
            mapper.save(q);
            Log.d(TAG, "User updated");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error updating user");
            UpdateEmergencyContact.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     ****************** Fetch and Read *****************************
     */

    /*
     * Get the NLX_USR_DETAILS table **STATUS** from the
     * GPSService Context.
     */
    public static String getNLXUserDetailTableStatusFromGPSService() {

        try {
            AmazonDynamoDBClient ddb = GPSTracker.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_USER_DETAILS);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            GPSTracker.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Get the NLX_QUANT table **STATUS** from the
     * GPSService Context.
     */
    public static String getNLXQuantTableStatusFromGPSService() {

        try {
            AmazonDynamoDBClient ddb = GPSTracker.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_QUANT);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            GPSTracker.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Get the NLX Emergency Contacts Table Status from
     * GPS Service
     */
    public static String getNLXEmerContactTableStatusFromGPSService() {

        try {
            AmazonDynamoDBClient ddb = GPSTracker.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_USR_EMER_CONTACTS);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            GPSTracker.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
    * Get the NLX table **STATUS** from the
    * Update Quantity Context.
    */
    public static String getNLXTableStatusFromUpdate() {

        try {
            AmazonDynamoDBClient ddb = UpdateQuantity.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            UpdateQuantity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Get the NLX Table STATUS from the
     * Emergency Contacts Context
     */
    public static String getNLXTableStatusFromEmergency() {

        try {
            AmazonDynamoDBClient ddb = UpdateEmergencyContact.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            UpdateEmergencyContact.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Get the NLX_USR_DETAILS table **STATUS** from the
     * Update Context.
     */
    public static String getNLXUserDetailTableStatusFromUpdate() {

        try {
            AmazonDynamoDBClient ddb = UpdateQuantity.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_USER_DETAILS);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            UpdateQuantity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Get the NLX USer Detail Table Status from the
     * Emergency Context
     */
    public static String getNLXUserDetailTableStatusFromEmergency() {

        try {
            AmazonDynamoDBClient ddb = UpdateEmergencyContact.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME_NLX_USER_DETAILS);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            UpdateEmergencyContact.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Get the NLX_USR_DETAILS **Data** given the user name.
     */
    public static UserDetails getUserDetailsFromGPSService(String userName) {

        AmazonDynamoDBClient ddb = GPSTracker.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserDetails userDetails = mapper.load(UserDetails.class,
                    userName);

            return userDetails;

        } catch (AmazonServiceException ex) {
            GPSTracker.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
    * Get the NLX_QUANT **Data** given the user name.
    */
    public static UserQuant getUserQuantFromGPSService(String userName) {

        AmazonDynamoDBClient ddb = GPSTracker.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserQuant userQuant = mapper.load(UserQuant.class,
                    userName);

            return userQuant;

        } catch (AmazonServiceException ex) {
            GPSTracker.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
     * Get emergency contacts from GPS Service
     */
    public static EmergencyContacts getUserEmerContactFromGPSService(String userName) {

        AmazonDynamoDBClient ddb = GPSTracker.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            EmergencyContacts emergencyContacts = mapper.load(EmergencyContacts.class,
                    userName);

            return emergencyContacts;

        } catch (AmazonServiceException ex) {
            GPSTracker.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
    * Get the NLX **Data** given the user name.
    */
    public static UserLocation getUserLocationFromUpdate(String userName) {

        AmazonDynamoDBClient ddb = UpdateQuantity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserLocation userLocation = mapper.load(UserLocation.class,
                    userName);

            return userLocation;

        } catch (AmazonServiceException ex) {
            UpdateQuantity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
     * Get the NLX Data from the Emergency Context
     */
    public static UserLocation getUserLocationFromEmergency(String userName) {

        AmazonDynamoDBClient ddb = UpdateEmergencyContact.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserLocation userLocation = mapper.load(UserLocation.class,
                    userName);

            return userLocation;

        } catch (AmazonServiceException ex) {
            UpdateEmergencyContact.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static UserDetails getUserDetailsFromUpdate(String userName) {

        AmazonDynamoDBClient ddb = UpdateQuantity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserDetails userDetails = mapper.load(UserDetails.class,
                    userName);

            return userDetails;

        } catch (AmazonServiceException ex) {
            UpdateQuantity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
     * Get the NL USR Details from the Emergency Context
     */
    public static UserDetails getUserDetailsFromEmergency(String userName) {

        AmazonDynamoDBClient ddb = UpdateEmergencyContact.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserDetails userDetails = mapper.load(UserDetails.class,
                    userName);

            return userDetails;

        } catch (AmazonServiceException ex) {
            UpdateEmergencyContact.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
     ***************** END : Fetch and Read ********************
     */

    @DynamoDBTable(tableName = Constants.TABLE_NAME_NLX)
    public static class UserLocation {
        private String userName;
        private Double latitude;
        private Double longitude;
        private String contactA;
        private String contactB;
        private String contactC;

        public String getContactA() {
            return contactA;
        }

        public void setContactA(String contactA) {
            this.contactA = contactA;
        }

        public String getContactB() {
            return contactB;
        }

        public void setContactB(String contactB) {
            this.contactB = contactB;
        }

        public String getContactC() {
            return contactC;
        }

        public void setContactC(String contactC) {
            this.contactC = contactC;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        private String givenName;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        private int age;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getPhoneNo() {
            return phoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
        }

        public String getEmailAddr() {
            return emailAddr;
        }

        public void setEmailAddr(String emailAddr) {
            this.emailAddr = emailAddr;
        }

        private int quantity;
        private String phoneNo;
        private String emailAddr;

        @DynamoDBHashKey(attributeName = "userName")
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        @DynamoDBAttribute(attributeName = "latitude")
        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        @DynamoDBAttribute(attributeName = "longitude")
        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }


    @DynamoDBTable(tableName = Constants.TABLE_NAME_NLX_USER_DETAILS)
    public static class UserDetails {
        private String userName;
        private String phoneNumber;
        private String givenname;
        private String emailId;

        @DynamoDBHashKey(attributeName = "userName")
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        @DynamoDBAttribute(attributeName = "givenName")
        public String getGivenname() {
            return givenname;
        }

        public void setGivenname(String givenname) {
            this.givenname = givenname;
        }

        @DynamoDBAttribute(attributeName = "emailId")
        public String getEmailId() {
            return emailId;
        }

        public void setEmailId(String emailId) {
            this.emailId = emailId;
        }


        @DynamoDBAttribute(attributeName = "phoneNumber")
        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    @DynamoDBTable(tableName = Constants.TABLE_NAME_NLX_QUANT)
    public static class UserQuant {
        private String userName;
        private int age;
        private int quant;

        @DynamoDBHashKey(attributeName = "userName")
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        @DynamoDBAttribute(attributeName = "age")
        public int getAge() {
            return age;
        }

        public void setAge(int a) {
            this.age = a;
        }

        @DynamoDBAttribute(attributeName = "quantity")
        public int getQuantity() {
            return quant;
        }

        public void setQuantity(int q) {
            this.quant = q;
        }
    }

    @DynamoDBTable(tableName = Constants.TABLE_NAME_NLX_USR_EMER_CONTACTS)
    public static class EmergencyContacts {
        private String userName;
        private String contactA;
        private String contactB;
        private String contactC;

        @DynamoDBHashKey(attributeName = "userName")
        public String getUserName() {
            return userName;
        }
        public void setUserName(String userName) {
            this.userName = userName;
        }

        @DynamoDBAttribute(attributeName = "contactA")
        public String getContactA() {
            return contactA;
        }
        public void setContactA(String a) {
            this.contactA = a;
        }

        @DynamoDBAttribute(attributeName = "contactB")
        public String getContactB() {
            return contactB;
        }
        public void setContactB(String b) {
            this.contactB = b;
        }

        @DynamoDBAttribute(attributeName = "contactC")
        public String getContactC() {
            return contactC;
        }
        public void setContactC(String c) {
            this.contactC = c;
        }


    }



}
