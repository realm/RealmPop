# Realm Pop


## Usage

A simple Realm Mobile Platform demo. It's a multiplayer game, in which players pair up and need to clear a level of numbered bubbles.

### Gameplay

Once on the game screen, players need to tap on the bubble with the highest number, once tapped the bubble disappears. The player should keep tapping on all bubbles in turn always selecting the current highest number.

* tapping a wrong number immediately ends the round, the opposite player wins,
* tapping "0" ends the round for the current player and shows their time, the game waits until the opposite player mistakes or taps all of their bubbles,
* if the player clears their level and the opposite side doesn't finish in another 20 seconds, the current player wins,
* if the player plays for more than 60 seconds they fail the round.

## Contents

### iOS 

Install dependencies via CocoaPods, replace the demo IP address with your own Realm Object Server address or IP in `RealmConnect.swift`, and finally build and run.

### Android

Android app, for installation instructions check Android/README.md

### JS

Node.js event handler to run on the server. Demonstrates how to react to object changes in Realm coming from remote clients and export the data in txt format to a file on the server. Check JS/install.txt.

### macOS

A "legacy system" server demo. This app looks for a `board.txt` file in the folder where the app is located and if found - displays it in a table view. `board.txt` is the file that the Node.js script exports.

This simple demo shows how a server legacy system might not depend in any way on Realm itself and use other formats/files to communicate with the  Realm Object Server.

## TODO

## License

Realm Pop is available under the MIT license. See the LICENSE file for more info.
