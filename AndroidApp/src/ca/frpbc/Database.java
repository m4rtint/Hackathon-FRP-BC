package ca.frpbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.frpbc.model.Agency;
import ca.frpbc.model.Hours;
import ca.frpbc.model.PointOfInterest;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public class Database {
	
	private static final HashMap<Integer, PointOfInterest> poiMap = new HashMap<Integer, PointOfInterest>(); // The points of interest in this database.
	private static final HashMap<Integer, Agency> agencyMap = new HashMap<Integer, Agency>(); // The agencies in this database.
	
	/**
	 * Set the points of interest in this database.
	 * 
	 * @param pois
	 *			The points of interest to be contained by the database.
	 */
	public static void setAllPointsOfInterest(Collection<PointOfInterest> pois) {
		poiMap.clear();
		for (PointOfInterest p : pois) {
			poiMap.put(p.getId(), p);
		}
	}
	
	/**
	 * Set the agencies in this database.
	 * 
	 * @param agencies
	 *			The agencies to be contained by this database.
	 */
	public static void setAllAgencies(Collection<Agency> agencies) {
		agencyMap.clear();
		for (Agency a : agencies) {
			agencyMap.put(a.getId(), a);
		}
	}
	
	/**
	 * Get a point of interest by id.
	 * 
	 * @param poiId
	 * @return
	 *			The point of interest with the given id, or null if none is found.
	 */
	public static PointOfInterest getPointOfInterest(int poiId) {
		return poiMap.get(poiId);
	}
	
	/**
	 * Get an agency by id.
	 * 
	 * @param agencyId
	 * @return
	 *			The agency with the given id, or null if none is found.
	 */
	public static Agency getAgency(int agencyId) {
		return agencyMap.get(agencyId);
	}
	
	/**
	 * Get points of interest based on different requirements. Null parameters are ignored.
	 * 
	 * @param keywords
	 *			Get points of interest with names, agency names, or programs matching any of these.
	 * @param day
	 *			Get points of interest open this day of the week.
	 * @param hours
	 *			Get points of interest open during these hours.
	 * @return
	 *			A list of POIs that satisfy all the given parameters.
	 */
	public static List<PointOfInterest> getPointsOfInterest(String[] keywords, String day, Hours hours) {
		// Make an array with all POIs and then filter out invalid things.
		List<PointOfInterest> out = new ArrayList<PointOfInterest>();
		for (Map.Entry<Integer, PointOfInterest> p : poiMap.entrySet()) {
			out.add(p.getValue());
		}
		
		// First, filter by time.
		if (day != null) {
			filterPointsOfInterestByTime(out, day, hours);
		}
		
		// After, filter by keyword.
		if (keywords != null) {
			filterPointsOfInterestByKeyword(out, keywords);
		}
		
		// Return.
		return out;
	}

	/**
	 * Remove the POIs in pois that either do not have a match in keyword for their name, 
	 * their agency name, or one of their programs.
	 * 
	 * @param pois
	 * @param keyword
	 */
	private static void filterPointsOfInterestByKeyword(List<PointOfInterest> pois, String[] keywords) {
		Iterator<PointOfInterest> iterator = pois.iterator();
		
		// Iterate through with an iterator so we can use remove().
		while (iterator.hasNext()) {
			PointOfInterest poi = iterator.next();
			boolean passes = false; // Keep track of if the poi is valid.
			
			// Test the keywords.
			for (String keyword : keywords) {
				if (poi.hasSearchTerm(keyword)) {
					passes = true;
					break;
				}
			}
			
			// If the poi did not pass, remove it.
			if (!passes) {
				Log.i("john", poi.getName() + " did not match.");
				iterator.remove();
			}
		}
	}

	/**
	 * Remove the POIs in pois that are not open on the given day at the given time.
	 * If no time is given, being open on a given day is all that is required to stay in pois.
	 * 
	 * @param pois
	 * @param day
	 * @param hours
	 */
	private static void filterPointsOfInterestByTime(List<PointOfInterest> pois, String day, Hours hours) {
		Iterator<PointOfInterest> iterator = pois.iterator();
		
		// Iterate through with an iterator so we can use remove().
		while (iterator.hasNext()) {
			PointOfInterest poi = iterator.next();
			
			// Get the hours for the given day.
			Hours[] todayHours;
			if (day.equals(Search.DAYS[0])) {
				todayHours = poi.getMonHours();
			}
			else if (day.equals(Search.DAYS[1])) {
				todayHours = poi.getTuesHours();
			}
			else if (day.equals(Search.DAYS[2])) {
				todayHours = poi.getWedHours();
			} 
			else if (day.equals(Search.DAYS[3])) {
				todayHours = poi.getThursHours();
			}
			else if (day.equals(Search.DAYS[4])) {
				todayHours = poi.getFriHours();
			}
			else if (day.equals(Search.DAYS[5])) {
				todayHours = poi.getSatHours();
			}
			else if (day.equals(Search.DAYS[6])) {
				todayHours = poi.getSunHours();
			}
			// If no day is properly specified, throw an IllegalArgumentException.
			else {
				throw new IllegalArgumentException("day must be an element of DAYS.");
			}
			
			// If todayHours is null, then the poi is closed on day, so remove it.
			if (todayHours == null) {
				iterator.remove();
				continue;
			}
			
			// If only a day was specified, by not hours, then this passes and can remain in the list.
			// However, if hours are specified, we need to check that poi is open during them.
			if (hours != null) {
				// If the first and last elements of todayHours do not enclose hours, remove the poi.
				if (todayHours[0].getOpenTime() > hours.getOpenTime() ||
						todayHours[todayHours.length - 1].getCloseTime() < hours.getCloseTime()) {
					iterator.remove();
				}
			}
		}
	}
	
}
