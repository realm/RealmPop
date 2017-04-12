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

class GameLoginViewController: UIViewController {

    // MARK: - View controller life cycle

    override func viewDidLoad() {
        super.viewDidLoad()

        // create login screen, add it on top of self
        let loginViewController = LoginViewController(style: .darkOpaque)
        if loginViewController.serverURL == nil {
            loginViewController.serverURL = defaultSyncHost
        }

        addChildViewController(loginViewController)
        view.addSubview(loginViewController.view)
        loginViewController.didMove(toParentViewController: self)

        loginViewController.loginSuccessfulHandler = {user in
            print("did login: \(user.identity!)")
            mainQueue {[unowned self] in
                self.didLogIn(with: user)
            }
        }

        // detect multiple users
        if SyncUser.all.count > 1 {
            for (_, user) in SyncUser.all {
                user.logOut()
            }
        }
    }

    private func didLogIn(with user: SyncUser) {
        _ = GameModel().currentPlayer(for: user)
        showSetupViewController(user: user)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController!.navigationBar.isHidden = true
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if let user = SyncUser.current {
            showSetupViewController(user: user)
        }
    }

    private func showSetupViewController(user: SyncUser) {
        navigationController!.pushViewController(SetupViewController.create(user: user), animated: true)
    }
}
