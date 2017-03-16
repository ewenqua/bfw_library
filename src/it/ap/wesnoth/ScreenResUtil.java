package it.ap.wesnoth;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScreenResUtil {
	public static class Resolution {
		public int width;
		public int height;
		public boolean same(Resolution r) {
			return width == r.width && height == r.height;
		}
	}

	enum Mode {
		PORTRAIT, LANDSCAPE;
		public static Mode from(int w, int h) {
			for (Mode m : values()) {
				if (m.is(w, h)) {
					return m;
				}
			}
			return LANDSCAPE;
		}
		public Mode opposite() {
			switch (this) {
			case LANDSCAPE:
				return PORTRAIT;
			case PORTRAIT:
				return LANDSCAPE;
			}
			return LANDSCAPE;
		}
		public boolean is(int w, int h) {
			switch (this) {
			case LANDSCAPE:
				return h <= w;
			case PORTRAIT:
				return h > w;
			}
			return true;
		}
	}
	public static Resolution getRes(Activity ctx, Mode mode) {
		Resolution res = new Resolution();

		View content = ctx.getWindow().getDecorView();
		int vw = content.getWidth(); // 1280
		int vh = content.getHeight(); // 752

		res.width = vw;
		res.height = vh;

		if (!mode.is(vw, vh)) {
			res = rotate(ctx, res);
		}

		return res;
	}

	public static Resolution rotate(Activity ctx, Resolution src) {
		Resolution res = new Resolution();

		View content = ctx.getWindow().getDecorView();
		int vw = content.getWidth(); // 1280
		int vh = content.getHeight(); // 752

		Resolution screen = getFullScreenResolution(ctx, vw, vh);
		int w = screen.width; // 1280
		int h = screen.height; // 800

		int decorW = w - vw; // 0
		int decorH = h - vh; // 48

		Logger.log("Resolution.rotate(): vw=" + vw + ",vh=" + vh + ",sw=" + w + ",sh=" + h+",decW="+decorW+",decH="+decorH);

		res.width = src.height + decorH - decorW; // 800
		res.height = src.width + decorW - decorH; // 1232

		Logger.log("Resolution.rotate(): rotating "+src.width+"x"+src.height + " -> " +res.width+"x"+res.height);

		return res;
	}

	private static Resolution getFullScreenResolution(Context ctx, int defw, int defh) {
		Resolution r = new Resolution();
		r.width = defw;
		r.height = defh;

		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		try {
			DisplayMetrics metrics = new DisplayMetrics(); 
			Method getRealMetrics = Display.class.getMethod("getRealMetrics", DisplayMetrics.class);

			getRealMetrics.invoke(display, metrics);

			r.width = metrics.widthPixels;
			r.height = metrics.heightPixels;
			return r;
		} catch (IllegalArgumentException e) {
			Logger.log("getFullScreenResolution() try1 failed, IllegalArgumentException " + e.getMessage() + " (v: " + android.os.Build.VERSION.SDK_INT + ")");
		} catch (IllegalAccessException e) {
			Logger.log("getFullScreenResolution() try1 failed, IllegalAccessException " + e.getMessage() + " (v: " + android.os.Build.VERSION.SDK_INT + ")");
		} catch (NoSuchMethodException e) {
			Logger.log("getFullScreenResolution() try1 failed, NoSuchMethodException " + e.getMessage() + " (v: " + android.os.Build.VERSION.SDK_INT + ")");
		} catch (InvocationTargetException ex) {
			Throwable e = ex.getCause() == null ? ex : ex.getCause();
			Logger.log("getFullScreenResolution() try1 failed, InvocationTargetException " + e.getClass() + " " + e.getMessage() + " (v: " + android.os.Build.VERSION.SDK_INT + ")");
		}

		try {
			Method mGetRawW = Display.class.getMethod("getRawWidth");
			Method mGetRawH = Display.class.getMethod("getRawHeight");

			r.width = (Integer) mGetRawW.invoke(display);
			r.height = (Integer) mGetRawH.invoke(display);
			return r;
		} catch (IllegalArgumentException e) {
			Logger.log("getFullScreenResolution() try2 failed, IllegalArgumentException " + e.getMessage() + " (v: " + android.os.Build.VERSION.SDK_INT + ")");
		} catch (IllegalAccessException e) {
			Logger.log("getFullScreenResolution() try2 failed, IllegalAccessException " + e.getMessage() + " (v: " + android.os.Build.VERSION.SDK_INT + ")");
		} catch (NoSuchMethodException e) {
			Logger.log("getFullScreenResolution() try2 failed, NoSuchMethodException " + e.getMessage() + " (v: " + android.os.Build.VERSION.SDK_INT + ")");
		} catch (InvocationTargetException ex) {
			Throwable e = ex.getCause() == null ? ex : ex.getCause();
			Logger.log("getFullScreenResolution() try2 failed, InvocationTargetException " + e.getClass() + " " + e.getMessage() + " (v: " + android.os.Build.VERSION.SDK_INT + ")");
		}

		r.width = display.getWidth();
		r.height = display.getHeight();
		return r;
	}
}
