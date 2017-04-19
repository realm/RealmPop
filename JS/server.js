var Realm = require('realm');

var users = new Object();

var {
  Player, Score, Game, Side,
} = require('./server-schema');

function Server(config, adminToken) {
  this.minutesToExpire = 1.0;
  this.callbackMinutesInterval = 0.1;

  this.config = config;
  this.timer = null;
  Server.adminUser = Realm.Sync.User.adminUser(adminToken);
  Server.instance = this;
}

Server.prototype.start = function() {
  this.timer = setInterval(Server.callback, this.callbackMinutesInterval * 60000);
}

Server.prototype.stop = function() {
  if (this.timer != null) {
    clearInterval(this.timer);
  }
}

Server.callback = function() {
  Server.instance.callback();
}

Server.prototype.callback = function() {
  //print('server callback');

  //dumpUsers(users);

  let unavailableTime = new Date().getTime() - this.minutesToExpire * 60000;

  var expired = new Array();

  for (var id in users) {
    //print('compare: now ' + unavailableTime + ' to last: ' + users[id] + ' diff: '+ (users[id] - unavailableTime));

    if ( users[id] < unavailableTime ) {
      print('set to unavailable: ' + id);
      expired.push(id);
    };
  }

  //available
  if (expired.length > 0) {

    Realm.Sync.User.login('http://' + this.config.host + ':' + this.config.port, 'default@realm', 'password', (error, user) => {
      //print('connected');

      if (!error) {
        let gameUrl = 'realm://' + this.config.host + ':' + this.config.port + '/~/game';
        //print('opening: ' + gameUrl);

        let realm = new Realm({
          sync: { user: user, url: gameUrl },
          schema: [Player.schema, Score.schema, Game.schema, Side.schema]
        });

        var shouldUpdate = true;
        realm.objects("Player").addListener((objects, changes) => {

          if (shouldUpdate && objects.length > 0) {

            shouldUpdate = false;
            realm.removeAllListeners();

            print("updating " + expired.length + " players");

            realm.write(() => {
              for (var i in expired) {
                let expiredId = expired[i];
                print('find player with ID: ' + expiredId);
                let player = realm.objectForPrimaryKey('Player', expiredId);
                player.available = false;
                this.didUpdateAvailability(player.id, false);
              }
            });
            
            print("updated.");
            //dumpUsers(users);
          };
        });

      } else {
        print('error: ' + error);
        process.exit(1);
      }
    });
  }
}

Server.prototype.didUpdateAvailability = function(userId, available) {

  if (available == false) {
    delete users[userId];
  } else {
    users[userId] = new Date().getTime();
  }

  //dumpUsers(users);
}

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
