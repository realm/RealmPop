'use strict';

//
// TODO: 1. replace with your own admin token before starting the app
// 2. replace the server IP
//

const REALM_ADMIN_TOKEN = "ewoJImlkZW50aXR5IjogIl9fYXV0aCIsCgkiYWNjZXNzIjogWyJ1cGxvYWQiLCAiZG93bmxvYWQiLCAibWFuYWdlIl0KfQo=:JrlE/EenpDV8n8a6UZd3ybmIEsVobO+eblIuVT+g+GEzgX1JSXmMe5qDot6TapEshQkq5k2NUGsxXKug8eIW277ijjN9Vh7teJbau9HykkUQHGG238kdBn+jlUhJbgRy5wQ7Om0ft/A9qSxtueYoEGXZtJZHFtvs/6dIoK7KuVSy1BMsh19b6164z2ZX8dZv/kqQRbC4luU/9HbK1IGyUevD9pgS8Am010PC9dRimfO1mdxekgTTCaaYAVicWmIEw44wNrNOMKVzJg0xnv9VtCTdDBLPdcDc8JEOQPuaaThO2eGCBCKJQMD//WtFf2z9IfTFeRRc7Jrfl8o4PKOH7Q==";

const IP = '192.168.1.33';

var fs = require('fs');
var Realm = require('realm');

function isRealmObject(x) {
  return x !== null && x !== undefined && x.constructor === Realm.Object
}

/**
 * Pop game class
 *
 * Connects to Realm Object Server, installs event listener, processes Score object insertions
 */

function Pop(url, path) {
  this.url = url;
  this.path = path;
}

Pop.prototype.connect = function(token) {
  var admin = Realm.Sync.User.adminUser(token);

  Realm.Sync.addListener(this.url, admin, this.path, 'change', Pop.changeCallback);
  console.log('Pop app at: ' + this.url + " observing changes at: " + this.path);
}

Pop.changeCallback = function(event) {

  let realm = event.realm;

  let changes = event.changes.Score;
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

var pop = new Pop('realm://'+IP+':9080', '.*/game')
pop.connect(REALM_ADMIN_TOKEN)
