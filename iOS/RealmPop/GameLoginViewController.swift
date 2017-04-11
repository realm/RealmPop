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

    // MARK: - View controller life cycle

    override func viewDidLoad() {
        super.viewDidLoad()
        navigationController!.navigationBar.isHidden = true
    }

    private var inSegue = false

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        print("viewWillAppear \(String(describing: SyncUser.current))");

        // has already signed in previously
        if let user = SyncUser.current, !inSegue {
            print("has sync user")
            // observer for received ConnectedUser and proceed to next screen
            fetchConnectedUser(with: user, completion: {[weak self] connectedUser in
                print("has connected user")
                self?.showPreGameRoomViewController(me: connectedUser)
            })
            return
        }

        // login or register
        showLoginViewController(defaultHost: defaultSyncHost) {[unowned self] username, user in
            self.inSegue = true
            self.fetchConnectedUser(with: user, completion: {[unowned self] connectedUser in
                print("showing next screen!")
                self.showPreGameRoomViewController(me: connectedUser)
                self.inSegue = false
            })
        }
    }

    private func showLoginViewController(defaultHost: String?, completion: @escaping (String?, SyncUser) -> Void) {
        let loginViewController = LoginViewController(style: .darkOpaque)
        if loginViewController.serverURL == nil {
            loginViewController.serverURL = defaultHost
        }
        loginViewController.loginSuccessfulHandler = {user in
            DispatchQueue.main.async {
                completion(loginViewController.username, user)
                loginViewController.dismiss(animated: true, completion: nil)
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
        let game = GameModel()!
        let player = game.currentPlayer()

        let usersRealm = RealmConfig(.users).realm

        if let me = usersRealm.object(ofType: ConnectedUser.self, forPrimaryKey: player.id) {
            print("ConnectedUser exists")
            completion(me)
            return
        }

        let users = usersRealm.objects(ConnectedUser.self).filter(NSPredicate(format: "id == %@", player.id))
        userToken = users.addNotificationBlock { [weak self] changes in
            if let me = users.first {
                // ConnectedUser created, move on
                print("ConnectedUser created")
                self?.userToken = nil
                completion(me)
            }
        }
    }
}
