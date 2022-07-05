import java.util.*;

class Player{

    public static int myPosition;

    public static String[] runes;
    public static String[] magicPhrase;
    public static String[] letters;

    public static List<String> instructions;

    public static void main(String args[]){

        INITIALIZE();

        for(int i = 0; i < magicPhrase.length; i++){

            int runeID = i % runes.length;

            String currentLetter = magicPhrase[i];

            setLetter(runeID, currentLetter);

            if(i < magicPhrase.length - 1) MOVE_RIGHT();            

        }


        PRINT_INSTRUCTIONS();
    }

    public static void setLetter(int runeID, String letter){

        String currentLetter = runes[runeID];

        int letterIndexToSet = Arrays.asList(letters).indexOf(letter);
        int letterIndexActual = Arrays.asList(letters).indexOf(currentLetter);

        int difference = letterIndexActual - letterIndexToSet;

        if (difference > 0){

            for(int i = 0; i < Math.abs(difference); i++){
                DECREASE();
            }

        }else if(difference < 0){

            for(int i = 0; i < Math.abs(difference); i++){
                INCREASE();
            }
        }

        instructions.add(".");
    }

    public static void INITIALIZE(){

        Scanner in = new Scanner(System.in);

        magicPhrase = in.nextLine().split(""); 

        in.close();

        instructions = new ArrayList<String>();
        
        runes = new String[30];        

        for(int i = 0; i < runes.length; i++){
            runes[i] = " ";
        }

        letters = new String[27];

        letters[0] = " ";
        letters[1] = "A";
        letters[2] = "B";
        letters[3] = "C";
        letters[4] = "D";
        letters[5] = "E";
        letters[6] = "F";
        letters[7] = "G";
        letters[8] = "H";
        letters[9] = "I";
        letters[10] = "J";
        letters[11] = "K";
        letters[12] = "L";
        letters[13] = "M";
        letters[14] = "N";
        letters[15] = "O";
        letters[16] = "P";
        letters[17] = "Q";
        letters[18] = "R";
        letters[19] = "S";
        letters[20] = "T";
        letters[21] = "U";
        letters[22] = "V";
        letters[23] = "W";
        letters[24] = "X";
        letters[25] = "Y";
        letters[26] = "Z";
    }

    public static void MOVE_RIGHT(){
        instructions.add(">");
    }

    public static void MOVE_LEFT(){
        instructions.add("<");
    }

    public static void DECREASE(){
        instructions.add("-");
    }

    public static void INCREASE(){
        instructions.add("+");
    }

    public static void PRINT_INSTRUCTIONS(){
        
        String[] instructionsArray = instructions.toArray(new String[instructions.size()]);

        System.out.println(String.join("", instructionsArray));
    }


}