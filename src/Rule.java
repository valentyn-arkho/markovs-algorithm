import java.util.regex.Pattern;

public class Rule {

	private String from;
	private String to;
	
	private boolean last;
	
	public Rule(String from, String to){
		this.from = from;
		
		if (to.length() > 0 &&to.charAt(0) == '.'){
			last = true;
			this.to = to.substring(1, to.length());
		}
		else {
			this.to = to;
			last = false;
		}
	}
	
	public String getFrom(){
		return from;
	}
	
	public String getTo(){
		return to;
	}
	
	public String apply(String to){
		System.out.println(from + " => " + this.to);
		return to.replaceFirst(Pattern.quote(from), this.to);
	}
	
	public boolean canApply(String to){
		return to.contains(from);
	}
	
	public boolean isLast(){
		return last;
	}
	
	public String toString(){
		return from + "=>" + (last? "." : "") + to; 
	}
}
