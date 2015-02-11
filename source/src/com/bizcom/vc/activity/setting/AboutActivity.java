package com.bizcom.vc.activity.setting;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.bizcom.vc.application.GlobalConfig;
import com.v2tech.R;

public class AboutActivity extends Activity {

	private TextView mVersion;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		mVersion = (TextView)findViewById(R.id.about_version);
		mVersion.setText(GlobalConfig.GLOBAL_VERSION_NAME);
		this.overridePendingTransition(R.animator.alpha_from_0_to_1, R.animator.alpha_from_1_to_0);
	}
	@Override
	public void finish() {
		super.finish();
		this.overridePendingTransition(R.animator.alpha_from_0_to_1, R.animator.alpha_from_1_to_0);
	}
}