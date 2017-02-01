//
//  BubbleView.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/31/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation
import UIKit

class BubbleView: UIView {

    private var color = UIColor(red: 254.0/255.0, green: 159.0/255.0, blue: 147.0/255.0, alpha: 0.8)
    private static let size = CGSize(width: 60, height: 60)

    private var number = 0

    public var tap: ((Int) -> Void)?

    static func bubble(number: Int, inRect: CGRect) -> BubbleView {
        let b = BubbleView()
        b.number = number

        let randomPt = CGPoint(
            x: Int(inRect.origin.x) + Int(arc4random_uniform(UInt32(inRect.width))),
            y: Int(inRect.origin.y) + Int(arc4random_uniform(UInt32(inRect.height)))
        )

        b.frame = CGRect(origin: randomPt, size: size)
        return b
    }

    override func willMove(toSuperview newSuperview: UIView?) {
        super.willMove(toSuperview: newSuperview)

        guard newSuperview != nil else { return }

        isUserInteractionEnabled = true

        let layer = CAShapeLayer()
        layer.fillColor = UIColor.black.cgColor
        layer.strokeColor = color.cgColor
        layer.lineWidth = 5
        layer.path = UIBezierPath(ovalIn: bounds).cgPath
        self.layer.addSublayer(layer)

        let number = UILabel(frame: bounds)
        number.textAlignment = .center
        number.font = UIFont.boldSystemFont(ofSize: 20)
        number.textColor = color
        number.text = String(self.number)
        addSubview(number)
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        removeFromSuperview()
        tap?(number)
    }
}
