package bdiagent;

import org.json.JSONException;
import org.json.JSONObject;


public class Event {
	String where;
	int day;
	int month;
	String type;
	String eventType;
	 double totalimportance;
	 double baseimportance;
	 double additionalimportance;
	 double totalAdditionalimportance;
	 int additionalCounter;
	 int lockAdditionalDegree;
	 double friendsImportance;
	String explanation;
	String period;
	JSONObject event;
	public Event(JSONObject event) {
		this.event=event;
		filldata();
		additionalimportance=0;
		baseimportance=0;
		totalimportance=baseimportance+additionalimportance;
		
	}
	public Event() {
		
	}
	//JSONObjeyi parçalayıp atıcak
	public void filldata() {

		try {
			where = event.getString("where");
			type = event.getString("type");	
			explanation = event.getString("explanation");
			eventType = event.getString("event-type");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.err.println("Main Items Json error ");
		}
		try {
			if (event.has("day")){
				day = event.getInt("day");
			}else{
				day = -1;
			}
			
			month = event.getInt("month");
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Day details not found in Json");
		}
		try {
			if (event.has("period")){
				period = event.getString("period");
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Period not found in Json");
		}
		
	}
	public String getExplanation() {
		return this.explanation;
	}
	public void setDate(int time) {
		this.day+=time;
	}
	public void setAdditional(double d) {
		this.additionalimportance=d;
		this.totalimportance=d+this.baseimportance;
	}
	public void addAdditional(double d) {
		this.totalAdditionalimportance+=d;
		this.additionalCounter++;
	}
	public void calculateMovieFinalAdditionalImportance() {
		
	}
	public void setBase(double d) {
		this.baseimportance=d;
		this.totalimportance=this.additionalimportance+d;
	}
	public double getTotal() {
		return this.totalimportance;
	}
	public double getAdditional() {
		return this.additionalimportance;
	}
	public double getBase() {
		return this.baseimportance;
	}
	public int getMovieID() {
		return -1;
	}
	public void lockAdditional(int degree) {
		this.lockAdditionalDegree=degree;
	}
	public int getLockDegree() {
		return this.lockAdditionalDegree;
	}
	public String toTrustString() {
		return "";
	}
	public void friendsRequestHandler(double number ) {
		friendsImportance = number;
		totalimportance += number;
	}
	@Override
	public String toString(){
		return "New Belief: "+"\""+explanation+"\" in Day "+day+ ", its period is "+period+" and its importance is " + (baseimportance+additionalimportance);
		//return "New Belief: "+"\""+explanation+"\" in Day "+day+ ", its period is "+period+" and its importance is " + (baseimportance+additionalimportance) +"-"+baseimportance+"-"+additionalimportance ;
	}
	public String eventInfo(){
		return "\""+explanation+"\" in Day "+day+ ", its period is "+period+" and its importance is " + (baseimportance+additionalimportance)  ;
	}
	
}
