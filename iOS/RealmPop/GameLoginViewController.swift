//
//  LoginViewController.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/4/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit
import RealmSwift
import RealmLoginKit

let kSharedRealmFileCreatedNotification = "kSharedRealmFileCreatedNotification"

class GameLoginViewController: UIViewController {

    private var username: String?

    // MARK: - View controller life cycle

    override func viewDidLoad() {
        super.viewDidLoad()

        navigationController!.navigationBar.isHidden = true
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        if let user = SyncUser.current {
            // observer for received ConnectedUser and proceed to next screen
            fetchConnectedUser(with: user, completion: {[weak self] connectedUser in
                self?.showPreGameRoomViewController(me: connectedUser)
            })
        } else {
            // show login screen
            showLoginViewController(defaultHost: defaultSyncHost) {[unowned self] username, user in
                self.username = username
            }
        }
    }

    private func showLoginViewController(defaultHost: String?, completion: @escaping (String?, SyncUser) -> Void) {
        let loginViewController = LoginViewController(style: .darkOpaque)
        if loginViewController.serverURL == nil {
            loginViewController.serverURL = defaultHost
        }
        loginViewController.loginSuccessfulHandler = {user in
            DispatchQueue.main.async {
                loginViewController.dismiss(animated: true, completion: nil)
                completion(loginViewController.username, user)
            }
        }
        present(loginViewController, animated: false, completion: nil)
    }

    private func showPreGameRoomViewController(me: ConnectedUser) {
        navigationController!.navigationBar.isHidden = false
        navigationController!.pushViewController(PreGameRoomViewController.create(connectedUser: me), animated: true)
    }

    // MARK: - Data stuffs
    private var userToken: NotificationToken?

    private func fetchConnectedUser(with user: SyncUser, completion: @escaping (ConnectedUser)->Void ) {
        guard let identity = SyncUser.current?.identity else {
            fatalError("Should have user at this point")
        }

        let game = GameModel()!
        let player = game.currentPlayer()

        let usersRealm = RealmConfig(.users).realm

        if let user = usersRealm.object(ofType: ConnectedUser.self, forPrimaryKey: player.id) {
            completion(user)
            return
        }

        userToken = usersRealm.objects(ConnectedUser.self)
            .filter(NSPredicate(format: "id == %@", player.id))
            .addNotificationBlock { [weak self] changes in

                switch changes {
                case .initial(let users):
                    if !users.isEmpty {
                        completion(users.first!)
                        self?.userToken = nil
                    }
                case .update(let users, _, _, _):
                    if !users.isEmpty {
                        completion(users.first!)
                        self?.userToken = nil
                    }
                default:
                    fatalError("Errored out")
                }
            }
    }
}
