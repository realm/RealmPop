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

    init?(config: Realm.Configuration = Realm.Configuration.defaultConfiguration) {
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

    func otherPlayers(than me: Player) -> Results<Player> {
        return realm.objects(Player.self)
            .filter("id != %@", me.id)
            .sorted(byKeyPath: "name")
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

    func logTime(for side: Side) {
        realm.add(Score(name: side.name, time: side.time))
    }
}
