package it.ap.wesnoth;

public class Mouse {
	public static final int LEFT_CLICK_NORMAL = 0;
	public static final int LEFT_CLICK_NEAR_CURSOR = 1;
	public static final int LEFT_CLICK_WITH_MULTITOUCH = 2;
	public static final int LEFT_CLICK_WITH_PRESSURE = 3;
	public static final int LEFT_CLICK_WITH_KEY = 4;
	public static final int LEFT_CLICK_WITH_TIMEOUT = 5;
	public static final int LEFT_CLICK_WITH_TAP = 6;
	public static final int LEFT_CLICK_WITH_TAP_OR_TIMEOUT = 7;
	
	public static final int RIGHT_CLICK_NONE = 0;
	public static final int RIGHT_CLICK_WITH_MULTITOUCH = 1;
	public static final int RIGHT_CLICK_WITH_PRESSURE = 2;
	public static final int RIGHT_CLICK_WITH_KEY = 3;
	public static final int RIGHT_CLICK_WITH_TIMEOUT = 4;

	public static final int SDL_FINGER_DOWN = 0;
	public static final int SDL_FINGER_UP = 1;
	public static final int SDL_FINGER_MOVE = 2;
	public static final int SDL_FINGER_HOVER = 3;

	public static final int ZOOM_NONE = 0;
	public static final int ZOOM_MAGNIFIER = 1;
	public static final int ZOOM_SCREEN_TRANSFORM = 2;
	public static final int ZOOM_FULLSCREEN_MAGNIFIER = 3;
}


