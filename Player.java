import java.util.*;

class Player {

    private static Map<String, int[]> directions;
    private static int nbOfPlayer;
    private static int myID;
    private static final int PLAYS_COUNT = 10;

    public static void main(String args[]) {

        //main_Vanilla();
        //main_Minimax();
        main_Simulation();
    }

    public static void main_Minimax(){

        Scanner in = new Scanner(System.in);

        Grid gameGrid = new Grid();
        Snake[] snakes = initSnakes();
        directions = initDirections();     

        while (true){

            long start = System.currentTimeMillis();

            snakes = updateSnakes(in, snakes, gameGrid);            
            gameGrid.update(snakes);

            String bestMove = "UP";
            int maxEval = -1;

            for(String strDirection : directions.keySet()){
                
                Node directionNode = new Node(gameGrid, snakes);
                int nodeEval = minimax(directionNode, 3, myID);

                System.err.printf("%s : %s\n", strDirection, nodeEval);

                if(nodeEval > maxEval){
                    maxEval = nodeEval;
                    bestMove = strDirection;
                }
            }
            
            printTimer(start);
            gameGrid.Print();
            
            System.out.println(bestMove);
        }        
    }

    public static void main_Vanilla(){

        Scanner in = new Scanner(System.in);

        Grid gameGrid = new Grid();
        Snake[] snakes = initSnakes();
        directions = initDirections();     

        while (true){

            long start = System.currentTimeMillis();

            snakes = updateSnakes(in, snakes, gameGrid);            
            gameGrid.update(snakes);

            int maxFloodFill = 0;
            int maxTerritory = 0;
            String bestMove = "UP";

            for(String strDirection : directions.keySet()){

                int rowNext = snakes[myID].getRow() + directions.get(strDirection)[0];
                int colNext = snakes[myID].getCol() + directions.get(strDirection)[1];

                Grid simGrid = gameGrid.clone();
                Snake[] simSnakes = snakes.clone();
                simSnakes[myID] = snakes[myID].clone().simUpdate(simGrid, rowNext, colNext);
                simGrid.update(simSnakes);

                if(simSnakes[myID] != null){

                    int simTerritory = simGrid.getTerritory(simSnakes[myID]);
                    int floodFillCount = simGrid.floodFillCount(rowNext, colNext);
                
                    if(floodFillCount > maxFloodFill){
                        maxTerritory = Math.max(simTerritory, maxTerritory);
                        maxFloodFill = floodFillCount;                        
                        bestMove = strDirection;
                    }
                    else if(floodFillCount == maxFloodFill && simTerritory > maxTerritory){
                        maxTerritory = simTerritory;
                        bestMove = strDirection;
                    }
                }         
            }
            
            printTimer(start);
            gameGrid.Print();
            
            System.out.println(bestMove);
        }
    }

    public static void main_Simulation(){

        Scanner in = new Scanner(System.in);

        Grid gameGrid = new Grid();
        Snake[] snakes = initSnakes();
        directions = initDirections();     

        while (true){

            long start = System.currentTimeMillis();

            snakes = updateSnakes(in, snakes, gameGrid);            
            gameGrid.update(snakes);

            String bestMove = "LEFT";
            int bestScore = -1;

            for(String move : directions.keySet()){

                Simulation newSim = new Simulation(gameGrid, move, snakes);
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

    private static void printTimer(long start){
        System.err.printf("update : %s ms\n", System.currentTimeMillis() - start);        
    }

    // CLASSES

    public static class Simulation{

        private Snake[] m_snakes;
        private Grid m_grid;
        private long m_start;
        private long m_timer;
        private String m_firstMove;

        public Simulation(Grid grid, String firstMove, Snake[] snakes){
            m_snakes = snakes;
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

            int score = -2;

            score = endGame();
            if(score < 0){
                return -1;
            }

            for(int i = 0; i < PLAYS_COUNT; i++){
                score = Math.max(score, endGame());
            }

            m_timer = System.currentTimeMillis() - m_start;   

            return score;
        }

        public boolean isGameOver(){
            
            int livingSnakes = 0;

            for(Snake snake : m_snakes){
                if(snake != null){
                    livingSnakes += 1;
                }
            }
            
            return livingSnakes == 1 || m_snakes[myID] == null;
        }

        private int endGame(){

            Snake[] simSnakes = m_snakes.clone();
            Grid simGrid = m_grid.clone();

            for(int i = 0; i < nbOfPlayer; i++){
                if(simSnakes[i] != null){
                    simSnakes[i] = simSnakes[i].clone();
                }
            }

            playFirstTurn(simGrid, simSnakes);

            if(simSnakes[myID] == null){
                return - 1;
            }
            int index = 0;
            /* System.err.println("simIndex = " + index);
            simGrid.Print(); */
            while(simSnakes[myID] != null && !this.isGameOver() && index < 10){
                playOneTurn(simGrid, simSnakes);
                index++;
            }
            /* System.err.println("simIndex = " + index);
            simGrid.Print(); */

            if(m_snakes[myID] == null){
                return m_grid.floodFillCount(m_snakes[myID].getRow() ,m_snakes[myID].getCol());
            }
            else{
                return -11;
            }
        }

        private void playFirstTurn(Grid grid, Snake[] snakes){

            for(int i = 0; i < nbOfPlayer; i++){

                if(snakes[i] != null){

                    if(i == myID){    

                        int rowNext = snakes[i].row + directions.get(m_firstMove)[0];
                        int colNext = snakes[i].col + directions.get(m_firstMove)[1];                        
                        
                        snakes[i] = snakes[i].simUpdate(grid, rowNext, colNext);
                    }
                    else{
                        playSmart(grid, snakes, i);
                    }
                }
            }            

        }

        private void playOneTurn(Grid grid, Snake[] snakes){

            for(int i = 0; i < nbOfPlayer; i++){
                if(snakes[i] != null){
                    playSmart(grid, snakes, i);
                }
            } 
        }

        private void playSmart(Grid grid, Snake[] snakes, int playerID){

            int maxFloodFill = 0;
            int maxTerritory = 0;
            int[] bestDirection = new int[]{0, 1};

            for(int[] direction : directions.values()){

                int rowNext = snakes[playerID].getRow() + direction[0];
                int colNext = snakes[playerID].getCol() + direction[1];

                if(grid.canGoto(rowNext, colNext)){

                    Grid simGrid = grid.clone();
                    Snake[] simSnakes = snakes.clone();

                    simSnakes[playerID] = snakes[playerID].clone().simUpdate(simGrid, rowNext, colNext);
                    simGrid.update(simSnakes);

                    if(simSnakes[playerID] != null){

                        int floodFillCount = simGrid.floodFillCount(rowNext, colNext);
                    
                        if(floodFillCount > maxFloodFill){
                            maxFloodFill = floodFillCount;                        
                            bestDirection = direction;
                        }
                    }  
                }       
            }

            int rowNext = snakes[playerID].getRow() + bestDirection[0];
            int colNext = snakes[playerID].getCol() + bestDirection[1];
        
            snakes[playerID] = snakes[playerID].simUpdate(m_grid, rowNext, colNext);
            m_grid.update(snakes);
        }
    }    

    public static class Snake{

        private int ID;
        private int startRow;
        private int startCol;
        private int row;
        private int col;

        public List<int[]> coords;
        
        public Snake(int mID){
            coords = new ArrayList<int[]>();
            ID = mID;
        }

        public Snake clone(){

            Snake clonedSnake = new Snake(this.ID);
            
            clonedSnake.coords = new ArrayList<>(this.coords);
            clonedSnake.startRow = this.startRow;
            clonedSnake.startCol = this.startCol;
            clonedSnake.row = this.row;
            clonedSnake.col = this.col;

            return clonedSnake;
        }

        public int getID(){
            return this.ID;
        }

        public int getRow(){
            return this.row;
        }

        public int getCol(){
            return this.col;
        }

        public void kill(Grid grid){
            for(int[] coord : coords){
                grid.setBlank(coord[0], coord[1]);
            }           
        }

        public Snake update(Scanner in, Grid grid){

            startCol = in.nextInt();    // starting X coordinate of Snake (or -1)
            startRow = in.nextInt();    // starting Y coordinate of Snake (or -1)
            col = in.nextInt();         // starting X coordinate of Snake (can be the same as X0 if you play before this player)
            row = in.nextInt();         // starting Y coordinate of Snake (can be the same as Y0 if you play before this player)

            if(grid.canGoto(row, col)){
                coords.add(new int[]{row, col});
                grid.setPlayer(this); 
            }else{
                this.kill(grid);
                return null;
            }

            return this;
        }

        public Snake simUpdate(Grid grid, int nextRow, int nextCol){

            col = nextCol;
            row = nextRow;

            if(grid.canGoto(row, col)){
                coords.add(new int[]{row, col});
                grid.setPlayer(this); 
            }else{
                this.kill(grid);
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
        private int[] territories;

        static final int GRID_WIDTH = 30;
        static final int GRID_HEIGHT = 20;

        // CONSTRUCTORS
        
        public Grid(){
            this.arrayGrid = new String[GRID_HEIGHT][GRID_WIDTH];
        }

        public Grid clone(){

            Grid clonedGrid = new Grid();
            clonedGrid.arrayGrid = this.arrayGrid.clone();

            for (int i = 0; i < GRID_HEIGHT; i++){
                clonedGrid.arrayGrid[i] = this.arrayGrid[i].clone();
            }

            return clonedGrid;
        }

        public Grid setPlayer(Snake snake){
            arrayGrid[snake.getRow()][snake.getCol()] = ".";
            return this;
        }

        public Grid setBlank(int row, int col){
            arrayGrid[row][col] = "";
            return this;
        }

        public boolean canGoto(int row, int col){

            boolean isReachable = false;

            if(this.isValid(row, col)){
                return this.arrayGrid[row][col] != ".";
            }

            return isReachable;
        }

        public boolean isValid(int row, int col){
            return row >= 0 && row < GRID_HEIGHT && col >= 0 && col < GRID_WIDTH;
        }

        public void update(Snake[] snakes){

            this.territories = new int[]{0, 0, 0, 0};

            for(int row = 0; row < GRID_HEIGHT; row++){

                for(int col = 0; col < GRID_WIDTH; col++){    

                    if(this.arrayGrid[row][col] != "."){
                        int closerSnakeID = getCloserSnake(row, col, snakes);
                        if(closerSnakeID > -1){
                            this.arrayGrid[row][col] = Integer.toString(closerSnakeID);             
                            this.territories[closerSnakeID] += 1;                            
                        }
                    }
                }
            }
        }

        public void Print(){
            for(int i = 0; i < arrayGrid.length; i++){
                System.err.println(String.join(" ", arrayGrid[i]));
            }
        }

        public int getTerritory(Snake snake){
            return this.territories[snake.getID()];
        }

        private int getCloserSnake(int row, int col, Snake[] snakes){

            int[] snakesDistances = new int[4];
            int minDistance = 9999;
            int closerSnakeID = -1;

            for(int i = 0; i < nbOfPlayer; i++){

                if(snakes[i] != null){

                    snakesDistances[i] = this.getDistanceBetween(snakes[i].getRow(), snakes[i].getCol(), row, col);
            
                    if(snakesDistances[i] < minDistance){
                        minDistance = snakesDistances[i];
                        closerSnakeID = i;
                    }
                }
            }

            return closerSnakeID;
        }        

        private int getDistanceBetween(int startRow, int startCol, int destRow, int destCol){

            return Math.abs(startRow - destRow) + Math.abs(startCol - destCol);

            /* List<Border> borders = new ArrayList<>();
            boolean[][] investigatePoints = new boolean[GRID_HEIGHT][GRID_WIDTH];
            Border bestBorder = null;
            boolean pathFound = false;
    
            borders.add(new Border(startRow, startCol));
            investigatePoints[startRow][startCol] = true;
    
            while(borders.size() > 0 && !pathFound){

                List<Border> bordersTemp = new ArrayList<Border>(borders);
    
                for(Border border : bordersTemp){

                    borders.remove(borders.indexOf(border));
    
                    for(int[] direction : directions.values()){
    
                        int nextRow = border.row + direction[0];
                        int nextCol = border.col + direction[1];
    
                        if(this.canGoto(nextRow, nextCol)){
    
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
    
            return bestBorder != null ? bestBorder.TotalDist() : 9999; */
        }

        public int floodFillCount(int row, int col){

            List<int[]> borders = new ArrayList<int[]>();
            boolean[][] visitedPoints = new boolean[20][30];
    
            borders.add(new int[]{row, col});
    
            int bordersCount = 1;
    
            while(borders.size() > 0){
    
                List<int[]> bordersTemp = new ArrayList<int[]>(borders);
    
                for(int[] border : bordersTemp){
    
                    borders.remove(borders.indexOf(border));
    
                    for(int[] direction : directions.values()){
    
                        int borderRow = border[0] + direction[0];
                        int borderCol = border[1] + direction[1];
        
                        if(this.canGoto(borderRow, borderCol)){
    
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
    }

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

    public static class Node{

        private Snake[] m_snakes;
        private Grid m_grid;

        public Node(Grid grid, Snake[] snakes){
            m_grid = grid;
            m_snakes = snakes;
        }

        public boolean isGameOver(){
            
            int livingSnakes = 0;

            for(Snake snake : m_snakes){
                if(snake != null){
                    livingSnakes += 1;
                }
            }
            
            return livingSnakes == 1 || m_snakes[myID] == null;
        }

        public int getScore(){
            return m_grid.getTerritory(m_snakes[myID]) + 2 * m_grid.floodFillCount(m_snakes[myID].getRow(), m_snakes[myID].getCol());
        }

        public List<Node> getChilds(int playerID){

            List<Node> childs = new ArrayList<>();
            Snake[] simSnakes = m_snakes.clone();

            simSnakes[playerID] = simSnakes[playerID].clone();

            if(m_snakes[playerID] != null){

                for(int[] direction : directions.values()){

                    Grid simGrid = m_grid.clone();

                    int rowNext = simSnakes[playerID].getRow() + direction[0];
                    int colNext = simSnakes[playerID].getCol() + direction[1];

                    if(simGrid.canGoto(rowNext, colNext)){

                        simSnakes[playerID] = simSnakes[playerID].simUpdate(simGrid, rowNext, colNext);
                        simGrid.update(simSnakes);
    
                        childs.add(new Node(simGrid, simSnakes));
                    }
                }                
            }

            return childs;
        }
    
    }

    static int minimax(Node node, int depth, int playerID){

        if(depth == 0 || node.isGameOver()){
            return node.getScore();
        }

        boolean maximizingPlayer = playerID == myID; 

        if (maximizingPlayer){
            int maxEval = Integer.MIN_VALUE;
            for(Node child : node.getChilds(playerID)){
                int eval = minimax(child, depth - 1, (playerID + 1) % nbOfPlayer);
                maxEval = Math.max(eval, maxEval);
            }
            return maxEval;
        }
        else{
            int minEval = Integer.MAX_VALUE;   
            for(Node child : node.getChilds(playerID)){
                int eval = minimax(child, depth - 1, (playerID + 1) % nbOfPlayer);
                minEval = Math.min(eval, minEval);
            }
            return minEval;
        }
    }

    // PATHFINDING FUNCTIONS


    // PARSING FUNCTIONS

    private static Snake[] initSnakes(){

        Snake[] snakes = new Snake[4];

        snakes[0] = new Snake(0);
        snakes[1] = new Snake(1);
        snakes[2] = new Snake(2);
        snakes[3] = new Snake(3);

        return snakes;
    }

    private static Snake[] updateSnakes(Scanner in, Snake[] snakes, Grid grid){

        nbOfPlayer = in.nextInt();      // total number of players (2 to 4).
        myID = in.nextInt();            // your player number (0 to 3).

        for(int i = 0; i < nbOfPlayer; i++){

            if(snakes[i] != null){
                snakes[i] = snakes[i].update(in, grid);
            }
            else{
                in.nextInt();
                in.nextInt();
                in.nextInt();
                in.nextInt();
            }            
        }

        for(int i = nbOfPlayer; i < 4; i++){
            snakes[i] = null;
        }        

        return snakes;
    }    

    private static Map<String, int[]> initDirections(){

        directions = new HashMap<String, int[]>();

        directions.put("RIGHT", new int[]{ 0 ,  1});
        directions.put("DOWN",  new int[]{ 1 ,  0});
        directions.put("LEFT",  new int[]{ 0 , -1});
        directions.put("UP",    new int[]{-1 ,  0});        

        return directions;
    }

    private static void printSnakes(Snake[] snakes, Grid grid){
        for(int i = 0; i < nbOfPlayer; i++){
            if(snakes[i] != null){
                System.err.printf("snake %02d : %s (%s /100)\n", i, grid.getTerritory(snakes[i]), 100 * grid.getTerritory(snakes[i]) / (20*30));
            }
        }        
    }
}