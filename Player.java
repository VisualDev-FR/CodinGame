import java.util.*;

class Solution {

    static TreeMap<Character, String> traductions;

    static String morseSequence;    
    static int dictionaryCount;

    static String[] morseWords;
    static int[] lengthTable;    

    public static void main(String args[]) {

        InitializeLetters();
        ReadInputs();

        int totalCombinaisons = GetCominaisons(morseSequence);

        System.out.println(totalCombinaisons);
    }

    static int GetCominaisons(String morseSequence){
        
        int totalCombinaisons = 0;

        for(String word : morseWords){

            if(morseSequence.indexOf(word)>-1){
                
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
}

