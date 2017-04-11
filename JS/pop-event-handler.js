//
// Pop worker
//

var Schema = require('./server-schema.js');
var ConnectedUser = Schema.ConnectedUser;
var Player = Schema.Player;

function isRealmObject(x) {
  return x !== null && x !== undefined && x.constructor === Realm.Object
}

function Pop(config, path) {
  Pop.config = config;
  Pop.serverUrl = 'realm://' + Pop.config.host + ':' + Pop.config.port;
  Pop.adminUser = null;
  this.path = '.*\/app';
}

Pop.usersUrl = function() {
   return Pop.serverUrl + "/users";
}

Pop.prototype.connect = function(adminToken, accessToken) {
  // get access to ros
  //print('using access token: ' + accessToken);
  Realm.Sync.setAccessToken(accessToken);
  //print('using admin token: ' + adminToken);
  Pop.adminUser = Realm.Sync.User.adminUser(adminToken);
  
  //install listener
  Realm.Sync.addListener(Pop.serverUrl, Pop.adminUser, this.path, 'change', Pop.changeCallback);
  print('Install event listener on ' + Pop.serverUrl + ' at ' + this.path);
}

Pop.changeCallback = function(event) {

  var matches = event.path.match(/^\/([0-9a-f]+)\/app$/);
  var sourceUserId = matches[1];

  let changes = event.changes.Player;
  if (changes == undefined) {
    return
  }
  
  // open users realm
  let usersRealm = new Realm({
    sync: {
      user: Pop.adminUser,
      url: Pop.usersUrl(),
    },
    schema: [ConnectedUser.schema]
  });

  // open source realm
  let sourceRealm = event.realm;
  let players = sourceRealm.objects('Player');

  // insertions
  let insertions = changes.insertions;
  if (insertions.length > 0) {
    Pop.insertions(players, sourceUserId, usersRealm, insertions);
  }

  // modifications
  let modifications = changes.modifications;
  if (modifications.length > 0) {
    Pop.modifications(players, sourceUserId, usersRealm, modifications);
  }

}

Pop.insertions = function(players, sourceUserId, usersRealm, indexes) {

  // insert new user objects
  let now = new Date()

  for (var i = 0; i < indexes.length; i++) {
    let index = indexes[i];
    let player = players[index];

    if (isRealmObject(player) && player.id == sourceUserId) {
      //print("add player: " + player.name);

      usersRealm.write(() => {
      	let newUser = usersRealm.create('ConnectedUser', {
		      id: player.id,
		      creationDate: now,
    		  username: player.name,
		      lastUpdate: now,
		      available: false
      	});
      });
    } 
  }
}

Pop.modifications = function(players, sourceUserId, usersRealm, indexes) {

  // update a user objects
  let now = new Date()

  for (var i = 0; i < indexes.length; i++) {
    let index = indexes[i];
    let player = players[index];

    if (isRealmObject(player)) {
      //print("update player: " + player.name);

      usersRealm.write(() => {
      	usersRealm.create('ConnectedUser', {
    		  id: player.id,
          creationDate: now, //TODO: fix
		      username: player.name,
		      lastUpdate: now,
          available: player.available
      	}, true);
      });
    } 
  }

}

exports.Pop = Pop;