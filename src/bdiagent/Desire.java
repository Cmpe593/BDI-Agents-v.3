package bdiagent;

import java.util.ArrayList;

public class Desire {
	int day;
	ArrayList<Event> events;
	public Desire(int day) {
		this.day=day;
		this.events = new ArrayList<Event>();
	}
	public void addEvent(Event event) {
		events.add(event);
	}
	public String toString() {
		String s="";
		s=s+"List of Desires day " + day+ ":\n";
		for(int i =0;i<events.size();i++) {
			s=s+events.get(i).eventInfo()+"\n";
		}
		return s;
	}
	public String toStringTenDesire() {
		String s="";
		s=s+"List of 10 Desires day " + day+ ":\n";
		int size=10;
		if(events.size()<10) {
			size=events.size();
		}
		for(int i =0;i<size;i++) {
			s=s+events.get(i).eventInfo()+"\n";
		}
		s+=".\n.\n.\n";
		return s;
	}
}
