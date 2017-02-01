//
//  Game.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/31/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

class Bubble: Object {
    dynamic var number = 0

    convenience init(nr: Int) {
        self.init()
        self.number = nr
    }
}

class Side: Object {
    dynamic var name = ""
    let bubbles = List<Bubble>()
    dynamic var time = 0.0
    dynamic var failed = false
}

class Game: Object {
    dynamic var player1: Side?
    dynamic var player2: Side?

    convenience init(challenger: Player, opponent: Player) {
        self.init()

        let numbers = (0...15).reduce([0]) { acc, _  in
            return acc + [acc.last! + Int(arc4random_uniform(5)+1)]
        }

        let player1 = Side()
        player1.name = challenger.name
        player1.bubbles.append(objectsIn: numbers.map(Bubble.init))
        self.player1 = player1

        let player2 = Side()
        player2.name = opponent.name
        player2.bubbles.append(objectsIn: numbers.map(Bubble.init))
        self.player2 = player2
    }
}
