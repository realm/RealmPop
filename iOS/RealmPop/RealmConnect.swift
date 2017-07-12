//
//  RealmConnect.swift
//
//  Created by Marin Todorov on 12/14/16.
//  Copyright © 2016 Realm. All rights reserved.
//

import Foundation
import RealmSwift

var host = "45.55.81.82"

let user = "default@realm"
let pass = "password"

func connect(completion: @escaping (Bool) -> Void) {
    print("connecting to \(host)")

    let cred = SyncCredentials.usernamePassword(
        username: user, password: pass, register: false)

    SyncUser.logIn(with: cred, server: URL(string: "http://\(host):9080")!) {user, error in
        if let error = error {
            print("error: \(error.localizedDescription)")
        }

        guard let user = user else {
            completion(false)
            return
        }

        var config = Realm.Configuration.defaultConfiguration
        config.schemaVersion = 1
        config.syncConfiguration = SyncConfiguration(user: user, realmURL: URL(string: "realm://\(host):9080/~/game")!)
        Realm.Configuration.defaultConfiguration = config

        DispatchQueue.main.async {
            completion(true)
        }
    }
}
