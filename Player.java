import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.text.FieldPosition;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.StreamTokenizer;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;


class Solution {

    static TreeMap<Character, String> traductions;
    static TreeMap<String, Sequence> encounteredSequences;
    static TreeMap<Integer, Node> nodes;

    static String morseInput;    
    static int dictionaryCount;
    static double[][] adjacentMatrix;
    static List<Matrix> matrixes;

    static long branchesCount = 0;

    static String[] morseWords;
    static int[] lengthTable;   
    static long totalCombinaisons;
    static boolean nullMatrix;

    public static void main(String args[]) {

        int validator = 4;

        Initialize();
        LocalSession(validator);
        //OnlineSession();

    }

    static void CountCombinaisons(){

        int nodesCount = adjacentMatrix.length;
        
        int pow = 2;

        adjacentMatrix = MatPow(new Matrix(adjacentMatrix), pow).A;

        for(int i = 0; i < adjacentMatrix.length;  i++){

            Debug(Arrays.toString(adjacentMatrix[i]));

        }Debug(" ");
                

        totalCombinaisons = (long) adjacentMatrix[nodesCount-1][0];

/*         double[][] matProd = adjacentMatrix;
        
        for(int i = 0; i < nodesCount-1;  i++){

            matProd = ProdMat(nodesCount, adjacentMatrix, matProd);

            if(nullMatrix) return;

            totalCombinaisons += matProd[nodesCount-1][0];
        }  */
    }

    static Matrix MatPow(Matrix mat, int n){

        //if(n < 0){

            //return mat.inverse();

        /* }else */ 
        
        if(n == 0){

            return mat.identity(mat.m, mat.n);

        }else if(n % 2 == 0){ //n is even

            return MatPow(mat.times(mat), n / 2);

        }else{
            
            return mat.times(MatPow(mat.times(mat), (n - 1) / 2));

        }
    }

    static double[][] ProdMat(int N ,double[][] matrix_1, double[][] matrix_2){

        double[][] matrix_3 = new double[N][N];

        nullMatrix = true;

        for(int i = 0; i < N; i++){

            for(int j = 0; j < i; j++){
                
                for(int k = 0; k < N; k++){
                    matrix_3[i][j] += matrix_1[i][k] * matrix_2[k][j];
                    nullMatrix = matrix_3[i][j] == 0 && nullMatrix;
                }
            }

            //Debug(Arrays.toString(matrix_3[i]));
        }        
        //Debug(" ");
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

        adjacentMatrix = new double[morseInput.length()+1][morseInput.length()+1];

        for (int i = 0; i < dictionaryCount; i++) {

            new Sequence(in.next());          
        }
    }

    static void Initialize(){

        totalCombinaisons = 0;
        branchesCount = 0;

        encounteredSequences = new TreeMap<String, Sequence>();
        //sequences = new TreeMap<String, Sequence>();
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

        //public int occurences;
        //public String asciiSequence;
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

            System.err.printf("Connect : node %s[index : %s] + node %s[index : %s] \n", minNode.ID, minNode.index, maxNode.ID, maxNode.index);

            adjacentMatrix[maxNode.index][minNode.index]++;
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
                
        CountCombinaisons();

        System.out.println(" ");
        System.out.printf(" Validator %s : Answer = %s / Found = %s \n\n", Validator, answer, totalCombinaisons);
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

        adjacentMatrix = new double[morseInput.length()+1][morseInput.length()+1];

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

    static List<String> ReadFile(String fullPath) {
        
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

    //Jama Package

    public static class Matrix{
        
        private int m, n;
        private double[][] A;


        public Matrix minus (Matrix B) {
            checkMatrixDimensions(B);
            Matrix X = new Matrix(m,n);
            double[][] C = X.getArray();
            for (int i = 0; i < m; i++) {
               for (int j = 0; j < n; j++) {
                  C[i][j] = A[i][j] - B.A[i][j];
               }
            }
            return X;
        }

        private void checkMatrixDimensions (Matrix B) {
            if (B.m != m || B.n != n) {
               throw new IllegalArgumentException("Matrix dimensions must agree.");
            }
        }      

        public int getRowDimension () {
            return m;
        }

        public int getColumnDimension () {
            return n;
        }    

        public double[][] getArrayCopy () {
            double[][] C = new double[m][n];
            for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j];
            }
            }
            return C;
        }    

        public Matrix (int m, int n){
            this.m = m;
            this.n = n;
            A = new double[m][n];
        }

        public Matrix (int m, int n, double s) {
            this.m = m;
            this.n = n;
            A = new double[m][n];
            for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s;
                }
            }
        }

        public Matrix (double[][] A) {
            m = A.length;
            n = A[0].length;
            for (int i = 0; i < m; i++) {
                if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
            }
            this.A = A;
        }

        public Matrix (double[][] A, int m, int n) {
            this.A = A;
            this.m = m;
            this.n = n;
        }

        public double[][] getArray(){
            return A;
        }  

        public Matrix solve (Matrix B) {
            return (m == n ? (new LUDecomposition(this)).solve(B) :
            (new QRDecomposition(this)).solve(B));
        }

        public Matrix identity (int m, int n) {
            Matrix A = new Matrix(m,n);
            double[][] X = A.getArray();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    X[i][j] = (i == j ? 1.0 : 0.0);
                }
            }
            return A;
        }

        public Matrix inverse () {
            return solve(identity(m,m));
        }

        public Matrix getMatrix (int[] r, int[] c) {
            
            Matrix X = new Matrix(r.length,c.length);
            double[][] B = X.getArray();
            
            try {
                for (int i = 0; i < r.length; i++) {
                    for (int j = 0; j < c.length; j++) {
                        B[i][j] = A[r[i]][c[j]];
                    }
                }
            }catch(ArrayIndexOutOfBoundsException e) {
                throw new ArrayIndexOutOfBoundsException("Submatrix indices");
            }
            return X;
        }

        public Matrix getMatrix (int i0, int i1, int j0, int j1) {
            Matrix X = new Matrix(i1-i0+1,j1-j0+1);
            double[][] B = X.getArray();
            try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    B[i-i0][j-j0] = A[i][j];
                }
            }
            } catch(ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
            }
            return X;
        }     

        public Matrix getMatrix(int i0, int i1, int[] c){
            
            Matrix X = new Matrix(i1-i0+1,c.length);
            double[][] B = X.getArray();
            try {
                for (int i = i0; i <= i1; i++) {
                    for (int j = 0; j < c.length; j++) {
                        B[i-i0][j] = A[i][c[j]];
                    }
                }
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new ArrayIndexOutOfBoundsException("Submatrix indices");
            }
            return X;
        }

        public Matrix getMatrix (int[] r, int j0, int j1) {
            
            Matrix X = new Matrix(r.length,j1-j0+1);
            double[][] B = X.getArray();
            try {
                for (int i = 0; i < r.length; i++) {
                    for (int j = j0; j <= j1; j++) {
                        B[i][j-j0] = A[r[i]][j];
                    }
                }
            } catch(ArrayIndexOutOfBoundsException e) {
                throw new ArrayIndexOutOfBoundsException("Submatrix indices");
            }
            return X;
        }
        
        public Matrix times (Matrix B) {
            
            Matrix matrix_3 = new Matrix(m,B.n);

            for(int i = 0; i < m; i++){
    
                for(int j = 0; j < i; j++){
                    
                    for(int k = 0; k < m; k++){
                        matrix_3.A[i][j] += A[i][k] * B.A[k][j];
                    }

                    Debug(""+i + " " + j);
                } 
                
                
            }        

            return matrix_3;

/*             if (B.m != n) {
               throw new IllegalArgumentException("Matrix inner dimensions must agree.");
            }
            
            Matrix X = new Matrix(m,B.n);
            
            double[][] C = X.getArray();
            
            double[] Bcolj = new double[n];
            
            for (int j = 0; j < B.n; j++) {
               
                for (int k = 0; k < n; k++) {
                  Bcolj[k] = B.A[k][j];
               }
               
               for (int i = 0; i < j; i++) {
                  double[] Arowi = A[i];
                  double s = 0;
                  for (int k = 0; k < n; k++) {
                     s += Arowi[k]*Bcolj[k];
                  }
                  C[i][j] = s;
               }
            }

            return X; */
         }

    }

    public static class LUDecomposition implements java.io.Serializable {

        private int m, n, pivsign; 

        private int[] piv;
        private double[][] LU;

        public LUDecomposition (Matrix A) {

            LU = A.getArrayCopy();
            m = A.getRowDimension();
            n = A.getColumnDimension();
            piv = new int[m];
            for (int i = 0; i < m; i++) {
                piv[i] = i;
            }
            pivsign = 1;
            double[] LUrowi;
            double[] LUcolj = new double[m];

            for (int j = 0; j < n; j++) {

                for (int i = 0; i < m; i++) {
                LUcolj[i] = LU[i][j];
                }

                for (int i = 0; i < m; i++) {
                LUrowi = LU[i];

                int kmax = Math.min(i,j);
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += LUrowi[k]*LUcolj[k];
                }
    
                LUrowi[j] = LUcolj[i] -= s;
                }

                int p = j;
                for (int i = j+1; i < m; i++) {
                if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                        p = i;
                    }
                }
                if (p != j) {
                    for (int k = 0; k < n; k++) {
                            double t = LU[p][k]; LU[p][k] = LU[j][k]; LU[j][k] = t;
                        }
                        int k = piv[p]; piv[p] = piv[j]; piv[j] = k;
                        pivsign = -pivsign;
                }

                if (j < m & LU[j][j] != 0.0) {
                    for (int i = j+1; i < m; i++) {
                        LU[i][j] /= LU[j][j];
                    }
                }
            }
        }

        public boolean isNonsingular () {
            for (int j = 0; j < n; j++) {
                if (LU[j][j] == 0)
                return false;
            }
            return true;
        }

        public Matrix getL(){
            
            Matrix X = new Matrix(m,n);
            
            double[][] L = X.getArray();
            
            for(int i = 0; i < m; i++){                
                for(int j = 0; j < n; j++){
                    if(i > j){
                        L[i][j] = LU[i][j];
                    } else if (i == j) {
                        L[i][j] = 1.0;
                    } else {
                        L[i][j] = 0.0;
                    }
                }
            }

            return X;
        }

        public Matrix getU(){
            Matrix X = new Matrix(n,n);
            double[][] U = X.getArray();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                if (i <= j) {
                    U[i][j] = LU[i][j];
                } else {
                    U[i][j] = 0.0;
                }
                }
            }
            return X;
        }

        public int[] getPivot () {
            int[] p = new int[m];
            for (int i = 0; i < m; i++) {
                p[i] = piv[i];
            }
            return p;
        }

        public double[] getDoublePivot () {
            double[] vals = new double[m];
            for (int i = 0; i < m; i++) {
                vals[i] = (double) piv[i];
            }
            return vals;
        }

        public double det () {
            if (m != n) {
                throw new IllegalArgumentException("Matrix must be square.");
            }
            double d = (double) pivsign;
            for (int j = 0; j < n; j++) {
                d *= LU[j][j];
            }
            return d;
        }

        public Matrix solve(Matrix B){
            
            if (B.getRowDimension() != m) {
                throw new IllegalArgumentException("Matrix row dimensions must agree.");
            }
            if (!this.isNonsingular()) {
                throw new RuntimeException("Matrix is singular.");
            }

            int nx = B.getColumnDimension();
            Matrix Xmat = B.getMatrix(piv,0,nx-1);
            double[][] X = Xmat.getArray();

            for (int k = 0; k < n; k++) {
                for (int i = k+1; i < n; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j]*LU[i][k];
                }
                }
            }

            for (int k = n-1; k >= 0; k--) {
                for (int j = 0; j < nx; j++) {
                    X[k][j] /= LU[k][k];
                }

                for (int i = 0; i < k; i++){
                    for (int j = 0; j < nx; j++) {
                        X[i][j] -= X[k][j]*LU[i][k];
                    }
                }
            }
            return Xmat;
        }

        private static final long serialVersionUID = 1;
    }

    public static class QRDecomposition implements java.io.Serializable {

        private double[][] QR;
        private int m, n;
        private double[] Rdiag;
        private static final long serialVersionUID = 1;

        public QRDecomposition (Matrix A){
            
            QR = A.getArrayCopy();
            m = A.getRowDimension();
            n = A.getColumnDimension();
            Rdiag = new double[n];

            for (int k = 0; k < n; k++) {

                double nrm = 0;
                for (int i = k; i < m; i++) {
                    nrm = Maths.hypot(nrm,QR[i][k]);
                }
        
                if (nrm != 0.0){

                    if (QR[k][k] < 0) {
                        nrm = -nrm;
                    }

                    for (int i = k; i < m; i++) {
                        QR[i][k] /= nrm;
                    }

                    QR[k][k] += 1.0;

                    for (int j = k+1; j < n; j++) {
                        double s = 0.0; 
                        for (int i = k; i < m; i++) {
                            s += QR[i][k]*QR[i][j];
                        }
                        s = -s/QR[k][k];
                        for (int i = k; i < m; i++) {
                            QR[i][j] += s*QR[i][k];
                        }
                    }
                }                
                
                Rdiag[k] = -nrm;
            }
        }

        public boolean isFullRank(){
            for (int j = 0; j < n; j++) {
                if (Rdiag[j] == 0) return false;
            }
            return true;
        }

        public Matrix getH () {
            Matrix X = new Matrix(m,n);
            double[][] H = X.getArray();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (i >= j) {
                    H[i][j] = QR[i][j];
                    } else {
                    H[i][j] = 0.0;
                    }
                }
            }
            return X;
        }

        public Matrix getR () {
            Matrix X = new Matrix(n,n);
            double[][] R = X.getArray();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i < j) {
                    R[i][j] = QR[i][j];
                    } else if (i == j) {
                    R[i][j] = Rdiag[i];
                    } else {
                    R[i][j] = 0.0;
                    }
                }
            }
            return X;
        }

        public Matrix getQ () {
            Matrix X = new Matrix(m,n);
            double[][] Q = X.getArray();
            for (int k = n-1; k >= 0; k--) {
                for (int i = 0; i < m; i++) {
                    Q[i][k] = 0.0;
                }
                Q[k][k] = 1.0;
                for (int j = k; j < n; j++) {
                    if (QR[k][k] != 0) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR[i][k]*Q[i][j];
                    }
                    s = -s/QR[k][k];
                    for (int i = k; i < m; i++) {
                        Q[i][j] += s*QR[i][k];
                    }
                    }
                }
            }
            return X;
        }

        public Matrix solve (Matrix B) {
            if (B.getRowDimension() != m) {
                throw new IllegalArgumentException("Matrix row dimensions must agree.");
            }
            if (!this.isFullRank()) {
                throw new RuntimeException("Matrix is rank deficient.");
            }
            
            // Copy right hand side
            int nx = B.getColumnDimension();
            double[][] X = B.getArrayCopy();
        
            // Compute Y = transpose(Q)*B
            for (int k = 0; k < n; k++) {
                for (int j = 0; j < nx; j++) {
                    double s = 0.0; 
                    for (int i = k; i < m; i++) {
                    s += QR[i][k]*X[i][j];
                    }
                    s = -s/QR[k][k];
                    for (int i = k; i < m; i++) {
                    X[i][j] += s*QR[i][k];
                    }
                }
            }
            // Solve R*X = Y;
            for (int k = n-1; k >= 0; k--) {
                for (int j = 0; j < nx; j++) {
                    X[k][j] /= Rdiag[k];
                }
                for (int i = 0; i < k; i++) {
                    for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j]*QR[i][k];
                    }
                }
            }
            return (new Matrix(X,n,nx).getMatrix(0,n-1,0,nx-1));
        }

    }    

    public static class Maths {

        public static double hypot(double a, double b) {
           
            double r;
           
           if (Math.abs(a) > Math.abs(b)) {
              r = b/a;
              r = Math.abs(a)*Math.sqrt(1+r*r);
            }else if (b != 0) {
              r = a/b;
              r = Math.abs(b)*Math.sqrt(1+r*r);
            }else {
              r = 0.0;
            }
           return r;
        }
    }    

}