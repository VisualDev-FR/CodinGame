import java.util.*;

class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        
        int numberOfFolds = in.nextInt();
        
        int width = in.nextInt();
        int height = in.nextInt();
        
        if (in.hasNextLine()) in.nextLine();

        String[][] foldedPaper = new String[height][width];

        for (int i = 0; i < height; i++) {
            foldedPaper[i] = in.nextLine().split(" ");

            System.err.println(String.join("", foldedPaper[i]));
        }

        System.err.println(" ");

        foldedPaper = Unfold(foldedPaper);
        
        for (int i = 0; i < height; i++) {
            System.err.println(String.join("", foldedPaper[i]));
        }        

    }

    static String[][] Unfold(String[][] folded){

        int width = folded[0].length;
        int height = folded.length;

        String[][] unfolded = new String[height*2][width*2];
        
        for(int i = 0; i < height; i++){

            for(int j = 0; j < width; j++){

                unfolded[height + i][width + j] = folded[i][j];
                unfolded[j][i] = folded[i][j];
                unfolded[height + i][j] = folded[i][j];                
                unfolded[i][width + j] = folded[i][j];
            }
        }

        return unfolded;
    }
}