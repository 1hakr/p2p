/*
 * Copyright (C) 2014 Hari Krishna Dulipudi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.p2p;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.p2p.misc.Utils;

public class AboutActivity extends ActionBarActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		TextView logo = (TextView)findViewById(R.id.logo);
        String header = logo.getText() + getSuffix() + " v" + BuildConfig.VERSION_NAME;
		logo.setText(header);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about, menu);
        if(isNonPlay()){
            menu.removeItem(R.id.action_rate);
            menu.removeItem(R.id.action_support);
        }
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.action_github:
			startActivity(new Intent("android.intent.action.VIEW",
					Uri.parse("https://github.com/DWorkS")));
			break;
		case R.id.action_gplus:
			startActivity(new Intent("android.intent.action.VIEW",
					Uri.parse("https://plus.google.com/+HariKrishnaDulipudi")));
			break;
		case R.id.action_twitter:
			startActivity(new Intent("android.intent.action.VIEW",
					Uri.parse("https://twitter.com/1HaKr")));
			break;
		case R.id.action_feedback:
			Intent intentFeedback = new Intent("android.intent.action.SEND");
			intentFeedback.setType("text/email");
			intentFeedback.putExtra("android.intent.extra.EMAIL", new String[]{"hakr@dworks.in"});
			intentFeedback.putExtra("android.intent.extra.SUBJECT", "AnExplorer Feedback" + getSuffix());
			startActivity(Intent.createChooser(intentFeedback, "Send Feedback"));
			break;
		case R.id.action_rate:
			Intent intentMarket = new Intent("android.intent.action.VIEW");
			intentMarket.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
			startActivity(intentMarket);
			break;
		case R.id.action_support:
			Intent intentMarketAll = new Intent("android.intent.action.VIEW");
			intentMarketAll.setData(Uri.parse("market://search?q=pub:D WorkS"));
			startActivity(intentMarketAll);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void startActivity(Intent intent) {
        if(Utils.isIntentAvailable(this, intent)) {
            super.startActivity(intent);
        }
    }

    private String getSuffix(){
        return BuildConfig.FLAVOR.contains("Pro") ? " Pro" : "";
    }

    private boolean isNonPlay(){
        return BuildConfig.FLAVOR.contains("other");
    }
}