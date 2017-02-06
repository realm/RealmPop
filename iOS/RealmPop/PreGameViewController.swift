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

    override func viewDidLoad() {
        super.viewDidLoad()

        guard let game = GameModel() else {
            //total disaster
            return alert(message: "Could not initialize the game. Try turning it off an on again")
        }
        
        me = game.currentPlayer()
        playerName.text = me.name
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        me.resetState()
    }
}

extension PreGameRoomViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let text = (textField.text ?? "") as NSString
        let name = String(text.replacingCharacters(in: range, with: string))!

        try! me.realm?.write {
            me.name = name
        }
        
        return true
    }
}
