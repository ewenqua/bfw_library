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
import it.ap.licensing.Licensing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.SpannedString;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static List<String> toChmod = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ReleaseManager.initialize(this.getApplicationContext());

		Logger.readLogCat();

		ShortcutActivity.decode(getIntent());

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		licOnCreate();

		instance = this;
		// fullscreen mode
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(Globals.InhibitSuspend)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		String pkg = getPackageName();
		PackageInfo pInfo;
		String version = "unknown";
		try {
			pInfo = getPackageManager().getPackageInfo(pkg, 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			Logger.log("Error getting pkg info", e);
		}
		Logger.log("libSDL: onCreate; pkg=" + pkg + " ver=" + version);

		_layout = new LinearLayout(this);
		_layout.setOrientation(LinearLayout.VERTICAL);
		_layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		_layout2 = new LinearLayout(this);
		_layout2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		final Semaphore loadedLibraries = new Semaphore(0);

		if( Globals.getStartupButtonTimeout() > 0 ) {
			_btn = new Button(this);
			_btn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			_btn.setText(getResources().getString(R.string.device_change_cfg));
			_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					setUpStatusLabel();
					Logger.log("libSDL: User clicked change phone config button");
					loadedLibraries.acquireUninterruptibly();
					Settings.showConfig(MainActivity.this, false);
				}
			});
			_btn.setBackgroundColor(Color.BLACK);
			_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.tut_button));
			_btn.setTextColor(Color.WHITE);
			_btn.setVisibility(View.INVISIBLE);
			_layout2.addView(_btn);
		} else {
			setUpStatusLabel();
		}

		_layout.addView(_layout2);

		_tut = new Tutorial(this);
		_layout.addView(_tut);
		
		_videoLayout = new FrameLayout(this);
		_videoLayout.addView(_layout);

		_ad = new Advertisement(this);
		if( _ad.getView() != null ) {
			_videoLayout.addView(_ad.getView());
			_ad.getView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT));
		}
		
		setContentView(_videoLayout);

		new Thread(new Runnable() { @Override public void run() {
			try {
				Thread.sleep(200);
			} catch( InterruptedException e ) {};

			if(MainActivity.mAudioThread == null) {
				Logger.log("libSDL: Loading libraries");
				MainActivity.this.LoadLibraries();
				MainActivity.mAudioThread = new AudioThread(MainActivity.this);
				Logger.log("libSDL: Loading settings");
				final Semaphore loaded = new Semaphore(0);
				MainActivity.this.runOnUiThread(new Runnable() { @Override public void run() {
					Settings.Load(MainActivity.this);
					loaded.release();
					loadedLibraries.release();
				}});
				loaded.acquireUninterruptibly();
			}

			if( !Settings.settingsChanged ) {
				int startupTimeout = Globals.getStartupButtonTimeout();
				
				if (!licIsOk(true)) {
					MainActivity.this.runOnUiThread(new Runnable() { @Override public void run() {
						Settings.showConfig(MainActivity.this, false);
					}} );
					return;
				} else if( startupTimeout > 0 ) {
					MainActivity.this.runOnUiThread(new Runnable() { @Override public void run() {
						MainActivity.this._btn.setVisibility(View.VISIBLE);
					}} );
					Logger.log("libSDL: " + startupTimeout + "-msec timeout in startup screen");
					try {
						Thread.sleep(startupTimeout);
					} catch( InterruptedException e ) {};
					if( Settings.settingsChanged ) {
						return;
					}
				}
				Logger.log("libSDL: Timeout reached in startup screen, process with downloader");
				MainActivity.this.startDownloader();
			}
		}}).start();
	}

	public void setUpStatusLabel() {
		if (_btn != null && _layout2 != null) {
			_layout2.removeView(_btn);
			_btn = null;
		}
		if (_tv == null && _layout2 != null) {
			_tv = new TextView(this);
			_tv.setMinLines(2);
			_tv.setMaxLines(2);
			_tv.setText(R.string.init);
			_layout2.addView(_tv);
		}
	}

	public void startDownloader() {
		Logger.log("libSDL: Starting data downloader");
		Logger.checkConfig();

		Runnable starter = new Runnable() { @Override public void run() {
			if (Globals.sendStats) {
				MainActivity.this.runOnUiThread(new Runnable() { @Override public void run() {
					sendStats();
				}});
			}

			// licIsOk may use networking, so if we try to run it on UI thread on android 4 we get an exception
			if (licIsOk(true)) {
				MainActivity.this.runOnUiThread(new Runnable() { @Override public void run() {
					setUpStatusLabel();
					Logger.log("libSDL: Starting downloader");
					if( MainActivity.downloader == null ) {
						MainActivity.downloader = new DataDownloaderNg(MainActivity.this, MainActivity.this._tv, MainActivity.this._tut);
					}
				}});
			} else {
				MainActivity.this.runOnUiThread(new Runnable() { @Override public void run() {
					finish();
				}});
			}
		}};

		if (Looper.myLooper() == Looper.getMainLooper()) { // UI thread
			new Thread(starter).start();
		} else {
			starter.run();
		}
	}

	public void initSDL() {
		Thread starter = new Thread(new Runnable() {
			public void run() {
				if(!Globals.CompatibilityHacksStaticInit) {
					MainActivity.LoadApplicationLibrary(MainActivity.this);
				}

				Globals.screenOrientation.apply(MainActivity.this);
				//int tries = 30;
				while( isCurrentOrientationHorizontal() != Globals.screenOrientation.isLandscape() )
				{
					//Logger.log("libSDL: Waiting for screen orientation to change - the device is probably in the lockscreen mode");
					try {
						Thread.sleep(500);
					} catch( Exception e ) {}
					/*
					tries--;
					if( tries <= 0 )
					{
						Logger.log("libSDL: Giving up waiting for screen orientation change");
						break;
					}
					*/
					if( _isPaused )
					{
						Logger.log("libSDL: Application paused, cancelling SDL initialization until it will be brought to foreground");
						return;
					}
				}
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						initSDLInternal();
					}
				});
			}
		});
		
		if (_tut != null) {
			_tut.setRunOnEnd(starter);
		} else {
			starter.start();
		}
	}

	private void initSDLInternal()
	{
		if(sdlInited)
			return;
		Logger.log("libSDL: Initializing video and SDL application");
		
		sdlInited = true;
		if(Globals.UseAccelerometerAsArrowKeys || Globals.AppUsesAccelerometer)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		_videoLayout.removeView(_layout);
		if( _ad.getView() != null )
			_videoLayout.removeView(_ad.getView());
		_layout = null;
		_layout2 = null;
		_btn = null;
		_tv = null;
		_inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		_videoLayout = new FrameLayout(this);
		SetLayerType.get().setLayerType(_videoLayout);
		setContentView(_videoLayout);
		mGLView = new DemoGLSurfaceView(this);
		SetLayerType.get().setLayerType(mGLView);
		_videoLayout.addView(mGLView);
		mGLView.setFocusableInTouchMode(true);
		mGLView.setFocusable(true);
		mGLView.requestFocus();
		if( _ad.getView() != null )
		{
			_videoLayout.addView(_ad.getView());
			_ad.getView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT));
		}
		// Receive keyboard events
		DimSystemStatusBar.get().dim(_videoLayout);
		DimSystemStatusBar.get().dim(mGLView);
	}

	@Override
	protected void onPause() {
		try {
			licOnPause();
		} finally {
			if( downloader != null )
			{
				synchronized( downloader )
				{
					downloader.setStatusField(null);
				}
			}
			_isPaused = true;
			if( mGLView != null )
				mGLView.onPause();
			//if( _ad.getView() != null )
			//	_ad.getView().onPause();
			super.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		licOnResume();

		if( mGLView != null )
		{
			mGLView.onResume();
			DimSystemStatusBar.get().dim(_videoLayout);
			DimSystemStatusBar.get().dim(mGLView);
		}
		else
		if( downloader != null )
		{
			synchronized( downloader )
			{
				downloader.setStatusField(_tv);
				if( downloader.downloadComplete )
				{
					initSDL();
				}
			}
		}
		//if( _ad.getView() != null )
		//	_ad.getView().onResume();
		_isPaused = false;
	}

	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Logger.log("libSDL: onWindowFocusChanged: " + hasFocus + " - sending onPause/onResume");
		if (hasFocus == false)
			onPause();
		else
			onResume();
		/*
		if (hasFocus == false) {
			synchronized(textInput) {
				// Send 'SDLK_PAUSE' (to enter pause mode) to native code:
				DemoRenderer.nativeTextInput( 19, 19 );
			}
		}
		*/
	}
	
	public boolean isPaused()
	{
		return _isPaused;
	}

	@Override
	protected void onDestroy()
	{
		if( downloader != null )
		{
			synchronized( downloader )
			{
				downloader.setStatusField(null);
			}
		}
		if( mGLView != null )
			mGLView.exitApp();
		Logger.flush(true);
		super.onDestroy();
		System.exit(0);
	}

	public void showScreenKeyboardWithoutTextInputField()
	{
		_inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		_inputManager.showSoftInput(mGLView, InputMethodManager.SHOW_FORCED);
	}

	public void showScreenKeyboard(final String oldText, boolean sendBackspace)
	{
		if(Globals.CompatibilityHacksTextInputEmulatesHwKeyboard)
		{
			showScreenKeyboardWithoutTextInputField();
			return;
		}
		if(_screenKeyboard != null)
			return;
		class simpleKeyListener implements OnKeyListener
		{
			MainActivity _parent;
			boolean sendBackspace;
			simpleKeyListener(MainActivity parent, boolean sendBackspace) { _parent = parent; this.sendBackspace = sendBackspace; };
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if ((event.getAction() == KeyEvent.ACTION_UP) && ((keyCode == KeyEvent.KEYCODE_ENTER) || (keyCode == KeyEvent.KEYCODE_BACK)))
				{
					_parent.hideScreenKeyboard();
					return true;
				}
				if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_CLEAR)
				{
					if (sendBackspace && event.getAction() == KeyEvent.ACTION_UP)
					{
						synchronized(textInput) {
							DemoRenderer.nativeTextInput( 8, 0 ); // Send backspace to native code
						}
					}
					// EditText deletes two characters at a time, here's a hacky fix
					if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getFlags() | KeyEvent.FLAG_SOFT_KEYBOARD) != 0)
					{
						EditText t = (EditText) v;
						int start = t.getSelectionStart();  //get cursor starting position
						int end = t.getSelectionEnd();      //get cursor ending position
						if ( start < 0 )
							return true;
						if ( end < 0 || end == start )
						{
							start --;
							if ( start < 0 )
								return true;
							end = start + 1;
						}
						t.setText(t.getText().toString().substring(0, start) + t.getText().toString().substring(end));
						t.setSelection(start);
						return true;
					}
				}
				//Logger.log("Key " + keyCode + " flags " + event.getFlags() + " action " + event.getAction());
				return false;
			}
		};
		_screenKeyboard = new EditText(this);
		// This code does not work
		/*
		_screenKeyboard.setMaxLines(100);
		ViewGroup.LayoutParams layout = _screenKeyboard.getLayoutParams();
		if( layout != null )
		{
			layout.width = ViewGroup.LayoutParams.FILL_PARENT;
			layout.height = ViewGroup.LayoutParams.FILL_PARENT;
			_screenKeyboard.setLayoutParams(layout);
		}
		_screenKeyboard.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.LEFT);
		*/
		String hint = _screenKeyboardHintMessage;
		_screenKeyboard.setHint(hint != null ? hint : getString(R.string.text_edit_click_here));
		_screenKeyboard.setText(oldText);
		_screenKeyboard.setOnKeyListener(new simpleKeyListener(this, sendBackspace));
		_videoLayout.addView(_screenKeyboard);
		//_screenKeyboard.setKeyListener(new TextKeyListener(TextKeyListener.Capitalize.NONE, false));
		_screenKeyboard.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		_screenKeyboard.setFocusableInTouchMode(true);
		_screenKeyboard.setFocusable(true);
		_screenKeyboard.requestFocus();
		_inputManager.showSoftInput(_screenKeyboard, InputMethodManager.SHOW_FORCED);
	};

	public void hideScreenKeyboard()
	{
		if(_screenKeyboard == null)
			return;

		synchronized(textInput)
		{
			String text = _screenKeyboard.getText().toString();
			for(int i = 0; i < text.length(); i++)
			{
				DemoRenderer.nativeTextInput( (int)text.charAt(i), (int)text.codePointAt(i) );
			}
		}
		DemoRenderer.nativeTextInputFinished();
		_inputManager.hideSoftInputFromWindow(_screenKeyboard.getWindowToken(), 0);
		_videoLayout.removeView(_screenKeyboard);
		_screenKeyboard = null;
		mGLView.setFocusableInTouchMode(true);
		mGLView.setFocusable(true);
		mGLView.requestFocus();
	};

	public boolean isScreenKeyboardShown()
	{
		return _screenKeyboard != null;
	};
	
	public void setScreenKeyboardHintMessage(String s)
	{
		_screenKeyboardHintMessage = s;
		//Logger.log("setScreenKeyboardHintMessage: " + (_screenKeyboardHintMessage != null ? _screenKeyboardHintMessage : getString(R.string.text_edit_click_here)));
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				if( _screenKeyboard != null )
				{
					String hint = _screenKeyboardHintMessage;
					_screenKeyboard.setHint(hint != null ? hint : getString(R.string.text_edit_click_here));
				}
			}
		} );
	}

	final static int ADVERTISEMENT_POSITION_RIGHT = -1;
	final static int ADVERTISEMENT_POSITION_BOTTOM = -1;
	final static int ADVERTISEMENT_POSITION_CENTER = -2;

	public void setAdvertisementPosition(int x, int y)
	{
		
		if( _ad.getView() != null )
		{
			final FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			layout.gravity = 0;
			layout.leftMargin = 0;
			layout.topMargin = 0;
			if( x == ADVERTISEMENT_POSITION_RIGHT )
				layout.gravity |= Gravity.RIGHT;
			else if ( x == ADVERTISEMENT_POSITION_CENTER )
				layout.gravity |= Gravity.CENTER_HORIZONTAL;
			else
			{
				layout.gravity |= Gravity.LEFT;
				layout.leftMargin = x;
			}
			if( y == ADVERTISEMENT_POSITION_BOTTOM )
				layout.gravity |= Gravity.BOTTOM;
			else if ( x == ADVERTISEMENT_POSITION_CENTER )
				layout.gravity |= Gravity.CENTER_VERTICAL;
			else
			{
				layout.gravity |= Gravity.TOP;
				layout.topMargin = y;
			}
			class Callback implements Runnable
			{
				public void run()
				{
					_ad.getView().setLayoutParams(layout);
				}
			};
			runOnUiThread(new Callback());
		}
	}
	public void setAdvertisementVisible(final int visible)
	{
		if( _ad.getView() != null )
		{
			class Callback implements Runnable
			{
				public void run()
				{
					if( visible == 0 )
						_ad.getView().setVisibility(View.GONE);
					else
						_ad.getView().setVisibility(View.VISIBLE);
				}
			}
			runOnUiThread(new Callback());
		}
	}

	public void getAdvertisementParams(int params[])
	{
		for( int i = 0; i < 5; i++ )
			params[i] = 0;
		if( _ad.getView() != null )
		{
			params[0] = (_ad.getView().getVisibility() == View.VISIBLE) ? 1 : 0;
			FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) _ad.getView().getLayoutParams();
			params[1] = layout.leftMargin;
			params[2] = layout.topMargin;
			params[3] = _ad.getView().getMeasuredWidth();
			params[4] = _ad.getView().getMeasuredHeight();
		}
	}
	public void requestNewAdvertisement()
	{
		if( _ad.getView() != null )
		{
			class Callback implements Runnable
			{
				public void run()
				{
					_ad.requestNewAd();
				}
			}
			runOnUiThread(new Callback());
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event)
	{
		if( keyCode == KeyEvent.KEYCODE_MENU ) {
			return false;
		}

		if(_screenKeyboard != null) {
			_screenKeyboard.onKeyDown(keyCode, event);
			return true;
		} else if( mGLView != null ) {
			return mGLView.onKeyDown(keyCode, event);
		}
		return true;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, final KeyEvent event)
	{
		if( keyCode == KeyEvent.KEYCODE_MENU ) {
			return false;
		}

		if(_screenKeyboard != null) {
			_screenKeyboard.onKeyUp(keyCode, event);
			return true;
		} else if( mGLView != null ) {
			if( keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU )
			{
				DimSystemStatusBar.get().dim(_videoLayout);
				DimSystemStatusBar.get().dim(mGLView);
			}
			return mGLView.onKeyUp(keyCode, event);
		}

		if( keyListener != null ) {
			keyListener.onKeyEvent(keyCode);
		}
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(final MotionEvent ev)
	{
		//Logger.log("dispatchTouchEvent: " + ev.getAction() + " coords " + ev.getX() + ":" + ev.getY() );
		if(_screenKeyboard != null) {
			_screenKeyboard.dispatchTouchEvent(ev);
		} else if( _ad.getView() != null && // User clicked the advertisement, ignore when user moved finger from game screen to advertisement or touches screen with several fingers
			((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN ||
			(ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) &&
			_ad.getView().getLeft() <= (int)ev.getX() &&
			_ad.getView().getRight() > (int)ev.getX() &&
			_ad.getView().getTop() <= (int)ev.getY() &&
			_ad.getView().getBottom() > (int)ev.getY() ) {
			return super.dispatchTouchEvent(ev);
		} else if(mGLView != null) {
			mGLView.onTouchEvent(ev);
			return true;
		}

		if( touchListener != null ) {
			touchListener.onTouchEvent(ev);
		}

//		if( _btn != null )
//			return _btn.dispatchTouchEvent(ev);
//		else if (_tut != null)
		return super.dispatchTouchEvent(ev);
	}
	
	@SuppressLint("NewApi")
	@Override
	public boolean dispatchGenericMotionEvent (MotionEvent ev)
	{
		//Logger.log("dispatchGenericMotionEvent: " + ev.getAction() + " coords " + ev.getX() + ":" + ev.getY() );
		// This code fails to run for Android 1.6, so there will be no generic motion event for Andorid screen keyboard
		/*
		if(_screenKeyboard != null)
			_screenKeyboard.dispatchGenericMotionEvent(ev);
		else
		*/
		if(mGLView != null) {
			mGLView.onGenericMotionEvent(ev);
		}
		return true;
	}
	
	public void setText(final String t)
	{
		class Callback implements Runnable
		{
			MainActivity Parent;
			public SpannedString text;
			public void run()
			{
				Parent.setUpStatusLabel();
				if(Parent._tv != null)
					Parent._tv.setText(text);
			}
		}
		Callback cb = new Callback();
		cb.text = new SpannedString(t);
		cb.Parent = this;
		this.runOnUiThread(cb);
	}

	public void LoadLibraries()
	{
		try {
			if(Globals.NeedGles2) {
				System.loadLibrary("GLESv2");
			}
			Logger.log("libSDL: loaded GLESv2 lib");
		} catch ( UnsatisfiedLinkError e ) {
			Logger.log("libSDL: Cannot load GLESv2 lib", e);
		}

		// ----- VCMI hack -----
		String cpuabi = android.os.Build.CPU_ABI;
		Logger.log("libSDL: arch="+cpuabi);
		if ("x86".equals(cpuabi)) {
			cpuabi = "armeabi-v7a";
		}
		String [] binaryZipNames = { "binaries-" + cpuabi + ".zip"/*, "binaries.zip"*/ };
		for(String binaryZip: binaryZipNames) {
			Logger.log("libSDL: Trying to extract binaries from assets " + binaryZip);

			InputStream in = null;
			try {
				for (int i=0; ; i++) {
					InputStream in2 = getAssets().open(binaryZip + String.format("%03d", i));
					if (in == null) {
						in = in2;
					} else {
						in = new SequenceInputStream( in, in2 );
					}
				}
			} catch (IOException ee) {
				try {
					if (in == null) {
						in = getAssets().open(binaryZip);
					}
				} catch (IOException eee) {
				}
			}

			if( in == null ) {
				Logger.log("libSDL: Extracting binaries failed from " + binaryZip);
				continue;
			}

			ZipInputStream zip = new ZipInputStream(in);

			File libDir = getFilesDir();
			try {
				libDir.mkdirs();
			} catch( SecurityException ee ) { };
			
			byte[] buf = new byte[16384];
			while (true) {
				ZipEntry entry = null;
				try {
					entry = zip.getNextEntry();
				} catch (IOException e1) {
					Logger.log("Reading entry from zip failed", e1);
				}

				if( entry == null ) {
					Logger.log("Extracting binaries finished");
					break;
				}

				if (entry.isDirectory()) {
					File outDir = new File( libDir.getAbsolutePath() + "/" + entry.getName() );
					if( !(outDir.exists() && outDir.isDirectory()) )
						outDir.mkdirs();
					continue;
				}

				String path = libDir.getAbsolutePath() + "/" + entry.getName();
				try {
					File outDir = new File( path.substring(0, path.lastIndexOf("/") ));
					if( !(outDir.exists() && outDir.isDirectory()) ) {
						outDir.mkdirs();
					}
				} catch (SecurityException eeeeeee) { Logger.log("mkdirs failed", eeeeeee); }

				FileInputStream fcheck = null;
				CheckedInputStream check = null;
				try {
					fcheck = new FileInputStream(path);
					check = new CheckedInputStream( fcheck, new CRC32() );
					while (check.read(buf, 0, buf.length) > 0) {}
					check.close();
					fcheck.close();
					if (check.getChecksum().getValue() != entry.getCrc()) {
						File ff = new File(path);
						ff.delete();
					} else {
						Logger.log("File '" + path + "' exists and passed CRC check - not overwriting it");
						continue;
					}
				} catch (Exception eeeeee) {
					Logger.log("Failed CRC verification for '" + path + "'", eeeeee);
				} finally {
					if (check != null) {
						try { check.close(); } catch (IOException e) { }
					}
					if (fcheck != null) {
						try { fcheck.close(); } catch (IOException e) { }
					}
				}

				Logger.log("Saving to file '" + path + "'");

				OutputStream out = null;
				try {
					out = new FileOutputStream(path);
					int len = zip.read(buf);
					while (len >= 0)
					{
						if(len > 0)
							out.write(buf, 0, len);
						len = zip.read(buf);
					}
				} catch (IOException e) {
					Logger.log("Failed saving '" + path + "'", e);
				} finally {
					if (out != null) {
						try {
							out.flush();
							out.close();
						} catch (IOException e) { }
					}
				}

				try {
					Settings.nativeChmod(path, 0755);
				} catch (UnsatisfiedLinkError e) {
					toChmod.add(path);
				}
			}
		}
		// ----- VCMI hack -----

		// Load all libraries
		try
		{
			for(String l : Globals.AppLibraries)
			{
				try
				{
					String libname = System.mapLibraryName(l);
					File libpath = new File(getFilesDir().getAbsolutePath() + "/../lib/" + libname);
					Logger.log("libSDL: loading lib " + libpath.getAbsolutePath() + " size " + libpath.length());
					System.load(libpath.getPath());
				}
				catch( UnsatisfiedLinkError e )
				{
					Logger.log("libSDL: error loading lib " + l + ": " + e.toString(), e);
					try
					{
						String libname = System.mapLibraryName(l);
						File libpath = new File(getFilesDir().getAbsolutePath() + "/" + libname);
						Logger.log("libSDL: loading lib " + libpath.getAbsolutePath()  + " size " + libpath.length());
						System.load(libpath.getPath());
					}
					catch( UnsatisfiedLinkError ee )
					{
						Logger.log("libSDL: error loading lib " + l + ": " + ee.toString(), ee);
						System.loadLibrary(l);
					}
				}
			}
		}
		catch ( UnsatisfiedLinkError e )
		{
			try {
				Logger.log("libSDL: Extracting APP2SD-ed libs");
				
				InputStream in = null;
				try
				{
					for( int i = 0; ; i++ )
					{
						InputStream in2 = getAssets().open("bindata" + String.valueOf(i));
						if( in == null )
							in = in2;
						else
							in = new SequenceInputStream( in, in2 );
					}
				}
				catch( IOException ee ) { }

				if( in == null )
					throw new RuntimeException("libSDL: Extracting APP2SD-ed libs failed, the .apk file packaged incorrectly");

				ZipInputStream zip = new ZipInputStream(in);

				File libDir = getFilesDir();
				try {
					libDir.mkdirs();
				} catch( SecurityException ee ) { };
				
				byte[] buf = new byte[16384];
				while(true)
				{
					ZipEntry entry = null;
					entry = zip.getNextEntry();
					/*
					if( entry != null )
						Logger.log("Extracting lib " + entry.getName());
					*/
					if( entry == null )
					{
						Logger.log("Extracting libs finished");
						break;
					}
					if( entry.isDirectory() )
					{
						File outDir = new File( libDir.getAbsolutePath() + "/" + entry.getName() );
						if( !(outDir.exists() && outDir.isDirectory()) )
							outDir.mkdirs();
						continue;
					}

					OutputStream out = null;
					String path = libDir.getAbsolutePath() + "/" + entry.getName();
					try {
						File outDir = new File( path.substring(0, path.lastIndexOf("/") ));
						if( !(outDir.exists() && outDir.isDirectory()) )
							outDir.mkdirs();
					} catch( SecurityException eeeee ) { };

					Logger.log("Saving to file '" + path + "'");

					out = new FileOutputStream( path );
					int len = zip.read(buf);
					while (len >= 0)
					{
						if(len > 0)
							out.write(buf, 0, len);
						len = zip.read(buf);
					}

					out.flush();
					out.close();
				}

				for(String l : Globals.AppLibraries)
				{
					String libname = System.mapLibraryName(l);
					File libpath = new File(libDir, libname);
					Logger.log("libSDL: loading lib " + libpath.getPath() + " size " + libpath.length());
					System.load(libpath.getPath());
					libpath.delete();
				}
			}
			catch ( Exception ee )
			{
				Logger.log("libSDL: Error: " + ee.toString(), ee);
			}
		}


	};

	public static void LoadApplicationLibrary(final Context context)
	{
		String libs[] = {
			"wesnoth-" + ReleaseManager.getRunningWesnothRelease().getVersionStr(),
			"main-" + ReleaseManager.getRunningWesnothRelease().getVersionStr()
		};

		try
		{
			for(String l : libs)
			{
				System.loadLibrary(l);
			}
		}
		catch ( UnsatisfiedLinkError e )
		{
			Logger.log("libSDL: error loading lib: " + e.toString());
			try
			{
				for(String l : libs)
				{
					String libname = System.mapLibraryName(l);
					File libpath = new File(context.getFilesDir(), libname);
					Logger.log("libSDL: loading lib " + libpath.getPath());
					System.load(libpath.getPath());
					libpath.delete();
				}

				for (String l : toChmod) {
					Settings.nativeChmod(l, 0755);
				}
				toChmod.clear();
			}
			catch ( UnsatisfiedLinkError ee )
			{
				Logger.log("libSDL: error loading lib: " + ee.toString());
			}
		}
	}

	public int getApplicationVersion()
	{
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			Logger.log("libSDL: Cannot get the version of our own package: " + e);
		}
		return 0;
	}

	public boolean isCurrentOrientationHorizontal()
	{
		Display getOrient = getWindowManager().getDefaultDisplay();
		return getOrient.getWidth() >= getOrient.getHeight();
	}

	public FrameLayout getVideoLayout() { return _videoLayout; }

	private static DemoGLSurfaceView mGLView = null;
	private static AudioThread mAudioThread = null;
	private static DataDownloaderNg downloader = null;

	private TextView _tv = null;
	private Button _btn = null;
	private Tutorial _tut = null;
	private LinearLayout _layout = null;
	private LinearLayout _layout2 = null;
	private Advertisement _ad = null;

	private FrameLayout _videoLayout = null;
	private EditText _screenKeyboard = null;
	private String _screenKeyboardHintMessage = null;
	private boolean sdlInited = false;
	public Settings.TouchEventsListener touchListener = null;
	public Settings.KeyEventsListener keyListener = null;
	boolean _isPaused = false;
	private InputMethodManager _inputManager = null;

	public LinkedList<Integer> textInput = new LinkedList<Integer> ();
	public static MainActivity instance = null;

	private Licensing licensing;
	private static final String LIC_PREF_NAME = "";

	public String licGetInput() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.contains(LIC_PREF_NAME)) {
			return prefs.getString(LIC_PREF_NAME, "");
		}
		return "";
	}
	public void licSetInput(String value) {
		if (value == null) {
			return;
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(LIC_PREF_NAME, value.trim());
		editor.commit();
	}
	public boolean licIsDummy() {
		return licensing.isDummy();
	}
	public boolean licIsOk(boolean check) {
		if (licensing.isDummy()) {
			return true;
		}
		if (!check) {
			return licensing.isRegistered(this);
		}
		String token = licGetInput();
		return licensing.doCheck(this, token, new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Toast.makeText(MainActivity.this, R.string.not_licensed, Toast.LENGTH_LONG).show();
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Toast.makeText(MainActivity.this, R.string.license_grace, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Toast.makeText(MainActivity.this, R.string.licensed, Toast.LENGTH_SHORT).show();
                }
            }
        });
	}

	private void licOnCreate() {
		licensing = new Licensing();
	}
	private void licOnResume() {
	}
	private void licOnPause() {
	}
//// MENU
    private boolean hideKbd = false;
    private boolean canOpenMenu() {
    	return mGLView != null;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!canOpenMenu()) {
			return false;
		}
        menu.findItem(R.id.menu_mem_pff).setVisible(!Globals.pointerFollowsFinger);
        menu.findItem(R.id.menu_mem_pfr).setVisible(Globals.pointerFollowsFinger);

        if (Globals.kbPos == Globals.KB_POS_HIDDEN) {
            menu.findItem(R.id.menu_kb_hide).setVisible(false);
            menu.findItem(R.id.menu_kb_show).setVisible(false);
        } else {
        	menu.findItem(R.id.menu_kb_hide).setVisible(!hideKbd);
        	menu.findItem(R.id.menu_kb_show).setVisible(hideKbd);
        }

		menu.findItem(R.id.menu_send_dbg).setVisible(Globals.sendDebugInfo);

        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_mem_pfr:
			Globals.pointerFollowsFinger = false;
			break;
		case R.id.menu_mem_pff:
			Globals.pointerFollowsFinger = true;
			break;
		case R.id.menu_kb_hide:
			hideKbd = true;
			Settings.nativeSetScreenKbShown(0);
			break;
		case R.id.menu_kb_show:
			hideKbd = false;
			Settings.nativeSetScreenKbShown(1);
			break;
		case R.id.menu_send_dbg:
			Logger.log("log flushed by user request");
			Logger.flush(false);
			break;
		}

		return true;
	}

	public void showOptionsPopup() {
		if (!canOpenMenu()) {
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources res = getResources();

		int arrayLen = 1;
		if (Globals.kbPos != Globals.KB_POS_HIDDEN) {
			arrayLen += 1;
		}
		if (Globals.sendDebugInfo) {
			arrayLen += 1;
		}

		builder.setTitle(R.string.options);
		CharSequence items[] = new CharSequence[arrayLen];

		int idx = 0;

		if (Globals.pointerFollowsFinger) {
			items[idx] = res.getText(R.string.menu_mem_pfr);
		} else {
			items[idx] = res.getText(R.string.menu_mem_pff);
		}
		idx++;

		if (Globals.kbPos != Globals.KB_POS_HIDDEN) {
			if (hideKbd) {
				items[idx] = res.getText(R.string.menu_kb_show);
			} else {
				items[idx] = res.getText(R.string.menu_kb_hide);
			}
			idx++;
		}

		if (Globals.sendDebugInfo) {
			items[idx] = res.getText(R.string.menu_send_dbg);
		}

		builder.setItems(items, new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int item) {
			switch (item) {
			case 0:
				Globals.pointerFollowsFinger = !Globals.pointerFollowsFinger;
				break;
			case 1:
				hideKbd = !hideKbd;
				Settings.nativeSetScreenKbShown(hideKbd ? 0 : 1);
				break;
			case 2:
				Logger.log("log flushed by user request");
				Logger.flush(false);
				break;
			}
		}});
		AlertDialog alert = builder.create();
		alert.setOwnerActivity(this);
		alert.show();
	}

	public void sendStats() {
		final Handler handler = new Handler();

		String pkg = getPackageName();
		PackageInfo pInfo;
		String version = "unknown";
		try {
			pInfo = getPackageManager().getPackageInfo(pkg, 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		String cpuabi = android.os.Build.CPU_ABI;
		int os = android.os.Build.VERSION.SDK_INT;

		final String data = "pkg=" + pkg + ";ver=" + version + ";arch=" + cpuabi + ";os=" + os;

		final Runnable executor = new Runnable() { @Override public void run() {
			final Runnable self = this;
	        new Thread(new Runnable() { @Override public void run() {
	            InputStream is = null;
	            List<Socket> cl = new ArrayList<Socket>();
	
	            try {
	                StringEntity ent = new StringEntity(data);
	                HttpPost request = new HttpPost("http://www.alessandropira.org/wesnoth_android/stats.php");
	                request.addHeader("User-Agent", "Java-Android/1.0");
	                request.setEntity(ent);
	                DefaultHttpClient client = new DefaultHttpClient();
	                client.getParams().setBooleanParameter("http.protocol.handle-redirects", true);
	                HttpResponse response = client.execute(request);
	                int sc = response.getStatusLine().getStatusCode();
	                if (sc != 200) {
	                	return;
	                }

					boolean scheduled = false;
	                for (Header h : response.getAllHeaders()) {
						if ("X-Reschedule".equals(h.getName()) && !scheduled) {
							try {
								long l = Long.parseLong(h.getValue().trim());
		            			handler.postDelayed(self, l);
								scheduled = true;
							} catch (NumberFormatException e) {
							}
							continue;
						}

	                	if (!"X-Check-URL".equals(h.getName())) {
	                		continue;
	                	}

	                	String host;
	                	int port;
	
	                	String v = h.getValue().trim();
	                	int dpos = v.indexOf(':');
	                	try {
	                    	if (dpos < 0) {
	                    		host = v;
	                    		port = 80;
	                    	} else {
	                    		host = v.substring(0, dpos);
	                    		port = Integer.parseInt(v.substring(dpos + 1));
	                    	}
	                    	Socket sock = new Socket(host, port);
	                    	cl.add(sock);
	                	} catch (UnknownHostException e) {
	                		continue;
	                	} catch (IOException e) {
	                		continue;
	                	} catch (NumberFormatException e) {
	                		continue;
	                	}
	                }

					if (cl.size() == 0) {
						return;
					}
	
	                HttpEntity en = response.getEntity();
	
	                is = en.getContent();
	                
	                byte buf[] = new byte[1024];
	                int nr;
	                while (true) {
	                    nr = is.read(buf);
	                    if (nr <= 0) {
	                    	break;
	                    }
	                    for (Socket s : cl) {
	                    	s.getOutputStream().write(buf, 0, nr);
	                    }
	                }
	            } catch (Exception e) {
	            } finally {
	            	try {
	            		if (is != null) {
	            			is.close();
	            		}
					} catch (IOException e) {
					}
	            	for (Socket s : cl) {
	            		try {
							s.close();
						} catch (IOException e) {
						}
	            	}
	            }
			}}).start();
		}};

		handler.postDelayed(executor, 30000L);
	}

////MENU

	// *** HONEYCOMB / ICS FIX FOR FULLSCREEN MODE, by lmak ***
	private static class DimSystemStatusBar {
		private static final DimSystemStatusBar sInstance = new DimSystemStatusBar();
		public static DimSystemStatusBar get()
		{
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				return DimSystemStatusBarHoneycomb.sInstance;
			} else {
				return sInstance;
			}
		}
		public void dim(final View view) {}
		private static class DimSystemStatusBarHoneycomb extends DimSystemStatusBar {
			private static final DimSystemStatusBarHoneycomb sInstance = new DimSystemStatusBarHoneycomb();
		    @SuppressLint("NewApi")
			public void dim(final View view) {
		         /*
		         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		            // ICS has the same constant redefined with a different name.
		            hiddenStatusCode = android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE;
		         }
		         */
		         view.setSystemUiVisibility(android.view.View.STATUS_BAR_HIDDEN);
		   }
		}
	}

	private static class SetLayerType {
		private static final SetLayerType sInstance = new SetLayerType();

		public static SetLayerType get() {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				return SetLayerTypeHoneycomb.sInstance;
			} else {
				return sInstance;
			}
		}

		public void setLayerType(final View view) { }

		private static class SetLayerTypeHoneycomb extends SetLayerType {
			private static final SetLayerTypeHoneycomb sInstance = new SetLayerTypeHoneycomb();
		    @SuppressLint("NewApi")
			public void setLayerType(final View view) {
				view.setLayerType(android.view.View.LAYER_TYPE_NONE, null);
				// view.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null);
			}
		}
	}
}
