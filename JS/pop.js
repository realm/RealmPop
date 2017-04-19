'use strict';

//
// imports
//

var fs = require('fs');
var Realm = require('realm');
var Admin = require('./token.js');

//
// parse command line arguments
//
global.print = function (line) { console.log('[pop] ' + line); }

const args = process.argv.slice(2);

if (args.length < 4) {
  print("start the pop server app with 4 parameters like so:\n  node pop.js IP PORT ADMIN_TOKEN_PATH ACCESS_TOKEN_PATH");
  process.exit(1);
}

const config = {
  host: args[0],
  port: args[1]
};

const token = new Admin.Token();
token.load('admin', args[2]);
token.load('access', args[3]);

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
  this.path = '.*/game';
}

Pop.prototype.connect = function(adminToken, accessToken) {
  // get access to ros
  Realm.Sync.setAccessToken(accessToken);
  Pop.adminUser = Realm.Sync.User.adminUser(adminToken);

  Realm.Sync.addListener(Pop.serverUrl, Pop.adminUser, this.path, 'change', Pop.changeCallback);
  console.log('Pop app at: ' + Pop.serverUrl + " observing changes at: " + this.path);
}

Pop.changeCallback = function(event) {

  let realm = event.realm;
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
      console.log("[score]: " + score.name + " > " + score.time)

      Board.addScore(score, function() {
        realm.write(function() {
          realm.delete(score)
        })
      })
    }
  }
}

/**
 * Score board class
 *
 * Saves scores to a text file
 */

var Board = {
  file: 'board.txt'
}

Board.addScore = function(score, success) {
  fs.appendFile(this.file, score.name+"\t"+score.time+"\n", function (err) {
    if (err) {
      return console.error(err);
    }
    success()
  })
}

//
// initialize and start the Pop app
//

let pop = new Pop(config);
pop.connect(token.get('admin'), token.get('access'));
