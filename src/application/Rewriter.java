package application;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rewriter {
	 	private Turtle turtleinstance;
	    private static Rewriter singleton = null;
	    private ArrayList<String> conditions;
	    private Symbol head;
	    private Symbol tail;
	    private LinkedList<String> previous_states;
	    private final int memory_limit = 200;
	    private final int memory_string_length_limit = 200000;
	    private int list_step = 0;
	    private ArrayList<ArrayList<String>> drawing_info;

	 
	    public void kill() {
	    	nullify();
	    	list_step = 0;
	    	
	    	if(previous_states != null) {
	    		previous_states.clear();
	    	}
	    	if(conditions != null) {
	    	conditions.clear();
	    	}
	    	if(drawing_info != null) {
	    	drawing_info.clear();
	    	}
	    	
	    	Symbol.kill();
	    }
	
    private Rewriter(){
        head = null;
        tail = null;
        conditions = new ArrayList<>();
        previous_states = new LinkedList<>();
    }
    public static Rewriter get_rewriter(){
        if(singleton == null){
            singleton = new Rewriter();
        }
        return singleton;
    }

    public void addSymbol(Symbol add_it){
        if(head == null){
            head = add_it;
            tail = add_it;
        } else {
            tail.set_next(add_it);
            tail.get_next().set_previous(tail);
            tail = tail.get_next();
        }
    }

    public void addAttributeToTail(String attr){
        tail.addAtribute(attr);
    }

    public void addCondition(String cond){
        conditions.add(cond);
    }

    public void nullify(){
        head = null;
        tail = null;
    }
    
   

    public void print(){
        for(Symbol s = head; s != null; s = s.get_next()){
            for(String st : s.getAtributes()){
            }
        }
    }
   
    public void callDraw(){
        Symbol.draw(head, turtleinstance);
    }

    public void nextStep(){
        Symbol head2;
        String helpful_1, helpful_2, helpful_3;
        boolean nothing_changed;

        HashMap<String, String> dict = new HashMap<>();

        head2 = head;
        nullify();

        if(list_step == previous_states.size()){

            Pattern pattern = Pattern.compile("^(.*)==>(.*)$");
            Matcher matcher;

            for(Symbol s = head2; s != null; s = s.get_next()){
                nothing_changed = true;

                for(String str : conditions){
                    matcher = pattern.matcher(str);
                    matcher.find();
                    helpful_1 = matcher.group(1);
                    helpful_2 = matcher.group(2);
                    if(helpful_2.contains(":")){
                        helpful_3 = helpful_2.substring(helpful_2.indexOf(":") + 1);
                        helpful_2 = helpful_2.substring(0, helpful_2.indexOf(":"));
                    } else {
                        helpful_3 = null;
                    }

                    dict.clear();
                    if(!neighborsOk(s, helpful_1, dict)) continue;

                    if(helpful_3 != null){
                        if(!conditionsOK(helpful_3, dict)){
                            continue;
                        }
                    }
                    modifyL(helpful_2, dict);
                    nothing_changed = false;
                    break;
                }

              
                if(nothing_changed){
                    addSymbol(new Symbol(s.getType()));
                    for(String ss : s.getAtributes()){
                        addAttributeToTail(ss);
                    }
                }
            }

         
            rememberNewState();
        } else {
            
            list_step += 1;
            parseString(previous_states.get(list_step - 1));
        }
        Symbol.draw(head, turtleinstance);
    }

    public void previousStep(){
        if(list_step > 1){
            nullify();
            list_step -= 1;
            parseString(previous_states.get(list_step - 1));
            
        }
        Symbol.draw(head, turtleinstance);
    }

    private boolean neighborsOk(Symbol s, String first, HashMap<String, String> dict){
        
        Pattern name = Pattern.compile("^[a-zA-Z]+");

      
        Pattern number = Pattern.compile("[0-9]+");
        Matcher matcher;

       
        if(!first.contains(".")){
            matcher = name.matcher(first);
            matcher.find();

            if(!matcher.group(0).equals(s.getType())){
                return false;
            } else {
                matcher = number.matcher(first);
                int i = 0;
                while (matcher.find()){
                    dict.put(matcher.group(0), s.getAtributes().get(i));
                    i++;
                }
                return true;
            }
        } else {
            String[] strings = first.split("_");

            for(int i = 0; i < strings.length; i++) {
                if (!strings[i].contains(".")) {
                    continue;
                } else {
                    strings[i] = strings[i].replaceAll("\\.", "");
                    matcher = name.matcher(strings[i]);
                    matcher.find();
                    if (!matcher.group(0).equals(s.getType())) {
                        return false;
                    } else {
                        
                        strings[i] = strings[i] + ".";
                        Symbol test = s;
                        for (int j = i + 1; j < strings.length; j++, test = test.get_next()) {
                            matcher = name.matcher(strings[j]);
                            matcher.find();
                            if (!matcher.group(0).equals(test.getType())) {
                                return false;
                            }
                        }
                       
                        test = s;
                        for (int j = i - 1; j >= 0; j--, test = test.get_previous()) {
                            matcher = name.matcher(strings[j]);
                            matcher.find();
                            if (!matcher.group(0).equals(test.getType())) {
                                return false;
                            }
                        }
                    }
                    break;
                }
            }

            int x = 0;
          
            for(int i = 0; i < strings.length; i++) {
                if (!strings[i].contains(".")) {
                    continue;
                } else {
                    matcher = number.matcher(strings[i]);
                    x = 0;
                    while (matcher.find()){
                        dict.put(matcher.group(0), s.getAtributes().get(x));
                        x++;
                    }
                    Symbol test = s;
                    for (int j = i + 1; j < strings.length; j++, test = test.get_next()) {
                        matcher = number.matcher(strings[j]);
                        x = 0;
                        while (matcher.find()){
                            dict.put(matcher.group(0), test.getAtributes().get(x));
                            x++;
                        }
                    }

                    test = s;
                    for (int j = i - 1; j >= 0; j--, test = test.get_previous()) {
                        matcher = number.matcher(strings[j]);
                        x = 0;
                        while (matcher.find()){
                            dict.put(matcher.group(0), test.getAtributes().get(x));
                            x++;
                        }
                    }
                    break;
                }
            }
        }
        return true;
    }

    private boolean conditionsOK(String str, HashMap<String, String> dict){
        str = NumberEvaluation.evaluateVariables(str, dict);
        str = NumberEvaluation.evaluateArithmetics(str);
        str = NumberEvaluation.evaluateLogical(str);
        str = NumberEvaluation.evaluateLogicalAnds(str);
        str = NumberEvaluation.evaluateLogicalOrs(str);
        if(str.equals("TRUE")){
            return true;
        } else {
            return false;
        }
    }

    private void modifyL(String str, HashMap<String, String> dict){
        if(str.equals("_")){
            return;
        }
        Pattern name = Pattern.compile("^[a-zA-Z]+");
        Pattern attributes = Pattern.compile("'\\w+'");
        Matcher matcher;
        str = NumberEvaluation.evaluateVariables(str, dict);
        str = NumberEvaluation.evaluateArithmetics(str);
        String[] strings = str.split("_");
        for(int i = 0; i < strings.length; i++){
            matcher = name.matcher(strings[i]);
            matcher.find();
            addSymbol(new Symbol(matcher.group(0)));
            matcher = attributes.matcher(strings[i]);
            while (matcher.find()){
                addAttributeToTail(matcher.group(0).substring(1, matcher.group(0).length()-1));
            }
        }
    }

    public void loopDraw(int loops, int millis_wait){
        for(int i = 0; i < loops; i++){
            try {
                Thread.sleep(millis_wait);
            } catch (InterruptedException e){
                System.out.println(e.getMessage());
            }
            nextStep();
            print();
        }
    }

    public void addDrawingInformation(ArrayList<String> newbie){
        drawing_info.add(newbie);
    }

    private void parseString(String s) {
        String temporary = "";

        String argument = "'[a-zA-Z0-9]+'";

        Pattern name = Pattern.compile("[a-zA-Z]+");
        Matcher matcher;
        

        String[] strings = s.split("_");
        for (int i = 0; i < strings.length; i++) {

            matcher = name.matcher(strings[i]);
            matcher.find();
            temporary = matcher.group(0);
            
            addSymbol(new Symbol(temporary));

            matcher = Pattern.compile(argument).matcher(strings[i]);

            while (matcher.find()){
                temporary = matcher.group(0);
                temporary = temporary.substring(1, temporary.length() - 1);

                addAttributeToTail(temporary);

            }
        }
    }

    public void rememberNewState(){
        if(!previous_states.isEmpty() && previous_states.peekLast().length() >= memory_string_length_limit){
            return;
        }

        String temporary = "";
        for(Symbol s = head; s != null; s = s.get_next()){
            temporary = temporary + "_" + s.getType();
            if(s.getAtributes().size() != 0){
                temporary = temporary + "('" + s.getAtributes().get(0) + "'";
                for(int i = 1; i < s.getAtributes().size(); i++){
                    temporary = temporary + ",'" + s.getAtributes().get(i) + "'";
                }
                temporary = temporary + ")";
            }
        }

        if(previous_states.size() == memory_limit){
            previous_states.pollFirst();
            list_step -=1;
        }

       
        temporary = temporary.substring(1);
        previous_states.add(temporary);
        list_step += 1;
    }
	public Turtle getTurtle() {
		return turtleinstance;
	}
	public void setTurtle(Turtle turtle) {
		this.turtleinstance = turtle;
	}
}
