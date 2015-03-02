// GLOBAL VAR
var indexFinger = {
    x:0,
    y:0,
    z:0
  },
  $win = $(window),
  winHeight = $win.innerHeight(),
  winWidth = $win.innerWidth(),
  socket = io.connect('http://localhost');

var numGesture = 0;

var rotation = {
  deg:0
};
var futurRotation = {
  deg:0
};

var $deg247,
    $deg225,
    $deg202,
    $deg180,
    $deg158,
    $deg135,
    $deg113;

var $mainCircle = $("#main-circle");

defineTargets();
update();

function defineTargets(){
    $deg247 = $(".current_circle").find(".deg247");
    $deg225 = $(".current_circle").find(".deg225");
    $deg202 = $(".current_circle").find(".deg202");
    $deg180 = $(".current_circle").find(".deg180");
    $deg158 = $(".current_circle").find(".deg158");
    $deg135 = $(".current_circle").find(".deg135");
    $deg113 = $(".current_circle").find(".deg113");
    rotation = {
      deg:0
    };
    futurRotation = {
      deg:0
    };
    numGesture = 0;
}

var firstSwipeUpdate = true;
var firstCircleUpdate = true;

// var

// LEAP CONTROLLER
var controller = new Leap.Controller({enableGestures: true}),
callMuteRequestMade = false;
controller.loop(function(frame) {
  
  var gestures = frame.gestures,
    circle,
    swipe,
    pointable,
    direction,
    normal;
    // Check if is there any gesture going on
    if(gestures.length > 0) {
        console.log(gestures,gestures[0].type)
        // In this example we will focus only on the first gesture, for the sake of simplicity
        if(gestures[0].type == 'circle') {
            socket.emit('Circle', gestures);
            circle = gestures[0];
            console.log(circle)
            // Get Pointable object
            circle.pointable = frame.pointable(circle.pointableIds[0]);
            // Reset circle gesture variables as nedded, not really necessary in this case
            if(circle.state == 'start') {
                clockwise = true;
            } else if (circle.state == 'stop' || circle.state == 'update') {
                if (firstCircleUpdate){
                  firstCircleUpdate = false;
                  setTimeout(function(){
                    firstCircleUpdate = true;
                  },200);
                  direction = circle.pointable.direction;
                  // Check if pointable exists
                  if(direction) {
                      normal = circle.normal;
                      // Check if product of vectors is going forwards or backwards
                      // Since Leap uses a right hand rule system
                      // forward is into the screen, while backwards is out of it
                      clockwise = Leap.vec3.dot(direction, normal) > 0;
                      if(clockwise) {
                          //Do clockwose stuff
                          numGesture++;
                          futurRotation.deg = 20*numGesture;
                      } else {
                          //Do counterclockwise stuff
                          numGesture--;
                          futurRotation.deg = 20*numGesture;
                      }
                  }
                }
            }
        }else if(gestures[0].type == 'swipe') {
          socket.emit('Swipe', gestures);
          swipe = gestures[0];
          // if (swipe.state == 'stop' || swipe.state == 'update'){
            if (firstSwipeUpdate){
              firstSwipeUpdate = false;
              if (swipe.direction[0]<0){
                enterLayout();
              }else{
                leaveLayout();
              }
            // }
          }
        }
    }

});

function enterLayout(){
  if ($mainCircle.is(".artists")){
        TweenMax.to($("#artists"),0.3,{
          scale:2.5,
          ease:Back.EaseIn,
          onStart:function(){
            $mainCircle.removeClass('artists').addClass('albums');
            $("#artists").removeClass('current_circle');
            $("#albums").addClass('current_circle');
            defineTargets();
            minRotate = 0;
            maxRotate = 20;
            minNumGesture = 0;
            maxNumGesture = 1;
          },
          onComplete:function(){
            setTimeout(function(){
              firstSwipeUpdate = true;
            },500);
          }
        });
        TweenMax.fromTo($("#albums"),0.3,{
          scale:0
        },{
          scale:1,
          ease:Back.EaseIn
        });
      }else if ($mainCircle.is(".albums")){
        TweenMax.to($("#artists"),0.3,{
          scale:5,
          ease:Back.EaseIn,
          onStart:function(){
            $mainCircle.removeClass('albums').addClass('titles');
            $("#albums").removeClass('current_circle');
            $("#titles").addClass('current_circle');
            defineTargets();
            minRotate = 0;
            maxRotate = 60;
            minNumGesture = 0;
            maxNumGesture = 3;
          },
          onComplete:function(){
            setTimeout(function(){
              firstSwipeUpdate = true;
            },500);
          }
        });
        TweenMax.to($("#albums"),0.3,{
          scale:2.5,
          ease:Back.EaseIn
        });
        TweenMax.fromTo($("#titles"),0.3,{
          scale:0
        },{
          scale:1,
          ease:Back.EaseIn
        });
      }else if ($mainCircle.is(".titles")){
        $("#player").removeClass("player1").addClass("player2")
      }
}
function leaveLayout(){
  if ($mainCircle.is(".albums")){
        TweenMax.to($("#artists"),0.3,{
          scale:1,
          ease:Back.EaseIn,
          onStart:function(){
            $mainCircle.removeClass('albums').addClass('artists');
            $("#albums").removeClass('current_circle');
            $("#artists").addClass('current_circle');
            defineTargets();
            minRotate = -60;
            maxRotate = 60;
            minNumGesture = -3;
            maxNumGesture = 3;
          },
          onComplete:function(){
            setTimeout(function(){
              firstSwipeUpdate = true;
            },500);
          }
        });
        TweenMax.fromTo($("#albums"),0.3,{
          scale:1
        },{
          scale:0,
          ease:Back.EaseIn
        });
      }else if ($mainCircle.is(".titles")){
        TweenMax.to($("#artists"),0.3,{
          scale:2.5,
          ease:Back.EaseIn,
          onStart:function(){
            $mainCircle.removeClass('titles current_circle').addClass('albums');
            $("#titles").removeClass('current_circle');
            $("#albums").addClass('current_circle');
            defineTargets();
            minRotate = 0;
            maxRotate = 20;
            minNumGesture = 0;
            maxNumGesture = 1;
          },
          onComplete:function(){
            setTimeout(function(){
              firstSwipeUpdate = true;
            },500);
          }
        });
        TweenMax.to($("#albums"),0.3,{
          scale:1,
          ease:Back.EaseIn
        });
        TweenMax.fromTo($("#titles"),0.3,{
          scale:1
        },{
          scale:0,
          ease:Back.EaseIn
        });
      }
}
// testServer();
keyboardShortcut();
function keyboardShortcut(){
  window.addEventListener("keyup",function(e){
    console.log(e.keyCode)
    if (e.keyCode == "40"){
      numGesture++;
      futurRotation.deg = 20*numGesture;
      console.log(futurRotation)
    }else if(e.keyCode == "38"){
      numGesture--;
      futurRotation.deg = 20*numGesture;
      console.log(futurRotation)
    }else if(e.keyCode == "37"){
      enterLayout();
    }else if(e.keyCode == "39"){
      leaveLayout();
    }
  })
};

var minRotate = -60;
var maxRotate = 60;
var minNumGesture = -3;
var maxNumGesture = 3;

function update(){
  if (futurRotation.deg < minRotate){
    futurRotation.deg = minRotate;
    numGesture = minNumGesture;
  }else if (futurRotation.deg > maxRotate){
    futurRotation.deg = maxRotate;
    numGesture = maxNumGesture;
  }
  rotation.deg += ( futurRotation.deg - rotation.deg )*0.1;
  TweenMax.set($(".current_circle"),{
    rotation:rotation.deg
  });
  TweenMax.set($(".current_circle").find(".deg_container"),{
    rotation:-rotation.deg
  });
  switch(futurRotation.deg){
    case -60:
      $deg247.removeClass('current4 current3 current2 current1').addClass('current');
      $deg225.removeClass('current4 current3 current2 current').addClass('current1');
      $deg202.removeClass('current4 current3 current1 current').addClass('current2');
      $deg180.removeClass('current4 current2 current1 current').addClass('current3');
      $deg158.removeClass('current3 current2 current1 current').addClass('current4');
      $deg135.removeClass('current3 current2 current1 current').addClass('current4');
      $deg113.removeClass('current3 current2 current1 current').addClass('current4');
    break; 
    case -40:
      $deg247.removeClass('current4 current3 current2 current').addClass('current1');
      $deg225.removeClass('current4 current3 current2 current1').addClass('current');
      $deg202.removeClass('current4 current3 current2 current').addClass('current1');
      $deg180.removeClass('current4 current3 current1 current').addClass('current2');
      $deg158.removeClass('current4 current2 current1 current').addClass('current3');
      $deg135.removeClass('current3 current2 current1 current').addClass('current4');
      $deg113.removeClass('current3 current2 current1 current').addClass('current4');
    break;
    case -20:
      $deg247.removeClass('current4 current3 current1 current').addClass('current2');
      $deg225.removeClass('current4 current3 current2 current').addClass('current1');
      $deg202.removeClass('current4 current3 current2 current1').addClass('current');
      $deg180.removeClass('current4 current3 current2 current').addClass('current1');
      $deg158.removeClass('current4 current3 current1 current').addClass('current2');
      $deg135.removeClass('current4 current2 current1 current').addClass('current3');
      $deg113.removeClass('current3 current2 current1 current').addClass('current4');
    break; 
    case 0:
      $deg247.removeClass('current4 current2 current1 current').addClass('current3');
      $deg225.removeClass('current4 current3 current1 current').addClass('current2');
      $deg202.removeClass('current4 current3 current2 current').addClass('current1');
      $deg180.removeClass('current4 current3 current2 current1').addClass('current');
      $deg158.removeClass('current4 current3 current2 current').addClass('current1');
      $deg135.removeClass('current4 current3 current1 current').addClass('current2');
      $deg113.removeClass('current4 current2 current1 current').addClass('current3');
    break;
    case 20:
      $deg247.removeClass('current3 current2 current1 current').addClass('current4');
      $deg225.removeClass('current4 current2 current1 current').addClass('current3');
      $deg202.removeClass('current4 current3 current1 current').addClass('current2');
      $deg180.removeClass('current4 current3 current2 current').addClass('current1');
      $deg158.removeClass('current4 current3 current2 current1').addClass('current');
      $deg135.removeClass('current4 current3 current2 current').addClass('current1');
      $deg113.removeClass('current4 current3 current1 current').addClass('current2');
    break; 
    case 40:
      $deg247.removeClass('current3 current2 current1 current').addClass('current4');
      $deg225.removeClass('current3 current2 current1 current').addClass('current4');
      $deg202.removeClass('current4 current2 current1 current').addClass('current3');
      $deg180.removeClass('current4 current3 current1 current').addClass('current2');
      $deg158.removeClass('current4 current3 current2 current').addClass('current1');
      $deg135.removeClass('current4 current3 current2 current1').addClass('current');
      $deg113.removeClass('current4 current3 current2 current').addClass('current1');
    break;
    case 60:
      $deg247.removeClass('current3 current2 current1 current').addClass('current4');
      $deg225.removeClass('current3 current2 current1 current').addClass('current4');
      $deg202.removeClass('current3 current2 current1 current').addClass('current4');
      $deg180.removeClass('current4 current2 current1 current').addClass('current3');
      $deg158.removeClass('current4 current3 current1 current').addClass('current2');
      $deg135.removeClass('current4 current3 current2 current').addClass('current1');
      $deg113.removeClass('current4 current3 current2 current1').addClass('current');
    break; 
  }
  TweenMax.ticker.addEventListener("tick",update);
}

function CircleGesture(gesture){
  
}

function updateIndexFingerServer(){
  // setInterval(function(){
    socket.emit('HandPosition', indexFinger);
  // },2000);
};

function testServer(){
  socket.on('HandPosition', function(data){
    // console.log(data)
  });
  socket.on('Circle', function(data){
    // CircleGesture(data);
    // console.log(data,data.type,data.progress)
  });
};

// LEAP EVENTS
controller.on('ready', function() {
  console.log("ready");
});
controller.on('connect', function() {
  console.log("connect");
});
controller.on('disconnect', function() {
  console.log("disconnect");
});
controller.on('focus', function() {
  console.log("focus");
});
controller.on('blur', function() {
  console.log("blur");
});
controller.on('deviceConnected', function() {
  console.log("deviceConnected");
});
controller.on('deviceDisconnected', function() {
  console.log("deviceDisconnected");
});
