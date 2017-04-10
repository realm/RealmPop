var Realm = require('realm');
var ADMIN_TOKEN = 'ADMIN_TOKEN';
var SERVER_URL = 'realm://127.0.0.1:9080';
var NOTIFIER_PATH = '/^\/([0-9a-f]+)\/private$/';

function handleChange(changeEvent) {
  console.log('EVENT!');
  
  var matches = changeEvent.path.match(/^\/([0-9a-f]+)\/private$/);
  var userId = matches[1];

  var realm = changeEvent.realm;
  var coupons = realm.objects('Coupon');
  var couponIndexes = changeEvent.changes.Coupon.insertions;

  for (var couponIndex in couponIndexes) {
    var coupon = coupons[couponIndex];
    if (coupon.isValid !== undefined) {
      var isValid = verifyCouponForUser(coupon, userId);
      // Attention: Writes here will trigger a subsequent notification.
      // Take care that this doesn't cause infinite changes!
      realm.write(function() {
        coupon.isValid = isValid;
      });
    }
  }
}

// create the admin user
var adminUser = Realm.Sync.User.adminUser(adminToken);

// register the event handler callback
Realm.Sync.addListener(SERVER_URL, adminUser, NOTIFIER_PATH, 'change', handleChange);

console.log('Listening for Realm changes');