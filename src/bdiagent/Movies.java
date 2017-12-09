package bdiagent;

import java.util.Random;

public class Movies extends Event {
	 int movieID;
	 String Type;
	 String filmTypes;
	 int filmtypeID;
	public Movies(int id) {
		Random random = new Random();
		this.movieID=id;
		this.where = "Amsterdam";
		this.day = random.nextInt(6)+16;
		this.month = 7;
		this.type = "Entertainment";
		this.eventType = "Movies";
		boolean temp =random.nextBoolean();
		if(temp){
			this.period = "Day";
		}else{
			this.period = "Night";
		}
		this.filmtypeID=random.nextInt(10);
		this.filmTypes = typeMaker(this.filmtypeID);
		this.explanation = "Movie "+movieID+" and type: "+ filmTypes ;
		this.additionalimportance=0;
		this.baseimportance=0;
		this.totalimportance=baseimportance+additionalimportance;
		this.lockAdditionalDegree=0;
		this.totalAdditionalimportance=0;
		this.additionalCounter=0;
		this.friendsImportance=0;
	}
	public Movies(Movies movie) {
		// TODO Auto- constructor stub
		this.movieID=movie.movieID;
		this.where = "Amsterdam";
		this.day = movie.day;
		this.month = 7;
		this.type = "Entertainment";
		this.eventType = "Movies";
		
			this.period =movie.period;
		
		this.filmtypeID=movie.filmtypeID;
		this.filmTypes = typeMaker(this.filmtypeID);
		this.explanation = "Movie "+movieID+" and type: "+ filmTypes ;
		this.additionalimportance=0;
		this.baseimportance=0;
		this.totalimportance=baseimportance+additionalimportance;
		this.lockAdditionalDegree=0;
		this.totalAdditionalimportance=0;
		this.additionalCounter=0;
	}
	private static String typeMaker (int key) {
		switch (key) {
		
		case 0:
			return "Comedy" ;
			
		case 1:
			return "Sci-Fi" ;
				
		case 2:
			return "Action" ;
			
		case 3:
			return "War" ;
			
		case 4:
			return "Sport" ;
			
		case 5:
			return "Drama" ;
			
		case 6:
			return "Adventure" ;
			
		case 7:
			return "Horror" ;
			
		case 8:
			return "Animation" ;
			
		case 9:
			return "Documentary" ;
			
		default:
			return "Family" ;
			
		}
	}
	@Override 
	public void filldata(){
		
	}
	public int getMovieID() {
		return this.movieID;
	}
	public void setMovieID(int value) {
		this.movieID=value;
		
	}

	public void calculateMovieFinalAdditionalImportance() {
		if(this.additionalCounter>0)
			setAdditional((this.totalAdditionalimportance/this.additionalCounter)+(5-this.filmtypeID)*0.03);
		else
			setAdditional((5-this.filmtypeID)*0.02);
	}
	//This sysout show that total notes for a movie by truster, # of truster and trust degree
	public String toTrustString() {
		String s ="Trust degree for movie "+this.getMovieID()+": "+this.getLockDegree();
		if(this.getLockDegree()==0) {
			s+=" so we cannot get feedback from our friends.";
		}
		else {
			s+=" importance effect for this feedback: "+(this.totalAdditionalimportance/this.additionalCounter);
			s+=" we ask "+this.additionalCounter+" different agent.";
		}
		return s;
	}

	
	
}
