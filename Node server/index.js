var express = require('express');
var http = require('http');
var app = express();
var server = require('http').createServer(app);
var port = process.env.PORT || 5000;
var io = require('socket.io').listen(server);

app.use(express.bodyParser());

app.get('/', function(req, res){
  res.sendfile('public_circle/index.html');
});

app.get(/^(.+)$/, function(req, res) {
  res.sendfile('public_circle/' + req.params[0]);
});

var user = {
  liveConnected : 0
};

io.sockets.on('connection', function(socket){
    user.liveConnected++;
    console.log("userConnected",user.liveConnected)
    io.sockets.emit('UserConnection',JSON.stringify(user));
    socket.on('disconnect', function(){
      user.liveConnected--;
      console.log("userDisconnected",user.liveConnected)
      io.sockets.emit('UserDisonnection',JSON.stringify(user));
    });
    socket.on('HandPosition', function(data){
      console.log('HandPosition: ' + data);
      io.sockets.emit('HandPosition', JSON.stringify(data));
    });
    socket.on('Circle', function(data){
      console.log('Circle: ' + data);
      io.sockets.emit('Circle', JSON.stringify(data));
    });
    socket.on('Swipe', function(data){
      console.log('Swipe: ' + data);
      io.sockets.emit('Swipe', JSON.stringify(data));
    });
});

server.listen(port, function(){
  console.log('listening on *:5000');
});
