import java.util.*;

import javax.swing.text.Position;

class Player {

    static int nbOfPlayer;
    static int myPlayerID;

    static Map <Integer, LightCycle> lightCycles;
    static Map<String, int[]> directions;
    static Grid grid;
    static Grid diffuseGrid;

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);

        boolean firstTurn = true;

        ReadFirstInputs(in);

        while (true){

            if(!firstTurn) ReadInputs(in);

            grid.Print();

/* 
            int[] reachablePositions = CountMaxReachablePositions();

            for(int i = 0; i < reachablePositions.length; i++){
                System.err.printf("%s : %s\n", i, reachablePositions[i]);
            }
*/

            for(LightCycle lightCycle : lightCycles.values()){
                System.err.printf("%s : %s\n", lightCycle.ID, GetDiffusedSurface(lightCycle, grid));
            }

            String bestDirection = "";
            int bestScore = 0;

            System.err.printf("myPosition = %s %s\n", lightCycles.get(myPlayerID).Y1, lightCycles.get(myPlayerID).X1);

            for(String direction : directions.keySet()){

                int[] dirCoord = directions.get(direction);

                int nextRow = lightCycles.get(myPlayerID).Y1 + dirCoord[0];
                int nextCol = lightCycles.get(myPlayerID).X1 + dirCoord[1];
                
                int directionScore = EvaluateScore(nextRow, nextCol, 0);

                System.err.printf("%s : %s\n", direction, directionScore);

                if(directionScore > bestScore || (directionScore == bestScore && bestScore == 0)){
                    bestDirection = direction;
                    bestScore = directionScore;
                }
            }

            System.out.println(bestDirection);
            firstTurn = false;
        }
    }
/*

    //initial Call

    maxEval = +infinity
    for each direction from currentPosition{
        eval = minimax(currentPosition, 6, true, -infinity, +infinity)
        maxEval = Math.max(maxEval, eval)
        bestDirection = direction
    }

    Play maxEval

    static String MiniMax(int[] positionInMinimaxGraph, int depth, int maximizingPLayer, int alpha, int beta){
 
        if depth == 0 or gameOver in position{
            return static evaluation of position
        }

        if maximizingPlayer == true{
            
            maxEval = -infinity
            
            for each child of currentPosition{
                eval = minimax(child position, depth-1, false, alpha, beta)
                maxEval = Math.max(maxEval, eval)
                alpha = Math.max(alpha, eval)
                
                if(beta <= alpha){
                    break;
                }
            }
            return maxEval;

        }else{
            
            minEval = +infinity
            
            for each child of currentPosition{
                
                eval = minimax(childPosition, depth-1, true, alpha, beta)
                minEval = Math.min(minEval, eval)
                beta = Math.min(beta, eval)
                
                if(beta <= alpha){
                    break;
                }
            }
            Return minEval
        }
    }
*/

    static int[] CountMaxReachablePositions(Grid mGrid){

        int[] reachablePositions = new int[nbOfPlayer];

        for(int i = 0; i < mGrid.exploredGrid.length; i++){

            for(int j = 0; j < mGrid.exploredGrid[0].length; j++){

                if(mGrid.exploredGrid[i][j].equals(".")){

                    int minDist = 9999;
                    int minID = -1;
                        
                    for(LightCycle lightCycle : lightCycles.values()){

                        int lighDist = GetDistance(i, j, lightCycle.Y1, lightCycle.X1);

                        if(lighDist < minDist){
                            minDist = lighDist;
                            minID = lightCycle.ID;
                        }                                
                        
                    }

                    reachablePositions[minID]++;
                }
            }
        }
                
        return reachablePositions;
    }

    static int EvaluateScore(int nextRow, int nextCol, int bestScore){

        if(!IsPositionValid(nextRow, nextCol, grid)) return 0;

        return 1;
    }

    static boolean IsPositionValid(int row, int col, Grid mGrid){

        boolean isValid = false;

        if(col >= 0 && col < 30 && row >=0 && row < 20){

            isValid = mGrid.get(row, col).equals(".");

        }

        return isValid;

    }

    static void ReadFirstInputs(Scanner in){

        directions = new HashMap<String, int[]>();
        directions.put("RIGHT", new int[]{ 0 ,  1});
        directions.put("LEFT",  new int[]{ 0 , -1});
        directions.put("UP",    new int[]{-1 ,  0});
        directions.put("DOWN",  new int[]{ 1 ,  0});  
        
        grid = new Grid();

        nbOfPlayer = in.nextInt(); // total number of players (2 to 4).
        myPlayerID = in.nextInt(); // your player number (0 to 3).

        lightCycles = new HashMap<Integer, LightCycle>();
        
        for (int i = 0; i < nbOfPlayer; i++){
            
            LightCycle mLightCycle = new LightCycle(i);

            mLightCycle.Update(in);

            lightCycles.put(i, mLightCycle);            
        }
    }

    static int GetDistance(int row1, int col1, int row2, int col2){
        return Math.abs(row1-row2) + Math.abs(col1-col2);
    }

    static void ReadInputs(Scanner in){

        nbOfPlayer = in.nextInt(); // total number of players (2 to 4).
        myPlayerID = in.nextInt(); // your player number (0 to 3).

        for(LightCycle lightCycle : lightCycles.values()){
            lightCycle.Update(in);
        }
    }

    public static class LightCycle{

        private int ID;
        private int X0;
        private int Y0;
        private int X1;
        private int Y1;

        private List<int[]> coords;
        private boolean isDead;

        public LightCycle(int mID){            
            coords = new ArrayList<int[]>();
            ID = mID;
            isDead = false;
        }

        public void Update(Scanner in){

            X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
            Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
            X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
            Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

            isDead = X0 == -1 && Y0 == -1 && X1 == -1 && Y1 == -1;

            if(isDead){

                for(int[] coord : coords){
                    grid.set(coord[1], coord[0], ".");
                }

            }else{
                coords.add(new int[]{X1, Y1});
                grid.set(Y1, X1, Integer.toString(myPlayerID));
            }
            
        }
    }
    
    public static class Grid{

        String[][] exploredGrid;
        
        public Grid(){

            exploredGrid = new String[20][30];

            for(int i = 0; i < exploredGrid.length; i++){
                for(int j = 0; j < exploredGrid[0].length; j++){
                    exploredGrid[i][j] = ".";
                }
            }
        }

        public String get(int row, int column){            
            return exploredGrid[row][column];
        }

        public void set(int row, int column, String value){
            exploredGrid[row][column] = value;
        }

        public void Print(){

            for(int i = 0; i < exploredGrid.length; i++){

                for(int j = 0; j < exploredGrid[0].length; j++){

                    //System.err.print(exploredGrid[i][j]);

                    //if(j % 5 == 0) System.err.print ("  ");

                    if(exploredGrid[i][j].equals(".")){

                        int myDist = GetDistance(i, j, lightCycles.get(myPlayerID).Y1, lightCycles.get(myPlayerID).X1);
                        int minDist = 9999;
                        
                        for(LightCycle lightCycle : lightCycles.values()){
                            if(lightCycle.ID != myPlayerID){                                
                                minDist = Math.min(minDist, GetDistance(i, j, lightCycle.Y1, lightCycle.X1));                                
                            }
                        }
                        System.err.print(myDist < minDist ? "." : (myDist == minDist ? "+" : "-"));
                        
                    }else if(exploredGrid[i][j].equals(Integer.toString(myPlayerID))){
                        System.err.print("#");
                        //System.err.printf("%s %s val = %s / myID = %s\n", i, j, exploredGrid[i][j], myPlayerID);
                    }else{                        
                        System.err.print(exploredGrid[i][j]);
                    }   
                }
                System.err.print("\n");
            }
        }
    }

    // TESTING

    static int GetDiffusedSurface(LightCycle mLightCycle, Grid mGrid){

        List<int[]> borders = new ArrayList<int[]>();
        boolean[][] visitedPoints = new boolean[20][30];

        borders.add(new int[]{mLightCycle.Y1, mLightCycle.X1});

        int bordersCount = 1;

        while(borders.size() > 0){

            List<int[]> bordersTemp = new ArrayList<int[]>(borders);

            for(int[] border : bordersTemp){

                borders.remove(borders.indexOf(border));

                for(int[] direction : directions.values()){

                    int borderRow = border[0] + direction[0];
                    int borderCol = border[1] + direction[1];
    
                    if(IsPositionValid(borderRow, borderCol, mGrid)){

                        if(visitedPoints[borderRow][borderCol] == false){

                            visitedPoints[borderRow][borderCol] = true;
                            borders.add(new int[]{borderRow, borderCol});
                            bordersCount++;
                        }
                    }
                }
            }
        }

        return bordersCount;
    }

    static Integer[] GetCloserLightCycle(int row, int col){

        List<Integer> distances = new ArrayList<Integer>();
        int minDist = 20 * 30;
        
        for(LightCycle lightCycle : lightCycles.values()){

            int lighDist = GetDistance(row, col, lightCycle.Y1, lightCycle.X1);

            if(lighDist < minDist){
                
                distances = new ArrayList<Integer>();                
                distances.add(lightCycle.ID);

            }else if(lighDist == minDist){
                
                distances.add(lightCycle.ID);
            }            
        }        

        return distances.toArray(new Integer[0]);
    }    
}


