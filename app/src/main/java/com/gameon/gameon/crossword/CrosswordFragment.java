//package com.gameon.gameon.crossword;
//
//import android.app.Activity;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.net.Uri;
//import android.os.Bundle;
//import android.app.Fragment;
//import android.support.v4.view.GestureDetectorCompat;
//import android.support.v4.view.ViewCompat;
//import android.util.Log;
//import android.view.GestureDetector;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.gameon.gameon.R;
//import com.gameon.gameon.controller.Settings;
//
//public class CrosswordFragment extends Fragment {
//
//    private GestureDetectorCompat mDetector;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event){
//        this.mDetector.onTouchEvent(event);
//        return super.onTouchEvent(event);
//    }
//
//    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
//        private static final String DEBUG_TAG = "Gestures";
//
//        @Override
//        public boolean onDown(MotionEvent event) {
//            Log.d(DEBUG_TAG,"onDown: " + event.toString());
//            return true;
//        }
//
//        @Override
//        public boolean onFling(MotionEvent event1, MotionEvent event2,
//                               float velocityX, float velocityY) {
//            Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
//            return true;
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_crossword, container, false);
//    }
//}
