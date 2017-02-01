//
//  AppDelegate.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/25/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey : Any]? = nil) -> Bool {

        connect {[unowned self] in
            if let splash = self.window?.rootViewController,
                let nav = splash.storyboard?.instantiateViewController(withIdentifier: "Navigation") {
                self.window!.rootViewController = nav
            }
        }
        
        return true
    }
}
