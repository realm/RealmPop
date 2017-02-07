//
//  ViewController.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/25/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit
import RealmSwift

class GameRoomViewController: UIViewController {

    @IBOutlet var tableView: UITableView!

    fileprivate var game: GameModel!

    fileprivate var me: Player!
    fileprivate var meToken: NotificationToken?

    fileprivate var players: Results<Player>!
    fileprivate var playersToken: NotificationToken?

    override func viewDidLoad() {
        super.viewDidLoad()

        guard let game = GameModel() else {
            fatalError("Couldn't open realm")
        }
        self.game = game

        me = game.currentPlayer()
        players = game.otherPlayers(than: me)
    }

    var alert: UIAlertController?

    func handleInvite(from: Player) {
        alert = UIAlertController(title: "You were invited", message: "to a game by \(from.name)", preferredStyle: .alert)
        alert?.addAction(UIAlertAction(title: "Accept", style: .default, handler: { [weak self] _ in
            self?.createGame(vs: from)
        }))
        alert?.addAction(UIAlertAction(title: "No, thanks", style: .default, handler: { [weak self] _ in
            try! self?.me.realm?.write {
                self?.me.challenger = nil
            }
        }))
        present(alert!, animated: true, completion: nil)
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        meToken = me.addNotificationBlock {[weak self] change in
            switch change {
            case .change(let properties):
                if properties.first(where: { $0.name == "challenger"}) != nil,
                    let challenger = self?.me.challenger {
                    self?.handleInvite(from: challenger)
                }
                if properties.first(where: { $0.name == "currentGame"}) != nil,
                    let challenge = self?.me.currentGame {
                    self?.showGameViewController(with: challenge)
                }

            case .error(let error):
                NSLog("error: \(error)")
                _ = self?.navigationController?.popViewController(animated: true)
            case .deleted:
                _ = self?.navigationController?.popViewController(animated: true)
            }
        }

        playersToken = players.addNotificationBlock { [weak self] changes in
            guard let strongSelf = self else { return }

            switch changes {
            case .update://(_, let del, let ins, let mod):
                //strongSelf.tableView.applyChanges(deletions: del, insertions: ins, updates: mod)
                strongSelf.tableView.reloadData()
            default:
                strongSelf.tableView.reloadData()
            }
        }

        me.resetState(available: true)
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        meToken?.stop()
        playersToken?.stop()

        try! me.realm?.write {
            me.available = false
        }
    }

    deinit {
        print("deinit GameRoom")
    }

    private func createGame(vs: Player) {
        try! me.realm!.write {
            let game = Game(challenger: vs, opponent: me)
            me.currentGame = game
            vs.currentGame = game
        }
    }

    private func showGameViewController(with challenge: Game) {
        let gameVC = storyboard!.instantiateViewController(withIdentifier: "GameViewController") as! GameViewController
        gameVC.game = game
        gameVC.challenge = challenge
        gameVC.haveChallenged = (me.challenger == nil)
        navigationController!.pushViewController(gameVC, animated: true)
    }
}

extension GameRoomViewController: UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return players.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let opponent = players[indexPath.row]

        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell")!
        cell.textLabel?.text = opponent.name
        cell.textLabel?.textColor = opponent.available ? UIColor.white : UIColor.gray
        return cell
    }

    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return "Available players"
    }
}

extension GameRoomViewController: UITableViewDelegate {
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)

        let opponent = players[indexPath.row]
        guard opponent.available else { return }

        try! game.realm.write {
            for player in me.realm!.objects(Player.self).filter("challenger = %@", me) {
                player.challenger = nil
            }
            opponent.challenger = me
        }
    }
}

