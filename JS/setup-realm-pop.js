//
// Script setup
// 
var Realm = require('realm');

//
// Import server schema
// 

var Schema = require('./server-schema.js');
var ConnectedUser = Schema.ConnectedUser;

//
// Class to do the game server setup
//

class SetupRealmPop { }

SetupRealmPop.setConfig = function(config) {
  SetupRealmPop.url = config.host + ':' + config.port;
  SetupRealmPop.usersUrl = 'realm://' + SetupRealmPop.url + '/users';
}

SetupRealmPop.generateUniqueId = function(object)  {
  return object + ':' + (new Date) + ':' + Math.random;
}

SetupRealmPop.run = function(config, token) {
  SetupRealmPop.setConfig(config);

  Realm.Sync.User.login('http://'+SetupRealmPop.url, 'popadmin@realm', 'p@s$w0rd', (error, user) => {
    if (user !== undefined) {
      this.__setup(user, config, token);
    } else {
      console.log(error);
      console.log('You need to create a "popadmin@realm" admin user first.');
      process.exit(1);
    };
  });
}

SetupRealmPop.__setup = function(admin, config, token) {

  let realm = new Realm({
    sync: {
      user: admin,
      url: SetupRealmPop.usersUrl,
    },
    schema: [ConnectedUser.schema]
  });

  let users = realm.objects('ConnectedUser');
  console.log('users: '+users.length);

  if (users.length > 0) {
    // setup's been already performed
    console.log('Setup has already been performed previously.');
  } else {
    let now = new Date()

    // create a new admin user
    realm.write(() => {
      realm.create('ConnectedUser', {
        id: admin.identity,
        username: 'Admin',
        creationDate: now,
        lastUpdate: now
      });
    });
    console.log('Admin user Created.');
    
    // not needed when an admin creates a shared file
    var managementRealm = null
    try {
      console.log('Open management realm');
      managementRealm = admin.openManagementRealm();
    } catch (e) {
      console.log(e);
      process.exit(1);
    }

    if (managementRealm != null) {
        console.log('Add sharing permission to users');
        managementRealm.write(() => {
          managementRealm.create('PermissionChange', {
            id: SetupRealmPop.generateUniqueId('PermissionChange'),
            createdAt: Date.now(),
            userId: '*',
            realmUrl: SetupRealmPop.usersUrl,
            mayRead: true
          }, true);
        });

        console.log('Created shared users file.');
    }
  }
}

//
// Module exports
//

exports.GameSetup = SetupRealmPop;