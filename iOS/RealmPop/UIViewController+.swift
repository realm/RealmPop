//
//  UIViewController+.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/31/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import UIKit

extension UIViewController {
    func alert(message: String, completion: (()->Void)? = nil) {
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Close", style: .default, handler: {[weak self] _ in
            self?.dismiss(animated: true, completion: completion)
        }))
        present(alert, animated: true, completion: nil)
    }
}
