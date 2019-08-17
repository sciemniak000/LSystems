package application;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Symbol {
    public Symbol(String type){
        this.type = type;
        next = null;
        previous = null;
        atributes = new ArrayList<>();
        number_of_attributes = 0;
    }
    public void set_next(Symbol to_be_set){
        next = to_be_set;
    }
    public void set_previous(Symbol to_be_set) {
        previous = to_be_set;
    }
    public Symbol get_next(){
        return next;
    }
    public Symbol get_previous(){
        return previous;
    }

    public void addAtribute(String attr){

        atributes.add(attr);
        number_of_attributes += 1;
    }

    public String getType(){
        return type;
    }

    public int getNumberOfAttributes(){
        return number_of_attributes;
    }

    public ArrayList<String> getAtributes(){
        return atributes;
    }

    public static void addDrawingInfo(String info){
        drawing_info.add(info);
    }

    private String type;
    private ArrayList<String> atributes;
    private Symbol previous;
    private Symbol next;
    private int number_of_attributes;
    private static ArrayList<String> drawing_info = new ArrayList<>();
    
    public static void kill() {
    	if(drawing_info != null) {
    	drawing_info.clear();
    	}
    }
    
    public static void draw(Symbol head, Turtle turtleinstance){    
    	
        Pattern name = Pattern.compile("^[a-zA-Z]+");
        Pattern separation = Pattern.compile("^(.+)==>(.+)$");
        Pattern nonApoNumber = Pattern.compile("[0-9]+");
        HashMap<String, String> key = new HashMap<>();
        Matcher matcher;
        String beginning, end;
        int xcounter;

        for(Symbol s = head; s != null; s = s.get_next()){
            for(String str : drawing_info){
                matcher = name.matcher(str);
                matcher.find();
                if(matcher.group(0).equals(s.getType())){
                    key.clear();
                    xcounter = 0;

                    matcher = separation.matcher(str);
                    matcher.find();
                    beginning = matcher.group(1);
                    end = matcher.group(2);

                    matcher = nonApoNumber.matcher(beginning);
                    while (matcher.find()){
                        key.put(matcher.group(0), s.getAtributes().get(xcounter));
                        xcounter++;
                    }
                    end = NumberEvaluation.evaluateVariables(end,key);
                    end = NumberEvaluation.evaluateArithmetics(end);
                    callTurtleDrawer(end,turtleinstance);
                }
            }
        }
    }
    private static void callTurtleDrawer(String instructions, Turtle turtleinstance){
        Pattern name = Pattern.compile("^[a-zA-Z]+");
        Matcher matcher;
        String temporary = "";
        String arg1, arg2;
        for(String s : instructions.split(";")){
            s = s.replaceAll("'", "");

            matcher = name.matcher(s);
            matcher.find();
            temporary = matcher.group(0);

        
            if(temporary.equals("mt")){
                temporary = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                arg1 = temporary.substring(0, temporary.indexOf(","));
                arg2 = temporary.substring(temporary.indexOf(",") + 1);
              
                	turtleinstance.moveTo(Integer.parseInt(arg1),Integer.parseInt(arg2));
                
            } else if(temporary.equals("rmb")){
            	turtleinstance.remember();
            } else if(temporary.equals("gb")){
            	turtleinstance.goBack();
            } else if(temporary.equals("ft")){
                temporary = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                arg1 = temporary.substring(0, temporary.indexOf(","));
                arg2 = temporary.substring(temporary.indexOf(",") + 1);
                
                turtleinstance.forwardTo(Integer.parseInt(arg1),Integer.parseInt(arg2));
                
            } else if(temporary.equals("f")){
                arg1 = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                
                turtleinstance.forward(Integer.parseInt(arg1));
                
            } else if(temporary.equals("sd")){
                arg1 = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                
                turtleinstance.setDirection(Integer.parseInt(arg1));
                
            } else if(temporary.equals("l")){
                arg1 = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                
                turtleinstance.left(Integer.parseInt(arg1));
                
            } else if(temporary.equals("r")){
                arg1 = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                
                turtleinstance.right(Integer.parseInt(arg1));
                
            } else if(temporary.equals("pu")){
            	turtleinstance.penUp();
            } else if(temporary.equals("pd")){
            	turtleinstance.penDown();
            }
        }
    }
}
