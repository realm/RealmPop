//
//  SetupViewController.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/12/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit
import RealmSwift
import SwiftSpinner

func userCount() {
    print("user count: \(RealmFile.users.realm.objects(ConnectedUser.self).count)")
}

class SetupViewController: UIViewController {

    private var syncToken: NotificationToken?

    //private var usersRealm: Realm = {
    //    return RealmFile.users.realm
    //}()
    private var user: SyncUser!
    //private var users: Results<ConnectedUser>?

    static func create(user: SyncUser) -> SetupViewController {
        return UIStoryboard.instantiateViewController(ofType: self).then { vc in
            vc.user = user
        }
    }

    deinit {
        print("deiniting setup vc")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        userCount()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        userCount()
        fetchConnectedUserAndStart()
    }

    private func fetchConnectedUserAndStart() {
        // create player
        let player = GameModel().currentPlayer(for: user)

        // create game
        Game.createEmpty()

        // create connected user
//        users = usersRealm.objects(ConnectedUser.self)
//        print("users count: \(users!.count)")

        if let connectedUser = RealmFile.users.realm.object(ofType: ConnectedUser.self, forPrimaryKey: player.id) {
            SwiftSpinner.hide()
            showPreGameRoomViewController(me: connectedUser)
            return
        }

        SwiftSpinner.show("Setting up...")

        let usersUrl = RealmFile.users.url
        userCount()
        if let session = SyncUser.current?.session(for: usersUrl), session.state == .active {
            //syncing
            print("active sync session")
            syncToken?.stop()
            syncToken = session.addProgressNotification(for: .download, mode: .forCurrentlyOutstandingWork) { progress in
                mainQueue {[weak self] in
                    SwiftSpinner.show(progress: Double(progress.fractionTransferred), title: "Downloading...")

                    if progress.isTransferComplete {
                        self?.syncToken?.stop()
                        SwiftSpinner.hide()

                        delay(seconds: 1, completion: {[weak self] in
                            self?.fetchConnectedUserAndStart()
                        })
                    }
                }
            }
        } else {
            // retry
            print("no active session")
            delay(seconds: 3, completion: {[weak self] in
                self?.fetchConnectedUserAndStart()
            })
        }
    }

    private func showPreGameRoomViewController(me: ConnectedUser) {
        navigationController!.navigationBar.isHidden = false
        navigationController!.pushViewController(PreGameRoomViewController.create(connectedUser: me), animated: true)
    }
}
