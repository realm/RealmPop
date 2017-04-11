//
//  AppDelegate.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/25/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit
import RealmSwift

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey : Any]? = nil) -> Bool {
        application.isIdleTimerDisabled = true
        SyncManager.shared.logLevel = .error
        observeNotification(name: kSharedRealmFileCreatedNotification, selector: #selector(didCreateSharedRealm))
        return true
    }
}

extension AppDelegate {
    func didCreateSharedRealm() {
        let alert = UIAlertController(title: "Success", message: "Admin shared realm created, now log in as a normal user.", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Close", style: .default, handler: {[unowned self] _ in
            SyncUser.current!.logOut()
            (self.window!.rootViewController as! UINavigationController).popToRootViewController(animated: false)
            alert.dismiss(animated: true)
        }))
        window!.rootViewController?.present(alert, animated: true, completion: nil)
    }
}
