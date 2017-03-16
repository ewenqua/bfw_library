package it.ap.wesnoth;

import it.ap.wesnoth.DataDownload.DownloadPack;
import it.ap.wesnoth.DataDownloaderNg.DownloadReference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class DataPatcher {
	private int strip;
	private String targetPath;

	public DataPatcher(String targetPath) {
		this.targetPath = targetPath;
		if (!this.targetPath.endsWith("/")) {
			this.targetPath += "/";
		}
		this.strip = 0;
	}

	public static class PatchException extends Exception {
		private static final long serialVersionUID = 415097146605507509L;
		public PatchException(String string) {
			super(string);
		}
		public PatchException(Exception e) {
			super(e);
		}
	}

	private List<String> readLines(File f) throws IOException {
		BufferedReader br = null;
		List<String> rv = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(f));
			while (true) {
				String s = br.readLine();
				if (s == null) {
					break;
				}
				rv.add(s);
			}
		} catch (FileNotFoundException e) {
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
		}
		return rv;
	}
	private void writeLines(File f, List<?> lines) throws IOException {
		if (lines.size() == 0) {
			f.delete();
			return;
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f, false));
			for (Object s : lines) {
				if (s == null) {
					continue;
				}
				bw.write(s.toString());
				bw.write("\n");
			}
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
			}
		}
	}

	private String stripPath(String str) {
		if (strip > 0) {
			String path[] = str.split("/");
			StringBuilder sb = new StringBuilder("");
			for (int idx=strip; idx<path.length; idx++) {
				if (sb.length() > 0) {
					sb.append('/');
				}
				sb.append(path[idx]);
			}
			str = sb.toString();
		}
		return str;
	}

	private class FilePatch {
		private ArrayList<String> patchstr;
		private Patch patch;
		private String file;

		public FilePatch() {
			patchstr = new ArrayList<String>();
			patch = null;
			file = null;
		}

		public void addLine(String s) {
			if (file == null && s.startsWith("--- ")) {
				int tab = s.indexOf('\t');
				String str = s.substring(4, tab);
				file = stripPath(str);
			}
			patch = null;
			patchstr.add(s);
		}

		public void apply() throws PatchException {
			if (file == null) {
				throw new PatchException("Invalid patch format");
			}
			if (patch == null) {
				patch = DiffUtils.parseUnifiedDiff(patchstr);
			}

			File f = new File(targetPath + file);
			try {
				List<?> lines = readLines(f);
				try {
					lines = patch.applyTo(lines);
				} catch (PatchFailedException e) {
					for (Delta d : patch.getDeltas()) {
						try {
							d.getRevised().verify(lines);
						} catch (PatchFailedException e1) {
							throw new PatchException("Patching " + file + " failed: " + e.getMessage());
						}
					}
					return;
				}
				writeLines(f, lines);
			} catch (IOException e) {
				throw new PatchException(e);
			}
		}
	}

	private String extractCmd(String str) {
		int is = str.indexOf(' ');
		return str.substring(is).trim();
	}

	private void remove(File tgt) {
		if (".".equals(tgt.getName())) {
			return;
		}
		if ("..".equals(tgt.getName())) {
			return;
		}
		if (tgt.isDirectory()) {
			for (File f : tgt.listFiles()) {
				remove(f);
			}
		}
		tgt.delete();
	}

	public void doPatch(DownloadPack pack, DownloadReference dwl, String progressPrefix, DataDownloaderNg zipDownloader) throws PatchException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(dwl.stream));
			FilePatch producedPatch = null;

			while (true) {
				String s = br.readLine();
				zipDownloader.updateProgress(progressPrefix, dwl.totalLen, dwl.stream.getBytesRead(), dwl.url);

				if (s == null) {
					break;
				}
				if (s.startsWith("strip ")) {
					try {
						strip = Integer.parseInt(extractCmd(s));
					} catch (NumberFormatException e) {
						throw new PatchException(e);
					}
					continue;
				}
				if (s.startsWith("rm ")) {
					File f = new File(targetPath + stripPath(extractCmd(s)));
					remove(f);
					continue;
				}
				if (s.startsWith("dwl ")) {
					zipDownloader.processUpgradeDownload(pack, progressPrefix, extractCmd(s));
					continue;
				}
				if (s.startsWith("diff ")) {
					if (producedPatch != null) {
						producedPatch.apply();
					}
					producedPatch = new FilePatch();
					continue;
				}
				if (producedPatch == null) {
					throw new PatchException("Invalid header " + s);
				}
				producedPatch.addLine(s);
			}
			if (producedPatch != null) {
				producedPatch.apply();
			}
		} catch (IOException e) {
			Log.d("DataPatcher", "Upgrade failed", e);
			throw new PatchException(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
		}
	}
}
