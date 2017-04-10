//
//  NSError.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/9/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation

private extension NSError {

    convenience init(error: NSError, description: String?, recoverySuggestion: String?) {
        var userInfo = error.userInfo

        userInfo[NSLocalizedDescriptionKey] = description
        userInfo[NSLocalizedRecoverySuggestionErrorKey] = recoverySuggestion

        self.init(domain: error.domain, code: error.code, userInfo: userInfo)
    }
    
}
