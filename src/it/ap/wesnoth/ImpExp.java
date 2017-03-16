package it.ap.wesnoth;

import it.alessandropira.wesnoth112.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.EditText;

public class ImpExp {
	private static final String DEFAULT_FILE = "wesnoth_saves.zip";
	
	private static boolean addToZip(File file, ZipOutputStream zos) throws IOException {
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(file);

			ZipEntry zipEntry = new ZipEntry(file.getName());
			zos.putNextEntry(zipEntry);
			try {
				byte[] bytes = new byte[4096];
				int length;
				while ((length = fis.read(bytes)) >= 0) {
					zos.write(bytes, 0, length);
				}
			} finally {
				zos.closeEntry();
			}

			return true;
		} catch (FileNotFoundException e) {
    		Logger.log("addToZip: exc1", e);
			return false;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	public static int writeZipFile(String dir, String outputZip) {
		if (!outputZip.toLowerCase(Locale.US).endsWith(".zip")) {
			outputZip += ".zip";
		}
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			int count = 0;
			for (File file : new File(dir).listFiles()) {
				if (file.isDirectory()) { // we only zip files, not directories
					continue;
				}
				if (fos == null)  {
					fos = new FileOutputStream(outputZip, false);
					zos = new ZipOutputStream(fos);
				}
				if (addToZip(file, zos)) {
					count += 1;
				}
			}
			return count;
		} catch (IOException e) {
    		Logger.log("writeZipFile: exc1", e);
			return -1;
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static void runExport(final MainActivity p, final String fname, final Runnable closeAction) {
		final ProgressDialog waiterDialog = ProgressDialog.show(p, "Exporting...", "Exporting data to " + fname, true, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
            	final int rv = writeZipFile(Globals.getSavegameDir(), fname);
           		p.runOnUiThread(new Runnable() { @Override public void run() {
                	waiterDialog.dismiss();
            		if (rv > 0) {
            			new AlertDialog.Builder(p).setMessage(R.string.ap_impexp_exp_res_ok).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item)  {
								closeAction.run();
							}
						}).show();
            		} else if (rv == 0) {
            			new AlertDialog.Builder(p).setMessage(R.string.ap_impexp_exp_res_nodata).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item)  {
								closeAction.run();
							}
						}).show();
            		} else {
            			new AlertDialog.Builder(p).setMessage(R.string.ap_impexp_exp_res_fail).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item)  {
								closeAction.run();
							}
						}).show();
            		}
           		}});
            }
        }).start();
	}

	public static void doSaveExport(final MainActivity p, final Runnable closeAction) {
		AlertDialog.Builder builder = new AlertDialog.Builder(p);
		builder.setTitle(p.getResources().getString(R.string.ap_impexp_exp_file));

		final EditText edit = new EditText(p);
		edit.setFocusableInTouchMode(true);
		edit.setFocusable(true);
		edit.setText(new File(Environment.getExternalStorageDirectory(), DEFAULT_FILE).getAbsolutePath());
		builder.setView(edit);

		builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item)  {
				final String fname = "" + edit.getText();
				if (new File(fname).exists()) {
				       new AlertDialog.Builder(p)
				        .setMessage(R.string.ap_impexp_exp_file_exists)
				        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				            @Override public void onClick(DialogInterface dialog, int which) {
				            	runExport(p, fname, closeAction);
				            }
				        })
				        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item)  {
								closeAction.run();
							}
						})
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override public void onCancel(DialogInterface arg0) {
								closeAction.run();
							}
						})
						.show();
				} else {
	            	runExport(p, fname, closeAction);
				}
			}
		});
		builder.setNegativeButton(p.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item)  {
				closeAction.run();
			}
		});

		AlertDialog alert = builder.create();
		alert.setOwnerActivity(p);
		alert.show();
	}

	////////////////////

	private static boolean checkExisting(File dest, String fname) {
		if (!dest.exists()) {
    		return false;
    	}
 
		boolean rv = false;
    	//get the zip file content
    	ZipInputStream zis = null;

    	try {
	    	zis = new ZipInputStream(new FileInputStream(fname));
	    	ZipEntry ze;
	    	while ( (ze  = zis.getNextEntry()) != null ) {
	    	   String fileName = ze.getName();
	    	   if (fileName.indexOf('/') >= 0) {
	    		   continue;
	    	   }
	    	   File target = new File(dest, fileName);
	    	   if (target.isDirectory()) {
	    		   continue;
	    	   }
	    	   if (target.exists()) {
	    		   rv = true;
	    		   break;
	    	   }
	    	}
    	} catch (FileNotFoundException e) {
    		Logger.log("checkExisting: exc1", e);
    		rv = false;
    	} catch (IOException e) {
    		Logger.log("checkExisting: exc2", e);
    		rv = false;
		} finally {
			if (zis != null) {
				try {
					zis.closeEntry();
					zis.close();
				} catch (IOException e) {
				}
			}
    	}

    	return rv;
	}
	
	private static int unpackZipFile(File dest, String fname, boolean overwrite) {
		if (!dest.exists()) {
			dest.mkdirs();
    	}
 
		int rv = 0;
		boolean error = false;
    	//get the zip file content
    	ZipInputStream zis = null;
        byte[] bytes = new byte[4096];

    	try {
	    	zis = new ZipInputStream(new FileInputStream(fname));
	    	ZipEntry ze;
	    	while ( (ze  = zis.getNextEntry()) != null ) {
	    		if (ze.isDirectory()) {
	    			continue;
	    		}
	    		String fileName = ze.getName();
	    		int lpos = fileName.lastIndexOf('/');
	    		if (lpos >= 0) {
	    			fileName = fileName.substring(lpos + 1);
	    		}
	    		File target = new File(dest, fileName);
	    		if (target.isDirectory()) {
	    			continue;
	    		}
	    		if (!overwrite && target.exists()) {
	    			continue;
	    		}

	    		FileOutputStream fos = null;

	    		try {
	    			fos = new FileOutputStream(target, false);
	    			int len;
	    			while ((len = zis.read(bytes)) > 0) {
	    				fos.write(bytes, 0, len);
	    			}
	    			rv += 1;
	    		} catch (FileNotFoundException e) {
	    			Logger.log("unpackZipFile: exc1 " + target.getAbsolutePath(), e);
	    			error = true;
	    		} catch (IOException e) {
	    			Logger.log("unpackZipFile: exc2 " + target.getAbsolutePath(), e);
	    			error = true;
	    		} finally {
	    			if (fos != null) {
	    				try {
	    					fos.close();
	    				} catch (IOException e) {
	    				}
	    			}
	           	}
	    	}
    	} catch (FileNotFoundException e) {
    		Logger.log("unpackZipFile: exc3", e);
    		rv = 0;
    	} catch (IOException e) {
    		Logger.log("unpackZipFile: exc4", e);
    		error = true;
		} finally {
			if (zis != null) {
				try {
					zis.closeEntry();
					zis.close();
				} catch (IOException e) {
				}
			}
    	}

    	if (error && rv > 0) {
    		rv = -rv;
    	}
    	return rv;
	}

	private static void runImport2(final MainActivity p, final File dest, final String fname, final boolean overwrite, final Runnable closeAction) {
    	final ProgressDialog waiterDialog = ProgressDialog.show(p, "Importing...", "Importing data from " + fname, true, false);
    	new Thread(new Runnable() { @Override public void run() {
        	final int rv = unpackZipFile(dest, fname, overwrite);
       		p.runOnUiThread(new Runnable() { @Override public void run() {
       			waiterDialog.dismiss();
        		if (rv > 0) {
        			new AlertDialog.Builder(p).setMessage(R.string.ap_impexp_imp_res_ok).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item)  {
							closeAction.run();
						}
					}).show();
        		} else if (rv == 0) {
        			new AlertDialog.Builder(p).setMessage(R.string.ap_impexp_imp_res_nodata).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item)  {
							closeAction.run();
						}
					}).show();
        		} else {
        			new AlertDialog.Builder(p).setMessage(R.string.ap_impexp_imp_res_fail).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item)  {
							closeAction.run();
						}
					}).show();
        		}
       		}});
        }}).start();
	}

	private static void runImport(final MainActivity p, final String fname, final Runnable closeAction) {
		final File dest = new File(Globals.getSavegameDir());
		final ProgressDialog waiterDialog = ProgressDialog.show(p, "Importing...", "Checking file " + fname, true, false);

        new Thread(new Runnable() { @Override public void run() {
        	final boolean existing = checkExisting(dest, fname);
       		p.runOnUiThread(new Runnable() { @Override public void run() {
            	waiterDialog.dismiss();
            	if (existing) {
            		new AlertDialog.Builder(p)
			        .setMessage(R.string.ap_impexp_imp_exists)
			        .setPositiveButton(R.string.ap_impexp_imp_exists_overwrite, new DialogInterface.OnClickListener() {
			            @Override public void onClick(DialogInterface dialog, int which) {
		            		runImport2(p, dest, fname, true, closeAction);
			            }
			        })
			        .setNeutralButton(R.string.ap_impexp_imp_exists_keep, new DialogInterface.OnClickListener() {
			            @Override public void onClick(DialogInterface dialog, int which) {
		            		runImport2(p, dest, fname, false, closeAction);
			            }
			        })
			        .setNegativeButton(R.string.ap_impexp_imp_exists_cancel, new DialogInterface.OnClickListener() {
			            @Override public void onClick(DialogInterface dialog, int which) {
							closeAction.run();
			            }
			        })
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override public void onCancel(DialogInterface arg0) {
							closeAction.run();
						}
					})
					.show();
            	} else {
            		runImport2(p, dest, fname, true, closeAction);
            	}
       		}});
        }}).start();
	}

	public static void doSaveImport(final MainActivity p, final Runnable closeAction) {
		AlertDialog.Builder builder = new AlertDialog.Builder(p);
		builder.setTitle(p.getResources().getString(R.string.ap_impexp_imp_file));

		final EditText edit = new EditText(p);
		edit.setFocusableInTouchMode(true);
		edit.setFocusable(true);
		edit.setText(new File(Environment.getExternalStorageDirectory(), DEFAULT_FILE).getAbsolutePath());
		builder.setView(edit);

		builder.setPositiveButton(p.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item)  {
				final String fname = "" + edit.getText();
				if (!new File(fname).exists()) {
				       new AlertDialog.Builder(p)
				        .setMessage(R.string.ap_impexp_imp_res_nofile)
				        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item)  {
								closeAction.run();
							}
						})
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override public void onCancel(DialogInterface arg0) {
								closeAction.run();
							}
						})
						.show();
				} else {
	            	runImport(p, fname, closeAction);
				}
			}
		});
		builder.setNegativeButton(p.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item)  {
				closeAction.run();
			}
		});

		AlertDialog alert = builder.create();
		alert.setOwnerActivity(p);
		alert.show();
	}
}
