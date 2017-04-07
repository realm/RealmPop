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

    private var me: ConnectedUser?
    private var username: String?

    // MARK: - View controller life cycle

    override func viewDidLoad() {
        super.viewDidLoad()

        navigationController!.navigationBar.isHidden = true
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        if let user = SyncUser.current {
            // user logged in, proceed to username screen

            setupDefaultGlobalPermissions(user: user)
            me = createOrFetchConnectedUser(with: user)

            showGameRoomViewController()

        } else {
            // show login screen

            let loginViewController = LoginViewController(style: .darkOpaque)

            if loginViewController.serverURL == nil {
                loginViewController.serverURL = defaultSyncHost
            }

            loginViewController.loginSuccessfulHandler = { user in
                DispatchQueue.main.async {[unowned self] in
                    self.username = loginViewController.username
                    loginViewController.dismiss(animated: true, completion: nil)
                }
            }

            present(loginViewController, animated: false, completion: nil)
        }
    }

    private func showGameRoomViewController() {
        navigationController!.navigationBar.isHidden = false
        navigationController!.pushViewController(PreGameRoomViewController.create(username: username), animated: true)
    }

    // MARK: - Data stuffs

    private func createOrFetchConnectedUser(with user: SyncUser) -> ConnectedUser {
        host = user.authenticationServer!.host!

        let usersConfig = RealmConfig(.users)
        guard let identity = SyncUser.current?.identity else {
            fatalError("Should have user at this point")
        }

        return usersConfig.realm.object(ofType: ConnectedUser.self, forPrimaryKey: identity) ?? {
            //create user
            let me = ConnectedUser(identity)
            try! usersConfig.realm.write {
                usersConfig.realm.add(me, update: true)
            }
            return me
        }()
    }

    private var permissionsToken: NotificationToken!

    private func setupDefaultGlobalPermissions(user: SyncUser) {
        return;

        let managementRealm = try! user.managementRealm()
        let fileUrl = RealmConfig(.users).url
        let permissionChange = SyncPermissionChange(realmURL: fileUrl.absoluteString,
            userID: "*",
            mayRead: true,
            mayWrite: false,
            mayManage: false)

        permissionsToken = managementRealm.objects(SyncPermissionChange.self)
            .filter("id = %@", permissionChange.id).addNotificationBlock {[weak self] notification in
            if case .update(let changes, _, _, _) = notification, let change = changes.first {
                // Object Server processed the permission change operation
                switch change.status {
                case .notProcessed:
                    print("not processed.")
                case .success:
                    self?.notification(name: kSharedRealmFileCreatedNotification)
                case .error:
                    print("Error.")
                }
                print("change notification: \(change.debugDescription)")
            }
        }

        try! managementRealm.write {
            managementRealm.add(permissionChange)
        }
    }

}
