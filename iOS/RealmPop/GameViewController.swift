//
//  GameViewController.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/31/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit
import RealmSwift

class GameViewController: UIViewController {

    @IBOutlet var elapsed: UILabel!
    @IBOutlet var player1: UILabel!
    @IBOutlet var player2: UILabel!
    @IBOutlet var message: UILabel!

    fileprivate var game: GameModel!

    var challenge: Game!

    var haveChallenged = false

    private var mySide: Side!
    private var mySideToken: NotificationToken?

    private var otherSide: Side!
    private var otherSideToken: NotificationToken?

    private var staredAt: Date!

    override func viewDidLoad() {
        super.viewDidLoad()

        if haveChallenged {
            mySide = challenge.player1
            otherSide = challenge.player2
        } else {
            otherSide = challenge.player2
            mySide = challenge.player1
        }

        mySideToken = mySide.addNotificationBlock {[weak self] _ in
            self?.update()
        }
        otherSideToken = mySide.addNotificationBlock {[weak self] _ in
            self?.update()
        }

        //build UI
        let playRect = view.bounds.insetBy(dx: 40, dy: 70).offsetBy(dx: 0, dy: 30)

        for bubble in mySide.bubbles {
            let bubbleView = BubbleView.bubble(number: bubble.number, inRect: playRect)
            bubbleView.tap = {[weak self] number in
                self?.didPop(number: number)
            }
            view.addSubview(bubbleView)
        }

        update()

        message.isHidden = true
        view.bringSubview(toFront: player1)
        view.bringSubview(toFront: player2)
        view.bringSubview(toFront: message)
    }

    var timer: Timer!

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        staredAt = Date()
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) {[weak self] timer in
            guard let strongSelf = self else { return }

            let myTime = Date().timeIntervalSince(strongSelf.staredAt)
            strongSelf.elapsed.text = String(format: "%02.2f", myTime)
        }
    }

    func didPop(number: Int) {
        try! challenge.realm?.write {
            if let bubble = mySide.bubbles.last, bubble.number == number {
                mySide.bubbles.removeLast()
            } else {
                message.isHidden = false
                message.text = "You tapped \(number) instead of \(mySide.bubbles.last?.number ?? 0)"
                mySide.failed = true
                endGame()
            }
        }
    }

    func update() {
        player1.text = challenge.player1!.name + " : \(challenge.player1!.bubbles.count)"
        player2.text = "\(challenge.player2!.bubbles.count) : "+challenge.player2!.name

        if otherSide.failed {
            message.isHidden = false
            message.text = "You win! Congrats"
        } else if mySide.failed {
            message.isHidden = false
            message.text = "You lost!"
        }

        if mySide.bubbles.count == 0 {

            if mySide.time == 0 {
                let myTime = Date().timeIntervalSince(staredAt)
                try! mySide.realm?.write {
                    mySide.time = myTime
                }
                timer.invalidate()
                message.isHidden = false
                message.text = String(format: "Your time: %.2fs", myTime)
                elapsed.text = String(format: "%02.2f", myTime)
            }

            if otherSide.time > 0 && mySide.time > 0 {
                if otherSide.time < mySide.time {
                    mySide.failed = true
                } else {
                    otherSide.failed = true
                    try! challenge.realm?.write {
                        challenge.realm!.add(Score(name: mySide.name, time: mySide.time))
                    }
                }
            }
        }
    }

    func endGame() {
        mySideToken?.stop()
        otherSideToken?.stop()

        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            _ = self.navigationController?.popViewController(animated: true)
        }
    }
}
