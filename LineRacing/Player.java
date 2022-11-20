/* package LineRacing;
import LineRacing.lineRacing.*; */

import java.security.KeyStore.Entry;
import java.util.*;

class Player {

    public static Map<String, int[]> directions;
    public static int nbOfPlayer;
    public static int myID;
    public static Random random;
    public static final int PLAYS_COUNT = 50;
    public static final boolean DEBUG_MODE = false;

    public static void main(String args[]) {

        main_Vanilla_Sim();
        //main_Minimax();
        //main_Simulation();
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

            String bestDirection = "UP";
            int maxSurface = 0;
            int minOppDist = 9999;

            for(String direction : directions.keySet()){

                int[] dirCoord = directions.get(direction);

                int nextRow = snakes[myID].getRow() + dirCoord[0];
                int nextCol = snakes[myID].getCol() + dirCoord[1];

                if(gameGrid.canGoto(nextRow, nextCol)){

                    int surface = GetDiffusedSurface(nextRow, nextCol, gameGrid.arrayGrid);
                    int oppDist = GetCloserOpponent(nextRow, nextCol, snakes, gameGrid);

                    System.err.printf("%s : surface = %s / oppDist : %s\n", direction, surface, oppDist);
                    System.err.printf("%s : surface = %s\n", direction, gameGrid.floodFillCount(nextRow, nextCol));
    
                    if(surface > maxSurface || (surface == maxSurface && oppDist < minOppDist)){
                        bestDirection = direction;
                        maxSurface = surface;
                        minOppDist = oppDist;
                    }
                }
            }
            printTimer(start);
            printLog(gameGrid, snakes);        

            System.out.println(bestDirection);            
        }
    }

    public static void main_Vanilla_Sim(){

        Scanner in = new Scanner(System.in);

        random = new Random();

        Grid gameGrid = new Grid();
        Snake[] snakes = initSnakes();
        directions = initDirections();     

        while (true){

            long start = System.currentTimeMillis();

            snakes = updateSnakes(in, snakes, gameGrid);

            String bestDirection = "Fuck !";
            int maxSurface = 0;
            int minOppDist = 9999;

            for(String direction : directions.keySet()){

                int[] dirCoord = directions.get(direction);

                int nextRow = snakes[myID].getRow() + dirCoord[0];
                int nextCol = snakes[myID].getCol() + dirCoord[1];

                if(gameGrid.canGoto(nextRow, nextCol)){

                    int surface = gameGrid.floodFillCount(nextRow, nextCol);
                    int oppDist = GetCloserOpponent(nextRow, nextCol, snakes, gameGrid);

                    Simulation sim = new Simulation(gameGrid.clone(), direction, snakes);
                    int simScore = sim.getScore();                                    

                    System.err.printf("%s : surface = %s, oppDist : %s, simScore = %s\n", direction, surface, oppDist, simScore);

                    if(simScore <= 0 && !bestDirection.equals("Fuck !")) continue;    
    
                    if(surface > maxSurface || (surface == maxSurface && oppDist < minOppDist)){
                        bestDirection = direction;
                        maxSurface = surface;
                        minOppDist = oppDist;
                    }
                }
                else{
                    System.err.printf("%s : Impossible\n", direction);
                }
            }
            
            printTimer(start);
            printLog(gameGrid, snakes);
            System.out.println(bestDirection);            
        }
    }

    public static void main_Vanilla_Plus(){

        Scanner in = new Scanner(System.in);

        Grid gameGrid = new Grid();
        Snake[] snakes = initSnakes();
        directions = initDirections();     

        while (true){

            long start = System.currentTimeMillis();

            snakes = updateSnakes(in, snakes, gameGrid);

            Map<String, Integer> evalMap = new HashMap<>();
            String bestDirection = "FUCK :'(";

            for(String direction : directions.keySet()){

                int[] dirCoord = directions.get(direction);

                int nextRow = snakes[myID].getRow() + dirCoord[0];
                int nextCol = snakes[myID].getCol() + dirCoord[1];

                if(gameGrid.canGoto(nextRow, nextCol)){

                    Snake[] simSnakes = snakes.clone();

                    simSnakes[myID] = simSnakes[myID].clone();
                    simSnakes[myID].row = nextRow;
                    simSnakes[myID].row = nextCol;

                    int voronoi = gameGrid.clone().update(simSnakes).getTerritory(myID);
                    int surface = gameGrid.floodFillCount(nextRow, nextCol);
                    int oppDist = GetCloserOpponent(nextRow, nextCol, snakes, gameGrid);

                    int eval = voronoi + surface;

                    evalMap.put(direction, eval);
                    
                }
            }

            int maxScore = 0;

            for(String direction : evalMap.keySet()){
                int eval = evalMap.get(direction);

                System.err.printf("%s : %s\n", direction, eval);
                
                if(eval > maxScore){
                    maxScore = eval;
                    bestDirection = direction;
                }
            }

            printTimer(start);
            printLog(gameGrid, snakes);        

            System.out.println(bestDirection);            
        }
    }

    public static void main_Simulation(){

        Scanner in = new Scanner(System.in);

        Grid gameGrid = new Grid();
        Snake[] snakes = initSnakes();
        directions = initDirections();   
        random = new Random();  

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

            printLog(gameGrid, snakes);

            System.out.println(bestMove);
        }        
    }

    private static void printLog(Grid grid, Snake[] snakes){
        grid.Print();
        System.err.printf("%s %s\n", myID, nbOfPlayer);
        printSnakes(snakes);        
    }

    public static void printTimer(long start){
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

            int score = endGame();
            if(score < 0){
                return -1;
            }

            for(int i = 0; i < PLAYS_COUNT; i++){
                score += endGame();
            }

            m_timer = System.currentTimeMillis() - m_start;   

            return score;
        }

        public boolean isGameOver(Snake[] snakes){
            
            int livingSnakes = 0;

            for(Snake snake : snakes){
                if(snake != null){
                    livingSnakes += 1;
                }
            }
            
            return livingSnakes == 1 || snakes[myID] == null;
        }

        public int endGame(){

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
            while(!this.isGameOver(simSnakes) && index < 200){
                playOneTurn(simGrid, simSnakes);
                if(DEBUG_MODE) System.err.printf("turn index = %s\n", index);
                index++;
            }

            return simSnakes[myID] == null ? 0 : 1;
        }

        private void playFirstTurn(Grid grid, Snake[] snakes){

            if(DEBUG_MODE) grid.Print();

            for(int i = 0; i < nbOfPlayer; i++){

                if(snakes[i] != null){

                    if(i == myID){    

                        int rowNext = snakes[i].row + directions.get(m_firstMove)[0];
                        int colNext = snakes[i].col + directions.get(m_firstMove)[1];                        
                        
                        snakes[i] = snakes[i].simUpdate(grid, rowNext, colNext);
                        //grid.update(snakes);
                    }
                    else{

                        if(snakes[myID] == null) break;

                        int minDist = 9999;
                        int[] nextDirection = new int[]{0, 1};;
                        for(int[] direction : snakes[i].getPossibleMove(grid)){
                            int dist = grid.getDistanceBetween(snakes[i].getRow(), snakes[i].getCol(), snakes[myID].getRow(), snakes[myID].getCol());
                            
                            if(dist < minDist){
                                minDist = dist;
                                nextDirection = direction;
                            }
                        }
    
                        snakes[i] = snakes[i].simUpdate(grid, snakes[i].row + nextDirection[0], snakes[i].col + nextDirection[1]);   
                    }
                }

                if(DEBUG_MODE) grid.Print();
            }            

        }
       
        private void playOneTurn(Grid grid, Snake[] snakes){

            for(int i = 0; i < nbOfPlayer; i++){

                if(snakes[i] != null){
                    if(i == myID){
                        int[] nextDirection = getRandomDirection(snakes[i], grid);                        
                        snakes[i] = snakes[i].simUpdate(grid, snakes[i].row + nextDirection[0], snakes[i].col + nextDirection[1]); 
                    }
                    else{

                        if(snakes[myID] != null){
                            int minDist = 9999;
                            int[] nextDirection = new int[]{0, 1};

                            for(int[] direction : snakes[i].getPossibleMove(grid)){
                                int dist = grid.getDistanceBetween(snakes[i].getRow(), snakes[i].getCol(), snakes[myID].getRow(), snakes[myID].getCol());
                                
                                if(dist < minDist){
                                    minDist = dist;
                                    nextDirection = direction;
                                }
                            }

                                    
                            snakes[i] = snakes[i].simUpdate(grid, snakes[i].row + nextDirection[0], snakes[i].col + nextDirection[1]); 
                        }
                        else{
                            int[] nextDirection = getRandomDirection(snakes[i], grid);                        
                            snakes[i] = snakes[i].simUpdate(grid, snakes[i].row + nextDirection[0], snakes[i].col + nextDirection[1]);                             
                        }
    

    
                    }                    
                }



 

                /* if(i == myID){    
                    playSmart(grid, snakes, myID);
                }
                else{
                    playRandom(grid, snakes[i]);
                } */
                

                if(DEBUG_MODE) grid.Print();
            }         
        }

        private void playRandom(Grid grid,Snake snake){
            int[] nextDirection = getRandomDirection(snake, grid);                        
            snake = snake.simUpdate(grid, snake.row + nextDirection[0], snake.col + nextDirection[1]);            
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
                    //simGrid.update(simSnakes);

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
        
            snakes[playerID] = snakes[playerID].simUpdate(grid, rowNext, colNext);
            grid.update(snakes);
        }
    }    

    public static class Snake{

        private int ID;
        private int startRow;
        private int startCol;
        private int row;
        private int col;

        public List<int[]> coords;

        //new Snake(i, snakeRow, snakeCol);
        
        public Snake(int id, int sRow, int sCol){
            coords = new ArrayList<>();
            ID = id;
            row = sRow;
            col = sCol;
            coords.add(new int[]{row, col});
        }

        public void addCoords(int sRow, int sCol){
            coords.add(new int[]{sRow, sCol});
        }

        public Snake(int mID){
            coords = new ArrayList<int[]>();
            ID = mID;
        }

        public Snake(int mID, int rowStart, int colStart, Grid grid){
            coords = new ArrayList<>();

            coords.add(new int[]{rowStart, colStart});
            coords.add(new int[]{row, col});

            ID = mID;
            this.startRow = rowStart;
            this.startCol = colStart;
            this.row = rowStart;
            this.col = colStart;
            grid.setPlayer(this);
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
    
        public List<int[]> getPossibleMove(Grid grid){
            
            List<int[]> possibleDir = new ArrayList<>();

            for(int[] d : directions.values()){
                if(grid.canGoto(this.row + d[0], this.col + d[1])){
                    possibleDir.add(d);
                }
            }
            return possibleDir;
        }
    }
    
    public static class Grid{

        private String[][] territoryGrid;
        public String[][] arrayGrid;
        private int[] territories;

        static final int GRID_WIDTH = 30;
        static final int GRID_HEIGHT = 20;

        public Grid(String[][] strGrid){
            this.arrayGrid = strGrid;
        }
        
        public Grid(){
            this.arrayGrid = new String[GRID_HEIGHT][GRID_WIDTH];

            for(int i = 0; i < GRID_HEIGHT; i++){
                for (int j = 0; j < GRID_WIDTH; j++) {
                    this.arrayGrid[i][j] = ".";
                }
            }
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
            arrayGrid[snake.getRow()][snake.getCol()] = Integer.toString(snake.getID()); //".";
            arrayGrid[snake.startRow][snake.startCol] = Integer.toString(snake.getID());
            return this;
        }

        public Grid setBlank(int row, int col){
            arrayGrid[row][col] = ".";
            return this;
        }

        public boolean canGoto(int row, int col){

            if(this.isValid(row, col)){
                return this.arrayGrid[row][col].equals(".");
            }

            return false;
        }

        public boolean isValid(int row, int col){
            return row >= 0 && row < GRID_HEIGHT && col >= 0 && col < GRID_WIDTH;
        }

        public Grid update(Snake[] snakes){

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

            return this;
        }

        public void Print(){

            //if(DEBUG_MODE)lineRacing.clearConsole();

            for(int i = 0; i < arrayGrid.length; i++){
                System.err.println(String.join(" ", arrayGrid[i]));
            }
        }

        public int getTerritory(int snakeID){
            return this.territories[snakeID];
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
    
            return bestBorder != null ? bestBorder.TotalDist() : 9998;*/
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
    
        public int GetCloserOpponentFrom(int row, int col, Snake[] snakes){

            int minDist = 9999;
    
            for(int i = 0; i < nbOfPlayer; i++){

                if(snakes[i] != null && i != myID){
                    int oppDist = this.getDistanceBetween(row, col, snakes[i].getRow(), snakes[i].getCol());    
                    minDist = Math.min(minDist, oppDist);
                }
            }
    
            return minDist;
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
            return 0;//m_grid.getTerritory(m_snakes[myID]) + 2 * m_grid.floodFillCount(m_snakes[myID].getRow(), m_snakes[myID].getCol());
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

    public static void initPlayer(int nbOfPlayers, int myPlayerID){
        nbOfPlayer = nbOfPlayers;
        myID = myPlayerID;        
        random = new Random();
        directions = initDirections();
    }    


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

    public static Map<String, int[]> initDirections(){

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
                System.err.printf("snake %02d : %s (%s /100)\n", i, grid.getTerritory(i), 100 * grid.getTerritory(i) / (20*30));
            }
        }        
    }

    private static void printSnakes(Snake[] snakes){
        for(int i = 0; i < nbOfPlayer; i++){
            if(snakes[i] != null){
                System.err.printf("%s,%s,%s\n", i, snakes[i].getRow(), snakes[i].getCol());
            }
        }        
    }    

    private static int randBetween(int min, int max){
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static int[] getRandomDirection(Snake snake, Grid grid){
        
        List<int[]> possibleDirections = snake.getPossibleMove(grid);

        if(possibleDirections.size() == 1){
            return possibleDirections.get(0); 
        }
        else if(possibleDirections.size() > 1){
            return possibleDirections.get(random.nextInt(possibleDirections.size())); 
        }
        else{
            return new int[]{0, 1};
        }        
       
    }

    // OLD FUNCTIONS

    static int GetMinPathLength(int startRow, int startCol, int destRow, int destCol, String[][] arrayGrid, Snake[] snakes){

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

                    if(IsValidForPathFinding(nextRow, nextCol, snakes, arrayGrid)){

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

    static int GetCloserOpponent(int row, int col, Snake[] snakes, Grid grid){

        int minDist = 9999;

        for(int i = 0; i < nbOfPlayer; i++){

            if(snakes[i] != null && i != myID){

                int oppDist = GetMinPathLength(row, col, snakes[i].getRow(), snakes[i].getCol(), grid.arrayGrid, snakes);

                //System.err.printf("%s : %s %s %s %s\n", i, row, col, snakes[i].getRow(), snakes[i].getCol());


                minDist = Math.min(minDist, oppDist);
            }
        }

        return minDist;
    }

    public static int GetDiffusedSurface(int row, int col, String[][] arrayGrid){

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
    
                    if(IsPositionValid(borderRow, borderCol, arrayGrid)){

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

    static boolean IsPositionValid(int row, int col, String[][] arrayGrid){

        boolean isValid = false;

        if(col >= 0 && col < 30 && row >=0 && row < 20){
            isValid = arrayGrid[row][col].equals(".");
        }
        return isValid;
    }
    
    static boolean IsValidForPathFinding(int row, int col, Snake[] snakes, String[][] arrayGrid){

        boolean isValid = false;

        for(int i = 0; i< nbOfPlayer; i++){

            if(snakes[i] != null && i != myID){

                if(row == snakes[i].getRow() && col == snakes[i].getCol()) return true;
            }

        }

        if(col >= 0 && col < 30 && row >=0 && row < 20){
            isValid = arrayGrid[row][col].equals(".");
        }
        return isValid;
    }    

    static int GetDistance(int row1, int col1, int row2, int col2){
        return Math.abs(row1-row2) + Math.abs(col1-col2);
    }


}