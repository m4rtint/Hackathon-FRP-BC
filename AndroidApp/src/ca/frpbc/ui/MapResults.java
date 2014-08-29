package ca.frpbc.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;

import ca.frpbc.model.PointOfInterest;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapResults extends MapFragment implements SearchActivity.ResultDisplay, OnInfoWindowClickListener {
	
	private Map<Marker, PointOfInterest> markerMap;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		markerMap = new HashMap<Marker, PointOfInterest>();
	}

	@Override
	public void showResults(SearchActivity context, List<PointOfInterest> results) {
		// Listen to the map.
		getMap().setOnInfoWindowClickListener(this);
		
		// Clear the map.
		getMap().clear();

		// Highlight results and add markers that do not already exist.
		for(PointOfInterest poi : results) {
			LatLng pos = new LatLng(poi.getAddress().getLatitude(), poi.getAddress().getLongitude());
			Marker marker = getMap().addMarker(new MarkerOptions().position(pos)
					.title(poi.getName())
					.draggable(false)
					.snippet(poi.getAddress().getAddressString())
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			markerMap.put(marker, poi);
		}
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		PointOfInterest poi = markerMap.get(marker);
				
		Intent intent = new Intent("ca.frpbc.ui.POIDetails");
		intent.putExtra(POIDetails.PARCEL_EXTRA_NAME, poi);
		startActivity(intent);
	}
	
	/**
	 * @return
	 *			A LatLngBounds representing the bounds of all the markers on this map.
	 */
	public LatLngBounds getMarkerBounds() {
		LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
		for (Map.Entry<Marker, PointOfInterest> entry : markerMap.entrySet()) {
			boundsBuilder.include(entry.getKey().getPosition());
		}
		return boundsBuilder.build();
	}

}
