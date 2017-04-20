/**
 * Score board class
 *
 * Saves scores to a text file
 */
var fs = require('fs');

function ScoreRecord(name, time) {
    this.time = time;
    this.name = name;
    this.maxRecords = 10;
}

var Board = {
  resultsFilePath: 'board.txt',
  templateFilePath: 'index-template.html',
  targetFilePath: null
}

Board.results = function() {
    let content = fs.readFileSync(this.resultsFilePath, 'utf-8').toString().trim();
    let lines = content.split("\n");

    var results = new Array();
    for (var i in lines) {
        if (i < 10) {
            let components = lines[i].split("\t");
            if (components[1] > 0.0 && components[0].length > 0) {
                results.push(
                    new ScoreRecord(components[0], components[1])
                );
            }
        }
    }

    return results;
}

Board.sort = function(results) {
    results.sort((a, b) => {
        if (a.time > b.time) return 1;
        if (a.time < b.time) return -1;
        return 0;
    });
    
    return results;
}

Board.addScore = function(score, success) {
  if (this.targetFilePath == null) {
    console.error('You need to set Board.targetFilePath before calling Board.addScore');
    process.exit(-1);
    return;
  }

  var sortedResults = this.sort(this.results());

  let slowestResult = sortedResults[sortedResults.length - 1];

  if (sortedResults.length < 10 || score.time <= slowestResult.time) { //TODO: remove equals
    sortedResults.push(score);
    sortedResults = this.sort(sortedResults).slice(0, this.maxRecords);

    fs.writeFile(this.resultsFilePath, sortedResults.map((result)=>{ return result.name + "\t" + result.time}).join("\n"), function (err) {
      if (err) {
        return console.error(err);
      }
      Board.updateHtmlFile();
      success();
    });
  }

}

Board.updateHtmlFile = function() {
    let results = this.sort(this.results());

    var resultsHtml = '';
    for (var i in results) {
        let nr = i*1 + 1;
        resultsHtml += '<p><span class="nr">'+nr+'.</span><span class="name">'+results[i].name+'</span><span class="time">'+results[i].time+'</span></p>';
    }

    var html = fs.readFileSync(this.templateFilePath, 'utf-8').toString().trim();
    html = html.replace(/##results##/gi, resultsHtml);

    fs.writeFile(this.targetFilePath, html, function (err) {
        if (err) {
            return console.error(err);
        }
    });
}

exports.Board = Board;
exports.ScoreRecord = ScoreRecord;