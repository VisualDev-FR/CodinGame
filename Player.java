import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

class Solution {

    static TreeMap<Character, String> traductions;
    static TreeMap<String, Sequence> sequences;
    static TreeMap<String, Sequence> encounteredSequences;

    static TreeMap<Integer, List<Integer>> beginMap;
    static TreeMap<Integer, List<Integer>> endMap;

    static String morseInput;    
    static int dictionaryCount;

    static long totalKeys = 0;

    static String[] morseWords;
    static int[] lengthTable;   
    static long totalCombinaisons;

    public static void main(String args[]) {

        InitializeLetters();

        LocalSession(0);
        //OnlineSession();

    }

    static void ExploreIndexMap(int begIndex){

        if(begIndex == 0) {
            Debug("Total Keys = " + totalKeys);
            Debug("Total morse = " + encounteredSequences.size());
        }

        if(!beginMap.containsKey(begIndex)) return;

        for(int endIndex : beginMap.get(begIndex)){

            if(endIndex == morseInput.length()){
            
                totalCombinaisons++;
            
            }else if(beginMap.containsKey(endIndex)){

                ExploreIndexMap(endIndex);
            }
        }        
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

        totalCombinaisons = 0;

        beginMap = new TreeMap<Integer, List<Integer>>();
        sequences = new TreeMap<String, Sequence>();

        for (int i = 0; i < dictionaryCount; i++) {

            Sequence mSequence = new Sequence(in.next());

            if (mSequence.occurences > 0){

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

        public List<Integer> begIndexes;
        public List<Integer> endIndexes;

        public int occurences;
        public String asciiSequence;
        public String morseSequence;
        public int length;
        
        public Sequence(String word){

            morseSequence = TraduceToMorse(word);

            if(encounteredSequences.containsKey(morseSequence)){

                this.Set(encounteredSequences.get(morseSequence));

                for(int begIndex : this.begIndexes){
                    int endIndex = begIndex + this.length;
                    AddIndexes(begIndex, endIndex);
                }

            }else if(morseInput.indexOf(morseSequence, 0) >= 0){

                asciiSequence = word;

                length = morseSequence.length();

                begIndexes = new ArrayList<Integer>();
                endIndexes = new ArrayList<Integer>();

                encounteredSequences.put(morseSequence, this);

                for(int i = 0; i < morseInput.length();  i++){

                    int begIndex = morseInput.indexOf(morseSequence, i);
                    int endIndex = begIndex + length;
    
                    if(begIndex > -1 && !begIndexes.contains(begIndex)){
                        occurences++;

                        begIndexes.add(begIndex);
                        endIndexes.add(endIndex);

                        AddIndexes(begIndex, endIndex);                        

                    }  
                }
            }

            //System.out.printf("%s : %s\n", occurences, morseSequence);
            totalKeys += occurences;
        }

        public void AddIndexes(int begIndex, int endIndex){
            
            if(!beginMap.containsKey(begIndex)) beginMap.put(begIndex, new ArrayList<Integer>());
            if(!endMap.containsKey(endIndex)) endMap.put(endIndex, new ArrayList<Integer>());
            
            endMap.get(endIndex).add(begIndex);
            beginMap.get(begIndex).add(endIndex);            
        }

        public void Set(Sequence mSequence){

            begIndexes = mSequence.begIndexes;
            endIndexes = mSequence.endIndexes;
    
            occurences = mSequence.occurences;
            asciiSequence = mSequence.asciiSequence;
            length = mSequence.length;            
        }

        public void Print(){
            System.err.printf("%s : occurences = %s, indexes : ", this.morseSequence, this.occurences);

            for(Integer index : this.begIndexes){
                System.err.print("" + index + " ");
            }
    
            System.err.print("\n");             
        }
    }

    //Functions for switching local to online running

    static void OnlineSession(){

        ReadInputs();

        ExploreIndexMap(0);

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
            
                    ExploreIndexMap(0);
                    
                    System.out.println(" ");
                    System.out.printf(" Validator %s : Answer = %s / Found = %s \n\n", number, answer, totalCombinaisons);

                }

            }else{

                long answer = GetValidator(number);
                
                ExploreIndexMap(0);

                System.out.println(" ");
                System.out.printf(" Validator %s : Answer = %s / Found = %s \n\n", number, answer, totalCombinaisons);           
            }        

            System.out.println(" ");
            LocalSession(0);
        
        }else{
            long answer = GetValidator(number);
                
            ExploreIndexMap(0);

            System.out.println(" ");
            System.out.printf(" Validator %s : Answer = %s / Found = %s \n\n", number, answer, totalCombinaisons);
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

        totalCombinaisons = 0;
        totalKeys = 0;

        encounteredSequences = new TreeMap<String, Sequence>();
        beginMap = new TreeMap<Integer, List<Integer>>();
        endMap = new TreeMap<Integer, List<Integer>>();
        sequences = new TreeMap<String, Sequence>();

        for (int i = 0; i < unicodeWords.length; i++) {
            
            Sequence mSequence = new Sequence(unicodeWords[i]);

            if (mSequence.occurences > 0){

                //mSequence.Print();
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

        return 57330892800L; //57 330 892 800
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

