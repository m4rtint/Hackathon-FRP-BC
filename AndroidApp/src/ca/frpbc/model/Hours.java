 package ca.frpbc.model;

public class Hours {

	private int openTime;
	private int closeTime;
	
	public Hours(int openTime, int closeTime) {
		setOpenTime(openTime);
		setCloseTime(closeTime);
	}
	
	public void setOpenTime(int oT) {
		openTime = oT;
	}
	
	public void setCloseTime(int cT) {
		closeTime = cT;
	}
	
	public int getOpenTime() {
		return openTime;
	}
	
	public int getCloseTime() {
		return closeTime;
	}
	
	public String toString() {
		return timeToString(openTime) + " - " + timeToString(closeTime);
	}

	private String timeToString(int time) {
		int hours = time / 100;
		int minutes = time % 100;
		String minutesString;
		if (minutes > 9)
			minutesString = String.valueOf(minutes);
		else
			minutesString = '0' + String.valueOf(minutes);
		
		if (hours == 0) {
			return "12:" + minutesString + " AM";
		}
		if (hours == 12) {
			return "12:" + minutesString + " PM";
		}
		if (hours < 12) {
			return hours + ":" + minutesString + " AM";
		}
		hours -= 12;
		return hours + ":" + minutesString + " PM";
	}
}
