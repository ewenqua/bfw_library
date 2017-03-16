package it.ap.wesnoth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Looper;
import android.util.Log;

public class Logger {
	public static String TAG = "Wesnoth";
	private static StringBuilder log = new StringBuilder("");

	public static void logl(String str) {
		Log.d(TAG, str);
	}
	public static void log(String str) {
		Log.d(TAG, str);

		synchronized (Logger.class) {
			if (log != null) {
				log.append(str);
				log.append("\n");
			}
		}
	}
	public static void logl(String tag, String str) {
		Log.d(TAG + "." + tag, str);
	}
	public static void log(String tag, String str) {
		Log.d(TAG + "." + tag, str);

		synchronized (Logger.class) {
			if (log != null) {
				log.append(str);
				log.append("\n");
			}
		}
	}
	private static String getStackTrace(Throwable t) {
		StringWriter sw = null;
		PrintWriter pw = null;

		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			t.printStackTrace(pw);
		} finally {
			if (pw != null) pw.close();
			try {
				if (sw != null) sw.close();
			} catch (IOException e) {
			}
		}

		return sw == null ? "Error handling printStackTrace" : sw.toString();
	}

	public static void log(String str, Throwable t) {
		Log.e(TAG, str, t);

		String err = getStackTrace(t);
		synchronized (Logger.class) {
			if (log != null) {
				log.append(str);
				log.append("\n");
				log.append(err);
				log.append("\n");
			}
		}
	}
	public static void log(String tag, String str, Throwable t) {
		Log.e(TAG + "." + tag, str, t);

		String err = getStackTrace(t);
		synchronized (Logger.class) {
			if (log != null) {
				log.append(str);
				log.append("\n");
				log.append(err);
				log.append("\n");
			}
		}
	}

	public static void checkConfig() {
		synchronized (Logger.class) {
			if (!Globals.sendDebugInfo) {
				log = null;
			}
		}
	}

	public static void flush(boolean sync) {
		String data = null;
		synchronized (Logger.class) {
			if (Globals.sendDebugInfo && log != null) {
				data = log.toString();
			}
			checkConfig();
		}

		if (data != null) {
			final String fdata = data;
			final Runnable r = new Runnable() { @Override public void run() {
				try {
					StringEntity ent = new StringEntity(fdata);
					HttpPost request = new HttpPost("http://www.alessandropira.org/wesnoth_android/dev.php");
					request.addHeader("User-Agent", "Wget/1.13.4 (linux-gnu)");
					request.setEntity(ent);
					DefaultHttpClient client = new DefaultHttpClient();
					client.getParams().setBooleanParameter("http.protocol.handle-redirects", true);
					Log.d(TAG + ".flush", "Sending data");
					HttpResponse response = client.execute(request);
					Log.d(TAG + ".flush", "Reply: " + response.getStatusLine().getStatusCode());
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG + ".flush", "UEE: " + Globals.sendDebugInfo, e);
				} catch (ClientProtocolException e) {
					Log.e(TAG + ".flush", "CPE: " + Globals.sendDebugInfo, e);
				} catch (IOException e) {
					Log.e(TAG + ".flush", "IOE: " + Globals.sendDebugInfo, e);
				}
			}};
			if (sync) {
				if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
					final Object lock = new Object();
					new Thread(new Runnable() { @Override public void run() {
						r.run();
						synchronized (lock) {
							lock.notifyAll();
						}
					}}).start();
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							Log.e(TAG + ".flush", "Network operation interrupted", e);
						}
					}
				} else {
					r.run();
				}
			} else {
				new Thread(r).start();
			}
		}
	}
	
	public static void readLogCat() {
		synchronized (Logger.class) {
			if (log == null) {
				return;
			}
			try {
				Process process = Runtime.getRuntime().exec("logcat -d");
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					log.append(line);
					log.append("\n");
				}
				log.append("-- end of logcat extraction --\n");
			} catch (IOException e) {
				log("Failure reading logcat", e);
			}
		}
	}
}
