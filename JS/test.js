'use strict';

var Realm = require('realm');

function ConnectedUser() {
  this.available = false
}

ConnectedUser.schema = {
  name: 'ConnectedUser',
  primaryKey: 'id',
  properties: {
    id: {type: 'string'},
    creationDate: {type: 'date'},
    username: {type: 'string'},
    lastUpdate: {type: 'date'},
    available: {type: 'bool', default: false}
  }
};


console.log('start');
var user = null;

Realm.Sync.User.login('http://localhost:9080', 'test@host', 'test', (error, u) => { 
  console.log(': login user: ' + user + ' error: ' + error);

	if (error !== undefined) {
    	console.log('errored');
    	process.exit();
	} else {
		console.log('logged in');
	    user = u;
   		listenForEvents();
		setTimeout(loggedIn, 5000);
	}
});

function loggedIn() {
	var realm = new Realm({
      sync: {
        user: user,
        url: 'realm://localhost:9080/~/test',
      },
      schema: [ConnectedUser.schema]
    });
	console.log('opened realm');
	
    realm.write(() => { 
      let u = realm.create('ConnectedUser', {
        id: Date(),
        creationDate: new Date(),
        lastUpdate: new Date(),
      	username: 'test123'
      });
    });
    console.log('added user');
    
}

function handleChange(changeEvent) {
	console.log('EVENT!');
}

function listenForEvents() {
  // events
  var SERVER_URL = 'realm://localhost:9080';
  var NOTIFIER_PATH = '/^\/([0-9a-f]+)\/test/';

  var ADMIN_TOKEN = 'ewoJImlkZW50aXR5IjogIl9fYXV0aCIsCgkiYWNjZXNzIjogWyJ1cGxvYWQiLCAiZG93bmxvYWQiLCAibWFuYWdlIl0KfQo=:REl/0p/CMHIouYfIWblUe19OnF4OXPZ7ky5X+PL7T36DeqwcXbJgtKBVjff6bzhn10XE8HPTcaRxlomxzwIlM1jl1oZlRUVqEs9HLfAWVXG7tfmqCKo7xTA+F2baQq2NfXp34L6nMZXXtcmnMV7IdpZ3jqfSetoGu4Y6Fs8hwSMQCmWze1SOvD6YbeDPaA7No4D7E2cULaQo7wwjqlqHWbyti/uVQSVE/o6Yjc+AtHBikrsAQbmwbhIS5CaOXkCLE0XmeyeFQptwb2x+XLQi/7mlZiy2Pd0eK4fwjNQ12koeN2qNvdsD3xB1gZGqUF0gkLyqj9KJSA6TwmAqr8ribQ==';

  var adminUser = Realm.Sync.User.adminUser(ADMIN_TOKEN);
  Realm.Sync.addListener(SERVER_URL, adminUser, NOTIFIER_PATH, 'change', handleChange);
  console.log('Listening for Realm changes');
}
