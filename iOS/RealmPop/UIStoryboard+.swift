//
//  UIStoryboard+.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/19/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit

extension UIStoryboard {
    func instantiateViewController<T>(ofType type: T.Type) -> T {
        return instantiateViewController(withIdentifier: String(describing: type)) as! T
    }
}
