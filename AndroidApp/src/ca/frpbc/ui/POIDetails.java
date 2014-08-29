package ca.frpbc.ui;

import ca.frpbc.R;
import ca.frpbc.model.PointOfInterest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class POIDetails extends Activity {
	
	public static final String PARCEL_EXTRA_NAME = "POI";
	
	private PointOfInterest pointOfInterest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_poi_info);
		
		// Get the up button to do its thing.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// Get the POI from the intent and display it.
		Intent intent = getIntent();
		PointOfInterest poi = (PointOfInterest)intent.getParcelableExtra(PARCEL_EXTRA_NAME);
		displayPointOfInterest(poi);
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    // Up/Home button.
	    case android.R.id.home:
	    	finish(); // A hacky, but effective way to make sure that up acts like back.
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void displayPointOfInterest(PointOfInterest poi) {
		pointOfInterest = poi;
		
		setTextViewText(R.id.poi_info_name, poi.getName());
		setTextViewText(R.id.poi_info_agency, poi.getAgency().getName());
		setTextViewText(R.id.poi_info_address, poi.getAddress().getAddressString());
		
		// Display the programs.
		StringBuilder builder = new StringBuilder();
		String[] programs = poi.getPrograms();
		if (programs == null) {
			LinearLayout layout = (LinearLayout)findViewById(R.id.poi_info_details);
			View view = layout.findViewById(R.id.poi_info_programs_title);
			layout.removeView(view);
			view = layout.findViewById(R.id.poi_info_programs);
			layout.removeView(view);
		}
		else {
			for (String s : programs) {
				builder.append(s);
				builder.append("\n");
			}
			setTextViewText(R.id.poi_info_programs, builder.toString().trim());
		}
		
		// Display the hours.
		builder.delete(0, builder.length());
		if (poi.getMonHours() != null) {
			builder.append("Mon: ");
			appendArray(builder, poi.getMonHours());
		}
		if (poi.getTuesHours() != null) {
			builder.append("Tues: ");
			appendArray(builder, poi.getTuesHours());
		}
		if (poi.getWedHours() != null) {
			builder.append("Wed: ");
			appendArray(builder, poi.getWedHours());
		}
		if (poi.getThursHours() != null) {
			builder.append("Thurs: ");
			appendArray(builder, poi.getThursHours());
		}
		if (poi.getFriHours() != null) {
			builder.append("Fri: ");
			appendArray(builder, poi.getFriHours());
		}
		if (poi.getSatHours() != null) {
			builder.append("Sat: ");
			appendArray(builder, poi.getSatHours());
		}
		if (poi.getSunHours() != null) {
			builder.append("Sun: ");
			appendArray(builder, poi.getSunHours());
		}
		String hours = builder.toString().trim();
		if (hours.length() == 0) {
			LinearLayout layout = (LinearLayout)findViewById(R.id.poi_info_details);
			View view = layout.findViewById(R.id.poi_info_hours_title);
			layout.removeView(view);
			view = layout.findViewById(R.id.poi_info_hours);
			layout.removeView(view);
		}
		else {
			setTextViewText(R.id.poi_info_hours, hours);
		}
		
		// Display the accreditation.
		String accreditation = poi.getAccreditation();
		if (accreditation == null) {
			LinearLayout layout = (LinearLayout)findViewById(R.id.poi_info_details);
			View view = layout.findViewById(R.id.poi_info_accreditation_title);
			layout.removeView(view);
			view = layout.findViewById(R.id.poi_info_accreditation_description);
			layout.removeView(view);
		}
		else {
			final int accredStart = poi.getAgency().getAccredStartYear();
			final int accredRenew = poi.getAgency().getAccredRenewYear();
			if (accredStart != -1 && accredRenew != -1)
				setTextViewText(R.id.poi_info_accreditation_description, accredStart + " - " + accredRenew + "\n" + accreditation);
			else
				setTextViewText(R.id.poi_info_accreditation_description, accreditation);
		}
		
		// Give the action buttons functionality, or, if they are unneeded, remove them.
		GridLayout grid = (GridLayout)findViewById(R.id.poi_info_action_grid);
		
		// Call button.
		View callButton = grid.findViewById(R.id.poi_info_call_button);
		if (poi.getPhoneNumber() == null) {
			grid.removeView(callButton);
		}
		else {
			callButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent callIntent = new Intent(Intent.ACTION_DIAL, pointOfInterest.getPhoneNumber());
				    startActivity(callIntent);
				}
				
			});
		}
		
		// Website button.
		View websiteButton = grid.findViewById(R.id.poi_info_website_button);
		if (poi.getWebsite() == null) {
			grid.removeView(websiteButton);
		}
		else {
			websiteButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent launchBrowser = new Intent(Intent.ACTION_VIEW, pointOfInterest.getWebsite());
					startActivity(launchBrowser);
				}
				
			});
		}
		
		// Route button.
		View routeButton = grid.findViewById(R.id.poi_info_directions_button);
		routeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				double lat = pointOfInterest.getAddress().getLatitude();
				double lon = pointOfInterest.getAddress().getLongitude();
				String markerLabel = pointOfInterest.getName().replaceAll("&", "and");
				Intent geoIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("geo:" + lat + "," + lon + "?q=" + lat + "," + lon + "(" + markerLabel + ")"));
				startActivity(geoIntent);
			}
			
		});
		
		// Share button.
		View shareButton = grid.findViewById(R.id.poi_info_share_button);
		shareButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Create the string to represent the POI.
				StringBuilder poiData = new StringBuilder();
				poiData.append(pointOfInterest.getName());
				poiData.append("\n\n");
				poiData.append(pointOfInterest.getAddress().getAddressString());
				if (pointOfInterest.getPhoneNumber() != null) {
					poiData.append("\n\n");
					poiData.append(pointOfInterest.getPhoneNumber());
				}
				if (pointOfInterest.getWebsite() != null) {
					poiData.append("\n\n");
					poiData.append(pointOfInterest.getWebsite());
				}
				
				// Send the string.
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, poiData.toString());
				startActivity(Intent.createChooser(shareIntent, "Share this location"));
			}
			
		}); 
	}

	private void setTextViewText(int tVID, String text) {
		TextView textView = (TextView)findViewById(tVID);
		textView.setText(text);
	}
	
	private void appendArray(StringBuilder builder, Object[] array) {
		if (array.length == 1) {
			builder.append(array[0]);
		}
		else {
			for (int i = 0; i < array.length - 1; i++) {
				builder.append(array[i]);
				builder.append(", ");
			}
			builder.append(array[array.length - 1]);
		}
		builder.append("\n");
	}

}