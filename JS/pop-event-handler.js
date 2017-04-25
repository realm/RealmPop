//
// Server event handler
//

var Realm = require('realm');
var { Board } = require('./board.js');

/**
 * A singleton RealmPop server event handler
 *
 * Reacts to changes in the game realm:
 *   * catches submitted score objects and hands them off to Board
 *   * hands off Player changes to the Server class
 */
function Pop(config, credentials) {

  Pop.config = config;
  Pop.serverUrl = 'realm://' + Pop.config.host + ':' + Pop.config.port;
  Pop.adminUser = null;
  Pop.instance = this;

  this.uid = credentials.split(':')[0];
  this.didUpdateAvailability = null;
  this.path = '\/'+this.uid+'\/game';
}

// connects to Realm Object Server, installs an event listener
Pop.prototype.connect = function(adminToken) {
  print('Trying to connect');

  Pop.adminUser = Realm.Sync.User.adminUser(adminToken);
  Realm.Sync.addListener(Pop.serverUrl, Pop.adminUser, this.path, 'change', Pop.changeCallback);

  print('Pop app at: ' + Pop.serverUrl + " observing changes at: " + this.path);
}

// Player object changes handler, relays relevant changes to didUpdateAvailability()
Pop.handlePlayerEvents = function(event) {
  
  // check if there are Player change events
  const changesPlayer = event.changes.Player;
  if (!changesPlayer) return;

  // check if there are Player modifications
  const indexesPlayers = changesPlayer.modifications;
  if (indexesPlayers.length == 0) return;

  // fetch Player list
  const players = event.realm.objects("Player");

  // loop over modified Player objects
  for (const i in indexesPlayers) {
    const index = indexesPlayers[i];
    const player = players[index];

    if (!!player) {
      // update player's availability
      print("player: " + player.name + " > " + player.available)
      Pop.instance.didUpdateAvailability(player.id, player.available);
    }
  }
}

// Score object changes handler, adds new high scores via Board.addScore()
Pop.handleScoreEvents = function(event) {
  
  // check if there are Score change events
  const changes = event.changes.Score;
  if (!changes) return;

  // check if there are Score insertions
  const indexes = changes.insertions;
  if (indexes.length == 0) return;

  // fetch the newly inserted Score objects
  const realm = event.realm;
  const scores = realm.objects("Score");

  // loop over the Score objects
  for (const i in indexes) {
    const index = indexes[i];
    const score = scores[index];

    if (!!score) {
      print("score: " + score.name + " > " + score.time)

      // add score to the high score board
      Board.addScore(score, () => {
        // delete processed Score objects
        realm.write(() => {
          realm.delete(score);
        });
      });
    }
  }
}

// event callback handler
Pop.changeCallback = function(event) {
  Pop.handlePlayerEvents(event);
  Pop.handleScoreEvents(event);
}

exports.Pop = Pop;
