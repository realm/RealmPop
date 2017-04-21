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
            return alert(message: "Could not initialize the game. Try turning it off an on again", completion: { fatalError() })
        }
        
        me = game.currentPlayer()
        playerName.text = me.name
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        me.resetState()
    }

    override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
        switch identifier {
        case "ShowGameRoom":
            if playerName.text?.isEmpty == true {
                alert(message: "You need to pick a game handle first")
                return false
            }
            return true
        default:
            return true
        }
    }

}

extension PreGameRoomViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        if string == "\n" {
            performSegue(withIdentifier: "ShowGameRoom", sender: nil)
            return false
        }

        let text = (textField.text ?? "") as NSString
        let name = String(text.replacingCharacters(in: range, with: string))!

        try! me.realm?.write {
            me.name = name
        }
        
        return true
    }
}
