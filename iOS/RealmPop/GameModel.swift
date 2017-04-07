//
//  GameModel.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/25/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

class GameModel {

    let realm: Realm

    init?(config: Realm.Configuration = RealmConfig(.app).configuration) {
        guard let realm = try? Realm(configuration: config) else {
            return nil
        }
        self.realm = realm
    }

    func currentPlayer() -> Player {
        let currentId = UserDefaults.idForCurrentPlayer()

        if let player = realm.object(ofType: Player.self, forPrimaryKey: currentId) {
            return player
        }

        let player = Player(id: currentId)
        try! realm.write {
            realm.add(player)
        }
        return player
    }

    func update(name: String) {
        let player = currentPlayer()
        try! player.realm?.write {
            player.name = name
        }
    }

    func otherPlayers(than me: Player) -> Results<Player> {
        return realm.objects(Player.self)
            .filter("id != %@", me.id)
            .sorted(byKeyPath: "available", ascending: false)
    }

    func challenge(me: Player, vs: Player) {
        guard vs.available else { return }

        try! me.realm!.write {
            for player in me.realm!.objects(Player.self).filter("challenger = %@", me) {
                player.challenger = nil
            }
            //vs.challenger = me
        }
    }

    func createGame(me: Player, vs: Player) {
        if vs.available {
            try! me.realm!.write {
                let game = Game(challenger: vs, opponent: me)
                //me.currentGame = game
                //vs.currentGame = game
            }
        }
    }

    func updateTime(mySide: Side, myTime: Double, otherSide: Side) {
        try! mySide.realm!.write {
            mySide.time = myTime
            
            let iAmTheWinner = (otherSide.time == 0) || (otherSide.time > myTime)
            if iAmTheWinner {
                logTime(for: mySide)
            }
        }
    }

    func updateBubbles(side: Side, count: Int) {
        try! side.realm!.write {
            side.left = count
        }
    }

    func fail(side: Side) {
        try! side.realm?.write {
            side.failed = true
        }
    }

    func determineOutcome(mine mySide: Side, theirs otherSide: Side) {
        if otherSide.time > 0 && mySide.time > 0 {
            try! mySide.realm?.write {
                if otherSide.time < mySide.time {
                    mySide.failed = true
                } else {
                    otherSide.failed = true
                }
            }
        }
    }

    private func logTime(for side: Side) {
        realm.add(Score(name: side.name, time: side.time))
    }
}
