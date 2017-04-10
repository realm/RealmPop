//
// Realm Schema
//

// ConnectedUser

function ConnectedUser() {
  this.available = false
}

ConnectedUser.schema = {
  name: 'ConnectedUser',
  primaryKey: 'id',
  properties: {
    id: {type: 'string'},
    creationDate: {type: 'date'},
    username: {type: 'string'},
    lastUpdate: {type: 'date'},
    available: {type: 'bool', default: false}
  }
};

// Player

function Player() {
  this.username = ""
}

Player.schema = {
  name: 'Player',
  primaryKey: 'id',
  properties: {
    id: {type: 'string'},
    name: {type: 'string'},
    available: {type: 'bool', default: false},
    challengerId: {type: 'string'},
    currentGame: {type: 'string'}
  }
};

// exports
exports.ConnectedUser = ConnectedUser;
exports.Player = Player;
