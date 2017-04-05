//
//  Notification.swift
//  EventBlank
//
//  Created by Marin Todorov on 6/22/15.
//  Copyright (c) 2015 Underplot ltd. All rights reserved.
//

import Foundation

extension NSObject {

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
