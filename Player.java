import java.util.*;
import java.io.*;

class Solution {

    static TreeMap<Character, String> traductions;

    static String morseInput;    
    static int dictionaryCount;

    static String[] morseWords; 
    static long totalCombinaisons;
    static long nbIterations;
    static String[] unicodeWords;


    public static void main(String args[]) {

        LocalSession(0);
        //OnlineSession();        
    }

    static void CountCombinaisons(){

        Initialize();

        List<String> morseSequences = new ArrayList<String>();

        for(String word : unicodeWords){            
            
            String traduction = TraduceToMorse(word);

            if(!morseSequences.contains(word)){
                morseSequences.add(traduction);
            }
            
        }

        long[] countTable = new long[morseInput.length()+1];
        countTable[morseInput.length()] = 1;

        morseWords = morseSequences.toArray(new String[0]);

        for(int i = morseInput.length()-1; i >= 0; i--){

            long acc = 0;
            
            for(int j = 0; j < morseWords.length ; j++){

                nbIterations++;

                if(morseInput.startsWith(morseWords[j], i)){

                    acc += countTable[i + morseWords[j].length()];
                }
            }
            countTable[i] = acc;
            System.err.printf("countTable[%s] = %s\n", i, acc);
        }

        totalCombinaisons = countTable[0];

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

        totalCombinaisons = 0;    
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

    //Online Sessions

    static void ReadInputs(){

        Scanner in = new Scanner(System.in);

        morseInput = in.next();        
        dictionaryCount = in.nextInt();

        unicodeWords = new String[dictionaryCount];

        for (int i = 0; i < dictionaryCount; i++) {
            unicodeWords[i] = (in.next());          
        }
    }

    static void OnlineSession(){

        ReadInputs();

        CountCombinaisons();

        System.out.println(totalCombinaisons);        
    }

    //Local Sessions

    static void LocalSession(int number){

        if(number == 0){

            System.out.print("Validator to run ?  ");

            number = new Scanner(System.in).nextInt();

            System.out.print("\n");

            if(number > 6 || number < 0) return;

            if(number == 0){

                for(int i = 1; i < 7; i++) RunLocal(i);

            }else{

                RunLocal(number);
            }        

            System.out.println(" ");
            LocalSession(0);
        
        }else{
            RunLocal(number);
        }
    }

    static void RunLocal(int Validator){

        Initialize();

        long answer = GetValidator(Validator);

        CountCombinaisons();

        System.out.println(" ");
        System.out.printf(" Validator %s : Answer = %s / Found = %s nbIterations = %s\n\n", Validator, answer, totalCombinaisons, nbIterations);
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

    static long Validator_1(){

        morseInput = "-.-";            
        unicodeWords = "A B C HELLO K WORLD".split(" ");

        return 1;
    }

    static long Validator_2(){

        morseInput = "--.-------..";            
        unicodeWords = "GOD GOOD MORNING G HELLO".split(" ");

        return 1;
    }

    static long Validator_3(){

        morseInput = "......-...-..---.-----.-..-..-..";            
        unicodeWords = "HELL HELLO OWORLD WORLD TEST".split(" ");

        return 2;
    }
        
    static long Validator_4(){

        List<String> fileContent = ReadFile("Draft.txt");

        morseInput = fileContent.get(0);
        
        fileContent.remove(0);

        unicodeWords = fileContent.toArray(new String[0]);

        return 57330892800L; //57 330 892 800
    }

    static long Validator_5(){

        morseInput = "-.-..---.-..---.-..--";            
        unicodeWords = "CAT KIM TEXT TREM CEM".split(" ");

        return 125;
    }    

    static long Validator_6(){

        morseInput = "..............................................";            
        unicodeWords = "E I".split(" ");

        return 2971215073L; //2 971 215 073
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