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
    var numbers: [Int]!

    var challenge: Game! {
        didSet {
            let haveChallenged = challenge.player1!.playerId == game.currentPlayer().id
            mySide =  haveChallenged ? challenge.player1 : challenge.player2
            otherSide = haveChallenged ? challenge.player2 : challenge.player1
            numbers = challenge.numbers.components(separatedBy: ",").map { return Int($0)! }
        }
    }

    private var mySide: Side!
    private var mySideToken: NotificationToken?

    private var otherSide: Side!
    private var otherSideToken: NotificationToken?

    private var staredAt: Date!
    private var timer: Timer!

    override func viewDidLoad() {
        super.viewDidLoad()

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
                strongSelf.endGame("You're out of time")
            }
        }

        mySideToken = mySide.addNotificationBlock { [weak self] _ in
            self?.update()
        }
        otherSideToken = otherSide.addNotificationBlock { [weak self] _ in
            self?.update()
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        mySideToken?.stop()
        otherSideToken?.stop()
    }

    func didPop(number: Int) {
        if let last = numbers.last, last == number {
            numbers.removeLast()
            game.updateBubbles(side: mySide, count: numbers.count)
        } else {
            stopGame("Lost. You tapped \(number) instead of \(numbers.last ?? 0)")
            game.fail(side: mySide)
        }
    }

    func update() {
        player1.text = challenge.player1!.name + " : \(challenge.player1!.left)"
        player2.text = "\(challenge.player2!.left) : "+challenge.player2!.name

        if otherSide.failed {
            endGame("You win! Sweet")
            return
        } else if mySide.failed {
            endGame(message.isHidden ? "You lost" : message.text ?? "You lost")
            return
        }

        if mySide.left == 0 {

            if mySide.time == 0 {
                //store the time for current player
                let myTime = Date().timeIntervalSince(staredAt)
                elapsed.text = String(format: "%02.2f", myTime)

                game.updateTime(mySide: mySide, myTime: myTime, otherSide: otherSide)
                stopGame(String(format: "Your time: %.2fs", myTime))

                //timeout if other player doesn't finish
                DispatchQueue.main.asyncAfter(deadline: .now() + 20) { [weak self] in
                    self?.endGameTimeout()
                }
            }

            if challenge.isActive() {
                game.determineOutcome(mine: mySide, theirs: otherSide)
            }
        }
    }

    func stopGame(_ text: String) {
        timer.invalidate()
        view.isUserInteractionEnabled = false
        message.isHidden = false
        message.text = text
    }

    func endGame(_ text: String) {
        stopGame(text)
        DispatchQueue.main.asyncAfter(deadline: .now() + 5) { [weak self] in
            if self?.navigationController != nil {
                _ = self?.navigationController?.popViewController(animated: true)
            }
        }
    }

    func endGameTimeout() {
        guard navigationController != nil else {
            return
        }
        if challenge.isActive() {
            game.fail(side: otherSide)
        }
    }
}
