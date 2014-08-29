package ca.frpbc;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.frpbc.model.Address;
import ca.frpbc.model.Agency;
import ca.frpbc.model.Hours;
import ca.frpbc.model.PointOfInterest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public class WebParser {
	
	private static int getIntFrom(JSONObject obj, String key) {
		try {
			final Integer out = (Integer) obj.get(key);
			if (out != null)
				return out.intValue();
			return Integer.MIN_VALUE;
		}
		catch (JSONException e) {
			return Integer.MIN_VALUE;
		}
		catch (ClassCastException e) {
			return Integer.MIN_VALUE;
		}
	}
	
	private static String getStringFrom(JSONObject obj, String key) {
		try {
			final String out = (String) obj.get(key);
			if (out != null)
				return out.trim();
			return null;
		}
		catch (JSONException e) {
			return null;
		}
		catch (ClassCastException e) {
			return null;
		}
	}
	
	private static double parseDouble(String raw) {
		if (raw == null)
			return Double.MIN_VALUE;
		try {
			final double d = Double.parseDouble(raw);
			return d;
		}
		catch (NumberFormatException e) {
			return Double.MIN_VALUE;
		}
	}
	
	/*
	 * Read an input stream in as a StringBuilder.
	 */
	private static StringBuilder readInputStream(InputStream in) {
		// Declare a reader and an output.
		Reader reader = null;
		StringBuilder out = null;
		
		// Use the reader to read the input stream into the output.
		try {
			reader = new InputStreamReader(in);
			out = new StringBuilder();
			int c = reader.read();
			while (c != -1) {
				out.append((char) c);
				c = reader.read();
			}
			return out;
		}
		// If we fail while reading, return null.
		catch (IOException e) {
			return null;
		}
		// After we read, or fail to, try to close the reader before we return.
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					// If we fail to close, whatever, worse things have happened.
					// Failing to close will still return out.
				}
			}
		}
	}
	
	/*
	 * Read data from the cache.
	 */
	private static StringBuilder readFromCache(Context context, String filePath) {
		InputStream in = null;
		try {
			in = context.openFileInput(filePath);
			Log.i("WebParser", "Read file from " + filePath);
			return readInputStream(in);
		}
		catch (FileNotFoundException e) {
			Log.i("WebParser", "Failed to retrieve file from " + filePath);
			return null;
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (IOException e) {
				Log.e("WebParser", "Failed to close stream for " + filePath, e);
			}
		}
	}
	
	/*
	 * Cache some data. Does not timestamp it.
	 */
	private static void saveToCache(Context context, String filePath, String data) {
		FileOutputStream out = null;
		try {
			out = context.openFileOutput(filePath, Context.MODE_PRIVATE);
			out.write(data.getBytes());
			Log.i("WebParser", "Saved data to cache at " + filePath);
		}
		catch (FileNotFoundException e) {
			Log.e("WebParser", "Couldn't find " + filePath, e);
		}
		catch (IOException e) {
			Log.e("WebParser", "Failed to write data to " + filePath, e);
		}
		finally {
			try {
				if (out != null)
					out.close();
			}
			catch (IOException e) {
				Log.e("WebParser", "Failed to close stream for " + filePath, e);
			}
		}
	}
	
	/*
	 * Try an open a connection, but just return null on a failure.
	 */
	private static HttpURLConnection openConnection(java.net.URL url) {
		try {
			final HttpURLConnection out = (HttpURLConnection) url.openConnection();
			return out;
		}
		catch (IOException e) {
			Log.e("WebParser", "openConnection didn't work for " + url, e);
			return null;
		}
	}
	
	/*
	 * Open a connection, read it, and return the result as a string.
	 */
	private static String readFromURL(java.net.URL url) {
		HttpURLConnection urlConnection = openConnection(url);
		if (urlConnection == null)
			return null;
		
		try {
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			String out = readInputStream(in).toString();
			Log.i("WebParser", "Read data from " + url);
			return out;
		}
		catch (IOException e) {
			Log.e("WebParser", "readFromURL didn't work for " + url, e);
			return null;
		}
		finally {
			urlConnection.disconnect();
		}
	}
	
	/*
	 * Read the timestamp of a cached file.
	 */
	private static Date readCacheTimestamp(Context context, String cacheFilePath) {
		final StringBuilder cachedTimeString = readFromCache(context, cacheFilePath + ".timestamp");
		if (cachedTimeString != null) {
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
			try {
				return df.parse(cachedTimeString.toString());
			}
			catch (ParseException e) {
				Log.e("WebParser", "failed to parse cached time for " + cacheFilePath, e);
			}
		}
		return null;
	}
	
	/*
	 * Read from the cache if it isn't expired or there isn't internet; download otherwise.
	 */
	private static String readSmart(Context context, java.net.URL url, String cacheFilePath) {
		final Date cacheTimestamp = readCacheTimestamp(context, cacheFilePath);
		final Calendar currentCal = Calendar.getInstance();
		
		if (cacheTimestamp != null)
			Log.i("WebParser", "Cached file " + cacheFilePath + " expires at " + cacheTimestamp.toString());
		
		// Use the cache if it isn't expired or if there isn't an internet connection.
		if (cacheTimestamp != null && cacheTimestamp.after(currentCal.getTime()) ||
			    ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() == null) {
			final StringBuilder cached = readFromCache(context, cacheFilePath);
			if (cached != null)
				return cached.toString();
		}
		
		// If the cache is expired and there is an internet connection, download new data.
		// The new data is save to the cache and timestamped.
		final String fromWeb = readFromURL(url);
		if (fromWeb != null) {
			saveToCache(context, cacheFilePath, fromWeb);
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
			currentCal.add(Calendar.DAY_OF_MONTH, 1);
			saveToCache(context, cacheFilePath + ".timestamp", df.format(currentCal.getTime()));
		}
		
		return fromWeb;
	}
	
	/*
	 * Parse several raw strings to form a database.
	 */
	private static void parse(String rawAgencies, String rawHours, String rawLocations) {
		try {
			// Parse the agencies.
			final JSONArray agencyArray = (JSONArray) new JSONTokener(rawAgencies).nextValue();
			final HashMap<Integer, Agency> agencies = new HashMap<Integer, Agency>();
			final Matcher intMatcher = Pattern.compile("\\d+").matcher("");
			
			for (int i = 0; i < agencyArray.length(); i++) {
				final JSONObject obj0 = agencyArray.getJSONObject(i);
				final JSONObject obj = obj0.getJSONObject("fields");
				final int id = obj0.getInt("pk");
				final String name = getStringFrom(obj, "agency");
				final Agency agency = new Agency(id, name);
				
				agency.setPhoneNumber(getStringFrom(obj, "phone"));
				final String rawWebsite = getStringFrom(obj, "website");
				if (rawWebsite != null && rawWebsite.length() > 0)
					agency.setWebsite(java.net.URI.create(rawWebsite));
				
				final String rawAccred = getStringFrom(obj, "accredited");
				if (rawAccred != null) {
					if (rawAccred.equals("carf"))
						agency.setAccreditation("Commission on Accreditation of Rehabilitation Facilities (CARF)"); // http://www.carf.org/home/
					else if (rawAccred.equals("accredit_can"))
						agency.setAccreditation("Accreditation Canada"); // http://www.accreditation.ca/
					else if (rawAccred.equals("coa"))
						agency.setAccreditation("Council on Accreditation (COA)"); // http://coanet.org/
				}
				
				if (agency.getAccreditation() != null) {
					final String rawAccredStart = getStringFrom(obj, "standards_beginning_year");
					final String rawAccredRenew = getStringFrom(obj, "standards_renewal_year");
					
					if (rawAccredStart != null && rawAccredRenew != null && 
							intMatcher.reset(rawAccredStart).matches() &&
							intMatcher.reset(rawAccredRenew).matches()) {
						agency.setAccreditationPeriod(Integer.parseInt(rawAccredStart), Integer.parseInt(rawAccredRenew));
					}
				}
				
				agencies.put(id, agency);
			}
			
			// Parse the hours.
			final JSONArray hoursArray = (JSONArray) new JSONTokener(rawHours).nextValue();
			// A map of locPK -> Map<Day, Hourses>, aka locPK -> week
			final HashMap<Integer, HashMap<String, ArrayList<Hours>>> hours = new HashMap<Integer, HashMap<String, ArrayList<Hours>>>();
			
			for (int i = 0; i < hoursArray.length(); i++) {
				final JSONObject obj0 = hoursArray.getJSONObject(i);
				final JSONObject obj = obj0.getJSONObject("fields");
				final int pk = obj0.getInt("pk");
				final int id = getIntFrom(obj, "location");
				if (id == Integer.MIN_VALUE) {
					Log.w("WebParser", "Failed to get location id for hours " + pk);
					continue;
				}
				final String day = getStringFrom(obj, "day");
				if (day == null) {
					Log.w("WebParser", "Failed to get day for hours " + pk);
					continue;
				}
				final String rawOpenTime = getStringFrom(obj, "open_time");
				if (rawOpenTime == null) {
					Log.w("WebParser", "Failed to get open time for hours " + pk);
					continue;
				}
				final int openTime = Integer.parseInt(rawOpenTime.substring(0, 2) + rawOpenTime.substring(3, 5));
				final String rawCloseTime = getStringFrom(obj, "close_time");
				if (rawCloseTime == null) {
					Log.w("WebParser", "Failed to get close time for hours " + pk);
					continue;
				}
				final int closeTime = Integer.parseInt(rawCloseTime.substring(0, 2) + rawCloseTime.substring(3, 5));
				final Hours h = new Hours(openTime, closeTime);
				
				// Make the week if it doesn't exist.
				HashMap<String, ArrayList<Hours>> week = hours.get(id);
				if (week == null) {
					week = new HashMap<String, ArrayList<Hours>>();
					hours.put(id, week);
				}
				
				// Make the periods for this day if it doesn't exist.
				ArrayList<Hours> periods = week.get(day);
				if (periods == null) {
					periods = new ArrayList<Hours>();
					week.put(day, periods);
				}
				
				periods.add(h);
			}
			
			// Parse the locations.
			final JSONArray locArray = (JSONArray) new JSONTokener(rawLocations).nextValue();
			final PointOfInterest.Builder builder = new PointOfInterest.Builder();
			final Pattern whitespacePattern = Pattern.compile("\\s+");
			final ArrayList<PointOfInterest> pois = new ArrayList<PointOfInterest>();
			
			for (int i = 0; i < locArray.length(); i++) {
				final JSONObject obj0 = locArray.getJSONObject(i);
				final JSONObject obj = obj0.getJSONObject("fields");
				builder.id = obj0.getInt("pk");
				builder.name = getStringFrom(obj, "frp_program_name");
				
				builder.address = new Address(getStringFrom(obj, "geo_place"));
				builder.address.setLatitude(parseDouble(getStringFrom(obj, "geo_lat")));
				builder.address.setLongitude(parseDouble(getStringFrom(obj, "geo_lng")));
				
				final String rawPhoneNumber = getStringFrom(obj, "phone");
				if (rawPhoneNumber != null && rawPhoneNumber.length() > 0)
					builder.phoneNumber = Uri.parse("tel:" + rawPhoneNumber);
				
				final int agencyId = obj.getInt("member");
				builder.agency = agencies.get(agencyId);
				
				// Programs.
				final boolean pcmg = obj.getBoolean("pcmg_offered");
				final boolean tripleP = obj.getBoolean("triplep_offered");
				final boolean npp = obj.getBoolean("npp_offered");
				final ArrayList<String> programs = new ArrayList<String>();
				if (pcmg)
					programs.add("Parent-Child Mother Goose");
				if (tripleP)
					programs.add("Triple P Parenting");
				if (npp)
					programs.add("Nobody's Perfect Parenting");
				if (programs.size() > 0)
					builder.programs = programs.toArray(new String[programs.size()]);
				
				// Accreditations and website are taken from the agency.
				if (builder.agency != null) {
					builder.accreditation = builder.agency.getAccreditation();
					
					if (builder.agency.getWebsite() != null) {
						builder.website = Uri.parse(builder.agency.getWebsite().toString());
					}
				}
				
				// Hours of operation.
				final HashMap<String, ArrayList<Hours>> week = hours.get(builder.id);
				if (week != null) {
					for (Map.Entry<String, ArrayList<Hours>> entry : week.entrySet()) {
						final String day = entry.getKey();
						final ArrayList<Hours> periods = entry.getValue();
						builder.setHours(day, periods.toArray(new Hours[periods.size()]));
					}
				}
				
				final PointOfInterest poi = builder.build(whitespacePattern);
				// Make sure location is within BC.
				if (poi.getAddress() == null ||
						poi.getAddress().getLatitude() < 48.0 || 
						poi.getAddress().getLatitude() > 61.0 || 
						poi.getAddress().getLongitude() < -140.0 ||
						poi.getAddress().getLongitude() > -113.0) {
					Log.w("WebParser", "Discarded location outside of BC: " + poi.getName());
				}
				else {
					if (poi.getAgency() != null)
						poi.getAgency().addLocation(poi);
					pois.add(poi);
				}
			}
			
			Database.setAllPointsOfInterest(pois);
			Database.setAllAgencies(agencies.values());
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Parse the data from frpbc.ca
	 */
	public static void parseFRPBC(Context context) {
		try {
			String rawAgencies = readSmart(context, new java.net.URL("http://frpbc.ca/json/Member"), "Member.json");
			String rawHours = readSmart(context, new java.net.URL("http://frpbc.ca/json/HoursOfOperation"), "Hour.json");
			String rawLocations = readSmart(context, new java.net.URL("http://frpbc.ca/json/Location"), "Location.json");
			
			parse(rawAgencies, rawHours, rawLocations);
			Log.i("WebParser", "Parsed FRPBC.");
		}
		catch (MalformedURLException e) {
			Log.e("WebParser", "parseFRPBC", e);
		}
	}
}
