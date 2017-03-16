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
import it.ap.wesnoth.Globals.ScreenOrientation;
import it.ap.wesnoth.KeyCaptureDialog.KeyCaptureCallback;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;


// TODO: too much code here, split into multiple files, possibly auto-generated menus?
public class Settings {
	static String SettingsFileName = "libsdl-settings.cfg";

	static boolean settingsLoaded = false;
	static boolean settingsChanged = false;
	static final int SETTINGS_FILE_VERSION = 13;

	private static void initOptionalDataDownload() {
		int requiredSize = DataDownload.packCount();

		if( Globals.OptionalDataDownload == null || Globals.OptionalDataDownload.length != requiredSize ) {
			boolean dwl[] = new boolean[requiredSize];
			for( int i = 0; i < requiredSize; i++ ) {
				if (DataDownload.isMandatory(i)) {
					dwl[i] = true;
				} else if (Globals.OptionalDataDownload != null && i < Globals.OptionalDataDownload.length) {
					dwl[i] = Globals.OptionalDataDownload[i];
				} else {
					dwl[i] = false;
				}
			}
			Globals.OptionalDataDownload = dwl;
		}
	}

	@SuppressLint("WorldReadableFiles")
	public static void Save(final MainActivity p) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(p.openFileOutput( SettingsFileName, MainActivity.MODE_WORLD_READABLE ));
			out.writeInt(SETTINGS_FILE_VERSION);
			out.writeInt(Globals.kbPos);

			out.writeBoolean(Globals.DownloadToSdcard);
			out.writeInt(Globals.OptionalDataDownload.length);
			for(int i = 0; i < Globals.OptionalDataDownload.length; i++)
				out.writeBoolean(Globals.OptionalDataDownload[i]);
			out.writeInt(Globals.DataDir.length());
			for( int i = 0; i < Globals.DataDir.length(); i++ )
				out.writeChar(Globals.DataDir.charAt(i));
			out.writeBoolean(Globals.pointerFollowsFinger);
			out.writeInt(Globals.accel);
			out.writeInt(Globals.tap2ndFingerButton);

			out.writeInt(Globals.apOskPosition);
			out.writeInt(Globals.apOskSize);
			out.writeInt(Globals.apOskDrawSize);
			out.writeInt(Globals.apOskKeyMask);
			out.writeInt(Globals.apOskTransparency);

			out.writeInt(Globals.screenOrientation.toInt());

			out.writeInt(1); //Globals.upgrade_1_12_confirmed);

			out.writeBoolean(Globals.sendStats);

//			out.writeBoolean(Globals.PhoneHasArrowKeys);
//			out.writeBoolean(Globals.PhoneHasTrackball);
//			out.writeBoolean(Globals.UseAccelerometerAsArrowKeys);
//			out.writeBoolean(Globals.UseTouchscreenKeyboard);
//			out.writeInt(Globals.TouchscreenKeyboardSize);
//			out.writeInt(Globals.AccelerometerSensitivity);
//			out.writeInt(Globals.AccelerometerCenterPos);
//			out.writeInt(Globals.TrackballDampening);
//			out.writeInt(Globals.AudioBufferConfig);
//			out.writeInt(Globals.TouchscreenKeyboardTheme);
//			out.writeInt(Globals.RightClickMethod);
//			out.writeInt(Globals.ShowScreenUnderFinger);
//			out.writeInt(Globals.LeftClickMethod);
//			out.writeBoolean(Globals.MoveMouseWithJoystick);
//			out.writeBoolean(Globals.ClickMouseWithDpad);
//			out.writeInt(Globals.ClickScreenPressure);
//			out.writeInt(Globals.ClickScreenTouchspotSize);
//			out.writeBoolean(Globals.KeepAspectRatio);
//			out.writeInt(Globals.MoveMouseWithJoystickSpeed);
//			out.writeInt(Globals.MoveMouseWithJoystickAccel);
//			out.writeInt(SDL_Keys.JAVA_KEYCODE_LAST);
//			for( int i = 0; i < SDL_Keys.JAVA_KEYCODE_LAST; i++ )
//			{
//				out.writeInt(Globals.RemapHwKeycode[i]);
//			}
//			out.writeInt(Globals.RemapScreenKbKeycode.length);
//			for( int i = 0; i < Globals.RemapScreenKbKeycode.length; i++ )
//			{
//				out.writeInt(Globals.RemapScreenKbKeycode[i]);
//			}
//			out.writeInt(Globals.ScreenKbControlsShown.length);
//			for( int i = 0; i < Globals.ScreenKbControlsShown.length; i++ )
//			{
//				out.writeBoolean(Globals.ScreenKbControlsShown[i]);
//			}
//			out.writeInt(Globals.TouchscreenKeyboardTransparency);
//			out.writeInt(Globals.RemapMultitouchGestureKeycode.length);
//			for( int i = 0; i < Globals.RemapMultitouchGestureKeycode.length; i++ )
//			{
//				out.writeInt(Globals.RemapMultitouchGestureKeycode[i]);
//				out.writeBoolean(Globals.MultitouchGesturesUsed[i]);
//			}
//			out.writeInt(Globals.MultitouchGestureSensitivity);
//			for( int i = 0; i < Globals.TouchscreenCalibration.length; i++ )
//				out.writeInt(Globals.TouchscreenCalibration[i]);
//			out.writeInt(Globals.CommandLine.length());
//			for( int i = 0; i < Globals.CommandLine.length(); i++ )
//				out.writeChar(Globals.CommandLine.charAt(i));
//			out.writeInt(Globals.ScreenKbControlsLayout.length);
//			for( int i = 0; i < Globals.ScreenKbControlsLayout.length; i++ )
//				for( int ii = 0; ii < 4; ii++ )
//					out.writeInt(Globals.ScreenKbControlsLayout[i][ii]);
//			out.writeInt(Globals.LeftClickKey);
//			out.writeInt(Globals.RightClickKey);
//			out.writeBoolean(Globals.VideoLinearFilter);
//			out.writeInt(Globals.LeftClickTimeout);
//			out.writeInt(Globals.RightClickTimeout);
//			out.writeBoolean(Globals.RelativeMouseMovement);
//			out.writeInt(Globals.RelativeMouseMovementSpeed);
//			out.writeInt(Globals.RelativeMouseMovementAccel);
//			out.writeBoolean(Globals.MultiThreadedVideo);
//
//			out.writeBoolean(Globals.BrokenLibCMessageShown);
//			out.writeInt(Globals.TouchscreenKeyboardDrawSize);
//			out.writeInt(p.getApplicationVersion());
//			out.writeFloat(Globals.gyro_x1);
//			out.writeFloat(Globals.gyro_x2);
//			out.writeFloat(Globals.gyro_xc);
//			out.writeFloat(Globals.gyro_y1);
//			out.writeFloat(Globals.gyro_y2);
//			out.writeFloat(Globals.gyro_yc);
//			out.writeFloat(Globals.gyro_z1);
//			out.writeFloat(Globals.gyro_z2);
//			out.writeFloat(Globals.gyro_zc);
//
			out.close();
			settingsLoaded = true;

		} catch( FileNotFoundException e ) {
		} catch( SecurityException e ) {
		} catch ( IOException e ) {};
	}

	static void Load( final MainActivity p )
	{
		if(settingsLoaded) // Prevent starting twice
		{
			return;
		}
		Logger.log("libSDL: Settings.Load(): enter");
		nativeInitKeymap();
		for( int i = 0; i < SDL_Keys.JAVA_KEYCODE_LAST; i++ )
		{
			int sdlKey = nativeGetKeymapKey(i);
			int idx = 0;
			for(int ii = 0; ii < SDL_Keys.values.length; ii++)
				if(SDL_Keys.values[ii] == sdlKey)
					idx = ii;
			Globals.RemapHwKeycode[i] = idx;
		}
		for( int i = 0; i < Globals.RemapScreenKbKeycode.length; i++ )
		{
			int sdlKey = nativeGetKeymapKeyScreenKb(i);
			int idx = 0;
			for(int ii = 0; ii < SDL_Keys.values.length; ii++) {
				if(SDL_Keys.values[ii] == sdlKey) {
					idx = ii;
					break;
				}
			}
			Globals.RemapScreenKbKeycode[i] = idx;
		}
		Globals.ScreenKbControlsShown[0] = Globals.AppNeedsArrowKeys;
		Globals.ScreenKbControlsShown[1] = Globals.AppNeedsTextInput;
		for( int i = 2; i < Globals.ScreenKbControlsShown.length; i++ )
			Globals.ScreenKbControlsShown[i] = ( i - 2 < Globals.AppTouchscreenKeyboardKeysAmount );
		for( int i = 0; i < Globals.RemapMultitouchGestureKeycode.length; i++ )
		{
			int sdlKey = nativeGetKeymapKeyMultitouchGesture(i);
			int idx = 0;
			for(int ii = 0; ii < SDL_Keys.values.length; ii++)
				if(SDL_Keys.values[ii] == sdlKey)
					idx = ii;
			Globals.RemapMultitouchGestureKeycode[i] = idx;
		}
		for( int i = 0; i < Globals.MultitouchGesturesUsed.length; i++ )
			Globals.MultitouchGesturesUsed[i] = true;

		Logger.log("android.os.Build.MODEL: " + android.os.Build.MODEL);
		if( (android.os.Build.MODEL.equals("GT-N7000") || android.os.Build.MODEL.equals("SGH-I717"))
			&& android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1 )
		{
			// Samsung Galaxy Note generates a keypress when you hover a stylus over the screen, and that messes up OpenTTD dialogs
			// ICS update sends events in a proper way
			Globals.RemapHwKeycode[112] = SDL_Keys.SDLK_UNKNOWN;
		}

		ObjectInputStream settingsFile = null;
		try {
			settingsFile = new ObjectInputStream(new FileInputStream( p.getFilesDir().getAbsolutePath() + "/" + SettingsFileName ));
			int ver = settingsFile.readInt();

			if (ver <= 8 ) {
				/*Globals.kbSize = */settingsFile.readInt(); // obsolete
			}
			Globals.kbPos = settingsFile.readInt();

			Globals.DownloadToSdcard = settingsFile.readBoolean();
			Globals.OptionalDataDownload = new boolean[settingsFile.readInt()];
			for(int i = 0; i < Globals.OptionalDataDownload.length; i++)
				Globals.OptionalDataDownload[i] = settingsFile.readBoolean();
			StringBuilder b = new StringBuilder();
			int len = settingsFile.readInt();
			for( int i = 0; i < len; i++ )
				b.append( settingsFile.readChar() );
			Globals.DataDir = b.toString();
			if (ver >= 6) {
				Globals.pointerFollowsFinger = settingsFile.readBoolean();
			}
			if (ver >= 7) {
				Globals.accel = settingsFile.readInt();
			}
			if (ver >= 8) {
				Globals.tap2ndFingerButton = settingsFile.readInt();
			}

			if (ver >= 9) {
				Globals.apOskPosition = settingsFile.readInt();
				Globals.apOskSize = settingsFile.readInt();
				Globals.apOskDrawSize = settingsFile.readInt();
				Globals.apOskKeyMask = settingsFile.readInt();
				Globals.apOskTransparency = settingsFile.readInt();

				if (ver < 12) {
					int reordered = 0;

					if ((Globals.apOskKeyMask & (1<<0))  != 0) reordered |= (1<<0);
					if ((Globals.apOskKeyMask & (1<<3))  != 0) reordered |= (1<<5);
					if ((Globals.apOskKeyMask & (1<<12)) != 0) reordered |= (1<<6);
					if ((Globals.apOskKeyMask & (1<<4))  != 0) reordered |= (1<<7);
					if ((Globals.apOskKeyMask & (1<<5))  != 0) reordered |= (1<<9);
					if ((Globals.apOskKeyMask & (1<<6))  != 0) reordered |= (1<<10);
					if ((Globals.apOskKeyMask & (1<<7))  != 0) reordered |= (1<<11);
					if ((Globals.apOskKeyMask & (1<<9))  != 0) reordered |= (1<<15);
					if ((Globals.apOskKeyMask & (1<<8))  != 0) reordered |= (1<<16);
					if ((Globals.apOskKeyMask & (1<<10)) != 0) reordered |= (1<<17);
					if ((Globals.apOskKeyMask & (1<<11)) != 0) reordered |= (1<<18);
					if ((Globals.apOskKeyMask & (1<<13)) != 0) reordered |= (1<<21);

					Globals.apOskKeyMask = reordered;
				}
			}
			
			if (ver >= 10) {
				Globals.screenOrientation = ScreenOrientation.fromInt(settingsFile.readInt());
			}

			if (ver >= 11) {
				//Globals.upgrade_1_12_confirmed = 
						settingsFile.readInt();
			}

			if (ver >= 13) {
				Globals.sendStats = settingsFile.readBoolean();
			}

//			Globals.PhoneHasArrowKeys = settingsFile.readBoolean();
//			Globals.PhoneHasTrackball = settingsFile.readBoolean();
//			Globals.UseAccelerometerAsArrowKeys = settingsFile.readBoolean();
//			Globals.UseTouchscreenKeyboard = settingsFile.readBoolean();
//			Globals.TouchscreenKeyboardSize = settingsFile.readInt();
//			Globals.AccelerometerSensitivity = settingsFile.readInt();
//			Globals.AccelerometerCenterPos = settingsFile.readInt();
//			Globals.TrackballDampening = settingsFile.readInt();
//			Globals.AudioBufferConfig = settingsFile.readInt();
//			Globals.TouchscreenKeyboardTheme = settingsFile.readInt();
//			Globals.RightClickMethod = settingsFile.readInt();
//			Globals.ShowScreenUnderFinger = settingsFile.readInt();
//			Globals.LeftClickMethod = settingsFile.readInt();
//			Globals.MoveMouseWithJoystick = settingsFile.readBoolean();
//			Globals.ClickMouseWithDpad = settingsFile.readBoolean();
//			Globals.ClickScreenPressure = settingsFile.readInt();
//			Globals.ClickScreenTouchspotSize = settingsFile.readInt();
//			Globals.KeepAspectRatio = settingsFile.readBoolean();
//			Globals.MoveMouseWithJoystickSpeed = settingsFile.readInt();
//			Globals.MoveMouseWithJoystickAccel = settingsFile.readInt();
//			int readKeys = settingsFile.readInt();
//			for( int i = 0; i < readKeys; i++ )
//			{
//				Globals.RemapHwKeycode[i] = settingsFile.readInt();
//			}
//			if( settingsFile.readInt() != Globals.RemapScreenKbKeycode.length )
//				throw new IOException();
//			for( int i = 0; i < Globals.RemapScreenKbKeycode.length; i++ )
//			{
//				Globals.RemapScreenKbKeycode[i] = settingsFile.readInt();
//			}
//			if( settingsFile.readInt() != Globals.ScreenKbControlsShown.length )
//				throw new IOException();
//			for( int i = 0; i < Globals.ScreenKbControlsShown.length; i++ )
//			{
//				Globals.ScreenKbControlsShown[i] = settingsFile.readBoolean();
//			}
//			Globals.TouchscreenKeyboardTransparency = settingsFile.readInt();
//			if( settingsFile.readInt() != Globals.RemapMultitouchGestureKeycode.length )
//				throw new IOException();
//			for( int i = 0; i < Globals.RemapMultitouchGestureKeycode.length; i++ )
//			{
//				Globals.RemapMultitouchGestureKeycode[i] = settingsFile.readInt();
//				Globals.MultitouchGesturesUsed[i] = settingsFile.readBoolean();
//			}
//			Globals.MultitouchGestureSensitivity = settingsFile.readInt();
//			for( int i = 0; i < Globals.TouchscreenCalibration.length; i++ )
//				Globals.TouchscreenCalibration[i] = settingsFile.readInt();
//
//			b = new StringBuilder();
//			len = settingsFile.readInt();
//			for( int i = 0; i < len; i++ )
//				b.append( settingsFile.readChar() );
//			Globals.CommandLine = b.toString();
//
//			if( settingsFile.readInt() != Globals.ScreenKbControlsLayout.length )
//				throw new IOException();
//			for( int i = 0; i < Globals.ScreenKbControlsLayout.length; i++ )
//				for( int ii = 0; ii < 4; ii++ )
//					Globals.ScreenKbControlsLayout[i][ii] = settingsFile.readInt();
//			Globals.LeftClickKey = settingsFile.readInt();
//			Globals.RightClickKey = settingsFile.readInt();
//			Globals.VideoLinearFilter = settingsFile.readBoolean();
//			Globals.LeftClickTimeout = settingsFile.readInt();
//			Globals.RightClickTimeout = settingsFile.readInt();
//			Globals.RelativeMouseMovement = settingsFile.readBoolean();
//			Globals.RelativeMouseMovementSpeed = settingsFile.readInt();
//			Globals.RelativeMouseMovementAccel = settingsFile.readInt();
//			Globals.MultiThreadedVideo = settingsFile.readBoolean();
//
//			Globals.BrokenLibCMessageShown = settingsFile.readBoolean();
//			Globals.TouchscreenKeyboardDrawSize = settingsFile.readInt();
//			int cfgVersion = settingsFile.readInt();
//			Globals.gyro_x1 = settingsFile.readFloat();
//			Globals.gyro_x2 = settingsFile.readFloat();
//			Globals.gyro_xc = settingsFile.readFloat();
//			Globals.gyro_y1 = settingsFile.readFloat();
//			Globals.gyro_y2 = settingsFile.readFloat();
//			Globals.gyro_yc = settingsFile.readFloat();
//			Globals.gyro_z1 = settingsFile.readFloat();
//			Globals.gyro_z2 = settingsFile.readFloat();
//			Globals.gyro_zc = settingsFile.readFloat();
//
			settingsLoaded = true;

			Logger.log("libSDL: Settings.Load(): loaded settings successfully");
			settingsFile.close();

//			Logger.log("libSDL: old cfg version " + cfgVersion + ", our version " + p.getApplicationVersion());
//			if( cfgVersion != p.getApplicationVersion() )
//			{
//				DeleteFilesOnUpgrade();
//				if( Globals.ResetSdlConfigForThisVersion )
//				{
//					Logger.log("libSDL: old cfg version " + cfgVersion + ", our version " + p.getApplicationVersion() + " and we need to clean up config file");
//					// Delete settings file, and restart the application
//					DeleteSdlConfigOnUpgradeAndRestart(p);
//				}
//				Save(p);
//			}

			return;

		} catch( FileNotFoundException e ) {
		} catch( SecurityException e ) {
		} catch ( IOException e ) {
			DeleteFilesOnUpgrade();
			if( Globals.ResetSdlConfigForThisVersion )
			{
				Logger.log("libSDL: old cfg version unknown or too old, our version " + p.getApplicationVersion() + " and we need to clean up config file");
				DeleteSdlConfigOnUpgradeAndRestart(p);
			}
		} finally {
			if (settingsFile != null) {
				try {
					settingsFile.close();
				} catch (IOException e) { }
			}
		}
		
		if( Globals.DataDir.length() == 0 )
		{
			if( !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) )
			{
				Logger.log("libSDL: SD card or external storage is not mounted (state " + Environment.getExternalStorageState() + "), switching to the internal storage.");
				Globals.DownloadToSdcard = false;
			}
			Globals.DataDir = Globals.DownloadToSdcard ?
								SdcardAppPath.getPath(p) :
								p.getFilesDir().getAbsolutePath();
		}
		Logger.log("libSDL: Settings.Load(): loading settings failed, running config dialog");
		p.setUpStatusLabel();
		showConfig(p, true);
	}

	// ===============================================================================================

	public static abstract class Menu
	{
		// Should be overridden by children
		abstract void run(final MainActivity p);
		abstract String title(final MainActivity p);
		boolean enabled()
		{
			return true;
		}
		// Should not be overridden
		boolean enabledOrHidden()
		{
//			for( Menu m: Globals.HiddenMenuOptions )
//			{
//				if( m.getClass().getName().equals( this.getClass().getName() ) )
//					return false;
//			}
			return enabled();
		}
		void showMenuOptionsList(final MainActivity p, final Menu[] list)
		{
			menuStack.add(this);
			ArrayList<CharSequence> items = new ArrayList<CharSequence> ();
			for( Menu m: list )
			{
				if(m.enabledOrHidden())
					items.add(m.title(p));
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(title(p));
			builder.setItems(items.toArray(new CharSequence[0]), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					dialog.dismiss();
					int selected = 0;

					for( Menu m: list )
					{
						if(m.enabledOrHidden())
						{
							if( selected == item )
							{
								m.run(p);
								return;
							}
							selected ++;
						}
					}
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBackOuterMenu(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static ArrayList<Menu> menuStack = new ArrayList<Menu> ();

	public static void showConfig(final MainActivity p, final boolean firstStart)
	{
		settingsChanged = true;
		initOptionalDataDownload();

		if(!firstStart)
			new MainMenu().run(p);
		else
		{
			if( Globals.getStartupButtonTimeout() > 0 ) // If we did not disable startup menu altogether
			{
				for( Menu m: Globals.FirstStartMenuOptions )
				{
					boolean hidden = false;
					for( Menu m1: Globals.HiddenMenuOptions )
					{
						if( m1.getClass().getName().equals( m.getClass().getName() ) )
							hidden = true;
					}
					if( ! hidden )
						menuStack.add(m);
				}
			}
			if (p.licIsDummy() || p.licIsOk(false)) {
				goBack(p);
			} else {
				showTransactionIdQuestion(p,
					new Runnable() { @Override public void run() {
						goBack(p);
					}},
					new Runnable() { @Override public void run() {
						goBack(p);
					}}
				);
			}
		}
	}

	static void goBack(final MainActivity p)
	{
		if(menuStack.isEmpty())
		{
			Save(p);
			p.startDownloader();
		}
		else
		{
			Menu c = menuStack.remove(menuStack.size() - 1);
			c.run(p);
		}
	}

	static void goBackOuterMenu(final MainActivity p)
	{
		if(!menuStack.isEmpty())
			menuStack.remove(menuStack.size() - 1);
		goBack(p);
	}

	static class PlayButton extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.play_game);
		}
		void run (final MainActivity p)
		{
			goBackOuterMenu(p);
		}
	}

	static class QuitButton extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.quit_game);
		}
		void run (final MainActivity p)
		{
			Save(p);
			Logger.flush(true);
			System.exit(0);
		}
	}

	static class OkButton extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ok);
		}
		void run (final MainActivity p)
		{
			goBackOuterMenu(p);
		}
	}

	static class DummyMenu extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ok);
		}
		void run (final MainActivity p)
		{
			goBack(p);
		}
	}

	public static void showTransactionIdQuestion(final MainActivity parentActivity, final Runnable onOk, final Runnable onCancel) {
		parentActivity.runOnUiThread(new Runnable() { @Override public void run() {
			AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
			builder.setCancelable(false);
			
			LinearLayout ll = new LinearLayout(parentActivity);
			ll.setOrientation(LinearLayout.VERTICAL);

			TextView tv = new TextView(parentActivity);
			tv.setText(R.string.ap_insert_transaction_id_title);
			tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			tv.setBackgroundColor(0xff000000);
			tv.setTextColor(0xffffffff);
			tv.setPadding(5, 5, 5, 5);
			ll.addView(tv);

			final EditText et = new EditText(parentActivity);
			et.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			et.setInputType(InputType.TYPE_CLASS_PHONE);
			et.setText(parentActivity.licGetInput());
			ll.addView(et);

            builder.setView(ll);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
            	parentActivity.licSetInput(et.getText().toString());
				onOk.run();
            }});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() { public void onCancel(DialogInterface dialog) {
				onCancel.run();
			}});

            AlertDialog alert = builder.create();
            alert.show();
		}});
	}
	static class InsertTransIdButton extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_insert_transaction_id);
		}
		void run (final MainActivity p)
		{
			Runnable action = new Runnable() { @Override public void run() {
				goBack(p);
			}};

			showTransactionIdQuestion(p, action, action);
		}
	}

	public static void showUpgradeQuestion(final MainActivity parentActivity, final Runnable onOk, final Runnable onCancel) {
		onOk.run();
//		parentActivity.runOnUiThread(new Runnable() { @Override public void run() {
//			AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
//			builder.setCancelable(false);
//			
//			LinearLayout ll = new LinearLayout(parentActivity);
//			ll.setOrientation(LinearLayout.VERTICAL);
//
//			TextView tv = new TextView(parentActivity);
//			tv.setText(R.string.upg_12_title);
//			tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
//			tv.setBackgroundColor(0xff000000);
//			tv.setTextColor(0xffffffff);
//			tv.setPadding(5, 5, 5, 5);
//			ll.addView(tv);
//
//			LinearLayout ll2 = new LinearLayout(parentActivity);
//			ll2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
//			ll2.setOrientation(LinearLayout.HORIZONTAL);
//
//			TextView cbtv = new TextView(parentActivity);
//			cbtv.setText(R.string.upg_12_show);
//			cbtv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//			cbtv.setBackgroundColor(0xff000000);
//            cbtv.setTextColor(0xffffffff);
//
//			final CheckBox cb = new CheckBox(parentActivity);
//			cb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//			cb.setChecked(Globals.upgrade_1_12_confirmed != 2);
//			cb.setText("");
//
//			ll2.addView(cbtv);
//			ll2.addView(cb);
//
//			ll2.setGravity(Gravity.RIGHT | Gravity.CENTER);
//			ll2.setPadding(5, 5, 5, 15);
//
//			ll.addView(ll2);
//
//            builder.setView(ll);
//            builder.setPositiveButton(R.string.upg_12_yes, new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {
//            	dialog.dismiss();
//				Globals.upgrade_1_12_confirmed = 1;
//				Save(parentActivity);
//				onOk.run();
//            }});
//            builder.setNegativeButton(R.string.upg_12_no, new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {
//            	dialog.dismiss();
//           		if (cb.isChecked()) {
//           			Globals.upgrade_1_12_confirmed = 0;
//           		} else {
//           			Globals.upgrade_1_12_confirmed = 2;
//           		}
//           		Save(parentActivity);
//				onCancel.run();
//			}});
//			builder.setOnCancelListener(new DialogInterface.OnCancelListener() { public void onCancel(DialogInterface dialog) {
//				if (cb.isChecked()) {
//					Globals.upgrade_1_12_confirmed = 0;
//				} else {
//					Globals.upgrade_1_12_confirmed = 2;
//				}
//				Save(parentActivity);
//				onCancel.run();
//			}});
//
//            AlertDialog alert = builder.create();
//            alert.show();
//		}});
	}

	static class UpgradeButton extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_upgrade_12);
		}
		void run (final MainActivity p)
		{
			Runnable action = new Runnable() { @Override public void run() {
				goBack(p);
			}};
			
			showUpgradeQuestion(p, action, action);
		}
	}

	static class MainMenu extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.device_config);
		}
		void run (final MainActivity p)
		{
			Menu options[] = {
				new PlayButton(),
				new QuitButton(),
				new APMouseConfig(),
				new APOSKConfig(),
//				new APRemapButtons(),
				new APImpExpMenu(),
				new APFlagsMenu(),
				new APScreenOrientationMenu(),
				new DownloadConfig(),
				new OptionalDownloadConfig(false),
			};

			if (!p.licIsOk(false)) {
				Menu options2[] = new Menu[options.length + 1];
				options2[0] = new InsertTransIdButton();
				System.arraycopy(options, 0, options2, 1, options.length);
				options = options2;
			}

/*			if ((DataDownload.getAvailableVersion() >= 2) && ((Globals.upgrade_1_12_confirmed & 1) == 0)) {
				Menu options2[] = new Menu[options.length + 1];
				options2[0] = new UpgradeButton();
				System.arraycopy(options, 0, options2, 1, options.length);
				options = options2;
			} __1_12_UPG__ */
			showMenuOptionsList(p, options);
		}
	}

	static class APMouseConfig extends Menu {
		String title(final MainActivity p) {   
			return p.getResources().getString(R.string.ap_mouse);
		}

		void run (final MainActivity p)
		{
			Menu options[] =
			{
				new APMemConfig(),
				new APMAccelConfig(),
				new AP2ndFingerTapConfig(),
				new OkButton()
			};
			showMenuOptionsList(p, options);
		}
	}


	static class APMemConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_mem);
		}
		void run (final MainActivity p)
		{
			Resources res = p.getResources();
			final CharSequence[] items = {	res.getString(R.string.ap_mem1), res.getString(R.string.ap_mem2) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_kb_size_t));
			builder.setSingleChoiceItems(items, Globals.pointerFollowsFinger ? 0 : 1, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					dialog.dismiss();
					if (item == 0) Globals.pointerFollowsFinger = true;
					if (item == 1) Globals.pointerFollowsFinger = false;
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}
	static class APMAccelConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_maccel);
		}
		void run (final MainActivity p)
		{
			Resources res = p.getResources();
			final CharSequence[] items = {
					res.getString(R.string.ap_maccel0),
					res.getString(R.string.ap_maccel1),
					res.getString(R.string.ap_maccel2),
					res.getString(R.string.ap_maccel3),
					res.getString(R.string.ap_maccel4),
					res.getString(R.string.ap_maccel5),
					res.getString(R.string.ap_maccel6)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_maccel_t));
			builder.setSingleChoiceItems(items, Globals.accel, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					dialog.dismiss();
					Globals.accel = item;
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}
	static class AP2ndFingerTapConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_2nd_finger_tap);
		}
		void run (final MainActivity p)
		{
			Resources res = p.getResources();
			final CharSequence[] items = {
					res.getString(R.string.ap_2nd_finger_tap_none),
					res.getString(R.string.ap_2nd_finger_tap_left),
					res.getString(R.string.ap_2nd_finger_tap_right)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_2nd_finger_tap_t));
			builder.setSingleChoiceItems(items, Globals.tap2ndFingerButton <= 0 ? 0 : Globals.tap2ndFingerButton, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					dialog.dismiss();
					Globals.tap2ndFingerButton = item == 0 ? -1 : item;
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class APOSKConfig extends Menu {
		String title(final MainActivity p) {   
			return p.getResources().getString(R.string.ap_osk);
		}

		void run (final MainActivity p)
		{
			Menu options[] =
			{
				new APOSKIconConfig(),
				new APKbPosConfig(),
				new APOSKPosConfig(),
				new APKbSizeConfig(),
				new APOSKTranspConfig(),
				new OkButton()
			};
			showMenuOptionsList(p, options);
		}
	}

	static class APOSKIconConfig extends Menu {
		String title(final MainActivity p) {
			return p.getResources().getString(R.string.ap_osk_icons);
		}
		void run (final MainActivity p) {
			Resources res = p.getResources();
			final CharSequence[] items = {
				res.getString(R.string.ap_osk_kb),
				res.getString(R.string.ap_osk_next),
				res.getString(R.string.ap_osk_endut),
				res.getString(R.string.ap_osk_undo),
				res.getString(R.string.ap_osk_em),
				res.getString(R.string.ap_osk_bem),
				res.getString(R.string.ap_osk_updshr),
				res.getString(R.string.ap_osk_wbdel),
				res.getString(R.string.ap_osk_wbexec),
				res.getString(R.string.ap_osk_save),
				res.getString(R.string.ap_osk_load),
				res.getString(R.string.ap_osk_msg),
				res.getString(R.string.ap_osk_privmsg),
				res.getString(R.string.ap_osk_endt)
			};
			final boolean values[] = {
				(Globals.apOskKeyMask & 1) != 0,
				(Globals.apOskKeyMask & (1<<5)) != 0,
				(Globals.apOskKeyMask & (1<<6)) != 0,
				(Globals.apOskKeyMask & (1<<7)) != 0,
				(Globals.apOskKeyMask & (1<<9)) != 0,
				(Globals.apOskKeyMask & (1<<10)) != 0,
				(Globals.apOskKeyMask & (1<<11)) != 0,
				(Globals.apOskKeyMask & (1<<15)) != 0,
				(Globals.apOskKeyMask & (1<<16)) != 0,
				(Globals.apOskKeyMask & (1<<17)) != 0,
				(Globals.apOskKeyMask & (1<<18)) != 0,
				(Globals.apOskKeyMask & (1<<19)) != 0,
				(Globals.apOskKeyMask & (1<<20)) != 0,
				(Globals.apOskKeyMask & (1<<21)) != 0
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.ap_osk_icons_t));

			builder.setMultiChoiceItems(items, values, new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int item, boolean isChecked) {
					values[item] = isChecked;
				}
			});
			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item)  {
					Globals.apOskKeyMask =
						(values[0] ? 1 : 0) |
						(values[1] ? (1<<5) : 0) |
						(values[2] ? (1<<6) : 0) |
						(values[3] ? (1<<7) : 0) |
						(values[4] ? (1<<9) : 0) |
						(values[5] ? (1<<10) : 0) |
						(values[6] ? (1<<11) : 0) |
						(values[7] ? (1<<15) : 0) |
						(values[8] ? (1<<16) : 0) |
						(values[9] ? (1<<17) : 0) |
						(values[10] ? (1<<18) : 0) |
						(values[11] ? (1<<19) : 0) |
						(values[12] ? (1<<20) : 0) |
						(values[13] ? (1<<21) : 0);
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}
	static class APOSKPosConfig extends Menu {
		String title(final MainActivity p) {
			return p.getResources().getString(R.string.ap_osk_pos);
		}

		void run (final MainActivity p) {
			Resources res = p.getResources();
			final CharSequence[] items = {
				res.getString(R.string.ap_osk_pos0),
				res.getString(R.string.ap_osk_pos1),
				res.getString(R.string.ap_osk_pos2),
				res.getString(R.string.ap_osk_pos3)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_osk_pos_t));
			builder.setSingleChoiceItems(items, Globals.apOskPosition, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					Globals.apOskPosition = item;
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class APOSKTranspConfig extends Menu {
		String title(final MainActivity p) {
			return p.getResources().getString(R.string.ap_osk_transp);
		}

		void run (final MainActivity p) {
			Resources res = p.getResources();
			final CharSequence[] items = {
				res.getString(R.string.ap_osk_transp0),
				res.getString(R.string.ap_osk_transp1),
				res.getString(R.string.ap_osk_transp2),
				res.getString(R.string.ap_osk_transp3),
				res.getString(R.string.ap_osk_transp4)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_osk_transp_t));
			builder.setSingleChoiceItems(items, Globals.apOskTransparency, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					dialog.dismiss();
					Globals.apOskTransparency = item;
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class APKbSizeConfig extends Menu {
		String title(final MainActivity p) {
			return p.getResources().getString(R.string.ap_kb_size);
		}

		void run (final MainActivity p) {
			Resources res = p.getResources();
			final CharSequence[] items = {
				res.getString(R.string.ap_kb_size0),
				res.getString(R.string.ap_kb_size1),
				res.getString(R.string.ap_kb_size2)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_kb_size_t));
			builder.setSingleChoiceItems(items, Globals.apOskSize, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					dialog.dismiss();
					Globals.apOskSize = item;
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}
	static class APKbPosConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_kb_pos);
		}
		void run (final MainActivity p)
		{
			Resources res = p.getResources();
			final CharSequence[] items = {
				res.getString(R.string.ap_kb_pos1),
				res.getString(R.string.ap_kb_pos2),
				res.getString(R.string.ap_kb_pos3)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_kb_pos_t));
			builder.setSingleChoiceItems(items, Globals.kbPos, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					dialog.dismiss();
					if (item >= 0 && item < 3) Globals.kbPos = item;
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class APFlagsMenu extends Menu {
		String title(final MainActivity p) {
			return p.getResources().getString(R.string.ap_flags_menu);
		}

		@SuppressLint("InflateParams")
		static void showCustomResolutionConfig(final MainActivity p) {
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.ap_flags_respop_title));

			LayoutInflater inflater = LayoutInflater.from(p);
			View view = inflater.inflate(R.layout.resolution, null, false);

			final EditText edtWidth = (EditText) view.findViewById(R.id.res_edt_w);
			edtWidth.setText("" + Globals.customResW);
			final EditText edtHeight = (EditText) view.findViewById(R.id.res_edt_h);
			edtHeight.setText("" + Globals.customResH);

			builder.setView(view);

			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item)  {
					int w = -1;
					int h = -1;

					try {
						w = Integer.parseInt(edtWidth.getText().toString());
					} catch (NumberFormatException e) {
					}
					try {
						h = Integer.parseInt(edtHeight.getText().toString());
					} catch (NumberFormatException e) {
					}

					if (w < 800) {
						w = 800;
					}
					if (h < 480) {
						h = 480;
					}

					Globals.customResW = w;
					Globals.customResH = h;
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	
		void run (final MainActivity p)
		{
			final Resources res = p.getResources();
			final CharSequence[] items = {
				res.getString(R.string.ap_flags_res),
				res.getString(R.string.ap_flags_tut),
				res.getString(R.string.ap_flags_clean_installation),
				res.getString(R.string.ap_flags_send_debug),
				res.getString(R.string.ap_flags_send_stats),
			};
			final boolean value[] = { Globals.customRes, Globals.showTutorial, Globals.doFullClean, Globals.sendDebugInfo, Globals.sendStats };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_flags_menu));
			builder.setMultiChoiceItems(items, value, new DialogInterface.OnMultiChoiceClickListener() {
				private boolean warnShown = false;
				public void onClick(DialogInterface dialog, int item, boolean isChecked) {
					if (item == 0 && isChecked) {
						value[item] = isChecked;
						showCustomResolutionConfig(p);
					} else if (item == 2 && isChecked && !warnShown) {
						warnShown = true;
						value[2] = false;
						((AlertDialog)dialog).getListView().setItemChecked(2, false);

						AlertDialog.Builder builder = new AlertDialog.Builder(p);
						builder.setTitle(res.getString(R.string.ap_flags_clean_warn_title));
						builder.setMessage(res.getString(R.string.ap_flags_clean_warn));
						builder.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								dialog.dismiss();
							}
						});
						AlertDialog alert = builder.create();
						alert.setOwnerActivity(p);
						alert.show();
					} else {
						value[item] = isChecked;
					}
				}
			});
			builder.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.customRes = value[0];
					Globals.showTutorial = value[1];
					Globals.doFullClean = value[2];
					Globals.sendDebugInfo = value[3];
					Globals.sendStats = value[4];
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class APScreenOrientationMenu extends Menu {
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_screenorientation_menu);
		}
		void run (final MainActivity p)
		{
			Resources res = p.getResources();
			ArrayList<CharSequence> it = new ArrayList<CharSequence>();
			ArrayList<ScreenOrientation> vl = new ArrayList<ScreenOrientation>();

			int idx = 0;
			int current = -1;
			for (ScreenOrientation so : ScreenOrientation.values()) {
				if (!so.enabled(p)) {
					continue;
				}
				it.add(res.getString(so.getResId()));
				vl.add(so);
				if (so == Globals.screenOrientation) {
					current = idx;
				}
				idx++;
			}
			
			final CharSequence[] items = it.toArray(new CharSequence[it.size()]);
			final ScreenOrientation[] values = vl.toArray(new ScreenOrientation[it.size()]);

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_screenorientation_menu_t));
			builder.setSingleChoiceItems(items, current, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					dialog.dismiss();
					Globals.screenOrientation = values[item];
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class APImpExpMenu extends Menu {
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_impexp_menu);
		}
		void run (final MainActivity p)
		{
			Resources res = p.getResources();
			final CharSequence[] items = {
				res.getString(R.string.ap_impexp_exp),
				res.getString(R.string.ap_impexp_imp),
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.ap_impexp_menu));
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
					case 0:
						ImpExp.doSaveExport(p, new Runnable() {
							@Override public void run() {
								APImpExpMenu.this.run(p);
							}
						});
						break;
					case 1:
						ImpExp.doSaveImport(p, new Runnable() {
							@Override public void run() {
								APImpExpMenu.this.run(p);
							}
						});
						break;
					} 
				}
			});
			builder.setNegativeButton(p.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});

			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class APRemapButtons extends Menu { // WIP
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.ap_remap_buttons_menu);
		}
		void run (final MainActivity p)
		{
			Resources res = p.getResources();
			final CharSequence[] items = {
					// mouse click
					// mouse up
					// mouse down
					// mouse left
					// mouse right
					// show/hide osk
					// scroll up
					// scroll down
					// scroll left
					// scroll right
					// zoom in
					// zoom out
				res.getString(R.string.ap_osk_next),
				res.getString(R.string.ap_osk_endut),
				res.getString(R.string.ap_osk_undo),
				res.getString(R.string.ap_osk_em),
				res.getString(R.string.ap_osk_bem),
				res.getString(R.string.ap_osk_updshr),
				res.getString(R.string.ap_osk_wbdel),
				res.getString(R.string.ap_osk_wbexec),
				res.getString(R.string.ap_osk_save),
				res.getString(R.string.ap_osk_load),
				res.getString(R.string.ap_osk_msg),
				res.getString(R.string.ap_osk_privmsg),
				res.getString(R.string.ap_osk_endt)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(res.getString(R.string.ap_remap_buttons_menu_t));
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					boolean populated = false;

					switch (item) { //
					case 0: //
						break; //
					case 1: //
						break; //
					}

					KeyCaptureDialog.execute(p, populated, new KeyCaptureCallback() {
						@Override public void onKeySelected(int keyCode) {
							// create association: item -> keyCode
							APRemapButtons.this.run(p);
						}
						@Override public void onClear() {
							// clear association: item -> 0
							APRemapButtons.this.run(p);
						}
						@Override public void onCancel() {
							APRemapButtons.this.run(p);
						}
					});
				}
			});
			builder.setNegativeButton(p.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});

			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class MouseConfigMainMenu extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.mouse_emulation);
		}
		boolean enabled()
		{
			return Globals.AppUsesMouse;
		}
		void run (final MainActivity p)
		{
			Menu options[] =
			{
				new DisplaySizeConfig(false),
				new LeftClickConfig(),
				new RightClickConfig(),
				new AdditionalMouseConfig(),
				new JoystickMouseConfig(),
				new TouchPressureMeasurementTool(),
//				new CalibrateTouchscreenMenu(),
				new OkButton(),
			};
			showMenuOptionsList(p, options);
		}
	}

	static class KeyboardConfigMainMenu extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.controls_screenkb);
		}
		boolean enabled()
		{
			return Globals.UseTouchscreenKeyboard;
		}
		void run (final MainActivity p)
		{
			Menu options[] =
			{
				new ScreenKeyboardThemeConfig(),
				new ScreenKeyboardSizeConfig(),
				new ScreenKeyboardDrawSizeConfig(),
				new ScreenKeyboardTransparencyConfig(),
				new RemapScreenKbConfig(),
//				new CustomizeScreenKbLayout(),
				new OkButton(),
			};
			showMenuOptionsList(p, options);
		}
	}

	static class DownloadConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.storage_question);
		}
		void run (final MainActivity p)
		{
			long freeSdcard = 0;
			long freePhone = 0;
			try
			{
				StatFs sdcard = new StatFs(Environment.getExternalStorageDirectory().getPath());
				StatFs phone = new StatFs(Environment.getDataDirectory().getPath());
				freeSdcard = (long)sdcard.getAvailableBlocks() * sdcard.getBlockSize() / 1024 / 1024;
				freePhone = (long)phone.getAvailableBlocks() * phone.getBlockSize() / 1024 / 1024;
			}
			catch(Exception e) {}

			final CharSequence[] items = { p.getResources().getString(R.string.storage_phone, freePhone),
											p.getResources().getString(R.string.storage_sd, freeSdcard),
											p.getResources().getString(R.string.storage_custom) };
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.storage_question));
			builder.setSingleChoiceItems(items, Globals.DownloadToSdcard ? 1 : 0, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					dialog.dismiss();

					if( item == 2 )
						showCustomDownloadDirConfig(p);
					else
					{
						Globals.DownloadToSdcard = (item != 0);
						Globals.DataDir = Globals.DownloadToSdcard ?
										SdcardAppPath.getPath(p) :
										p.getFilesDir().getAbsolutePath();
						goBack(p);
					}
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
		static void showCustomDownloadDirConfig(final MainActivity p)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.storage_custom));

			final EditText edit = new EditText(p);
			edit.setFocusableInTouchMode(true);
			edit.setFocusable(true);
			edit.setText(Globals.DataDir);
			builder.setView(edit);

			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.DataDir = edit.getText().toString();
					dialog.dismiss();
					goBack(p);//showCommandLineConfig(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
		/*static void showCommandLineConfig(final MainActivity p)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.storage_commandline));

			final EditText edit = new EditText(p);
			edit.setFocusableInTouchMode(true);
			edit.setFocusable(true);
			edit.setText(Globals.CommandLine);
			builder.setView(edit);

			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.CommandLine = edit.getText().toString();
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}*/
	}

	static class OptionalDownloadConfig extends Menu {
		boolean firstStart = false;
		OptionalDownloadConfig() {
			firstStart = false;
		}
		OptionalDownloadConfig(boolean firstStart) {
			this.firstStart = firstStart;
		}
		String title(final MainActivity p) {
			return p.getResources().getString(R.string.downloads);
		}

		private int mapIndexToMenu(int idx) {
			switch (idx) {
			case 3:
				return 4;
			case 4:
				return 3;
			}
			return idx;
		}
		private int mapMenuToIndex(int idx) {
			switch (idx) {
			case 3:
				return 4;
			case 4:
				return 3;
			}
			return idx;
		}
		
		void run (final MainActivity p) {
			int size = DataDownload.packCount();

			initOptionalDataDownload();

			CharSequence items[] = new CharSequence[size];
			final boolean values[] = new boolean[size];

			for (int i=0; i<size; i++) {
				int ii = mapIndexToMenu(i);
				items[i] = DataDownload.getPack(ii).getDescription();
				values[i] = Globals.OptionalDataDownload[ii];
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.downloads));

			builder.setMultiChoiceItems(items, values, new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int item, boolean isChecked) {
					int ii = mapMenuToIndex(item);
					values[item] = isChecked;
					Globals.OptionalDataDownload[ii] = isChecked;

					if ( DataDownload.isMandatory(ii) && !isChecked ) {
						Globals.OptionalDataDownload[ii] = true;
						values[item] = true;
						((AlertDialog)dialog).getListView().setItemChecked(item, true);

					} else if (isChecked && ii == 2) {
						Globals.OptionalDataDownload[4] = false;
						values[mapIndexToMenu(4)] = false;
						((AlertDialog)dialog).getListView().setItemChecked(mapIndexToMenu(4), false);
					} else if (isChecked && ii == 4) {
						Globals.OptionalDataDownload[2] = false;
						values[mapIndexToMenu(2)] = false;
						((AlertDialog)dialog).getListView().setItemChecked(mapIndexToMenu(2), false);
					}
				}
			});
			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item)  {
					dialog.dismiss();
					goBack(p);
				}
			});
			if (firstStart) {
				builder.setNegativeButton(p.getResources().getString(R.string.show_more_options), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						menuStack.clear();
						new MainMenu().run(p);
					}
				});
			}
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenKeyboardSizeConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.controls_screenkb_size);
		}
		void run (final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.controls_screenkb_large),
											p.getResources().getString(R.string.controls_screenkb_medium),
											p.getResources().getString(R.string.controls_screenkb_small),
											p.getResources().getString(R.string.controls_screenkb_tiny) };

			for( int i = 0; i < Globals.ScreenKbControlsLayout.length; i++ )
				for( int ii = 0; ii < 4; ii++ )
					Globals.ScreenKbControlsLayout[i][ii] = 0;

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.controls_screenkb_size));
			builder.setSingleChoiceItems(items, Globals.TouchscreenKeyboardSize, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.TouchscreenKeyboardSize = item;

					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenKeyboardDrawSizeConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.controls_screenkb_drawsize);
		}
		void run (final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.controls_screenkb_large),
											p.getResources().getString(R.string.controls_screenkb_medium),
											p.getResources().getString(R.string.controls_screenkb_small),
											p.getResources().getString(R.string.controls_screenkb_tiny) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.controls_screenkb_drawsize));
			builder.setSingleChoiceItems(items, Globals.TouchscreenKeyboardDrawSize, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.TouchscreenKeyboardDrawSize = item;

					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenKeyboardThemeConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.controls_screenkb_theme);
		}
		void run (final MainActivity p)
		{
			final CharSequence[] items = {
				p.getResources().getString(R.string.controls_screenkb_by, "Ultimate Droid", "Sean Stieber"),
				p.getResources().getString(R.string.controls_screenkb_by, "Simple Theme", "Beholder"),
				p.getResources().getString(R.string.controls_screenkb_by, "Sun", "Sirea")
				};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.controls_screenkb_theme));
			builder.setSingleChoiceItems(items, Globals.TouchscreenKeyboardTheme, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.TouchscreenKeyboardTheme = item;

					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ScreenKeyboardTransparencyConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.controls_screenkb_transparency);
		}
		void run (final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.controls_screenkb_trans_0),
											p.getResources().getString(R.string.controls_screenkb_trans_1),
											p.getResources().getString(R.string.controls_screenkb_trans_2),
											p.getResources().getString(R.string.controls_screenkb_trans_3),
											p.getResources().getString(R.string.controls_screenkb_trans_4) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.controls_screenkb_transparency));
			builder.setSingleChoiceItems(items, Globals.TouchscreenKeyboardTransparency, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.TouchscreenKeyboardTransparency = item;

					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class AudioConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.audiobuf_question);
		}
		void run (final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.audiobuf_verysmall),
											p.getResources().getString(R.string.audiobuf_small),
											p.getResources().getString(R.string.audiobuf_medium),
											p.getResources().getString(R.string.audiobuf_large) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.audiobuf_question);
			builder.setSingleChoiceItems(items, Globals.AudioBufferConfig, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.AudioBufferConfig = item;
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class DisplaySizeConfig extends Menu
	{
		boolean firstStart = false;
		DisplaySizeConfig()
		{
			this.firstStart = false;
		}
		DisplaySizeConfig(boolean firstStart)
		{
			this.firstStart = firstStart;
		}
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.display_size_mouse);
		}
		void run (final MainActivity p)
		{
			CharSequence[] items = {
										p.getResources().getString(R.string.display_size_tiny_touchpad),
										p.getResources().getString(R.string.display_size_tiny),
										p.getResources().getString(R.string.display_size_small),
										p.getResources().getString(R.string.display_size_small_touchpad),
										p.getResources().getString(R.string.display_size_large),
									};
			int _size_tiny_touchpad = 0;
			int _size_tiny = 1;
			int _size_small = 2;
			int _size_small_touchpad = 3;
			int _size_large = 4;
			int _more_options = 5;

			if( ! Globals.SwVideoMode )
			{
				CharSequence[] items2 = {
											p.getResources().getString(R.string.display_size_small_touchpad),
											p.getResources().getString(R.string.display_size_large),
										};
				items = items2;
				_size_small_touchpad = 0;
				_size_large = 1;
				_size_tiny_touchpad = _size_tiny = _size_small = 1000;

			}
			if( firstStart )
			{
				CharSequence[] items2 = {
											p.getResources().getString(R.string.display_size_tiny_touchpad),
											p.getResources().getString(R.string.display_size_tiny),
											p.getResources().getString(R.string.display_size_small),
											p.getResources().getString(R.string.display_size_small_touchpad),
											p.getResources().getString(R.string.display_size_large),
											p.getResources().getString(R.string.show_more_options),
										};
				items = items2;
				if( ! Globals.SwVideoMode )
				{
					CharSequence[] items3 = {
												p.getResources().getString(R.string.display_size_small_touchpad),
												p.getResources().getString(R.string.display_size_large),
												p.getResources().getString(R.string.show_more_options),
											};
					items = items3;
					_more_options = 3;
				}
			}
			// Java is so damn worse than C++11
			final int size_tiny_touchpad = _size_tiny_touchpad;
			final int size_tiny = _size_tiny;
			final int size_small = _size_small;
			final int size_small_touchpad = _size_small_touchpad;
			final int size_large = _size_large;
			final int more_options = _more_options;

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.display_size);
			class ClickListener implements DialogInterface.OnClickListener
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					dialog.dismiss();
					if( item == size_large )
					{
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_NORMAL;
						Globals.RelativeMouseMovement = false;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_NONE;
					}
					if( item == size_small )
					{
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_NEAR_CURSOR;
						Globals.RelativeMouseMovement = false;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_MAGNIFIER;
					}
					if( item == size_small_touchpad )
					{
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_WITH_TAP_OR_TIMEOUT;
						Globals.RelativeMouseMovement = true;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_NONE;
					}
					if( item == size_tiny )
					{
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_NEAR_CURSOR;
						Globals.RelativeMouseMovement = false;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_SCREEN_TRANSFORM;
					}
					if( item == size_tiny_touchpad )
					{
						Globals.LeftClickMethod = Mouse.LEFT_CLICK_WITH_TAP_OR_TIMEOUT;
						Globals.RelativeMouseMovement = true;
						Globals.ShowScreenUnderFinger = Mouse.ZOOM_FULLSCREEN_MAGNIFIER;
					}
					if( item == more_options )
					{
						menuStack.clear();
						new MainMenu().run(p);
						return;
					}
					goBack(p);
				}
			}
			builder.setItems(items, new ClickListener());
			/*
			else
				builder.setSingleChoiceItems(items,
					Globals.ShowScreenUnderFinger == Mouse.ZOOM_NONE ?
					( Globals.RelativeMouseMovement ? Globals.SwVideoMode ? 2 : 1 : 0 ) :
					( Globals.ShowScreenUnderFinger == Mouse.ZOOM_MAGNIFIER && Globals.SwVideoMode ) ? 1 :
					Globals.ShowScreenUnderFinger + 1,
					new ClickListener());
			*/
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class LeftClickConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.leftclick_question);
		}
		void run (final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.leftclick_normal),
											p.getResources().getString(R.string.leftclick_near_cursor),
											p.getResources().getString(R.string.leftclick_multitouch),
											p.getResources().getString(R.string.leftclick_pressure),
											p.getResources().getString(R.string.rightclick_key),
											p.getResources().getString(R.string.leftclick_timeout),
											p.getResources().getString(R.string.leftclick_tap),
											p.getResources().getString(R.string.leftclick_tap_or_timeout) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.leftclick_question);
			builder.setSingleChoiceItems(items, Globals.LeftClickMethod, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					dialog.dismiss();
					Globals.LeftClickMethod = item;
					if( item == Mouse.LEFT_CLICK_WITH_KEY )
						p.keyListener = new KeyRemapToolMouseClick(p, true);
					else if( item == Mouse.LEFT_CLICK_WITH_TIMEOUT || item == Mouse.LEFT_CLICK_WITH_TAP_OR_TIMEOUT )
						showLeftClickTimeoutConfig(p);
					else
						goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
		static void showLeftClickTimeoutConfig(final MainActivity p) {
			final CharSequence[] items = {	p.getResources().getString(R.string.leftclick_timeout_time_0),
											p.getResources().getString(R.string.leftclick_timeout_time_1),
											p.getResources().getString(R.string.leftclick_timeout_time_2),
											p.getResources().getString(R.string.leftclick_timeout_time_3),
											p.getResources().getString(R.string.leftclick_timeout_time_4) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.leftclick_timeout_time);
			builder.setSingleChoiceItems(items, Globals.LeftClickTimeout, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.LeftClickTimeout = item;
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class RightClickConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.rightclick_question);
		}
		boolean enabled()
		{
			return Globals.AppNeedsTwoButtonMouse;
		}
		void run (final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.rightclick_none),
											p.getResources().getString(R.string.rightclick_multitouch),
											p.getResources().getString(R.string.rightclick_pressure),
											p.getResources().getString(R.string.rightclick_key),
											p.getResources().getString(R.string.leftclick_timeout) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.rightclick_question);
			builder.setSingleChoiceItems(items, Globals.RightClickMethod, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.RightClickMethod = item;
					dialog.dismiss();
					if( item == Mouse.RIGHT_CLICK_WITH_KEY )
						p.keyListener = new KeyRemapToolMouseClick(p, false);
					else if( item == Mouse.RIGHT_CLICK_WITH_TIMEOUT )
						showRightClickTimeoutConfig(p);
					else
						goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showRightClickTimeoutConfig(final MainActivity p) {
			final CharSequence[] items = {	p.getResources().getString(R.string.leftclick_timeout_time_0),
											p.getResources().getString(R.string.leftclick_timeout_time_1),
											p.getResources().getString(R.string.leftclick_timeout_time_2),
											p.getResources().getString(R.string.leftclick_timeout_time_3),
											p.getResources().getString(R.string.leftclick_timeout_time_4) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.leftclick_timeout_time);
			builder.setSingleChoiceItems(items, Globals.RightClickTimeout, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.RightClickTimeout = item;
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	public static class KeyRemapToolMouseClick implements KeyEventsListener
	{
		MainActivity p;
		boolean leftClick;
		public KeyRemapToolMouseClick(MainActivity _p, boolean leftClick)
		{
			p = _p;
			p.setText(p.getResources().getString(R.string.remap_hwkeys_press));
			this.leftClick = leftClick;
		}
		
		public void onKeyEvent(final int keyCode)
		{
			p.keyListener = null;
			int keyIndex = keyCode;
			if( keyIndex < 0 )
				keyIndex = 0;
			if( keyIndex > SDL_Keys.JAVA_KEYCODE_LAST )
				keyIndex = 0;

			if( leftClick )
				Globals.LeftClickKey = keyIndex;
			else
				Globals.RightClickKey = keyIndex;

			goBack(p);
		}
	}

	static class AdditionalMouseConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.pointandclick_question);
		}
		void run (final MainActivity p)
		{
			CharSequence[] items = {
				p.getResources().getString(R.string.pointandclick_joystickmouse),
				p.getResources().getString(R.string.click_with_dpadcenter),
				p.getResources().getString(R.string.pointandclick_relative)
			};

			boolean defaults[] = { 
				Globals.MoveMouseWithJoystick,
				Globals.ClickMouseWithDpad,
				Globals.RelativeMouseMovement
			};

			
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.pointandclick_question));
			builder.setMultiChoiceItems(items, defaults, new DialogInterface.OnMultiChoiceClickListener()
			{
				public void onClick(DialogInterface dialog, int item, boolean isChecked) 
				{
					if( item == 0 )
						Globals.MoveMouseWithJoystick = isChecked;
					if( item == 1 )
						Globals.ClickMouseWithDpad = isChecked;
					if( item == 2 )
						Globals.RelativeMouseMovement = isChecked;
				}
			});
			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					dialog.dismiss();
					if( Globals.RelativeMouseMovement )
						showRelativeMouseMovementConfig(p);
					else
						goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showRelativeMouseMovementConfig(final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.accel_veryslow),
											p.getResources().getString(R.string.accel_slow),
											p.getResources().getString(R.string.accel_medium),
											p.getResources().getString(R.string.accel_fast),
											p.getResources().getString(R.string.accel_veryfast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.pointandclick_relative_speed);
			builder.setSingleChoiceItems(items, Globals.RelativeMouseMovementSpeed, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.RelativeMouseMovementSpeed = item;

					dialog.dismiss();
					showRelativeMouseMovementConfig1(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showRelativeMouseMovementConfig1(final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.none),
											p.getResources().getString(R.string.accel_slow),
											p.getResources().getString(R.string.accel_medium),
											p.getResources().getString(R.string.accel_fast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.pointandclick_relative_accel);
			builder.setSingleChoiceItems(items, Globals.RelativeMouseMovementAccel, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.RelativeMouseMovementAccel = item;

					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class JoystickMouseConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.pointandclick_joystickmousespeed);
		}
		boolean enabled()
		{
			return Globals.MoveMouseWithJoystick;
		};
		void run (final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.accel_slow),
											p.getResources().getString(R.string.accel_medium),
											p.getResources().getString(R.string.accel_fast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.pointandclick_joystickmousespeed);
			builder.setSingleChoiceItems(items, Globals.MoveMouseWithJoystickSpeed, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.MoveMouseWithJoystickSpeed = item;

					dialog.dismiss();
					showJoystickMouseAccelConfig(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showJoystickMouseAccelConfig(final MainActivity p)
		{
			final CharSequence[] items = {	p.getResources().getString(R.string.none),
											p.getResources().getString(R.string.accel_slow),
											p.getResources().getString(R.string.accel_medium),
											p.getResources().getString(R.string.accel_fast) };

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.pointandclick_joystickmouseaccel);
			builder.setSingleChoiceItems(items, Globals.MoveMouseWithJoystickAccel, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.MoveMouseWithJoystickAccel = item;

					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	public interface TouchEventsListener
	{
		public void onTouchEvent(final MotionEvent ev);
	}

	public interface KeyEventsListener
	{
		public void onKeyEvent(final int keyCode);
	}

	static class TouchPressureMeasurementTool extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.measurepressure);
		}
		boolean enabled()
		{
			return Globals.RightClickMethod == Mouse.RIGHT_CLICK_WITH_PRESSURE ||
					Globals.LeftClickMethod == Mouse.LEFT_CLICK_WITH_PRESSURE;
		};
		void run (final MainActivity p)
		{
			p.setText(p.getResources().getString(R.string.measurepressure_touchplease));
			p.touchListener = new TouchMeasurementTool(p);
		}

		public static class TouchMeasurementTool implements TouchEventsListener
		{
			MainActivity p;
			ArrayList<Integer> force = new ArrayList<Integer>();
			ArrayList<Integer> radius = new ArrayList<Integer>();
			static final int maxEventAmount = 100;
			
			public TouchMeasurementTool(MainActivity _p) 
			{
				p = _p;
			}

			public void onTouchEvent(final MotionEvent ev)
			{
				force.add((int)(ev.getPressure() * 1000.0));
				radius.add((int)(ev.getSize() * 1000.0));
				p.setText(p.getResources().getString(R.string.measurepressure_response, force.get(force.size()-1), radius.get(radius.size()-1)));
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) { }
				
				if( force.size() >= maxEventAmount )
				{
					p.touchListener = null;
					Globals.ClickScreenPressure = getAverageForce();
					Globals.ClickScreenTouchspotSize = getAverageRadius();
					Logger.log("SDL: measured average force " + Globals.ClickScreenPressure + " radius " + Globals.ClickScreenTouchspotSize);
					goBack(p);
				}
			}

			int getAverageForce()
			{
				int avg = 0;
				for(Integer f: force)
				{
					avg += f;
				}
				return avg / force.size();
			}
			int getAverageRadius()
			{
				int avg = 0;
				for(Integer r: radius)
				{
					avg += r;
				}
				return avg / radius.size();
			}
		}
	}
	
	static class RemapHwKeysConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.remap_hwkeys);
		}
		//boolean enabled() { return true; };
		void run (final MainActivity p)
		{
			p.setText(p.getResources().getString(R.string.remap_hwkeys_press));
			p.keyListener = new KeyRemapTool(p);
		}

		public static class KeyRemapTool implements KeyEventsListener
		{
			MainActivity p;
			public KeyRemapTool(MainActivity _p)
			{
				p = _p;
			}
			
			public void onKeyEvent(final int keyCode)
			{
				p.keyListener = null;
				int keyIndex = keyCode;
				if( keyIndex < 0 )
					keyIndex = 0;
				if( keyIndex > SDL_Keys.JAVA_KEYCODE_LAST )
					keyIndex = 0;

				final int KeyIndexFinal = keyIndex;
				AlertDialog.Builder builder = new AlertDialog.Builder(p);
				builder.setTitle(R.string.remap_hwkeys_select);
				builder.setSingleChoiceItems(SDL_Keys.namesSorted, SDL_Keys.namesSortedBackIdx[Globals.RemapHwKeycode[keyIndex]], new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int item)
					{
						Globals.RemapHwKeycode[KeyIndexFinal] = SDL_Keys.namesSortedIdx[item];

						dialog.dismiss();
						goBack(p);
					}
				});
				builder.setOnCancelListener(new DialogInterface.OnCancelListener()
				{
					public void onCancel(DialogInterface dialog)
					{
						goBack(p);
					}
				});
				AlertDialog alert = builder.create();
				alert.setOwnerActivity(p);
				alert.show();
			}
		}
	}

	static class RemapScreenKbConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.remap_screenkb);
		}
		//boolean enabled() { return true; };
		void run (final MainActivity p)
		{
			CharSequence[] items = {
				p.getResources().getString(R.string.remap_screenkb_joystick),
				p.getResources().getString(R.string.remap_screenkb_button_text),
				p.getResources().getString(R.string.remap_screenkb_button) + " 1",
				p.getResources().getString(R.string.remap_screenkb_button) + " 2",
				p.getResources().getString(R.string.remap_screenkb_button) + " 3",
				p.getResources().getString(R.string.remap_screenkb_button) + " 4",
				p.getResources().getString(R.string.remap_screenkb_button) + " 5",
				p.getResources().getString(R.string.remap_screenkb_button) + " 6",
			};

			boolean defaults[] = { 
				Globals.ScreenKbControlsShown[0],
				Globals.ScreenKbControlsShown[1],
				Globals.ScreenKbControlsShown[2],
				Globals.ScreenKbControlsShown[3],
				Globals.ScreenKbControlsShown[4],
				Globals.ScreenKbControlsShown[5],
				Globals.ScreenKbControlsShown[6],
				Globals.ScreenKbControlsShown[7],
			};
			
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.remap_screenkb));
			builder.setMultiChoiceItems(items, defaults, new DialogInterface.OnMultiChoiceClickListener() 
			{
				public void onClick(DialogInterface dialog, int item, boolean isChecked) 
				{
					if( ! Globals.UseTouchscreenKeyboard )
						item += 8;
					Globals.ScreenKbControlsShown[item] = isChecked;
				}
			});
			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					dialog.dismiss();
					showRemapScreenKbConfig2(p, 0);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showRemapScreenKbConfig2(final MainActivity p, final int currentButton)
		{
			CharSequence[] items = {
				p.getResources().getString(R.string.remap_screenkb_button) + " 1",
				p.getResources().getString(R.string.remap_screenkb_button) + " 2",
				p.getResources().getString(R.string.remap_screenkb_button) + " 3",
				p.getResources().getString(R.string.remap_screenkb_button) + " 4",
				p.getResources().getString(R.string.remap_screenkb_button) + " 5",
				p.getResources().getString(R.string.remap_screenkb_button) + " 6",
			};
			
			if( currentButton >= Globals.RemapScreenKbKeycode.length )
			{
				goBack(p);
				return;
			}
			if( ! Globals.ScreenKbControlsShown[currentButton + 2] )
			{
				showRemapScreenKbConfig2(p, currentButton + 1);
				return;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(items[currentButton]);
			builder.setSingleChoiceItems(SDL_Keys.namesSorted, SDL_Keys.namesSortedBackIdx[Globals.RemapScreenKbKeycode[currentButton]], new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					Globals.RemapScreenKbKeycode[currentButton] = SDL_Keys.namesSortedIdx[item];

					dialog.dismiss();
					showRemapScreenKbConfig2(p, currentButton + 1);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}
	
	static class ScreenGesturesConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.remap_screenkb_button_gestures);
		}
		//boolean enabled() { return true; };
		void run (final MainActivity p)
		{
			CharSequence[] items = {
				p.getResources().getString(R.string.remap_screenkb_button_zoomin),
				p.getResources().getString(R.string.remap_screenkb_button_zoomout),
				p.getResources().getString(R.string.remap_screenkb_button_rotateleft),
				p.getResources().getString(R.string.remap_screenkb_button_rotateright),
			};

			boolean defaults[] = { 
				Globals.MultitouchGesturesUsed[0],
				Globals.MultitouchGesturesUsed[1],
				Globals.MultitouchGesturesUsed[2],
				Globals.MultitouchGesturesUsed[3],
			};
			
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.remap_screenkb_button_gestures));
			builder.setMultiChoiceItems(items, defaults, new DialogInterface.OnMultiChoiceClickListener() 
			{
				public void onClick(DialogInterface dialog, int item, boolean isChecked) 
				{
					Globals.MultitouchGesturesUsed[item] = isChecked;
				}
			});
			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					dialog.dismiss();
					showScreenGesturesConfig2(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showScreenGesturesConfig2(final MainActivity p)
		{
			final CharSequence[] items = {
				p.getResources().getString(R.string.accel_slow),
				p.getResources().getString(R.string.accel_medium),
				p.getResources().getString(R.string.accel_fast),
				p.getResources().getString(R.string.accel_veryfast)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(R.string.remap_screenkb_button_gestures_sensitivity);
			builder.setSingleChoiceItems(items, Globals.MultitouchGestureSensitivity, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					Globals.MultitouchGestureSensitivity = item;

					dialog.dismiss();
					showScreenGesturesConfig3(p, 0);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}

		static void showScreenGesturesConfig3(final MainActivity p, final int currentButton)
		{
			CharSequence[] items = {
				p.getResources().getString(R.string.remap_screenkb_button_zoomin),
				p.getResources().getString(R.string.remap_screenkb_button_zoomout),
				p.getResources().getString(R.string.remap_screenkb_button_rotateleft),
				p.getResources().getString(R.string.remap_screenkb_button_rotateright),
			};
			
			if( currentButton >= Globals.RemapMultitouchGestureKeycode.length )
			{
				goBack(p);
				return;
			}
			if( ! Globals.MultitouchGesturesUsed[currentButton] )
			{
				showScreenGesturesConfig3(p, currentButton + 1);
				return;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(items[currentButton]);
			builder.setSingleChoiceItems(SDL_Keys.namesSorted, SDL_Keys.namesSortedBackIdx[Globals.RemapMultitouchGestureKeycode[currentButton]], new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item)
				{
					Globals.RemapMultitouchGestureKeycode[currentButton] = SDL_Keys.namesSortedIdx[item];

					dialog.dismiss();
					showScreenGesturesConfig3(p, currentButton + 1);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}
	
//	static class CalibrateTouchscreenMenu extends Menu
//	{
//		String title(final MainActivity p)
//		{
//			return p.getResources().getString(R.string.calibrate_touchscreen);
//		}
//		//boolean enabled() { return true; };
//		void run (final MainActivity p)
//		{
//			p.setText(p.getResources().getString(R.string.calibrate_touchscreen_touch));
//			Globals.TouchscreenCalibration[0] = 0;
//			Globals.TouchscreenCalibration[1] = 0;
//			Globals.TouchscreenCalibration[2] = 0;
//			Globals.TouchscreenCalibration[3] = 0;
//			ScreenEdgesCalibrationTool tool = new ScreenEdgesCalibrationTool(p);
//			p.touchListener = tool;
//			p.keyListener = tool;
//		}
//
//		static class ScreenEdgesCalibrationTool implements TouchEventsListener, KeyEventsListener
//		{
//			MainActivity p;
//			ImageView img;
//			Bitmap bmp;
//			
//			public ScreenEdgesCalibrationTool(MainActivity _p) 
//			{
//				p = _p;
//				img = new ImageView(p);
//				img.setLayoutParams(new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
//				img.setScaleType(ImageView.ScaleType.MATRIX);
//				bmp = BitmapFactory.decodeResource( p.getResources(), R.drawable.calibrate );
//				img.setImageBitmap(bmp);
//				Matrix m = new Matrix();
//				RectF src = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
//				RectF dst = new RectF(Globals.TouchscreenCalibration[0], Globals.TouchscreenCalibration[1], 
//										Globals.TouchscreenCalibration[2], Globals.TouchscreenCalibration[3]);
//				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//				img.setImageMatrix(m);
//				p.getVideoLayout().addView(img);
//			}
//
//			public void onTouchEvent(final MotionEvent ev)
//			{
//				if( Globals.TouchscreenCalibration[0] == Globals.TouchscreenCalibration[1] &&
//					Globals.TouchscreenCalibration[1] == Globals.TouchscreenCalibration[2] &&
//					Globals.TouchscreenCalibration[2] == Globals.TouchscreenCalibration[3] )
//				{
//					Globals.TouchscreenCalibration[0] = (int)ev.getX();
//					Globals.TouchscreenCalibration[1] = (int)ev.getY();
//					Globals.TouchscreenCalibration[2] = (int)ev.getX();
//					Globals.TouchscreenCalibration[3] = (int)ev.getY();
//				}
//				if( ev.getX() < Globals.TouchscreenCalibration[0] )
//					Globals.TouchscreenCalibration[0] = (int)ev.getX();
//				if( ev.getY() < Globals.TouchscreenCalibration[1] )
//					Globals.TouchscreenCalibration[1] = (int)ev.getY();
//				if( ev.getX() > Globals.TouchscreenCalibration[2] )
//					Globals.TouchscreenCalibration[2] = (int)ev.getX();
//				if( ev.getY() > Globals.TouchscreenCalibration[3] )
//					Globals.TouchscreenCalibration[3] = (int)ev.getY();
//				Matrix m = new Matrix();
//				RectF src = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
//				RectF dst = new RectF(Globals.TouchscreenCalibration[0], Globals.TouchscreenCalibration[1], 
//										Globals.TouchscreenCalibration[2], Globals.TouchscreenCalibration[3]);
//				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//				img.setImageMatrix(m);
//			}
//
//			public void onKeyEvent(final int keyCode)
//			{
//				p.touchListener = null;
//				p.keyListener = null;
//				p.getVideoLayout().removeView(img);
//				goBack(p);
//			}
//		}
//	}

//	static class CustomizeScreenKbLayout extends Menu
//	{
//		String title(final MainActivity p)
//		{
//			return p.getResources().getString(R.string.screenkb_custom_layout);
//		}
//		//boolean enabled() { return true; };
//		void run (final MainActivity p)
//		{
//			p.setText(p.getResources().getString(R.string.screenkb_custom_layout_help));
//			CustomizeScreenKbLayoutTool tool = new CustomizeScreenKbLayoutTool(p);
//			p.touchListener = tool;
//			p.keyListener = tool;
//		}
//
//		static class CustomizeScreenKbLayoutTool implements TouchEventsListener, KeyEventsListener
//		{
//			MainActivity p;
//			FrameLayout layout = null;
//			ImageView imgs[] = new ImageView[Globals.ScreenKbControlsLayout.length];
//			Bitmap bmps[] = new Bitmap[Globals.ScreenKbControlsLayout.length];
//			ImageView boundary = null;
//			Bitmap boundaryBmp = null;
//			int currentButton = 0;
//			int buttons[] = {
//				R.drawable.dpad,
//				R.drawable.keyboard,
//				R.drawable.b1,
//				R.drawable.b2,
//				R.drawable.b3,
//				R.drawable.b4,
//				R.drawable.b5,
//				R.drawable.b6
//			};
//			
//			public CustomizeScreenKbLayoutTool(MainActivity _p) 
//			{
//				p = _p;
//				layout = new FrameLayout(p);
//				p.getVideoLayout().addView(layout);
//				boundary = new ImageView(p);
//				boundary.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
//				boundary.setScaleType(ImageView.ScaleType.MATRIX);
//				boundaryBmp = BitmapFactory.decodeResource( p.getResources(), R.drawable.rectangle );
//				boundary.setImageBitmap(boundaryBmp);
//				layout.addView(boundary);
//				currentButton = 0;
//				if( Globals.TouchscreenKeyboardTheme == 2 )
//				{
//					int buttons2[] = {
//						R.drawable.sun_dpad,
//						R.drawable.sun_keyboard,
//						R.drawable.sun_b1,
//						R.drawable.sun_b2,
//						R.drawable.sun_b3,
//						R.drawable.sun_b4,
//						R.drawable.sun_b5,
//						R.drawable.sun_b6
//					};
//					buttons = buttons2;
//				}
//
//				for( int i = 0; i < Globals.ScreenKbControlsLayout.length; i++ )
//				{
//					imgs[i] = new ImageView(p);
//					imgs[i].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
//					imgs[i].setScaleType(ImageView.ScaleType.MATRIX);
//					bmps[i] = BitmapFactory.decodeResource( p.getResources(), buttons[i] );
//					imgs[i].setImageBitmap(bmps[i]);
//					imgs[i].setAlpha(128);
//					layout.addView(imgs[i]);
//					Matrix m = new Matrix();
//					RectF src = new RectF(0, 0, bmps[i].getWidth(), bmps[i].getHeight());
//					RectF dst = new RectF(Globals.ScreenKbControlsLayout[i][0], Globals.ScreenKbControlsLayout[i][1],
//											Globals.ScreenKbControlsLayout[i][2], Globals.ScreenKbControlsLayout[i][3]);
//					m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//					imgs[i].setImageMatrix(m);
//				}
//				boundary.bringToFront();
//
//				setupButton(true);
//			}
//			
//			void setupButton(boolean undo)
//			{
//				do {
//					currentButton += (undo ? -1 : 1);
//					if(currentButton >= Globals.ScreenKbControlsLayout.length)
//					{
//						p.getVideoLayout().removeView(layout);
//						layout = null;
//						p.touchListener = null;
//						p.keyListener = null;
//						goBack(p);
//						return;
//					}
//					if(currentButton < 0)
//					{
//						currentButton = 0;
//						undo = false;
//					}
//				} while( ! Globals.ScreenKbControlsShown[currentButton] );
//				
//				if( Globals.ScreenKbControlsLayout[currentButton][0] == Globals.ScreenKbControlsLayout[currentButton][2] ||
//					Globals.ScreenKbControlsLayout[currentButton][1] == Globals.ScreenKbControlsLayout[currentButton][3] )
//				{
//					int displayX = 800;
//					int displayY = 480;
//					try {
//						DisplayMetrics dm = new DisplayMetrics();
//						p.getWindowManager().getDefaultDisplay().getMetrics(dm);
//						displayX = dm.widthPixels;
//						displayY = dm.heightPixels;
//					} catch (Exception eeeee) {}
//					Globals.ScreenKbControlsLayout[currentButton][0] = displayX / 2 - displayX / 6;
//					Globals.ScreenKbControlsLayout[currentButton][2] = displayX / 2 + displayX / 6;
//					Globals.ScreenKbControlsLayout[currentButton][1] = displayY / 2 - displayY / 4;
//					Globals.ScreenKbControlsLayout[currentButton][3] = displayY / 2 + displayY / 4;
//				}
//				Matrix m = new Matrix();
//				RectF src = new RectF(0, 0, bmps[currentButton].getWidth(), bmps[currentButton].getHeight());
//				RectF dst = new RectF(Globals.ScreenKbControlsLayout[currentButton][0], Globals.ScreenKbControlsLayout[currentButton][1],
//										Globals.ScreenKbControlsLayout[currentButton][2], Globals.ScreenKbControlsLayout[currentButton][3]);
//				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//				imgs[currentButton].setImageMatrix(m);
//				m = new Matrix();
//				src = new RectF(0, 0, boundaryBmp.getWidth(), boundaryBmp.getHeight());
//				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//				boundary.setImageMatrix(m);
//				String buttonText = (currentButton == 0 ? "DPAD" : ( currentButton == 1 ? "Text input" : "" ));
//				if ( currentButton >= 2 && currentButton - 2 < Globals.AppTouchscreenKeyboardKeysNames.length )
//					buttonText = Globals.AppTouchscreenKeyboardKeysNames[currentButton - 2];
//				p.setText(p.getResources().getString(R.string.screenkb_custom_layout_help) + "\n" + buttonText.replace("_", " "));
//			}
//
//			public void onTouchEvent(final MotionEvent ev)
//			{
//				if(currentButton >= Globals.ScreenKbControlsLayout.length)
//				{
//					setupButton(false);
//					return;
//				}
//				if( ev.getAction() == MotionEvent.ACTION_DOWN )
//				{
//					Globals.ScreenKbControlsLayout[currentButton][0] = (int)ev.getX();
//					Globals.ScreenKbControlsLayout[currentButton][1] = (int)ev.getY();
//					Globals.ScreenKbControlsLayout[currentButton][2] = (int)ev.getX();
//					Globals.ScreenKbControlsLayout[currentButton][3] = (int)ev.getY();
//				}
//				if( ev.getAction() == MotionEvent.ACTION_MOVE )
//				{
//					if( Globals.ScreenKbControlsLayout[currentButton][0] > (int)ev.getX() )
//						Globals.ScreenKbControlsLayout[currentButton][0] = (int)ev.getX();
//					if( Globals.ScreenKbControlsLayout[currentButton][1] > (int)ev.getY() )
//						Globals.ScreenKbControlsLayout[currentButton][1] = (int)ev.getY();
//					if( Globals.ScreenKbControlsLayout[currentButton][2] < (int)ev.getX() )
//						Globals.ScreenKbControlsLayout[currentButton][2] = (int)ev.getX();
//					if( Globals.ScreenKbControlsLayout[currentButton][3] < (int)ev.getY() )
//						Globals.ScreenKbControlsLayout[currentButton][3] = (int)ev.getY();
//				}
//
//				Matrix m = new Matrix();
//				RectF src = new RectF(0, 0, bmps[currentButton].getWidth(), bmps[currentButton].getHeight());
//				RectF dst = new RectF(Globals.ScreenKbControlsLayout[currentButton][0], Globals.ScreenKbControlsLayout[currentButton][1],
//										Globals.ScreenKbControlsLayout[currentButton][2], Globals.ScreenKbControlsLayout[currentButton][3]);
//				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//				imgs[currentButton].setImageMatrix(m);
//				m = new Matrix();
//				src = new RectF(0, 0, boundaryBmp.getWidth(), boundaryBmp.getHeight());
//				m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//				boundary.setImageMatrix(m);
//
//				if( ev.getAction() == MotionEvent.ACTION_UP )
//					setupButton(false);
//			}
//
//			public void onKeyEvent(final int keyCode)
//			{
//				if( layout != null && imgs[currentButton] != null )
//					layout.removeView(imgs[currentButton]);
//				imgs[currentButton] = null;
//				setupButton(true);
//			}
//		}
//	}

	static class VideoSettingsConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.video);
		}
		//boolean enabled() { return true; };
		void run (final MainActivity p)
		{
			CharSequence[] items = {
				p.getResources().getString(R.string.pointandclick_keepaspectratio),
				p.getResources().getString(R.string.video_smooth)
			};
			boolean defaults[] = { 
				Globals.KeepAspectRatio,
				Globals.VideoLinearFilter
			};

			if(Globals.SwVideoMode && !Globals.CompatibilityHacksVideo)
			{
				CharSequence[] items2 = {
					p.getResources().getString(R.string.pointandclick_keepaspectratio),
					p.getResources().getString(R.string.video_smooth),
					p.getResources().getString(R.string.video_separatethread),
				};
				boolean defaults2[] = { 
					Globals.KeepAspectRatio,
					Globals.VideoLinearFilter,
					Globals.MultiThreadedVideo
				};
				items = items2;
				defaults = defaults2;
			}

			if(Globals.Using_SDL_1_3)
			{
				CharSequence[] items2 = {
					p.getResources().getString(R.string.pointandclick_keepaspectratio),
				};
				boolean defaults2[] = { 
					Globals.KeepAspectRatio,
				};
				items = items2;
				defaults = defaults2;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.video));
			builder.setMultiChoiceItems(items, defaults, new DialogInterface.OnMultiChoiceClickListener() 
			{
				public void onClick(DialogInterface dialog, int item, boolean isChecked) 
				{
					if( item == 0 )
						Globals.KeepAspectRatio = isChecked;
					if( item == 1 )
						Globals.VideoLinearFilter = isChecked;
					if( item == 2 )
						Globals.MultiThreadedVideo = isChecked;
				}
			});
			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

	static class ShowReadme extends Menu
	{
		String title(final MainActivity p)
		{
			return "Readme";
		}
		boolean enabled()
		{
			return true;
		}
		void run (final MainActivity p)
		{
			String readmes[] = Globals.ReadmeText.split("\\^");
			String lang = new String(Locale.getDefault().getLanguage()) + ":";
			String readme = readmes[0];
			for( String r: readmes )
			{
				if( r.startsWith(lang) )
					readme = r.substring(lang.length());
			}
			TextView text = new TextView(p);
			text.setMaxLines(1000);
			text.setText(readme);
			text.setLayoutParams(new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT));
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			ScrollView scroll = new ScrollView(p);
			scroll.addView(text);
			Button ok = new Button(p);
			final AlertDialog alertDismiss[] = new AlertDialog[1];
			ok.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					alertDismiss[0].cancel();
				}
			});
			ok.setText(R.string.ok);
			LinearLayout layout = new LinearLayout(p);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(scroll);
			layout.addView(ok);
			builder.setView(layout);
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alertDismiss[0] = alert;
			alert.setOwnerActivity(p);
			alert.show();
		}
	}

//	static class GyroscopeCalibration extends Menu implements SensorEventListener
//	{
//		String title(final MainActivity p)
//		{
//			return p.getResources().getString(R.string.calibrate_gyroscope);
//		}
//		boolean enabled()
//		{
//			return Globals.AppUsesGyroscope;
//		}
//		void run (final MainActivity p)
//		{
//			if( !Globals.AppUsesGyroscope || !AccelerometerReader.gyro.available(p) )
//			{
//				Toast toast = Toast.makeText(p, p.getResources().getString(R.string.calibrate_gyroscope_not_supported), Toast.LENGTH_LONG);
//				toast.show();
//				goBack(p);
//				return;
//			}
//			AlertDialog.Builder builder = new AlertDialog.Builder(p);
//			builder.setTitle(p.getResources().getString(R.string.calibrate_gyroscope));
//			builder.setMessage(p.getResources().getString(R.string.calibrate_gyroscope_text));
//			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
//			{
//				public void onClick(DialogInterface dialog, int item) 
//				{
//					dialog.dismiss();
//					startCalibration(p);
//				}
//			});
//			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
//			{
//				public void onCancel(DialogInterface dialog)
//				{
//					goBack(p);
//				}
//			});
//			AlertDialog alert = builder.create();
//			alert.setOwnerActivity(p);
//			alert.show();
//		}
//
//		ImageView img;
//		Bitmap bmp;
//		int numEvents;
//		MainActivity p;
//
//		void startCalibration(final MainActivity _p)
//		{
//			p = _p;
//			img = new ImageView(p);
//			img.setLayoutParams(new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
//			img.setScaleType(ImageView.ScaleType.MATRIX);
//			bmp = BitmapFactory.decodeResource( p.getResources(), R.drawable.calibrate );
//			img.setImageBitmap(bmp);
//			Matrix m = new Matrix();
//			RectF src = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
//			RectF dst = new RectF(	p.getVideoLayout().getWidth()/2 - 50, p.getVideoLayout().getHeight()/2 - 50,
//									p.getVideoLayout().getWidth()/2 + 50, p.getVideoLayout().getHeight()/2 + 50);
//			m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//			img.setImageMatrix(m);
//			p.getVideoLayout().addView(img);
//			numEvents = 0;
//			AccelerometerReader.gyro.x1 = 100;
//			AccelerometerReader.gyro.x2 = -100;
//			AccelerometerReader.gyro.xc = 0;
//			AccelerometerReader.gyro.y1 = 100;
//			AccelerometerReader.gyro.y2 = -100;
//			AccelerometerReader.gyro.yc = 0;
//			AccelerometerReader.gyro.z1 = 100;
//			AccelerometerReader.gyro.z2 = -100;
//			AccelerometerReader.gyro.zc = 0;
//			AccelerometerReader.gyro.registerListener(p, this);
//			(new Thread(new Runnable()
//			{
//				public void run()
//				{
//					for(int count = 1; count < 10; count++)
//					{
//						p.setText("" + count + "0% ...");
//						try {
//							Thread.sleep(500);
//						} catch( Exception e ) {}
//					}
//					finishCalibration(p);
//				}
//			}
//			)).start();
//		}
//
//		public void onSensorChanged(SensorEvent event)
//		{
//			gyroscopeEvent(event.values[0], event.values[1], event.values[2]);
//		}
//		public void onAccuracyChanged(Sensor s, int a)
//		{
//		}
//		void gyroscopeEvent(float x, float y, float z)
//		{
//			numEvents++;
//			AccelerometerReader.gyro.xc += x;
//			AccelerometerReader.gyro.yc += y;
//			AccelerometerReader.gyro.zc += z;
//			AccelerometerReader.gyro.x1 = Math.min(AccelerometerReader.gyro.x1, x * 1.1f); // Small safety bound coefficient
//			AccelerometerReader.gyro.x2 = Math.max(AccelerometerReader.gyro.x2, x * 1.1f);
//			AccelerometerReader.gyro.y1 = Math.min(AccelerometerReader.gyro.y1, y * 1.1f);
//			AccelerometerReader.gyro.y2 = Math.max(AccelerometerReader.gyro.y2, y * 1.1f);
//			AccelerometerReader.gyro.z1 = Math.min(AccelerometerReader.gyro.z1, z * 1.1f);
//			AccelerometerReader.gyro.z2 = Math.max(AccelerometerReader.gyro.z2, z * 1.1f);
//			final Matrix m = new Matrix();
//			RectF src = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
//			RectF dst = new RectF(	x * 5000 + p.getVideoLayout().getWidth()/2 - 50, y * 5000 + p.getVideoLayout().getHeight()/2 - 50,
//									x * 5000 + p.getVideoLayout().getWidth()/2 + 50, y * 5000 + p.getVideoLayout().getHeight()/2 + 50);
//			m.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
//			p.runOnUiThread(new Runnable()
//			{
//				public void run()
//				{
//					img.setImageMatrix(m);
//				}
//			});
//		}
//		void finishCalibration(final MainActivity p)
//		{
//			AccelerometerReader.gyro.unregisterListener(p, this);
//			try {
//				Thread.sleep(200); // Just in case we have pending events
//			} catch( Exception e ) {}
//			if( numEvents > 5 )
//			{
//				AccelerometerReader.gyro.xc /= (float)numEvents;
//				AccelerometerReader.gyro.yc /= (float)numEvents;
//				AccelerometerReader.gyro.zc /= (float)numEvents;
//			}
//			p.runOnUiThread(new Runnable()
//			{
//				public void run()
//				{
//					p.getVideoLayout().removeView(img);
//					goBack(p);
//				}
//			});
//		}
//	}

	static class ResetToDefaultsConfig extends Menu
	{
		String title(final MainActivity p)
		{
			return p.getResources().getString(R.string.reset_config);
		}
		boolean enabled()
		{
			return true;
		}
		void run (final MainActivity p)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.reset_config_ask));
			builder.setMessage(p.getResources().getString(R.string.reset_config_ask));
			
			builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					DeleteSdlConfigOnUpgradeAndRestart(p); // Never returns
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setNegativeButton(p.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					dialog.dismiss();
					goBack(p);
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					goBack(p);
				}
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}


	// ===============================================================================================

	public static boolean deleteRecursively(File dir)
	{
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteRecursively(new File(dir, children[i]));
				if (!success)
					return false;
			}
		}
		return dir.delete();
	}
	public static void DeleteFilesOnUpgrade()
	{
		String [] files = Globals.DeleteFilesOnUpgrade.split(" ");
		for(String path: files) {
			if( path.equals("") )
				continue;
			File f = new File( Globals.DataDir + "/" + path );
			if( !f.exists() )
				continue;
			deleteRecursively(f);
		}
	}
	@SuppressLint("WorldReadableFiles")
	public static void DeleteSdlConfigOnUpgradeAndRestart(final MainActivity p)
	{
		try {
			ObjectOutputStream out = new ObjectOutputStream(p.openFileOutput( SettingsFileName, MainActivity.MODE_WORLD_READABLE ));
			out.writeInt(-1);
			out.close();
		} catch( FileNotFoundException e ) {
		} catch ( IOException e ) { }
		new File( p.getFilesDir() + "/" + SettingsFileName ).delete();
		PendingIntent intent = PendingIntent.getActivity(p, 0, new Intent(p.getIntent()), p.getIntent().getFlags());
		AlarmManager mgr = (AlarmManager) p.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, intent);
		System.exit(0);
	}


	// ===============================================================================================

	static void Apply(Activity p)
	{
		nativeSetVideoDepth(Globals.VideoDepthBpp, Globals.NeedGles2 ? 1 : 0);
		if(Globals.VideoLinearFilter)
			nativeSetVideoLinearFilter();
		if( Globals.CompatibilityHacksVideo )
		{
			Globals.MultiThreadedVideo = true;
			Globals.SwVideoMode = true;
			nativeSetCompatibilityHacks();
		}
		if( Globals.SwVideoMode )
			nativeSetVideoForceSoftwareMode();
		if( Globals.SwVideoMode && Globals.MultiThreadedVideo )
			nativeSetVideoMultithreaded();
		if( Globals.PhoneHasTrackball )
			nativeSetTrackballUsed();
		if( Globals.AppUsesMouse )
			nativeSetMouseUsed( Globals.RightClickMethod,
								Globals.ShowScreenUnderFinger,
								Globals.LeftClickMethod,
								Globals.MoveMouseWithJoystick ? 1 : 0,
								Globals.ClickMouseWithDpad ? 1 : 0,
								Globals.ClickScreenPressure,
								Globals.ClickScreenTouchspotSize,
								Globals.MoveMouseWithJoystickSpeed,
								Globals.MoveMouseWithJoystickAccel,
								Globals.LeftClickKey,
								Globals.RightClickKey,
								Globals.LeftClickTimeout,
								Globals.RightClickTimeout,
								Globals.RelativeMouseMovement ? 1 : 0,
								Globals.RelativeMouseMovementSpeed,
								Globals.RelativeMouseMovementAccel,
								Globals.ShowMouseCursor ? 1 : 0 );
		if( Globals.AppUsesJoystick && (Globals.UseAccelerometerAsArrowKeys || Globals.UseTouchscreenKeyboard) )
			nativeSetJoystickUsed();
		if( Globals.AppUsesAccelerometer )
			nativeSetAccelerometerUsed();
		if( Globals.AppUsesMultitouch )
			nativeSetMultitouchUsed();
		nativeSetAccelerometerSettings(Globals.AccelerometerSensitivity, Globals.AccelerometerCenterPos);
		nativeSetTrackballDampening(Globals.TrackballDampening);
		if( Globals.UseTouchscreenKeyboard )
		{
			nativeSetTouchscreenKeyboardUsed();
			nativeSetupScreenKeyboard(	Globals.apOskPosition,
										Globals.apOskKeyMask,
										Globals.apOskDrawSize,
										42, // unused
										Globals.apOskTransparency,
										Globals.apOskSize,
										Globals.kbPos);
			SetupTouchscreenKeyboardGraphics(p);
/*
			boolean screenKbReallyUsed = false;
			for( int i = 0; i < Globals.ScreenKbControlsShown.length; i++ )
				if( Globals.ScreenKbControlsShown[i] )
					screenKbReallyUsed = true;
			if( screenKbReallyUsed && Globals.kbPos != Globals.KB_POS_HIDDEN )
			{
				nativeSetTouchscreenKeyboardUsed();
				nativeSetupScreenKeyboard(	Globals.apOskPosition,
											Globals.apOskKeyMask,
											Globals.apOskDrawSize,
											42, // unused
											Globals.apOskTransparency,
											Globals.apOskSize,
											Globals.kbPos);
				SetupTouchscreenKeyboardGraphics(p);
				for( int i = 0; i < Globals.ScreenKbControlsShown.length; i++ )
					nativeSetScreenKbKeyUsed(i, Globals.ScreenKbControlsShown[i] ? 1 : 0);
				for( int i = 0; i < Globals.RemapScreenKbKeycode.length; i++ )
					nativeSetKeymapKeyScreenKb(i, SDL_Keys.values[Globals.RemapScreenKbKeycode[i]]);
				for( int i = 0; i < Globals.ScreenKbControlsLayout.length; i++ )
					if( Globals.ScreenKbControlsLayout[i][0] < Globals.ScreenKbControlsLayout[i][2] )
						nativeSetScreenKbKeyLayout( i, Globals.ScreenKbControlsLayout[i][0], Globals.ScreenKbControlsLayout[i][1],
							Globals.ScreenKbControlsLayout[i][2], Globals.ScreenKbControlsLayout[i][3]);
			}
			else
				Globals.UseTouchscreenKeyboard = false;
*/		}

		for( int i = 0; i < SDL_Keys.JAVA_KEYCODE_LAST; i++ )
			nativeSetKeymapKey(i, SDL_Keys.values[Globals.RemapHwKeycode[i]]);
		for( int i = 0; i < Globals.RemapMultitouchGestureKeycode.length; i++ )
			nativeSetKeymapKeyMultitouchGesture(i, Globals.MultitouchGesturesUsed[i] ? SDL_Keys.values[Globals.RemapMultitouchGestureKeycode[i]] : 0);
		nativeSetMultitouchGestureSensitivity(Globals.MultitouchGestureSensitivity);
		if( Globals.TouchscreenCalibration[2] > Globals.TouchscreenCalibration[0] )
			nativeSetTouchscreenCalibration(Globals.TouchscreenCalibration[0], Globals.TouchscreenCalibration[1],
				Globals.TouchscreenCalibration[2], Globals.TouchscreenCalibration[3]);

		try {
			String lang = new String(Locale.getDefault().getLanguage());
			if( Locale.getDefault().getCountry().length() > 0 )
				lang = lang + "_" + Locale.getDefault().getCountry();
			Logger.log( "libSDL: setting envvar LANGUAGE to '" + lang + "'");
			nativeSetEnv( "LANG", lang );
			nativeSetEnv( "LANGUAGE", lang );
			// TODO: get current user name and set envvar USER, the API is not availalbe on Android 1.6 so I don't bother with this
			nativeSetEnv( "APPDIR", p.getFilesDir().getAbsolutePath() );
			nativeSetEnv( "SECURE_STORAGE_DIR", p.getFilesDir().getAbsolutePath() );
			nativeSetEnv( "DATADIR", Globals.DataDir );
			nativeSetEnv( "UNSECURE_STORAGE_DIR", Globals.DataDir );
			nativeSetEnv( "HOME", Globals.DataDir );
			nativeSetEnv( "ANDROID_VERSION", String.valueOf(android.os.Build.VERSION.SDK_INT) );

			DisplayMetrics dm = new DisplayMetrics();
			p.getWindowManager().getDefaultDisplay().getMetrics(dm);
			float xx = dm.widthPixels/dm.xdpi;
			float yy = dm.heightPixels/dm.ydpi;
			float x = Math.max(xx, yy);
			float y = Math.min(xx, yy);
			float displayInches = (float)Math.sqrt( x*x + y*y );
			nativeSetEnv( "DISPLAY_SIZE", String.valueOf(displayInches) );
			nativeSetEnv( "DISPLAY_SIZE_MM", String.valueOf((int)(displayInches*25.4f)) );
			nativeSetEnv( "DISPLAY_WIDTH", String.valueOf(x) );
			nativeSetEnv( "DISPLAY_HEIGHT", String.valueOf(y) );
			nativeSetEnv( "DISPLAY_WIDTH_MM", String.valueOf((int)(x*25.4f)) );
			nativeSetEnv( "DISPLAY_HEIGHT_MM", String.valueOf((int)(y*25.4f)) );
			nativeSetEnv( "DISPLAY_RESOLUTION_WIDTH", String.valueOf(Math.max(dm.widthPixels, dm.heightPixels)) );
			nativeSetEnv( "DISPLAY_RESOLUTION_HEIGHT", String.valueOf(Math.min(dm.widthPixels, dm.heightPixels)) );
		} catch (Exception e) {
			Logger.log("nativeSetEnv failed", e);
		}
	}

	static byte [] loadRaw(Activity p, int res) {
		byte buf[] = new byte[4096];

		ByteArrayOutputStream os = new ByteArrayOutputStream(65536 * 50);
		InputStream is = null;
		try {
			is = new GZIPInputStream(p.getResources().openRawResource(res));

			int readed;
			while( (readed = is.read(buf)) > 0 ) {
				os.write(buf, 0, readed);
			}
			os.flush();
		} catch (IOException e) {
			Logger.log("Fallito cariamento file raw", e);
			return new byte[0];
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}

		buf = os.toByteArray();
		try {
			os.close();
		} catch (IOException e) {
		}
		return buf;
	}

	static void SetupTouchscreenKeyboardGraphics(Activity p)
	{
		if( Globals.UseTouchscreenKeyboard )
		{
/*			if(Globals.TouchscreenKeyboardTheme < 0)
				Globals.TouchscreenKeyboardTheme = 0;
			if(Globals.TouchscreenKeyboardTheme > 2)
				Globals.TouchscreenKeyboardTheme = 2;

			if( Globals.TouchscreenKeyboardTheme == 0 )
			{
				nativeSetupScreenKeyboardButtons(loadRaw(p, R.raw.ultimatedroid));
			}
			if( Globals.TouchscreenKeyboardTheme == 1 )
			{
				nativeSetupScreenKeyboardButtons(loadRaw(p, R.raw.simpletheme));
			}
			if( Globals.TouchscreenKeyboardTheme == 2 )
			{
				nativeSetupScreenKeyboardButtons(loadRaw(p, R.raw.sun));
			} */


			nativeSetupScreenKeyboardSingleButton(0, loadRaw(p, R.raw.ap0));
//			nativeSetupScreenKeyboardSingleButton(1, loadRaw(p, R.raw.ap1));
//			nativeSetupScreenKeyboardSingleButton(2, loadRaw(p, R.raw.ap2));
//			nativeSetupScreenKeyboardSingleButton(3, loadRaw(p, R.raw.ap3));
//			nativeSetupScreenKeyboardSingleButton(4, loadRaw(p, R.raw.ap4));
			nativeSetupScreenKeyboardSingleButton(5, loadRaw(p, R.raw.ap5));
			nativeSetupScreenKeyboardSingleButton(6, loadRaw(p, R.raw.ap6));
			nativeSetupScreenKeyboardSingleButton(7, loadRaw(p, R.raw.ap7));
//			nativeSetupScreenKeyboardSingleButton(8, loadRaw(p, R.raw.ap8));
			nativeSetupScreenKeyboardSingleButton(9, loadRaw(p, R.raw.ap9));
			nativeSetupScreenKeyboardSingleButton(10, loadRaw(p, R.raw.ap10));
			nativeSetupScreenKeyboardSingleButton(11, loadRaw(p, R.raw.ap11));
//			nativeSetupScreenKeyboardSingleButton(12, loadRaw(p, R.raw.ap12));
//			nativeSetupScreenKeyboardSingleButton(13, loadRaw(p, R.raw.ap13));
//			nativeSetupScreenKeyboardSingleButton(14, loadRaw(p, R.raw.ap14));
			nativeSetupScreenKeyboardSingleButton(15, loadRaw(p, R.raw.ap15));
			nativeSetupScreenKeyboardSingleButton(16, loadRaw(p, R.raw.ap16));
			nativeSetupScreenKeyboardSingleButton(17, loadRaw(p, R.raw.ap17));
			nativeSetupScreenKeyboardSingleButton(18, loadRaw(p, R.raw.ap18));
			nativeSetupScreenKeyboardSingleButton(19, loadRaw(p, R.raw.ap19));
			nativeSetupScreenKeyboardSingleButton(20, loadRaw(p, R.raw.ap20));
			nativeSetupScreenKeyboardSingleButton(21, loadRaw(p, R.raw.ap21));
		}
	}

	abstract static class SdcardAppPath
	{
		private static SdcardAppPath get()
		{
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
				return Froyo.Holder.sInstance;
			else
				return Dummy.Holder.sInstance;
		}
		public abstract String path(final Context p);
		public static String deprecatedPath(final Context p)
		{
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "/app-data/" + p.getPackageName();
		}
		public static String getPath(final Context p)
		{
			try {
				return get().path(p);
			} catch(Exception e) { }
			return Dummy.Holder.sInstance.path(p);
		}

		private static class Froyo extends SdcardAppPath
		{
			private static class Holder
			{
				private static final Froyo sInstance = new Froyo();
			}
			public String path(final Context p)
			{
				return p.getExternalFilesDir(null).getAbsolutePath();
			}
		}
		private static class Dummy extends SdcardAppPath
		{
			private static class Holder
			{
				private static final Dummy sInstance = new Dummy();
			}
			public String path(final Context p)
			{
				return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + p.getPackageName() + "/files";
			}
		}
	}
	
	static boolean checkRamSize(final MainActivity p) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("/proc/meminfo"));
			String line = null;
			while( ( line = reader.readLine() ) != null )
			{
				if( line.indexOf("MemTotal:") == 0 )
				{
					String[] fields = line.split("[ \t]+");
					Long size = Long.parseLong(fields[1]);
					Logger.log("Device RAM size: " + size / 1024 + " Mb, required minimum RAM: " + Globals.AppMinimumRAM + " Mb" );
					if( size / 1024 < Globals.AppMinimumRAM )
					{
						settingsChanged = true;
						AlertDialog.Builder builder = new AlertDialog.Builder(p);
						builder.setTitle(R.string.not_enough_ram);
						builder.setMessage(p.getResources().getString( R.string.not_enough_ram_size, Globals.AppMinimumRAM, (int)(size / 1024)) );
						builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int item)
							{
								p.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + p.getPackageName())));
								System.exit(0);
							}
						});
						builder.setNegativeButton(p.getResources().getString(R.string.ignore), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int item)
							{
								showConfig(p, true);
								return;
							}
						});
						builder.setOnCancelListener(new DialogInterface.OnCancelListener()
						{
							public void onCancel(DialogInterface dialog)
							{
								p.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + p.getPackageName())));
								System.exit(0);
							}
						});
						final AlertDialog alert = builder.create();
						alert.setOwnerActivity(p);
						alert.show();
						return false;
					}
				}
			}
		} catch ( Exception e ) {
			Logger.log("Error: cannot parse /proc/meminfo: " + e.toString());
		} finally {
			try {
				reader.close();
			} catch (IOException e) { }
		}
		return true;
	}
	
	private static native void nativeSetTrackballUsed();
	private static native void nativeSetTrackballDampening(int value);
	private static native void nativeSetAccelerometerSettings(int sensitivity, int centerPos);
	private static native void nativeSetMouseUsed(int RightClickMethod, int ShowScreenUnderFinger, int LeftClickMethod, 
													int MoveMouseWithJoystick, int ClickMouseWithDpad, int MaxForce, int MaxRadius,
													int MoveMouseWithJoystickSpeed, int MoveMouseWithJoystickAccel,
													int leftClickKeycode, int rightClickKeycode,
													int leftClickTimeout, int rightClickTimeout,
													int relativeMovement, int relativeMovementSpeed,
													int relativeMovementAccel, int showMouseCursor);
	private static native void nativeSetJoystickUsed();
	private static native void nativeSetAccelerometerUsed();
	private static native void nativeSetMultitouchUsed();
	private static native void nativeSetTouchscreenKeyboardUsed();
	private static native void nativeSetVideoLinearFilter();
	private static native void nativeSetVideoDepth(int bpp, int gles2);
	private static native void nativeSetCompatibilityHacks();
	private static native void nativeSetVideoMultithreaded();
	private static native void nativeSetVideoForceSoftwareMode();
	private static native void nativeSetupScreenKeyboard(int size, int drawsize, int theme, int nbuttonsAutoFire, int transparency, int n1, int n2);
	public static native void nativeSetScreenKbShown(int value);
	private static native void nativeSetupScreenKeyboardSingleButton(int index, byte[] img);
	private static native void nativeSetupScreenKeyboardButtons(byte[] img);
	private static native void nativeInitKeymap();
	private static native int  nativeGetKeymapKey(int key);
	private static native void nativeSetKeymapKey(int javakey, int key);
	private static native int  nativeGetKeymapKeyScreenKb(int keynum);
	private static native void nativeSetKeymapKeyScreenKb(int keynum, int key);
	private static native void nativeSetScreenKbKeyUsed(int keynum, int used);
	private static native void nativeSetScreenKbKeyLayout(int keynum, int x1, int y1, int x2, int y2);
	private static native int  nativeGetKeymapKeyMultitouchGesture(int keynum);
	private static native void nativeSetKeymapKeyMultitouchGesture(int keynum, int key);
	private static native void nativeSetMultitouchGestureSensitivity(int sensitivity);
	private static native void nativeSetTouchscreenCalibration(int x1, int y1, int x2, int y2);
	public static native void  nativeSetEnv(final String name, final String value);
	public static native int   nativeChmod(final String name, int mode);
}

