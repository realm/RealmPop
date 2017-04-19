//
//  ViewController.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/25/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit
import RealmSwift
import Then

class GameRoomViewController: UIViewController {

    @IBOutlet var tableView: UITableView!

    fileprivate var game: GameModel!

    fileprivate var me: Player!
    fileprivate var meToken: NotificationToken?

    fileprivate var players: Results<Player>!
    fileprivate var playersToken: NotificationToken?

    private var timer: Timer?
    private var alert: UIAlertController?

    // MARK: - view/app cycle

    override func viewDidLoad() {
        super.viewDidLoad()

        guard let game = GameModel() else {
            return alert(message: "Could not initialize the game model. Try turning it off an on again", completion: { fatalError() })
        }

        self.game = game

        me = game.currentPlayer()
        players = game.otherPlayers(than: me)
    }


    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        let meId = me.id
        meToken = me.addNotificationBlock { [weak self] change in

            switch change {
            case .change(let properties):
                if properties.first(where: { $0.name == "challenger"}) != nil,
                    let strongSelf = self,
                    let challenger = strongSelf.me.challenger {
                        strongSelf.handleInvite(from: challenger)
                }

                if properties.first(where: { $0.name == "currentGame"}) != nil,
                    let strongSelf = self,
                    let challenge = strongSelf.me.currentGame {
                        strongSelf.showGameViewController(with: challenge)
                }

            case .error(let e):
                print("[error] observing me \(e.localizedDescription)")
                _ = self?.navigationController?.popViewController(animated: true)

            case .deleted:
                print("[error] observing me \(meId) was deleted")
                _ = self?.navigationController?.popViewController(animated: true)
            }
        }

        playersToken = players.addNotificationBlock { [weak self] changes in
            guard let strongSelf = self else { return }

            switch changes {
            case .update(_, let del, let ins, let mod):
                strongSelf.tableView.applyChanges(deletions: del, insertions: ins, updates: mod)
            case .error(let e):
                print("[error] observing players \(e.localizedDescription)")
            default:
                strongSelf.tableView.reloadData()
            }
        }

        me.resetState(available: true)

        resetTimer()

        observeNotification(notification: NSNotification.Name.UIApplicationWillEnterForeground, selector: #selector(willEnterForeground))
        observeNotification(notification: NSNotification.Name.UIApplicationDidEnterBackground, selector: #selector(didEnterBackground))
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        timer?.invalidate()

        meToken?.stop()
        playersToken?.stop()

        me.updateAvailableIfNeeded(false)

        observeNotification(notification: NSNotification.Name.UIApplicationWillEnterForeground, selector: nil)
    }

    func willEnterForeground() {
        resetTimer()
    }

    func didEnterBackground() {
        timer?.invalidate()
    }

    // MARK: - user

    func handleInvite(from: Player) {
        alert = UIAlertController(title: "You were invited", message: "to a game by \(from.name)", preferredStyle: .alert)

        if let alert = alert {
            alert.addAction(UIAlertAction(title: "Accept", style: .default, handler: { [weak self] _ in
                guard let game = self?.game, let me = self?.me else { return }
                game.createGame(me: me, vs: from)
            }))
            alert.addAction(UIAlertAction(title: "No, thanks", style: .default, handler: { [weak self] _ in
                guard let me = self?.me else { return }
                me.resetState(available: true)
            }))
            present(alert, animated: true, completion: nil)
        }
    }
    
    func resetTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 10, repeats: true, block: { [weak self] timer in
            print("timer fire")
            if let me = self?.me {
                me.updateAvailableIfNeeded(true)
            }
        })
        timer!.fire()
    }

    // MARK: - navigation

    private func showGameViewController(with challenge: Game) {
        navigationController!.pushViewController(storyboard!.instantiateViewController(ofType: GameViewController.self).then { gameVC in
            gameVC.game = game
            gameVC.challenge = challenge
        }, animated: true)
    }

    @IBAction func back(_ sender: Any) {
        navigationController!.popViewController(animated: true)
    }
}

// MARK: - table view

extension GameRoomViewController: UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return players.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let opponent = players[indexPath.row]

        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell")!
        cell.textLabel?.text = opponent.name
        cell.textLabel?.textColor = opponent.available ? UIColor.melon : UIColor.gray
        cell.accessoryType = opponent.available ? .disclosureIndicator : .none
        return cell
    }

}

extension GameRoomViewController: UITableViewDelegate {
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let opponent = players[indexPath.row]

        if let cell = tableView.cellForRow(at: indexPath) {
            cell.contentView.backgroundColor = UIColor.elephant
            UIView.animate(withDuration: 0.33, animations: {
                cell.contentView.backgroundColor = UIColor.clear
            }) {[weak self]_ in
                guard let strongSelf = self else { return }
                strongSelf.game.challenge(me: strongSelf.me, vs: opponent)
            }
        }
    }
}

