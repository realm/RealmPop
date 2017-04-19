//
//  NSObject+.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/19/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation

extension NSObject {

    func observeNotification(notification name: Notification.Name, selector: Selector?) {
        if let selector = selector {
            NotificationCenter.default.addObserver(self, selector: selector, name: name, object: nil)
        } else {
            NotificationCenter.default.removeObserver(self, name: name, object: nil)
        }
    }

    func observeNotification(name: String, selector: Selector?) {
        if let selector = selector {
            NotificationCenter.default.addObserver(self, selector: selector, name: Notification.Name(name), object: nil)
        } else {
            NotificationCenter.default.removeObserver(self, name: Notification.Name(name), object: nil)
        }
    }

    func notification(name: String, object: AnyObject? = nil) {
        if let dict = object as? NSDictionary {
            NotificationCenter.default.post(name: Notification.Name(name), object: nil, userInfo: dict as [NSObject: AnyObject])
        } else if let object: AnyObject = object {
            NotificationCenter.default.post(name: Notification.Name(name), object: nil, userInfo: ["object": object])
        } else {
            NotificationCenter.default.post(name: Notification.Name(name), object: nil, userInfo: nil)
        }
    }

}
