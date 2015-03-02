// GLOBAL VAR
var indexFinger = {
    x:0,
    y:0,
    z:0
  },
  $win = $(window),
  winHeight = $win.innerHeight(),
  winWidth = $win.innerWidth(),
  $abcdrList = $("#abcdr-ui ul"),
  abcdrOffset = {
    Top: $abcdrList.offset().top,
    Left: $abcdrList.offset().left,
    Right: $abcdrList.offset().left+parseInt($abcdrList.outerWidth()),
    Bottom: $abcdrList.offset().top+parseInt($abcdrList.outerHeight())
  },
  socket = io.connect('http://localhost');


// LEAP CONTROLLER
var controller = new Leap.Controller({enableGestures: true}),
callMuteRequestMade = false;
controller.loop(function(frame) {
  
    var gesture  = frame.gestures[0],
        type = gesture ? gesture.type : "";

      if (gesture){
        if (gesture.type == "circle" && gesture.progress){
          socket.emit('Circle', gesture);
        }
      }

      if (frame.hands.length){
        updateIndexFingerServer();
        // var indexFingerX = frame.hands[0].indexFinger.tipPosition[0];
        if (frame.hands[0].indexFinger.tipPosition[0] + 200 > 0 && frame.hands[0].indexFinger.tipPosition[0] + 200 < 400){
          indexFinger.x = frame.hands[0].indexFinger.tipPosition[0] + 200;
        }
        if (frame.hands[0].indexFinger.tipPosition[1] < 200){
          indexFinger.y = frame.hands[0].indexFinger.tipPosition[1];
        }
        indexFinger.z = frame.hands[0].indexFinger.tipPosition[2];
        
        if (pointer.x > abcdrOffset.Left && pointer.x < abcdrOffset.Right ){
          hoverTracking(pointer);
        }

      }else{
        initIndexFinger();
      }

      if (logLeap){
          console.log(gesture)
      }
});
testServer();

function updateIndexFingerServer(){
  // setInterval(function(){
    socket.emit('HandPosition', indexFinger);
  // },2000);
};

function testServer(){
  socket.on('HandPosition', function(data){
    console.log(data)
  });
  socket.on('Circle', function(data){
    console.log(data,data.type,data.progress)
  });
};

// LOG LEAP
var logLeap = false;
window.onkeyup = function(e) {
   var key = e.keyCode ? e.keyCode : e.which;
   if (key == 84) {
      if (logLeap){
        logLeap = false;
      }else{
        logLeap = true;
      }
      console.log("logLeap",logLeap)
   }
}

// INIT LEAP INDEX FINDER
function initIndexFinger(){
  indexFinger = {
    x:0,
    y:0,
    z:0
  };
};

// UPDATE POINTER POSITION
var pointer = {
  x : 0,
  y : 0,
  $obj : $("#pointer")
};
function updatePointer(){
  pointer.x = ((indexFinger.x)*winWidth)/400;
  pointer.y = ((200 - indexFinger.y)*winHeight)/200;
  TweenMax.set(pointer.$obj,{
    x:pointer.x,
    y:pointer.y
  });
};
TweenMax.ticker.addEventListener("tick",updatePointer);

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









// INIT OBJ LETTER
var letters = [];
var letterWidth = $abcdrList.find("li").outerWidth();
$abcdrList.find("li").each(function(){
  var $this = $(this);
  var letter = {
    position:$this.index()*letterWidth,
    scale:1,
    destScale:1,
    letter:$this.html(),
    $obj:$this
  }
  letters.push(letter);
});

// LETTER HOVER
var hoverObj;
var lastHoverObj = hoverObj;
var firstHover = true;
var pointerPosition = {
  x:0,
  y:0
}
$(window).mousemove(function(e){
  pointerPosition.x = e.clientX;
  pointerPosition.y = e.clientY;
  if (pointerPosition.y > abcdrOffset.Top && pointerPosition.y < abcdrOffset.Bottom && pointerPosition.x > abcdrOffset.Left && pointerPosition.x < abcdrOffset.Right ){
    hoverTracking(pointerPosition);
  }
});
$abcdrList.mouseout(function(e) {
    $.each(letters,function(){
      this.destScale = 1;
    });
});

function hoverTracking(obj){
  hoverObj = Math.round(((obj.x + 30)-abcdrOffset.Left)/60 - 1);
  if (lastHoverObj!=hoverObj){
    samePositionTimer();
  }

  if (!firstHover && lastHoverObj!=hoverObj){
    letters[lastHoverObj].destScale = 1;
    if (lastHoverObj == 0){
      letters[1].destScale = 1;
    }else if (lastHoverObj == letters.length - 1){
      letters[letters.length - 2].destScale = 1;
    }else{
      letters[lastHoverObj-1].destScale = 1;
      letters[lastHoverObj+1].destScale = 1;
    }
  }

  if (hoverObj == 0){
    letters[1].destScale = 1.2;
  }else if (hoverObj == letters.length - 1){
    letters[letters.length - 2].destScale = 1.2;
  }else{
    letters[hoverObj-1].destScale = 1.2;
    letters[hoverObj+1].destScale = 1.2;
  }

  letters[hoverObj].destScale = 2;
  firstHover = false;
  lastHoverObj = hoverObj;
}

// ANIMATE LETTER HOVER
TweenMax.ticker.addEventListener("tick",tick);
function tick(){
  if (hoverObj != 100){
    $.each(letters,function(){
        this.scale += ( this.destScale - this.scale)*0.15;
        TweenMax.set(this.$obj,{
          scale : this.scale
        });
    });
  }
};

// LISTENER SAME POSITION
var samePositionTimeout;
function samePositionTimer(){
  clearTimeout(samePositionTimeout);
  samePositionTimeout = setTimeout(function(){
    playLetter(letters[hoverObj].letter)
  },0);
};







// AUDIO
var playing = [];
function playLetter(letter){
  $.each(playing,function(){
    document.getElementById(this).pause();
  });
  playing = [];
  var id = letter+"_audio";
  playing.push(id);
  document.getElementById(id).play();
};






//  DISPLAY SCREEN
$abcdrList.addClass("display");
