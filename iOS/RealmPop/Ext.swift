//
//  Ext.swift
//  EventBlank
//
//  Created by Marin Todorov on 3/12/15.
//  Copyright (c) 2015 Underplot ltd. All rights reserved.
//

import Foundation
import UIKit

public func delay(seconds: Double, completion: @escaping () -> Void) {
    DispatchQueue.main.asyncAfter(deadline: .now() + seconds, execute: completion)
}

public func backgroundQueue(block: @escaping () -> Void) {
    DispatchQueue.global(qos: .background).async(execute: block)
}

func mainQueue(block: @escaping ()->Void) {
    DispatchQueue.main.async(execute: block)
}
