import java.util.*;

class Solution {

    static TreeMap<Character, String> traductions;

    public static void main(String args[]) {
        
        Initialize();

        Scanner in = new Scanner(System.in);

        String morseInput = in.next();        
        int dictionaryCount = in.nextInt();

        List<String> morseSequences = new ArrayList<String>();
        long[] countTable = new long[morseInput.length()+1];
        
        countTable[morseInput.length()] = 1;        

        for (int i = 0; i < dictionaryCount; i++) {

            String traduction = TraduceToMorse(in.next());

            if(morseInput.indexOf(traduction) > -1){
                morseSequences.add(traduction);
            }                      
        }

        for(int i = morseInput.length()-1; i >= 0; i--){

            long count = 0;
            
            for(String sequence : morseSequences){

                if(morseInput.startsWith(sequence, i)){

                    count += countTable[i + sequence.length()];
                }
            }
            
            countTable[i] = count;
        }

        System.out.println(countTable[0]);        
    }

    static String TraduceToMorse(String word){
        
        String finalString = "";

        for(int i = 0; i < word.length(); i++){
            
            char currentLetter = word.charAt(i);

            finalString += traductions.get(currentLetter); 
        }

        return finalString;
    }

    static void Initialize(){
 
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
}