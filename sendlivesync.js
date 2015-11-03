var net = require('net');

var client = new net.Socket();
client.connect(18181, '127.0.0.1', function() {
	console.log('Connected');
	client.write(new Buffer([0, 0, 0, 1, 1]));
	client.destroy();
});