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
import it.ap.wesnoth.ReleaseManager.Major;
import it.ap.wesnoth.ScreenResUtil.Mode;
import it.ap.wesnoth.ScreenResUtil.Resolution;
import it.ap.wesnoth.ShortcutActivity.Action;

import java.io.File;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;

public class Globals
{
	// These config options are modified by ChangeAppsettings.sh script - see the detailed descriptions there
	public static String ApplicationName = "Wesnoth";
	public static String AppLibraries[] = { "gnustl_shared", "sdl-1.2", "bfw_pixman", "ffi", "bfw_expat", "freetype", "fontconfig", "bfw_png", "cairo", "intl", "sdl_sound", "sdl_net", "sdl_mixer", "sdl_ttf", "sdl_image", "bfw_glib", "pango" };
	public static final boolean Using_SDL_1_3 = false;

	public static boolean OptionalDataDownload[] = null;

	public static int VideoDepthBpp = 16;
	public static boolean SwVideoMode = true;
	public static boolean NeedDepthBuffer = false;
	public static boolean NeedStencilBuffer = false;
	public static boolean NeedGles2 = false;
	public static boolean CompatibilityHacksVideo = false;
	public static boolean CompatibilityHacksStaticInit = false;
	public static boolean CompatibilityHacksTextInputEmulatesHwKeyboard = true;
	public static boolean KeepAspectRatioDefaultSetting = false;
	public static boolean InhibitSuspend = true;
	public static String ReadmeText = "^Readme text";
	public static boolean AppUsesMouse = true;
	public static boolean AppNeedsTwoButtonMouse = true;
	public static boolean ForceRelativeMouseMode = false; // If both on-screen keyboard and mouse are needed, this will only set the default setting, user may override it later
	public static boolean ShowMouseCursor = false;
	public static boolean AppNeedsArrowKeys = false;
	public static boolean AppNeedsTextInput = true;
	public static boolean AppUsesJoystick = false;
	public static boolean AppUsesAccelerometer = false;
	public static boolean AppUsesGyroscope = false;
	public static boolean AppUsesMultitouch = false;
	public static boolean NonBlockingSwapBuffers = false;
	public static boolean ResetSdlConfigForThisVersion = false;
	public static String DeleteFilesOnUpgrade = "%";
	public static int AppTouchscreenKeyboardKeysAmount = 0;
	public static int AppTouchscreenKeyboardKeysAmountAutoFire = 0;
	public static String[] AppTouchscreenKeyboardKeysNames = "0 1 2 3 4 5 6 7 8 9".split(" ");
	public static int AppMinimumRAM = 0;
	public static Settings.Menu HiddenMenuOptions [] = { };
	public static Settings.Menu FirstStartMenuOptions [] = { new Settings.DownloadConfig(), new Settings.OptionalDownloadConfig(true) };
	public static String AdmobPublisherId = "";
	public static String AdmobTestDeviceId = "";
	public static String AdmobBannerSize = "";

	// Phone-specific config, modified by user in "Change phone config" startup dialog, TODO: move this to settings
	public static boolean DownloadToSdcard = true;
	public static boolean PhoneHasTrackball = true;
	public static boolean PhoneHasArrowKeys = false;
	public static boolean UseAccelerometerAsArrowKeys = false;
	public static boolean UseTouchscreenKeyboard = true;
	public static int TouchscreenKeyboardSize = 1;
	public static int TouchscreenKeyboardDrawSize = 1;
	public static int TouchscreenKeyboardTheme = 2;
	public static int TouchscreenKeyboardTransparency = 2;
	public static int AccelerometerSensitivity = 2;
	public static int AccelerometerCenterPos = 2;
	public static int TrackballDampening = 1;
	public static int AudioBufferConfig = 0;
	public static int LeftClickMethod = Mouse.LEFT_CLICK_NORMAL;
	public static int LeftClickKey = KeyEvent.KEYCODE_DPAD_CENTER;
	public static int LeftClickTimeout = 3;
	public static int RightClickTimeout = 4;
	public static int RightClickMethod = AppNeedsTwoButtonMouse ? Mouse.RIGHT_CLICK_WITH_MULTITOUCH : Mouse.RIGHT_CLICK_NONE;
	public static int RightClickKey = KeyEvent.KEYCODE_MENU;
	public static boolean MoveMouseWithJoystick = false;
	public static int MoveMouseWithJoystickSpeed = 0;
	public static int MoveMouseWithJoystickAccel = 0;
	public static boolean ClickMouseWithDpad = false;
	public static boolean RelativeMouseMovement = ForceRelativeMouseMode; // Laptop touchpad mode
	public static int RelativeMouseMovementSpeed = 2;
	public static int RelativeMouseMovementAccel = 0;
	public static int ShowScreenUnderFinger = Mouse.ZOOM_NONE;
	public static boolean KeepAspectRatio = KeepAspectRatioDefaultSetting;
	public static int ClickScreenPressure = 0;
	public static int ClickScreenTouchspotSize = 0;
	public static int RemapHwKeycode[] = new int[SDL_Keys.JAVA_KEYCODE_LAST];
	public static int RemapScreenKbKeycode[] = new int[6];
	public static boolean ScreenKbControlsShown[] = new boolean[8]; /* Also joystick and text input button added */
	public static int ScreenKbControlsLayout[][] = new int[8][4];
	public static int RemapMultitouchGestureKeycode[] = new int[4];
	public static boolean MultitouchGesturesUsed[] = new boolean[4];
	public static int MultitouchGestureSensitivity = 1;
	public static int TouchscreenCalibration[] = new int[4];
	public static String DataDir = "";
	public static boolean VideoLinearFilter = true;
	public static boolean MultiThreadedVideo = false;
	public static boolean BrokenLibCMessageShown = false;
	// Gyroscope calibration
	public static float gyro_x1, gyro_x2, gyro_xc, gyro_y1, gyro_y2, gyro_yc, gyro_z1, gyro_z2, gyro_zc;

	public static final int KB_POS_LEFT = 0;
	public static final int KB_POS_MID = 1;
	public static final int KB_POS_RIGHT = 2;
	public static final int KB_POS_HIDDEN = 3;
	public static int kbPos = 0;
	public static boolean showTutorial = false;
	public static boolean pointerFollowsFinger = false;
	public static float getAccelFloat() {
		switch(accel) {
		case 0:
			return 0.0f;
		case 1:
			return 0.5f;
		case 2:
			return 0.7f;
		case 3:
			return 0.9f;
		case 4:
			return 1.1f;
		case 5:
			return 1.3f;
		case 6:
			return 1.5f;
		}
		return 1.0f;
	}
	public static int accel = 3;

	public static boolean customRes = false;
	public static int customResW = 800;
	public static int customResH = 480;

	public static int tap2ndFingerButton = -1; // 1 - left, 2 - right, -1 - disabled

	public static boolean doFullClean = false;
	public static boolean sendDebugInfo = false;

	public static final int AP_OSK_POS_BL2RIGHT = 0;
	public static final int AP_OSK_POS_BL2TOP = 1;
	public static final int AP_OSK_POS_BR2LEFT = 2;
	public static final int AP_OSK_POS_BR2TOP = 3;
	public static int apOskPosition = AP_OSK_POS_BL2RIGHT;

	public static final int AP_OSK_SIZE_BIG = 0; // X
	public static final int AP_OSK_SIZE_NORMAL = 1; // X*2/3
	public static final int AP_OSK_SIZE_SMALL = 2; // X*1/2
	public static int apOskSize = AP_OSK_SIZE_BIG; // dimensione effettiva dell'area del bottone
	public static int apOskDrawSize = AP_OSK_SIZE_NORMAL; // ridimensionamento dell'immagine all'interno dell'area

	public static int apOskKeyMask = 1;

	public static final int AP_OSK_TRANSP_LIGHTER = 0;
	public static final int AP_OSK_TRANSP_LIGHT = 1;
	public static final int AP_OSK_TRANSP_MEDIUM = 2;
	public static final int AP_OSK_TRANSP_STRONG = 3;
	public static final int AP_OSK_TRANSP_STRONGER = 4;
	public static int apOskTransparency = AP_OSK_TRANSP_MEDIUM;

	public enum ScreenOrientation {
		LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, R.string.ap_screenorientation_landscape),
		REVERSE_LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, R.string.ap_screenorientation_reverse_landscape),
		PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, R.string.ap_screenorientation_portrait),
		REVERSE_PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, R.string.ap_screenorientation_reverse_portrait);

		private int cod;
		private int res;
		private ScreenOrientation(int cod, int res) {
			this.cod = cod;
			this.res = res;
		}
		public boolean isLandscape() {
			switch (this) {
			case LANDSCAPE:
			case REVERSE_LANDSCAPE:
				return true;
			case PORTRAIT:
			case REVERSE_PORTRAIT:
				return false;
			}
			return true;
		}
		public boolean enabled(Activity ctx) {
			switch (this) {
			case LANDSCAPE:
			case REVERSE_LANDSCAPE:
				return true;
			case PORTRAIT:
			case REVERSE_PORTRAIT:
				Logger.log("Calculating screen resolution in portrait mode...");
				Resolution res = ScreenResUtil.getRes(ctx, Mode.PORTRAIT);
				return (res.width >= 800 && res.height >= 480);
			}
			return false;
		}
		public void apply(Activity ctx) {
			ctx.setRequestedOrientation(cod);
		}

		public int getResId() {
			return res;
		}

		public int toInt() {
			return cod;
		}
		public static ScreenOrientation fromInt(int cod) {
			for (ScreenOrientation sc : values()) {
				if (sc.cod == cod) {
					return sc;
				}
			}
			return LANDSCAPE;
		}
	}
	public static ScreenOrientation screenOrientation = ScreenOrientation.LANDSCAPE;

	private static int startupMenuButtonTimeout = 3000;
	private static Action startupAction = null;

	public static int getStartupButtonTimeout() {
		return startupMenuButtonTimeout;
	}

	public static void setStartupAction(Action action) {
		startupAction = action;
		if (action != null) {
			startupMenuButtonTimeout = 0;
		}
	}

	public static String getPrefFolder() {
		return ".wesnoth" + ReleaseManager.getRunningWesnothRelease().getMajor().getVersionStr();
	}

	public static String getSavegameDir() {
		return Globals.DataDir + "/" + getPrefFolder() + "/saves";
	}

	private static String getBaseCommandLine() {
		if (ReleaseManager.getRunningWesnothRelease().getMajor() == Major.R_1_10) {
			return "wesnoth --smallgui";
		} else {
			return "wesnoth";
		}
	}

	public static String getCommandLine() {
		if (startupAction == null) {
			return getBaseCommandLine();
		}

		String extra = "";

		switch (startupAction) {
		case OPEN_LAST:
			long lastTs = 0L;
			File recent = null;
			File saves = new File(getSavegameDir());

			if (saves.isDirectory()) {
				for (File f : saves.listFiles()) { 
					String n = f.getName();

					if (n.contains(" ")) {
						continue;
					}
					if (n.contains("\n")) {
						continue;
					}
					if (n.contains("\r")) {
						continue;
					}
					if (n.contains("\t")) {
						continue;
					}

					long ts = f.lastModified();
					if (ts > lastTs) {
						lastTs = ts;
						recent = f;
					}
				}
			}

			if (recent != null) {
				extra = " --load " + recent.getName();
			}
			break;
		case CAMPAIGN:
			extra = " --campaign";
			break;
		case MAP_EDITOR:
			extra = " --editor";
			break;
		case SERVER:
			extra = " --server";
			break;
		}

		return getBaseCommandLine() + extra;
	}

	public static boolean sendStats = true;
}

