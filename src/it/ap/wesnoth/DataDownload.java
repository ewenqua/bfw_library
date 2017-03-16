package it.ap.wesnoth;

import it.ap.wesnoth.ReleaseManager.Minor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

public class DataDownload {
	private static interface OverwriteChecker {
		boolean canOverwrite(String name);
	}
	private static abstract class Remover {
		private String base;
		public Remover() {
			this.base = "";
		}

		private void removeRec(File tgt, OverwriteChecker checker) {
			if (".".equals(tgt.getName())) {
				return;
			}
			if ("..".equals(tgt.getName())) {
				return;
			}
			if (checker != null) {
				String path = tgt.getAbsolutePath();
				if (path.startsWith(base)) {
					path = path.substring(base.length());
					if (path.startsWith("/")) {
						path = path.substring(1);
					}
				}
				if (!checker.canOverwrite(path)) {
					return;
				}
			}

			if (tgt.isDirectory()) {
				for (File f : tgt.listFiles()) {
					removeRec(f, checker);
				}
			}
			tgt.delete();
		}

		protected void remove(String path) {
			removeRec(new File(base, path), null);
		}

		protected void removeFolderContent(String path) {
			removeFolderContent(path, null);
		}

		protected void removeFolderContent(String path, OverwriteChecker checker) {
			File fold = new File(base, path);
			File list[] = fold.listFiles();
			if (list != null) {
				for (File f : list) {
					removeRec(f, checker);
				}
			}
		}

		protected String[] getContent(String path) {
			File fold = new File(base, path);
			File list[] = fold.listFiles();
			if (list == null) {
				return new String[0];
			}
			String rv[] = new String[list.length];
			for (int i=0; i<list.length; i++) {
				rv[i] = list[i].getName();
			}
			return rv;
		}

		protected void removeFolderIfEmpty(String path) {
			File fold = new File(base, path);
			File list[] = fold.listFiles();
			if ((list == null) || (list.length == 0)) {
				fold.delete();
			}
		}

		protected boolean copyFile(String src, String dest) {
			File sourceFile = new File(base, src);
			File destFile = new File(base, dest);
			
			FileChannel source = null;
			FileChannel destination = null;

			try {
				if (!destFile.exists()) {
					destFile.createNewFile();
				}

				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());
				return true;
			} catch (IOException e) {
				destFile.delete();
				return false;
			} finally {
				if (destination != null) {
					try {
						destination.close();
					} catch (IOException e) {
					}
				}
				if (source != null) {
					try {
						source.close();
					} catch (IOException e) {
					}
				}
			}
		}

		public void execute(String base) {
			this.base = base == null ? "" : base;
			remove();
		}

		protected abstract void remove();
	}

	public static class DownloadPack {
//		private static final String SEP = "|||";
		private String description;
		private List<String> url;
		private int availableVersion;
		private boolean mandatory;
		private List<List<String>> upgradeUrlsDiff;
		private List<List<String>> upgradeUrlsZip;
		private int currentVersion;
		private boolean selected;
		private boolean downloaded;
		private OverwriteChecker overwriteChecker;
		private Remover remover;

		private DownloadPack(boolean mandatory, int ver, String desc) {
			this.description = desc;
			this.mandatory = mandatory;
			this.availableVersion = ver;
			this.url = new ArrayList<String>();
			this.upgradeUrlsDiff = new ArrayList<List<String>>();
			this.upgradeUrlsZip = new ArrayList<List<String>>();
			this.overwriteChecker = null;
			this.remover = null;
		}
		private void setOverwriteChecker(OverwriteChecker overwriteChecker) {
			this.overwriteChecker = overwriteChecker;
		}
		private void addUrl(String url) {
			this.url.add(url);
		}
		private void addUpgradeUrl(int version, String urlDiff, String urlZip) {
			List<String> urls = null;
			for (int i=this.upgradeUrlsDiff.size(); i<=version; i++) {
				this.upgradeUrlsDiff.add(i, null);
			}
			for (int i=this.upgradeUrlsZip.size(); i<=version; i++) {
				this.upgradeUrlsZip.add(i, null);
			}

			if (urlDiff != null) {
				urls = this.upgradeUrlsDiff.get(version);
				if (urls == null) {
					urls = new ArrayList<String>();
					this.upgradeUrlsDiff.set(version, urls);
				}
				urls.add(urlDiff);
			}
			if (urlZip != null) {
				urls = this.upgradeUrlsZip.get(version);
				if (urls == null) {
					urls = new ArrayList<String>();
					this.upgradeUrlsZip.set(version, urls);
				}
				urls.add(urlZip);
			}
		}

		public String getDescription() {
			return description;
		}
		public boolean isInstalled() {
			return downloaded;
		}
		public boolean isSelected() {
			return selected || mandatory;
		}
		public boolean isToInstall() {
			return isSelected() && !isInstalled();
		}
		public boolean isToUpgrade() {
			return isSelected() && availableVersion > currentVersion;
		}
		public boolean cleanBeforeUpgrade() {
			return isToUpgrade() && isFullUpgrade(currentVersion, availableVersion);
		}
		public boolean isToRemove() {
			return !isSelected() && isInstalled();
		}
		public boolean isToInstallOrUpgrade() {
			return isToInstall() || isToUpgrade();
		}
		public String[] getUrls() {
			return url.toArray(new String[url.size()]);
		}
		public String[] getUpgradeUrlsDiff() {
			if (availableVersion <= currentVersion || downloaded == false) {
				return null;
			}

			try {
				List<String> urls = upgradeUrlsDiff.get(currentVersion);
				if (urls == null) {
					return null;
				}
				return urls.toArray(new String[urls.size()]);
			} catch (ArrayIndexOutOfBoundsException e) {
			} catch (IndexOutOfBoundsException e) {
			}

			return null;
		}
		public String[] getUpgradeUrlsZip() {
			if (availableVersion <= currentVersion || downloaded == false) {
				return null;
			}

			try {
				List<String> urls = upgradeUrlsZip.get(currentVersion);
				if (urls == null) {
					return null;
				}
				return urls.toArray(new String[urls.size()]);
			} catch (ArrayIndexOutOfBoundsException e) {
			} catch (IndexOutOfBoundsException e) {
			}

			return null;
		}

		private void markUpgraded() {
			if (currentVersion >= 0) {
				Minor ver = Minor.fromCode(currentVersion);
				if (ver != null) {
					ver = ver.getNext();
				}
				if (ver != null) {
					currentVersion = ver.getVersionCode();
				} else {
					currentVersion ++;
				}

				if (currentVersion > availableVersion) {
					currentVersion = availableVersion;
				}
			}
		}
		private void markInstalled() {
			this.downloaded = true;
			this.currentVersion = availableVersion;
		}
		private void markNotInstalled() {
			this.downloaded = false;
		}

		private void readData(String data, boolean selected) {
			this.selected = selected;

			if (data == null || data.length() == 0) {
				downloaded = false;
				currentVersion = -1;
			} else if (data.charAt(0) == 'X') {
				downloaded = false;
				currentVersion = -1;

				try {
					int v = Integer.parseInt(data.substring(1));
					switch (v) {
					case 0:
						currentVersion = Minor.R_1_10_6.getVersionCode();
						downloaded = true;
						break;
					case 1:
						currentVersion = Minor.R_1_10_7.getVersionCode();
						downloaded = true;
						break;
					case 2:
						currentVersion = Minor.R_1_11_17.getVersionCode();
						downloaded = true;
						break;
					}
				} catch (NumberFormatException e) {
					Log.w("Wesnoth", "DownloadPack parsing failed '" + data + "'", e);
				}
			} else if (data.charAt(0) == 'F') {
				downloaded = false;
				currentVersion = -1;

				try {
					currentVersion = Integer.parseInt(data.substring(1));
					downloaded = true;
				} catch (NumberFormatException e) {
					Log.w("Wesnoth", "DownloadPack parsing failed '" + data + "'", e);
				}
			} else {
				downloaded = true;
				currentVersion = 0;
			}
		}

		private String writeData() {
			if (!downloaded) {
				return "";
			} else {
				return "F" + currentVersion;
			}
		}

		public boolean canOverwrite(String name) {
			if (overwriteChecker == null) {
				return true;
			}
			return overwriteChecker.canOverwrite(name);
		}

		public void setRemover(Remover remover) {
			this.remover = remover;
		}

		public void remove(String baseFold) {
			downloaded = false;
			if (remover != null) {
				remover.execute(baseFold);
			}
		}
		public boolean isOptional() {
			return !mandatory;
		}
	}

	private static void addUrls(DownloadPack dp, String base[], String file, Map<String,String> add) {
		for (String url : base) {
			String addstr = add.get(url);
			if (addstr != null) {
				url = url.toLowerCase(Locale.US);
				url += addstr;
			}
			url = String.format(url, file);
			dp.addUrl(url);
		}
	}
	private static void addUpgradeUrls(int version, DownloadPack dp, String base[], String fileDiff, Map<String,String> addDiff, String fileZip, Map<String,String> addZip) {
		for (String url : base) {
			String ud, uz;
			ud = uz = url;

			String addstr = addDiff.get(ud);
			if (addstr != null) {
				ud = url.toLowerCase(Locale.US);
				ud += addstr;
			}
			addstr = addZip.get(uz);
			if (addstr != null) {
				uz = url.toLowerCase(Locale.US);
				uz += addstr;
			}

			dp.addUpgradeUrl(version, fileDiff == null ? null : String.format(ud, fileDiff), fileZip == null ? null : String.format(uz, fileZip));
		}
	}

	private static final boolean SDCARD_ONLY = false;

	private static final String DWLS[][] = {
			{"essential", "Game core data (mandatory) [105MB download, 144MB unpacked]", "1"},
			{"campaigns", "Campaign data (official campaigns) [89MB download, 102MB unpacked]", "0"},
			{"music", "Music data (no game music, without this) [137MB download, 139MB unpacked]", "0"},
			{"translations", "Language data (english only, without this) [39MB download, 101MB unpacked]", "0"},
			{"music_comp", "Alternative music data by Lukas Ehrig (compressed, lower quality) [83MB download, 84MB unpacked]", "0"},
			{"bestumc", "Best of user made campaigns selected by the Wesnoth community [212MB download, 274MB unpacked]", "0"}
	};

	private static DownloadPack downloads[];

	private static String getVersionName(int version) {
		return Minor.fromCode(version).getVersionStr();
	}
	private static boolean isNoUpgrade(String pkg, Minor targetVersion) {
		if ("music".equals(pkg) || "music_comp".equals(pkg) || "umc".equals(pkg)) {
			return true;
		}
		return false;
	}

	public static void initialize(int currentVersion) {
		String vn = getVersionName(currentVersion);
		String URLS[] = {
			"%s",
			SDCARD_ONLY ? "%s" : "http://sourceforge.net/projects/wesnoth-on-android/files/" + vn + "/datafiles/%s/download"
		};

		String v = "-" + vn;

		Map<String,String> add = new HashMap<String, String>();

		downloads = new DownloadPack[DWLS.length];

		for (int idx=0; idx < DWLS.length; idx++) {
			String d[] = DWLS[idx];
			String pkg = d[0];
			boolean mandatory = "1".equals(d[2]);

			downloads[idx] = new DownloadPack(mandatory, currentVersion, d[1]);
			addUrls(downloads[idx], URLS, "wesnoth_" + pkg + v + ".zip", add);

			Minor ver = Minor.fromCode(currentVersion);
			Minor prev = ver.getPrevious();
			if (prev != null) {
				int prevCode = prev.getVersionCode();
				
				if (isFullUpgrade(prevCode, ver.getVersionCode())) {
					addUpgradeUrls(prevCode, downloads[idx], URLS, null, add, "wesnoth_" + pkg + "-"+ver.getVersionStr()+".zip", add);
				} else if (isNoUpgrade(pkg, ver)) {
					addUpgradeUrls(prevCode, downloads[idx], URLS, null, add, null, add);
				} else {
					addUpgradeUrls(prevCode, downloads[idx], URLS, "wesnoth_" + pkg + "-" + prev.getVersionStr() + "_" + ver.getVersionStr() + ".diff", add, "wesnoth_" + pkg + "-" + prev.getVersionStr() + "_" + ver.getVersionStr() + ".zip", add);
				}
			}
		}

		setupRemovers();
	}

	private static void setupRemovers() {
		downloads[0].setOverwriteChecker(new OverwriteChecker() {
			@Override
			public boolean canOverwrite(String name) {
				// se il pacchetto music e' installato i suoi file non vanno sovrascritti anche se il CRC non fa match
				if (downloads.length < 3) {
					return true;
				}

				if (downloads[1].isInstalled() && name.startsWith("data/campaigns/") && !name.startsWith("data/campaigns/tutorial/")) {
					return false;
				}

				if (downloads[2].isInstalled() || downloads[4].isInstalled()) {
					if (name.startsWith("data/core/music/") && name.endsWith(".ogg") && !name.equals("data/core/music/silence.ogg")) {
						return false;
					}
				}

				return true;
			}
		});

		downloads[0].setRemover(new Remover() { @Override protected void remove() {
			this.remove("fonts");
			this.remove("images");
			this.remove("l10n-track");
			this.remove("pango");
			this.remove("sounds");
			this.removeFolderIfEmpty("translations");
			removeFolderContent("data", downloads[0].overwriteChecker);
			this.removeFolderIfEmpty("data/core/music");
			this.removeFolderIfEmpty("data/core");
			this.remove("data/campaigns/tutorial");
			this.removeFolderIfEmpty("data/campaigns");
			this.removeFolderIfEmpty("data");
		}});
		downloads[1].setRemover(new Remover() { @Override protected void remove() {
			this.removeFolderContent("data/campaigns");
			if (!downloads[0].isInstalled()) {
				this.remove("data/campaigns");
				this.removeFolderIfEmpty("data");
			}
		}});
		downloads[2].setRemover(new Remover() { @Override protected void remove() {
			String names[] = getContent("data/core/music");
			if (downloads[0].isInstalled()) {
				for (String f : names) {
					if ("silence.ogg".equals(f)) {
						continue;
					}
					copyFile("data/core/music/silence.ogg", "data/core/music/" + f);
				}
			} else {
				this.remove("data/core/music");
				this.removeFolderIfEmpty("data/core");
				this.removeFolderIfEmpty("data");
			}
		}});
		downloads[3].setRemover(new Remover() { @Override protected void remove() {
			this.removeFolderContent("translations");
			if (!downloads[0].isInstalled()) {
				this.remove("translations");
			}
		}});
		downloads[4].setRemover(downloads[2].remover);
		downloads[5].setRemover(new Remover() { @Override protected void remove() {
			String fold = ReleaseManager.getRunningWesnothFolder();
			this.remove(fold + "/data/add-ons/After_the_Storm");
			this.remove(fold + "/data/add-ons/AtS_Music");
			this.remove(fold + "/data/add-ons/Elvish_Dynasty");
			this.remove(fold + "/data/add-ons/Era_of_Magic");
			this.remove(fold + "/data/add-ons/Fate_of_a_Princess");
			this.remove(fold + "/data/add-ons/Grnk");
			this.remove(fold + "/data/add-ons/IftU_Music");
			this.remove(fold + "/data/add-ons/Invasion_from_the_Unknown");
			this.remove(fold + "/data/add-ons/Irdya_Dragon");
			this.remove(fold + "/data/add-ons/Legend_of_the_Invincibles");
			this.remove(fold + "/data/add-ons/Ooze_Mini_Campaign");
			this.remove(fold + "/data/add-ons/Secrets_of_the_Ancients");
			this.remove(fold + "/data/add-ons/Soldier_of_Wesnoth");
			this.remove(fold + "/data/add-ons/Swamplings");
			this.remove(fold + "/data/add-ons/To_Lands_Unknown");
			this.remove(fold + "/data/add-ons/To_Lands_Unknown_Images");
		}});
	}

	public static String[] getDownloadURLs(int index) {
		return downloads[index].getUrls();
	}

	public static final String DOWNLOAD_FLAG_FILENAME = "libsdl-DownloadFinished-";
	private static String getDownloadFlagFilename(String baseFold, int index) {
		return getOutFilePath(baseFold, DOWNLOAD_FLAG_FILENAME + index + ".flag");
	}
	private static  String getOutFilePath(String baseFold, String filename) {
		return baseFold + "/" + filename;
	}
	private static String readFlagFile(String path) {
		InputStream checkFile = null;
		try {
			checkFile = new FileInputStream( path );
			byte b[] = new byte[1024];
			int pos = 0;
			
			for (int i=0; i<b.length; i++) {
				int nr = checkFile.read(b, pos, b.length - pos);
				if (nr <= 0) {
					break;
				}
				pos += nr;
			}

			if (pos < b.length) {
				byte b1[] = new byte[pos];
				System.arraycopy(b, 0, b1, 0, pos);
				b = b1;
			}

			return new String(b, "UTF-8");
		} catch( FileNotFoundException e ) {
		} catch( SecurityException e ) {
		} catch (IOException e) {
		} finally {
			if (checkFile != null) {
				try {
					checkFile.close();
				} catch (IOException e) { }
			}
		}

		return null;
	}
	private static void writeFlagFile(String path, String content) {
		OutputStream out = null;
		try {
			out = new FileOutputStream( path, false );
			out.write(content.getBytes("UTF-8"));
			out.flush();
			out.close();
		} catch( SecurityException e ) {
		} catch( IOException e ) {
		}
	}

	private static boolean isFullUpgrade(int fromVer, int toVer) {
		Minor from = Minor.fromCode(fromVer);
		Minor to = Minor.fromCode(toVer);
		if (from == null) {
			return true;
		}
		return from.getMajor() != to.getMajor() || to.getPrevious() != from;
	}

	private static void fillCurrentInfo(int index, String baseFold) {
		String path = getDownloadFlagFilename(baseFold, index);
		String data = readFlagFile(path);

		downloads[index].readData(data, index < Globals.OptionalDataDownload.length ? Globals.OptionalDataDownload[index] : false);
	}
	public static void fillCurrentInfo(String baseFold) {
		for (int index=0; index<downloads.length; index++) {
			fillCurrentInfo(index, baseFold);
		}
	}
	public static void markUpgradeOk(int index, String baseFold) {
		downloads[index].markUpgraded();
		String path = getDownloadFlagFilename(baseFold, index);
		writeFlagFile(path, downloads[index].writeData());
	}
	public static void markDownloadOk(int index, String baseFold) {
		downloads[index].markInstalled();
		String path = getDownloadFlagFilename(baseFold, index);
		writeFlagFile(path, downloads[index].writeData());
	}
	public static void markNotInstalled(int index, String baseFold) {
		downloads[index].markNotInstalled();
		String path = getDownloadFlagFilename(baseFold, index);
		writeFlagFile(path, downloads[index].writeData());
	}
	public static void removeDownload(int index, String baseFold, boolean unselect) {
		downloads[index].remove(baseFold);
		if (unselect) {
			downloads[index].selected = false;
		}
		String path = getDownloadFlagFilename(baseFold, index);
		writeFlagFile(path, downloads[index].writeData());
		downloads[index].markNotInstalled();
	}

	public static int getAvailableVersion() {
		return ReleaseManager.getMaxAvailWesnothRelease().getVersionCode();
	}

	public static int getMaxDataVersion() {
		int v = -1;
		for (DownloadPack dp : downloads) {
			if (dp.isInstalled() && dp.currentVersion > v) {
				v = dp.currentVersion;
			}
		}
		if (v < 0) {
			v = getAvailableVersion();
		}
		return v;
	}

	public static int getMinDataVersion() {
		int v = Integer.MAX_VALUE;
		for (DownloadPack dp : downloads) {
			if (dp.isInstalled() && dp.currentVersion < v) {
				v = dp.currentVersion;
			}
		}
		if (v == Integer.MAX_VALUE) {
			v = getAvailableVersion();
		}
		return v;
	}

	public static boolean needsCleaning() {
		Boolean rv = null;
		for (DownloadPack dp : downloads) {
			if (!dp.isSelected()) {
				continue;
			}
			rv = true;
			if (!dp.cleanBeforeUpgrade()) {
				rv = false;
				break;
			}
		}
		return rv != null && rv.booleanValue();
	}

	public static int packCount() {
		return downloads.length;
	}
	public static DownloadPack getPack(int index) {
		return downloads[index];
	}
	public static boolean isMandatory(int index) {
		return downloads[index].mandatory;
	}
}
