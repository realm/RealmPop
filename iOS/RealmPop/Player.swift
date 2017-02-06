//
//  Player.swift
//  RealmTTT
//
//  Created by Marin Todorov on 9/29/16.
//  Copyright © 2016 Realm Inc. All rights reserved.
//

import RealmSwift

class Player: Object {
    dynamic var id = UUID().uuidString
    dynamic var name = "Anonymous"

    dynamic var available = false
    dynamic var challenger: Player?

    dynamic var currentGame: Game?

    convenience init(id: String) {
        self.init()
        self.id = id
    }

    override class func primaryKey() -> String? {
        return "id"
    }

    func resetState(available: Bool = false) {
        try? realm?.write {
            self.available = available
            challenger = nil
            currentGame = nil
        }
    }
}
