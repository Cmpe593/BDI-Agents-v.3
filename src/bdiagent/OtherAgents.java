package bdiagent;

import java.util.ArrayList;

public class OtherAgents {
	private int id;
	private ArrayList<Integer> trustedAgents;
	ArrayList<ArrayList<String>> movierating;
	public OtherAgents(int id){
		this.id=id;
		this.trustedAgents=new ArrayList<Integer> ();
		this.movierating=new ArrayList<ArrayList<String>>();

	}
	public OtherAgents(int id,int mytrust){
		this.id=id;
		this.trustedAgents=new ArrayList<Integer> ();
	}
	public void addTrust(int value) {
		this.trustedAgents.add(value);
	}
	public void addMovieRating(String movieID,String rating) {
		ArrayList<String> s = new ArrayList<String>();
		s.add(movieID);
		s.add(rating);
		movierating.add(s);
	}
	public int getID() {
		return this.id;
	}
	public ArrayList<Integer> getTrustedAgents() {
		return this.trustedAgents;
	}
}
