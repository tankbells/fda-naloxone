// Copyright 2016. TankBell Technologies.
// All Rights Reserved.

console.log('Loading event');

// Twilio Credentials
var accountSid = 'id_here';
var authToken = 'pwd_here';
var fromNumber = '+1xxx';

var https = require('https');
var queryString = require('querystring');

var os = require('os');

var client = require('twilio')(accountSid, authToken);

// Lambda function:
exports.handler = function (event, context) {

    console.log('Running event');

    var isProvider = event.provider;
    if (isProvider == 1) {
        var gMaps = 'http://www.google.com/maps/place/' +
                     event.lat + ',' + event.lng;
        var msg = event.name + ' needs help.' + os.EOL +
        'Location:' + gMaps + os.EOL +
        //'GPS Coordinates: ' + event.lat + ',' + event.lng + os.EOL +
        'Phone Number: ' + event.ph;
        console.log(msg);
        SendSMS('event.to1', msg, // Change to event.to1
                function (status) {
                  console.log('Event ' + '1');
                  SendSMS('event.to2',msg, function() { // Change to event.to2
                    console.log('Event ' + '2');
                    SendSMS('event.to3', msg, function() { //Change to event.to3
                      console.log('Event ' + '3');
                      var msgSeeker = 'Help is on the way.' + os.EOL +
                              'Alerts sent to:' + os.EOL +
                              event.name1 + ':' + event.ph1 + os.EOL +
                              event.name2 + ':' + event.ph2 + os.EOL +
                              event.name3 + ':' + event.ph3 + os.EOL;
                      SendSMS(event.toseeker, msgSeeker, function() {
                        console.log('Event ' + '4');
                        callFirstResponders(event.toseeker,function() {
                          console.log(status);
                          context.done(null,status);
                        });
                      });
                    });
                  });
        });
    }
};

// Sends an SMS message using the Twilio API
// to: Phone number to send to
// body: Message body
// completedCallback(status) : Callback with status message when the function completes.
function SendSMS(to, body, completedCallback) {

    console.log('Going to send SMS to ' + to);

    // The SMS message to send
    var message = {
        To: to,
        From: fromNumber,
        Body: body
    };

    var messageString = queryString.stringify(message);

    // Options and headers for the HTTP request
    var options = {
        host: 'api.twilio.com',
        port: 443,
        path: '/2010-04-01/Accounts/' + accountSid + '/Messages.json',
        method: 'POST',
        headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Content-Length': Buffer.byteLength(messageString),
                    'Authorization': 'Basic ' + new Buffer(accountSid + ':' + authToken).toString('base64')
                 }
    };

    // Setup the HTTP request
    var req = https.request(options, function (res) {

        res.setEncoding('utf-8');

        // Collect response data as it comes back.
        var responseString = '';
        res.on('data', function (data) {
            responseString += data;
        });

        // Log the responce received from Twilio.
        // Or could use JSON.parse(responseString) here to get at individual properties.
        res.on('end', function () {
            console.log('Twilio Response: ' + responseString);
            completedCallback('API request sent successfully.');
        });
    });

    // Handler for HTTP request errors.
    req.on('error', function (e) {
        console.error('HTTP error: ' + e.message);
        completedCallback('API request completed with error(s).');
    });

    // Send the HTTP request to the Twilio API.
    // Log the message we are sending to Twilio.
    console.log('Twilio API call: ' + messageString);
    req.write(messageString);
    req.end();

}

function callFirstResponders(toCall,completedCallback) {

  console.log('Going to call ' + toCall);

  var message = {
      To: 'xxxxxxx', // Replace with 911. Seek authorization and all that. But for now ....
      From: toCall,
      Url: 'https://pr8s9jnbhb.execute-api.us-east-1.amazonaws.com/beta/response',
      UserName: 'GV2016'
  };

  var messageString = queryString.stringify(message);

  // Options and headers for the HTTP request
  var options = {
      host: 'api.twilio.com',
      port: 443,
      path: '/2010-04-01/Accounts/' + accountSid + '/Calls',
      method: 'POST',
      headers: {
                  'Content-Type': 'application/x-www-form-urlencoded',
                  'Content-Length': Buffer.byteLength(messageString),
                  'Authorization': 'Basic ' + new Buffer(accountSid + ':' + authToken).toString('base64')
               }
  };

  // Setup the HTTP request
  var req = https.request(options, function (res) {

      res.setEncoding('utf-8');

      // Collect response data as it comes back.
      var responseString = '';
      res.on('data', function (data) {
          responseString += data;
      });

      // Log the responce received from Twilio.
      // Or could use JSON.parse(responseString) here to get at individual properties.
      res.on('end', function () {
          console.log('Twilio Response: ' + responseString);
          completedCallback('API request sent successfully.');
      });
  });

  // Handler for HTTP request errors.
  req.on('error', function (e) {
      console.error('HTTP error: ' + e.message);
      completedCallback('API request completed with error(s).');
  });

  // Send the HTTP request to the Twilio API.
  // Log the message we are sending to Twilio.
  console.log('Twilio API call: ' + messageString);
  req.write(messageString);
  req.end();
}
