import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

class Solution {

    static TreeMap<Character, String> traductions;
    static TreeMap<String, Sequence> sequences;
    static TreeMap<String, Sequence> encounteredSequences;
    static TreeMap<Integer, Node> nodes;

    static String morseInput;    
    static int dictionaryCount;

    static long branchesCount = 0;

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
            System.err.printf("nbNodes : %s\n", nodes.size());
            System.err.printf("Branches : %s\n", branchesCount);
        }

        totalCombinaisons = 1;
        
        for(Node node : nodes.values()){

            System.err.printf("Node %s : connection : %s isValid : %s isAlone : %s\n", node.ID, node.connections, node.IsValid(), (!node.begConnected && !node.endConnected));

            if(node.connections >0 && node.IsValid()){
                totalCombinaisons *= node.connections;                
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
        branchesCount = 0;

        encounteredSequences = new TreeMap<String, Sequence>();
        sequences = new TreeMap<String, Sequence>();
        nodes = new TreeMap<Integer, Node>();

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

        public List<int[]> branches;
        public List<Integer> indexes;

        public int occurences;
        public String asciiSequence;
        public String morseSequence;
        public int length;
        
        public Sequence(String word){

            morseSequence = TraduceToMorse(word);
            asciiSequence = word;
            length = morseSequence.length();

            if(encounteredSequences.containsKey(morseSequence)){

                branches = new ArrayList<int[]>();
                indexes = new ArrayList<Integer>();

                this.Duplicate(encounteredSequences.get(morseSequence));

            }else{

                encounteredSequences.put(morseSequence, this);

                branches = new ArrayList<int[]>();
                indexes = new ArrayList<Integer>();
                
                int i = 0;
                int begIndex = morseInput.indexOf(morseSequence, i);

                while(begIndex > -1){

                    if(begIndex > -1 && !indexes.contains(begIndex)){

                        indexes.add(begIndex);
    
                        int endIndex = begIndex + length;
    
                        Node beginNode = nodes.containsKey(begIndex) ? nodes.get(begIndex) : new Node(begIndex);
                        Node endNode = nodes.containsKey(endIndex) ? nodes.get(endIndex) : new Node(endIndex);
                        
                        beginNode.Connect(endNode);                    
                        branches.add(new int[]{begIndex, endIndex});
                        branchesCount++;
                    }

                    i++;
                    begIndex = morseInput.indexOf(morseSequence, i);

                }

            }        


        }

        public void Print(){
            System.err.printf("%s : ", this.morseSequence);

            for(Integer index : this.indexes){
                System.err.print("" + index + " ");
            }
    
            System.err.print("\n");             
        }          

        public void Duplicate(Sequence mSequence){

            for(int[] branch : mSequence.branches){

                branchesCount++;
                
                Node begNode = nodes.get(branch[0]);
                Node endNode = nodes.get(branch[1]);

                begNode.Connect(endNode);

            }
        }
    }

    public static class Node{

        public boolean begConnected;
        public boolean endConnected;
        
        public List<Node> nodesFrom;
        public List<Node> nodesTo;
        
        public int ID;
        public int connections;

        public Node(int nodeID){

            ID = nodeID;

            nodesFrom = new ArrayList<Node>();
            nodesTo = new ArrayList<Node>();

            begConnected = nodeID == 0;
            endConnected = nodeID == morseInput.length();

            if(!nodes.containsKey(ID)) nodes.put(ID, this);
        }

        public void Connect(Node mNode){

            Node minNode = mNode.ID < this.ID ? mNode : this;
            Node maxNode = mNode.ID < this.ID ? this : mNode;

            minNode.connections++;

            maxNode.nodesFrom.add(minNode);
            minNode.nodesTo.add(maxNode);

            maxNode.begConnected = maxNode.begConnected || minNode.begConnected;
            maxNode.endConnected = maxNode.endConnected || minNode.endConnected;
            
            minNode.begConnected = minNode.begConnected || maxNode.begConnected;
            minNode.endConnected = minNode.endConnected || maxNode.endConnected;

            if(minNode.endConnected) minNode.nodesFrom.forEach(node -> node.endConnected  = true);
            if(maxNode.begConnected) maxNode.nodesTo.forEach(node -> node.begConnected = true);
        }

        public boolean IsValid(){
            return begConnected && endConnected;
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
        branchesCount = 0;

        encounteredSequences = new TreeMap<String, Sequence>();
        sequences = new TreeMap<String, Sequence>();
        nodes = new TreeMap<Integer, Node>();

        //Debug(morseInput);

        for (int i = 0; i < unicodeWords.length; i++) {
            
            Sequence mSequence = new Sequence(unicodeWords[i]);

            if (mSequence.indexes.size() > 0){ 
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

