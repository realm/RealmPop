# Realm Pop

<img src=http://i.imgur.com/pTxoTPX.png width=300> <img src=http://i.imgur.com/Zrvfypq.png width=300>

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

Android version of RealmPop.  Installation instructions are below.

### JS

Node.js event handler to run on the server. Demonstrates how to react to object changes in Realm coming from remote clients and export the data in txt format to a file on the server. Check JS/install.txt.

### macOS

A "legacy system" server demo. This app looks for a `board.txt` file in the folder where the app is located and if found - displays it in a table view. `board.txt` is the file that the Node.js script exports.

This simple demo shows how a server legacy system might not depend in any way on Realm itself and use other formats/files to communicate with the  Realm Object Server.

## Installation

Here are the instructions to run the project locally.

#### Server: 
  * download PE Trial from here: https://realm.io/pricing/
  * unpack the archive on your computer, and from the resulting folder start the server by typing: `./start-object-server.command`
  * in the web browser that pops up create an admin account
  * once logged in the web console, click "+New User", enter email "`default@realm`", password "`password`"

#### iOS:
  * install all dependencies of the iOS project. In the shell, navigate to `RealmPop/iOS` and type in `pod install` to install the dependencies
 * open the `RealmConnect.swift` file and at the top put in the IP of your server (if you run the server on your computer, open System Preference/Network - your IP is towards the top of the window)
 
#### Android:

 * [See Android Sub-project](Android#installation-instructions)

#### Event handler:
 * install the latest Node.js from https://nodejs.org/en/
 * copy 'realm-1.3.0-professional.tgz' from your PE/EE folder to the JS folder of this repo (version might differ for you)
 * run 'npm install'
 * start the app by typing in the shell 'node pop.js'

Any time a player stores their high score, the event handler will grab the Score object and append it a `board.txt` file in the `JS` folder.

#### Legacy system:
 * build the project located in `RealmPop/macOS` (it has no dependencies)
 * once built, in Xode inside the Project Navigator find the file `PopBoard.app`, right-click it and choose "Show in Finder"
 * copy `PopBoard.app` from Finder into the `JS` folder and start the app

The app will monitor `board.txt` and display a dynamically updating score board.

## TODO

## License

Realm Pop is available under the MIT license. See the LICENSE file for more info.

_Press Start 2p_ is a free font under the SIL Open Font License(OFL). For more information: [http://www.fontspace.com/codeman38/press-start-2p](http://www.fontspace.com/codeman38/press-start-2p).
