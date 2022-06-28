import java.util.*;

/* IMPROVE LIST
     
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

    static List<int[]> playersCoords;

    static boolean moveUp;
    static boolean moveDown;
    static boolean moveRight;
    static boolean moveLeft;

    static int myRow;
    static int myCol;
    static int possibleDir;
    static int gameTurn;
    static int lootCount;

    // DATA STORAGE

    static Map<String, int[]> directions;  
    static Map<String, int[]> strMoves;
    static List<String> commentaries;

    static boolean[][] lootedPoints;
    static boolean[][] visitedPoints;
    static boolean[][] myPath;
    static String[][] map;

    // MAP DRAWING CONSTS

    static final String MAP_BORDER = "#";
    static final String MAP_WALL   = " ";
    static final String MAP_UNKOWN = " ";
    static final String MAP_MY_POS = "@";
    static final String MAP_ENNEMY = "!";
    static final String MAP_LOOT   = "*";
    static final String MAP_LOOTED = ".";

    public static void main(String args[]){
        
        Scanner in = new Scanner(System.in); 
        
        PARSE_INITIALIZE(in);        

        while (true){

            PARSE_UPDATE(in);
            PRINT_MAP(); 

            Border lootBorder = FindLoot(myRow, myCol);  

            MOVE_BORDER(lootBorder);           

            gameTurn++;
        }
    }

    public static void MOVE_TEMP(){

        boolean moveFound = false;

        while(!moveFound){

            if(moveRight){
                System.out.println("A");
                moveFound = true;
            }else if(moveDown){
                System.out.println("D");
                moveFound = true;
            }else if(moveUp){
                System.out.println("C");
                moveFound = true;
            }else if(moveLeft){
                System.out.println("E");
                moveFound = true;
            }else{
                MOVE_RANDOM();
                moveFound = true;
            }
        }       
    }    

    public static void MOVE(){

        boolean moveFound = false;
        boolean anyMovePossible = false;

        int minDist = 5;
        int cycles = 0;

        while(!moveFound){

            if(moveRight && canIMove("RH", anyMovePossible, minDist)){
                MOVE_RIGHT();
                moveFound = true;
            }else if(moveUp && canIMove("UP", anyMovePossible, minDist)){
                MOVE_UP();
                moveFound = true;
            }else if(moveDown && canIMove("DW", anyMovePossible, minDist)){
                MOVE_DOWN();
                moveFound = true;
            }else if(moveLeft && canIMove("LH", anyMovePossible, minDist)){
                MOVE_LEFT();
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

    // GENERIC FUNCTIONS

    public static boolean canIMove(String direction, boolean resetPath, int distanceCriteria){

        if(resetPath) myPath = new boolean[mapHeight][mapWidth];

        int[] dir = directions.get(direction);

        int nextRow = myRow + dir[0];
        int nextCol = myCol + dir[1];

        int minDist = GetMinEnnemyDistance(nextRow, nextCol);        

        if(!IsPointOnMap(nextRow, nextCol) || minDist <= distanceCriteria){
            return false;
        }else{
            return !myPath[nextRow][nextCol] || possibleDir == 1;
        }      
    }

    public static int GetMinEnnemyDistance(int row, int col){

        int minDist = Integer.MAX_VALUE;

        for(int i = 0; i < playersCoords.size() - 2; i++){

            int rowEnnemy = playersCoords.get(i)[0];
            int colEnnemy = playersCoords.get(i)[1];

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

    static boolean IsPointOnMap(int mRow, int mCol){
        return mRow >= 0 && mRow < mapHeight && mCol >= 0 && mCol < mapWidth;
    }

    // PARSING FUNCTIONS

    public static void PARSE_MAP_INIT(){

        for (int i = 0; i < mapHeight; i++){

            for (int j = 0; j < mapWidth; j++){

                if(i == 0 || j == 0 || i == mapHeight - 1 || j == mapWidth -1){
                    
                    map[i][j] = MAP_BORDER;
                    visitedPoints[i][j] = true;
                    
                }else{

                    map[i][j] = MAP_UNKOWN;
                }                                        
            }
        }        
    }    

    public static void PARSE_INITIALIZE(Scanner in){

        commentaries = new ArrayList<String>();
        directions = new HashMap<String, int[]>();
        strMoves = new HashMap<String, int[]>();       

        directions.put("RH", new int[] { 0 , 1 });
        directions.put("UP", new int[] {-1 , 0 });
        directions.put("LH", new int[] { 0 ,-1 });
        directions.put("DW", new int[] { 1 , 0 });

        strMoves.put("A", directions.get("RH").clone());
        strMoves.put("C", directions.get("UP").clone());
        strMoves.put("E", directions.get("LH").clone());
        strMoves.put("D", directions.get("DW").clone());         
        
        mapHeight = in.nextInt();
        mapWidth = in.nextInt();
        pointsCount = in.nextInt();

        map =           new String [mapHeight][mapWidth];
        myPath =        new boolean[mapHeight][mapWidth];
        visitedPoints = new boolean[mapHeight][mapWidth];
        lootedPoints =  new boolean[mapHeight][mapWidth];        

        PARSE_MAP_INIT();

        if (in.hasNextLine()) {
            in.nextLine();
        }        
    }

    public static void PARSE_UPDATE(Scanner in){

        commentaries = new ArrayList<String>();

        PARSE_MOVES(in);        
        PARSE_PLAYERS(in);
        SCAN_MOVES();      
        
        UPDATE_MAP();
        ADD_PLAYERS();

        myPath[myRow][myCol] = true;
    }

    public static void UPDATE_MAP(){

        lootCount = 0;

        for (int i = 0; i < mapHeight; i++){

            for (int j = 0; j < mapWidth; j++){

                boolean isWall = map[i][j].equals(MAP_WALL);
                boolean isBorder = map[i][j].equals(MAP_BORDER);
                boolean isLooted = lootedPoints[i][j];
                boolean isVisited = visitedPoints[i][j];

                if(!isBorder && !isWall && isVisited && !isLooted){

                    VisitPoint(i, j, MAP_LOOT, true, "UPDATE_MAP (LOOT)");
                }

                if(isLooted) Lootpoint(i, j);
            }
        }
    }

    public static void PARSE_PLAYERS(Scanner in){

        playersCoords = new ArrayList<int[]>();

        for (int i = 0; i < pointsCount; i++) {

            int col = in.nextInt(); //TODO : essayer d'inverser l'ordre des deux pour voir ce que ça fait
            int row = in.nextInt();

            playersCoords.add(new int[] {row, col});

        }in.nextLine();

        myRow = playersCoords.get(4)[0];
        myCol = playersCoords.get(4)[1];

        Lootpoint(myRow, myCol);
    }

    public static void PARSE_MOVES(Scanner in){

        input_1 = in.nextLine();
        input_2 = in.nextLine();
        input_3 = in.nextLine();
        input_4 = in.nextLine();

        commentaries.add(String.format("INP : %s %s %s %s\n", input_1, input_2, input_3, input_4));        
    }

    public static void SCAN_MOVES(){

        moveUp =    input_1.equals("_");        
        moveRight = input_2.equals("_");  
        moveDown =  input_3.equals("_");              
        moveLeft =  input_4.equals("_");  
        
        possibleDir = 0;

        possibleDir += SCAN(moveUp,     "UP");
        possibleDir += SCAN(moveDown,   "DW");
        possibleDir += SCAN(moveLeft,   "LH");
        possibleDir += SCAN(moveRight,  "RH");        
        
    } 

    public static void ADD_PLAYERS(){

        //playersCoords = new ArrayList<int[]>();

        for (int i = 0; i < playersCoords.size(); i++) {

            int row = playersCoords.get(i)[0];
            int col = playersCoords.get(i)[1];

            VisitPoint(row, col, i < 4 ? MAP_ENNEMY : MAP_MY_POS, false, "ADD_PLAYERS");        
        }        
    }

    public static int SCAN(boolean canMove, String direction){

        int addPossibleDir = 0;

        int nextRow = myRow + directions.get(direction)[0];
        int nextCol = myCol + directions.get(direction)[1];        

        if(canMove){

            boolean isLooted = lootedPoints[nextRow][nextCol];
            boolean isVisited = visitedPoints[nextRow][nextCol];

            if(!isVisited && !isLooted) VisitPoint(nextRow, nextCol, MAP_LOOT, true, "SCAN (" + direction + ")");            
            addPossibleDir++;
        
        }else if(!visitedPoints[nextRow][nextCol]){
            
            VisitPoint(nextRow, nextCol, MAP_WALL, true, "SCAN (" + direction + ")");
        }

        return addPossibleDir;        
    }

    // PRINTING FUNCTIONS

    public static void Lootpoint(int row, int col){

        visitedPoints[row][col] = true;
        lootedPoints[row][col] = true;

        map[row][col] = MAP_LOOTED;
        lootCount++;
    }

    public static void VisitPoint(int row, int col, String mChar, boolean applySymmetry, String parent){

        if(!IsPointOnMap(row, col)) return;

        if(applySymmetry) SymetricalVisit(row, col, mChar);

        map[row][col] = mChar;        
        visitedPoints[row][col] = true;
    }

    public static void SymetricalVisit(int row, int col, String mChar){

        boolean hSym = mapHeight % 2 == 0;
        boolean vSym = mapWidth  % 2 == 0;

        if(hSym && vSym){

            int symRow = HorizontalVisit(row, col, mChar);
            int symCol = VerticalVisit(row, col, mChar);
            map[symRow][symCol] = mChar;

            visitedPoints[symRow][symCol] = true;

        }else if(hSym){

            HorizontalVisit(row, col, mChar);

        }else if(vSym){

            VerticalVisit(row, col, mChar);
        }
    }

    public static int VerticalVisit(int row, int col, String mChar){

        int symCol = mapWidth - col - 1;

        map[row][symCol] = mChar;
        visitedPoints[row][symCol] = true;

        if(mChar.equals(MAP_LOOTED)) System.err.printf("symCol : [%s]\n", mChar);

        return symCol;        
    }

    public static int HorizontalVisit(int row, int col, String mChar){

        int symRow = mapHeight - row - 1;

        map[symRow][col] = mChar;
        visitedPoints[symRow][col] = true;

        if(mChar.equals(MAP_LOOTED)) System.err.printf("symRow : [%s]\n", mChar);

        return symRow;
    }

    public static void PRINT_INPUTS(){

        System.err.printf("INI : %s %s %s\n", mapWidth, mapHeight, pointsCount);

        for(String commentary : commentaries){
            System.err.println(commentary);
        }        
    }

    public static void PRINT_MAP(){

        for(int i = 0; i < map.length; i++){
            System.err.println(String.join(" ", map[i]));
        }
    }
    
    // PATHFINDING FUNCTIONS

    public static class Border{

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
            }else{
                parentList = new ArrayList<Border>(mParent.parentList);
                mParent.action = mAction;
                parentList.add(mParent);
            }            
        }

        public String FirstAction(){
            return parentList.get(0).action;
        }

        public int TotalDist(){
            return parentList.size();
        }
    }

    static Border FindLoot(int startRow, int startCol){

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(startRow, startCol, null, null));

        boolean[][] investigatePoints = new boolean[mapHeight][mapWidth];

        investigatePoints[startRow][startCol] = true;

        Border bestBorder = null;

        while(borders.size() > 0 && bestBorder == null){

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            int minBorderDist = mapWidth * mapHeight;

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));

                for(String strDir : strMoves.keySet()){

                    int[] dirCoord = strMoves.get(strDir);

                    int nextRow = border.row + dirCoord[0];
                    int nextCol = border.col + dirCoord[1];

                    if(IsPointOnMap(nextRow, nextCol)){

                        boolean isRoad = map[nextRow][nextCol].equals(MAP_LOOTED);
                        boolean isLooted = lootedPoints[nextRow][nextCol];

                        if(!investigatePoints[nextRow][nextCol] && (isRoad || isLooted)){

                            Border mNewBorder = new Border(nextRow, nextCol, strDir, border);

                            int borderDist = mNewBorder.TotalDist();

                            if(borderDist < minBorderDist && !isLooted){
                                minBorderDist = borderDist;
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

    // MOVE FUNCTIONS

    public static void MOVE_BORDER(Border mBorder){

        if(mBorder != null){                
                
            String strMove = mBorder.FirstAction();
            System.out.println(strMove);
    
            String traducedMove = "unroconized Move : " + strMove;
            String strCoords = String.format("[%s, %s]", mBorder.row, mBorder.col);
    
            switch(strMove){
    
                case "A":
                    traducedMove = "MOVE BORDER : RIGHT " + strCoords;
                    break;
                case "E":
                    traducedMove = "MOVE BORDER : LEFT  " + strCoords;
                    break;            
                case "C":
                    traducedMove = "MOVE BORDER : UP    " + strCoords;
                    break;            
                case "D":
                    traducedMove = "MOVE BORDER : DOWN  " + strCoords;
                    break;            
                default:
                    break;    
            }
    
            System.err.println(traducedMove);

        }else{
            MOVE();
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

    public static void MOVE_RIGHT(){
        System.out.println("A");
        System.err.println("MOVE RIGHT");
    }

    public static void MOVE_LEFT(){
        System.out.println("E");
        System.err.println("MOVE LEFT");
    }
    
    public static void MOVE_UP(){
        System.out.println("C");
        System.err.println("MOVE UP");
    }
    
    public static void MOVE_DOWN(){
        System.out.println("D");
        System.err.println("MOVE DOWN");
    }
    
    public static void WAIT(){
        System.out.println("B");
        System.err.println("WAIT");
    }
}