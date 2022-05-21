import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

class Solution {

    static TreeMap<Character, String> traductions;

    static String morseSequence;    
    static int dictionaryCount;

    static String[] morseWords;
    static int[] lengthTable;   

    public static void main(String args[]) {

        InitializeLetters();

        LocalSession();
        //OnlineSession();

    }

    static int GetCominaisons(String morseSequence){

        //Debug(morseSequence);
        //Debug(" ");
        
        int totalCombinaisons = 0;

        for(String word : morseWords){

            //Debug(word);

            if(morseSequence.indexOf(word) > -1){
                
                if(morseSequence.length() - word.length() == 0){

                    totalCombinaisons++;
                
                }else{
                    String cuttedSequence = morseSequence.replaceFirst(word, "");
                    
                    totalCombinaisons += GetCominaisons(cuttedSequence);
                }
            }
        }
        return totalCombinaisons;
    }

    static String TraduceToMorse(String word){
        
        String finalString = "";

        for(int i = 0; i < word.length(); i++){
            
            char currentLetter = word.charAt(i);

            finalString += traductions.get(currentLetter); 
        }

        return finalString;
    }

    static void ReadInputs(){

        Scanner in = new Scanner(System.in);

        morseSequence = in.next();        
        dictionaryCount = in.nextInt();

        morseWords = new String[dictionaryCount];
        lengthTable = new int[dictionaryCount];
        
        for (int i = 0; i < dictionaryCount; i++) {
            morseWords[i] = TraduceToMorse(in.next());
            lengthTable[i] = morseWords[i].length();
        }
    }

    static void InitializeLetters(){

        traductions = new TreeMap<Character, String>();

        traductions.put('A', ".-"   );
        traductions.put('B', "-..." );
        traductions.put('C', "-.-." );
        traductions.put('D', "-.."  );
        traductions.put('E', "."    );
        traductions.put('F', "..-." );
        traductions.put('G', "--."  );
        traductions.put('H', "...." );
        traductions.put('I', ".."   );
        traductions.put('J', ".---" );
        traductions.put('K', "-.-"  );
        traductions.put('L', ".-.." );
        traductions.put('M', "--"   );
        traductions.put('N', "-."   );
        traductions.put('O', "---"  );
        traductions.put('P', ".--." );
        traductions.put('Q', "--.-" );
        traductions.put('R', ".-."  );
        traductions.put('S', "..."  );
        traductions.put('T', "-"    );
        traductions.put('U', "..-"  );
        traductions.put('V', "...-" );
        traductions.put('W', ".--"  );
        traductions.put('X', "-..-" );
        traductions.put('Y', "-.--" );
        traductions.put('Z', "--.." );

    }

    static void Debug(String message){
        System.err.println(message);
    }

    //Functions for local tests

    static void OnlineSession(){

        ReadInputs();

        long totalCombinaisons = GetCominaisons(morseSequence);

        System.out.println(totalCombinaisons);        
    }

    static void LocalSession(){

        System.out.println("Validator to run ?");
        
        int number = new Scanner(System.in).nextInt();
        
        System.out.println(" ");

        if(number == -1){

            for(int i = 1; i < 6; i++){

                long answer = GetValidator(i);
        
                long totalCombinaisons = GetCominaisons(morseSequence);

                System.out.printf("Validator %s : Answer = %d / Found = %d \n\n", i, answer, totalCombinaisons);

            }            
        }else{
            long answer = GetValidator(number);
            
            long totalCombinaisons = GetCominaisons(morseSequence);

            System.out.printf("Validator %s : Answer = %d / Found = %d \n\n", number, answer, totalCombinaisons);            
        }        

        System.out.println(" ");
    }

    static long GetValidator(int number){

        switch (number){

            case 1: return Validator_1();
            case 2: return Validator_2();
            case 3: return Validator_3();
            case 4: return Validator_4();
            case 5: return Validator_5();
            case 6: return Validator_6();
        }

        return -1;
    }

    static void ParseValidator(String[] unicodeWords){

        morseWords = new String[unicodeWords.length];
        lengthTable = new int[unicodeWords.length];
        
        for (int i = 0; i < morseWords.length; i++) {
            morseWords[i] = TraduceToMorse(unicodeWords[i]);
            lengthTable[i] = morseWords[i].length();
        }        

    }

    static long Validator_1(){

        morseSequence = "-.-";            
        String[] unicodeWords = "A B C HELLO K WORLD".split(" ");

        ParseValidator(unicodeWords);

        return 1;

    }

    static long Validator_2(){

        morseSequence = "--.-------..";            
        String[] unicodeWords = "GOD GOOD MORNING G HELLO".split(" ");

        ParseValidator(unicodeWords);

        return 1;

    }

    static long Validator_3(){

        morseSequence = "......-...-..---.-----.-..-..-..";            
        String[] unicodeWords = "HELL HELLO OWORLD WORLD TEST".split(" ");

        ParseValidator(unicodeWords);

        return 2;

    }
        
    static long Validator_4(){

        List<String> fileContent = ReadFile("Draft.txt");

        morseSequence = fileContent.get(0);
        
        fileContent.remove(0);

        String[] unicodeWords = fileContent.toArray(new String[0]);

        ParseValidator(unicodeWords);

        System.out.println("Validator 4 Parsed");

        return 2971215073L;

    }

    static long Validator_5(){

        morseSequence = "-.-..---.-..---.-..--";            
        String[] unicodeWords = "CAT KIM TEXT TREM CEM".split(" ");

        ParseValidator(unicodeWords);

        return 125;

    }    

    static long Validator_6(){

        morseSequence = "..............................................";            
        String[] unicodeWords = "E I".split(" ");

        ParseValidator(unicodeWords);

        return 2971215073L;

    } 
    
    public static List<String> ReadFile(String fullPath) {
        
        List<String> lines = new ArrayList<String>();

        try {
            File myObj = new File(fullPath);

            Scanner myReader = new Scanner(myObj);

            while (myReader.hasNextLine()) lines.add(myReader.nextLine());
            
            myReader.close();
        
        }catch (FileNotFoundException e) {
            
            System.out.println("An error occurred.");
            e.printStackTrace();
        
        }

        return lines;
    }    
}

