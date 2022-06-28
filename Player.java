import java.util.*;

/* IMPROVE LIST

    # Voir on peut détecter la map par symétrie
    # implémenter du pathfinding
 
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

    static List<int[]> pointsCoords;

    static boolean moveUp;
    static boolean moveDown;
    static boolean moveRight;
    static boolean moveLeft;

    static int myRow;
    static int myCol;
    static int possibleDir;
    static int gameTurn;

    // DATA STORAGE

    static Map<String, int[]> directions;    
    static List<String> lastMoves;
    static List<String> commentaries;

    static boolean[][] visitedPoints;
    static boolean[][] myPath;
    static String[][] map;

    public static void main(String args[]) {
        
        Scanner in = new Scanner(System.in); 
        
        PARSE_INITIALIZE(in);

        while (true) {

            map = new String[mapWidth][mapHeight];

            pointsCoords = new ArrayList<int[]>();
            commentaries = new ArrayList<String>();
            
            PARSE_UPDATE(in);

            PRINT_INPUTS();
            PRINT_MAP();

            MOVE();

            gameTurn++;
        }
    }

    public static void MOVE(){

        if(moveUp && canIMove("UP")){
            MOVE_UP();
        }else if(moveRight && canIMove("RH")){
            MOVE_RIGHT();
        }else if(moveDown && canIMove("DW")){
            MOVE_DOWN();
        }else if(moveLeft && canIMove("LH")){
            MOVE_LEFT();
        }else{
            WAIT();
        }        
    }

    // GENERIC FUNCTIONS

    public static int[] GetSymetricalPoint(int row, int col){


        return null;
    }

    public static boolean canIMove(String direction){

        int[] dir = directions.get(direction);

        int nextRow = myRow + dir[0];
        int nextCol = myCol + dir[1];

        return !myPath[nextRow][nextCol] || possibleDir == 1;
        //return lastMoves.size() > 0 ? lastMoves.get(lastMoves.size() - 1) != direction : true;
    }

    public static int RandBetween(int minVal, int maxVal){
        return minVal + (int)(Math.random() * ((maxVal - minVal) + 1));        
    }

    // PARSING FUNCTIONS

    public static void PARSE_INITIALIZE(Scanner in){

        directions = new HashMap<String, int[]>();
        lastMoves = new ArrayList<String>();

        directions.put("RH", new int[] { 0 , 1 });
        directions.put("UP", new int[] {-1 , 0 });
        directions.put("LH", new int[] { 0 ,-1 });
        directions.put("DW", new int[] { 1 , 0 });        

        mapWidth = in.nextInt() + 1;
        mapHeight = in.nextInt() + 1;
        pointsCount = in.nextInt();

        visitedPoints = new boolean[mapWidth][mapHeight];
        myPath = new boolean[mapWidth][mapHeight];

        if (in.hasNextLine()) {
            in.nextLine();
        }        
    }

    public static void PARSE_UPDATE(Scanner in){

        

        for (int i = 0; i < map.length; i++){

            for (int j = 0; j < map[0].length; j++){

                map[i][j] = visitedPoints[i][j] ? "." : " ";                
            }
        }

        input_1 = in.nextLine();
        input_2 = in.nextLine();
        input_3 = in.nextLine();
        input_4 = in.nextLine();

        moveUp =    input_1.equals("_");        
        moveRight = input_2.equals("_");  
        moveDown =  input_3.equals("_");              
        moveLeft =  input_4.equals("_"); 

        commentaries.add(String.format("DW : %s, %s\n", moveDown, canIMove("DW")));
        commentaries.add(String.format("RH : %s, %s\n", moveRight, canIMove("RH")));
        commentaries.add(String.format("UP : %s, %s\n", moveUp, canIMove("UP")));
        commentaries.add(String.format("LF : %s, %s\n", moveLeft, canIMove("LH")));

        for (int i = 0; i < pointsCount; i++) {

            int col = in.nextInt();
            int row = in.nextInt();

            pointsCoords.add(new int[] {row, col});

            String mPoint = i < 4 ? "!" : "@";

            map[row][col] = mPoint;
            visitedPoints[row][col] = true;
        
        }in.nextLine();

        myRow = pointsCoords.get(4)[0];
        myCol = pointsCoords.get(4)[1];

        PARSE_POSSIBLE_MOVES();          

        myPath[myRow][myCol] = true;
    }

    public static void PARSE_POSSIBLE_MOVES(){

        possibleDir = 0;

        if(moveUp){
            
            int nextRow = myRow + directions.get("UP")[0];
            int nextCol = myCol + directions.get("UP")[1];

            map[nextRow][nextCol] = ".";
            visitedPoints[nextRow][nextCol] = true;
            possibleDir++;
        }

        if(moveDown){
            
            int nextRow = myRow + directions.get("DW")[0];
            int nextCol = myCol + directions.get("DW")[1];

            map[nextRow][nextCol] = ".";
            visitedPoints[nextRow][nextCol] = true;
            possibleDir++;
        }

        if(moveRight){
            
            int nextRow = myRow + directions.get("RH")[0];
            int nextCol = myCol + directions.get("RH")[1];

            map[nextRow][nextCol] = ".";
            visitedPoints[nextRow][nextCol] = true;
            possibleDir++;
        }

        if(moveLeft){
            
            int nextRow = myRow + directions.get("LH")[0];
            int nextCol = myCol + directions.get("LH")[1];

            map[nextRow][nextCol] = ".";
            visitedPoints[nextRow][nextCol] = true;
            possibleDir++;
        }         
        
    }

    // PRINTING FUNCTIONS

    public static void PRINT_INPUTS(){

        commentaries.add(String.format("INI : %s %s %s", mapWidth, mapHeight, pointsCount));
        commentaries.add(String.format("INP : %s %s %s %s", input_1, input_2, input_3, input_4));

        for(int i = 0; i < pointsCoords.size(); i++){

            int[] arr = pointsCoords.get(i);
            commentaries.add(String.format("%02d : [%02d, %02d]", i, arr[0], arr[1]));
        }

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

        // CONSTRUCTORS

        public Border(int pRow, int pCol, String mAction){
            
            row = pRow;
            col = pCol;
            action = mAction;
            parentList = new ArrayList<Border>();
            
        }

        public Border(int pRow, int pCol, String mAction, Border mParent){

            row = pRow;
            col = pCol;
            action = mAction;
            parentList = new ArrayList<Border>(mParent.parentList);                
            parentList.add(mParent);
        }

        public Border(Border mBorder){
            row = mBorder.row;
            col = mBorder.col;
            action = mBorder.action;
            parentList = new ArrayList<Border>(mBorder.parentList);
        }

        public Border clone(){
            return new Border(this);
        }

        // METHODS

        public String FirstAction(){
            return parentList.get(1).action;
        }

        public int TotalDist(){
            return parentList.size();
        }
    }
    
    static boolean IsPointOnMap(int row, int col){
        return col >= 0 && col < mapWidth && row >= 0 && row < mapHeight ? true : false;
    }

    static boolean FindShortPath(int myRow, int myCol, int destRow, int destCol){

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(myRow, myCol, null, null));

        boolean[][] investigatePoints = new boolean[mapWidth][mapHeight];

        investigatePoints[myRow][myCol] = true;

        Border bestBorder = null;
        boolean pathFound = false;

        while(borders.size()>0 && !pathFound){

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));                

                for(String strDir : directions.keySet()){

                    int[] dirCoord = directions.get(strDir);

                    int nextRow = border.row + dirCoord[0];
                    int nextCol = border.col + dirCoord[1];

                    if(IsPointOnMap(nextRow, nextCol)){

                        String currentChar = map[nextRow][nextCol];

                        if(!investigatePoints[nextRow][nextCol] && currentChar != "#"){

                            Border mNewBorder = new Border(nextRow, nextCol, strDir, border);

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

        if(Math.abs(myRow - destRow) + Math.abs(myCol - destCol) == 1){
            
            //Move(heroX, heroY, destX, destY);
            
        }else if(bestBorder != null){
            
            System.out.println(bestBorder.FirstAction());
            return true;
        }
        
        return false;
    }

    static boolean ExploreNewRoad(int myRow, int myCol){

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(myRow, myCol, null, null));

        boolean[][] investigatePoints = new boolean[mapWidth][mapHeight];

        investigatePoints[myRow][myCol] = true;

        Border bestBorder = null;

        while(borders.size()>0 && bestBorder == null){

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            int minBorderDist = mapWidth * mapHeight;

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));                

                for(String strDir : directions.keySet()){

                    int[] dirCoord = directions.get(strDir);

                    int nextRow = border.row + dirCoord[0];
                    int nextCol = border.col + dirCoord[1];

                    if(IsPointOnMap(nextRow, nextCol)){

                        String currentChar = map[nextRow][nextCol];

                        if(!investigatePoints[nextRow][nextCol] && currentChar != "#"){

                            Border mNewBorder = new Border(nextRow, nextCol, strDir, border);

                            int borderDist = mNewBorder.TotalDist();

                            if(borderDist < minBorderDist && currentChar == " "){
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

        if(bestBorder != null){
            System.out.println(bestBorder.FirstAction());
            return false;
        }else{
            return true;
        }
    }

    // MOVE FUNCTIONS

    public static void MOVE_RIGHT(){
        lastMoves.add("RH");
        System.out.println("A");
        System.err.println("MOVE RIGHT");
    }

    public static void MOVE_LEFT(){
        lastMoves.add("LH");
        System.out.println("E");
        System.err.println("MOVE LEFT");
    }
    
    public static void MOVE_UP(){
        lastMoves.add("UP");
        System.out.println("C");
        System.err.println("MOVE UP");
    }
    
    public static void MOVE_DOWN(){
        lastMoves.add("DW");
        System.out.println("D");
        System.err.println("MOVE DOWN");
    }
    
    public static void WAIT(){
        System.out.println("B");
        System.err.println("WAIT");
    }
}