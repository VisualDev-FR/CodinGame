import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

class Solution {

    static TreeMap<Character, String> traductions;
    static TreeMap<String, Sequence> sequences;

    static String morseInput;    
    static int dictionaryCount;

    static String[] morseWords;
    static int[] lengthTable;   

    public static void main(String args[]) {

        InitializeLetters();

        LocalSession(0);
        OnlineSession();

    }

    static int GetCominaisons(String morseSequence){

        //Debug("morseSequence : " + morseSequence);
        //Debug(" ");
        
        int totalCombinaisons = 0;

        for(Sequence sequence : sequences.values()){

            String word = sequence.morseSequence; 

            if(morseSequence.indexOf(word) > -1){
                
                if(morseSequence.length() - word.length() == 0){

                    totalCombinaisons++;
                
                }else{
                    String cuttedSequence = morseSequence.replaceFirst(word, "");
                    
                    //totalCombinaisons += GetCominaisons(cuttedSequence);
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

        morseInput = in.next();        
        dictionaryCount = in.nextInt();

        sequences = new TreeMap<String, Sequence>();

        for (int i = 0; i < dictionaryCount; i++) {

            Sequence mSequence = new Sequence(in.next());

            if (mSequence.occurences > 0){

                mSequence.Print();
                sequences.put(mSequence.asciiSequence, mSequence);
            }            
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
    
    //Class

    public static class Sequence{

        public List<Integer> indexes;
        public int occurences;
        public String asciiSequence;
        public String morseSequence;
        public int length;
        
        public Sequence(String word){

            asciiSequence = word;
            morseSequence = TraduceToMorse(asciiSequence);
            length = morseSequence.length();

            indexes = new ArrayList<Integer>();

            int lastIndex = morseInput.indexOf(morseSequence, 0);

            while (lastIndex != -1) {
                
                occurences++;
                indexes.add(lastIndex);
                lastIndex += morseSequence.length();
                lastIndex = morseInput.indexOf(morseSequence, lastIndex);

            }
        }

        public void Print(){
            System.err.printf("%s : occurences = %s, indexes : ", this.morseSequence, this.occurences);

            for(Integer index : this.indexes){
                System.err.print("" + index + " ");
            }
    
            System.err.print("\n");             
        }
    }

    public static class Combinaison{

        public List<Sequence> sequenceList;
        public boolean IsValid;
        public int length;
        public String strCombinaison;

        public Combinaison(){

            strCombinaison = "0".repeat(morseInput.length());

            sequenceList = new ArrayList<Sequence>();
            IsValid = true;
        }

        public boolean TryInsertAll(Sequence mSequence){

            String beforeInsert = strCombinaison;

            for(int index : mSequence.indexes){
                
                int begIndex = index;
                int endIndex = index + mSequence.length;

                String subSequence = strCombinaison.substring(begIndex, endIndex);
                StringBuffer strBufCombinaison = new StringBuffer(strCombinaison);

                if(subSequence == "0".repeat(subSequence.length())){
                    strCombinaison = strBufCombinaison.replace(begIndex, endIndex, mSequence.morseSequence).toString();
                }

            }
            return beforeInsert != strCombinaison ? true : false;
        }

    }

    //Functions for local tests

    static void OnlineSession(){

        ReadInputs();

        long totalCombinaisons = GetCominaisons(morseInput);

        System.out.println(totalCombinaisons);        
    }

    static void LocalSession(int number){

        if(number == 0){

            System.out.print("Validator to run ?  ");

            number = new Scanner(System.in).nextInt();

            System.out.print("\n");

            if(number > 6 || number < 0) return;

            if(number == 0){

                for(int i = 1; i < 6; i++){

                    long answer = GetValidator(i);
            
                    long totalCombinaisons = GetCominaisons(morseInput);
                    
                    System.out.println(" ");
                    System.out.printf(" Validator %s : Answer = %d / Found = %d \n\n", i, answer, totalCombinaisons);

                }

            }else{
                long answer = GetValidator(number);
                
                long totalCombinaisons = GetCominaisons(morseInput);
                System.out.println(" ");
                System.out.printf(" Validator %s : Answer = %d / Found = %d \n\n", number, answer, totalCombinaisons);            
            }        

            System.out.println(" ");
            LocalSession(0);
        
        }else{
            long answer = GetValidator(number);
                
            long totalCombinaisons = GetCominaisons(morseInput);
            
            System.out.println(" ");
            System.out.printf(" Validator %s : Answer = %d / Found = %d \n\n", number, answer, totalCombinaisons);
        }
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

        sequences = new TreeMap<String, Sequence>();

        for (int i = 0; i < unicodeWords.length; i++) {
            
            Sequence mSequence = new Sequence(unicodeWords[i]);

            if (mSequence.occurences > 0){

                mSequence.Print();
                sequences.put(mSequence.asciiSequence, mSequence);
            }
        }
    }

    static long Validator_1(){

        morseInput = "-.-";            
        String[] unicodeWords = "A B C HELLO K WORLD".split(" ");

        ParseValidator(unicodeWords);

        return 1;

    }

    static long Validator_2(){

        morseInput = "--.-------..";            
        String[] unicodeWords = "GOD GOOD MORNING G HELLO".split(" ");

        ParseValidator(unicodeWords);

        return 1;

    }

    static long Validator_3(){

        morseInput = "......-...-..---.-----.-..-..-..";            
        String[] unicodeWords = "HELL HELLO OWORLD WORLD TEST".split(" ");

        ParseValidator(unicodeWords);

        return 2;

    }
        
    static long Validator_4(){

        List<String> fileContent = ReadFile("Draft.txt");

        morseInput = fileContent.get(0);
        
        fileContent.remove(0);

        String[] unicodeWords = fileContent.toArray(new String[0]);

        ParseValidator(unicodeWords);

        return 2971215073L;

    }

    static long Validator_5(){

        morseInput = "-.-..---.-..---.-..--";            
        String[] unicodeWords = "CAT KIM TEXT TREM CEM".split(" ");

        ParseValidator(unicodeWords);

        return 125;

    }    

    static long Validator_6(){

        morseInput = "..............................................";            
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

