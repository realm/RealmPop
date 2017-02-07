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

    var game: GameModel!
    var challenge: Game!
    var numbers: [Int]!

    var haveChallenged = false

    private var mySide: Side!
    private var mySideToken: NotificationToken?

    private var otherSide: Side!
    private var otherSideToken: NotificationToken?

    private var staredAt: Date!
    private var timer: Timer!

    override func viewDidLoad() {
        super.viewDidLoad()

        numbers = challenge.numbers.components(separatedBy: ",")
            .map {
                return Int($0)!
            }

        mySide = haveChallenged ? challenge.player1 : challenge.player2
        otherSide = haveChallenged ? challenge.player2 : challenge.player1

        //build UI
        let playRect = view.bounds.insetBy(dx: 40, dy: 70).offsetBy(dx: 0, dy: 30)

        for number in numbers {
            let bubbleView = BubbleView.bubble(number: number, inRect: playRect)
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

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        staredAt = Date()
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] timer in
            guard let strongSelf = self else { return }

            let myTime = Date().timeIntervalSince(strongSelf.staredAt)
            strongSelf.elapsed.text = String(format: "%02.2f", myTime)

            if myTime > 60.0 {
                strongSelf.message.text = "You're out of time"
                strongSelf.endGame()
            }
        }

        mySideToken = mySide.addNotificationBlock { [weak self] _ in
            self?.update()
        }
        otherSideToken = otherSide.addNotificationBlock { [weak self] _ in
            self?.update()
        }
    }

    func didPop(number: Int) {
        try! challenge.realm?.write {
            if let last = numbers.last, last == number {
                numbers.removeLast()
                mySide.left = numbers.count
            } else {
                message.isHidden = false
                message.text = "You tapped \(number) instead of \(numbers.last ?? 0)"
                mySide.failed = true
                stopGame()
            }
        }
    }

    func update() {
        player1.text = challenge.player1!.name + " : \(challenge.player1!.left)"
        player2.text = "\(challenge.player2!.left) : "+challenge.player2!.name

        if otherSide.failed {
            message.text = "You win! Congrats"
            endGame()
            return
        } else if mySide.failed {
            if message.isHidden {
                message.text = "You lost!"
            }
            endGame()
            return
        }

        if mySide.left == 0 {

            if mySide.time == 0 {
                //store the time for current player
                let myTime = Date().timeIntervalSince(staredAt)
                try! mySide.realm?.write {
                    mySide.time = myTime
                    elapsed.text = String(format: "%02.2f", myTime)
                    
                    if otherSide.time < myTime {
                        game.logTime(for: mySide)
                    }
                }

                message.text = String(format: "Your time: %.2fs", myTime)
                stopGame()

                //timeout if other player doesn't finish
                DispatchQueue.main.asyncAfter(deadline: .now() + 20) { [weak self] in
                    guard let strongSelf = self, strongSelf.navigationController != nil else {
                        return
                    }
                    if strongSelf.challenge.isActive() {
                        try! strongSelf.otherSide.realm?.write {
                            strongSelf.otherSide.failed = true
                        }
                    }
                }
            }

            if challenge.isActive() {
                game.determineOutcome(mine: mySide, theirs: otherSide)
            }
        }
    }

    func stopGame() {
        message.isHidden = false
        view.isUserInteractionEnabled = false
        timer.invalidate()
    }

    func endGame() {
        stopGame()
        DispatchQueue.main.asyncAfter(deadline: .now() + 5) { [weak self] in
            if self?.navigationController != nil {
                _ = self?.navigationController?.popViewController(animated: true)
            }
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        mySideToken?.stop()
        otherSideToken?.stop()
    }
}
