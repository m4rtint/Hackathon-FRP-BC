package ca.frpbc;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.frpbc.model.Address;
import ca.frpbc.model.Hours;
import ca.frpbc.model.PointOfInterest;

import android.content.Context;
import android.util.Log;

public class Search {
	
	private static Search instance = null;
	
	public static Search getInstance(Context context) {
		if (instance == null) {
			WebParser.parseFRPBC(context);
			instance = new Search();
		}
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;
	}
	
	private static final String JOHN_TAG = "john";
	
	// Time formatting
	private static final String DAY_MATCH = "((mon|tues|thurs|fri|sun)(day)?|tue|thur|wed(nesday)?|sat(urday)?|today|tomorrow)";
	private static final String TIME_MATCH = "(now|([01]?[0-9]|2[0-3])(:[0-5][0-9])?(am|pm)?)";
	private static final String OPEN_MATCH = "open(\\s+" + DAY_MATCH + ")?((\\s+(from|at))?\\s+" + TIME_MATCH + "((\\s+to\\s+|\\s*\\-\\s*)" + TIME_MATCH + ")?)?";
	
	private static final String MONDAY_MATCH = "monday|mon";
	private static final String TUESDAY_MATCH = "tuesday|tue(s)?";
	private static final String WEDNESDAY_MATCH = "wednesday|wed";
	private static final String THURSDAY_MATCH = "thursday|thur(s)?";
	private static final String FRIDAY_MATCH = "friday|fri";
	private static final String SATURDAY_MATCH = "saturday|sat";
	private static final String SUNDAY_MATCH = "sunday|sun";
	private static final String TODAY = "today";
	private static final String TOMORROW = "tomorrow";
	
	public static final String[] DAYS = { "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" };
	
	// Location
	private static final String NEAR = "near";
	
	private Pattern whitespacePattern; // Pattern to split whitespace.
	private Matcher timeMatcher; // Matches time strings.
	private Matcher openMatcher; // Matches operating hours queries.
	private Matcher mondayMatcher;
	private Matcher tuesdayMatcher;
	private Matcher wednesdayMatcher;
	private Matcher thursdayMatcher;
	private Matcher fridayMatcher;
	private Matcher saturdayMatcher;
	private Matcher sundayMatcher;
	
	// Data from the latest query.
	private List<PointOfInterest> latestSearchResults;
	private Address latestSearchAddress;
	
	/**
	 * Make a Search object.
	 * 
	 * @param context
	 * @param database
	 */
	public Search() {
		whitespacePattern = Pattern.compile("\\s+");
		timeMatcher = Pattern.compile(TIME_MATCH).matcher("");
		openMatcher = Pattern.compile(OPEN_MATCH).matcher("");
		mondayMatcher = Pattern.compile(MONDAY_MATCH).matcher("");
		tuesdayMatcher = Pattern.compile(TUESDAY_MATCH).matcher("");
		wednesdayMatcher = Pattern.compile(WEDNESDAY_MATCH).matcher("");
		thursdayMatcher = Pattern.compile(THURSDAY_MATCH).matcher("");
		fridayMatcher = Pattern.compile(FRIDAY_MATCH).matcher("");
		saturdayMatcher = Pattern.compile(SATURDAY_MATCH).matcher("");
		sundayMatcher = Pattern.compile(SUNDAY_MATCH).matcher("");
	}

	/**
	 * @return
	 *			The most recent search results.
	 */
	public List<PointOfInterest> getLatestSearchResults() {
		return latestSearchResults;
	}
	
	/**
	 * @return
	 *			The most recent address that was searched near.
	 */
	public Address getLatestSearchAddress() {
		return latestSearchAddress;
	}
	
	/**
	 * Break down a search string from the search bar and place the results in
	 * lastestSearchResults.
	 * 
	 * @param searchString
	 */
	public void search(String searchString) {
		Log.i(JOHN_TAG, "Searching for " + searchString);
		
		// Trim the string and convert it to lower case.
		StringBuilder searchBuilder = new StringBuilder(searchString.trim().toLowerCase(Locale.US));
		
		// Identify the parts of the query.
		String[] keywords = null;
		String day = null;
		Hours hours = null;
		Address location = null;
		
		// Get the open time.
		openMatcher.reset(searchBuilder);
		if (openMatcher.find()) {
			// Get and remove the string from the builder.
			String openString = openMatcher.group();
			searchBuilder.delete(openMatcher.start(), openMatcher.end());
			
			// Get the day out of openString.
			day = extractDayFromOpenString(openString);
			Log.i(JOHN_TAG, openString + " -> " + day);
			
			// If no day is found or today was requested, get today.
			if (day == null || day.equals(TODAY)) {
				day = getToday();
			}
			// If tomorrow was requested, get tomorrow.
			else if (day.equals(TOMORROW)) {
				day = getTomorrow();
			}
			
			Log.i(JOHN_TAG, "Day: " + day);
			
			// Get up to two times out of openString.
			timeMatcher.reset(openString);
			if (timeMatcher.find()) {
				int openTime = getTimeFromTimeString(timeMatcher.group());
				Log.i(JOHN_TAG, "Open time: " + openTime);
				if (timeMatcher.find()) {
					int closeTime = getTimeFromTimeString(timeMatcher.group());
					Log.i(JOHN_TAG, "Close time: " + closeTime);
					hours = new Hours(openTime, closeTime);
				}
				else {
					hours = new Hours(openTime, openTime);
				}
			}
		}
		
		// Get the near address.
		// Essentially, the near portion of the query is everything after the word near if it exists.
		int nearIndex = searchBuilder.indexOf(NEAR);
		if (nearIndex != -1) {
			int nearQueryIndex = nearIndex + NEAR.length();
			// If there's a valid query, grab it.
			if (nearQueryIndex < searchBuilder.length()) {
				String nearQuery = searchBuilder.substring(nearQueryIndex).trim();
				location = new Address(nearQuery);
			}
			// Remove near and everything after it from the search builder.
			searchBuilder.delete(nearIndex, searchBuilder.length());
		}
		
		// Treat the rest of the query as a sequence of keywords.
		String remaining = searchBuilder.toString().trim();
		if (remaining.length() > 0) {
			keywords = whitespacePattern.split(remaining);
		}
		
		// Query the server with the info.
		queryDatabase(keywords, day, hours, location);
	}
	
	// Query the database. Currently does not take into account the given location.
	private void queryDatabase(String[] keywords, String day, Hours hours, Address location) {
		Log.i(JOHN_TAG, "Querying the database.");
		if (keywords != null)
			Log.i(JOHN_TAG, "Keywords: " + Arrays.toString(keywords));
		if (day != null)
			Log.i(JOHN_TAG, "Day: " + day);
		if (hours != null)
			Log.i(JOHN_TAG, "Hours: " + hours);
		if (location != null)
			Log.i(JOHN_TAG, "Location: " + location.getAddressString());
		
		// If we have an address, fill in its latitude and longitude with the geocoder.
		// If the geocoder fails, get rid of the address.
		/*if (location != null && location.getLatitude() == Double.MIN_VALUE) {
			try {
				Geocoder geocoder = new Geocoder(context);
				List<android.location.Address> geoAddresses = geocoder.getFromLocationName(location.getAddressString(), 5);
				
				// If we found potential matches, go with the first one.
				if (geoAddresses != null && geoAddresses.size() > 0) {
					android.location.Address geoAddress = geoAddresses.get(0);
					location.setLatitude(geoAddress.getLatitude());
					location.setLongitude(geoAddress.getLongitude());
					Log.i(JOHN_TAG, "Location geocoding accomplished: " + location.getLatitude() + ", " + location.getLongitude() + ".");
				}
				// If no match was found, just return.
				else {
					Log.i(JOHN_TAG, "Failed to match " + location.getAddressString() + " to a LatLon.");
					return;
				}
			}
			catch (IOException e) {
				Log.i(JOHN_TAG, "Failed to geocode: IOException.");
				return;
			}
		}
		
		if (location != null)
			Log.i(JOHN_TAG, "Geocoding complete.");
		
		// If we have a location, center the map on it.
		if (location != null) {
			...
		}*/
		
		// Keep track of the search terms.
		latestSearchAddress = location;
		
		// Perform a database query.
		latestSearchResults = Database.getPointsOfInterest(keywords, day, hours);
	}
	
	/**
	 * @return
	 *			The current time as an int in 24 hour format.
	 */
	private int getCurrentTime() {
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		return hour * 100 + minute;
	}
	
	/**
	 * @return
	 *			The day of the week.
	 */
	private String getToday() {
		return getStringFromCalendarDayOfWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
	}
	
	/**
	 * @return
	 *			The day of the week of tomorrow.
	 */
	private String getTomorrow() {
		return getStringFromCalendarDayOfWeek((Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1) % 7);
	}
	
	/**
	 * Convert a day from Calendar (such as Calendar.MONDAY) to its string equivalent.
	 */
	private String getStringFromCalendarDayOfWeek(int day) {
		switch (day) {
		case Calendar.MONDAY: return DAYS[0];
		case Calendar.TUESDAY: return DAYS[1];
		case Calendar.WEDNESDAY: return DAYS[2]; 
		case Calendar.THURSDAY: return DAYS[3]; 
		case Calendar.FRIDAY: return DAYS[4]; 
		case Calendar.SATURDAY: return DAYS[5]; 
		case Calendar.SUNDAY: return DAYS[6];
		default: return null;
		}
	}
	
	/**
	 * Get the specified day from the open string.
	 * 
	 * TODO redo with one regex
	 * 
	 * @param openString
	 * @return
	 */
	private String extractDayFromOpenString(String openString) {
		mondayMatcher.reset(openString);
		if (mondayMatcher.find())
			return DAYS[0];
		
		tuesdayMatcher.reset(openString);
		if (tuesdayMatcher.find())
			return DAYS[1];
		
		wednesdayMatcher.reset(openString);
		if (wednesdayMatcher.find())
			return DAYS[2];
		
		thursdayMatcher.reset(openString);
		if (thursdayMatcher.find())
			return DAYS[3];
		
		fridayMatcher.reset(openString);
		if (fridayMatcher.find())
			return DAYS[4];
		
		saturdayMatcher.reset(openString);
		if (saturdayMatcher.find())
			return DAYS[5];
		
		sundayMatcher.reset(openString);
		if (sundayMatcher.find())
			return DAYS[6];
		
		if (openString.indexOf(TODAY) != -1)
			return TODAY;
		
		if (openString.indexOf(TOMORROW) != -1)
			return TOMORROW;
		
		return null;
	}

	/**
	 * Convert a time string (a string that matches TIME_MATCH) into an int.
	 * 
	 * @param timeString
	 *			The time string.
	 * @return
	 *			An int representing the time string in 24 hour time.
	 */
	private int getTimeFromTimeString(String timeString) {
		// If the time is now, return the current time.
		if (timeString.equals("now"))
			return getCurrentTime();
		
		int pmBonus = 0;
		// If the string ends in am, remove the am and ignore it.
		if (timeString.endsWith("am"))
			timeString = timeString.substring(0, timeString.length() - 2);
		// If the string ends in pm, remove the pm and add 12 to the hours.
		if (timeString.endsWith("pm")) {
			timeString = timeString.substring(0, timeString.length() - 2);
			pmBonus = 1200;
		}
		
		// If there is a colon, look at minutes and hours.
		int colonIndex = timeString.indexOf(':');
		if (colonIndex > -1) {
			String hourString = timeString.substring(0, colonIndex);
			String minuteString = timeString.substring(colonIndex + 1);
			int hour = Integer.parseInt(hourString) * 100;
			int minutes = Integer.parseInt(minuteString);
			hour += pmBonus;
			return (hour + minutes) % 2400;
		}
		
		// Otherwise, treat it as an hour.
		int hour = Integer.parseInt(timeString) * 100;
		return (hour + pmBonus) % 2400;
	}

}
