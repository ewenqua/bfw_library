package it.ap.wesnoth;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class ReleaseManager {
	public enum Major {
		R_1_10(0),
		R_1_11(1),
		R_1_12(2);

		private int index;

		private Major (int index) {
			this.index = index;
		}
		public int getVersionCode() {
			return index;
		}
		public String getVersionStr() {
			return name().replace("R_", "").replace('_', '.');
		}
	}
	public enum Minor {
		R_1_10_6(Major.R_1_10, 6),
		R_1_10_7(Major.R_1_10, 7),
		R_1_11_17(Major.R_1_11, 17),
		R_1_11_18(Major.R_1_11, 18),
		R_1_11_19(Major.R_1_11, 19),
		R_1_11_20(Major.R_1_11, 20),
		R_1_11_21(Major.R_1_11, 21),
		R_1_12_0(Major.R_1_12, 0),
		R_1_12_1(Major.R_1_12, 1),
		R_1_12_2(Major.R_1_12, 2),
		// 1.12.3 was never released
		//R_1_12_3(Major.R_1_12, 3),
		R_1_12_4(Major.R_1_12, 4),
		R_1_12_5(Major.R_1_12, 5),
		R_1_12_6(Major.R_1_12, 6),
		R_1_12_7(Major.R_1_12, 7),
		R_1_12_8(Major.R_1_12, 8),
		R_1_12_9(Major.R_1_12, 9),
		R_1_12_10(Major.R_1_12, 10),
		R_1_12_11(Major.R_1_12, 11);

		private Major major;
		private int index;

		private Minor(Major major, int index) {
			this.major = major;
			this.index = index;
		}

		public Major getMajor() {
			return major;
		}
		public int getVersionCode() {
			return major.getVersionCode() * 1000 + index;
		}
		public String getVersionStr() {
			return major.getVersionStr() + "." + index;
		}

		public Minor getPrevious() {
			Minor rv = null;
			for (Minor m : Minor.values()) {
				int c = m.getVersionCode();
				if (rv != null && rv.getVersionCode() > c) {
					continue;
				}
				if (c < getVersionCode()) {
					rv = m;
				}
			}
			return rv;
		}

		public Minor getNext() {
			Minor rv = null;
			for (Minor m : Minor.values()) {
				int c = m.getVersionCode();
				if (rv != null && rv.getVersionCode() < c) {
					continue;
				}
				if (c > getVersionCode()) {
					rv = m;
				}
			}
			return rv;
		}

		public static Minor fromStr(String ver) {
			for (Minor m : Minor.values()) {
				if (m.getVersionStr().equals(ver)) {
					return m;
				}
			}
			return null;
		}
		public static Minor fromCode(int code) {
			for (Minor m : Minor.values()) {
				if (m.getVersionCode() == code) {
					return m;
				}
			}
			return null;
		}
	}

	private static String APP_VERSION_STR;
	private static Minor APP_VERSION;

	public static void initialize(Context ctx) {
		try {
			PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			APP_VERSION_STR = pInfo.versionName;
			int idxSep = APP_VERSION_STR.indexOf('-');
			if (idxSep >= 0) {
				APP_VERSION_STR = APP_VERSION_STR.substring(0, idxSep);
			}
			APP_VERSION = Minor.fromStr(APP_VERSION_STR);
			DataDownload.initialize(APP_VERSION.getVersionCode());
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getMaxAvailWesnothFolder() {
		return ".wesnoth" + APP_VERSION.getMajor().getVersionStr();
	}
	public static Minor getMaxAvailWesnothRelease() {
		return APP_VERSION;
	}

	public static String getRunningWesnothFolder() {
		return getMaxAvailWesnothFolder();
	}
	public static Minor getRunningWesnothRelease() {
		return getMaxAvailWesnothRelease();
	}
}
