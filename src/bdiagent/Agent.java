package bdiagent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class Agent extends Thread {
	private String name;
	private int sday;
	private int eday;
	//private ArrayList<Belief> beliefs;
	private ArrayList<Desire> desires;
	private ArrayList<Intention> intentions;
	private ArrayList<Event> events;
	private ArrayList<Comparison> comparisons;
	private ArrayList<Relation> relations;
	private ArrayList<OtherAgents> allagents;
	private ArrayList<OtherAgents> mytrustedagents;
	private ArrayList<Movies> movies;
	private double desireThreshold=0.4;
	private double[][] list = {{0.1,0.3,0.6,0.9},{0.1,0.3,0.5,0.8}};
	private int type;
	private int scenario;
	SecondaryAgent newAgent ;
	public  Agent (String name, int startday, int endday,int type,int scenario){
		this.type = type;
		this.name = name;
		//this.beliefs = new ArrayList<Belief> ();
		this.desires = new ArrayList<Desire>();
		this.intentions = new ArrayList<Intention> ();
		this.events = new ArrayList<Event>();
		this.comparisons = new ArrayList<Comparison>();
		this.relations = new ArrayList<Relation>();
		this.allagents = new ArrayList<OtherAgents>();
		this.mytrustedagents = new ArrayList<OtherAgents>();
		this.movies = new ArrayList<Movies>();
		this.sday=startday;
		this.eday=endday;
		this.scenario=scenario;
		//run();
		new Thread(this).start();
	}
	
	public void run() {
		FileWriter fw = null;
		try {
		fw = new FileWriter("console.txt");
		BufferedWriter console = new BufferedWriter(fw);
		createMovies();// new location
		newAgent= new SecondaryAgent("Zaum", eday);
		newAgent.fulfillSchedule(movies);
		for(int i=sday;i<=eday;i++) {
			
			String belief;
			//System.out.println();
			console.write("\n");
			console.write("\n");
			//System.out.println(name+" wakes up at Day "+i);
			console.write(name+" wakes up at Day "+i);
			console.write("\n");
			//System.out.println();
			console.write("\n");
			console.write("\n");
			
			if (i==sday) {
				belief=readInitialBelief();
				createAgents();
				getAllAgentsTrustBelief();
				setMyTrust(this.scenario);
//				createMovies(); former location
				movieBaseImpCalculator();
				setMovieRatings();
				setMyMovieAdditionalImportance();
				calculateMovieBeliefFinalImportance();
				writeTrustForAllMovies();
			}
			else {
				belief = getForEachDay(i);
			}
			if(belief.isEmpty()) {
				continue;
			}
			matchUpdater(false);
			String[] arr=belief.split("!!!");
			for(int j=0;j<arr.length;j++) {
				//System.out.println(arr[j]);
				try {
					JSONObject json = new JSONObject(arr[j]);

					try {

						if(json.has("event-schedule")){
							Event event = new Event(json.getJSONObject("event-schedule"));
							events.add(event);
							calculateEventImportance(event);
							//System.out.println(event.toString());
							console.write(event.toString());
							console.write("\n");
						}else if(json.has("comparison")){
							Comparison comparison = new Comparison(json.getJSONObject("comparison"));
							comparisons.add(comparison);
							//System.out.println(comparison.toString());
							console.write(comparison.toString());
							console.write("\n");
						}else if(json.has("relation")) {
							Relation relation = new Relation(json.getJSONObject("relation"));
							relations.add(relation);
							//System.out.println(relation.toString());
							console.write(relation.toString());
							console.write("\n");
						}else if(json.has("change")) {
							JSONObject change = json.getJSONObject("change");
							if(change.getString("type").equalsIgnoreCase("update-event")){
								int number= getEventID(change.getString("explanation"));
								if(number!=-1) {
									//System.out.print(events.get(number).toString()+" and change its day to ");
									console.write(events.get(number).toString()+" and change its day to ");
									events.get(number).setDate(change.getInt("time"));
									//System.out.println(events.get(number).day);
									console.write(events.get(number).day);
									console.write("\n");
								}
							}else if(change.getString("type").equalsIgnoreCase("drop-event")) {
								int number= getEventID(change.getString("explanation"));
								if(number!=-1) {
									//System.out.println(events.get(number).toString()+" is removed.");
									console.write(events.get(number).toString()+" is removed.");
									console.write("\n");
									events.remove(number);
								}
							}
						}else if(json.has("movie-rating")) {
							JSONObject mrating = json.getJSONObject("movie-rating");
							int agentid=mrating.getInt("agent");
							String movieid=mrating.getString("movieID");
							int movieID=Integer.parseInt(movieid);
							String rating=mrating.getString("rating");
							double Rating=Double.parseDouble(rating);
							allagents.get(agentid-1).addMovieRating(movieid, rating);
							int deg = getAgentDegree(agentid);
							int eventid=getMovieIDinEventTable(movieID);
							if(deg!=0 && (events.get(eventid).getLockDegree()>=deg || events.get(eventid).getLockDegree()==0)) {
								
								events.get(eventid).addAdditional(caltulateAdditionalImportanceInOrderToTrust(deg, Rating));
								events.get(eventid).calculateMovieFinalAdditionalImportance();
								console.write("New belief: Movie "+movieID+" is rated by an agent "+agentid+" with "+deg+". trust degree.");
								console.write("Its importance effect is: "+caltulateAdditionalImportanceInOrderToTrust(deg, Rating));
								console.write("\n");
							}
							else 
								console.write("This agent has low trust degree.So nothing effected. \n");
							
						}
					}
					catch (Exception e) {
						// TODO: handle exception
						System.err.println("json cannot be parsed");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (Event event : events) {
				for (Relation r : relations) {
					relationExecuter(event,r);
				}
				for (Comparison c : comparisons) {
					comparisonExecuter(event,c);
				}
			}
			//System.out.println(events.size());
			/*for(int p=0;p<events.size();p++) {
				if(events.get(p).getAdditional()>0.5)
				System.out.println(events.get(p).toString());
			}*/
			
			
			Desire desire = new Desire(i);
			
			for(int d=i;d<=eday;d++) {
				for(int e=0;e<events.size();e++) {
					if(events.get(e).day==d && events.get(e).period.equalsIgnoreCase("Day")) {
						if(events.get(e).getTotal()>this.desireThreshold)
							desire.addEvent(events.get(e));
					}
				}
				for(int e=0;e<events.size();e++) {
					if(events.get(e).day==d && events.get(e).period.equalsIgnoreCase("Night")) {
						if(events.get(e).getTotal()>this.desireThreshold)
							desire.addEvent(events.get(e));
					}
				}
			}
			desires.add(desire);
			//System.out.println();
			console.write("\n");
			//console.write(desire.toString());
			console.write(desire.toStringTenDesire());
			console.write("\n");
			//System.out.println(desire.toString());
			
			
			
			Intention intention = new Intention(i);
			for(int d=i;d<=eday;d++) {
				Event ev = null;
				Event ev2 = null;
				double temp=0;
				for(int e=0;e<events.size();e++) {
					if(events.get(e).day==d && events.get(e).period.equalsIgnoreCase("Day")) {
						if(events.get(e).getTotal()>temp) {
							desire.addEvent(events.get(e));
							ev=events.get(e);
							temp=events.get(e).getTotal();
						}
					}
				}
				if(ev!=null)
					intention.addEvent(ev);
				temp=0;
				for(int e=0;e<events.size();e++) {
					if(events.get(e).day==d && events.get(e).period.equalsIgnoreCase("Night")) {
						if(events.get(e).getTotal()>temp) {
							desire.addEvent(events.get(e));
							ev2=events.get(e);
							temp=events.get(e).getTotal();
						}
					}
				}
				if(ev2!=null)
					intention.addEvent(ev2);
			}
			intentions.add(intention);
			//System.out.println();
			//System.out.println(intention.toString());
			console.write("\n");
			console.write(intention.toString());
			console.write("\n");
			/*THIRD PROJECT MAIN START*/
			newAgent.DIMaker(i);
			compareIntentions(i-sday);
			int temp =0;
			while(!isMatchFound()){
				updateSecondarysImportance(i-sday);
				newAgent.calculateUpdatedIntention(i, temp);
				compareIntentions(i-sday);
				temp++;
				
			}
		}
		newAgent.consoleCloser();
		console.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	boolean matchFound =false;
	public void matchUpdater (boolean newMatch) {
		matchFound = newMatch;
		newAgent.matchFound = newMatch;
	}
	public boolean isMatchFound(){
		if (matchFound&&newAgent.matchFound){
			return true;
		}
		return false;
	}
	public void updateSecondarysImportance(int turn) {
		ArrayList<Event> movieIntention = movieIntentionFinder(turn);
		System.out.println("in the day "+(turn+sday));
		for (Event eve:movieIntention) {
			System.out.println("Agent 2K says Zaum to update importance of "+eve.toString());
			newAgent.isfriendRequestAffortable(eve.explanation, eve.totalimportance);
		}
	}
	public void compareIntentions(int turn) {
		ArrayList<Event> movieIntention = movieIntentionFinder(turn);
		System.out.println("in the day "+(turn+sday)+"Agent 2K says to Zaum");
		for (Event eve:movieIntention) {
			System.out.println("Agent2K intents to go to "+eve.explanation);
			System.out.println("Zaum says to Agent2K");
			for (Event eveNewAgent:newAgent.intentions.get(turn).events){
				if(eve.explanation.equalsIgnoreCase(eveNewAgent.explanation)){
					System.out.println("Zaum also INTENTS to go to "+ eveNewAgent.explanation+"\n");
					matchUpdater(true);
					break;
				}
			}
			if(isMatchFound()){
				System.out.println("They agreed upon going to "+ eve.explanation +" together");
				break;
			}
			System.out.println("Zaum has no intention to go to "+ eve.explanation+"\n");
		}
	}
	public ArrayList<Event> movieIntentionFinder(int turn) {
		ArrayList<Event> collector = new ArrayList<Event>();
		Intention s = intentions.get(turn);
		for(Event event :s.events){
			if (event.eventType.equalsIgnoreCase("Movies")){
				System.out.println();
				collector.add(event);
			}
		}
		return collector;
	}	
	public int getEventID(String a) {
		for (int i = 0; i < events.size(); i++) {
			if(a.equalsIgnoreCase(events.get(i).getExplanation())) {
				return i;
			}
		}
		return -1;
	}
	public String readInitialBelief() {
		File file = new File("belief.txt");
		try {
			Scanner sc = new Scanner(file);
			StringBuffer sb= new StringBuffer();
			while(sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			sc.close();
			return sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "File Not Found";
		}
	}
	public String getForEachDay(int day)  {
		File file = new File("day"+day+".txt");
		try {
			Scanner sc = new Scanner(file);
			StringBuffer sb= new StringBuffer();
			while(sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			sc.close();
			return sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "File Not Found";
		}
	}
	public void calculateEventImportance(Event event) {
		double importance = 0;
		Random random = new Random();
		if(event.type.equalsIgnoreCase("Stay")){
			if(event.period.equalsIgnoreCase("day")){
				importance = random.nextDouble()*list[type][1];
			}else if (event.period.equalsIgnoreCase("night")){
				importance = random.nextDouble()*list[type][0];
			}
		}else if(event.type.equalsIgnoreCase("Work")){
			if(event.eventType.equalsIgnoreCase("Talk-Speaker")){
				importance = 1.5;
			}else{
				importance = random.nextDouble()*(1-list[type][3])+list[type][3];
			}
		}else if (event.type.equalsIgnoreCase("Entertainment")){
			importance = random.nextDouble()*(list[type][2]-list[type][1])+list[type][1];
		}
		event.setBase(importance);
		
	}
	
	public void  relationExecuter(Event event,Relation r) {
		if(event.eventType.equalsIgnoreCase(r.first) && event.period.equalsIgnoreCase(r.firstperiod)){
			for (Event event2 : events) {
				if(event2.eventType.equalsIgnoreCase(r.second) && event2.period.equalsIgnoreCase(r.secondperiod)){
					if (event2.day==event.day+r.time){
						
							event2.setAdditional(event.getBase());
							//System.out.println("oluyor mu "+event2.toString());
							
						
					}
				}
			}
		}
	}
	public void  comparisonExecuter(Event event,Comparison c) {
		if(event.eventType.equalsIgnoreCase(c.first)){
			for (Event event2 : events) {
				if(event2.eventType.equalsIgnoreCase(c.second)){
					if(event.day==event2.day&&event.period.equalsIgnoreCase(event2.period)){
						event.setAdditional(c.amount); 
						//System.out.println("update geldi hanım"+event.toString());
						break;
					}
				}
			}
		}
	}
	public void createAgents() {
		for(int i=1;i<=1642;i++) {
			OtherAgents oagent=new OtherAgents(i);
			allagents.add(oagent);
		}
	}
	/* the changes
	 * it creates movies without calculating base importance
	 * send the clear movie list to new Agent
	 * */
	public void createMovies() {
		for(int i=1;i<=2071;i++) {
			Movies movie=new Movies(i);
//			calculateMovieBaseImportance(movie);//former version
			movies.add(movie);
//			events.add(movie);//former version
		}
	}
	/* calculate base importance of movies 
	 * 
	 * 
	 * */
	public void movieBaseImpCalculator() {
		for(Movies movie:movies){
			Movies temp = new Movies(movie);
			calculateMovieBaseImportance(temp);
			events.add(temp);
		}
	}
	public void getAllAgentsTrustBelief() {
		File filetrust = new File("filmtrust/trust.txt");
		try {
			Scanner sc = new Scanner(filetrust);
			while(sc.hasNextLine()) {
				String s=sc.nextLine().trim();
				String[] sarray = s.split(" ");
				int trustor=Integer.parseInt(sarray[0]);
				int trustee=Integer.parseInt(sarray[1]);
				allagents.get(trustor-1).addTrust(trustee);
				
			}
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setMyTrust(int scenario){
		if(scenario==1) {
			for(int i=0;i<30;i++) {
				this.mytrustedagents.add(allagents.get(i));
			}
		}
		else if(scenario==2) {
			
		}
	}
	public void setMovieRatings(){
		File filemovie = new File("filmtrust/ratings.txt");
		try {
			Scanner sc = new Scanner(filemovie);
			while(sc.hasNextLine()) {
				String s=sc.nextLine().trim();
				String[] sarray = s.split(" ");
				int agent=Integer.parseInt(sarray[0]);
				allagents.get(agent-1).addMovieRating(sarray[1], sarray[2]);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void calculateMovieBaseImportance(Movies event) {
		double baseimp=0;
		Random random = new Random();
		baseimp = random.nextDouble()*(list[type][2]-list[type][1])+list[type][1];
		event.setBase(baseimp);
	}
	
	public int getMovieIDinEventTable(int id) {
		for(int i=0;i<events.size();i++) {
			if(events.get(i).eventType.equalsIgnoreCase("Movies") && events.get(i).getMovieID()==id){
				return i;
			}
		}
		return -1;
	}
	public double caltulateAdditionalImportanceInOrderToTrust(int trustDegree,double movieRating) {
		//if(trustDegree<0 || trustDegree>4) return 0;
		
		return 0.1*(movieRating-2.25)/trustDegree;
	}
	
	public void setMyMovieAdditionalImportance() {
		for(int i=0;i<mytrustedagents.size();i++) {
			OtherAgents o=mytrustedagents.get(i);
			for(int j=0;j<o.movierating.size();j++) {
				int movieid=Integer.parseInt(o.movierating.get(j).get(0));
				double movieRating=Double.parseDouble(o.movierating.get(j).get(1));
				int eventid= getMovieIDinEventTable(movieid);
				//System.out.println("Order: 1 - AdditionalTrust: "+caltulateAdditionalImportanceInOrderToTrust(1,movieRating)+"For movie ID: "+movieid+"And Agent ID: "+o.getID());
				events.get(eventid).addAdditional(caltulateAdditionalImportanceInOrderToTrust(1,movieRating));
				events.get(eventid).lockAdditional(1);
			}
			for(int k=0;k<o.getTrustedAgents().size();k++) {
				OtherAgents o2=allagents.get(o.getTrustedAgents().get(k)-1);
				for(int j=0;j<o2.movierating.size();j++) {
					int movieid=Integer.parseInt(o2.movierating.get(j).get(0));
					double movieRating=Double.parseDouble(o2.movierating.get(j).get(1));
					int eventid= getMovieIDinEventTable(movieid);
					if(events.get(eventid).getLockDegree()!=1) {
						//System.out.println("Order: 2 - AdditionalTrust: "+caltulateAdditionalImportanceInOrderToTrust(2,movieRating)+"For movie ID: "+movieid);
						events.get(eventid).addAdditional(caltulateAdditionalImportanceInOrderToTrust(2,movieRating));
						events.get(eventid).lockAdditional(2);
					}
				}
				for(int l=0;l<o2.getTrustedAgents().size();l++) {
					OtherAgents o3=allagents.get(o2.getTrustedAgents().get(l)-1);
					for(int j=0;j<o3.movierating.size();j++) {
						int movieid=Integer.parseInt(o3.movierating.get(j).get(0));
						double movieRating=Double.parseDouble(o3.movierating.get(j).get(1));
						int eventid= getMovieIDinEventTable(movieid);
						if(events.get(eventid).getLockDegree()==0 || events.get(eventid).getLockDegree()==3) {
							//System.out.println("Order: 3 - AdditionalTrust: "+caltulateAdditionalImportanceInOrderToTrust(3,movieRating)+"For movie ID: "+movieid);
							events.get(eventid).addAdditional(caltulateAdditionalImportanceInOrderToTrust(3,movieRating));
							events.get(eventid).lockAdditional(3);
						}
					}
					
				}
			}
			
			
		}
	}
	public void calculateMovieBeliefFinalImportance() {
		for(int i=0;i<events.size();i++) {
			if(events.get(i).eventType.equalsIgnoreCase("Movies")) {
				events.get(i).calculateMovieFinalAdditionalImportance();
			}
		}
	}
	public void writeTrustForAllMovies() {
		String s="";
		FileWriter fw;
		try {
			fw = new FileWriter("AgentTrust.txt");
		
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i=0;i<events.size();i++) {
			if(events.get(i).eventType.equalsIgnoreCase("Movies")) {
				s=events.get(i).toTrustString();
				bw.write(s);
				bw.write("\n");
				bw.write("\n");
			}
		}
		bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int getAgentDegree(int agentID) {
		for(int i=0;i<mytrustedagents.size();i++) {
			if(mytrustedagents.get(i).getID()==agentID) {
				return 1;
			}
			if(mytrustedagents.get(i).getTrustedAgents().contains(agentID)) {
				return 2;
			}
			for(int j=0;j<mytrustedagents.get(i).getTrustedAgents().size();j++) {
				OtherAgents o=allagents.get(mytrustedagents.get(i).getTrustedAgents().get(j)-1);
				if(o.getTrustedAgents().contains(agentID)) {
					return 3;
				}
			}	
		}
		return 0;
	}
}
