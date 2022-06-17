import java.util.*;

class Solution {

    static String[][] unfoldedPaper; 
    public static void main(String args[]) {
        
        Scanner in = new Scanner(System.in);
        
        int numberOfFolds = in.nextInt();
        
        int width = in.nextInt();
        int height = in.nextInt();
        
        if (in.hasNextLine()) in.nextLine();

        unfoldedPaper = new String[height][width];
        
        int borderCount = 0;
        int middleCount = 0;

        for (int i = 0; i < height; i++) {
            
            unfoldedPaper[i] = in.nextLine().split("");

            for(int j = 0; j < unfoldedPaper[i].length; j++){

                if(unfoldedPaper[i][j].equals("#")){
                    if(i == 0 || j == 0){
                        borderCount++;
                    }else{
                        middleCount++;
                    }
                } 
            }
        }

        System.err.printf("borders : %s, middle : %s\n", borderCount, middleCount);

        for (int i = 0; i < numberOfFolds; i++) {
            unfoldedPaper = Unfold(unfoldedPaper);
        }

        
        
        for (int i = 0; i < unfoldedPaper.length; i++) {
            System.err.println(String.join("", unfoldedPaper[i]));
        }

        System.out.println(CountPapers(unfoldedPaper));
    }

    // UNFOLDING FUNCTIONS

    static String[][] Unfold(String [][]folded){

        String[][] unfolded = new String[][]{};

        unfolded = HorizontalUnfold(folded);
        unfolded = VerticalUnfold(unfolded);

        return unfolded;
    }

    static String[][] HorizontalUnfold(String[][] folded){

        int width = folded[0].length;
        int height = folded.length;

        String[][] unfolded = new String[height][width*2];
        
        for(int i = 0; i < height; i++){

            unfolded[i] = (new StringBuilder(String.join("", folded[i])).reverse().toString()+String.join("", folded[i])).split("");
        }

        return unfolded;
    }

    static String[][] VerticalUnfold(String[][] folded){

        int height = folded.length;
        int width = folded[0].length; 

        String[][] unfolded = new String[height*2][width];
        
        for(int i = 0; i < height; i++){

            unfolded[height - i-1] = folded[i];
            unfolded[height + i] = folded[i]; 

        }

        return unfolded;
    }
    
    // DIFFUSING FUNCTIONS

    static long CountPapers(String[][] unfoldedPaper){
        
        List<int[]> directions = new ArrayList<int[]>();
        directions.add(new int[]{ 0 ,  1});
        directions.add(new int[]{ 0 , -1});
        directions.add(new int[]{ 1 , 0 });
        directions.add(new int[]{-0 , 0 });

        boolean[][] visitedPoints = new boolean[unfoldedPaper.length][unfoldedPaper[0].length];

        long nbSurfaces = 0;

        for(int i = 0; i < unfoldedPaper.length; i++){

            for(int j = 0; j < unfoldedPaper[0].length; j++){

                if(!visitedPoints[i][j] && unfoldedPaper[i][j].equals("#")){
                    
                    nbSurfaces++;
                    visitedPoints[i][j] = true;

                    List<int[]> borders = new ArrayList<int[]>();
                    borders.add(new int[]{i, j});

                    while(borders.size() > 0){
                        
                        List<int[]> bordersTemp = new ArrayList<int[]>(borders);

                        for(int[] border : bordersTemp){
                            
                            borders.remove(borders.indexOf(border));

                            if(!visitedPoints[border[0]][border[1]]){
                                
                                visitedPoints[border[0]][border[1]] = true;

                                for(int[] direction : directions){

                                    int nextRow = border[0] + direction[0];
                                    int nextCol = border[1] + direction[1];
        
                                    if(unfoldedPaper[nextRow][nextCol].equals("#")){

                                        borders.add(new int[]{nextRow, nextCol});    
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return nbSurfaces;        
    }

  
}