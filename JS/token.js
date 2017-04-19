//
// Class to get the access token
//

var fs = require('fs');

class Token {
  constructor() {
    this.current = { };
  }

  load(name, filePath) {
    this.current[name] = fs.readFileSync(filePath, 'utf-8').toString();
  }

  get(name) {
    return this.current[name];
  }
}

exports.Token = Token;