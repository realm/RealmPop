//
//  ViewController.swift
//  PopBoard
//
//  Created by Marin Todorov on 2/7/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Cocoa

struct Score {
    let name: String
    let time: Double
}

class ViewController: NSViewController {
    @IBOutlet var tableView: NSTableView!
    
    var scores: [Score]?
    let file = ScoresFile("board.txt")
    
    override func viewDidAppear() {
        super.viewDidAppear()
        refresh()
    }

    func refresh() {
        scores = file.scores()
        guard scores != nil else {
            let alert = NSAlert()
            alert.informativeText = "Couldn't load \(file.file.absoluteString)"
            alert.beginSheetModal(for: view.window!, completionHandler: nil)
            return
        }
        tableView.reloadData()
        DispatchQueue.main.asyncAfter(deadline: .now()+5, execute: refresh)
    }
}

extension ViewController: NSTableViewDataSource {
    func numberOfRows(in tableView: NSTableView) -> Int {
        return scores?.count ?? 0
    }
}

extension ViewController: NSTableViewDelegate {
    func tableView(_ tableView: NSTableView, viewFor tableColumn: NSTableColumn?, row: Int) -> NSView? {
        let id = tableColumn!.identifier == "name" ? "NameCell" : "TimeCell"
        guard let cell = tableView.make(withIdentifier: id, owner: nil) as? NSTableCellView,
            let scores = scores else {
            return nil
        }

        let score = scores[row]
        cell.textField?.stringValue = tableColumn!.identifier == "name" ? score.name : String(format: "%.4f", score.time)
        return cell
    }

    func tableView(_ tableView: NSTableView, heightOfRow row: Int) -> CGFloat {
        return 40.0
    }
}
