package it.ap.wesnoth;

import it.alessandropira.wesnoth112.R;
import it.ap.wesnoth.DataDownload.DownloadPack;
import it.ap.wesnoth.DataPatcher.PatchException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Environment;
import android.widget.TextView;

public class DataDownloaderNg extends Thread {
	private StatusWriter statusWriter;
	public volatile boolean downloadComplete = false;
	private volatile boolean downloadFailed = false;
	private boolean downloadCanBeResumed = false;
	private MainActivity parentActivity;
	private String outFilesDir = null;
	private Tutorial tut;

	public static class CountingInputStream extends BufferedInputStream {
		private long bytesReadMark = 0;
		private long bytesRead = 0;

		public CountingInputStream(InputStream in, int size) {
			super(in, size);
		}

		public CountingInputStream(InputStream in) {
			super(in);
		}

		public long getBytesRead() {
			return bytesRead;
		}

		public synchronized int read() throws IOException {
			int read = super.read();
			if (read >= 0) {
				bytesRead++;
			}
			return read;
		}

		public synchronized int read(byte[] b, int off, int len) throws IOException {
			int read = super.read(b, off, len);
			if (read >= 0) {
				bytesRead += read;
			}
			return read;
		}

		public synchronized long skip(long n) throws IOException {
			long skipped = super.skip(n);
			if (skipped >= 0) {
				bytesRead += skipped;
			}
			return skipped;
		}

		public synchronized void mark(int readlimit) {
			super.mark(readlimit);
			bytesReadMark = bytesRead;
		}

		public synchronized void reset() throws IOException {
			super.reset();
			bytesRead = bytesReadMark;
		}
	}

	private static class StatusWriter {
		private TextView status;
		private MainActivity parent;
		private String lastText = "";

		public StatusWriter( TextView status, MainActivity parent ) {
			synchronized (this) {
				this.status = status;
				this.parent = parent;
			}
		}
		public void setParent( TextView status, MainActivity parent ) {
			synchronized (this) {
				this.status = status;
				this.parent = parent;
				setText( lastText );
			}
		}
		public void setText(final String str) {
			synchronized (this) {
				lastText = str;
				if( parent != null && status != null ) {
					parent.runOnUiThread(new Runnable() { @Override public void run() {
						synchronized (this) {
							if( parent != null && status != null ) {
								status.setText(str);
							}
						}
					}});
				}
			}
		}
	}

	private void logl(String str) {
		Logger.logl("DataDownloaderNg", str);
	}
	private void log(String str) {
		Logger.log("DataDownloaderNg", str);
	}
	private void log(String str, Throwable t) {
		Logger.log("DataDownloaderNg", str, t);
	}

	private boolean tutStarted = false;
	private void startTutorial() {
		if (tutStarted) {
			return;
		}
		tutStarted = true;
		this.tut.start();
	}

	public DataDownloaderNg( MainActivity parent, TextView status , Tutorial tut ) {
		parentActivity = parent;
		statusWriter = new StatusWriter( status, parent );
		outFilesDir = Globals.DataDir;
		downloadComplete = false;
		this.tut = tut;
		this.start();
		if (Globals.showTutorial) {
			startTutorial();
		}
	}
	
	public void setStatusField(TextView status) {
		statusWriter.setParent( status, parentActivity );
	}

	private void cleanRec(File fold) {
		for (File f : fold.listFiles()) {
			if (f.getName().startsWith(".wesnoth")) {
				continue;
			}
			if (f.getName().equals(".")) {
				continue;
			}
			if (f.getName().equals("..")) {
				continue;
			}
			if (f.isDirectory()) {
				cleanRec(f);
			}
			f.delete();
		}
	}

	private boolean ensureFolderExists() {
		File fold = new File (outFilesDir);
		if (!fold.exists()) {
			if (!fold.mkdirs()) {
				statusWriter.setText( parentActivity.getResources().getString(R.string.cannot_create_folder) );
				return false;
			}
		}
		return true;
	}

	private void cleanInstallation() {
		File fold = new File (outFilesDir);
		cleanRec(fold);
	}

	@Override
	public void run() {
		parentActivity.keyListener = new BackKeyListener(parentActivity);

		DataDownload.fillCurrentInfo(outFilesDir);

//		if (DataDownload.getMinDataVersion() >= 2) {
//			Globals.upgrade_1_12_confirmed = 1;
//			Settings.Save(parentActivity);
//		}
//
//		if (Globals.upgrade_1_12_confirmed == 2) {
//			DataDownload.lockMajorUpgrades(outFilesDir);
//		}
//
//		if (DataDownload.getAvailableVersion() >= 2 && DataDownload.getMinDataVersion() < 2) {
//			if (Globals.upgrade_1_12_confirmed == 0) {
//				Settings.showUpgradeQuestion(parentActivity,
//					new Runnable() { @Override public void run() {
//						new Thread(new Runnable() { @Override public void run() {
//							run1();
//						}}).start();
//					}},
//					new Runnable() { @Override public void run() {
//						DataDownload.lockMajorUpgrades(outFilesDir);
//						new Thread(new Runnable() { @Override public void run() {
//							run1();
//						}}).start();
//					}}
//				);
//
//				return;
//			} else if (Globals.upgrade_1_12_confirmed == 2) {
//				// Dont show dialog again
//				DataDownload.lockMajorUpgrades(outFilesDir);
//			}
//		}

		run1();
	}

	public void run1() {
		if (DataDownload.getMinDataVersion() < DataDownload.getAvailableVersion()) {
			parentActivity.runOnUiThread(new Runnable() { @Override public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
				builder.setCancelable(false);
				TextView tv = new TextView(parentActivity);
				String txt = parentActivity.getResources().getString(R.string.upg_title, ReleaseManager.getMaxAvailWesnothRelease().getVersionStr());
				tv.setText(txt);
				tv.setBackgroundColor(0xff000000);
				tv.setTextColor(0xffffffff);
	            builder.setView(tv);
				tv.setPadding(5, 5, 5, 5);

				builder.setPositiveButton(R.string.upg_yes, new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {
	            	dialog.dismiss();
	            	new Thread(new Runnable() { @Override public void run() {
		    			runDownload();
	            	}}).start();
	            }});
	            builder.setNegativeButton(R.string.upg_no, new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {
	            	dialog.dismiss();
	    			parentActivity.finish();
	            }});
	            AlertDialog alert = builder.create();
	            alert.show();
			}});
		} else {
			runDownload();
		}
	}

	private void runDownload() {
		int total = 0;
		int count = 0;

		if (!ensureFolderExists()) {
			downloadFailed = true;
			return;
		}

		if (Globals.doFullClean || DataDownload.needsCleaning()) {
			statusWriter.setText( parentActivity.getResources().getString(R.string.upg_clean) );
			cleanInstallation();
			for (int i=0; i<DataDownload.packCount(); i++) {
				DataDownload.markNotInstalled(i, outFilesDir);
			}
		}

		for (int i=0; i < DataDownload.packCount(); i++) {
			if (DataDownload.getPack(i).isToInstallOrUpgrade()) { 
				total += 1;
			}
		}

		for (int i=0; i < DataDownload.packCount(); i++) {
			DownloadPack pack = DataDownload.getPack(i);
			boolean ok = true;

			String progressPrefix = (count+1) + "/" + total + ": ";
			if (pack.isToInstall()) {
				ok = downloadDataFile(pack, progressPrefix);
				if (ok) {
					DataDownload.markDownloadOk(i, outFilesDir);
				}
				count += 1;
			} else if (pack.isToUpgrade()) {
				do {
					ok = downloadDataUpgrade(pack, progressPrefix);
					if (ok) {
						DataDownload.markUpgradeOk(i, outFilesDir);
					}
				} while (ok && pack.isToUpgrade());
				count += 1;
			} else if (!pack.isSelected()) {
				if (pack.isInstalled()) {
					statusWriter.setText( parentActivity.getResources().getString(R.string.upg_clean) );
					DataDownload.removeDownload(i, outFilesDir, false);
				}
				DataDownload.markNotInstalled(i, outFilesDir);
			}

			if (!ok) {
				if (pack.isOptional()) {
					DataDownload.removeDownload(i, outFilesDir, false);
					DataDownload.markNotInstalled(i, outFilesDir);
				} else {
					downloadFailed = true;
					return;
				}
			}
		}

		SettingsPatcher setPat = new SettingsPatcher();
		if (Globals.customRes) {
			setPat.patchFile(outFilesDir, Globals.customResW, Globals.customResH);
		}
		setPat.ensureResolution(parentActivity, outFilesDir);

		downloadComplete = true;
		parentActivity.keyListener = null;
		initParent();
	}

	public void updateProgress(String progressPrefix, long totalLen, long position, String path) {
		float percent = 0.0f;
		if( totalLen > 0 ) {
			percent = position * 100.0f / totalLen;
		}
		Resources res = parentActivity.getResources();
		statusWriter.setText(progressPrefix + res.getString(R.string.dl_progress, percent, path) );
	}

	private long[] getCRC(String path, byte buf[]) {
		long count = 0, ret = 0;
		CheckedInputStream check = null;
		try {
			check = new CheckedInputStream( new FileInputStream(path), new CRC32() );
			while ( ret >= 0 ) {
				count += ret;
				ret = check.read(buf, 0, buf.length);
			}
		} catch (IOException e) {
			return new long[] { -1L, -1L };
		} finally {
			if (check != null) {
				try {
					check.close();
				} catch (IOException e) {
				}
			}
		}
		return new long[] { check.getChecksum().getValue() , count };
	}

	public static class DownloadReference {
		public CountingInputStream stream;
		public long totalLen = 0;
		public String url;
	}

	@SuppressLint("SdCardPath")
	private DownloadReference getStream(String downloadUrls[], String progressPrefix, byte buf[]) {
		Resources res = parentActivity.getResources();

		HttpResponse response = null, responseError = null;
		HttpGet request;

		DownloadReference rv = new DownloadReference();
		boolean fileInAssets = false;
		File sdFile = null;
		long partialDownloadLen = 0;

		int downloadUrlIndex = 0;
		while( downloadUrlIndex < downloadUrls.length ) {
			log("Processing download " + downloadUrls[downloadUrlIndex]);
			rv.url = downloadUrls[downloadUrlIndex];
			statusWriter.setText(progressPrefix + res.getString(R.string.connecting_to, rv.url) );

			if( rv.url.indexOf("http://") == -1 && rv.url.indexOf("https://") == -1 ) { // File inside assets or onto sd card
				boolean found = false;
				for (String s : new String[] { "/mnt/extSdCard/", "/extSdCard/", "/mnt/sdcard/", "/sdcard/", Environment.getExternalStorageDirectory().getAbsolutePath() }) {
					File f1 = new File(s + rv.url);
					if (f1.exists()) {
						sdFile = f1;
						found = true;
						break;
					}
				}

				if (!found) {
					InputStream stream1 = null;
					try {
						stream1 = parentActivity.getAssets().open(rv.url);
						stream1.close();
					} catch( Exception e ) {
						try {
							stream1 = parentActivity.getAssets().open(rv.url + "000");
							stream1.close();
						} catch( Exception ee ) {
							log("Failed to open file in assets: " + rv.url);
							downloadUrlIndex++;
							continue;
						}
					}
					fileInAssets = true;
					log("Fetching file from assets: " + rv.url);
				}
				break;
			} else {
				log("Connecting to: " + rv.url);
				request = new HttpGet(rv.url);
				request.addHeader("Accept", "*/*");
				request.addHeader("User-Agent", "Wget/1.13.4 (linux-gnu)");
				if( partialDownloadLen > 0 ) { // per ora non puo' essere
					request.addHeader("Range", "bytes=" + partialDownloadLen + "-");
				}
				try {
					DefaultHttpClient client = HttpWithDisabledSslCertCheck();
					client.getParams().setBooleanParameter("http.protocol.handle-redirects", true);
					response = client.execute(request);
				} catch (IOException e) {
					log("Failed to connect to " + rv.url);
					downloadUrlIndex++;
				};
				if( response != null )
				{
					if( response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 206 )
					{
						log("Failed to connect to " + rv.url + " with error " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
						responseError = response;
						response = null;
						downloadUrlIndex++;
					}
					else
						break;
				}
			}
		}

		if( fileInAssets ) {
			int multipartCounter = 0;
			List<InputStream> multipart = null;
			CountingInputStream stream = null;

			while (true) {
				String url1 = rv.url + String.format("%03d", multipartCounter);

				try {
					// Make string ".zip00", ".zip01" etc for multipart archives
					stream = new CountingInputStream(parentActivity.getAssets().open(url1), 8192);
					while( stream.skip(65536) > 0 ) { };
					rv.totalLen += stream.getBytesRead();
					InputStream s = parentActivity.getAssets().open(url1);

					if (multipart == null) {
						multipart = new ArrayList<InputStream>();
					}
					multipart.add(s);

					log("Multipart archive found: " + url1);
				} catch (IOException e) {
					break;
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
						}
					}
				}

				multipartCounter += 1;
			}

			if( multipart != null ) {
				rv.stream = new CountingInputStream(new SequenceInputStream(Collections.enumeration(multipart)), 8192);
			} else {
				stream = null;
				try {
					stream = new CountingInputStream(parentActivity.getAssets().open(rv.url), 8192);
					while( stream.skip(65536) > 0 ) { };
					rv.totalLen += stream.getBytesRead();
					rv.stream = new CountingInputStream(parentActivity.getAssets().open(rv.url), 8192);
				} catch (IOException e) {
					log("Unpacking from assets '" + rv.url + "' - error: " + e.toString());
					statusWriter.setText( res.getString(R.string.error_dl_from, rv.url) );
					return null;
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
						}
					}
				}
			}
		} else if (sdFile != null) {
			statusWriter.setText( progressPrefix + res.getString(R.string.dl_from, sdFile.getAbsolutePath()) );

			try {
				rv.stream = new CountingInputStream(new FileInputStream(sdFile), 8192);
				rv.totalLen += sdFile.length();
			} catch (IOException e) {
				statusWriter.setText( res.getString(R.string.error_dl_from, rv.url) );
				return null;
			}
		} else {
			if( response == null )
			{
				log("Error connecting to " + rv.url);
				statusWriter.setText( res.getString(R.string.failed_connecting_to, rv.url) + (responseError == null ? "" : ": " + responseError.getStatusLine().getStatusCode() + " " + responseError.getStatusLine().getReasonPhrase()) );
				return null;
			}

			statusWriter.setText( progressPrefix + res.getString(R.string.dl_from, rv.url) );
			rv.totalLen = response.getEntity().getContentLength();
			try {
				rv.stream = new CountingInputStream(response.getEntity().getContent(), 8192);
			} catch( java.io.IOException e ) {
				statusWriter.setText( res.getString(R.string.error_dl_from, rv.url) );
				return null;
			}
		}

		return rv;
	}

	private boolean unpackZip(final DownloadPack pack, DownloadReference data, String progressPrefix, byte[] buf) {
		Resources res = parentActivity.getResources();
		String path = null;

		ZipInputStream zip = null;
		
		try {
			zip = new ZipInputStream(data.stream);
			
			while(true) {
				ZipEntry entry = null;
				try {
					entry = zip.getNextEntry();
					if( entry != null ) {
						logl("Reading from zip file '" + data.url + "' entry '" + entry.getName() + "'");
					}
				} catch( java.io.IOException e ) {
					statusWriter.setText( res.getString(R.string.error_dl_from, data.url) );
					log("Error reading from zip file '" + data.url + "': " + e.toString());
					return false;
				}

				if( entry == null ) {
					log("Reading from zip file '" + data.url + "' finished");
					break;
				}

				if( entry.isDirectory() ) {
					log("Creating dir '" + getOutFilePath(entry.getName()) + "'");
					try {
						File outDir = new File( getOutFilePath(entry.getName()) );
						if( !(outDir.exists() && outDir.isDirectory()) )
							outDir.mkdirs();
					} catch( SecurityException e ) {
					}

					continue;
				}

				OutputStream out = null;
				path = getOutFilePath(entry.getName());

				logl("Saving file '" + path + "'");

				try {
					File outDir = new File( path.substring(0, path.lastIndexOf("/") ));
					if( !(outDir.exists() && outDir.isDirectory()) )
						outDir.mkdirs();
				} catch( SecurityException e ) { };

				if ( getCRC(path, buf)[0] != entry.getCrc() ) {
					if (pack.canOverwrite(entry.getName()) ) {
						File ff = new File(path);
						ff.delete();
					} else {
						// se il pacchetto music e' installato i suoi file non vanno sovrascritti anche se il CRC non fa match
						log("File '" + path + "' exists and belongs to a different package - not overwriting it");
						updateProgress(progressPrefix, data.totalLen, data.stream.getBytesRead(), path);
						continue;
					}
				} else {
					log("File '" + path + "' exists and passed CRC check - not overwriting it");
					updateProgress(progressPrefix, data.totalLen, data.stream.getBytesRead(), path);
					continue;
				}

				updateProgress(progressPrefix, data.totalLen, data.stream.getBytesRead(), path);
				
				try {
					out = new FileOutputStream( path, false );

					int len = zip.read(buf);
					while (len >= 0) {
						if(len > 0) {
							out.write(buf, 0, len);
						}
						updateProgress(progressPrefix, data.totalLen, data.stream.getBytesRead(), path);

						len = zip.read(buf);
					}

					out.flush();
				} catch( FileNotFoundException e ) {
					log("Saving file '" + path + "' - cannot create file: " + e.toString());
					statusWriter.setText( res.getString(R.string.error_write, path) );
					return false;
				} catch( SecurityException e ) {
					log("Saving file '" + path + "' - cannot create file: " + e.toString());
					statusWriter.setText( res.getString(R.string.error_write, path) );
					return false;
				} catch( java.io.IOException e ) {
					statusWriter.setText( res.getString(R.string.error_write, path) + ": " + e.getMessage() );
					log("Saving file '" + path + "' - error writing or downloading: " + e.toString());
					return false;
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
						}
						out = null;
					}
				}
				
				long crc[] = getCRC(path, buf); 
				if( crc[0] != entry.getCrc() || crc[1] != entry.getSize() ) {
					File ff = new File(path);
					ff.delete();
					log("Saving file '" + path + "' - CRC check failed, ZIP: " +
										String.format("%x", entry.getCrc()) + " actual file: " + String.format("%x", crc[0]) +
										" file size in ZIP: " + entry.getSize() + " actual size " + crc[1] );
					statusWriter.setText( res.getString(R.string.error_write, path) );
					return false;
				}
				logl("Saving file '" + path + "' done");
			}
			return true;
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) { }
			}
			if (data.stream != null) {
				try {
					data.stream.close();
				} catch (IOException e) { }
			}
		}
	}

	private boolean downloadDataUpgrade(DownloadPack pack, String progressPrefix) {
		byte[] buf = new byte[16384];
		DataPatcher patcher = new DataPatcher(outFilesDir);

		if (pack.cleanBeforeUpgrade()) {
			pack.remove(outFilesDir);
		}

		String upg[] = pack.getUpgradeUrlsZip();
		if (upg != null) {
			boolean ok = false;
			for (String u : upg) {
				try {
					processUpgradeDownload(pack, progressPrefix + " [unpacking] ", u);
					ok = true;
					break;
				} catch (IOException e) {
				}
			}
			if (!ok) {
				return false;
			}
		}

		upg = pack.getUpgradeUrlsDiff();
		if (upg != null) {
			DownloadReference data = getStream(upg, progressPrefix + " [patching] ", buf);
			if (data == null) {
				return false;
			}
			try {
				patcher.doPatch(pack, data, progressPrefix, this);
			} catch (PatchException e) {
				log("Patch failed", e);
				return false;
			}
		}

		return true;
	}

	public boolean downloadDataFile(final DownloadPack pack, String progressPrefix) {
		downloadCanBeResumed = false;
		Resources res = parentActivity.getResources();

		String downloadUrls[] = pack.getUrls();

		startTutorial();

		log("Downloading data to: '" + outFilesDir + "'");
		try {
			File outDir = new File( outFilesDir );
			if( !(outDir.exists() && outDir.isDirectory()) )
				outDir.mkdirs();
			OutputStream out = new FileOutputStream( getOutFilePath(".nomedia") );
			out.flush();
			out.close();
		} catch( SecurityException e ) {
		} catch( FileNotFoundException e ) {
		} catch( IOException e ) {
		};

		byte[] buf = new byte[16384];

		DownloadReference data = getStream(downloadUrls, progressPrefix, buf);
		if (data == null) {
			return false;
		}

		log("Reading from zip file '" + data.url + "'");
		if (!unpackZip(pack, data, progressPrefix, buf)) {
			return false;
		}

//		try {
//			markDownloadOk(downloadIndex, downloadUrlIndex);
//		} catch (IOException e) {
//			statusWriter.setText( res.getString(R.string.error_write, getDownloadFlagFilename(downloadIndex)) + ": " + e.getMessage() );
//			return false;
//		}

		statusWriter.setText(progressPrefix + res.getString(R.string.dl_finished) );

		return true;
	};

	public void processUpgradeDownload(DownloadPack pack, String progressPrefix, String downloadUrls) throws IOException {
		byte[] buf = new byte[16384];

		DownloadReference data = getStream(downloadUrls.split("#"), progressPrefix, buf);
		if (data == null) {
			throw new IOException("Open of '" + downloadUrls + "' failed");
		}

		log("Reading from zip file '" + data.url + "'");
		if (!unpackZip(pack, data, progressPrefix, buf)) {
			throw new IOException("Download of '" + data.url + "' failed");
		}
	}

	@SuppressWarnings("unused")
	private boolean writePangoModules() {
		File libDir = parentActivity.getFilesDir();
		String libDirS = libDir.getAbsolutePath();

		File outDir = new File( getOutFilePath("pango") );
		if( !(outDir.exists() && outDir.isDirectory()) )
			outDir.mkdirs();
		File pangoModules = new File (outDir, "pango.modules");

		log("Writing pango modules to '" + pangoModules.getAbsolutePath() + "', libDirS: '" + libDirS + "'");

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(pangoModules, false);
			fos.write("# Pango Modules file\n".getBytes("UTF-8"));
			fos.write("# Automatically generated file, do not edit\n".getBytes("UTF-8"));
			fos.write("#\n".getBytes("UTF-8"));
			fos.write((libDirS + "/pango/basic/pango-basic-fc.so BasicScriptEngineFc PangoEngineShape PangoRenderFc common:\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/arabic/pango-arabic-lang.so ArabicScriptEngineLang PangoEngineLang PangoRenderNone arabic:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so devaIndicScriptEngineLang PangoEngineLang PangoRenderNone devanagari:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so bengIndicScriptEngineLang PangoEngineLang PangoRenderNone bengali:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so guruIndicScriptEngineLang PangoEngineLang PangoRenderNone gurmukhi:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so gujrIndicScriptEngineLang PangoEngineLang PangoRenderNone gujarati:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so oryaIndicScriptEngineLang PangoEngineLang PangoRenderNone oriya:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so tamlIndicScriptEngineLang PangoEngineLang PangoRenderNone tamil:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so teluIndicScriptEngineLang PangoEngineLang PangoRenderNone telugu:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so kndaIndicScriptEngineLang PangoEngineLang PangoRenderNone kannada:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so mlymIndicScriptEngineLang PangoEngineLang PangoRenderNone malayalam:*\n").getBytes("UTF-8"));
			fos.write((libDirS + "/pango/indic/pango-indic-lang.so sinhIndicScriptEngineLang PangoEngineLang PangoRenderNone sinhala:*\n").getBytes("UTF-8"));
			fos.write("\n".getBytes("UTF-8"));
		} catch (FileNotFoundException e) {
			log("Writing pango modules failed", e);
			Resources res = parentActivity.getResources();
			statusWriter.setText( res.getString(R.string.error_write, pangoModules.getAbsolutePath()) + ": " + e.getMessage() );
			return false;
		} catch (IOException e) {
			log("Writing pango modules failed", e);
			Resources res = parentActivity.getResources();
			statusWriter.setText( res.getString(R.string.error_write, pangoModules.getAbsolutePath()) + ": " + e.getMessage() );
			return false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) { }
			}
		}
		return true;
	}

	private void initParent() {
		synchronized(this) {
			if(parentActivity != null) {
				parentActivity.runOnUiThread(new Runnable() { @Override public void run() {
					parentActivity.initSDL();
				}});
			}
		}
	}
	
	private String getOutFilePath(final String filename)
	{
		return outFilesDir + "/" + filename;
	};
	
	private static DefaultHttpClient HttpWithDisabledSslCertCheck()
	{
		return new DefaultHttpClient();
		// This code does not work
		/*
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        DefaultHttpClient client = new DefaultHttpClient();

        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        registry.register(new Scheme("https", socketFactory, 443));
        SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
        DefaultHttpClient http = new DefaultHttpClient(mgr, client.getParams());

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

        return http;
		*/
	}


	public class BackKeyListener implements Settings.KeyEventsListener
	{
		MainActivity p;
		public BackKeyListener(MainActivity _p) {
			p = _p;
		}

		public void onKeyEvent(final int keyCode) {
			if (downloadFailed) {
				System.exit(1);
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(p);
			builder.setTitle(p.getResources().getString(R.string.cancel_download));
			builder.setMessage(p.getResources().getString(R.string.cancel_download) + (downloadCanBeResumed ? " " + p.getResources().getString(R.string.cancel_download_resume) : ""));
			
			builder.setPositiveButton(p.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					System.exit(1);
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(p.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item)  {
					dialog.dismiss();
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) { }
			});
			AlertDialog alert = builder.create();
			alert.setOwnerActivity(p);
			alert.show();
		}
	}
}

