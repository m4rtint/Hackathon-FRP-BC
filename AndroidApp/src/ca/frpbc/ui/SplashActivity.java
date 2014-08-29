package ca.frpbc.ui;

import ca.frpbc.R;
import ca.frpbc.Search;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
 
public class SplashActivity extends Activity {
	
	public static final class FailedToStartDialogFragment extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Build an alert dialog.
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.dialog_fail_to_start_title)
				.setMessage(R.string.dialog_fail_to_start_body)
				.setPositiveButton(R.string.dialog_fail_to_start_close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								getActivity().finish();
							}
						});
			return builder.create();
		}
	}
	
	private SearchInit searchInit; // Keep track of the search initializer so it can be stopped if needed.
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Scale the logo image properly, because XML isn't enough.
     	ImageView logoView = (ImageView)findViewById(R.id.splash_logo);
     	int width = getResources().getDisplayMetrics().widthPixels;
     	int height = getResources().getDisplayMetrics().heightPixels;
     	ImageScaler.scaleImageView(logoView, width < height ? width : height);
     	
     	// Check that the google play services is available.
     	int googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
     	// If it's all good, do our thing.
     	if (googlePlayStatus == ConnectionResult.SUCCESS) {
     		// Initialize the search, and when finished, launch the search activity.
     		searchInit = new SearchInit();
     		searchInit.execute(this);
     	}
     	// If we can recover, display a dialog to try to.
     	else if (GooglePlayServicesUtil.isUserRecoverableError(googlePlayStatus)) {
     		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(googlePlayStatus, this, 0);
     		errorDialog.show();
     	}
     	// If we have hit an unrecoverable error, alert the user.
     	else {
     		DialogFragment failFragment = new FailedToStartDialogFragment();
     		failFragment.show(getFragmentManager(), "dialog");
     	}
    }
    
    // An async task that lets the search initialize off the UI thread.
    private static class SearchInit extends AsyncTask<SplashActivity, Object, SplashActivity> {

		@Override
		protected SplashActivity doInBackground(SplashActivity... args) {
			// Initialize the database.
			Search.getInstance(args[0]);
			return args[0];
		}
		
		@Override
		protected void onPostExecute(SplashActivity result) {
			// Get rid of the reference to this.
			result.searchInit = null;
			
			// When finished, launch the search activity!
			Intent i = new Intent(result, SearchActivity.class);
    		result.startActivity(i);
            result.finish();
		}
    	
    }
    
    @Override
    protected void onDestroy() {
    	// Kill the search initializer if necessary.
    	if (searchInit != null)
    		searchInit.cancel(true);
    	super.onDestroy();
    }
 
}
