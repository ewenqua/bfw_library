/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.ap.wesnoth;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class GestureDetector {
    public interface OnGestureListener {
    	void onSingleTapUp(float x, float y);
        void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
        void onMultiFingerTap(int count);
        void onMultiFingerLong(int count);
        void on2ndFingerTap(float x, float y);
        void onLongPress(MotionEvent e);
        void onMultiScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
        void onMultiZoom(double zoom);
        void onMultiEnd();
    }

    private int mTouchSlopSquare;
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();

    // constants for Message.what used by GestureHandler below
    private static final int LONG_PRESS = 1;
    private static final int LONG_PRESS_3F = 2;

    private final Handler mHandler;
    private final OnGestureListener mListener;

    private boolean mAlwaysInTapRegion;
    private boolean multiFinger;
    private boolean longPressing;

    private MotionEvent mCurrentDownEvent;

    private float mLastMotionY = 0;
    private float mLastMotionX = 0;

    private float mLastUpY;
    private float mLastUpX;
    private long mLastUpTime;

    private float mCumulScrollX;
    private float mCumulScrollY;

    private int firstMove;
	private int mNearTapThresoldSquare = 0;
	private static final int NEAR_TAP_MIN_THRESHOLD_PX_SQ = 60 * 60;
	private static final int NEAR_TAP_TIMEOUT = 600;

    private static class GestureHandler extends Handler {
    	private GestureDetector det;
        private GestureHandler(GestureDetector det) {
            super();
            this.det = det;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LONG_PRESS:
            case LONG_PRESS_3F:
                det.dispatchLongPress(msg.what);
                break;

            default:
                throw new RuntimeException("Unknown message " + msg); //never
            }
        }
    }

    public GestureDetector(Context context, OnGestureListener listener) {
        this(context, listener, null);
    }

    public GestureDetector(Context context, OnGestureListener listener, Handler handler) {
    	mHandler = new GestureHandler(this);
    	mListener = listener;
    	init(context);
    }

    private void init(Context context) {
        if (mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }

        // Fallback to support pre-donuts releases
        int touchSlop;
        if (context == null) {
            //noinspection deprecation
            touchSlop = ViewConfiguration.getTouchSlop();
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            touchSlop = configuration.getScaledTouchSlop();
        }
        mTouchSlopSquare = touchSlop * touchSlop;
        mNearTapThresoldSquare = mTouchSlopSquare * 16;
        if (mNearTapThresoldSquare < NEAR_TAP_MIN_THRESHOLD_PX_SQ) {
        	mNearTapThresoldSquare = NEAR_TAP_MIN_THRESHOLD_PX_SQ;
        }

        Logger.log("GestureDetector: touchSlopSquare=" + mTouchSlopSquare + " nearTapThresoldSquare=" + mNearTapThresoldSquare + " (context " + (context == null ? "" : "not ") + "null)");

		mCumulScrollX = mCumulScrollY = 0;
        mLastUpTime = 0;
		firstMove = 0;
		multiFinger = false;
    }

    private boolean isInTapRegion(float evX, float evY, float origX, float origY, int thresholdSquare) {
        int deltaX = (int)(evX - origX);
        int deltaY = (int)(evY - origY);
        int distance = (deltaX * deltaX) + (deltaY * deltaY);
		//Logger.log("GestureDetector: isInTapRegion dist=" + distance + " tslq=" + mTouchSlopSquare);
        return distance <= thresholdSquare;
    }

	private boolean isInTapRegion(MotionEvent down, MotionEvent cur, int thresholdSquare) {
		int pcount = down.getPointerCount();
		if (cur.getPointerCount() != pcount) {
			return false;
		}

		for (int p=0; p<pcount; p++) {
			if (!isInTapRegion(cur.getX(p), cur.getY(p), down.getX(p), down.getY(p), thresholdSquare)) {
				return false;
			}
		}
		return true;
	}

    private float tap2x = 0;
    private float tap2y = 0;
    private long tap2start = -1L;
    private void start2ndTap(MotionEvent ev) {
    	tap2x = ev.getX(1);
    	tap2y = ev.getY(1);
    	tap2start = System.currentTimeMillis();
    }
    private void cancel2ndTap() {
    	tap2start = -1;
    }
    private void check2ndTap(MotionEvent ev) {
    	if (tap2start < 0) {
    		return;
    	}
		long now = System.currentTimeMillis();
    	if ((now - tap2start <= TAP_TIMEOUT) && (isInTapRegion(ev.getX(1), ev.getY(1), tap2x, tap2y, mTouchSlopSquare))) {
            if ((now - mLastUpTime <= NEAR_TAP_TIMEOUT) && (isInTapRegion(mLastMotionX, mLastMotionY, mLastUpX, mLastUpY, mNearTapThresoldSquare))) {
	    		mListener.on2ndFingerTap(mLastUpX, mLastUpY);
			} else {
	    		mListener.on2ndFingerTap(mLastMotionX, mLastMotionY);
            	mLastUpX = mLastMotionX;
				mLastUpY = mLastMotionY;
            	mLastUpTime = now;
			}
    	}
		cancel2ndTap();
    }

    private long tap3start = -1L;
	private MotionEvent tap3StartEvent = null;

    private void start3rdTap(MotionEvent ev) {
    	tap3start = System.currentTimeMillis();
		if (tap3StartEvent != null) {
			tap3StartEvent.recycle();
		}
		tap3StartEvent = MotionEvent.obtain(ev);
    }
    private void cancel3rdTap() {
    	tap3start = -1;
    	cancelLongPress3f();
    }
    private void check3rdTap() {
    	if (tap3start < 0) {
    		return;
    	}
		long now = System.currentTimeMillis();
    	if (now - tap3start <= TAP_TIMEOUT) {
    		mListener.onMultiFingerTap(3);
    	}
		cancel3rdTap();
    }

    private float _2FingersX;
    private float _2FingersY;
    private double _2FingersDistance;

    private double fingerDistance(MotionEvent ev) {
    	float deltaX = (ev.getX(0) - ev.getX(1));
    	float deltaY = (ev.getY(0) - ev.getY(1));
    	return Math.sqrt( (deltaX * deltaX) + (deltaY * deltaY) );
	}

    private void update2FingersXY(MotionEvent ev) {
    	if (ev.getPointerCount() != 2) {
    		return;
    	}
    	_2FingersX = (ev.getX(0) + ev.getX(1)) / 2;
    	_2FingersY = (ev.getY(0) + ev.getY(1)) / 2;
    	_2FingersDistance = fingerDistance(ev);

    }
    private float get2FingersDeltaX(MotionEvent ev) {
    	if (ev.getPointerCount() != 2) {
    		return 0;
    	}
    	float x = (ev.getX(0) + ev.getX(1)) / 2;
    	return x - _2FingersX;
    }
    private float get2FingersDeltaY(MotionEvent ev) {
    	if (ev.getPointerCount() != 2) {
    		return 0;
    	}
    	float y = (ev.getY(0) + ev.getY(1)) / 2;
    	return y - _2FingersY;
    }
    private double get2FingersDeltaZoom(MotionEvent ev) {
    	if (ev.getPointerCount() != 2) {
    		return 0;
    	}
    	return fingerDistance(ev) - _2FingersDistance;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        float y = ev.getY();
        float x = ev.getX();
		int pcount = ev.getPointerCount();

        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_POINTER_DOWN:
        	// Multitouch event
            multiFinger = true;
        	if (pcount == 2) {
        		start2ndTap(ev);
        		update2FingersXY(ev);
        	} else if (pcount == 3) {
        		cancel2ndTap();
				start3rdTap(ev);
	            mHandler.removeMessages(LONG_PRESS_3F);
				long start = mCurrentDownEvent == null ? System.currentTimeMillis() : mCurrentDownEvent.getDownTime();
	            mHandler.sendEmptyMessageAtTime(LONG_PRESS_3F, start + TAP_TIMEOUT + LONGPRESS_TIMEOUT);
        	} else if (pcount > 3) {
        		cancel2ndTap();
        		cancel3rdTap();
				cancelLongPress3f();
        	}

        	cancelLongPress();
            break;

        case MotionEvent.ACTION_POINTER_UP:
            if (pcount == 2) {
                // Ending a multitouch gesture and going back to 1 finger
                check2ndTap(ev);
                int index = (((action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT) == 0) ? 1 : 0;
                mLastMotionX = ev.getX(index);
                mLastMotionY = ev.getY(index);
                mListener.onMultiEnd();
            } else if (pcount == 3) {
				check3rdTap();
        		update2FingersXY(ev);
			}
            break;

        case MotionEvent.ACTION_DOWN:
        	mCumulScrollX = mCumulScrollY = 0;
            mLastMotionX = x;
            mLastMotionY = y;
			firstMove = 0;
			longPressing = false;
            if (mCurrentDownEvent != null) {
                mCurrentDownEvent.recycle();
            }
            mCurrentDownEvent = MotionEvent.obtain(ev);
            mAlwaysInTapRegion = true;
            multiFinger = false;

            mHandler.removeMessages(LONG_PRESS);
            mHandler.sendEmptyMessageAtTime(LONG_PRESS, mCurrentDownEvent.getDownTime() + TAP_TIMEOUT + LONGPRESS_TIMEOUT);

            break;

        case MotionEvent.ACTION_MOVE:
            if (pcount > 1) {
            	multiFinger = true;
            }

            if (pcount == 2) {
                float scrollX = get2FingersDeltaX(ev);
                float scrollY = get2FingersDeltaY(ev);
                double zoom = get2FingersDeltaZoom(ev);

                update2FingersXY(ev);

                mListener.onMultiScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                mListener.onMultiZoom(zoom);

                break;
            } else if (pcount == 3) {
				if (tap3StartEvent != null) {
					if (!isInTapRegion(tap3StartEvent, ev, mTouchSlopSquare)) {
						cancelLongPress3f();
					}
				}
			} else if (pcount == 1) {
				float dx = mLastMotionX - x;
				float dy = mLastMotionY - y;
				if (firstMove < 5) {
					// Su alcuni device (i.e. samsung galaxy tab) il primo movimento arriva quando il dispositivo
					// e' "sicuro" che non stai facendo un longpress ed è comprensivo di tutto lo spostamento fatto
					// sinora. L'effetto e' che il mouse "scatta" e non si riescono a fare movimenti brevi
					dx /= 10;
					dy /= 10;
					firstMove++;
				}

	            mCumulScrollX += dx;
	            mCumulScrollY += dy;
	            mLastMotionX = x;
	            mLastMotionY = y;


	            if (mAlwaysInTapRegion) {
	            	if (!isInTapRegion(x, y, mCurrentDownEvent.getX(), mCurrentDownEvent.getY(), mTouchSlopSquare)) {
	                    mAlwaysInTapRegion = false;
	                    mHandler.removeMessages(LONG_PRESS);
	                }
	            }

	            if ((Math.abs(mCumulScrollX) >= 1) || (Math.abs(mCumulScrollY) >= 1)) {
	                mListener.onScroll(mCurrentDownEvent, ev, mCumulScrollX, mCumulScrollY);
	                mCumulScrollX = mCumulScrollY = 0;
	            }
			}

            break;

        case MotionEvent.ACTION_UP:
			long now = System.currentTimeMillis();
			Logger.log("GestureDetector: up xy: " + x + "," + y);
            if (mAlwaysInTapRegion && !multiFinger && !longPressing) {
				Logger.log("GestureDetector: now=" + now + " mLastUpTime=" + mLastUpTime);
            	if ((now - mLastUpTime <= NEAR_TAP_TIMEOUT) && (isInTapRegion(x, y, mLastUpX, mLastUpY, mNearTapThresoldSquare))) {
					x = mLastUpX;
					y = mLastUpY;
					Logger.log("GestureDetector: using last xy: " + x + "," + y);
            	}
            	mListener.onSingleTapUp(x, y);
            }
			cancelLongPress();
			cancelLongPress3f();
            mCumulScrollX = mCumulScrollY = 0;
            mLastUpX = x;
			mLastUpY = y;
            mLastUpTime = now;
			Logger.log("GestureDetector: setting last xy=" + x + "," + y + " mLastUpTime=" + mLastUpTime);
            break;
        case MotionEvent.ACTION_CANCEL:
            cancelLongPress();
            cancelLongPress3f();
            break;
        }
        return true;
    }

	private void cancelLongPress3f() {
        mHandler.removeMessages(LONG_PRESS_3F);
		if (tap3StartEvent != null) {
			tap3StartEvent.recycle();
			tap3StartEvent = null;
		}
	}

    private void cancelLongPress() {
        mHandler.removeMessages(LONG_PRESS);
    }

    private void dispatchLongPress(int kind) {
		longPressing = true;
		if (kind == LONG_PRESS) {
	        mListener.onLongPress(mCurrentDownEvent);
		} else if (kind == LONG_PRESS_3F) {
	        mListener.onMultiFingerLong(3);
		}
    }
}
