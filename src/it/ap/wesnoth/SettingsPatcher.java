package it.ap.wesnoth;

import it.ap.wesnoth.ScreenResUtil.Mode;
import it.ap.wesnoth.ScreenResUtil.Resolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;

public class SettingsPatcher {
	private static final String X_PATCH = "xwindowsize=\"%d\"";
	private static final String Y_PATCH = "ywindowsize=\"%d\"";

	private boolean xPatched, yPatched;

	private static interface LineParser {
		void parseLine(String line);
	}

	private String patchLine(String str, int w, int h) {
		if (str.startsWith("xwindowsize=")) {
			xPatched = true;
			return String.format(Locale.US, X_PATCH, w);
		}
		if (str.startsWith("ywindowsize=")) {
			yPatched = true;
			return String.format(Locale.US, Y_PATCH, h);
		}
		return str;
	}

	private void readLines(File f, LineParser parser) throws IOException {
		BufferedReader input = null;

		try {
			input = new BufferedReader(new FileReader(f));
			String text;
			while ((text = input.readLine()) != null) {
				parser.parseLine(text);
			}
		} catch (FileNotFoundException e) {
			Logger.log("SettingsPatcher", "File '" + f.getAbsolutePath() + "' not found");
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				Logger.log("SettingsPatcher", "Error closing file '" + f.getAbsolutePath() + "' after read", e);
			}
		}
	}

	private String getPatchedBuffer(File f, final int w, final int h) throws IOException {
		xPatched = yPatched = false;

		final StringBuffer buffer = new StringBuffer();
		readLines(f, new LineParser() {
			@Override
			public void parseLine(String line) {
				line = patchLine(line, w, h);
				buffer.append(line + "\n");
			}
		});

		if (!yPatched) {
			buffer.insert(0, String.format(Y_PATCH, h) + "\n");
		}
		if (!xPatched) {
			buffer.insert(0, String.format(X_PATCH, w) + "\n");
		}

		return buffer.toString();
	}

	private File getPrefFile(String baseFold) {
		File f = new File(baseFold, Globals.getPrefFolder());
		return new File(f, "preferences");
	}

	public void patchFile(String baseFold, int w, int h) {
		File f = getPrefFile(baseFold);

		String buf;
		try {
			buf = getPatchedBuffer(f, w, h);
		} catch (IOException e) {
			Logger.log("SettingsPatcher", "Error reading preferences file '" + f.getAbsolutePath() + "'", e);
			return;
		}
		FileOutputStream os = null;

		f.getParentFile().mkdirs();
		try {
			os = new FileOutputStream(f, false);
			os.write(buf.getBytes());
		} catch (IOException e) {
			Logger.log("SettingsPatcher", "Error writing preferences file '" + f.getAbsolutePath() + "'", e);
			f.delete();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					Logger.log("SettingsPatcher", "Error closing file '" + f.getAbsolutePath() + "' after write", e);
				}
			}
		}
	}

	private void readValue(String line, Resolution res) {
		String l[] = line.split("=");
		if (l.length != 2) {
			return;
		}
		l[0] = l[0].trim();

		try {
			if ("xwindowsize".equalsIgnoreCase(l[0])) {
				res.width = Integer.parseInt(l[1].trim().replace("\"", ""));
			} else if ("ywindowsize".equalsIgnoreCase(l[0])) {
				res.height = Integer.parseInt(l[1].trim().replace("\"", ""));
			}
		} catch (NumberFormatException e) {
		}
	}

	private Resolution getResolution(String baseFold) {
		File f = getPrefFile(baseFold);
		final Resolution rv = new Resolution();
		rv.width = -1;
		rv.height = -1;
		try {
			readLines(f, new LineParser() {
				@Override
				public void parseLine(String str) {
					str = str.trim();
					readValue(str, rv);
				}
			});
		} catch (IOException e) {
			Logger.log("SettingsPatcher", "Error reading preferences file '" + f.getAbsolutePath() + "'", e);
			return null;
		}

		if (rv.width > 0 && rv.height > 0) {
			return rv;
		}
		return null;
	}

	public void ensureResolution(Activity ctx, String baseFold) {
		Resolution res = getResolution(baseFold);
		if (res == null) {
			return;
		}
		if (Globals.screenOrientation.isLandscape() != Mode.LANDSCAPE.is(res.width, res.height)) {
			res = ScreenResUtil.rotate(ctx, res);
			patchFile(baseFold, res.width, res.height);
		}
		nativeAddCustomResolution(res.width, res.height);
	}
	public native void nativeAddCustomResolution(int width, int height);

//	public static void main(String a[]) throws Exception {
//		File f = new File("/mnt/sdb2/alex/android/wesnoth/preferences");
//		new SettingsPatcher().patchFile(f);
//	}
}
