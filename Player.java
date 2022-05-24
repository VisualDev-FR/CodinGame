import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

class Solution {

    static TreeMap<Character, String> traductions;
    static TreeMap<String, Sequence> encounteredSequences;
    static TreeMap<Integer, Node> nodes;

    static String morseInput;    
    static int dictionaryCount;
    //static int[][] adjacentMatrix;
    static Matrix adjacentMatrix;

    static long branchesCount = 0;

    static String[] morseWords;
    static int[] lengthTable;   
    static long totalCombinaisons;
    static boolean nullMatrix;
    static long nbIterations;
    static int matrixSize;    

    public static void main(String args[]) {

        Initialize();
        LocalSession(0);
        //OnlineSession();
    }

    static void CountCombinaisons(){

        Matrix matProd = adjacentMatrix;   

        totalCombinaisons = adjacentMatrix.Get(matrixSize-1, 0);

        nbIterations = 0;
        
        for(int i = 0; i < matrixSize;  i++){

            matProd.Print();

            matProd = ProdMat(i, adjacentMatrix, matProd);
            totalCombinaisons += matProd.Get(matrixSize - 1, 0);

            if(nullMatrix) return;
        }
    }

    static Matrix ProdMat(int N, Matrix matrix_1, Matrix matrix_2){

        Matrix matrix_3 = new Matrix();

        nullMatrix = true;

        for(int i = N; i < matrixSize; i++){

            for(int j = 0; j < i - N; j++){

                for(int k = N; k < i ; k++){

                    long increaseValue = matrix_1.Get(i, k) * matrix_2.Get(k, j);
                    matrix_3.Increase(i, j, increaseValue);

                    nullMatrix = increaseValue == 0L && nullMatrix;

                    nbIterations++;
                }                
            }
        }        

        return matrix_3;
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

        //adjacentMatrix = new int[morseInput.length()+1][morseInput.length()+1];

        for (int i = 0; i < dictionaryCount; i++) {

            new Sequence(in.next());          
        }
    }

    static void Initialize(){

        totalCombinaisons = 0;
        branchesCount = 0;

        adjacentMatrix = new Matrix();
        encounteredSequences = new TreeMap<String, Sequence>();
        nodes = new TreeMap<Integer, Node>();        
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
        public List<Integer> indexes; //TODO: essayer de le supprimer, normalement avec la dernière optimisation du compte d'occurences, on ne devrait plus en avoir besoin

        public String morseSequence;
        public int length;
        
        public Sequence(String word){

            morseSequence = TraduceToMorse(word);
            //asciiSequence = word; //TODO: Supprimer
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
    
                        Node beginNode = nodes.containsKey(begIndex) ? nodes.get(begIndex) : new Node(nodes.size(), begIndex);
                        Node endNode = nodes.containsKey(endIndex) ? nodes.get(endIndex) : new Node(nodes.size(), endIndex);
                        
                        beginNode.Connect(endNode);                    
                        branches.add(new int[]{begIndex, endIndex});
                        branchesCount++;
                    }

                    i = morseInput.indexOf(morseSequence, i) + 1;
                    begIndex = morseInput.indexOf(morseSequence, i);

                }

            }        


        }

        public void Duplicate(Sequence mSequence){

            for(int[] branch : mSequence.branches){

                branchesCount++;
                
                Node begNode = nodes.get(branch[0]);
                Node endNode = nodes.get(branch[1]);

                begNode.Connect(endNode);

            }
        }

        public void Print(){
            System.err.printf("%s : ", this.morseSequence);

            for(Integer index : this.indexes){
                System.err.print("" + index + " ");
            }
    
            System.err.print("\n");             
        }
    }

    public static class Node{

        public List<Node> nodesFrom;
        public List<Node> nodesTo;
        
        public int ID;
        public int index;

        public Node(int nodeID, int nodeIndex){

            ID = nodeID;
            index = nodeIndex;

            if(!nodes.containsKey(index)) nodes.put(index, this);
        }

        public void Connect(Node mNode){

            Node minNode = mNode.index < this.index ? mNode : this;
            Node maxNode = mNode.index < this.index ? this : mNode;

            //System.err.printf("Connect : node %s[index : %s] + node %s[index : %s] \n", minNode.ID, minNode.index, maxNode.ID, maxNode.index);

            //adjacentMatrix[maxNode.index][minNode.index]++;
            adjacentMatrix.Increase(maxNode.index, minNode.index, 1L);
        }
    }

    public static class Matrix{

        Map<String, Long> values;
        List<Integer> nonEmptyColumns;
        List<Integer> nonEmptyRows;

        public Matrix(){
            values = new HashMap<String, Long>();
            nonEmptyColumns = new ArrayList<Integer>();
            nonEmptyRows = new ArrayList<Integer>();
        }

        public void Reset(){
            nonEmptyColumns = new ArrayList<Integer>();
            nonEmptyRows = new ArrayList<Integer>();            
        }

        public void Increase(int m, int n, long value){
            
            values.put(m + ":" + n, values.getOrDefault(m + ":" + n, 0L) + value);
            
            if(!nonEmptyColumns.contains(m)) nonEmptyColumns.add(m);
            if(!nonEmptyRows.contains(n)) nonEmptyRows.add(n);
        }

        public long Get(int m, int n){
            return values.getOrDefault(m + ":" + n, 0L);
        }

        public void Print(){

            for(int i = 0; i < matrixSize; i++){

                String[] arrayTemp = new String[matrixSize];

                for(int j = 0; j < matrixSize; j++){
                    arrayTemp[j] = this.Get(i, j) > 0 ? ""+this.Get(i, j) : ".";
                }

                Debug(String.join(" ", arrayTemp));
            }

            Debug(" ");

        }

    }

    //Functions for switching local to online running

    static void OnlineSession(){

        ReadInputs();

        CountCombinaisons();

        System.out.println(totalCombinaisons);        
    }

    static void LocalSession(int number){

        Initialize();

        if(number == 0){

            System.out.print("Validator to run ?  ");

            number = new Scanner(System.in).nextInt();

            System.out.print("\n");

            if(number > 6 || number < 0) return;

            if(number == 0){

                for(int i = 1; i < 6; i++) RunLocal(i);

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

        long answer = GetValidator(Validator);

        matrixSize = morseInput.length() + 1;
                
        CountCombinaisons();

        System.out.println(" ");
        System.out.printf(" Validator %s : Answer = %s / Found = %s nbIterations = %s matrixSize = %s \n\n", Validator, answer, totalCombinaisons, nbIterations, matrixSize);
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

        //adjacentMatrix = new int[morseInput.length()+1][morseInput.length()+1];

        for (int i = 0; i < unicodeWords.length; i++) {            
            new Sequence(unicodeWords[i]);
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