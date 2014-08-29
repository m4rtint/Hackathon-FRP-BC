package ca.frpbc.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.frpbc.R;
import ca.frpbc.Search;
import ca.frpbc.model.PointOfInterest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLngBounds;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

public class SearchActivity extends Activity {
	
	/**
	 * An interface that lets this activity set search results easily.
	 */
	public static interface ResultDisplay {
		void showResults(SearchActivity context, List<PointOfInterest> results);
	}
	
	private boolean allMarkersVisible;
	private String lastSearchQuery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		// Handle the intent.
		handleIntent();
		
		// Adjust the map results to display the markers on the screen.
		MapResults map = ((MapResults)getFragmentManager().findFragmentById(R.id.mapResults));
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int size = (displayMetrics.widthPixels < displayMetrics.heightPixels)? displayMetrics.widthPixels : displayMetrics.heightPixels;
		
		LatLngBounds bounds = map.getMarkerBounds();
		map.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, size, size, 8));
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent();
	}
	
	private void handleIntent() {
		// Get the intent. If it's a search intent, get the query and search.
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	String query = intent.getStringExtra(SearchManager.QUERY);
	    	executeSearch(query);
	    }
	    // Otherwise, just display all the markers.
	    else {
	    	executeSearch("");
	    }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("lastSearch", lastSearchQuery);
		
		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		final String creationDate = df.format(Calendar.getInstance().getTime());
		outState.putString("creationDate", creationDate);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		final String creationDate = savedInstanceState.getString("creationDate");
		if (creationDate != null) {
			final Calendar yesterday = Calendar.getInstance();
			yesterday.add(Calendar.DAY_OF_MONTH, -1);
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
			try {
				final Date crDate = df.parse(creationDate);
				if (crDate.before(yesterday.getTime())) {
					Search.resetInstance();
					Intent i = new Intent(this, SplashActivity.class);
		    		startActivity(i);
		            finish();
		            return;
				}
			}
			catch (ParseException e) {
				Log.e("SearchActivity", "failed to parse creation time", e);
			}
		}
		
		Log.i("SearchActivity", "data is still good");
		lastSearchQuery = savedInstanceState.getString("lastSearch");
		if (lastSearchQuery != null)
			executeSearch(lastSearchQuery);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.search_activity_actions, menu);
	    
	    // Get the SearchView and set the searchable configuration.
	    SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setIconifiedByDefault(false);
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Show/hide the clear search button as needed.
		if (allMarkersVisible) 
			menu.findItem(R.id.action_undo_search).setVisible(false);
		else 
			menu.findItem(R.id.action_undo_search).setVisible(true);
		
		// Collapse the search action view, as whenever this is called it should be closed.
		menu.findItem(R.id.action_search).collapseActionView();
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    // Clear search.
	    case R.id.action_undo_search:
	    	executeSearch("");
	    	break;
	    // Contact us.
	    case R.id.action_contactus:
	    	Intent intent = new Intent(this, ContactUs.class);
			startActivity(intent);
	    	break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Search for a string and display the results.
	 * 
	 * @param query
	 */
	private void executeSearch(String query) {
		lastSearchQuery = query;
		// If the query is empty, hide the clear search button.
		if (query.length() == 0) {
			allMarkersVisible = true;
			getActionBar().setTitle(R.string.app_name);
		}
		// Otherwise, show it.
		else {
			allMarkersVisible = false;
			getActionBar().setTitle(query);
		}
		invalidateOptionsMenu();
		
		// Make and execute a search.
		Search.getInstance(this).search(query);
		List<PointOfInterest> results = Search.getInstance(this).getLatestSearchResults();

		// Show the results on the map.
		ResultDisplay mapResults = (ResultDisplay)getFragmentManager().findFragmentById(R.id.mapResults);
		mapResults.showResults(this, results);
		
		// Show the results in the list.
		//ResultDisplay listResults = (ResultDisplay)getFragmentManager().findFragmentById(R.id.listResults);
		//listResults.showResults(this, results);
	}
	
}
