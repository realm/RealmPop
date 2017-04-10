//
//  GameModel.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/25/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

class GameModel {

    let realm: Realm

    init?(config: Realm.Configuration = RealmConfig(.app).configuration) {
        guard let realm = try? Realm(configuration: config) else {
            return nil
        }
        self.realm = realm
    }

    func currentPlayer() -> Player {
        guard let id = SyncUser.current?.identity else {
            fatalError("Should not call this function if there's no logged in user")
        }

        if let player = realm.object(ofType: Player.self, forPrimaryKey: id) {
            return player
        }

        return createCurrentPlayer(id: id)
    }

    private func createCurrentPlayer(id: String) -> Player {
        // create private Player object
        let player = Player(id: id)
        try! realm.write {
            realm.add(player)
        }

        return player
    }

    func update(name: String) {
        let player = currentPlayer()
        try! player.realm?.write {
            player.name = name
        }
    }

    func otherPlayers(than me: Player) -> Results<ConnectedUser> {
        let usersRealm = RealmConfig(.users).realm

        return usersRealm.objects(ConnectedUser.self)
            //.filter("id != %@", me.id)
            .sorted(byKeyPath: "available", ascending: false)
    }

    private var challengeToken: NotificationToken?

    func challenge(me: Player, vs: ConnectedUser, completion: @escaping (String?) -> Void) {
        guard vs.available else { return }

        // build the game file URL
        let gameConfig = RealmConfig(.game)
        let gameUrl = gameConfig.url

        // TODO: unshare the game file with anyone else


        // offer the user access to the game file
        let challengeOffer = SyncPermissionOffer(
            realmURL: gameUrl.absoluteString,
            expiresAt: nil,
            mayRead: true,
            mayWrite: true,
            mayManage: false)

        // create the offer and send it off to the ros
        let managementRealm = try! SyncUser.current!.managementRealm()
        try! managementRealm.write {
            managementRealm.add(challengeOffer)
        }

        // observe for accepting the offer
        challengeToken = challengeOffer.addNotificationBlock { changes in
            switch changes {
            case .change where challengeOffer.status == .success:
                completion(challengeOffer.token)
            default:
                completion(nil)
            }
        }
    }

    private func sendChallenge() {

//        let listID = taskList.realm?.configuration.syncConfiguration?.realmURL.lastPathComponent
//
//        let syncConfig = Realm.Configuration.defaultConfiguration.syncConfiguration!
//
//        let rootRealmURL = syncConfig.realmURL.deletingLastPathComponent().deletingLastPathComponent()
//
//        let listRealmURL = rootRealmURL.appendingPathComponent("\(syncConfig.user.identity!)/\(listID!)")
//
//        let shareOffer = SyncPermissionOffer(realmURL: listRealmURL.absoluteString, expiresAt: nil, mayRead: true, mayWrite: true, mayManage: true)
//
//        let managementRealm = try! syncConfig.user.managementRealm()
//        try! managementRealm.write { managementRealm.add(shareOffer) }
//        shareOfferNotificationToken = managementRealm.objects(SyncPermissionOffer.self).filter("id = %@", shareOffer.id).addNotificationBlock { changes in
//            guard case let .update(change, _, _, _) = changes, let offer = change.first, offer.status == .success, let token = offer.token else { return }
//            let url = "realmtasks://" + token.replacingOccurrences(of: ":", with: "/")
//            let activityViewController = UIActivityViewController(activityItems: [url], applicationActivities: nil)
//            self.present(activityViewController, animated: true, completion: nil)
//        }
    }

    func createGame(me: Player, vs: ConnectedUser) {
        if vs.available {
            try! me.realm!.write {
                //let game = Game(challenger: vs, opponent: me)
                //me.currentGame = game
                //vs.currentGame = game
            }
        }
    }

    func updateTime(mySide: Side, myTime: Double, otherSide: Side) {
        try! mySide.realm!.write {
            mySide.time = myTime
            
            let iAmTheWinner = (otherSide.time == 0) || (otherSide.time > myTime)
            if iAmTheWinner {
                logTime(for: mySide)
            }
        }
    }

    func updateBubbles(side: Side, count: Int) {
        try! side.realm!.write {
            side.left = count
        }
    }

    func fail(side: Side) {
        try! side.realm?.write {
            side.failed = true
        }
    }

    func determineOutcome(mine mySide: Side, theirs otherSide: Side) {
        if otherSide.time > 0 && mySide.time > 0 {
            try! mySide.realm?.write {
                if otherSide.time < mySide.time {
                    mySide.failed = true
                } else {
                    otherSide.failed = true
                }
            }
        }
    }

    private func logTime(for side: Side) {
        realm.add(Score(name: side.name, time: side.time))
    }
}
