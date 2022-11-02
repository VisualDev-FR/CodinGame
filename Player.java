import java.util.*;

class Player{

    public static int myPosition;

    public static String[] runes;    
    public static String[] letters;

    public static String magicPhrase;
    public static String instructions;

    public static void main(String args[]){

        INITIALIZE();

        for (int i = 0; i < magicPhrase.length(); i++) {
            
            String currentLetter = magicPhrase.substring(i, i + 1);
            //String remainingString = magicPhrase.substring(i); //TODO: use this to find patterns

            Action bestAction = findBestRune(myPosition, currentLetter);
            
            instructions += bestAction.instruction;
            runes[bestAction.modifiedRuneID] = bestAction.modifiedLetter;
            myPosition = bestAction.modifiedRuneID;
        }

        System.out.println(instructions);
    }

    public static class Action {

        public String instruction;
        public String modifiedLetter;
        public int modifiedRuneID;

        public Action(String instruction_, String letter_, int runeID_){
            this.instruction = instruction_;
            this.modifiedLetter = letter_;
            this.modifiedRuneID = runeID_;
        }

        public int length(){
            return this.instruction.length();
        }
    }

    public static Action findBestRune(int actualPosition, String letterToSet){

        Action bestAction = null;
        int bestLenght = Integer.MAX_VALUE;

        for (int i = 0; i < runes.length; i++) {
            String move = getInstructionsFor(myPosition, i, letterToSet);

            Action action = new Action(move, letterToSet, i);

            if(action.length() < bestLenght){
                bestLenght = action.length();
                bestAction = action;
            }
        }

        return bestAction;
    }

    public static String getInstructionsFor(int myPosition, int runeIDToSet, String letterToSet){
        return MOVE_TO(myPosition, runeIDToSet) + trySetLetter(runes[runeIDToSet], letterToSet) + ".";
    }

    public static String trySetLetter(String actualLetter, String letter){

        int runeIndex = getCharIndex(actualLetter);
        int letterIndex = getCharIndex(letter);

        String instruction = "";
        int repeatCount = minimizeDelta(letterIndex - runeIndex);

        if(repeatCount > 0){
            instruction += repeat("+", repeatCount);
        }else{
            instruction += repeat("-", -repeatCount);
        }

        return instruction;
    }    



    public static boolean letterExists(String letter){
        return Arrays.asList(runes).indexOf(letter) > 0;
    }
    
    public static Map<String, Integer> getPatterns(String phrase){

        Map<String, Integer> patterns = new HashMap<String, Integer>();

        for(int len = 2; len < phrase.length(); len++){

            String beginPattern = phrase.substring(0, len);
            String endPattern = phrase.substring(phrase.length() - len, phrase.length());

            int beginCount = phrase.split(beginPattern, -1).length - 1;
            int endCount = phrase.split(endPattern, -1).length - 1;

            if(beginCount > 1 && !patternIsIn(patterns, beginPattern)){
                patterns.put(beginPattern, beginCount);
            }

            if(endCount > 1 && !patternIsIn(patterns, endPattern)){
                patterns.put(endPattern, endCount);
            }            
        }

        return patterns;
    }

    private static boolean patternIsIn(Map<String, Integer> patternsDico, String patternToFind){
        
        for(String pattern : patternsDico.keySet()){
            if(patternToFind.contains(pattern)) return true;
        }
        return false;
    }

    public static void main_OLD(){

        INITIALIZE();

        writeAllUsedLetters();

        int lastRuneID = myPosition + 1;

        for(int i = 0; i < magicPhrase.length(); i++){

            String currentLetter = magicPhrase.substring(i, i + 1);

            int runeID = findRune(currentLetter);
            instructions += MOVE_TO(myPosition, runeID) +".";
            //instructions += setLetter(0, currentLetter) + ".";
        }         

        System.err.println(instructions.length());

        PRINT_INSTRUCTIONS();
    }

    private static String MOVE_TO(int actualPosition, int runeID){
        
        int delta = runeID - actualPosition;
        actualPosition = runeID;
        
        if(delta > 0){
            return repeat(">", delta);
        }
        else if(delta < 0){
            return repeat("<", -delta);
        }
        else{
            return "";
        }
    }

    private static void writeAllUsedLetters(){

        String withoutDuplicates = removeStringDuplicates(magicPhrase);
        
        System.err.printf("%s : %s\n", withoutDuplicates.length(), withoutDuplicates);        

        for(int i = 0; i < withoutDuplicates.length(); i++){

            String currentLetter = withoutDuplicates.substring(i, i + 1);

            instructions += setLetter(myPosition, currentLetter);
            if(i < withoutDuplicates.length() - 1) MOVE_RIGHT();
        }        
    }

    private static String repeat(String s, int count){
        return new String(new char[count]).replace("\0", s);
    }

    public static int getCharIndex(String charLetter){
        return Arrays.asList(letters).indexOf(charLetter);
    }

    public static int findRune(String letter){
        return Arrays.asList(runes).indexOf(letter);
    }

    public static String setLetter(int runeID, String letter){

        int runeIndex = getCharIndex(runes[runeID]);
        int letterIndex = getCharIndex(letter);

        String instruction = "";
        int repeatCount = minimizeDelta(letterIndex - runeIndex);

        if(repeatCount > 0){
            instruction += repeat("+", repeatCount);
        }else{
            instruction += repeat("-", -repeatCount);
        }

        //System.err.printf("%s(%s) -> %s(%s) : %s (%s)\n", runes[runeID], runeIndex, letter, letterIndex, instruction, repeatCount);
        
        runes[runeID] = letter; 
        
        return instruction;
    }

    public static String resetRune(int runeID){

        int runeIndex = getCharIndex(runes[runeID]);

        if(runeIndex > 13){
            return "[+]";
        }else{
            return "[-]";
        }
    }

    public static int minimizeDelta(int delta){

        if(delta > 13){
            return delta - 27;
        }   
        else if(delta > 0 && delta <= 13){
            return delta;
        }   
        else if(delta < 0 && delta >= -13) {
            return delta;
        }
        else if(delta < -13){
            return delta + 27;
        }
        else if(delta == 0){
            return 0;
        }
        else{
            System.err.println("errValue = " + delta);
            assert false;
            return - 1;
        }
    }

    public static String removeStringDuplicates(String strWithDuplicates){

        String strWithoutDuplicates = "";

        for(int i = 0; i < strWithDuplicates.length(); i++){
            CharSequence subString = strWithDuplicates.subSequence(i, i+1);
            if(!strWithoutDuplicates.contains(subString) && !subString.equals(" ")){
                strWithoutDuplicates += subString;
            }
        }

        return strWithoutDuplicates;
    }    

    public static void INITIALIZE(){

        Scanner in = new Scanner(System.in);

        magicPhrase = in.nextLine();
        in.close();

        instructions = "";        
        runes = repeat(" ", 30).split("");
        letters = " ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
    }

    public static void PRINT_INSTRUCTIONS(){

        System.out.println(instructions);
    }

    public static void MOVE_RIGHT(){
        instructions += ">";
        myPosition = (myPosition + 1) % 30;
    }

    public static void MOVE_LEFT(){
        instructions += "<";
        myPosition = (myPosition - 1) % 30;
    }

}