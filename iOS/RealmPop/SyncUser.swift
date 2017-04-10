//
//  SyncUser.swift
//  RealmPop
//
//  Created by Marin Todorov on 4/9/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import RealmSwift

private var acceptShareNotificationToken: NotificationToken?

extension SyncUser {
    fileprivate func acceptShareToken(_ token: String) throws {
        let realm = try managementRealm()
        let response = SyncPermissionOfferResponse(token: token)
        try realm.write {
            realm.add(response)
        }
        acceptShareNotificationToken = realm.objects(SyncPermissionOfferResponse.self).filter("id = %@", response.id).addNotificationBlock { changes in
            print(changes)
            let response: SyncPermissionOfferResponse
            if case let .update(change, _, _, _) = changes, let theResponse = change.first {
                response = theResponse
            } else if case let .initial(change) = changes, let theResponse = change.first {
                response = theResponse
            } else {
                return
            }
            print(response)
            guard response.status == .success, let realmURL = response.realmUrl else { return }
            print(realmURL)
            
//            let defaultRealm = try! Realm()
//            let lists = defaultRealm.objects(TaskListList.self).first!.items
//            try! defaultRealm.write {
//                let listRef = TaskListReference()
//                listRef.fullServerPath = realmURL.replacingOccurrences(of: "realm://172.20.20.65:9080", with: "")
//                lists.append(listRef)
//            }
        }
    }
}
