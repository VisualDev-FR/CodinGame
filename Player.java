import java.util.*;

class Player {

    private static int nbOfPlayer;
    private static int activePlayerCount;
    private static int gameTurn;
    
    private static Map<String, int[]> directions;
    private static List<String> comments;
    private static int myID;
    private static Grid gameGrid;

    private static final int PLAYS_COUNT = 100;
    private static final String MAP_PLAYER = "@";
    private static final String MAP_ENNEMY = "!";
    private static final String MAP_BLANK  = ".";
    private static final String MAP_BORDER = "#";

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);

        LightCycle[] lightCycles = initLightCycles();

        while (true){

            comments = new ArrayList<String>();

            lightCycles = updateLightCycles(in, lightCycles);

            System.err.println(lightCycles[myID].toString());
            gameGrid.Print();
            
            System.out.println("LEFT");
        }
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
                gameGrid.setBlank(coord[0], coord[1]);
            }           
        }

        public boolean IsDead(){
            return startRow == -1;
        }

        public LightCycle Update(Scanner in){

            startCol = in.nextInt();    // starting X coordinate of lightcycle (or -1)
            startRow = in.nextInt();    // starting Y coordinate of lightcycle (or -1)
            col = in.nextInt();         // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
            row = in.nextInt();         // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

            if(!IsDead()){
                coords.add(new int[]{row, col});
                gameGrid.setPlayer(row, col, this.ID); 
                gameGrid.setPlayer(startRow, startCol, this.ID); 
            }else if(!erased){
                Erase();
                erased = true;
                return null;
            }

            return this;
        }
    
        public String toString(){
            return String.format("%02d : [%s, %s]", ID, row, col);
        }
    }
    
    public static class Grid{

        private String[][] arrayGrid;
        private boolean[][] occupedPoints;

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

        public boolean canGoto(int row, int col){
            return row >= 0 && row < GRID_HEIGHT && col >= 0 && col < GRID_WIDTH && !occupedPoints[row][col];
        }

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

        /* public int GetVoronoiSurface(int playerID){

            int voronoiSurface = 0;
    
            for (int i = 0; i < this.arrayGrid.length; i++){
    
                for (int j = 0; j < this.arrayGrid[0].length; j++) {
                    
                    int closerOpp = GetCloserPlayer_Absolute(i, j);
    
                    if(closerOpp == playerID) voronoiSurface++;                
                }            
            }

            //comments.add("Voronoi for player " + playerID + " = " + voronoiSurface);
    
            return voronoiSurface;
        }  */

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

        /* public void PrintVoronoi(){

            Grid gridTemp = this.clone();
            
            for (int i = 0; i < gridTemp.arrayGrid.length; i++){

                for (int j = 0; j < gridTemp.arrayGrid[0].length; j++) {
                    
                    int closerOpp = GetCloserPlayer_Absolute(i, j);
    
                    gridTemp.setPlayer(i, j, closerOpp);                
                }            
            }

            gridTemp.Print();
        } */

        public void Print(){
            for(int i = 0; i < arrayGrid.length; i++){
                System.err.println(String.join(" ", arrayGrid[i]));
            }
        }
    }
    
    public static class Simulation{

        private List<LightCycle> m_LightCycles;
        private Grid m_grid;
        private int m_rowStart;
        private int m_colStart;
        private int m_score;
        private long m_start;
        private long m_timer;

        public Simulation(Grid grid, int[] direction, List<LightCycle> lightCycles){
            m_LightCycles = lightCycles;
            m_grid = grid;
            m_rowStart = lightCycles.get(myID).row + direction[0];
            m_colStart = lightCycles.get(myID).col + direction[1];            
        }

        public int getScore(){

            m_start = System.currentTimeMillis();

            for(int i = 0; i < PLAYS_COUNT; i++){
                endGame();
            }

            m_timer = System.currentTimeMillis() - m_start;   

            return m_score;
        }

        private void endGame(){

            Random rand = new Random();

            List<LightCycle> lightCycles = new ArrayList<LightCycle>(m_LightCycles);
            Grid gameGrid = m_grid.clone();

            for(LightCycle l : lightCycles){
                l = l.clone();
            }

            while(!lightCycles.get(myID).IsDead() && m_LightCycles.size() > 1){
                playOneTurn();
            }

            m_score += lightCycles.get(myID).IsDead() ? 0 : 1;
        }

        private void playOneTurn(){

        }
    }

    // PARSING FUNCTIONS

    static LightCycle[] initLightCycles(){

        gameGrid = new Grid();

        directions = new HashMap<String, int[]>();
        directions.put("RIGHT", new int[]{ 0 ,  1});
        directions.put("DOWN",  new int[]{ 1 ,  0});
        directions.put("LEFT",  new int[]{ 0 , -1});
        directions.put("UP",    new int[]{-1 ,  0});

        LightCycle[] lightCycles = new LightCycle[4];

        lightCycles[0] = new LightCycle(0);
        lightCycles[1] = new LightCycle(1);
        lightCycles[2] = new LightCycle(2);
        lightCycles[3] = new LightCycle(3);

        return lightCycles;
    }

    static LightCycle[] updateLightCycles(Scanner in, LightCycle[] lightCycles){

        nbOfPlayer = in.nextInt();      // total number of players (2 to 4).
        myID = in.nextInt();            // your player number (0 to 3).

        for(int i = 0; i < nbOfPlayer; i++){

            if(lightCycles[i] != null){
                lightCycles[i] = lightCycles[i].Update(in);
            }
            else{
                in.nextInt();
                in.nextInt();
                in.nextInt();
                in.nextInt();
            }            
        }

        for(int i = nbOfPlayer; i < 4; i++){
            lightCycles[i] = null;
        }

        return lightCycles;
    }    

    // PRINTING FUNCTIONS

/*     static int GetCloserPlayer_Absolute(int row, int col){

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
    
    static int GetDistance_Absolute(int row1, int col1, int row2, int col2){
        return Math.abs(row1-row2) + Math.abs(col1-col2);
    } */
    
        

    public static void PRINT_COMMENTS(){
        for(String comment : comments){
            System.err.println(comment);
        }        
    }



}