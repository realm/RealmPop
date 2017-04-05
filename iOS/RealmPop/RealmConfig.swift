//
//  RealmConfig.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/4/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

enum RealmFile {
    case users
    case app
    case game(String)
}

struct RealmConfig {

    private let file: RealmFile
    private(set) var configuration: Realm.Configuration!

    init(_ realmFile: RealmFile) {
        guard let user = SyncUser.current else {
            fatalError("can't instantiate without logged user")
        }

        file = realmFile
        configuration = configurationForRealm(user: user, file: file, version: 1)
    }

    var realm: Realm {
        return try! Realm(configuration: self.configuration)
    }

    var path: String {
        switch file {
        case .app: return "/~/app"
        case .users: return "/users"
        case .game(let name): return "/~/\(name)"
        }
    }

    var schema: [Object.Type] {
        switch file {
        case .app: return [Player.self]
        case .users: return [ConnectedUser.self]
        case .game(let name): return [Game.self, Side.self]
        }
    }

    var url: URL {
        var syncURL = URLComponents()
        syncURL.scheme = "realm"
        syncURL.host = host
        syncURL.port = 9080
        syncURL.path = path
        return syncURL.url!
    }

    private func configurationForRealm(user: SyncUser, file: RealmFile, isPrivate: Bool = true, version: Int = 1) -> Realm.Configuration {
        var config = Realm.Configuration()
        config.schemaVersion = UInt64(version)
        config.syncConfiguration = SyncConfiguration(user: user, realmURL: url)
        config.deleteRealmIfMigrationNeeded = true
        config.objectTypes = schema
        return config
    }
}

extension Realm.Configuration {
    var defaultConfiguration: Realm.Configuration {
        fatalError()
    }
}
