//
//  Game.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/31/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

class Game: Object {
    dynamic var player1: Side?
    dynamic var player2: Side?
    dynamic var numbers = ""

    convenience init(challenger: Player, opponent: Player) {
        self.init()

        //create this game's numbers
        let ints = (0...5).reduce([0]) { acc, _  in
            return acc + [acc.last! + Int(arc4random_uniform(5)+1)]
        }
        numbers = ints.map(String.init).joined(separator: ",")

        //create player1 side
        let player1 = Side()
        player1.left = ints.count
        player1.name = challenger.name
        player1.playerId = challenger.id
        self.player1 = player1

        //create player2 side
        let player2 = Side()
        player2.left = ints.count
        player2.name = opponent.name
        player2.playerId = opponent.id
        self.player2 = player2
    }

    func isActive() -> Bool {
        return !player1!.failed && !player2!.failed
    }

    static func createEmpty() {
        // add faux object to create the file on the server
        let gameRealm = RealmFile.game.realm
        guard gameRealm.isEmpty else {
            return
        }

        try! gameRealm.write {
            gameRealm.add(Game())
        }
    }
}
