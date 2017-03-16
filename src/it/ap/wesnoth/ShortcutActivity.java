package it.ap.wesnoth;

import it.alessandropira.wesnoth112.R;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ShortcutActivity extends Activity {
	private static final String PARAM_NAME = "SHORTCUT_ACTION";

	public static enum Action {
		OPEN_LAST(R.string.ap_shortcut_open_last_save),
		SERVER(R.string.ap_shortcut_server),
		CAMPAIGN(R.string.ap_shortcut_campaign),
		MAP_EDITOR(R.string.ap_shortcut_map_editor);

		private int resId;
		private Action(int resId) {
			this.resId = resId;
		}
		public String getText(Resources res) {
			return res.getString(resId);
		}
	}

	public static final void decode(Intent intent) {
		if (intent == null) {
			return;
		}

		Bundle extras = intent.getExtras();
		if (extras == null) {
			return;
		}

		if (!extras.containsKey(PARAM_NAME)) {
			return;
		}

		String param = extras.getString(PARAM_NAME);
		
		Action action = Action.valueOf(param);
		Globals.setStartupAction(action);
	}

	private Intent getShortcutIntent(Action action) {
        Intent shortcutIntent = new Intent(this, MainActivity.class);
        Intent.ShortcutIconResource iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.icon);
        shortcutIntent.putExtra(PARAM_NAME, action.name());

        // The result we are passing back from this activity
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, action.getText(getResources()));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        
        return intent;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.shortcut_conf);

    	Button btnLast = (Button)findViewById(R.id.btn_shortcut_open_last_save);
    	Button btnServ = (Button)findViewById(R.id.btn_shortcut_server);
    	Button btnCamp = (Button)findViewById(R.id.btn_shortcut_campaign);
    	Button btnMapEd = (Button)findViewById(R.id.btn_shortcut_map_editor);

    	btnLast.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) {
            setResult(RESULT_OK, getShortcutIntent(Action.OPEN_LAST));
            finish();
		}});
    	btnServ.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) {
            setResult(RESULT_OK, getShortcutIntent(Action.SERVER));
            finish();
		}});
    	btnCamp.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) {
            setResult(RESULT_OK, getShortcutIntent(Action.CAMPAIGN));
            finish();
		}});
    	btnMapEd.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) {
            setResult(RESULT_OK, getShortcutIntent(Action.MAP_EDITOR));
            finish();
		}});
    }
}
