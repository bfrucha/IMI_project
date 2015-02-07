package upsud.students.imi_project;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.View;

import android.widget.EditText;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import android.content.Intent;

import java.util.Locale;

import android.widget.RelativeLayout;
import android.widget.Toast;

import upsud.students.imi_project.dialog.ScenariosDialog;
import upsud.students.imi_project.R;

public class MainActivity extends Activity implements OnInitListener {

    private int MY_DATA_CHECK_CODE = 0;
    private int SR_CHECK_CODE = 1;
    private TextToSpeech monTTS;
    private SpeechRecognizer speechRecognizer;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private SurfaceView drawView;
    private RectF rectangle;
    private Paint bgPaint;
    private Paint mainPaint;
    private int colorIndex = 0;
    private int[] colors = { Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.GRAY, Color.CYAN, Color.MAGENTA, Color.WHITE };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        RelativeLayout mainView = (RelativeLayout) findViewById(R.id.main_view);
        drawView = (SurfaceView) mainView.getChildAt(0);

        rectangle = new RectF(100, 100, 200, 200);
        bgPaint = new Paint(); bgPaint.setColor(Color.BLACK);
        mainPaint = new Paint(); mainPaint.setColor(colors[colorIndex]);

        gestureDetector = createGestureDetector();
        scaleGestureDetector = createScaleGestureDetector();

        mainView.setOnTouchListener(new View.OnTouchListener() {
            int tapCount = 0;
            long delay = 0, latency = 0, endTime = 0;

            Runnable speak = new Runnable() {
                public void run() {
                    if      (tapCount == 1) {speakWords("tap");}
                    else if (tapCount == 2) {speakWords("double tap");}
                    else if (tapCount == 3) {speakWords("triple tap");}
                    else                    {speakWords("multi tap");}
                    tapCount = 0;
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /*if(!monTTS.isSpeaking()) {
                    int pointersNb = event.getPointerCount();
                    if(pointersNb == 1) { speakWords("1 pointer detected"); }
                    else                { speakWords(pointersNb + " pointers detected"); }
                }

                if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    // remove all messages stack in the queue
                    v.getHandler().removeCallbacks(speak);
                }

                if(event.getActionMasked() == MotionEvent.ACTION_UP) {
                    delay = event.getEventTime() - event.getDownTime();

                    if(delay < 200) {
                        // count the number of tap the user does in a row (intervals of 200ms)
                        tapCount++;

                        v.getHandler().postAtTime(speak, event.getEventTime() + 200);
                    } else {
                        if(tapCount > 0) { speakWords("Tap and drag"); }
                    }

                    endTime = event.getEventTime();
                }*/
               /* return gestureDetector.onTouchEvent(event) || scaleGestureDetector.onTouchEvent(event);
            }
        });

        /*Button speakButton = (Button)findViewById(R.id.speak);
        speakButton.setOnClickListener(this);*/

       /*Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        /** SPEECH RECOGNITION **/

       /* speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("speech", "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("speech", "User began speaking");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                //Log.d("speech", "Sound level changed");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d("speech", "More sound received");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d("speech", "User stopped speaking");
            }

            @Override
            public void onError(int error) {
                if(error == SpeechRecognizer.ERROR_NO_MATCH) {
                    speakWords("I did not understand you.");
                } else if(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    Log.d("speech", "Recognizer service busy");
                } else {
                    Log.d("speech", "Error occured (error code : "+ error +")");
                }

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechRecognizer.startListening(intent);
            }

            @Override
            public void onResults(Bundle results) {
                for(CharSequence word : results.getCharSequenceArrayList(SpeechRecognizer.RESULTS_RECOGNITION)) {
                    String w = word.toString();
                    if(w.contains("couleur") || w.contains("color")) {
                        colorIndex = (colorIndex + 1)%colors.length;

                        draw();
                    }

                    Log.d("speech", "Results : " + w);
                }

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechRecognizer.startListening(intent);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d("speech", "Partial results : " + partialResults.getStringArray(SpeechRecognizer.RESULTS_RECOGNITION));
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d("speech", "Reserved for futur event");
            }
        });

        if(isNetworkAvailable()) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

            //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

            // startActivityForResult(intent, SR_CHECK_CODE);
            speechRecognizer.startListening(intent);
        } else {
            Toast.makeText(this, "Network connection unavailable.\nSpeech recognition can not be used.", Toast.LENGTH_LONG).show();
        } */
    }

    /** GESTURES DETECTORS **/

    public GestureDetector createGestureDetector() {
        return new GestureDetector(getBaseContext(),
            new GestureDetector.OnGestureListener() {

                // to detect onFling was launched on the rectangle
                boolean launchedAtRightPosition = false;

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if(rectangle.contains(e.getX(), e.getY())) {
                        colorIndex = (colorIndex + 1) % colors.length;
                        draw();
                    }
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    if(e1.getActionMasked() == MotionEvent.ACTION_CANCEL || rectangle.contains(e1.getX(), e1.getY())) {
                        rectangle.offset(-distanceX, -distanceY);
                        draw();
                        Log.d("gesture", "Scroll : " + distanceX + " " + distanceY);

                        e1.setAction(MotionEvent.ACTION_CANCEL);
                        return true;
                    }

                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {}

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {

                    if(e1.getActionMasked() == MotionEvent.ACTION_CANCEL || rectangle.contains(e1.getX(), e1.getY())) {
                        drawView.getHandler().post(new Runnable() {

                            @Override
                            public void run() {
                                double vecX = velocityX / 100, vecY = velocityY / 100;

                                double stepX = Math.abs(velocityX) / 20, stepY = Math.abs(velocityY) / 20;
                                double minX = 2, maxX = stepX;
                                double minY = 2, maxY = stepY;

                                while (Math.abs(minX) < Math.abs(velocityX) && Math.abs(minY) < Math.abs(velocityY)) {
                                    rectangle.offset((float) (vecX * (Math.log(maxX) - Math.log(minX))), (float) (vecY * (Math.log(maxY) - Math.log(minY))));
                                    draw();
                                    Log.d("Fling", vecX * (Math.log(maxX) - Math.log(minX)) + " " + vecY * (Math.log(maxY) - Math.log(minY)));

                                    minX += stepX; maxX += stepX;
                                    minY += stepY; maxY += stepY;
                                }
                            }
                        });
                        e1.setAction(MotionEvent.ACTION_CANCEL);
                        return true;
                    }
                    return false;
                }
        });
    }


    public ScaleGestureDetector createScaleGestureDetector() {
        return new ScaleGestureDetector(getBaseContext(), new ScaleGestureDetector.OnScaleGestureListener() {

            float width, height;
            float initSpanX = 0, initSpanY = 0;

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factorX = (Math.abs(detector.getCurrentSpanX()) - initSpanX) / initSpanX;
                float factorY = (Math.abs(detector.getCurrentSpanY()) - initSpanY) / initSpanY;

                rectangle.right = rectangle.left + width + width*factorX;
                rectangle.bottom = rectangle.top + height + height*factorY;

                draw();

                Log.d("scale", rectangle.toString());

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                width = rectangle.right - rectangle.left;
                height = rectangle.bottom - rectangle.top;

                initSpanX = Math.abs(detector.getCurrentSpanX());
                initSpanY = Math.abs(detector.getCurrentSpanY());
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {}
        });
    }


    /** DRAW ON SURFACE VIEW **/
    public void draw() {
        Canvas canvas = drawView.getHolder().lockCanvas();

        // erase all canvas
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), bgPaint);

        // draw rectangle at is actual position
        mainPaint.setColor(colors[colorIndex]);

        if(rectangle.left < 0) { rectangle.offset(-rectangle.left, 0); }
        if(rectangle.right > canvas.getWidth()) { rectangle.offset(canvas.getWidth() - rectangle.right, 0); }
        if(rectangle.top < 0) { rectangle.offset(0, -rectangle.top); }
        if(rectangle.bottom > canvas.getHeight()) { rectangle.offset(0, canvas.getHeight() - rectangle.bottom); }

        canvas.drawRect(rectangle, mainPaint);
        drawView.getHolder().unlockCanvasAndPost(canvas);
    }



    /** SPEECH & ACTIVITY METHODS **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add("Select a scenario");
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // mainView.createCategory("Category n°" + (int) (Math.random() * 100));

                ScenariosDialog dialog = new ScenariosDialog();
                dialog.show(getFragmentManager(), "");
                return false;
            }
        });

        return true;
    }

    /*public void onClick(View v) {
        //handle user clicks here
        EditText enteredText = (EditText)findViewById(R.id.enter);
        String words = enteredText.getText().toString();
        speakWords(words);
    } */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                monTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        } else if(requestCode == SR_CHECK_CODE) {
            if(resultCode == RESULT_CANCELED) {
                Log.d("speech", "Aborting speech recognition");
            } else {
                Log.d("speech", "Launch speech recognizer again (result code = " + resultCode +")");
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");

                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                startActivityForResult(intent, SR_CHECK_CODE);
            }
        }
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            monTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }

        draw();
    }

    private void speakWords(String speech) {
        //implement TTS here
        monTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}


