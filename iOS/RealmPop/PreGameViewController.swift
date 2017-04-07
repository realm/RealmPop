//
//  PreGameViewController.swift
//  RealmPop
//
//  Created by Marin Todorov on 1/31/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import UIKit
import RealmSwift

class PreGameRoomViewController: UIViewController {

    @IBOutlet var playerName: UITextField!
    fileprivate var me: Player!
    fileprivate var game: GameModel!
    private var defaultUsername: String?

    static func create(username: String?) -> PreGameRoomViewController {
        return UIStoryboard.instantiateViewController(ofType: self).then { vc in
            vc.game = GameModel()!
            vc.me = vc.game.currentPlayer()
            vc.defaultUsername = username
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        playerName.text = me.name.isEmpty ? defaultUsername : me.name
        game.update(name: playerName.text ?? "")
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        me.resetState()
    }

    @IBAction func showGameRoom(_ sender: Any) {
        guard let text = playerName.text, text.characters.count > 1 else {
            return
        }

        navigationController?.pushViewController(
            GameRoomViewController.create(with: me, game: game), animated: true
        )
    }

    @IBAction func logOut(_ sender: Any) {
        SyncUser.current?.logOut()
        navigationController?.popToRootViewController(animated: true)
    }
}

extension PreGameRoomViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let text = (textField.text ?? "") as NSString
        let name = String(text.replacingCharacters(in: range, with: string))!
        game.update(name: name)
        return true
    }
}
