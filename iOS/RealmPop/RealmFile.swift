//
//  RealmConfig.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/4/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

extension URL {
    func expandUserFolder(for user: SyncUser) -> URL {
        return URL(string: self.absoluteString.replacingOccurrences(of: "~", with: user.identity!))!
    }
    func replaceUserFolder(with path: String) -> URL {
        return URL(string: self.absoluteString.replacingOccurrences(of: "~", with: path))!
    }
}

enum RealmFile {

    case users // shared admin-to-all read-only user list
    case app   // private user data
    case game  // shared 1-to-1 file with game data

    var path: String {
        switch self {
        case .app: return "/~/app"
        case .users: return "/users"
        case .game: return "/~/game"
        }
    }

    var url: URL {
        return URL(string: "realm://localhost:9080\(path)")!
    }

    var schema: [Object.Type] {
        switch self {
        case .app: return [Player.self]
        case .users: return [ConnectedUser.self]
        case .game: return [Game.self, Side.self]
        }
    }

    var configuration: Realm.Configuration {
        var config = Realm.Configuration()
        config.schemaVersion = UInt64(1)
        config.syncConfiguration = SyncConfiguration(user: SyncUser.current!, realmURL: url)
        config.deleteRealmIfMigrationNeeded = true
        config.objectTypes = schema
        return config
    }

    var realm: Realm {
        return try! Realm(configuration: configuration)
    }
}

extension Realm.Configuration {
    var defaultConfiguration: Realm.Configuration {
        fatalError()
    }
}
