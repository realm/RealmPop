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

    static func create(with me: Player, game: GameModel) -> GameRoomViewController {
        return UIStoryboard.instantiateViewController(ofType: self).then { vc in
            vc.me = me
            vc.game = game
            vc.players = vc.game.otherPlayers(than: me)
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
    }

    var alert: UIAlertController?

    func handleInvite(from: Player) {
        alert = UIAlertController(title: "You were invited", message: "to a game by \(from.name)", preferredStyle: .alert)
        alert?.addAction(UIAlertAction(title: "Accept", style: .default, handler: { [weak self] _ in
            guard let game = self?.game, let me = self?.me else { return }
            game.createGame(me: me, vs: from)
        }))
        alert?.addAction(UIAlertAction(title: "No, thanks", style: .default, handler: { [weak self] _ in
            self?.me.resetState(available: true)
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
                    //self?.handleInvite(from: challenger)
                }
                if properties.first(where: { $0.name == "currentGame"}) != nil,
                    let challenge = self?.me.currentGame {
                    //self?.showGameViewController(with: challenge)
                }

            case .error, .deleted:
                _ = self?.navigationController?.popViewController(animated: true)
            }
        }

        playersToken = players.addNotificationBlock { [weak self] changes in
            guard let strongSelf = self else { return }

            switch changes {
            case .update(_, let del, let ins, let mod):
                strongSelf.tableView.applyChanges(deletions: del, insertions: ins, updates: mod)
                //strongSelf.tableView.reloadData()
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

    private func showGameViewController(with challenge: Game) {
        navigationController!.pushViewController(
            GameViewController.create(with: game, challenge: challenge), animated: true)
    }

    @IBAction func back(_ sender: Any) {
        navigationController!.popViewController(animated: true)
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

