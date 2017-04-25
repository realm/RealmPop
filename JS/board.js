//
// Score board
//

var fs = require('fs');

// generic high score object
function ScoreRecord(name, time) {
    this.time = time;
    this.name = name;
}

/**
 * Score board class
 *
 * Saves scores to a text file, exports to a simple html web page
 * demonstrates usage of "legacy" tech in conjuction with Realm
 */

const Board = {
  resultsFilePath: 'board.txt',
  templateFilePath: 'index-template.html',
  logoFilePath: 'PopIcon256.png',
  targetFolderPath: null,
  maxRecords: 10,
};

// reads the high scores from a text file and returns a list of objects
Board.results = function() {
    const content = fs.readFileSync(this.resultsFilePath, 'utf-8').toString().trim();
    const lines = content.split("\n");

    const results = new Array();
    for (const i in lines) {
        if (i < 10) {
            const components = lines[i].split("\t");
            if (components[1] > 0.0 && components[0].length > 0) {
                results.push(
                    new ScoreRecord(components[0], components[1])
                );
            }
        }
    }

    return results;
}

// sorts a list of high scores
Board.sort = function(results) {
    results.sort((a, b) => {
        if (a.time > b.time) return 1;
        if (a.time < b.time) return -1;
        return 0;
    });
    
    return results;
}

// adds a new high score to the list of scores
Board.addScore = function(score, success) {

  if (this.targetFolderPath === null) {
    console.error('You need to set Board.targetFolderPath before calling Board.addScore');
    process.exit(-1);
    return;
  }

  // get the list of scores
  var sortedResults = this.sort(this.results());

  // find the lowest high score
  const slowestResult = sortedResults[sortedResults.length - 1];

  // checks if the new score deserves to get on the score board
  if (sortedResults.length < 10 || score.time < slowestResult.time) {
    sortedResults.push(score);
    sortedResults = this.sort(sortedResults).slice(0, this.maxRecords);

    // save the score list back to disk
    fs.writeFile(this.resultsFilePath, sortedResults.map((result) => { return result.name + "\t" + result.time}).join("\n"), function (err) {
      if (err) {
        return console.error(err);
      }
      Board.updateHtmlFile(sortedResults);
      success();
    });
  }
}

// generates the web page, displaying the score board
Board.updateHtmlFile = function(results) {
    if (results === null) {
        results = this.sort(this.results());
    }

    // build simple html high score table
    var resultsHtml = '';
    if (results.length == 0) {
        resultsHtml = '<p>Play some games and you`ll see the high scores show up here!</p>';
    } else {
        for (const i in results) {
            const nr = i*1 + 1;
            resultsHtml += '<p><span class="nr">'+nr+'.</span><span class="name">'+results[i].name+'</span><span class="time">'+results[i].time+'</span></p>';
        }
    }

    // replace the template placeholder with the high scores
    var html = fs.readFileSync(this.templateFilePath, 'utf-8').toString().trim();
    html = html.replace(/##results##/gi, resultsHtml);

    // save the generated html file to the target web folder
    fs.writeFile(this.targetFolderPath + '/index.html', html, function (err) {
        if (err) {
            return console.error(err);
        }
    });

    // copy over the board graphic if it doesn't exist
    if (!fs.existsSync(this.targetFolderPath + '/PopIcon256.png')) {
        fs.createReadStream('PopIcon256.png').pipe(fs.createWriteStream(this.targetFolderPath + '/PopIcon256.png'));
    }
}

// exports
exports.Board = Board;
exports.ScoreRecord = ScoreRecord;
