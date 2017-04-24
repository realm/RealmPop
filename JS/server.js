//
// RealmPop server-side logic
//

var Realm = require('realm');
var { Player, Score, Game, Side } = require('./server-schema');

function isRealmObject(x) {
  return x !== null && x !== undefined && x.constructor === Realm.Object
}

// users availability cache
var users = new Object();

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
  let creds = credentials.split(':');
  this.config.uid = creds[0];
  this.config.username = creds[1];
  this.config.password = creds[2];

  // connect to Realm Object Server
  Server.adminUser = Realm.Sync.User.adminUser(adminToken);
  Server.instance = this;

  this.timer = null;

  //reset everyone's availability at start to clean the slate
  this.resetAvailability();
}

// periodically implement the "expiring" functionality
Server.prototype.start = function() {
  this.timer = setInterval(() => { Server.instance.callback(); }, this.callbackMinutesInterval * 60000);
}

// stop "expiring" users
Server.prototype.stop = function() {
  if (this.timer != null) {
    clearInterval(this.timer);
  }
}

// timer callback, loops over active users last check-in time
// and sets inactive ones to unavailable
Server.prototype.callback = function() {

  let unavailableTime = new Date().getTime() - this.minutesToExpire * 60000;
  var expired = new Array();

  for (var id in users) {
    //print('compare: now ' + unavailableTime + ' to last: ' + users[id] + ' diff: '+ (users[id] - unavailableTime));

    if (users[id] < unavailableTime) {
      print('set to unavailable: ' + id);
      // user hasn't checked-in for a while, expire them
      expired.push(id);
    };
  }

  if (expired.length > 0) {

    // connect to ROS as the game admin user
    Realm.Sync.User.login('http://' + this.config.host + ':' + this.config.port, this.config.username, this.config.password, (error, user) => {

      if (!error) {
        let gameUrl = 'realm://' + this.config.host + ':' + this.config.port + '/~/game';

        // open the shared game realm file
        let realm = new Realm({
          sync: { user: user, url: gameUrl },
          schema: [Player.schema, Score.schema, Game.schema, Side.schema]
        });

        // make sure the app has synchronized on very first open
        if (realm.objects("Player").length == 0) return;

        print("updating availability on " + expired.length + " players");

        // loop over players, set available to false
        realm.write(() => {
          for (var i in expired) {
            let expiredId = expired[i];
            
            let player = realm.objectForPrimaryKey('Player', expiredId);
            if (isRealmObject(player)) {
              player.available = false;
              this.didUpdateAvailability(player.id, false);
            }
          }
        });
        
        print("updated availability");
      } else {
        print('error while connecting as the game admin: ' + error);
        process.exit(1);
      }
    });
  }
}

// reset everyone's availability
Server.prototype.resetAvailability = function() {
  Realm.Sync.User.login('http://' + this.config.host + ':' + this.config.port, this.config.username, this.config.password, (error, user) => {
      if (!error) {
        let gameUrl = 'realm://' + this.config.host + ':' + this.config.port + '/~/game';

        let realm = new Realm({
          sync: { user: user, url: gameUrl },
          schema: [Player.schema, Score.schema, Game.schema, Side.schema]
        });

        let players = realm.objects("Player");
        realm.write(() => {
          for (var i in players) {
            print('reset: ' + players[i].id);
            players[i].available = false;
          }
        });
      } else {
        print('error while connecting as the game admin: ' + error);
        process.exit(1);
      }
  });
}

// update the in-memory check-in cache
Server.prototype.didUpdateAvailability = function(userId, available) {

  if (available == false) {
    delete users[userId];
  } else {
    users[userId] = new Date().getTime();
  }

  //dumpUsers(users);
}

// debug method, use it to dump the active users in the console
function dumpUsers() {
  //debug output
  if (Object.keys(users).length > 0) {
    print("--------------");
    for (var i in users) {
      print("["+i+"] => "+users[i])
    }
    print("--------------");
  }
}

exports.Server = Server;
