import java.util.*;
import java.io.*;

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
    static int kMax;

    public static void main(String args[]) {

        Initialize();
        LocalSession(0);
        //OnlineSession();        
    }

    static void CountCombinaisons(){

        nbIterations = 0;

        Debug("intersections 1 : " + adjacentMatrix.intersections.size());

        adjacentMatrix = SquareMat(adjacentMatrix);

        //adjacentMatrix.Print();

        Debug("intersections 2 : " + adjacentMatrix.intersections.size());

/*         Matrix matProd = adjacentMatrix;
        
        try {
            matProd.PrintTxtFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        totalCombinaisons = adjacentMatrix.Get(matrixSize-1, 0);

        nbIterations = 0;
        
        for(int i = 0; i < matrixSize;  i++){

            //matProd.Print();

            matProd = ProdMat(i, adjacentMatrix, matProd);
            totalCombinaisons += matProd.Get(matrixSize - 1, 0);

            if(nullMatrix) return;
        } */
    }

    static Matrix ProdMat(Matrix matrixA, Matrix matrixB){

        Matrix matProd = new Matrix();

        List<Intersection> intersections = matrixA.HasIntersections(matrixB);

        return null;
    }

    static Matrix SquareMat(Matrix matrix){

        Matrix sqrMatrix = new Matrix();

        for(Intersection intersection : matrix.intersections.values()){

            nbIterations++;

            long increaseValue = matrix.Get(intersection.parentRow1, intersection.parentColumn1) * matrix.Get(intersection.parentRow2, intersection.parentColumn2);    

            sqrMatrix.Increase(intersection.row, intersection.column, increaseValue);

        }

        return sqrMatrix;
    }

 /*     static Matrix ProdMat(int N, Matrix matrix_1, Matrix matrix_2){

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

                kMax = Math.max(i-N, kMax);
                
                System.err.printf("%s %s %s\n", i, j, i-N);

            }
        }        

        return matrix_3;
    } */  

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

            adjacentMatrix.Increase(maxNode.index, minNode.index, 1L);
        }
    }

    public static class Matrix{

        Map<String, Long> values;
        Map<Integer, List<Integer>> columns;
        Map<Integer, List<Integer>> rows;
        Map<String, Intersection> intersections;

        public Matrix(){

            values = new HashMap<String, Long>();
            columns = new HashMap<Integer, List<Integer>>();
            rows = new HashMap<Integer, List<Integer>>();
            intersections = new HashMap<String, Intersection>();
        }

        public void Increase(int row, int column, long value){
            
            values.put(row + ":" + column, values.getOrDefault(row + ":" + column, 0L) + value);

            SearchIntersections(row, column, this);

            columns.putIfAbsent(column, new ArrayList<Integer>());
            rows.putIfAbsent(row, new ArrayList<Integer>());

            columns.get(column).add(row);
            rows.get(row).add(column);

        }

        private void SearchIntersections(int row, int column, Matrix matrix){

            if(matrix.rows.containsKey(column)){
                
                for(int mColumn : matrix.rows.get(column)){

                    String interKey = ""+row+":"+column+":"+column+":"+mColumn;

                    if(!intersections.containsKey(interKey)){

                        Intersection intersection = new Intersection(row, column, column, mColumn);

                        intersections.put(interKey, intersection);
                    }
                }
            }

            if(matrix.columns.containsKey(row)){

                for(int mRow : matrix.columns.get(row)){

                    String interKey = ""+mRow+":"+row+":"+row+":"+column;
                    
                    if(!intersections.containsKey(interKey)){

                        Intersection intersection = new Intersection(mRow, row, row, column);
                        intersections.put(interKey, intersection);
                    }
                }
            }
        }

        public long Get(int row, int column){
            return values.getOrDefault(row + ":" + column, 0L);
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

        public void PrintTxtFile() throws FileNotFoundException, UnsupportedEncodingException{

            PrintWriter writer = new PrintWriter("Matrix.txt", "UTF-8");
            
            //writer.println("The first line");
            //writer.println("The second line");
            
            for(int i = 0; i < matrixSize; i++){

                String[] arrayTemp = new String[matrixSize];

                for(int j = 0; j < matrixSize; j++){
                    arrayTemp[j] = this.Get(i, j) > 0 ? ""+this.Get(i, j) : " ";
                }

                writer.println(String.join(";", arrayTemp));
            }            
            
            writer.close();

        }
    }

    public static class Intersection{
        
        public int row;
        public int column;

        public int parentRow1;
        public int parentColumn1;

        public int parentRow2;
        public int parentColumn2;

        public Intersection(int m1, int n1, int m2, int n2){

            parentRow1 = m1;
            parentColumn1 = n1;

            parentRow2 = m2;
            parentColumn2 = n2;

            row = m1;
            column = n2;

            //this.Print();
        }

        public void Print(){
            System.err.printf("%s %s : parent_1 = %s %s parent_2 = %s %s\n", row, column, parentRow1, parentColumn1, parentRow2, parentColumn2);
        }
    }

    //Functions for switching local to online running

    static void OnlineSession(){

        ReadInputs();

        CountCombinaisons();

        System.out.println(totalCombinaisons);        
    }

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

        matrixSize = morseInput.length() + 1;
                
        CountCombinaisons();

        System.out.println(" ");
        System.out.printf(" Validator %s : Answer = %s / Found = %s nbIterations = %s matrixSize = %s intersections : %s \n\n", Validator, answer, totalCombinaisons, nbIterations, matrixSize, adjacentMatrix.intersections.size());
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