package bdiagent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class SecondaryAgent {
	private String name;
	private int eday;
	//private ArrayList<Belief> beliefs;
	private ArrayList<Desire> desires;
	public ArrayList<Intention> intentions;
	private ArrayList<Event> events;
	private ArrayList<Comparison> comparisons;
	private ArrayList<Relation> relations;
	private ArrayList<OtherAgents> allagents;
	private ArrayList<OtherAgents> mytrustedagents;
	private ArrayList<Movies> movies;
	private double desireThreshold=0.4;
	private double[][] list = {{0.1,0.3,0.6,0.9},{0.1,0.3,0.5,0.8}};
	private int type;
	boolean matchFound = false;
	public SecondaryAgent(String name,	 int eday){
		this.name = name;
		this.type = 1;
		this.eday = eday;
		this.desires = new ArrayList<Desire>();
		this.intentions = new ArrayList<Intention> ();
		this.events = new ArrayList<Event>();
		this.comparisons = new ArrayList<Comparison>();
		this.relations = new ArrayList<Relation>();
		this.allagents = new ArrayList<OtherAgents>();
		this.mytrustedagents = new ArrayList<OtherAgents>();
		this.movies = new ArrayList<Movies>();
	}
	FileWriter fw = null;
	BufferedWriter console;
	public void fulfillSchedule(ArrayList<Movies> movies){
		
		try {
			fw = new FileWriter("consoleForSecondary.txt");
			console = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.movies = movies;
		createAgents();
		getAllAgentsTrustBelief();
		setMyTrust();
//		setMyTrust(this.scenario);
//		createMovies(); former location
		movieBaseImpCalculator();
		setMovieRatings();
		setMyMovieAdditionalImportance();
		calculateMovieBeliefFinalImportance();
		writeTrustForAllMovies();
	}
	public void DIMaker(int i) {
		try {
			console.write("\n");
			console.write("\n");
			//System.out.println(name+" wakes up at Day "+i);
			console.write(name+" wakes up at Day "+i);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
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
		try {
			console.write("\n");
			//console.write(desire.toString());
			console.write(desire.toStringTenDesire());
			console.write("\n");
			//System.out.println(desire.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
		try {
			console.write("\n");
			console.write(intention.toString());
			console.write("\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean isfriendRequestAffortable(String explanation,double agentDesire) {
		for(Event event :events){
			if (event.explanation.equalsIgnoreCase(explanation)){
				if(event.totalimportance>0.7){
					event.friendsRequestHandler(agentDesire*0.5);
					System.out.println("Zaum update intention to go to "+ explanation+"around"+(agentDesire*0.5)+"\n");
					return true;
				}else if(event.totalimportance>0.6){
					event.friendsRequestHandler(agentDesire*0.3);
					System.out.println("Zaum update intention to go to "+ explanation+"around"+(agentDesire*0.3)+"\n");
					return true;
				}
				else if(event.totalimportance>0.5){
					event.friendsRequestHandler(agentDesire*0.2);
					System.out.println("Zaum update intention to go to "+ explanation+"around"+(agentDesire*0.2)+"\n");
					return true;
				}else if(event.totalimportance>0.4){
					event.friendsRequestHandler(agentDesire*0.1);
					System.out.println("Zaum update intention to go to "+ explanation+"around"+(agentDesire*0.1)+"\n");
					return true;
				}else if(event.totalimportance>0.3){
					System.out.println("Zaum cannot update intention to go to "+ explanation+"\n");
					return false;
				}
			}
		}
		return false;
	}
	public void calculateUpdatedIntention(int i,int current) {
		FileWriter fw = null;
		BufferedWriter console1;
		try {
			fw = new FileWriter("consoleUpdatedSecondary"+current+".txt");
			console1 = new BufferedWriter(fw);
			console1.write("\n");
			console1.write("\n");
			//System.out.println(name+" wakes up at Day "+i);
			console1.write(name+" wakes up at Day "+i);
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
			desires.remove(desires.size()-1);
			desires.add(desire);
			//System.out.println();
			console1.write("\n");
			//console.write(desire.toString());
			console1.write(desire.toStringTenDesire());
			console1.write("\n");
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
			intentions.remove(intentions.size()-1);
			intentions.add(intention);
			//System.out.println();
			//System.out.println(intention.toString());
			console1.write("\n");
			console1.write(intention.toString());
			console1.write("\n");
			console1.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	public void consoleCloser() {
		try {
			console.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createAgents() {
		for(int i=1;i<=1642;i++) {
			OtherAgents oagent=new OtherAgents(i);
			allagents.add(oagent);
		}
	}
	/* calculate base importance of movies 
	 * 
	 * 
	 * */
	private void movieBaseImpCalculator() {
		for(Movies movie:movies){
			Movies temp = new Movies(movie);
			calculateMovieBaseImportance(temp);
			events.add(temp);	}
	}
	private void calculateMovieBaseImportance(Movies event) {
		double baseimp=0;
		Random random = new Random();
		baseimp = random.nextDouble()*(list[type][2]-list[type][1])+list[type][1];
		event.setBase(baseimp);
	}
	private void getAllAgentsTrustBelief() {
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
	private void calculateMovieBeliefFinalImportance() {
		for(int i=0;i<events.size();i++) {
			if(events.get(i).eventType.equalsIgnoreCase("Movies")) {
				events.get(i).calculateMovieFinalAdditionalImportance();
			}
		}
	}
	private void setMovieRatings(){
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
	private void writeTrustForAllMovies() {
		String s="";
		FileWriter fw;
		try {
			fw = new FileWriter("SecondaryAgentTrust.txt");
		
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
	private double caltulateAdditionalImportanceInOrderToTrust(int trustDegree,double movieRating) {
		//if(trustDegree<0 || trustDegree>4) return 0;
		
		return 0.1*(movieRating-2.25)/trustDegree;
	}
	private void setMyMovieAdditionalImportance() {
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
	private void setMyTrust(){
			for(int i=0;i<30;i++) {
				this.mytrustedagents.add(allagents.get(i));
			}	
		
	}
	private int getMovieIDinEventTable(int id) {
		for(int i=0;i<events.size();i++) {
			if(events.get(i).eventType.equalsIgnoreCase("Movies") && events.get(i).getMovieID()==id){
				return i;
			}
		}
		return -1;
	}
}
	

