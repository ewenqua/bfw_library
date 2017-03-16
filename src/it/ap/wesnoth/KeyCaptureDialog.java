package it.ap.wesnoth;

import it.alessandropira.wesnoth112.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class KeyCaptureDialog extends Dialog {
	private int keyCode;
	private boolean clearSelected;
	private boolean clearVisible;

	public KeyCaptureDialog(Context context, String title, boolean clearVisible) {
		super(context);
		setTitle(title);
		this.clearVisible = clearVisible;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.key_capture);
		keyCode = 0;
		clearSelected = false;
		
		Button cancel = (Button)findViewById(R.id.button_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		Button clear = (Button)findViewById(R.id.button_clear);
		clear.setVisibility(clearVisible ? View.VISIBLE : View.GONE);
		clear.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				clearSelected = true;
				dismiss();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		this.keyCode = keyCode;
		dismiss();
		return true;
	}

	public interface KeyCaptureCallback {
		void onKeySelected(int keyCode);
		void onClear();
		void onCancel();
	}
	public static void execute(Context ctx, boolean canClear, final KeyCaptureCallback cb) {
		final KeyCaptureDialog d = new KeyCaptureDialog(ctx, ctx.getResources().getString(R.string.ap_key_capture_dialog_title), canClear);
		d.setOnDismissListener(new OnDismissListener() {
			@Override public void onDismiss(DialogInterface dialog) {
				if (cb != null && d.keyCode != 0) {
					if (d.keyCode == 0) {
						if (d.clearSelected) {
							cb.onClear();
						} else {
							cb.onCancel();
						}
					} else {
						cb.onKeySelected(d.keyCode);
					}
				}
			}
		});
		d.show();
	}
}
