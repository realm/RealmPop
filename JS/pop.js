'use strict';

//
// imports
//

var Realm = require('realm');

var Admin = require('./token.js');
var Setup = require('./setup-realm-pop.js');
var Game = require('./pop-event-handler.js');

//
// parse command line arguments
//

const args = process.argv.slice(2);

const config = {
  host: args[0],
  port: args[1]
};

const token = new Admin.Token();
token.load('admin', args[2]);
token.load('access', args[3]);

//
// run server setup
//

Setup.GameSetup.run(config, token.get('admin'));

//
// listen for events
//

let pop = new Game.Pop(config);
pop.connect(token.get('admin'), token.get('access'));

console.log('Completed');
