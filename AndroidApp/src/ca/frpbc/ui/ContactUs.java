package ca.frpbc.ui;

import ca.frpbc.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class ContactUs extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contactus);
		
		// Get the up button to do its thing.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// Scale the banner image properly, because XML isn't enough.
		ImageView bannerView = (ImageView)findViewById(R.id.contactus_banner);
		ImageScaler.scaleImageView(bannerView, getResources().getDisplayMetrics().widthPixels);
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    // Up/Home button.
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void callFRPBC(View view) {
		Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:6047380068"));
	    startActivity(callIntent);
	}
	
	public void websiteFRPBC(View view) {
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.frpbc.ca"));
		startActivity(launchBrowser);
	}
	
	public void emailFRPBC(View view) {
		email("info@frpbc.ca");
	}
	
	/*public void emailExec(View view) {
		email("marianne@frpbc.ca");
	}
	
	public void emailCoordinator(View view) {
		email("debbie@frpbc.ca");
	}*/
	
	private void email(String address) {
		Intent email = new Intent(Intent.ACTION_SEND);
		email.setType("text/plain");
		email.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
		startActivity(Intent.createChooser(email, "Email with"));
	}

}