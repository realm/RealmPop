//
// Configuration - adjust for your setup if needed
//

let config = {
    host: 'localhost',
    port: '9080',
    token: 'ewoJImlkZW50aXR5IjogIl9fYXV0aCIsCgkiYWNjZXNzIjogWyJ1cGxvYWQiLCAiZG93bmxvYWQiLCAibWFuYWdlIl0KfQo=:thqMSBtWuhl2Mn1LX5TGTBNHxAL9KeDL1swzWq2/cmhHiYOFxu61y3+Wd4/CCP3ZS+ShaMq5Ve6uMw5Z0W4zwJ8RK+k+BZskl4XkJSYI1yz71W9my+KqbD20f7ClVKff5Oo8hFvKjNWnRZLgvMuUXFlPxjRnzUYSHONeM8EYpbDr2YtAYDyLOv0wZ4Szga/q6iOGApi57XFNV7Tn438i1+gA6TnR1klOmvtSF0x86q1N8g6UukcT0y1g/gvmJyyrO2Z68vgmy1vxnViE/r5bAvhAaDAxGJxgUV+D7mM0+NG7QkLVYhkfeP1ozn4AelkCwYFzQGKV8x7YAL4Trfyfdw==' //admin token
}

//
// Script setup
// 
console.log('Start setting up...');

var Realm = require('realm');

function isRealmObject(x) {
  return x !== null && x !== undefined && x.constructor === Realm.Object
}

//
// Realm Schema
//

class ConnectedUser { }
ConnectedUser.schema = {
  name: 'ConnectedUser',
  primaryKey: 'id',
  properties: {
    id: {type: 'string'},
    creationDate: {type: 'date'},
    username: {type: 'string'},
    lastUpdate: {type: 'date'},
    isAvailable: {type: 'bool', default: false}
  }
};

//
// Class to do the game server setup
//
class SetupRealmPop { }

SetupRealmPop.usersUrl = function() {
  return 'realm://' + config.host + ':' + config.port + '/users'
}

SetupRealmPop.generateUniqueId = function(object)  {
    return object + ':' + (new Date) + ':' + Math.random;
}

SetupRealmPop.run = function(token) {
  let admin = Realm.Sync.User.adminUser(token);
  let realm = new Realm({
    sync: {
      user: admin,
      url: SetupRealmPop.usersUrl(),
    },
    schema: [ConnectedUser]
  });

  let users = realm.objects('ConnectedUser');
  let admins = users.filtered('username == "Admin"')

  if (admins.length > 0) {
    // setup's been already performed
    console.log('Setup has already been performed previously! Nothing has been done now.');
  } else {
    // create a new admin user
    let now = new Date()
    realm.write(() => {
      realm.create('ConnectedUser', {
        id: admin.identity,
        username: 'Admin',
        creationDate: now,
        lastUpdate: now
      });
    });
    console.log('Admin user Created.');

    //share the users file with everyone
    const managementRealm = user.openManagementRealm();
    managementRealm.write(() => {
      managementRealm.create('PermissionChange', {
        id: SetupRealmPop.generateUniqueId('PermissionChange'),
        createdAt: new Date(),
        userId: '*',
        realmUrl: config.usersUrl(),
        mayRead: true
      });
    });
    console.log('Created shared users file.');
  }

}

//
// Run Script
//

SetupRealmPop.run(config.token);
console.log('Completed.');
