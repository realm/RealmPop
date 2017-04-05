//
//  ConnectedUser.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/4/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

class ConnectedUser: Object {

    dynamic var id = ""
    dynamic var creationDate = Date()
    dynamic var username = ""
    dynamic var lastUpdate = Date()
    dynamic var isAvailable = false

    convenience init(_ id: String) {
        self.init()
        self.id = id
    }

    override static func primaryKey() -> String? {
        return "id"
    }

    override static func indexedProperties() -> [String] {
        return ["username"]
    }
}
