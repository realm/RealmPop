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
        tryToConnect()
        return true
    }

    private func tryToConnect() {
        connect { [unowned self] success in
            guard success else {
                self.couldntConnect()
                return
            }
            if let splash = self.window?.rootViewController,
                let nav = splash.storyboard?.instantiateViewController(withIdentifier: "Navigation") {
                self.window!.rootViewController = nav
            }
        }
    }

    private func couldntConnect() {
        let alert = UIAlertController(title: "Can't connect", message: "The app can't connect to the \(host) server as \(user).", preferredStyle: .alert)
        alert.addTextField(configurationHandler: { field in
            field.text = host
        })
        alert.addAction(UIAlertAction(title: "Retry", style: .default, handler: { [unowned self] action in
            host = alert.textFields?.first!.text ?? ""
            self.tryToConnect()
        }))
        self.window!.rootViewController?.present(alert, animated: true, completion: nil)
    }
}
