import java.util.*;

class Player {

    private static int nbOfPlayer;    
    private static Map<String, int[]> directions;
    private static List<String> comments;
    private static int myID;
    private static Grid gameGrid;
    private static Random random;

    private static final int PLAYS_COUNT = 500;
    private static final String MAP_BLANK  = ".";

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);

        LightCycle[] lightCycles = initLightCycles();

        while (true){

            lightCycles = updateLightCycles(in, lightCycles);

            String bestMove = "LEFT";
            int bestScore = -1;

            //gameGrid.Print();

            for(String move : getPossibleDirections(lightCycles[myID], gameGrid)){

                Simulation newSim = new Simulation(gameGrid, move, lightCycles);
                int simScore = newSim.getScore();

                System.err.printf("%s = %s (%sms)\n", move, simScore, newSim.getTimer());

                if(simScore > bestScore){
                    bestScore = simScore;
                    bestMove = move;
                }
            }
            System.out.println(bestMove);
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
        public int row;
        public int col;
        private boolean m_isDead;
        public List<int[]> coords;
        
        public LightCycle(int mID){
            coords = new ArrayList<int[]>();
            ID = mID;
        }

        public int getDistanceFrom(int row, int col){
            return Math.abs(this.row - row) + Math.abs(this.col - col);
        }

        public LightCycle clone(){

            LightCycle mNewLightCycle = new LightCycle(this.ID);

            mNewLightCycle.coords = new ArrayList<>(this.coords);
            mNewLightCycle.row = this.row;
            mNewLightCycle.col = this.col;

            return mNewLightCycle;
        }

        public void kill(Grid grid){
            m_isDead = true;
            for(int[] coord : coords){
                grid.setBlank(coord[0], coord[1]);
            }           
        }

        public LightCycle Update(Scanner in, Grid grid){

            int startCol = in.nextInt();    // starting X coordinate of lightcycle (or -1)
            int startRow = in.nextInt();    // starting Y coordinate of lightcycle (or -1)
            col = in.nextInt();         // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
            row = in.nextInt();         // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

            if(startCol > -1){
                coords.add(new int[]{row, col});
                grid.setPlayer(row, col, this.ID); 
                grid.setPlayer(startRow, startCol, this.ID); 
            }else{
                this.kill(grid);
                return null;
            }

            return this;
        }

        public LightCycle simUpdate(Grid grid, int rowNext, int colNext){

            this.row = rowNext;
            this.col = colNext;

            if(grid.canGoto(rowNext, colNext)){
                this.coords.add(new int[]{rowNext, colNext});
                grid.setPlayer(rowNext, colNext, this.ID); 
                return this;
            }else{
                this.kill(grid);
                return null;
            }            
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

        public String get(int row, int column){            
            return arrayGrid[row][column];
        }

        public Grid setPlayer(int row, int column, int playerID){
            arrayGrid[row][column] = Integer.toString(playerID);
            occupedPoints[row][column] = true;
            return this;
        }

        public Grid setBlank(int row, int col){
            arrayGrid[row][col] = "@";
            occupedPoints[row][col] = false;
            return this;
        }

        public boolean canGoto(int row, int col){

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

        public int[] GetVoronoiSurfaces(LightCycle[] lightCycles){

            int[] voronoiSurfaces = new int[]{0, 0, 0, 0};
    
            for (int row = 0; row < GRID_HEIGHT; row++){
    
                for (int col = 0; col < GRID_WIDTH; col++) {
                    
                    int minDist = 9999;
                    int minID = 0;

                    for(int i = 0; i < nbOfPlayer; i++){

                        if(lightCycles[i] != null){

                            int dist = lightCycles[i].getDistanceFrom(row, col);

                            if(dist < minDist){
                                minDist = dist;
                                minID = i;
                            }                            
                        }
                    }

                    voronoiSurfaces[minID]++;
                }            
            }

            return voronoiSurfaces;
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
        
                        if(canGoto(borderRow, borderCol)){
    
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

        public void Print(){
            for(int i = 0; i < arrayGrid.length; i++){
                System.err.println(String.join(" ", arrayGrid[i]));
            }
        }
    }
    
    public static class Simulation{

        private LightCycle[] m_LightCycles;
        private Grid m_grid;
        private int m_score;
        private long m_start;
        private long m_timer;
        private String m_firstMove;

        public Simulation(Grid grid, String firstMove, LightCycle[] lightCycles){
            m_LightCycles = lightCycles;
            m_grid = grid;
            m_firstMove = firstMove;            
        }

        public String getFirstMove(){
            return m_firstMove;
        }        

        public long getTimer(){
            return m_timer;
        }

        public int getScore(){

            m_start = System.currentTimeMillis();

            int score = endGame();
            if(score < 0){
                return -1;
            }

            for(int i = 0; i < PLAYS_COUNT; i++){
                m_score += endGame();
            }

            m_timer = System.currentTimeMillis() - m_start;   

            return m_score;
        }

        private int endGame(){

            LightCycle[] simLightCycles = m_LightCycles.clone();
            Grid simGrid = m_grid.clone();

            for(int i = 0; i < nbOfPlayer; i++){
                if(simLightCycles[i] != null){
                    simLightCycles[i] = simLightCycles[i].clone();
                }
            }

            playFirstTurn(simGrid, simLightCycles);

            if(simLightCycles[myID] == null){
                return - 1;
            }
            int index = 0;
            /* System.err.println("simIndex = " + index);
            simGrid.Print(); */            
            while(simLightCycles[myID] != null && countLivingLightCycles(simLightCycles) > 1 && index < 2400){
                playOneTurn(simGrid, simLightCycles);
                index++;
            }
            /* System.err.println("simIndex = " + index);
            simGrid.Print(); */

            return simLightCycles[myID] == null ? 0 : 1;
        }

        private void playFirstTurn(Grid grid, LightCycle[] lightCycles){

            for(int i = 0; i < nbOfPlayer; i++){

                if(lightCycles[i] != null){

                    if(i == myID){    

                        int rowNext = lightCycles[i].row + directions.get(m_firstMove)[0];
                        int colNext = lightCycles[i].col + directions.get(m_firstMove)[1];                        
                        
                        lightCycles[i] = lightCycles[i].simUpdate(grid, rowNext, colNext);
                    }
                    else{
                        //playSmart(grid, lightCycles, i);
                        int[] nextDirection = getRandomDirection(lightCycles[i], grid);
                        lightCycles[i] = lightCycles[i].simUpdate(grid, lightCycles[i].row + nextDirection[0], lightCycles[i].col + nextDirection[1]);
                    }
                }
            }            

        }

        private void playOneTurn(Grid grid, LightCycle[] lightCycles){

            for(int i = 0; i < nbOfPlayer; i++){

                if(lightCycles[i] != null){

                    /* if(i == myID){
                        playSmart(grid, lightCycles, myID);
                    }
                    else{
                        int[] nextDirection = getRandomDirection(lightCycles[i], grid);                        
                        lightCycles[i] = lightCycles[i].simUpdate(grid, lightCycles[i].row + nextDirection[0], lightCycles[i].col + nextDirection[1]);
                    } */

                    int[] nextDirection = getRandomDirection(lightCycles[i], grid);                        
                    lightCycles[i] = lightCycles[i].simUpdate(grid, lightCycles[i].row + nextDirection[0], lightCycles[i].col + nextDirection[1]);

                    //playSmart(grid, lightCycles, i);
                }
            } 
        }

        private void playSmart(Grid grid, LightCycle[] lightCycles, int playerID){

            int[] bestDirection = new int[]{0, 1};
            int maxSurface = 0;
            //int minOppDist = 9999;

            for(int[] direction : directions.values()){

                int nextRow = lightCycles[playerID].row + direction[0];
                int nextCol = lightCycles[playerID].col + direction[1];

                if(grid.canGoto(nextRow, nextCol)){

                    int surface = grid.GetDiffusedSurface(nextRow, nextCol);
                    //int oppDist = GetCloserOpponent(nextRow, nextCol, lightCycles);
                    //int[] voronoiSurfaces = grid.GetVoronoiSurfaces(lightCycles);
                    /* int[] surfaces = grid.GetVoronoiSurfaces(lightCycles);
                    int maxID = getMaxID(surfaces); */

                    if(surface > maxSurface){ // || (surface == maxSurface && oppDist < minOppDist)){
                        bestDirection = direction;
                        //maxSurface = surface;
                        //minOppDist = oppDist;
                    }
                }
            }

            int rowNext = lightCycles[playerID].row + bestDirection[0];
            int colNext = lightCycles[playerID].row + bestDirection[1];

            lightCycles[playerID] = lightCycles[playerID].simUpdate(grid, rowNext, colNext);
        }
    }

    // PARSING FUNCTIONS

    private static int getMaxID(int[] list){
        int valMax = -999; 
        int maxID = -1;
        for(int i = 0; i < list.length; i++){
            if(list[i] > valMax){
                maxID = i;
                valMax = list[i];
            }
        }
        return maxID;
    }

    private static LightCycle[] initLightCycles(){

        random = new Random();
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

    private static LightCycle[] updateLightCycles(Scanner in, LightCycle[] lightCycles){

        nbOfPlayer = in.nextInt();      // total number of players (2 to 4).
        myID = in.nextInt();            // your player number (0 to 3).

        for(int i = 0; i < nbOfPlayer; i++){

            if(lightCycles[i] != null){
                lightCycles[i] = lightCycles[i].Update(in, gameGrid);
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

    private static int countLivingLightCycles(LightCycle[] lightCycles){
        int count = 0;
        for(LightCycle l : lightCycles){
            count += l == null ? 0 : 1;
        }
        return count;
    }

    // PRINTING FUNCTIONS

    public static void PRINT_COMMENTS(){
        for(String comment : comments){
            System.err.println(comment);
        }        
    }

    public static int randBetween(int min, int max){
        return random.nextInt((max - min) + 1) + min;
    }

    public static List<String> getPossibleDirections(LightCycle lightCycle, Grid grid){

        List<String> possibleDirections = new ArrayList<String>();

        for(String direction : directions.keySet()){

            int rowNext = lightCycle.row + directions.get(direction)[0];
            int colNext = lightCycle.col + directions.get(direction)[1];

            if(grid.canGoto(rowNext, colNext)){
                possibleDirections.add(direction);
            }
        }
        return possibleDirections;
    }    

    public static int[] getRandomDirection(LightCycle lightCycle, Grid grid){
        
        List<String> possibleDirections = getPossibleDirections(lightCycle, grid);

        if(possibleDirections.size() > 1){
            return directions.get(possibleDirections.get(random.nextInt(possibleDirections.size()))); 
        }
        else if(possibleDirections.size() == 1){
            return directions.get(possibleDirections.get(0)); 
        }
        else{
            return new int[]{0, 1};
        }        
       
    }
}