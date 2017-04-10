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
  this.path = '/.*/app';
}

Pop.usersUrl = function() {
   return Pop.serverUrl + "/users";
}

Pop.prototype.connect = function(adminToken, accessToken) {
  // get access to ros
  Realm.Sync.setAccessToken(accessToken);
  Pop.adminUser = Realm.Sync.User.adminUser(adminToken);
  
  //install listener
  Realm.Sync.addListener(Pop.serverUrl, Pop.adminUser, this.path, 'change', Pop.changeCallback);
  console.log('Install event listener for \''+Pop.serverUrl+this.path+'\'');
}

Pop.changeCallback = function(event) {
  //console.log('event callback');

  var matches = event.path.match(/^\/([0-9a-f]+)\/app$/);
  var sourceUserId = matches[1];

  let realm = event.realm;
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

  // insertions
  let insertions = changes.insertions;
  if (insertions.length > 0) {
    Pop.insertions(realm, sourceUserId, usersRealm, insertions);
  }

  // modifications
  let modifications = changes.modifications;
  if (modifications.length > 0) {
    Pop.modifications(realm, sourceUserId, usersRealm, modifications);
  }

  //console.log('event handled');
}

Pop.insertions = function(sourceRealm, sourceUserId, usersRealm, indexes) {

  // insert new user objects
  let now = new Date()
  let players = sourceRealm.objects('Player');

  for (var i = 0; i < indexes.length; i++) {
    let index = indexes[i];
    let player = players[index];

    if (isRealmObject(player) && player.id == sourceUserId) {
      console.log("[add player]: " + player.name);

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

Pop.modifications = function(sourceRealm, sourceUserId, usersRealm, indexes) {
  // console.log('modifications');
  // update a user objects
  let now = new Date()
  let players = sourceRealm.objects('Player');

  for (var i = 0; i < indexes.length; i++) {
    let index = indexes[i];
    let player = players[index];

    if (isRealmObject(player)) {
      console.log("[update player]: " + player.name);

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