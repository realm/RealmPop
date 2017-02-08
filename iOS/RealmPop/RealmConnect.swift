//
//  RealmConnect.swift
//  Inboxly
//
//  Created by Marin Todorov on 12/14/16.
//  Copyright © 2016 Realm. All rights reserved.
//

import Foundation
import RealmSwift

let host = "192.168.1.33"
let serverURL = URL(string: "http://\(host):9080")!
let syncURL = URL(string: "realm://\(host):9080/~/game")!

let user = "default@realm"
let pass = "password"

func connect(completion: @escaping () -> Void) {
    let cred = SyncCredentials.usernamePassword(
        username: user, password: pass, register: false)

    SyncUser.logIn(with: cred, server: serverURL) {user, error in
        guard let user = user else {
            return DispatchQueue.main.asyncAfter(deadline: .now() + 5) {
                connect(completion: completion)
            }
        }

        var config = Realm.Configuration.defaultConfiguration
        config.syncConfiguration = SyncConfiguration(user: user, realmURL: syncURL)
        Realm.Configuration.defaultConfiguration = config

        DispatchQueue.main.async(execute: completion)
    }
}
