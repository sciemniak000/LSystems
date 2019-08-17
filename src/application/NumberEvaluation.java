package application;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberEvaluation {
    public static String evaluateVariables(String original, HashMap<String, String> dict){
        //this pattern will allow us to find all the variables
        Pattern number = Pattern.compile(".?[0-9]+.?");
        Matcher matcher = number.matcher(original);
        String temporary;
        int beginindex, leng;

        while (matcher.find()){
            if(matcher.group(0).contains("'")){
                continue;
            } else {
                //carefully with the substitution because the indexes may vary
                temporary = matcher.group(0);
                beginindex = matcher.start();
                leng = temporary.length();

                //if the first character is not a digit, we should substitute beginning from the second character
                if(!Character.isDigit(temporary.charAt(0))){
                    beginindex += 1;
                    leng -= 1;
                }

                //if the last character is not a digit, we shouldn't include it in substitution
                if(!Character.isDigit(temporary.charAt(temporary.length() - 1))){
                    leng -= 1;
                }

                original = original.substring(0, beginindex) + "'" +
                        dict.get(temporary.replaceAll("[^0-9]", "")) + "'" +
                        original.substring(beginindex + leng);

                matcher = number.matcher(original);
            }
        }
        return original;
    }

    public static String evaluateArithmetics(String original){
        String arit_side = "'[a-zA-Z0-9]+'";
        String arit_sign = "[-+*/]";
        Pattern letter = Pattern.compile("[a-zA-Z]");
        Matcher lettersleft, lettersright;

        String left, centre, right, temporary = "";

        //we will evaluate only one equation at the time
        Pattern arithmetic = Pattern.compile("(" + arit_side + ")("  + arit_sign + ")(" + arit_side + ")");
        Matcher matcher = arithmetic.matcher(original);

        while (matcher.find()){
            left = matcher.group(1);
            centre = matcher.group(2);
            right = matcher.group(3);

            //there should be apostrophes which must be deleted
            left = left.replaceAll("'", "");
            right = right.replaceAll("'", "");
            lettersleft = letter.matcher(left);
            lettersright = letter.matcher(right);
            if(lettersleft.find() || lettersright.find()    ){
                original = original.substring(0, matcher.start()) + "'" +
                        left + right + "'" + original.substring(matcher.start() + matcher.group(0).length());
                matcher = arithmetic.matcher(original);
                continue;
            }
            if(centre.equals("+")){
                temporary = Integer.toString(Integer.parseInt(left) + Integer.parseInt(right));
            } else if(centre.equals("-")){
                temporary = Integer.toString(Math.abs(Integer.parseInt(left) - Integer.parseInt(right)));
            } else if(centre.equals("*")) {
                temporary = Integer.toString(Integer.parseInt(left) * Integer.parseInt(right));
            } else if(centre.equals("/")) {
                temporary = Integer.toString(Integer.parseInt(left) / Integer.parseInt(right));
            }

            original = original.substring(0, matcher.start()) + "'" +
                    temporary + "'" + original.substring(matcher.start() + matcher.group(0).length());
            matcher = arithmetic.matcher(original);
        }
        return original;
    }

    public static String evaluateLogical(String original){
        // those patterns will help to find logical expressions
        String logic_sign = "(?:<|>|<=|>=|==|!=)";
        Pattern logical = Pattern.compile("'([a-zA-Z0-9]+)'(" + logic_sign + ")'([a-zA-Z0-9]+)'");
        String left, centre, right, temporary = "";

        Matcher matcher = logical.matcher(original);
        while (matcher.find()){
            left = matcher.group(1);
            centre = matcher.group(2);
            right = matcher.group(3);
            if(left.contains("[a-zA-Z]") || right.contains("[a-zA-Z]")){
                if(centre.equals("==")){
                    if(left.equals(right)){
                        temporary = "TRUE";
                    } else {
                        temporary = "FALSE";
                    }
                } else if(centre.equals("!=")){
                    if(!left.equals(right)){
                        temporary = "TRUE";
                    } else {
                        temporary = "FALSE";
                    }
                } else {
                    temporary = "FALSE";
                }
            } else {
                if(centre.equals("<")){
                    if(Integer.parseInt(left) < Integer.parseInt(right)){
                        temporary = "TRUE";
                    } else {
                        temporary = "FALSE";
                    }
                } else if(centre.equals("<=")){
                    if(Integer.parseInt(left) <= Integer.parseInt(right)){
                        temporary = "TRUE";
                    } else {
                        temporary = "FALSE";
                    }
                } else if(centre.equals(">")){
                    if(Integer.parseInt(left) > Integer.parseInt(right)){
                        temporary = "TRUE";
                    } else {
                        temporary = "FALSE";
                    }
                } else if(centre.equals(">=")){
                    if(Integer.parseInt(left) >= Integer.parseInt(right)){
                        temporary = "TRUE";
                    } else {
                        temporary = "FALSE";
                    }
                } else if(centre.equals("==")){
                    if(Integer.parseInt(left) == Integer.parseInt(right)){
                        temporary = "TRUE";
                    } else {
                        temporary = "FALSE";
                    }
                } else if(centre.equals("!=")){
                    if(Integer.parseInt(left) != Integer.parseInt(right)){
                        temporary = "TRUE";
                    } else {
                        temporary = "FALSE";
                    }
                }
            }
            original = original.substring(0, matcher.start()) + temporary +
                    original.substring(matcher.start() + matcher.group(0).length());
            matcher = logical.matcher(original);
        }
        return original;
    }

    public static String evaluateLogicalAnds(String original){
        Pattern and = Pattern.compile("(TRUE|FALSE)&&(TRUE|FALSE)");
        Matcher matcher = and.matcher(original);
        String temporary = "";
        while (matcher.find()){
            if(matcher.group(1).equals("FALSE") || matcher.group(2).equals("FALSE")){
                temporary = "FALSE";
            } else {
                temporary = "TRUE";
            }
            original = original.substring(0, matcher.start()) + temporary +
                    original.substring(matcher.start() + matcher.group(0).length());
            matcher = and.matcher(original);
        }
        return original;
    }

    public static String evaluateLogicalOrs(String original){
        Pattern or = Pattern.compile("(TRUE|FALSE)\\|\\|(TRUE|FALSE)");
        Matcher matcher = or.matcher(original);
        String temporary = "";
        while (matcher.find()){
            if(matcher.group(1).equals("TRUE") || matcher.group(2).equals("TRUE")){
                temporary = "TRUE";
            } else {
                temporary = "FALSE";
            }
            original = original.substring(0, matcher.start()) + temporary +
                    original.substring(matcher.start() + matcher.group(0).length());
            matcher = or.matcher(original);
        }
        return original;
    }
}
