var Realm = require('realm');
var { Board } = require('./board.js');

function isRealmObject(x) {
  return x !== null && x !== undefined && x.constructor === Realm.Object
}

/**
 * Pop game class
 *
 * Connects to Realm Object Server, installs event listener, processes Score object insertions
 */

function Pop(config) {
  Pop.config = config;
  Pop.serverUrl = 'realm://' + Pop.config.host + ':' + Pop.config.port;
  Pop.adminUser = null;
  Pop.instance = this;
  this.didUpdateAvailability = null;
  this.path = '.*game';
}

Pop.prototype.connect = function(adminToken) {
  
  print('Trying to connect');

  Pop.adminUser = Realm.Sync.User.adminUser(adminToken);

  Realm.Sync.addListener(Pop.serverUrl, Pop.adminUser, this.path, 'change', Pop.changeCallback);
  print('Pop app at: ' + Pop.serverUrl + " observing changes at: " + this.path);
}

Pop.handlePlayerEvents = function(event) {
  let realm = event.realm;

   // Player
  let changesPlayer = event.changes.Player;
  if (changesPlayer == undefined) { 
    return
  }
  let indexesPlayers = changesPlayer.modifications;
  
  if (indexesPlayers.length == 0) return;

  var players = realm.objects("Player");

  for (var i = 0; i < indexesPlayers.length; i++) {
    let index = indexesPlayers[i];
    let player = players[index];

    if (isRealmObject(player)) {
      print("player: " + player.name + " > " + player.available)
      Pop.instance.didUpdateAvailability(player.id, player.available);
    }
  }
}

Pop.handleScoreEvents = function(event) {
  let realm = event.realm;

  // Score
  let changes = event.changes.Score;
  if (changes == undefined) { 
    return
  }
  let indexes = changes.insertions;

  if (indexes.length == 0) return;

  var scores = realm.objects("Score");

  for (var i = 0; i < indexes.length; i++) {
    let index = indexes[i];
    let score = scores[index];

    if (isRealmObject(score)) {
      print("score: " + score.name + " > " + score.time)

      Board.addScore(score, function() {
        realm.write(function() {
          realm.delete(score)
        })
      })
    }
  }
}

Pop.changeCallback = function(event) {
  Pop.handlePlayerEvents(event);
  Pop.handleScoreEvents(event);
}

exports.Pop = Pop;
