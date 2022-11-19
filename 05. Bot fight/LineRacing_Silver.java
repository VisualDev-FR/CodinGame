import java.util.*;

class Player {

    static int nbOfPlayer;
    static int activePlayerCount;
    static int gameTurn;
    
    static Map<String, int[]> directions;
    static List<String> comments;

    static Map <Integer, LightCycle> lightCycles;
    static Map <Integer, LightCycle> ennemies;
    static LightCycle myPlayer;
    static Grid grid;

    static final String MAP_PLAYER = "@";
    static final String MAP_ENNEMY = "!";
    static final String MAP_BLANK  = ".";
    static final String MAP_BORDER = "#";

    static final int MINIMAX_DEPTH = 6;

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);

        INITIALIZE(in);

        while (true){

            comments = new ArrayList<String>();

            UPDATE(in);            

            grid.Print();
            //grid.PrintVoronoi();

            String bestDirection = GetDirection();

            System.out.println(bestDirection);

            PRINT_COMMENTS();

            gameTurn++;
        }
    }

    // SUBMAIN FUNCTIONS

    public static String GetDirection(){

        String bestDirection = "ANY DIRECTION FOUND";

        int maxDiffusedSurface = -999;

        for(String direction : directions.keySet()){

            int[] dirCoord = directions.get(direction);

            int nextRow = myPlayer.row + dirCoord[0];
            int nextCol = myPlayer.col + dirCoord[1];

            if(grid.isPointReachable(nextRow, nextCol)){

                Grid gridTemp = grid.clone().setPlayer(nextRow, nextCol, myPlayer.ID);

                int myDiffusedSurface = gridTemp.GetDiffusedSurface(nextRow, nextCol);
                int myVoronoiSurface = gridTemp.GetVoronoiSurface(myPlayer.ID);

                int maxEnnemyVoronoi = -999;
                int maxEnnemyDiffused = -999;

                comments.add(String.format("%S, ME : Diffused = %s, Voronoi = %s [%s, %s]", direction, myDiffusedSurface, myVoronoiSurface, myPlayer.row, myPlayer.col));

                for(LightCycle ennemy : ennemies.values()){

                    int diffusedSurface = gridTemp.GetDiffusedSurface(ennemy.row, ennemy.col);
                    int voronoiSurface = gridTemp.GetVoronoiSurface(ennemy.ID);

                    comments.add(String.format("%S, %02d : Diffused = %s, Voronoi = %s [%s, %s]", direction,  ennemy.ID, diffusedSurface, voronoiSurface, ennemy.row, ennemy.col));

                    maxEnnemyDiffused = Math.max(maxEnnemyDiffused, diffusedSurface);
                    maxEnnemyVoronoi = Math.max(maxEnnemyVoronoi, voronoiSurface);
                }

                comments.add(" ");

                if(myDiffusedSurface > maxDiffusedSurface || (myDiffusedSurface >= maxEnnemyDiffused && myVoronoiSurface >= maxEnnemyVoronoi)){
                    bestDirection = direction;
                    maxDiffusedSurface = myDiffusedSurface;
                }
            }
        }

        return bestDirection;        
    }    

    // CLASSES

    public static class Border{

        private List<Border> parentList;
        private int row;
        private int col;

        public Border(int mRow, int mCol, Border mParent){
            
            row = mRow;
            col = mCol;
            parentList = new ArrayList<Border>(mParent.parentList);
            parentList.add(mParent);
                        
        }

        public Border(int mRow, int mCol){
            parentList = new ArrayList<Border>();
            row = mRow;
            col = mCol;
        }

        public int TotalDist(){
            return parentList.size();
        }
    }     

    public static class LightCycle{

        public int ID;
        public int startRow;
        public int startCol;
        public int row;
        public int col;

        public int voronoiSurface;
        public int diffusedSurface;

        public boolean erased;
        public List<int[]> coords;
        
        public LightCycle(int mID){
            coords = new ArrayList<int[]>();
            ID = mID;
            erased = false;
        }

        public LightCycle clone(){

            LightCycle mNewLightCycle = new LightCycle(this.ID);

            mNewLightCycle.coords = new ArrayList<>(this.coords);

            mNewLightCycle.startRow = this.startRow;
            mNewLightCycle.startCol = this.startCol;
            mNewLightCycle.row = this.row;
            mNewLightCycle.col = this.col;
            mNewLightCycle.voronoiSurface = this.voronoiSurface;
            mNewLightCycle.diffusedSurface = this.diffusedSurface;
            mNewLightCycle.erased = this.erased;

            return mNewLightCycle;
        }

        public void Erase(){

            for(int[] coord : coords){
                grid.setBlank(coord[0], coord[1]);
            }           
        }

        public boolean IsDead(){
            return startRow == -1 && startCol == -1 && row == -1 && col == -1;
        }

        public void Update(Scanner in){

            startCol = in.nextInt();    // starting X coordinate of lightcycle (or -1)
            startRow = in.nextInt();    // starting Y coordinate of lightcycle (or -1)
            col = in.nextInt();         // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
            row = in.nextInt();         // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

            if(!IsDead()){
                coords.add(new int[]{row, col});
                grid.setPlayer(row, col, this.ID); 
                grid.setPlayer(startRow, startCol, this.ID); 
            }else if(!erased){
                Erase();
                erased = true;
            }
        }
        
    }
    
    public static class Grid{

        String[][] arrayGrid;
        boolean[][] occupedPoints;

        static final int GRID_WIDTH = 30;
        static final int GRID_HEIGHT = 20;

        // CONSTRUCTORS
        
        public Grid(){

            arrayGrid = new String[GRID_HEIGHT][GRID_WIDTH];
            occupedPoints = new boolean[GRID_HEIGHT][GRID_WIDTH];

            for(int i = 0; i < arrayGrid.length; i++){
                for(int j = 0; j < arrayGrid[0].length; j++){
                    arrayGrid[i][j] = MAP_BLANK;
                }
            }
        }

        public Grid clone(){

            Grid mNewGrid = new Grid();

            mNewGrid.arrayGrid = this.arrayGrid.clone();
            mNewGrid.occupedPoints = this.occupedPoints.clone();

            for (int i = 0; i < GRID_HEIGHT; i++){
                mNewGrid.arrayGrid[i] = this.arrayGrid[i].clone();
                mNewGrid.occupedPoints[i] = this.occupedPoints[i].clone();
            }

            return mNewGrid;
        }

        // VALUES HANDLING

        public String get(int row, int column){            
            return arrayGrid[row][column];
        }

        public Grid setPlayer(int row, int column, int playerID){
            arrayGrid[row][column] = Integer.toString(playerID);
            occupedPoints[row][column] = true;
            return this;
        }

        public Grid setBlank(int row, int col){
            arrayGrid[row][col] = MAP_BLANK;
            occupedPoints[row][col] = false;
            return this;
        }

        public boolean isPointReachable(int row, int col){

            boolean isReachable = false;

            if(isPointOnGrid(row, col)){
                isReachable = !occupedPoints[row][col];
            }

            return isReachable;
        }

        public boolean isPointOnGrid(int row, int col){
            return row >= 0 && row < GRID_HEIGHT && col >= 0 && col < GRID_WIDTH;
        }

        // PARSING FUNCTIONS

        public int GetVoronoiSurface(int playerID){

            int voronoiSurface = 0;
    
            for (int i = 0; i < this.arrayGrid.length; i++){
    
                for (int j = 0; j < this.arrayGrid[0].length; j++) {
                    
                    int closerOpp = GetCloserPlayer_Absolute(i, j);
    
                    if(closerOpp == playerID) voronoiSurface++;                
                }            
            }

            //comments.add("Voronoi for player " + playerID + " = " + voronoiSurface);
    
            return voronoiSurface;
        } 

        public int GetDiffusedSurface(int startRow, int startCol){

            List<int[]> borders = new ArrayList<int[]>();
            boolean[][] visitedPoints = new boolean[GRID_HEIGHT][GRID_WIDTH];
    
            borders.add(new int[]{startRow, startCol});
    
            int bordersCount = 1;
    
            while(borders.size() > 0){
    
                List<int[]> bordersTemp = new ArrayList<int[]>(borders);
    
                for(int[] border : bordersTemp){
    
                    borders.remove(borders.indexOf(border));
    
                    for(int[] direction : directions.values()){
    
                        int borderRow = border[0] + direction[0];
                        int borderCol = border[1] + direction[1];
        
                        if(isPointReachable(borderRow, borderCol)){
    
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

        // PRINTING METHODS

        public void PrintVoronoi(){

            Grid gridTemp = this.clone();
            
            for (int i = 0; i < gridTemp.arrayGrid.length; i++){

                for (int j = 0; j < gridTemp.arrayGrid[0].length; j++) {
                    
                    int closerOpp = GetCloserPlayer_Absolute(i, j);
    
                    gridTemp.setPlayer(i, j, closerOpp);                
                }            
            }

            gridTemp.Print();
        }

        public void Print(){
            for(int i = 0; i < arrayGrid.length; i++){
                System.err.println(String.join(" ", arrayGrid[i]));
            }
        }
    }
    
    // GENERIC FUNCTIONS

    static int GetDistance_Dijkstra(int startRow, int startCol, int destRow, int destCol, String[][] arrayGrid){

        List<Border> borders = new ArrayList<Border>();
        boolean[][] investigatePoints = new boolean[20][30];
        Border bestBorder = null;
        boolean pathFound = false;

        borders.add(new Border(startRow, startCol));
        investigatePoints[startRow][startCol] = true;

        while(borders.size() > 0 && !pathFound){

            //System.err.printf("borders : %s \n", borders.size());

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));                

                for(int[] direction : directions.values()){

                    int nextRow = border.row + direction[0];
                    int nextCol = border.col + direction[1];

                    if(IsValidForPathFinding(nextRow, nextCol, arrayGrid)){

                        if(!investigatePoints[nextRow][nextCol]){

                            Border mNewBorder = new Border(nextRow, nextCol, border);

                            if(nextRow == destRow && nextCol == destCol){
                                pathFound = true;
                                bestBorder = mNewBorder;
                            }
                            
                            borders.add(mNewBorder);                            
                        }

                        investigatePoints[nextRow][nextCol] = true;
                    }        
                }
            }
        }        

        return bestBorder != null ? bestBorder.TotalDist() : 9998;
    }

    static int GetCloserPlayer_Dijkstra(int row, int col){

        int minDist = 9999;
        int closerPlayer = -1;

        for(LightCycle lightCycle : lightCycles.values()){

            if(!lightCycle.IsDead()){

                int playerDist = GetDistance_Dijkstra(row, col, lightCycle.row, lightCycle.col, grid.arrayGrid);

                if(playerDist < minDist){
                    minDist = playerDist;
                    closerPlayer = lightCycle.ID;
                }
            }
        }

        return closerPlayer;
    }    

    static int GetCloserPlayer_Absolute(int row, int col){

        int minDist = 9999;
        int closerPlayer = 0;

        for(LightCycle player : lightCycles.values()){

            if(!player.IsDead()){

                int lightDist = GetDistance_Absolute(row, col, player.row, player.col);

                if(lightDist < minDist){
                    minDist = lightDist;
                    closerPlayer = player.ID;
                }
            }
        }

        return closerPlayer;
    }       
    
    static boolean IsValidForPathFinding(int row, int col, String[][] arrayGrid){

        boolean isValid = false;

        for(LightCycle lightCycle : lightCycles.values()){

            if(lightCycle.ID != myPlayer.ID && row == lightCycle.row && col == lightCycle.col) return true; // TODO: mettre false ici pour voir ce que ça fait

        }

        if(col >= 0 && col < 30 && row >=0 && row < 20){
            isValid = arrayGrid[row][col].equals(".");
        }
        return isValid;
    }    

    static int GetDistance_Absolute(int row1, int col1, int row2, int col2){
        return Math.abs(row1-row2) + Math.abs(col1-col2);
    }

    // PARSING FUNCTIONS

    static void INITIALIZE(Scanner in){

        directions = new HashMap<String, int[]>();
        directions.put("RIGHT", new int[]{ 0 ,  1});
        directions.put("DOWN",  new int[]{ 1 ,  0});
        directions.put("LEFT",  new int[]{ 0 , -1});
        directions.put("UP",    new int[]{-1 ,  0});
        
        grid = new Grid();

        nbOfPlayer = in.nextInt(); // total number of players (2 to 4).
        int myPlayerID = in.nextInt(); // your player number (0 to 3).

        lightCycles = new HashMap<Integer, LightCycle>();
        ennemies = new HashMap<Integer, LightCycle>();

        for (int i = 0; i < nbOfPlayer; i++){
            
            LightCycle mLightCycle = new LightCycle(i);

            if(!mLightCycle.IsDead()) activePlayerCount ++;

            mLightCycle.Update(in);

            lightCycles.put(i, mLightCycle);
            
            if(mLightCycle.ID == myPlayerID){
                myPlayer = mLightCycle;
            }else{
                ennemies.put(mLightCycle.ID, mLightCycle);
            }
        }

        myPlayer = lightCycles.get(myPlayer.ID);
    }

    static void UPDATE(Scanner in){

        if(gameTurn == 0) return;

        nbOfPlayer = in.nextInt(); // total number of players (2 to 4).
        int myPlayerID = in.nextInt(); // your player number (0 to 3).

        ennemies = new HashMap<Integer, LightCycle>();

        for(LightCycle lightCycle : lightCycles.values()){
            lightCycle.Update(in);
            
            if(lightCycle.ID == myPlayerID){
                myPlayer = lightCycle;
            }else{
                ennemies.put(lightCycle.ID, lightCycle);
            }            
        }
    }    

    // PRINTING FUNCTIONS

    public static void PRINT_COMMENTS(){
        for(String comment : comments){
            System.err.println(comment);
        }        
    }
}