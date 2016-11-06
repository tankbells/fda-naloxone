// Copyright 2016. TankBell Technologies.
// All Rights Reserved.

var method = NLXUser.prototype;

function NLXUser() {
  this._userName = '';
  this._age = 0;
  this._emailAddr = '';
  this._givenName = '';
  this._latitude = 0;
  this._longitude = 0;
  this._phoneNo = '';
  this._quantity = 0;
}

method.getUserName = function() {
    return this._userName;
};

method.getAge = function() {
    return this._age;
};

method.getEmailAddr = function() {
    return this._emailAddr;
};

method.getGivenName = function() {
    return this._givenName;
};

method.getLatitude = function() {
    return this._latitude;
};

method.getLongitude = function() {
  return this._longitude;
}

method.getPhoneNo = function() {
    return this._phoneNo;
};

method.getQuantity = function() {
    return this._quantity;
};

module.exports = NLXUser;
