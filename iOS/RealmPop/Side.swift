//
//  Side.swift
//  RealmPop
//
//  Created by Marin Todorov on 2/7/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

class Side: Object {
    dynamic var name = ""
    dynamic var left = 0
    dynamic var time = 0.0
    dynamic var failed = false
}
