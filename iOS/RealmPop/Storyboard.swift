//
//  Storyboard.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/5/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit

extension UIStoryboard {
    static func instantiateViewController<T>(ofType type: T.Type, storyboardName: String = "Main") -> T {
        return UIStoryboard(name: storyboardName, bundle: nil)
            .instantiateViewController(withIdentifier: String(describing: type)) as! T
    }
}
