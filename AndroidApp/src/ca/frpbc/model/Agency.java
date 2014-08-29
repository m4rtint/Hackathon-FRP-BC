package ca.frpbc.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ca.frpbc.Database;


import android.os.Parcel;
import android.os.Parcelable;

public class Agency implements Parcelable {
	
	private final int id;
	private String name;
	private String phoneNumber;
	private URI website;
	private String accreditation;
	private int accredStartYear;
	private int accredRenewYear;
	private List<PointOfInterest> locations;
	
	public Agency(int id, String name) {
		this.id = id;
		this.name = name;
		this.locations = new ArrayList<PointOfInterest>();
		accredStartYear = -1;
		accredRenewYear = -1;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public void setWebsite(URI website) {
		this.website = website;
	}
	
	public void setAccreditation(String accreditation) {
		this.accreditation = accreditation;
	}
	
	public void setAccreditationPeriod(int start, int renew) {
		accredStartYear = start;
		accredRenewYear = renew;
	}
	
	public void setLocations(List<PointOfInterest> locations) {
		this.locations = locations;
	}
	
	public void addLocation(PointOfInterest location) {
		locations.add(location);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public URI getWebsite() {
		return website;
	}
	
	public String getAccreditation() {
		return accreditation;
	}
	
	public int getAccredStartYear() {
		return accredStartYear;
	}
	
	public int getAccredRenewYear() {
		return accredRenewYear;
	}
	
	public List<PointOfInterest> getLocations() {
		return locations;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
	}
	
	public static final Parcelable.Creator<Agency> CREATOR
			= new Parcelable.Creator<Agency>() {
		@Override
		public Agency createFromParcel(Parcel source) {
			int id = source.readInt();
			return Database.getAgency(id);
		}
		
		@Override
		public Agency[] newArray(int size) {
			return new Agency[size];
		}
	};

}
