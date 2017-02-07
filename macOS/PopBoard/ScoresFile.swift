//
//  TabFile.swift
//  PopBoard
//
//  Created by Marin Todorov on 2/7/17.
//  Copyright © 2017 Realm Inc. All rights reserved.
//

import Foundation

class ScoresFile {
    let file: URL

    init(_ name: String) {
        var url = Bundle.main.bundleURL
        url.deleteLastPathComponent()

        file = url.appendingPathComponent(name)
    }

    func scores() -> [Score]? {
        guard let content = try? NSString(contentsOf: file, encoding: String.Encoding.utf8.rawValue) else {
            return nil
        }

        return content.components(separatedBy: "\n").flatMap { row in
            let values = row.components(separatedBy: "\t")
            guard values.count == 2, let time = Double(values[1]) else {
                return nil
            }
            return Score(name: values[0], time: time)
        }
        .sorted(by: { score1, score2 -> Bool in
            return score1.time < score2.time
        })
    }
}
