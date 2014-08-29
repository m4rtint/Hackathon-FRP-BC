package ca.frpbc.model;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import ca.frpbc.Database;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class PointOfInterest implements Parcelable {
	
	public static final class Builder {
		
		public int id;
		public String name;
		public Address address;
		public Uri phoneNumber;
		public Agency agency;
		public String[] programs;
		public String accreditation;
		public Uri website;
		private Hours[] monHours;
		private Hours[] tuesHours;
		private Hours[] wedHours;
		private Hours[] thursHours;
		private Hours[] friHours;
		private Hours[] satHours;
		private Hours[] sunHours;
		
		public PointOfInterest build(Pattern whitespacePattern) {
			PointOfInterest out = new PointOfInterest(id, name, address, phoneNumber, agency, programs, accreditation, website,
					monHours, tuesHours, wedHours, thursHours, friHours, satHours, sunHours, whitespacePattern);
			
			id = -1;
			name = null;
			address = null;
			phoneNumber = null;
			agency = null;
			programs = null;
			accreditation = null;
			website = null;
			monHours = null;
			tuesHours = null;
			wedHours = null;
			thursHours = null;
			friHours = null;
			satHours = null;
			sunHours = null;
			
			return out;
		}
		
		public void setHours(String day, Hours[] hours) {
			if (day.equalsIgnoreCase("Mon") || day.equals("1"))
				monHours = hours;
			else if (day.equalsIgnoreCase("Tue") || day.equals("2"))
				tuesHours = hours;
			else if (day.equalsIgnoreCase("Wed") || day.equals("3"))
				wedHours = hours;
			else if (day.equalsIgnoreCase("Thur") || day.equals("4"))
				thursHours = hours;
			else if (day.equalsIgnoreCase("Fri") || day.equals("5"))
				friHours = hours;
			else if (day.equalsIgnoreCase("Sat") || day.equals("6"))
				satHours = hours;
			else if (day.equalsIgnoreCase("Sun") || day.equals("7"))
				sunHours = hours;
		}
	}
	
	private final int id;
	private final String name;
	private final Address address;
	private final Uri phoneNumber;
	private final Agency agency;
	private final String[] programs;
	private final String accreditation;
	private final Uri website;
	private final Hours[] monHours;
	private final Hours[] tuesHours;
	private final Hours[] wedHours;
	private final Hours[] thursHours;
	private final Hours[] friHours;
	private final Hours[] satHours;
	private final Hours[] sunHours;
	private final Set<String> searchTerms;
	
	private PointOfInterest(int id, String name, Address address, Uri phoneNumber,
			Agency agency, String[] programs, String accreditation, Uri website, Hours[] monHours,
			Hours[] tuesHours, Hours[] wedHours, Hours[] thursHours, Hours[] friHours,
			Hours[] satHours, Hours[] sunHours, Pattern whitespacePattern) {
		
		// Assign fields.
		this.id = id;
		this.name = name;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.agency = agency;
		this.programs = programs;
		this.accreditation = accreditation;
		this.website = website;
		this.monHours = monHours;
		this.tuesHours = tuesHours;
		this.wedHours = wedHours;
		this.thursHours = thursHours;
		this.friHours = friHours;
		this.satHours = satHours;
		this.sunHours = sunHours;
		
		// Figure out the search terms.
		searchTerms = new HashSet<String>();
		String[] terms = whitespacePattern.split(name);
		for (String t : terms)
			searchTerms.add(t.toLowerCase(Locale.US));
		
		terms = whitespacePattern.split(agency.getName());
		for (String t : terms)
			searchTerms.add(t.toLowerCase(Locale.US));
		
		if (programs != null) {
			for (String program : programs) {
				terms = whitespacePattern.split(program);
				for (String t : terms)
					searchTerms.add(t.toLowerCase(Locale.US));
			}
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public Address getAddress() {
		return address;
	}
	
	public Uri getPhoneNumber() {
		return phoneNumber;
	}
	
	public Agency getAgency() {
		return agency;
	}

	public String[] getPrograms() {
		return programs;
	}
	
	public String getAccreditation() {
		return accreditation;
	}

	public Uri getWebsite() {
		return website;
	}

	public Hours[] getMonHours() {
		return monHours;
	}

	public Hours[] getTuesHours() {
		return tuesHours;
	}
	
	public Hours[] getWedHours() {
		return wedHours;
	}
	
	public Hours[] getThursHours() {
		return thursHours;
	}
	
	public Hours[] getFriHours() {
		return friHours;
	}
	
	public Hours[] getSatHours() {
		return satHours;
	}
	
	public Hours[] getSunHours() {
		return sunHours;
	}
	
	public boolean hasSearchTerm(String term) {
		return searchTerms.contains(term);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
	}
	
	public static final Parcelable.Creator<PointOfInterest> CREATOR
			= new Parcelable.Creator<PointOfInterest>() {
		@Override
		public PointOfInterest createFromParcel(Parcel source) {
			int id = source.readInt();
			return Database.getPointOfInterest(id);
		}

		@Override
		public PointOfInterest[] newArray(int size) {
			return new PointOfInterest[size];
		}
	};

}
