//
//  Score.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/31/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

class Score: Object {

    dynamic var name = ""
    dynamic var time = 0.0

    convenience init(name: String, time: Double) {
        self.init()
        self.name = name
        self.time = time
    }
}
