import java.util.*;

/* IMPROVE LIST

    # implémenter la simultation de la partie en utilisant un algorithme de recherche en profondeur
    # implémenter la simulation du déplacement des ennemis dans la fonction de recherche de loot
*/

class Player {

    // BASIC INPUTS

    static int mapWidth;
    static int mapHeight;
    static int pointsCount;

    static String input_1;
    static String input_2;
    static String input_3;
    static String input_4;

    // ADVANCED INPUTS

    static List<int[]> ennemyCoords;         // a list of int[2] arrays, each containing the row and col an enemy at the actual gameTurn
    static List<int[]> lastEnnemyCoords;          // a list of int[2] arrays, each containing the row and col an enemy at the last turn

    static boolean moveUp;                  // a boolean indicating if the player is able to move up
    static boolean moveDown;                // a boolean indicating if the player is able to move down
    static boolean moveRight;               // a boolean indicating if the player is able to move right
    static boolean moveLeft;                // a boolean indicating if the player is able to move left

    static int myRow;                       // the row of the player
    static int myCol;                       // the col of the player
    static int possibleDir;                 // the number of possible directions the player can move in the actual gameTurn
    static int gameTurn;                    // the count of game turns since the beginning of the game
    static int lootCount;                   // the count of loot found since the beginning of the game

    // DATA STORAGE

    static Map<String, String> traductions; // a hashmap containing the traductions of the differents move keyWord, in order to make the program more readable
    static Map<String, int[]> directions;   // a hashmap containing a 2D vector of the possible directions of the player, in order to make the program more readable
    static List<String> comments;           // a list of strings, allowing to store comments during the game, it will be printed at the end of the game
    static List<String> mortalMoves;        // a list of all moves wich can lead to a game over

    static boolean[][] walkablePoints;      // a 2D array of booleans, indicating if a point is walkable or not, at each game turn, we set it true if my player or an ennemy is on it
    static boolean[][] lootedPoints;        // a 2D array of booleans, indicating if a point is looted or not, at each game turn, we set it true if my player is on it
    static boolean[][] myPath;              // old variable, used to store the path of the player, and avoid to return to the same point in the MOVE() function
    static String[][] map;                  // a 2D array of strings, containing the map of the game, at each game turn, we refresh it with the infomrations received in the standard inputs

    // MAP DRAWING CONSTS
    
    static final int HEIGHT_PRINT = 20;     // a constant used to define the height of the map

    static final String MAP_BORDER = "#";   // a constant used to define the border of the map
    static final String MAP_UNKOWN = " ";   // a constant used to define the unknown point of the map
    static final String MAP_MY_POS = "@";   // a constant used to define the position of the player
    static final String MAP_ENNEMY = "!";   // a constant used to define the position of the ennemy
    static final String MAP_LOOT   = "*";   // a constant used to define the position of the loot
    static final String MAP_LOOTED = ".";   // a constant used to define the position of the looted points
    static final String MAP_TARGET = "+";   // a constant used to define the position of the point where my player is trying to go
    static final String MAP_MORTAL = "X";   // a constant used to defin the position of the mortal points, where my player could want to go

    public static void main(String args[]){
        
        Scanner in = new Scanner(System.in); 
        
        INITIALIZE(in);        

        while (true){

            UPDATE(in);            

            Border lootBorder = FindLoot(myRow, myCol);  

            MOVE_BORDER(lootBorder);           

            PRINT_COMMENTS();
            PRINT_MAP();

            gameTurn++;
        }
    }

    // GENERIC FUNCTIONS

    public static String GetEnnemyDirection(int ennemyIndex){

        String direction = ""; // 

        if(lastEnnemyCoords == null){
            
            return direction;
        
        }else{

            int lastRow = lastEnnemyCoords.get(ennemyIndex)[0];
            int lastCol = lastEnnemyCoords.get(ennemyIndex)[1];
    
            int currRow = ennemyCoords.get(ennemyIndex)[0];
            int currCol = ennemyCoords.get(ennemyIndex)[1];
    
            direction = GetDirection(lastRow, lastCol, currRow, currCol);
    
            return direction;            

        }
    }    

    public static String GetDirection(int lastRow, int lastCol, int currRow, int currCol){

        String direction = "";

        if(lastRow > currRow){
            direction = traduce("U");
        }
        else if(lastRow < currRow){
            direction = traduce("D");
        }
        else if(lastCol > currCol){
            direction = traduce("L");
        }
        else if(lastCol < currCol){
            direction = traduce("R");
        }

        return direction;        
    }

    public static String GetOppositeDirection(String direction){        

        String oppositeDirection = "";
        
        if(direction.equals("U")){
            oppositeDirection = "D";
        }else if(direction.equals("D")){
            oppositeDirection = "U";
        }else if(direction.equals("L")){
            oppositeDirection = "R";
        }else if(direction.equals("R")){
            oppositeDirection = "L";
        }
        
        return oppositeDirection;
    } 

    public static String traduce(String keyWord){
        return traductions.containsKey(keyWord) ? traductions.get(keyWord) : keyWord;
    }

    public static boolean canIMove(String direction, boolean resetPath, int distanceCriteria){

        if(resetPath) myPath = new boolean[mapHeight][mapWidth];

        int[] nextPoint = GetNextPoint(myRow, myCol, direction);

        int nextRow = nextPoint[0];
        int nextCol = nextPoint[1];

        int minDist = GetMinEnnemyDistance(nextRow, nextCol);        

        if(!IsPointOnMap(nextRow, nextCol) || minDist <= distanceCriteria){
            return false;
        }else{
            return !myPath[nextRow][nextCol] || possibleDir == 1;
        }      
    }

    public static boolean IsPointOnMap(int mRow, int mCol){
        return mRow >= 0 && mRow < mapHeight && mCol >= 0 && mCol < mapWidth;
    }

    public static int GetMinEnnemyDistance(int row, int col){

        int minDist = Integer.MAX_VALUE;

        for(int i = 0; i < ennemyCoords.size() - 1; i++){

            int rowEnnemy = ennemyCoords.get(i)[0];
            int colEnnemy = ennemyCoords.get(i)[1];

            minDist = Math.min(minDist, GetDistance(row, col, rowEnnemy, colEnnemy));
        }

        return minDist;
    }

    public static int RandBetween(int minVal, int maxVal){
        return minVal + (int)(Math.random() * ((maxVal - minVal) + 1));        
    }

    public static int GetDistance(int row_A, int col_A, int row_B, int col_B){
        return Math.abs(row_A - row_B) + Math.abs(col_A - col_B);        
    }

    public static int GetEnnemyRow(int index){
        
        if(ennemyCoords == null){
            return - 1;
        }else{
            return ennemyCoords.get(index)[0];
        }        
    }

    public static int GetEnnemyCol(int index){
        
        if(ennemyCoords == null){
            return - 1;
        }else{
            return ennemyCoords.get(index)[1];
        }        
    }    

    public static int CountEnnemyOnCol(int col, List<int[]> ennemyCoord){

        int count = 0;

        for (int i = 0; i < ennemyCoord.size(); i++){

            if(ennemyCoord.get(i)[1] == col){
                count++;
            }
        }

        return count;
    }

    public static int CountEnnemyOnRow(int row, List<int[]> ennemyCoord){

        int count = 0;

        for (int i = 0; i < ennemyCoord.size() - 1; i++){

            if(ennemyCoord.get(i)[0] == row){
                count++;
            }
        }

        return count;
    }

    // STATIC PARSING

    public static void INITIALIZE(Scanner in){

        // STATIC INSTANCIATIONS

        comments = new ArrayList<String>();
        directions = new HashMap<String, int[]>();
        traductions = new HashMap<String, String>();

        // FILLING DIRECTIONS COORDS

        directions.put("A", new int[] { 0 , 1 });
        directions.put("C", new int[] {-1 , 0 });
        directions.put("E", new int[] { 0 ,-1 });
        directions.put("D", new int[] { 1 , 0 });        

        // FILLING TRADUCING MAP

        traductions.put("R", "A");
        traductions.put("U", "C");
        traductions.put("D", "D");
        traductions.put("L", "E");

        traductions.put("A", "R");
        traductions.put("C", "U");
        traductions.put("D", "D");
        traductions.put("E", "L");

        // READING STATIC INPUTS
        
        mapHeight = in.nextInt();
        mapWidth = in.nextInt();
        pointsCount = in.nextInt();

        // STATIC ARRAYS INSTANCIATIONS

        map = new String [mapHeight][mapWidth];
        myPath = new boolean[mapHeight][mapWidth];
        lootedPoints = new boolean[mapHeight][mapWidth];
        walkablePoints = new boolean[mapHeight][mapWidth];         

        INITIALIZE_MAP();

        if (in.hasNextLine()) {
            in.nextLine();
        }        
    }

    public static void INITIALIZE_MAP(){

        for (int i = 0; i < mapHeight; i++){

            for (int j = 0; j < mapWidth; j++){

                if(i == 0 || j == 0 || i == mapHeight - 1 || j == mapWidth -1){
                    
                    SetMap(i, j, MAP_BORDER);
                    
                }else{

                    SetMap(i, j, MAP_UNKOWN);
                }                                        
            }
        }        
    }    

    // UPDATE PARSING

    public static void UPDATE(Scanner in){

        comments = new ArrayList<String>();

        UPDATE_MOVES(in);        
        UPDATE_PLAYERS(in);
        SCAN_MOVES();

        UPDATE_MORTAL_MOVES();        
        UPDATE_MAP();
        //DRAW_MORTAL_MOVES();
        DRAW_PLAYERS();

        myPath[myRow][myCol] = true;
    }

    public static void UPDATE_MOVES(Scanner in){

        input_1 = in.nextLine();
        input_2 = in.nextLine();
        input_3 = in.nextLine();
        input_4 = in.nextLine();

        //comments.add(String.format("INP : %s %s %s %s\n", input_1, input_2, input_3, input_4));        
    }

    public static void UPDATE_PLAYERS(Scanner in){
        
        lastEnnemyCoords = ennemyCoords != null ? new ArrayList<int[]>(ennemyCoords) : null;
        ennemyCoords = new ArrayList<int[]>();

        for (int i = 0; i < pointsCount; i++) {

            int col = in.nextInt();
            int row = in.nextInt();

            if(i < 4){

                ennemyCoords.add(new int[] {row, col});

            }else{

                myRow = row;
                myCol = col;
                lootedPoints[myRow][myCol] = true;
            }

            SetPoint_Walkable(row, col);        

        }in.nextLine();          
    }

    public static void SCAN_MOVES(){

        moveUp =    input_1.equals("_");        
        moveRight = input_2.equals("_");  
        moveDown =  input_3.equals("_");              
        moveLeft =  input_4.equals("_");  
        
        possibleDir = 0;

        possibleDir += SCAN(moveUp,     traduce("U"));
        possibleDir += SCAN(moveDown,   traduce("D"));
        possibleDir += SCAN(moveLeft,   traduce("L"));
        possibleDir += SCAN(moveRight,  traduce("R"));        
        
    } 

    public static int SCAN(boolean canMove, String direction){

        int addPossibleDir = 0;

        int[] nextPoint = GetNextPoint(myRow, myCol, direction);

        int nextRow = nextPoint[0];
        int nextCol = nextPoint[1];

        //System.err.printf("next = [%s, %s]\n", nextRow, nextCol);

        if(canMove){
            SetPoint_Walkable(nextRow, nextCol);              
            addPossibleDir++;        
        }

        return addPossibleDir;        
    }

    public static void UPDATE_MAP(){

        lootCount = 0;

        for (int i = 0; i < mapHeight; i++){

            for (int j = 0; j < mapWidth; j++){

                boolean isWalkable = walkablePoints[i][j];
                boolean isLooted = lootedPoints[i][j];

                if(isLooted){

                    SetMap(i, j, MAP_LOOTED);

                }else if(isWalkable){

                    SetMap(i, j, MAP_LOOT);
                }
            }
        }
    }

    public static void DRAW_MORTAL_MOVES() {

        for (int i = 0; i < mortalMoves.size(); i++) {
            
            String move = mortalMoves.get(i);
            int[] coord = GetNextPoint(myRow, myCol, move);
            
            SetMap(coord[0], coord[1], MAP_MORTAL);
        }
    }

    public static void DRAW_PLAYERS(){

        for (int i = 0; i < ennemyCoords.size(); i++) {

            int row = ennemyCoords.get(i)[0];
            int col = ennemyCoords.get(i)[1];

            SetMap(row, col, Integer.toString(i));
        }

        SetMap(myRow, myCol, MAP_MY_POS);
    }

    // ARRAYS SETTING FUNCTIONS

    public static void SetMap(int row, int col, String mChar) {
        map[row][col] = mChar;
    }

    public static void SetPoint_Walkable(int row, int col){

        boolean hSym = mapHeight % 2 == 0 || row == 0 || row == mapHeight - 1;
        boolean vSym = mapWidth  % 2 == 0 || col == 0 || col == mapWidth - 1;

        walkablePoints[row][col] = true;

        if(hSym && vSym){

            int[] aSymPoint = Get_axisSym(row, col);
            walkablePoints[aSymPoint[0]][aSymPoint[1]] = true;

        }else if(hSym){

            int[] hSymPoint = Get_hSym(row, col);
            walkablePoints[hSymPoint[0]][hSymPoint[1]] = true;

        }else if(vSym){

            int[] vSymPoint = Get_vSym(row, col);
            walkablePoints[vSymPoint[0]][vSymPoint[1]] = true;
        }
    }

    public static void SetPoint_Looted(int row, int col){

        lootedPoints[row][col] = true;
        lootCount++;        
    }    

   // SYMMETRY FUNCTIONS

   public static int[] GetNextPoint(int startRow, int startCol, String direction){

        // in some map, we can cross some of the map borders, in this case the next position corresponds to the opposite border
        // this function allows to get the coordinates of a point according to the direction

        int nextRow = startRow + directions.get(direction)[0];
        int nextCol = startCol + directions.get(direction)[1];

        if(nextRow < 0){
            nextRow = mapHeight + nextRow;
        }else if(nextRow >= mapHeight){
            nextRow = nextRow - mapHeight;
        }

        if(nextCol < 0){
            nextCol = mapWidth + nextCol;
        }else if(nextCol >= mapWidth){            
            nextCol = nextCol - mapWidth;
        }

        return new int[] {nextRow, nextCol};          
   }

   public static int[] Get_vSym(int row, int col){
        // this functions returns the coordinates of the point that corresponds to the vertical symmetry of the point (row, col)
        return new int[]{row, mapWidth - col -1};        
    }

   public static int[] Get_hSym(int row, int col){
        // this function returns the coordinates of the point that corresponds to the horizontal symmetry of the point (row, col)
        return new int[]{mapHeight - row - 1, col};
    }

   public static int[] Get_axisSym(int row, int col){
        return new int[]{Get_hSym(row, col)[0], Get_vSym(row, col)[1]};        
    }    

    // PATHFINDING FUNCTIONS

    public static class Border{

        // this class represents a border of exploration points, it is used to find the path to the loot

        private String action;
        private List<Border> parentList;
        private int row;
        private int col;

        public Border(int mRow, int mCol, String mAction, Border mParent){
            
            row = mRow;
            col = mCol;
            action = mAction;

            if(mParent == null){
                parentList = new ArrayList<Border>();
                parentList.add(this);
            }else{
                parentList = new ArrayList<Border>(mParent.parentList);               
                parentList.add(this);
            }            
        }

        public String FirstAction(){
            return parentList.get(1).action;
        }

        public int TotalDist(){
            return parentList.size();
        }
    
        public String toString(){
            
            String[] parentCoords = new String[parentList.size()];
            
            for(Border parent : parentList){
                parentCoords[parent.TotalDist() - 1] = String.format("[%s, %s]", parent.row, parent.col);
            }

            return String.join(" -> ", parentCoords);
        }
    }

    public static Border FindLoot(int startRow, int startCol){

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(startRow, startCol, null, null));

        boolean[][] investigatePoints = new boolean[mapHeight][mapWidth];

        investigatePoints[startRow][startCol] = true;

        Border bestBorder = null;

        while(borders.size() > 0 && bestBorder == null){

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));

                for(String direction : directions.keySet()){

                    int[] nextPoint = GetNextPoint(border.row, border.col, direction);

                    int nextRow = nextPoint[0];
                    int nextCol = nextPoint[1];

                    int minDist = GetMinEnnemyDistance(nextRow, nextCol);

                    //Border worstEnnemyPath = GetCloserEnnemyPath(nextRow, nextCol);
                    //boolean isDangerous = worstEnnemyPath == null ? false : worstEnnemyPath.TotalDist() <= border.TotalDist();

                    if(IsPointOnMap(nextRow, nextCol) && minDist > 0){ //&& !isDangerous){                        

                        boolean isWalkable = walkablePoints[nextRow][nextCol];
                        boolean isLooted = lootedPoints[nextRow][nextCol];
                        boolean mortalMove = mortalMoves.contains(direction);

                        if(!investigatePoints[nextRow][nextCol] && isWalkable && !mortalMove){

                            Border mNewBorder = new Border(nextRow, nextCol, direction, border);

                            if(!isLooted){
                                bestBorder = mNewBorder;
                            }
                            
                            borders.add(mNewBorder);
                        }

                        investigatePoints[nextRow][nextCol] = true;
                    }
                }
            }
        }

        return bestBorder;
    }

    public static List<int[]> simulateNextEnnemyPositions(int nbTurns){

        // we simulate the positions of all the ennemies after nbTurns, and we return a list of int[], where each int[] is the next position of the ennemy

        List<int[]> simActualCoords = new ArrayList<int[]>(ennemyCoords);
        List<int[]> simLastCoords = new ArrayList<int[]>(lastEnnemyCoords);

        for(int i = 0; i < nbTurns; i++){

            for(int j = 0; j < simActualCoords.size(); j++){

                int[] ennemyCoord = simActualCoords.get(j);
                //int[] nextCoord = GetNextEnnemyPosition(ennemyIndex)

                //simActualCoords.set(j, nextCoord);
            }

            simLastCoords = new ArrayList<int[]>(simActualCoords);            
        }
        
        return null;
    }    

    public static int[] GetNextEnnemyPosition(int ennemyRow, int ennemyCol, int lastEnnemyRow, int lastEnnemyCol){

        int minDistanceFromMyPlayer = Integer.MAX_VALUE;

        int[] nextPosition = new int[]{ennemyRow, ennemyCol};

        String actualDirection = GetDirection(ennemyRow, ennemyCol, lastEnnemyRow, lastEnnemyCol);

        for(String direction : directions.keySet()){

            int[] nextPoint = GetNextPoint(ennemyRow, ennemyCol, direction);
            
            int nextRow = nextPoint[0];
            int nextCol = nextPoint[1];
            
            int distanceFromMyPlayer = GetDistance(nextRow, nextCol, myRow, myCol);

            boolean sameDirection = direction.equals(actualDirection);            
            boolean oppositeDirection = direction.equals(GetOppositeDirection(actualDirection));

            boolean closerDistance = distanceFromMyPlayer < minDistanceFromMyPlayer;
            boolean sameDistance = distanceFromMyPlayer == minDistanceFromMyPlayer && sameDirection;

            if(walkablePoints[nextRow][nextCol] && !oppositeDirection && closerDistance || sameDistance){
                nextPosition = nextPoint;
                minDistanceFromMyPlayer = distanceFromMyPlayer;
            }
        }
        
        return nextPosition;
    }    

    public static Border FindShortPath(int startRow, int startCol, int destRow, int destCol) {

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(startRow, startCol, null, null));

        boolean[][] investigatePoints = new boolean[mapHeight][mapWidth];

        investigatePoints[startRow][startCol] = true;

        Border bestBorder = null;

        while(borders.size() > 0 && bestBorder == null){

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));

                for(String strDir : directions.keySet()){

                    int[] nextPoint = GetNextPoint(border.row, border.col, strDir);

                    int nextRow = nextPoint[0];
                    int nextCol = nextPoint[1];

                    if(IsPointOnMap(nextRow, nextCol)){

                        boolean isWalkable = walkablePoints[nextRow][nextCol];

                        if(!investigatePoints[nextRow][nextCol] && isWalkable){

                            Border mNewBorder = new Border(nextRow, nextCol, strDir, border);

                            if(nextRow == destRow && nextCol == destCol){
                                bestBorder = mNewBorder;
                            }
                            
                            borders.add(mNewBorder);                            
                        }

                        investigatePoints[nextRow][nextCol] = true;
                    }
                }
            }
        }

        return bestBorder;
    }

    public static Border GetCloserEnnemyPath(int destRow, int destCol){

        Border closerEnnemy = null;

        int minDist = Integer.MAX_VALUE;

        for (int i = 0; i < ennemyCoords.size(); i++){

            int ennemyRow = ennemyCoords.get(i)[0];
            int ennemyCol = ennemyCoords.get(i)[1];

            Border ennemyPath = FindShortPath(ennemyRow, ennemyCol, destRow, destCol);

            if(ennemyPath != null){
                if(ennemyPath.TotalDist() <= minDist) closerEnnemy = ennemyPath;            
            }
        }

        return closerEnnemy;
    }

    /* MORTAL MOVES EXAMPLES

        // this is an exemple of the representation of the map, every single character represents a particular point, which is described by a constant begining with MAP_...
        // our player is represented by the '@' symbol, and the enemies by the '!' symbol
        // the loot is represented by the '*' symbol
        // the point wher we can move are represented by the '.' symbol
        // the points we are not able to say if we can walk on, are represented by the ' ' symbol

        # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
        # .         *     .                 *     *         * # 0
        # . . . . . .     . . . .     * * * *     * * * * * * # 1
        #           .           .     *           *           # 2
        #           .           .     *           *           # 3
        #           .     . . . . * * * * * *     *           # 4
        #           .     .       * *       *     *           # 5
        #           .     .   * * * * * *   *     *           # 6
        #           .     .   *         *   *     *           # 7
        #           .     .   * * * * * *   *     *           # 8
        #           .     .       * *       *     *           # 9
        #           .     . * * * * ! * * + @     *           # 10
        #           .     .                 .     *           # 11
        #           .     .                 !     *           # 12
        # . . . . . . . . . . . .     . . . . * * * * * * * * # 13
        # .         .           .     !           *         * # 14
        # .         .           .     .           *         * # 15
        # . . .     . . . . . . . ! . . * * * * * *     * * * # 16
        #     .     .     .                 *     *     *     # 17
        #     .     .     .                 *     *     *     # 18
        # . . . . . .     . . . .     * * * *     * * * * * * # 19
        # # # # # # # # # # # # # # # # # # # # # # # # # # # # 

        // on the map above, we can see that the player is at the position (10, 18) (represented by the '@' symbol) and that he is trying to move to the position (10, 17) (represented by the '+' symbol)
        // the map below correspond to the same map above, but at the next gameTurn, the player will be at the position (10, 17) and will try to move to the position (10, 16)
        // the map below correspond the the above map, but after a move up of my player

        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        # .         *     .                 *     *         * #
        # . . . . . .     . . . .     * * * *     * * * * * * #
        #           .           .     *           *           #
        #           .           .     *           *           #
        #           .     . . . . * * * * * *     *           #
        #           .     .       * *       *     *           #
        #           .     .   * * * * * *   *     *           #
        #           .     .   *         *   *     *           #
        #           .     .   * * * * * *   *     *           #
        #           .     .       * *       *     *           #
        #           .     . * * * * * ! + @ .     *           #
        #           .     .                 !     *           #
        #           .     .                 .     *           #
        # . . . . . . . . . . . .     ! . . . * * * * * * * * #
        # .         .           .     .           *         * #
        # .         .           .     .           *         * #
        # . . .     . . . . . . . . ! . * * * * * *     * * * #
        #     .     .     .                 *     *     *     #
        #     .     .     .                 *     *     *     #
        # . . . . . .     . . . .     * * * *     * * * * * * #
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #

        // the 6 maps below correspond to a zoom of the map above, of 3 rows of height        
        
        // GOOD MOVE EXAMPLE

        // the 3 examples below correspond to a case where the player avoid a move wich could have lead to a game over

        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        #           .     .       * *       +     *           #
        #           .     . * * * * * ! * @ .     *           #
        #           .     .                 !     *           #
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        #           .     .       * *       +     *           #
        #           .     . * * * * * ! * . @     *           #
        #           .     .                 !     *           #
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        #           .     .       * *       @     *           #
        #           .     . * * * * * * ! . !     *           #
        #           .     .                 .     *           #
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #        

        // BAD MOVE EXAMPLE

        // the 3 examples below corresponds to a case where the player is stuck between two ennemies, and he can't move anywhere. this lead to a game over and we want to avoid that

        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        #           .     .       * *       *     *           #
        #           .     . * * * * * ! + @ .     *           #
        #           .     .                 !     *           #
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        #           .     .       * *       +     *           #
        #           .     . * * * * * ! @ . .     *           #
        #           .     .                 !     *           #
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
        #           .     .       * *       *     *           #
        #           .     . * * * * * * ! @ !     *           #
        #           .     .                 .     *           #
        # # # # # # # # # # # # # # # # # # # # # # # # # # # #
    */

    // MORTAL MOVES FUNCTIONS

    public static List<String> UPDATE_MORTAL_MOVES(){ // return a list of Strings (A, E, C, or D)

        mortalMoves = new ArrayList<String>();

        for(String direction : directions.keySet()){

            if(isMortalMove(direction)) mortalMoves.add(direction);
        }

        return mortalMoves;
    }

    public static boolean isMortalMove(String direction){

        List<int[]> nextEnnemyCoords = new ArrayList<int[]>();

        for (int i = 0; i < ennemyCoords.size(); i++){

            int[] myNextPoint = GetNextPoint(myRow, myCol, direction);
        
            int myNextRow = myNextPoint[0];
            int myNextCol = myNextPoint[1];            

            int ennemyRow = GetEnnemyRow(i);
            int ennemyCol = GetEnnemyCol(i);

            if(ennemyRow == myNextRow && ennemyCol == myNextCol){

                // if we move in that direction, we will be in the same place as the ennemy so we will die
                
                comments.add(String.format("%s [%s, %s]: ennemyPoint", traduce(direction), myNextRow, myNextCol));
                return true;
            
            }else{

                int[] nextEnnemyCoord = GetNextEnnemyPosition(i);

                ennemyRow = nextEnnemyCoord[0];
                ennemyCol = nextEnnemyCoord[1];

                if(ennemyRow == myNextRow && ennemyCol == myNextCol){  
                    
                    // if we move in that direction, we will be in the same place as the ennemy so we will die
                    
                    comments.add(String.format("%s [%s, %s]: ennemyPoint moving at nextTurn", traduce(direction), myNextRow, myNextCol));
                    return true;
                }else{
                    
                    // we create a list of ennemy coordinates wich will be used as startpoint to simulate the ennemy movements and see if we can escape from my actual position

                    nextEnnemyCoords.add(nextEnnemyCoord);
                }
            }
        }

        // if any return true, we simulate the ennemy movements and see if we can escape from my actual position
        
        boolean canRun = CanIRun(myRow, myCol, nextEnnemyCoords);

        comments.add(String.format("%s [%s, %s]: canRun = %s", traduce(direction), myRow, myCol, canRun));

        return !canRun;
    }

    public static boolean CanIRun(int startRow, int startCol, List<int[]> nextEnnemyCoords){

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(startRow, startCol, null, null));

        boolean[][] investigatePoints = new boolean[mapHeight][mapWidth];

        investigatePoints[startRow][startCol] = true;

        Border bestBorder = null;

        while(borders.size() > 0 && bestBorder == null){

            // at each turn, the duplicate nextEnnemyCoords, in order to keep the last movements of the ennemies and be able to calculate the ennemies directions

            List<int[]> lastEnnemyCoords = new ArrayList<int[]>(nextEnnemyCoords);

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));

                for(String strDir : directions.keySet()){

                    int[] myNextPoint = GetNextPoint(border.row, border.col, strDir);

                    int myNextRow = myNextPoint[0];
                    int myNextCol = myNextPoint[1];

                    if(IsPointOnMap(myNextRow, myNextCol)){    
                        
                        // we simulate the ennemy movements and see if there is an ennemy on my Next Position

                        nextEnnemyCoords = simulateNextEnnemyPositions(border.row, border.col, nextEnnemyCoords, lastEnnemyCoords);

                        boolean isWalkable = walkablePoints[myNextRow][myNextCol];
                        boolean isLooted = lootedPoints[myNextRow][myNextCol];  
                        boolean isEnnemy = isThereEnnemyAt(myNextRow, myNextCol, nextEnnemyCoords);

                        //if(startRow == 19 && startCol == 16) comments.add(String.format("ennemy at [%s, %S] = %s, to escape from [%s, %s]", myNextRow, myNextCol, isEnnemy, startRow, startCol));

                        if(!investigatePoints[myNextRow][myNextCol] && isWalkable && !isEnnemy){

                            Border mNewBorder = new Border(myNextRow, myNextCol, strDir, border);

                            if(!isLooted){
                                bestBorder = mNewBorder;
                            }
                            
                            borders.add(mNewBorder);                            
                        }

                        investigatePoints[myNextRow][myNextCol] = true;
                    }
                }
            }
        }

        // if we didn't find a border, it means there is no way to escape from the ennemies

        return bestBorder != null;
    }

    public static List<int[]> simulateNextEnnemyPositions(int myRow_, int myCol_, List<int[]> actualEnnemyCoords, List<int[]> lastEnnemyCoords){

        // we simulate the positions of all the ennemies, and we return a list of int[], where each int[] is the next position of the ennemy

        List<int[]> simulatedCoords = new ArrayList<int[]>();

        for (int i = 0; i < actualEnnemyCoords.size(); i++){            

            int minDistanceFromMe = Integer.MAX_VALUE;

            // we read the actual and last positions of the ennemy at the index i
            
            int lastEnnemyRow = lastEnnemyCoords.get(i)[0];
            int lastEnnemyCol = lastEnnemyCoords.get(i)[1];

            int ennemyRow = actualEnnemyCoords.get(i)[0];
            int ennemyCol = actualEnnemyCoords.get(i)[1];            
            
            int[] nextEnnemyCoord = new int[]{ennemyRow, ennemyCol};

            String ennemyDirection = GetDirection(lastEnnemyRow, lastEnnemyCol, ennemyRow, ennemyCol);

            for(String direction : directions.keySet()){

                // for each direction, we search the point where the ennemy will be the nearest of my player, and we check if it is walkable

                int[] nextEnnemyPosition = GetNextPoint(ennemyRow, ennemyCol, direction);
                
                int nextEnnemyRow = nextEnnemyPosition[0];
                int nextEnnemyCol = nextEnnemyPosition[1];
                
                if(walkablePoints[nextEnnemyRow][nextEnnemyCol]){

                    // if we can walk on that point, we check if it is the nearest of my player

                    int distanceFromMe = GetDistance(nextEnnemyRow, nextEnnemyCol, myRow_, myCol_);                

                    boolean closerDistance = distanceFromMe < minDistanceFromMe;
                    boolean followingDirection = direction.equals(ennemyDirection);
                    boolean sameDistance = distanceFromMe == minDistanceFromMe && followingDirection;
                    
                    // if it is the nearest of my player, or if the ennemy is following the same direction and it is the same distance as the previous ennemy, we keep it

                    if(closerDistance || sameDistance){
                        nextEnnemyCoord = nextEnnemyPosition;   
                        minDistanceFromMe = distanceFromMe;
                    }
                }
            }
            
            // if we found a point, we add it to the list of simulated ennemies positions

            simulatedCoords.add(nextEnnemyCoord);
        }
        
        return simulatedCoords;
    }
    
    public static int[] GetNextEnnemyPosition(int ennemyIndex){

        int minDistance = Integer.MAX_VALUE;

        int[] nextPosition = ennemyCoords.get(ennemyIndex);
  
        int ennemyRow = GetEnnemyRow(ennemyIndex);
        int ennemyCol = GetEnnemyCol(ennemyIndex);

        for(String direction : directions.keySet()){

            int[] nextPoint = GetNextPoint(ennemyRow, ennemyCol, direction);
            
            int nextRow = nextPoint[0];
            int nextCol = nextPoint[1];
            
            int distance = GetDistance(nextRow, nextCol, myRow, myCol);

            boolean closerDistance = distance < minDistance;
            boolean followingDirection = direction.equals(GetEnnemyDirection(ennemyIndex));
            boolean sameDistance = distance == minDistance && followingDirection && minDistance == Integer.MAX_VALUE;

            if(walkablePoints[nextRow][nextCol] && closerDistance || sameDistance){
                nextPosition = nextPoint;   
                minDistance = distance;
            }
        }
        
        return nextPosition;
    }    

    public static boolean isThereEnnemyAt(int row, int col, List<int[]> ennemyCoords){

        for(int[] ennemyCoord : ennemyCoords){
            
            int ennemyRow = ennemyCoord[0];
            int ennemyCol = ennemyCoord[1];

            if(ennemyRow == row && ennemyCol == col){
                return true;
            }
        }   

        return false;        
    }

    // MOVE FUNCTIONS

    public static void MOVE_BORDER(Border mBorder){

        if(mBorder != null){
                
            // we draw the target point on the map to see where we are going

            SetMap(mBorder.row, mBorder.col, MAP_TARGET);

            // we write the action in the standards output

            String strMove = mBorder.FirstAction();
            System.out.println(strMove);

            // we add comments to the move description to make it easier to understand

            for (int i = 0; i < ennemyCoords.size() - 1; i++) {
                comments.add(String.format("ENNEMY [%02d] : %s [%s, %s] -> %s", i, traduce(GetEnnemyDirection(i)), ennemyCoords.get(i)[0], ennemyCoords.get(i)[1], Arrays.toString(GetNextEnnemyPosition(i))));
            }

            comments.add(String.format("\n%s = MOVE BORDER : %s", traduce(strMove), mBorder.toString()));

        }else{
            MOVE();
        }
    }

    public static void MOVE(){

        boolean moveFound = false;
        boolean anyMovePossible = false;

        int minDist = 1;
        int cycles = 0;

        String strComment = Integer.toString(GetMinEnnemyDistance(myRow, myCol)); //String.format(" UP = %s, DW = %s, RH = %s, LH = %s", moveUp, moveDown, moveRight, moveLeft);

        while(!moveFound){

            if(moveLeft && canIMove(traduce("L"), anyMovePossible, minDist)){
                MOVE_LEFT(strComment);
                moveFound = true;
            }else if(moveUp && canIMove(traduce("U"), anyMovePossible, minDist)){
                MOVE_UP(strComment);
                moveFound = true;
            }else if(moveDown && canIMove(traduce("D"), anyMovePossible, minDist)){
                MOVE_DOWN(strComment);
                moveFound = true;
            }else if(moveRight && canIMove(traduce("R"), anyMovePossible, minDist)){
                MOVE_RIGHT(strComment);
                moveFound = true;
            }else if(cycles > 5){
                MOVE_RANDOM();
                moveFound = true;
            }

            if(anyMovePossible) minDist = 1;
            anyMovePossible = true;
            cycles++;
        }
    }

    public static void MOVE_RANDOM(){
        
        List<String> letters = new ArrayList<String>();

        if(moveUp)      letters.add("C");
        if(moveDown)    letters.add("D");
        if(moveLeft)    letters.add("E");
        if(moveRight)   letters.add("A");
        
        String move = letters.get(RandBetween(0, letters.size() - 1));
        System.out.println(move);
        System.err.println("MOVE RANDOM : " + move);
    }

    public static void MOVE_RIGHT(String comment){
        System.out.println("A");
        System.err.println("MOVE RIGHT " + comment);
    }

    public static void MOVE_LEFT(String comment){
        System.out.println("E");
        System.err.println("MOVE LEFT " + comment);
    }
    
    public static void MOVE_UP(String comment){
        System.out.println("C");
        System.err.println("MOVE UP " + comment);
    }
    
    public static void MOVE_DOWN(String comment){
        System.out.println("D");
        System.err.println("MOVE DOWN " + comment);
    }
    
    public static void WAIT(){
        System.out.println("B");
        System.err.println("WAIT");
    }

    // PRINTING FUNCTIONS

    public static void PRINT_COMMENTS(){

        //System.err.printf("INI : %s %s %s\n", mapWidth, mapHeight, pointsCount);
        System.err.printf("my position = [%s, %s]\n\n", myRow, myCol);

        for(String comment : comments){
            System.err.println(comment);
        }        
    }

    public static void PRINT_MAP(){

        int printHeigth = HEIGHT_PRINT;

        int startRow = myRow < printHeigth / 2 ? 0 : myRow - printHeigth / 2;
        int endRow = myRow > (mapHeight - printHeigth / 2) ? mapHeight : myRow + printHeigth / 2;

        for(int i = startRow; i < endRow; i++){
            System.err.println(String.join(" ", map[i]));
        }
    }
    
}