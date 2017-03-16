
package it.ap.wesnoth;

import it.ap.wesnoth.GestureDetector.OnGestureListener;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.MotionEvent;

public class JavaManagedMouse implements OnGestureListener {
	// per quanti millisecondi considerare la distanza percorsa nel computo dell'accelerazione
	protected static final long ACCEL_TIME = 500;

	// soglia di distanza minima oltre cui scatta l'accelerazione
	protected static final float ACCEL_THRESHOLD = 20.0f;
	
	// oltre 100 pixel l'accelerazione resta costante:  ___/^^^
	protected static final float ACCEL_MAX = 125.0f;

	protected static final int FLING_THRESHOLD = 40;
	protected static final int ZOOM_THRESHOLD = 120;

	private GestureDetector gestureScanner;
	private Context context;
	private float xfact = 1.0f;
	private float yfact = 1.0f;
	private float avgfact = 1.0f;

	private float accelThreshold, accelMax, accelFactor;
	private int lastAccel;

	private static final int KEYCODE_DPAD_UP         = 19;
	private static final int KEYCODE_DPAD_DOWN       = 20;
	private static final int KEYCODE_DPAD_LEFT       = 21;
	private static final int KEYCODE_DPAD_RIGHT      = 22;

	private static final int KEYCODE_ZOOM_IN         = 241;
	private static final int KEYCODE_ZOOM_OUT        = 242;

	private float flingX, flingY, zoom;

	private static class Delta {
		private float dx;
		private float dy;
		private float sqrsumsqrt;
		private long ts;
		public Delta(long ts, float dx, float dy) {
			this.dx = dx;
			this.dy = dy;
			this.ts = ts;
			this.sqrsumsqrt = -1.0f;
		}
		public float getAccel() {
			if (sqrsumsqrt < 0) {
				sqrsumsqrt= (float)Math.sqrt(dx*dx + dy*dy);
			}
			return sqrsumsqrt;
		}
	}
	private List<Delta> deltas;

	public JavaManagedMouse(Context context) {
		if (context instanceof Activity) {
			DisplayMetrics metrics = new DisplayMetrics();
			((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
			xfact = 256.0f / metrics.xdpi;
			yfact = 256.0f / metrics.ydpi;
			avgfact = (xfact+yfact) / 2.0f;
		} else {
			avgfact = xfact = yfact = 1.0f;
		}
		accelThreshold = ACCEL_THRESHOLD/avgfact;
		accelMax = ACCEL_MAX/avgfact;
		lastAccel = -1;
		accelFactor = 0.0f;

		this.context = context;
		deltas = new ArrayList<Delta>();
		onMultiEnd();
        gestureScanner = new GestureDetector(context, this);
	}

	private float getAccelFactor() {
		// massimo fattore di accelerazione
		//  ** 1.0 significa che a getAccelFactor di spostamento la velocita' raddoppia
		//         variando quel valore si modifica la velocita' massima

		if (Globals.accel != lastAccel) {
			lastAccel = Globals.accel;
			accelFactor = Globals.getAccelFloat() / (accelMax - accelThreshold);
		}
		return accelFactor;
	}

	private JoystickCallback joyCb = new JoystickCallback() {
		@Override public void process(float x, float y) {
			nativeDrag(0, 0, 0, 0, -x, -y, 0);
		}
	};

	public void process(final MotionEvent event) {
		int source = event.getSource();
		if ((source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && event.getAction() == MotionEvent.ACTION_MOVE) {
			Logger.log("JMM: joystick movement");
			JoystickProcessor.get().processJoystick(event, joyCb);
		} else if ((source & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
			Logger.log("JMM: gamepad movement");
			JoystickProcessor.get().processJoystick(event, joyCb);
		} else {
			gestureScanner.onTouchEvent(event);

			if (event.getPointerCount() == 1) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					nativeEndDrag();
				}
			}
		}
	}

	public static native void nativeClick(int x, int y, int button, int pff);
	private static native void nativeDrag(int x, int y, int origx, int origy, float dx, float dy, int pff);
	private static native void nativeEndDrag();

	@Override
	public void onSingleTapUp(float x, float y) {
		nativeClick((int)x, (int)y, 1, Globals.pointerFollowsFinger ? 1 : 0);
	}

	@Override
	public void onLongPress(MotionEvent e) {
		nativeClick((int)e.getX(), (int)e.getY(), 2, Globals.pointerFollowsFinger ? 1 : 0);
	}

	@Override
	public void on2ndFingerTap(float x, float y) {
		if (Globals.tap2ndFingerButton > 0) {
			nativeClick((int)x, (int)y, Globals.tap2ndFingerButton, Globals.pointerFollowsFinger ? 1 : 0);
		}
	}

	@Override
	public void onMultiScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		flingX += distanceX;
		flingY += distanceY;

		if (flingX < -FLING_THRESHOLD) {
			DemoGLSurfaceView.nativeKey(KEYCODE_DPAD_RIGHT, 1, 0);
			DemoGLSurfaceView.nativeKey(KEYCODE_DPAD_RIGHT, 0, 0);
			flingX += FLING_THRESHOLD;
		} else if (flingX > FLING_THRESHOLD) {
			DemoGLSurfaceView.nativeKey(KEYCODE_DPAD_LEFT, 1, 0);
			DemoGLSurfaceView.nativeKey(KEYCODE_DPAD_LEFT, 0, 0);
			flingX -= FLING_THRESHOLD;
		}
		if (flingY < -FLING_THRESHOLD) {
			DemoGLSurfaceView.nativeKey(KEYCODE_DPAD_DOWN, 1, 0);
			DemoGLSurfaceView.nativeKey(KEYCODE_DPAD_DOWN, 0, 0);
			flingY += FLING_THRESHOLD;
		} else if (flingY > FLING_THRESHOLD) {
			DemoGLSurfaceView.nativeKey(KEYCODE_DPAD_UP, 1, 0);
			DemoGLSurfaceView.nativeKey(KEYCODE_DPAD_UP, 0, 0);
			flingY -= FLING_THRESHOLD;
		}
	}
	@Override
	public void onMultiEnd() {
		zoom = flingX = flingY = 0;
	}

	@Override
	public void onMultiZoom(double zoom) {
		this.zoom += zoom;

		if (this.zoom < -ZOOM_THRESHOLD) {
			DemoGLSurfaceView.nativeKey(KEYCODE_ZOOM_OUT, 1, 0);
			DemoGLSurfaceView.nativeKey(KEYCODE_ZOOM_OUT, 0, 0);
			this.zoom += ZOOM_THRESHOLD;
		} else if (this.zoom > ZOOM_THRESHOLD) {
			DemoGLSurfaceView.nativeKey(KEYCODE_ZOOM_IN, 1, 0);
			DemoGLSurfaceView.nativeKey(KEYCODE_ZOOM_IN, 0, 0);
			this.zoom -= ZOOM_THRESHOLD;
		}
	}

	@Override
	public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		float accel = 0;
		float factor = getAccelFactor();

		if (factor > 0) {
			synchronized (deltas) {
				long now = System.currentTimeMillis();
				deltas.add(new Delta(now, distanceX, distanceY));
				while (deltas.size() > 0 && now - deltas.get(0).ts > ACCEL_TIME) {
					deltas.remove(0);
				}
				for (Delta d : deltas) {
					accel += d.getAccel();
				}
			}

			if (accel > accelThreshold) {
				if (accel > accelMax) {
					accel = accelMax;
				}
				accel -= accelThreshold;
				distanceX *= 1.0f + accel * factor;
				distanceY *= 1.0f + accel * factor;
			}
		}

		distanceX *= xfact;
		distanceY *= yfact;
		nativeDrag((int)e2.getX(), (int)e2.getY(), 0, 0, distanceX, distanceY, Globals.pointerFollowsFinger ? 1 : 0);
	}

	@Override
	public void onMultiFingerLong(int count) {
		if (count != 3) {
			return;
		}
		if (!(context instanceof MainActivity)) {
			return;
		}
		((MainActivity)context).showOptionsPopup();
	}

	@Override
	public void onMultiFingerTap(int count) {
		if (count != 3) {
			return;
		}
		((MainActivity)context).showScreenKeyboard("", false);
	}

	private interface JoystickCallback {
		void process(float x, float y);
	}

	private static class JoystickProcessor {
		private static final JoystickProcessor sInstance = new JoystickProcessor();
		public static JoystickProcessor get() {
			// WIP
//			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
//				return JoystickProcessorHoneycomb.sInstance;
//			} else {
				return sInstance;
//			}
		}
		public void processJoystick(MotionEvent event, JoystickCallback cb) {
//			Logger.log("JMM: joystick support unavailable on API level " + android.os.Build.VERSION.SDK_INT);
		}

	    @SuppressLint("NewApi")
		private static class JoystickProcessorHoneycomb extends JoystickProcessor {
			private static final JoystickProcessorHoneycomb sInstance = new JoystickProcessorHoneycomb();

			private float processFlat(MotionEvent event, InputDevice device, int axis, int historyPos) {
	    	    InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
	    	    if (range == null) {
	    	    	Logger.log("JMM: range null");
	    	    } else {
	    	        float flat = range.getFlat();
	    	        float value = (historyPos < 0) ? event.getAxisValue(axis): event.getHistoricalAxisValue(axis, historyPos);

	    	        if (Math.abs(value) > flat) {
	    	            return value;
	    	        }

	    	        Logger.log("JMM: value " + value + " inside flat range " + flat);
	    	    }
	    	    return 0;
	    	}

			private void processJoystick(MotionEvent event, JoystickCallback cb, int historyPos) {
	    	    InputDevice mInputDevice = event.getDevice();

	    	    float x = processFlat(event, mInputDevice, MotionEvent.AXIS_X, historyPos);
	    	    if (x == 0) {
	    	        x = processFlat(event, mInputDevice, MotionEvent.AXIS_HAT_X, historyPos);
	    	        if (x != 0) {
	        	        Logger.log("JMM: using AXIS_HAT_X for x");
	    	        }
	    	    }
	    	    if (x == 0) {
	    	        x = processFlat(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
	    	        if (x != 0) {
	        	        Logger.log("JMM: using AXIS_Z for x");
	    	        }
	    	    }

	    	    float y = processFlat(event, mInputDevice, MotionEvent.AXIS_Y, historyPos);
	    	    if (y == 0) {
	    	        y = processFlat(event, mInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);
	    	        if (y != 0) {
	        	        Logger.log("JMM: using AXIS_HAT_Y for y");
	    	        }
	    	    }
	    	    if (y == 0) {
	    	        y = processFlat(event, mInputDevice, MotionEvent.AXIS_RZ, historyPos);
	    	        if (y != 0) {
	        	        Logger.log("JMM: using AXIS_RZ for y");
	    	        }
	    	    }

    	        Logger.log("JMM: processing x,y = " + x + "," + y);

    	        cb.process(x, y);
	    	}

	    	public void processJoystick(MotionEvent event, JoystickCallback cb) {
	    		if (cb == null) {
	    			return;
	    		}
				int historySize = event.getHistorySize();
				for (int i=0; i<historySize; i++) {
	                processJoystick(event, cb, i);
	            }
				processJoystick(event, cb, -1);
			}
	    }
	}
}

