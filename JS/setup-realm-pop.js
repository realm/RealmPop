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

SetupRealmPop._setConfig = function(config) {
  // set the server url and shared users realm url
  SetupRealmPop.url = config.host + ':' + config.port;
  SetupRealmPop.usersUrl = 'realm://' + SetupRealmPop.url + '/users';
  SetupRealmPop.__completion = null;
}

SetupRealmPop._generateUniqueId = function(object)  {
  //create a "random" string
  return object + ':' + (new Date) + ':' + Math.random;
}

SetupRealmPop.run = function(config, token, completion) {
  SetupRealmPop._setConfig(config);
  SetupRealmPop.__completion = completion;

  Realm.Sync.User.login('http://'+SetupRealmPop.url, 'popadmin@realm', 'p@s$w0rd', (error, user) => {
    if (user !== undefined) {
      print('Logged in as popadmin@realm. Syncing user list; takes a moment...');
      let realm = new Realm({
        sync: { user: user, url: SetupRealmPop.usersUrl },
        schema: [ConnectedUser.schema]
      });
      setTimeout(function () { //TODO: clean up
        SetupRealmPop._setup(realm, user, config, token);
      }, 5000);
    } else {
      print(error);
      print('You need to create a "popadmin@realm" admin user first.');
      process.exit(1);
    };
  });
}

SetupRealmPop._setup = function(realm, admin, config, token) {

  let users = realm.objects('ConnectedUser');
  print('User count: ' + users.length);

  if (users.length > 0) {
    // setup's been already performed
    print('Setup has already been performed, nothing to do from setup right now.');
    SetupRealmPop.__completion();
    SetupRealmPop.__completion = null;
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
    print('Admin user Created.');
    
    // not needed when an admin creates a shared file
    var managementRealm = null
    try {
      print('Open management realm');
      managementRealm = admin.openManagementRealm();
    } catch (e) {
      print(e);
      process.exit(1);
    }

    if (managementRealm != null) {
        print('Add sharing permission to users');
        managementRealm.write(() => {
          managementRealm.create('PermissionChange', {
            id: SetupRealmPop._generateUniqueId('PermissionChange'),
            createdAt: now,
            updatedAt: now, 
            userId: '*',
            realmUrl: SetupRealmPop.usersUrl,
            mayRead: true
          }, true);
        });

        print('Created shared users file.');
        SetupRealmPop.__completion();
        SetupRealmPop.__completion = null;
        
    } else {
      print('Couldn\'t open management realm.')
      process.exit(1);
    }
  }
}

//
// Module exports
//

exports.GameSetup = SetupRealmPop;