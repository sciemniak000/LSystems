package application;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private ArrayList<String> alphabet;
    private ArrayList<Integer> how_many_attributes;

    private Parser() {
        alphabet = new ArrayList<>();
        how_many_attributes = new ArrayList<>();
    }

    public static Parser getParser() {
        if (singleton == null) {
            singleton = new Parser();
        }
        return singleton;
    }

    private static Parser singleton = null;

    public void parse(File filename) throws IOException {
    	alphabet.clear();
    	Rewriter.get_rewriter().kill();
        String path = filename.getAbsolutePath();
        String file = "";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {

                file = file + line;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        file = file.replaceAll("\\s+", "");

        Pattern squares_splitter_pat = Pattern.compile("^\\[v](.+)\\[r](.+)\\[s](.+)\\[d](.+)\\[e]$");
        Matcher sq_sp_mat = squares_splitter_pat.matcher(file);

        if (sq_sp_mat.find()) {
            String v = sq_sp_mat.group(1);
            String r = sq_sp_mat.group(2);
            String s = sq_sp_mat.group(3);
            String d = sq_sp_mat.group(4);
            parseV(v);
            parseR(r);
            parseS(s);
            parseD(d);

        } else {
            throw new IOException("Syntax error. Pattern [v]_[r]_[s]_[d]_[e] not found in file");
        }
    }
    
    public void parse(String str) throws IOException {
    	alphabet.clear();
    	Rewriter.get_rewriter().kill();     
        String file = str;       
       

        file = file.replaceAll("\\s+", "");

        Pattern squares_splitter_pat = Pattern.compile("^\\[v](.+)\\[r](.+)\\[s](.+)\\[d](.+)\\[e]$");
        Matcher sq_sp_mat = squares_splitter_pat.matcher(file);

        if (sq_sp_mat.find()) {
            String v = sq_sp_mat.group(1);
            String r = sq_sp_mat.group(2);
            String s = sq_sp_mat.group(3);
            String d = sq_sp_mat.group(4);
            parseV(v);
            parseR(r);
            parseS(s);
            parseD(d);

        } else {
            Main.showInfoDialog("Syntax error."," Pattern [v]_[r]_[s]_[d]_[e] not found in file");
        }
    }

    private void parseV(String vSectionpPrser) throws IOException {

        String[] strings = vSectionpPrser.split(";");

        Pattern pattern = Pattern.compile("^[a-zA-Z]+(?:\\([0-9]+(?:,[0-9]+)*\\))?$");

        Pattern name = Pattern.compile("^[a-zA-Z]+");
        Pattern sub = Pattern.compile("\\d+");
        Matcher matcher;
        String temporary;

        for (int i = 0; i < strings.length; i++) {

            matcher = pattern.matcher(strings[i]);
            if (!matcher.matches()) {
                throw new IOException("Syntax error in [v] section, symbol " + i + " has wrong syntax");
            }

            matcher = name.matcher(strings[i]);
            matcher.find();
            temporary = matcher.group(0);

            if (!inAlphabet(temporary)) {
                alphabet.add(temporary);
                how_many_attributes.add(0);

                matcher = sub.matcher(strings[i]);
                while (matcher.find()) {
                    how_many_attributes.set(how_many_attributes.size() - 1,
                            how_many_attributes.get(how_many_attributes.size() - 1) + 1);
                }
            } else {
                throw new IOException("Syntax error, symbol " + i + " was to be defined second time");
            }
        }

    }

    private void parseR(String rSectionParser) throws IOException {
        String[] strings = rSectionParser.split(";");
        ArrayList<String> attributes;

        String whole = "^(.*)==>(.*)$";

        Matcher matcher;

        String first, second, third;
        for(int i = 0; i < strings.length; i++){

            matcher = Pattern.compile(whole).matcher(strings[i]);
            if(!matcher.matches()){
                throw new IOException("Syntax error in " + i + " part of the [r] part");
            }

            first = matcher.group(1);

            second = matcher.group(2);

            if(second.contains(":")){
                third = second.substring(second.indexOf(":") + 1);
                second = second.substring(0, second.indexOf(":"));
            } else {
                third = null;
            }
           

            attributes = rProcessFirstPart(first);

            rProcessSecondPart(second, attributes);

            if(third != null) {
                rProcessThirdPart(third, attributes);
            }

         
            Rewriter.get_rewriter().addCondition(strings[i]);
        }

    }

    private void parseS(String sSectionParser) throws IOException {
        Rewriter rewriter = Rewriter.get_rewriter();
        String temporary = "", temporary2 = "";
        int x = 0;
        String argument = "(?:'[a-zA-Z]+'|'[0-9]+')";
        String symbol_pattern = "[a-zA-Z]+(?:\\(" + argument +
                "(?:," + argument + ")*\\))?";

        Pattern aksjo = Pattern.compile("^" + symbol_pattern +
                "(?:_" + symbol_pattern + ")*;$");

        Pattern name = Pattern.compile("[a-zA-Z]+");

        Matcher matcher = aksjo.matcher(sSectionParser);
        if (!matcher.matches()) {
            Main.showInfoDialog("Syntax error", "In [s] section");
        }

        String[] strings = sSectionParser.split("_");
        for (int i = 0; i < strings.length; i++) {

            matcher = name.matcher(strings[i]);
            matcher.find();
            temporary = matcher.group(0);

            if(!inAlphabet(temporary)){
                Main.showInfoDialog("Syntax error", "In [s] section, symbol " + temporary + " is not defined");
            }

            rewriter.addSymbol(new Symbol(temporary));

            matcher = Pattern.compile(argument).matcher(strings[i]);

            x = 0;
            while (matcher.find()){
                temporary2 = matcher.group(0);
                temporary2 = temporary2.substring(1, temporary2.length() - 1);

                rewriter.addAttributeToTail(temporary2);
                x += 1;

            }
            if(x != how_many_attributes.get(alphabet.indexOf(temporary))){
            	Main.showInfoDialog("Error"," In [s] section, symbol " + temporary + " has wrong number of attributes");
            }
        }
        Rewriter.get_rewriter().rememberNewState();
    }

    private void parseD(String dSectionParser) throws IOException {
        String arit_side = "(?:(?:[0-9]+)|(?:'[0-9a-zA-Z]+'))";
        String arit_sign = "[-+*/]";
        String argument = arit_side + "(?:"  + arit_sign + arit_side + ")*";
        String symbol = "[a-zA-Z]+(?:\\([0-9]+(?:,[0-9]+)*\\))?";
        String mt = "mt\\(" + argument + "," + argument + "\\)";
        String rmb = "rmb\\(\\)";
        String gb = "gb\\(\\)";
        String ft = "ft\\(" + argument + "," + argument + "\\)";
        String f = "f\\(" + argument + "\\)";
        String sd = "sd\\(" + argument + "\\)";
        String l = "l\\(" + argument + "\\)";
        String r = "r\\(" + argument + "\\)";
        String pu = "pu\\(\\)";
        String pd = "pd\\(\\)";

        String function = "(?:" + mt + "|" + rmb + "|" + gb + "|" + f + "|"
                + ft + "|" + sd + "|" + l + "|" + r + "|" + pu + "|" + pd + ")";
        String row = symbol + "==>" + function + "(?:;" + function + ")*;;";
        String whole = "^(?:" + row + "){" + alphabet.size() + "}$";

        Pattern name = Pattern.compile("^[a-zA-Z]+");
        Pattern nonApoNumber = Pattern.compile("[0-9]+");
        Pattern number = Pattern.compile(".?[0-9]+.?");
        String temporary;

        Pattern pattern = Pattern.compile(whole);
        Matcher matcher = pattern.matcher(dSectionParser);
        if(!matcher.matches()){
            Main.showInfoDialog("Syntax error."," In [d] part of file");
        }

        ArrayList<String> already_defined = new ArrayList<>();

        String[] strings = dSectionParser.split(";;");
        for(int i = 0; i < strings.length; i++){

            matcher = name.matcher(strings[i]);
            matcher.find();
            temporary = matcher.group(0);
            if(!inAlphabet(temporary)){
            	Main.showInfoDialog("Warning. ", "Symbol " + temporary + " in [d] section is undefined");
            }

            if(already_defined.contains(temporary)){
            	Main.showInfoDialog("Warning" , "Drawing for symbol " + temporary + " defined more than once");
            }
            already_defined.add(temporary);
        }

        for(int i = 0; i < strings.length; i++){
            already_defined.clear();
            matcher = Pattern.compile(symbol).matcher(strings[i]);
            matcher.find();
            temporary = matcher.group(0);
            matcher = nonApoNumber.matcher(temporary);
            while (matcher.find()){
                if(already_defined.contains(matcher.group(0))){
                	Main.showInfoDialog("Warning. ", " " + matcher.group(0) + " was defined more than once in " + strings[i]);
                }
                already_defined.add(matcher.group(0));
            }

            matcher = name.matcher(strings[i]);
            matcher.find();
            if(already_defined.size() != how_many_attributes.get(alphabet.indexOf(matcher.group(0)))){
            	Main.showInfoDialog("Warning. ", "The amount of attributes of symbol " + matcher.group(0) + " is not correct");
            }

            matcher = number.matcher(strings[i]);
            while (matcher.find()){
                temporary = matcher.group(0);

                if(temporary.contains("'")){
                    continue;
                }

                temporary = temporary.replaceAll("[^0-9]", "");
                if(!already_defined.contains(temporary)){
                	Main.showInfoDialog("Warning. ", "Undefined symbol " + temporary + " used in " + strings[i] + " in [d] section");
                }
            }
            Symbol.addDrawingInfo(strings[i]);
        }
        Rewriter.get_rewriter().callDraw();
    }

    private boolean inAlphabet(String str) {
        if (alphabet.size() == 0) {
            return false;
        }
        boolean exists = false;
        for (String s : alphabet) {
            if (s.equals(str)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    private ArrayList<String> rProcessFirstPart(String part) throws IOException{
        String temporary, temporary2, temporary3;
        int xcounter;

        String part_one_symbol = "[a-zA-Z]+(?:\\([0-9]+(?:,[0-9]+)*\\))?";
        String combined_version_part_one = "^(?:" + part_one_symbol + "_\\.)*" + part_one_symbol +
                "(?:\\._" + part_one_symbol + ")*$";

        Pattern number = Pattern.compile("[0-9]+");
        Pattern name = Pattern.compile("^[a-zA-Z]+");

        ArrayList<String> attributes = new ArrayList<>();

        Matcher matcher = Pattern.compile(combined_version_part_one).matcher(part);

        if(!matcher.matches()){
            throw new IOException("Syntax error in [r] in " + part);
        }

        Matcher matcher2;
        matcher = Pattern.compile(part_one_symbol).matcher(part);

        while (matcher.find()){
            temporary = matcher.group(0);

            matcher2 = name.matcher(temporary);
            matcher2.find();

            temporary2 = matcher2.group(0);

            if(!inAlphabet(temporary2)){
            	Main.showInfoDialog("Syntax error. ", "In [r] in " + part + " - symbol " + temporary2 + " was not defined");
            }

            
            matcher2 = number.matcher(temporary);
            xcounter = 0;
            while (matcher2.find()){
                temporary3 = matcher2.group(0);
                if(attributes.contains(temporary3)){
                	Main.showInfoDialog("Syntax error." , "In [r] " + part +
                            " - attribute " + temporary3 + " declared twice");
                } else {
                    attributes.add(temporary3);
                    xcounter++;
                }
            }

            if(xcounter != how_many_attributes.get(alphabet.indexOf(temporary2))){
            	Main.showInfoDialog("Syntax error." , "In [r] " + part +
                        " - wrong number of attributes of " + temporary2);
            }
        }
        return attributes;
    }

    private void rProcessSecondPart(String second, ArrayList<String> attributes) throws IOException{
        String temporary, temporary2, temporary3;
        int xcounter;

        String arit_side = "(?:(?:[0-9]+)|(?:'[0-9]+')|(?:'[a-zA-Z]+'))";
        String arit_sign = "[-+*/]";
        String arithmetic = arit_side + "(?:"  + arit_sign + arit_side + ")*";
        String symbol = "[a-zA-Z]+(?:\\(" + arithmetic + "(?:," + arithmetic + ")*\\))?";
        String whole = "(?:(?:" + symbol + "(?:_" + symbol + ")*)|(?:_))";
        String name = "^[a-zA-Z]+";
        String number = ".?[0-9]+.?";

        Pattern pattern = Pattern.compile(whole);
        Matcher matcher2, matcher = pattern.matcher(second);
        if(!matcher.matches()){
            
        	Main.showInfoDialog("Syntax error." , "In [r], specifically in " + second);
        }


        pattern = Pattern.compile(symbol);
        matcher = pattern.matcher(second);

        while (matcher.find()){
            temporary = matcher.group(0);

            matcher2 = Pattern.compile(name).matcher(temporary);
            matcher2.find();
            temporary2 = matcher2.group(0);

            if(!inAlphabet(temporary2)){
            	Main.showInfoDialog("Syntax error." , "In [r] in " + second + " - symbol "
                        + temporary2 + " was not defined");
            }

            xcounter = 0;
            if(temporary.contains("(")){
                temporary3 = temporary.substring(temporary.indexOf("("), temporary.length() - 1);
                xcounter = temporary3.split(",").length;
            }
            if(xcounter != how_many_attributes.get(alphabet.indexOf(temporary2))){
            	Main.showInfoDialog("Syntax error." , "In [r] " + second +
                        " - wrong number of attributes of " + temporary2);
            }
        }

        pattern = Pattern.compile(number);
        matcher = pattern.matcher(second);

        while (matcher.find()){
            temporary = matcher.group(0);
            if(temporary.contains("'")){
                continue;
            } else {
                temporary = temporary.replaceAll("[^0-9]", "");
                if(!attributes.contains(temporary)){
                	Main.showInfoDialog("Syntax error." , "In [r] " + second + ", attribute " + temporary + " not defined");
                }
            }
        }
    }

    private void rProcessThirdPart(String third, ArrayList<String> attributes) throws IOException{
        String temporary;

        String arit_side = "(?:(?:[0-9]+)|(?:'[0-9]+')|(?:'[a-zA-Z]+'))";
        String arit_sign = "[-+*/]";
        String arithmetic = arit_side + "(?:"  + arit_sign + arit_side + ")*";
        String logic_sign = "(?:<|>|<=|>=|==|!=)";
        String logical = arithmetic + logic_sign + arithmetic;
        String logic_connector = "(?:&&|\\|\\|)";
        String whole = "^" + logical + "(?:" + logic_connector + logical + ")*$";

        String number = ".?[0-9]+.?";

        Pattern pattern = Pattern.compile(whole);
        Matcher matcher = pattern.matcher(third);
        if(!matcher.matches()){
        	Main.showInfoDialog("Syntax error." , "In [r] in " + third);
        }

        pattern = Pattern.compile(number);
        matcher = pattern.matcher(third);

        while (matcher.find()){
            temporary = matcher.group(0);
            if(temporary.contains("'")){
                continue;
            } else {
                temporary = temporary.replaceAll("[^0-9]", "");
                if(!attributes.contains(temporary)){
                	Main.showInfoDialog("Syntax error." , "In [r] " + third + ", attribute " + temporary + " not defined");
                }
            }
        }
    }

	
}