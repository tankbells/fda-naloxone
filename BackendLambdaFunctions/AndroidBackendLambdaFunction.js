// Copyright 2016. TankBell Technologies.
// All Rights Reserved.

console.log('Loading event');

var AWS = require('aws-sdk');
const lambda = require('aws-lambda-invoke');
var dynamodb = new AWS.DynamoDB({apiVersion: '2012-08-10'});
var NLXUser = require("./NLXUser.js");
var gl = require("geolib")
var map = new Object();
var helpSeekerUserName;
var nlxSeeker = new NLXUser();
// Array of phone numbers of closest neighbors
var closestPhoneNosArray = [];
var closestPhoneNosJSON;
//Phone no of Help seeker
var helpSeekerPhoneNo;
var distanceArray = [];
var sortedArray = [];
var seekerCoord = new Object();

// Handle the event : App user has pressed the 'Help Me'
// Button.
exports.handler = function(event, context) {
    var tableNLX = "NLX";

    helpSeekerUserName = event.userName;

    console.log(helpSeekerUserName);

    // Scan the Table:NLX in DynamoDB
    // Get the location of the help seeker.
    // Get the locations of all the users
    // in the table.
    dynamodb.scan({
        TableName : tableNLX,
        Limit : 10,
    }, function(err, data) {
        if (err) {
            context.done('error','reading dynamodb failed: '+err);
        }
        for (var i in data.Items) {
            i = data.Items[i];
            //console.log(i.userName.S);
            //console.log(i.latitude.N);
            //console.log(i.longitude.N);
            if (i.userName.S == helpSeekerUserName) {
              nlxSeeker._userName = i.userName.S;
              nlxSeeker._age = i.age.N;
              nlxSeeker._emailAddr = i.emailAddr.S;
              nlxSeeker._givenName = i.givenName.S;
              nlxSeeker._latitude = i.latitude.N;
              nlxSeeker._longitude = i.longitude.N;
              nlxSeeker._phoneNo = i.phoneNo.S;
              nlxSeeker._quantity = i.quantity.N;
              seekerCoord.latitude = i.latitude.N;
              seekerCoord.longitude = i.longitude.N;
              break;
            }
        }
        for (var i in data.Items) {
            i = data.Items[i];
            if (i.userName.S != helpSeekerUserName) {
              var curr_coord = new Object();
              curr_coord.latitude = i.latitude.N;
              curr_coord.longitude = i.longitude.N;
              var distance = gl.getDistance(seekerCoord, curr_coord);

              var nlxProvider = new Object();
              nlxProvider._userName = i.userName.S;
              nlxProvider._age = i.age.N;
              nlxProvider._emailAddr = i.emailAddr.S;
              nlxProvider._givenName = i.givenName.S;
              nlxProvider._latitude = i.latitude.N;
              nlxProvider._longitude = i.longitude.N;
              nlxProvider._phoneNo = i.phoneNo.S;
              nlxProvider._quantity = i.quantity.N;
              nlxProvider.distance = distance;
              distanceArray.push(nlxProvider);
            }
        }

        console.log('Done scanning the DynamoDB Table: NLX');
        console.log('NLX Seeker ->');
        console.log(nlxSeeker);

        console.log('NLX Provider array top ->');
        console.log(distanceArray[0]._userName);
        console.log(distanceArray[1]._userName);
        console.log(distanceArray[2]._userName);

        sortedArray = distanceArray.sort(function(a, b) {
            return a.distance - b.distance;
        });

        console.log('Done sorting the potential providers');

        console.log('NLX Provider array top after sorting ->');
        console.log(sortedArray[0]._userName);
        console.log(sortedArray[1]._userName);
        console.log(sortedArray[2]._userName);
        console.log(sortedArray[0].distance);
        console.log(sortedArray[1].distance);
        console.log(sortedArray[2].distance);

        for (var k = 0; k < 3; k++) {
            closestPhoneNosArray.push(sortedArray[k]._phoneNo);
        }

        console.log('top phone no');
        console.log(closestPhoneNosArray[0]);
        console.log(closestPhoneNosArray[1]);
        console.log(closestPhoneNosArray[2]);

        // Send SMS
        console.log('Sending SMS to nearest providers' + closestPhoneNosJSON);
        lambda.invoke('SendTwilioSMS',
                      {to1: closestPhoneNosArray[0],
                       to2: closestPhoneNosArray[1],
                       to3: closestPhoneNosArray[2],
                       name: nlxSeeker._givenName,
                       lat: seekerCoord.latitude,
                       lng: seekerCoord.longitude,
                       ph: nlxSeeker._phoneNo,
                       toseeker: nlxSeeker._phoneNo,
                        name1: sortedArray[0]._givenName,
                        ph1: sortedArray[0]._phoneNo,
                        name2: sortedArray[1]._givenName,
                        ph2: sortedArray[1]._phoneNo,
                        name3: sortedArray[2]._givenName,
                        ph3: sortedArray[2]._phoneNo,
                       provider: 1})
                      .then(function() {
                        closestPhoneNosArray = [];
                        distanceArray = [];
                        sortedArray = [];
                        context.done(null, "Help is on the way ! Please check your SMS");
                      });
    }); // End DynamoDB Scan of Table:NLX
};
