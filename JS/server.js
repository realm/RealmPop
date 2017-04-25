//
// RealmPop server-side logic
//

var Realm = require('realm');
var { Player, Score, Game, Side } = require('./server-schema');

// users availability cache
const users = new Object();

/**
 * Singleton game server-side logic class
 * 
 * Tracks the last time users set themselves to available, 
 * and "expires" them (e.g. set to unavailable) if they don't check in within a given time interval
 */ 
function Server(config, adminToken, credentials) {
  this.minutesToExpire = 2.0;
  this.callbackMinutesInterval = 1.0;

  // parse the user credentials
  this.config = config;
  const creds = credentials.split(':');
  this.config.uid = creds[0];
  this.config.username = creds[1];
  this.config.password = creds[2];

  // connect to Realm Object Server
  Server.adminUser = Realm.Sync.User.adminUser(adminToken);
  Server.instance = this;

  this.realm = null;
  this.timer = null;

  //reset everyone's availability at start to clean the slate
  this.openRealm((realm) => {
    this.realm = realm;
    this.resetAvailability();
  });
}

// periodically implement the "expiring" functionality
Server.prototype.start = function() {
  if (this.realm === null) {
    print("waiting for connection...");
    setTimeout(() => { this.start() }, 2000);
    return;
  }
  this.timer = setInterval(() => { Server.instance.poll(); }, this.callbackMinutesInterval * 60000);
}

// stop "expiring" users
Server.prototype.stop = function() {
  if (this.timer !== null) {
    clearInterval(this.timer);
  }
}

// connect to realm
Server.prototype.openRealm = function (callback) {
  Realm.Sync.User.login('http://' + this.config.host + ':' + this.config.port, this.config.username, this.config.password, (error, user) => {
    if (!error) {
      // successfully connected
      const gameUrl = 'realm://' + this.config.host + ':' + this.config.port + '/~/game';
      const realm = new Realm({
        sync: { user: user, url: gameUrl },
        schema: [Player.schema, Score.schema, Game.schema, Side.schema]
      });
      callback(realm);
    } else {
      print('error while connecting as the game admin: ' + error);
      process.exit(1);
    }
  });
}

// timer callback, loops over active users last check-in time
// and sets inactive ones to unavailable
Server.prototype.poll = function() {

  if (this.realm === null) {
    print("not connected yet");
    return;
  }

  const unavailableTime = new Date().getTime() - this.minutesToExpire * 60000;
  const expired = new Array();

  for (const id in users) {
    //print('compare: now ' + unavailableTime + ' to last: ' + users[id] + ' diff: '+ (users[id] - unavailableTime));

    if (users[id] < unavailableTime) {
      print('set to unavailable: ' + id);
      // user hasn't checked-in for a while, expire them
      expired.push(id);
    };
  }

  if (expired.length > 0) {
    // make sure the app has synchronized on very first open
    if (this.realm.objects("Player").length == 0) return;

    print("updating availability on " + expired.length + " players");

    // loop over players, set available to false
    this.realm.write(() => {
      for (const i in expired) {
        const expiredId = expired[i];
        
        const player = this.realm.objectForPrimaryKey('Player', expiredId);
        if (!!player) {
          player.available = false;
          this.didUpdateAvailability(player.id, false);
        }
      }
    });
    
    print("updated availability");
  }
}

// reset everyone's availability
Server.prototype.resetAvailability = function() {
  const players = this.realm.objects("Player");
  this.realm.write(() => {
    for (const i in players) {
      print('reset: ' + players[i].id);
      players[i].available = false;
    }
  });
}

// update the in-memory check-in cache
Server.prototype.didUpdateAvailability = function(userId, available) {

  if (!!available) {
    users[userId] = new Date().getTime();
  } else {
    delete users[userId];
  }
  //dumpUsers(users);
}

// debug method, use it to dump the active users in the console
function dumpUsers() {
  //debug output
  if (Object.keys(users).length > 0) {
    print("--------------");
    for (const i in users) {
      print("["+i+"] => "+users[i])
    }
    print("--------------");
  }
}

exports.Server = Server;
