package ca.frpbc.model;

public class Address {
	
	private static final double EARTH_RADIUS = 6371;

	private String addressString;	// The address.
	private double latitude;		// The latitude.
	private double longitude;		// The longitude.
	
	/**
	 * Create an address with no initialized fields.
	 */
	public Address() {
		this(null, Double.MIN_VALUE, Double.MIN_VALUE);
	}
	
	/**
	 * Create an address with a given address string and no latitude or longitude.
	 * 
	 * @param addressString
	 */
	public Address(String addressString) {
		this(addressString, Double.MIN_VALUE, Double.MIN_VALUE);
	}
	
	/**
	 * Create an address with a given address string and a latitude and longitude.
	 * 
	 * @param addressString
	 * @param latitude
	 * @param longitude
	 */
	public Address(String addressString, double latitude, double longitude) {
		this.addressString = addressString;
		setLatitude(latitude);
		setLongitude(longitude);
	}
	
	/**
	 * Set the latitude. Automatically forces it to be within the 
	 * range of [-90, 90].
	 * 
	 * @param lat
	 */
	public void setLatitude(double lat) {
		if (lat > 90)
			latitude = lat % 90;
		else if (lat < -90)
			latitude = -((-lat) % 90);
		else
			latitude = lat;
	}
	
	/**
	 * Set the longitude. Automatically forces it to be within the
	 * range of [-180, 180)
	 * 
	 * @param lon
	 */
	public void setLongitude(double lon) {
		if (lon >= 180)
			longitude = lon % 180;
		else if (lon < -180)
			longitude = -((-lon) % 180);
		else
			longitude = lon;
	}
	
	/**
	 * Set the address string.
	 * 
	 * @param add
	 */
	public void setAddressString(String add) {
		addressString = add;
	}
	
	/**
	 * @return
	 *			The address string.
	 */
	public String getAddressString() {
		return addressString;
	}
	
	/**
	 * @return
	 *			The latitude.
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * @return
	 *			The longitude.
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * Get the distance between this address and another address.
	 * 
	 * @param other
	 * @return
	 *			The distance between this and other. If either this or other
	 *			does not have an assigned coordinate, returns Double.MAX_VALUE.
	 */
	public double getDistanceTo(Address other) {
		if (other.latitude == Double.MIN_VALUE || other.longitude == Double.MIN_VALUE)
			return Double.MAX_VALUE;
		return getDistanceTo(other.getLatitude(), other.getLongitude());
	}
	
	/**
	 * Get the distance between this address and a coordinate.
	 * 
	 * @param otherLat
	 * @param otherLon
	 * @return
	 *			The distance between this and the coordinate. If this has an 
	 *			unassigned coordinate or the given coordinate is unassigned, 
	 *			returns Double.MAX_VALUE.
	 */
	public double getDistanceTo(double otherLat, double otherLon) {
		if (latitude == Double.MIN_VALUE || longitude == Double.MIN_VALUE)
			return Double.MAX_VALUE;
		return distanceBetween(latitude, longitude, otherLat, otherLon);
	}
	
	/**
	 * Get the distance between two addresses.
	 * 
	 * @param address1
	 * @param address2
	 * @return
	 *			The distance between the two addresses. If coordinates are
	 *			unassigned for either, the behavior is undefined.
	 */
	public static double distanceBetween(Address address1, Address address2) {
		return distanceBetween(address1.getLatitude(), address1.getLongitude(), address2.getLatitude(), address2.getLongitude());
	}
	
	/**
	 * Get the distance between two coordinates.
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 *			The distance between the two coordinates. If either is outside
	 *			the range of valid latitude/longitude, the behavior is undefined.
	 */
	public static double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double radLat1 = Math.toRadians(lat1);
		double radLat2 = Math.toRadians(lat2);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + 
				Math.sin(dLon/2) * Math.sin(dLon/2) *
				Math.cos(radLat1) * Math.cos(radLat2);
		
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
		return EARTH_RADIUS * c;
	}

}