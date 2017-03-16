/*
Simple DirectMedia Layer
Java source code (C) 2009-2012 Sergii Pylypenko
  
This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:
  
1. The origin of this software must not be misrepresented; you must not
   claim that you wrote the original software. If you use this software
   in a product, an acknowledgment in the product documentation would be
   appreciated but is not required. 
2. Altered source versions must be plainly marked as such, and must not be
   misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

package it.ap.wesnoth;

import it.alessandropira.wesnoth112.R;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

abstract class DifferentTouchInput
{
	public abstract void process(final MotionEvent event);
	public abstract void processGenericEvent(final MotionEvent event);

	public static boolean ExternalMouseDetected = false;

	public static DifferentTouchInput getInstance(Context context)
	{
		return JMM.getInstance(context);
/*
		boolean multiTouchAvailable1 = false;
		boolean multiTouchAvailable2 = false;
		// Not checking for getX(int), getY(int) etc 'cause I'm lazy
		Method methods [] = MotionEvent.class.getDeclaredMethods();
		for(Method m: methods) 
		{
			if( m.getName().equals("getPointerCount") )
				multiTouchAvailable1 = true;
			if( m.getName().equals("getPointerId") )
				multiTouchAvailable2 = true;
		}
		try {
			Logger.log("Device model: " + android.os.Build.MODEL);
			if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH )
			{
				return IcsTouchInput.Holder.sInstance;
			}
			if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD )
				return GingerbreadTouchInput.Holder.sInstance;
			if (multiTouchAvailable1 && multiTouchAvailable2)
				return MultiTouchInput.Holder.sInstance;
			else
				return SingleTouchInput.Holder.sInstance;
		} catch( Exception e ) {
			try {
				if (multiTouchAvailable1 && multiTouchAvailable2)
					return MultiTouchInput.Holder.sInstance;
				else
					return SingleTouchInput.Holder.sInstance;
			} catch( Exception ee ) {
				return SingleTouchInput.Holder.sInstance;
			}
		}
*/
	}
	private static class JMM extends DifferentTouchInput
	{
		private JavaManagedMouse JMM_Impl = null;

		public static JMM getInstance(Context ctx) {
			return new JMM(ctx);
		}
		private JMM(Context ctx) {
			JMM_Impl = new JavaManagedMouse(ctx);
		}

		@Override
		public void processGenericEvent(final MotionEvent event)
		{
			process(event);
		}
		public void process(final MotionEvent event)
		{
			JMM_Impl.process(event);
		}
	}

//	private static class SingleTouchInput extends DifferentTouchInput
//	{
//		private static class Holder
//		{
//			private static final SingleTouchInput sInstance = new SingleTouchInput();
//		}
//		@Override
//		public void processGenericEvent(final MotionEvent event)
//		{
//			process(event);
//		}
//		public void process(final MotionEvent event)
//		{
//			int action = -1;
//			if( event.getAction() == MotionEvent.ACTION_DOWN )
//				action = Mouse.SDL_FINGER_DOWN;
//			if( event.getAction() == MotionEvent.ACTION_UP )
//				action = Mouse.SDL_FINGER_UP;
//			if( event.getAction() == MotionEvent.ACTION_MOVE )
//				action = Mouse.SDL_FINGER_MOVE;
//			if ( action >= 0 )
//				DemoGLSurfaceView.nativeMotionEvent( (int)event.getX(), (int)event.getY(), action, 0, 
//												(int)(event.getPressure() * 1000.0),
//												(int)(event.getSize() * 1000.0) );
//		}
//	}
//	private static class MultiTouchInput extends DifferentTouchInput
//	{
//		public static final int TOUCH_EVENTS_MAX = 16; // Max multitouch pointers
//
//		private class touchEvent
//		{
//			public boolean down = false;
//			public int x = 0;
//			public int y = 0;
//			public int pressure = 0;
//			public int size = 0;
//		}
//		
//		protected touchEvent touchEvents[];
//		
//		MultiTouchInput()
//		{
//			touchEvents = new touchEvent[TOUCH_EVENTS_MAX];
//			for( int i = 0; i < TOUCH_EVENTS_MAX; i++ )
//				touchEvents[i] = new touchEvent();
//		}
//		
//		private static class Holder
//		{
//			private static final MultiTouchInput sInstance = new MultiTouchInput();
//		}
//
//		public void processGenericEvent(final MotionEvent event)
//		{
//			process(event);
//		}
//		public void process(final MotionEvent event)
//		{
//			int action = -1;
//
//			//Logger.log("Got motion event, type " + (int)(event.getAction()) + " X " + (int)event.getX() + " Y " + (int)event.getY());
//			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP ||
//				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_CANCEL )
//			{
//				action = Mouse.SDL_FINGER_UP;
//				for( int i = 0; i < TOUCH_EVENTS_MAX; i++ )
//				{
//					if( touchEvents[i].down )
//					{
//						touchEvents[i].down = false;
//						DemoGLSurfaceView.nativeMotionEvent( touchEvents[i].x, touchEvents[i].y, action, i, touchEvents[i].pressure, touchEvents[i].size );
//					}
//				}
//			}
//			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN )
//			{
//				action = Mouse.SDL_FINGER_DOWN;
//				for( int i = 0; i < event.getPointerCount(); i++ )
//				{
//					int id = event.getPointerId(i);
//					if( id >= TOUCH_EVENTS_MAX )
//						id = TOUCH_EVENTS_MAX - 1;
//					touchEvents[id].down = true;
//					touchEvents[id].x = (int)event.getX(i);
//					touchEvents[id].y = (int)event.getY(i);
//					touchEvents[id].pressure = (int)(event.getPressure(i) * 1000.0);
//					touchEvents[id].size = (int)(event.getSize(i) * 1000.0);
//					DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
//				}
//			}
//			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE ||
//				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN ||
//				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP )
//			{
//				/*
//				String s = "MOVE: ptrs " + event.getPointerCount();
//				for( int i = 0 ; i < event.getPointerCount(); i++ )
//				{
//					s += " p" + event.getPointerId(i) + "=" + (int)event.getX(i) + ":" + (int)event.getY(i);
//				}
//				Logger.log(s);
//				*/
//				int pointerReleased = -1;
//				if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP )
//					pointerReleased = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//
//				for( int id = 0; id < TOUCH_EVENTS_MAX; id++ )
//				{
//					int ii;
//					for( ii = 0; ii < event.getPointerCount(); ii++ )
//					{
//						if( id == event.getPointerId(ii) )
//							break;
//					}
//					if( ii >= event.getPointerCount() )
//					{
//						// Up event
//						if( touchEvents[id].down )
//						{
//							action = Mouse.SDL_FINGER_UP;
//							touchEvents[id].down = false;
//							DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
//						}
//					}
//					else
//					{
//						if( pointerReleased == id && touchEvents[pointerReleased].down )
//						{
//							action = Mouse.SDL_FINGER_UP;
//							touchEvents[id].down = false;
//						}
//						else if( touchEvents[id].down )
//						{
//							action = Mouse.SDL_FINGER_MOVE;
//						}
//						else
//						{
//							action = Mouse.SDL_FINGER_DOWN;
//							touchEvents[id].down = true;
//						}
//						touchEvents[id].x = (int)event.getX(ii);
//						touchEvents[id].y = (int)event.getY(ii);
//						touchEvents[id].pressure = (int)(event.getPressure(ii) * 1000.0);
//						touchEvents[id].size = (int)(event.getSize(ii) * 1000.0);
//						DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
//					}
//				}
//			}
//			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_HOVER_MOVE ) // Support bluetooth/USB mouse - available since Android 3.1
//			{
//				// TODO: it is possible that multiple pointers return that event, but we're handling only pointer #0
//				if( touchEvents[0].down )
//					action = Mouse.SDL_FINGER_UP;
//				else
//					action = Mouse.SDL_FINGER_HOVER;
//				touchEvents[0].down = false;
//				touchEvents[0].x = (int)event.getX();
//				touchEvents[0].y = (int)event.getY();
//				touchEvents[0].pressure = 0;
//				touchEvents[0].size = 0;
//				DemoGLSurfaceView.nativeMotionEvent( touchEvents[0].x, touchEvents[0].y, action, 0, touchEvents[0].pressure, touchEvents[0].size );
//			}
//		}
//	}
//	private static class GingerbreadTouchInput extends MultiTouchInput
//	{
//		private static class Holder
//		{
//			private static final GingerbreadTouchInput sInstance = new GingerbreadTouchInput();
//		}
//
//		GingerbreadTouchInput()
//		{
//			super();
//		}
//		public void process(final MotionEvent event)
//		{
//			boolean hwMouseEvent = (	(event.getSource() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE ||
//										(event.getSource() & InputDevice.SOURCE_STYLUS) == InputDevice.SOURCE_STYLUS ||
//										(event.getMetaState() & KeyEvent.FLAG_TRACKING) != 0 ); // Hack to recognize Galaxy Note Gingerbread stylus
//			if( ExternalMouseDetected != hwMouseEvent )
//			{
//				ExternalMouseDetected = hwMouseEvent;
//				DemoGLSurfaceView.nativeHardwareMouseDetected(hwMouseEvent ? 1 : 0);
//			}
//			super.process(event);
//		}
//		public void processGenericEvent(final MotionEvent event)
//		{
//			process(event);
//		}
//	}
//	private static class IcsTouchInput extends GingerbreadTouchInput
//	{
//		private static class Holder
//		{
//			private static final IcsTouchInput sInstance = new IcsTouchInput();
//		}
//		private int buttonState = 0;
//		public void process(final MotionEvent event)
//		{
//			//Logger.log("Got motion event, type " + (int)(event.getAction()) + " X " + (int)event.getX() + " Y " + (int)event.getY() + " buttons " + buttonState + " source " + event.getSource());
//			int buttonStateNew = event.getButtonState();
//			if( buttonStateNew != buttonState )
//			{
//				for( int i = 1; i <= MotionEvent.BUTTON_FORWARD; i *= 2 )
//				{
//					if( (buttonStateNew & i) != (buttonState & i) )
//						DemoGLSurfaceView.nativeMouseButtonsPressed(i, ((buttonStateNew & i) == 0) ? 0 : 1);
//				}
//				buttonState = buttonStateNew;
//			}
//			super.process(event); // Push mouse coordinate first
//		}
//		public void processGenericEvent(final MotionEvent event)
//		{
//			// Joysticks are supported since Honeycomb, but I don't care about it, because very little devices have it
//			if( (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK )
//			{
//				// event.getAxisValue(AXIS_HAT_X) and event.getAxisValue(AXIS_HAT_Y) are joystick arrow keys, they also send keyboard events
//				DemoGLSurfaceView.nativeGamepadAnalogJoystickInput(
//					event.getAxisValue(MotionEvent.AXIS_X), event.getAxisValue(MotionEvent.AXIS_Y),
//					event.getAxisValue(MotionEvent.AXIS_Z), event.getAxisValue(MotionEvent.AXIS_RZ),
//					event.getAxisValue(MotionEvent.AXIS_RTRIGGER), event.getAxisValue(MotionEvent.AXIS_LTRIGGER) );
//				return;
//			}
//			// Process mousewheel
//			if( event.getAction() == MotionEvent.ACTION_SCROLL )
//			{
//				int scrollX = Math.round(event.getAxisValue(MotionEvent.AXIS_HSCROLL));
//				int scrollY = Math.round(event.getAxisValue(MotionEvent.AXIS_VSCROLL));
//				DemoGLSurfaceView.nativeMouseWheel(scrollX, scrollY);
//				return;
//			}
//			super.processGenericEvent(event);
//		}
//	}
}


class DemoRenderer extends GLSurfaceView_SDL.Renderer {
	public DemoRenderer(MainActivity _context)
	{
		context = _context;
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Logger.log("libSDL: DemoRenderer.onSurfaceCreated(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart );
		mGlSurfaceCreated = true;
		mGl = gl;
		if( ! mPaused && ! mFirstTimeStart )
			nativeGlContextRecreated();
		mFirstTimeStart = false;
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		Logger.log("libSDL: DemoRenderer.onSurfaceChanged(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart + " W: " + w + " H: " + h );
		mWidth = w;
		mHeight = h;
		mGl = gl;
		nativeResize(w, h, Globals.KeepAspectRatio ? 1 : 0);
	}
	
	public void onSurfaceDestroyed() {
		Logger.log("libSDL: DemoRenderer.onSurfaceDestroyed(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart );
		mGlSurfaceCreated = false;
		mGlContextLost = true;
		nativeGlContextLost();
	};

	public void onDrawFrame(GL10 gl) {

		mGl = gl;
		DrawLogo(mGl);
		SwapBuffers();

		nativeInitJavaCallbacks();
		
		// Make main thread priority lower so audio thread won't get underrun
		// Thread.currentThread().setPriority((Thread.currentThread().getPriority() + Thread.MIN_PRIORITY)/2);
		
		mGlContextLost = false;

		if(Globals.CompatibilityHacksStaticInit)
			MainActivity.LoadApplicationLibrary(context);

		Settings.Apply(context);
		accelerometer = new Accelerometer(context);
		// Tweak video thread priority, if user selected big audio buffer
		if(Globals.AudioBufferConfig >= 2)
			Thread.currentThread().setPriority( (Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2 ); // Lower than normal

		if (Globals.sendDebugInfo) {
			new Thread() { public void run() { redirectOut(); } }.start();
			new Thread() { public void run() { redirectErr(); } }.start();
		}
		Logger.log("flushing log");
		Logger.flush(false);

		 // Calls main() and never returns, hehe - we'll call eglSwapBuffers() from native code
		nativeInit( Globals.DataDir,
					Globals.getCommandLine(),
					( (Globals.SwVideoMode && Globals.MultiThreadedVideo) || Globals.CompatibilityHacksVideo ) ? 1 : 0,
					android.os.Debug.isDebuggerConnected() ? 1 : 0 );
		System.exit(0); // The main() returns here - I don't bother with deinit stuff, just terminate process
	}

	public int swapBuffers() // Called from native code
	{
		if( ! super.SwapBuffers() && Globals.NonBlockingSwapBuffers )
		{
			if(mRatelimitTouchEvents)
			{
				synchronized(this)
				{
					this.notify();
				}
			}
			return 0;
		}

		if(mGlContextLost) {
			mGlContextLost = false;
			Settings.SetupTouchscreenKeyboardGraphics(context); // Reload on-screen buttons graphics
//			DrawLogo(mGl);
			super.SwapBuffers();
		}

		// Unblock event processing thread only after we've finished rendering
		if(mRatelimitTouchEvents)
		{
			synchronized(this)
			{
				this.notify();
			}
		}
		if( context.isScreenKeyboardShown() )
		{
			try {
				Thread.sleep(50); // Give some time to the keyboard input thread
			} catch(Exception e) { };
		}
		return 1;
	}

	public void showScreenKeyboardWithoutTextInputField() // Called from native code
	{
		class Callback implements Runnable
		{
			public MainActivity parent;
			public void run()
			{
				parent.showScreenKeyboardWithoutTextInputField();
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		context.runOnUiThread(cb);
	}

	public void showCloseToast() { // Called from native code
//		Toast.makeText(context, R.string.ap_free_close, Toast.LENGTH_LONG).show();
	}

	public void showScreenKeyboard(final String oldText, int sendBackspace) // Called from native code
	{
		class Callback implements Runnable
		{
			public MainActivity parent;
			public String oldText;
			public boolean sendBackspace;
			public void run()
			{
				parent.showScreenKeyboard(oldText, sendBackspace);
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		cb.oldText = oldText;
		cb.sendBackspace = (sendBackspace != 0);
		context.runOnUiThread(cb);
	}

	public void hideScreenKeyboard() // Called from native code
	{
		class Callback implements Runnable
		{
			public MainActivity parent;
			public void run()
			{
				parent.hideScreenKeyboard();
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		context.runOnUiThread(cb);
	}

	public int isScreenKeyboardShown() // Called from native code
	{
		return context.isScreenKeyboardShown() ? 1 : 0;
	}

	public void setScreenKeyboardHintMessage(String s)
	{
		context.setScreenKeyboardHintMessage(s);
	}

	public void startAccelerometerGyroscope(int started)
	{
		accelerometer.openedBySDL = (started != 0);
		if( accelerometer.openedBySDL && !mPaused )
			accelerometer.start();
		else
			accelerometer.stop();
	}

	public void exitApp()
	{
		 nativeDone();
	}

	public void getAdvertisementParams(int params[])
	{
		context.getAdvertisementParams(params);
	}
	public void setAdvertisementVisible(int visible)
	{
		context.setAdvertisementVisible(visible);
	}
	public void setAdvertisementPosition(int left, int top)
	{
		context.setAdvertisementPosition(left, top);
	}
	public void requestNewAdvertisement()
	{
		context.requestNewAdvertisement();
	}

	private int PowerOf2(int i)
	{
		int value = 1;
		while (value < i)
			value <<= 1;
		return value;
	}
	public void DrawLogo(GL10 gl)
	{
		// TODO: this not quite works, as it seems
		BitmapDrawable bmp = null;
		try
		{
			bmp = new BitmapDrawable(context.getAssets().open("logo.png"));
		}
		catch(Exception e)
		{
			bmp = new BitmapDrawable(context.getResources().openRawResource(R.drawable.wesnoth_ap_logo));
		}
		int width = bmp.getBitmap().getWidth();
		int height = bmp.getBitmap().getHeight();
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * width * height);
		//byteBuffer.order(ByteOrder.BIG_ENDIAN);
		bmp.getBitmap().copyPixelsToBuffer(byteBuffer);
		byteBuffer.position(0);

		gl.glViewport(0, 0, mWidth, mHeight);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
		gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		int textureName = -1;
		int mTextureNameWorkspace[] = new int[1];
		int mCropWorkspace[] = new int[4];
		gl.glGenTextures(1, mTextureNameWorkspace, 0);
		textureName = mTextureNameWorkspace[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);
		gl.glActiveTexture(textureName);
		gl.glClientActiveTexture(textureName);
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA,
				PowerOf2(width), PowerOf2(height), 0,
				GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);
		gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0,
				width, height,
				GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, byteBuffer);
		mCropWorkspace[0] = 0; // u
		mCropWorkspace[1] = height; // v
		mCropWorkspace[2] = width;
		mCropWorkspace[3] = -height;
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
				GL11Ext.GL_TEXTURE_CROP_RECT_OES, mCropWorkspace, 0);
		((GL11Ext) gl).glDrawTexiOES(0, -mHeight, 0, mWidth, mHeight);
		gl.glActiveTexture(0);
		gl.glClientActiveTexture(0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		gl.glDeleteTextures(1, mTextureNameWorkspace, 0);

		gl.glFlush();
	}


	private native void redirectOut();
	private native void redirectErr();

	private native void nativeInitJavaCallbacks();
	private native void nativeInit(String CurrentPath, String CommandLine, int multiThreadedVideo, int isDebuggerConnected);
	private native void nativeResize(int w, int h, int keepAspectRatio);
	private native void nativeDone();
	private native void nativeGlContextLost();
	public native void nativeGlContextRecreated();
	public native void nativeGlContextLostAsyncEvent();
	public static native void nativeTextInput( int ascii, int unicode );
	public static native void nativeTextInputFinished();

	private MainActivity context = null;
	public Accelerometer accelerometer = null;
	
	private GL10 mGl = null;
	private EGL10 mEgl = null;
	private EGLDisplay mEglDisplay = null;
	private EGLSurface mEglSurface = null;
	private EGLContext mEglContext = null;
	private boolean mGlContextLost = false;
	public boolean mGlSurfaceCreated = false;
	public boolean mPaused = false;
	private boolean mFirstTimeStart = true;
	public int mWidth = 0;
	public int mHeight = 0;

	public static final boolean mRatelimitTouchEvents = false; //(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO);
}

public class DemoGLSurfaceView extends GLSurfaceView_SDL {
	public DemoGLSurfaceView(Context context) {
		super(context);
		mParent = (MainActivity)context;
		touchInput = DifferentTouchInput.getInstance(context);
		setEGLConfigChooser(Globals.VideoDepthBpp, Globals.NeedDepthBuffer, Globals.NeedStencilBuffer, Globals.NeedGles2);
		mRenderer = new DemoRenderer((MainActivity)context);
		setRenderer(mRenderer);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) 
	{
		touchInput.process(event);
		if( DemoRenderer.mRatelimitTouchEvents )
		{
			limitEventRate(event);
		}
		return true;
	};

	@Override
	public boolean onGenericMotionEvent (final MotionEvent event)
	{
		touchInput.processGenericEvent(event);
		if( DemoRenderer.mRatelimitTouchEvents )
		{
			limitEventRate(event);
		}
		return true;
	}
	
	public void limitEventRate(final MotionEvent event)
	{
		// Wait a bit, and try to synchronize to app framerate, or event thread will eat all CPU and we'll lose FPS
		// With Froyo the rate of touch events seems to be limited by OS, but they are arriving faster then we're redrawing anyway
		if((event.getAction() == MotionEvent.ACTION_MOVE ||
			event.getAction() == MotionEvent.ACTION_HOVER_MOVE))
		{
			synchronized(mRenderer)
			{
				try
				{
					mRenderer.wait(300L); // And sometimes the app decides not to render at all, so this timeout should not be big.
				} catch (InterruptedException e) { }
			}
		}
	}

	public void exitApp() {
		mRenderer.exitApp();
	};

	@Override
	public void onPause() {
		if(mRenderer.mPaused)
			return;
		mRenderer.mPaused = true;
		mRenderer.nativeGlContextLostAsyncEvent();
		if( mRenderer.accelerometer != null ) // For some reason it crashes here often - are we getting this event before initialization?
			mRenderer.accelerometer.stop();
		super.onPause();
	};
	
	public boolean isPaused() {
		return mRenderer.mPaused;
	}

	@Override
	public void onResume() {
		if(!mRenderer.mPaused)
			return;
		mRenderer.mPaused = false;
		super.onResume();
		Logger.log("libSDL: DemoGLSurfaceView.onResume(): mRenderer.mGlSurfaceCreated " + mRenderer.mGlSurfaceCreated + " mRenderer.mPaused " + mRenderer.mPaused);
		if( mRenderer.mGlSurfaceCreated && ! mRenderer.mPaused || Globals.NonBlockingSwapBuffers )
			mRenderer.nativeGlContextRecreated();
		if( mRenderer.accelerometer != null && mRenderer.accelerometer.openedBySDL ) // For some reason it crashes here often - are we getting this event before initialization?
			mRenderer.accelerometer.start();
	}

	private boolean isReservedCustomKey(int keyCode) {
		// cfr. javakeycodes.h
		return keyCode >= 220 && keyCode < 250;
	}

	private boolean key(int keyCode, KeyEvent event, int updown, int unicode) {
		if (isReservedCustomKey(keyCode)) {
			return true;
		}

		if (keyCode == 113 || keyCode == 114) { // ctrl-left, ctrl-right
			return true;
		}
		if (keyCode == 57 || keyCode == 58) { // alt-left, alt-right
			return true;
		}
		if (keyCode == 59 || keyCode == 60) { // shift-left, shift-right
			return true;
		}

		boolean ctrl = (event.getMetaState() & KeyEvent.META_CTRL_ON) != 0;
		boolean shift = (event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0;
		boolean alt = (event.getMetaState() & KeyEvent.META_ALT_ON) != 0;

		if (updown == 1) {
			if (ctrl) {
				nativeKey( 113, 1, 0 );
			}
			if (alt) {
				nativeKey( 57, 1, 0 );
			}
			if (shift) {
				nativeKey( 59, 1, 0 );
			}
		}

		boolean rv = ( nativeKey( keyCode, updown, unicode ) != 0 );

		if (updown == 0) {
			if (shift) {
				nativeKey( 59, 0, 0 );
			}
			if (alt) {
				nativeKey( 57, 0, 0 );
			}
			if (ctrl) {
				nativeKey( 113, 0, 0 );
			}
		}

		return rv;
	}

	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event) {
		int src = event.getSource();
		InputDevice dev = event.getDevice();
		KeyCharacterMap kcm = (dev == null) ? null : dev.getKeyCharacterMap();
		int meta = event.getMetaState();
		int unicode = (kcm == null) ? -1 : kcm.get(keyCode, meta);
		if (unicode == 0) {
			unicode = kcm.get(keyCode, meta & ~(KeyEvent.META_CTRL_MASK|KeyEvent.META_ALT_MASK));
		}

		Logger.log("Got key down event, id " + keyCode + " meta " + meta + " source " + src + " device[" +
		           ((dev == null) ? "null" : (dev.getName() + " kbdtype " + dev.getKeyboardType())) +
		           " unicode " + unicode + "] event " + event.toString());

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (src == InputDevice.SOURCE_MOUSE || src == InputDevice.SOURCE_STYLUS) {
				JavaManagedMouse.nativeClick(-1, -1, 2, 0);
				return true;
			}
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return false;
		}

		return key( keyCode, event, 1, unicode );
	}

	@Override
	public boolean onKeyUp(int keyCode, final KeyEvent event) {
		int src = event.getSource();
		InputDevice dev = event.getDevice();
		KeyCharacterMap kcm = (dev == null) ? null : dev.getKeyCharacterMap();
		int meta = event.getMetaState();
		int unicode = (kcm == null) ? -1 : kcm.get(keyCode, meta);
		if (unicode == 0) {
			unicode = kcm.get(keyCode, meta & ~(KeyEvent.META_CTRL_MASK|KeyEvent.META_ALT_MASK));
		}

		Logger.log("Got key up event, id " + keyCode + " meta " + meta + " source " + src + " device[" +
		           ((dev == null) ? "null" : (dev.getName() + " kbdtype " + dev.getKeyboardType())) +
		           " unicode " + unicode + "] event " + event.toString());

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (src == InputDevice.SOURCE_MOUSE || src == InputDevice.SOURCE_STYLUS) {
				return true;
			}
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return false;
		}

		return key( keyCode, event, 0, unicode );
	}

	DemoRenderer mRenderer;
	MainActivity mParent;
	DifferentTouchInput touchInput = null;

	public static native void nativeMotionEvent( int x, int y, int action, int pointerId, int pressure, int radius );
	public static native int nativeKey( int keyCode, int down, int unicode );
	public static native void nativeTouchpad( int x, int y, int down, int multitouch );
	public static native void initJavaCallbacks();
	public static native void nativeHardwareMouseDetected( int detected );
	public static native void nativeMouseButtonsPressed( int buttonId, int pressedState );
	public static native void nativeMouseWheel( int scrollX, int scrollY );
	public static native void nativeGamepadAnalogJoystickInput( float stick1x,  float stick1y, float stick2x, float stick2y, float rtrigger, float ltrigger );
}


