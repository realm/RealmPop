//
//  UserDefaults+.swift
//  RealmTTT
//
//  Created by Marin Todorov on 1/6/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation

extension UserDefaults {
    static private let defaultsUserKey = "defaultsUserKey"

    static func idForCurrentPlayer() -> String {
        return standard.value(forKey: defaultsUserKey) as? String ?? {
            let id = UUID().uuidString
            standard.setValue(id, forKey: defaultsUserKey)
            standard.synchronize()
            return id
        }()
    }
}
