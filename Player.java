import java.util.*;

class Player {

    private static enum UnitType{
        CULTIST,
        CULTLEADER
    }

    private static enum TileType {
        FLOOR, 
        OBSTACLE,
        BORDER
    }

    private static final int[][] directions = {
        { 0,  1},
        { 0, -1},
        { 1,  0},
        {-1,  0},
    };

    private static Scanner in;
    private static Tile[][] tiles;

    private static int myId;    // 0 - you are the first player, 1 - you are the second player
    private static int mapWidth;   // Width of the board
    private static int mapHeight;  // Height of the board

    private static Map<Integer, Unit> allUnits;
    private static List<Unit> myUnits;
    private static List<Unit> ennemyUnits;
    private static List<Unit> neutralUnits;

    private static Unit myLeader;
    private static Unit ennemyLeader;    

    public static void main(String args[]) {

        in = new Scanner(System.in);

        INIT(in);

        // game loop
        while (true) {

            UPDATE(in);

            if(tryConvert() == true) continue;

            System.out.println("WAIT");
        }
    }

    private static boolean tryConvert(){

        List<Unit> unitsToConvert = myLeader.getUnitsToConvert();

        if(unitsToConvert.size() > 0){
            myLeader.CONVERT(unitsToConvert.get(0));
            return true;
        }

        return false;
    }

    private static void moveLeader(){

    }

    // CLASSES

    public static class Tile {

        protected final int m_col;
        protected final int m_row;
        private TileType type;
        private Unit unit;
    
        public Tile(int col, int row, TileType type) {
            this.m_col = col;
            this.m_row = row;
            this.type = type;
            this.unit = null;
        }
    
        public Unit getUnit() {
            return unit;
        }
    
        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public void removeUnit(){
            this.unit = null;
        }
    
        public int getRow() {
            return m_row;
        }
    
        public int getCol() {
            return m_col;
        }
    
        public TileType getType() {
            return type;
        }

        public String toString() {
            return String.format("Tile[%02d, %02d]", m_row, m_col);
        }
    }

    private static class Unit{

        private UnitType m_type;
        private int m_id;
        private int m_hp;
        private int m_row;
        private int m_col;
        private int m_ownerID;

        private boolean isInGame;
        private Tile m_tile;

        public Unit(int id, int hp, int row, int col, int owner, int type){

            this.m_id = id;
            this.m_hp = hp;
            this.m_row = row;
            this.m_col = col;
            this.m_ownerID = owner;
            this.m_type = UnitType.values()[type];

            this.m_tile = tiles[row][col];
            this.isInGame = true;
        }

        public int getRow(){return m_row;}
        public int getCol(){return m_col;}
        public int getHp(){return m_hp;}

        public UnitType getType(){return m_type;}
        
        public boolean isFriend(){return m_ownerID == myId;}
        public boolean isEnnemy(){return m_ownerID != myId && m_ownerID != 2;}
        public boolean isNeutral(){return m_ownerID == 2;}        

        public void move(int row, int col){

            this.m_row = row;
            this.m_col = col;

            this.m_tile.removeUnit();
            this.m_tile = tiles[row][col];
            this.m_tile.setUnit(this);            
        }

        private Unit getCloserUnit(List<Unit> units){

            int minDist = Integer.MAX_VALUE;

            for(Unit unit : units){

            }        

            return null;
        }        

        public List<Unit> getUnitsToConvert(){

            List<Unit> unitsToConvert = new ArrayList<Unit>();

            for(int[] dir : directions){

                Tile nextTile = tiles[m_row + dir[0]][m_col + dir[1]];

                if(nextTile.unit != null){
                    if(nextTile.unit.isNeutral()){
                        unitsToConvert.add(nextTile.unit);
                    }
                }
            }

            return unitsToConvert;
        }

        public void takeDamage(int damage) {
            m_hp = Math.max(0, m_hp - damage);
            isInGame = m_hp > 0;
        }        

        public void WAIT(){
            System.out.println("WAIT");
        }

        public void MOVE(int row, int col){
            System.out.printf("%s MOVE %s %s\n", this.m_id, col, row);
        }

        public void SHOOT(Unit unit){
            System.err.printf("%s SHOOT %s", this.m_id, unit.m_id);
        }

        public void CONVERT(Unit unit){
            System.err.printf("%s CONVERT %s", this.m_id, unit.m_id);
        }                
    
        public String toString(){
            return String.format("[%02d, %02d] %s : id = %02d, hp = %02d, type = %s", m_row, m_col, padStrOwner(m_ownerID), m_id, m_hp, m_type.toString());
        }
    
    }

    // PARSING FUNCTIONS

    private static void INIT(Scanner in){

        myId = in.nextInt(); 
        mapWidth = in.nextInt(); 
        mapHeight = in.nextInt();
        
        tiles = new Tile[mapHeight][mapWidth];

        for (int i = 0; i < mapHeight; i++) {

            String[] mapLine = in.next().split("");

            for (int j = 0; j < mapLine.length; j++){

                if(mapLine[j] == "x"){
                    tiles[i][j] = new Tile(j, i, TileType.OBSTACLE);
                }
                else{
                    tiles[i][j] = new Tile(j, i, TileType.FLOOR);
                }
            }
        }        
    }

    private static void UPDATE(Scanner in){

        myLeader = null;
        ennemyLeader = null; 

        myUnits = new ArrayList<Unit>();
        ennemyUnits = new ArrayList<Unit>();
        neutralUnits = new ArrayList<Unit>();
        allUnits = new HashMap<Integer, Unit>();

        int numOfUnits = in.nextInt();

        for (int i = 0; i < numOfUnits; i++) {

            int unitId = in.nextInt();      // The unit's ID
            int unitType = in.nextInt();    // The unit's type: 0 = Cultist, 1 = Cult Leader
            int hp = in.nextInt();          // Health points of the unit
            int col = in.nextInt();         // X coordinate of the unit
            int row = in.nextInt();         // Y coordinate of the unit
            int owner = in.nextInt();       // id of owner player

            Unit unit = new Unit(unitId, hp, row, col, owner, unitType);

            allUnits.put(unitId, unit);

            // put the unit in the good collection

            if(owner == myId){
                myUnits.add(unit);
            }
            else if(owner == 2){
                neutralUnits.add(unit);
            }
            else{
                ennemyUnits.add(unit);
            }

            // set my leader or ennemyLeader

            if(unit.getType() == UnitType.CULTLEADER){
                
                if(unit.isFriend()){
                    myLeader = unit;
                }
                else if(unit.isEnnemy()){
                    ennemyLeader = unit;
                }
            }
        }        
    }

    // GENERIC FUNCTIONS
    
    private static Unit getUnitAt(int row, int col){
        return tiles[row][col].getUnit();
    }

    private static int evalDamage(Unit shooter, Unit victim){
        return 7 - getDistance(shooter, victim);
    }

    private static int getDistance(Unit unitA, Unit unitB){
        return Math.abs(unitA.getRow() - unitB.getRow()) + Math.abs(unitA.getCol() - unitB.getCol());
    }

    private static String padStrOwner(int ownerID){

        String strOwner = "";
        int maxWidth = 10;

        if(ownerID == myId){
            strOwner = PadString("Friend", maxWidth);
        }
        else if(ownerID == 2){
            strOwner = PadString("Neutral", maxWidth);
        }
        else{
            strOwner = PadString("Ennemy", maxWidth);
        }

        return strOwner;
    }

    private static String PadString(String mStr, int maxWidth){

        if(mStr.length() >= maxWidth){
            return mStr;
        }else{

            String[] padSpaces = new String[maxWidth - mStr.length()];
            Arrays.fill(padSpaces, " ");

            return mStr + String.join("", padSpaces);
        }
    }    

    public Tile getTileAfterShoot(Tile startTile, Tile targetTile) {

        int x0, y0, x1, y1;

        if (startTile.getRow() < targetTile.getRow()) {

            x0 = startTile.getCol();
            y0 = startTile.getRow();
            
            x1 = targetTile.getCol();
            y1 = targetTile.getRow();

        } else {

            x0 = targetTile.getCol();
            y0 = targetTile.getRow();
            
            x1 = startTile.getCol();
            y1 = startTile.getRow();
        }

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
    
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
    
        int err = dx - dy;
        int currentX = x0;
        int currentY = y0;
    
        while (true) {

            int e2 = 2 * err;
            
            if (e2 > -1 * dy) {
                err -= dy;
                currentX += sx;
            }
    
            if (e2 < dx) {
                err += dx;
                currentY += sy;
            }
    
            if (currentX == x1 && currentY == y1) break;

            boolean isObstacle = tiles[currentX][currentY].getType().equals(TileType.OBSTACLE);
            boolean isUnit = tiles[currentX][currentY].getUnit() != null && tiles[currentX][currentY].getUnit().isInGame;

            if (isObstacle || isUnit) {
                return tiles[currentX][currentY];
            }
        }
        return targetTile;

    }

    // PAHTFINDING

    // PATHFINDING FUNCTIONS

    public static class Border extends Tile{

        // this class represents a border of exploration points, it is used to find the path to the loot

        private String action;
        private List<Border> parentList;
        private int row;
        private int col;

        public Border(int mRow, int mCol, String mAction, Border mParent){

            super(mCol, mRow, TileType.BORDER);
            
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

            for (int i = 0; i < parentList.size(); i++) {
                parentCoords[parentList.size() - i - 1] = String.format("[%s, %s]", parentList.get(i).row, parentList.get(i).col);
            }

            return String.join(" <- ", parentCoords);
        }
    }

    public static Border FindShortPath(Tile startTile, Tile destTile) {

        

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(startTile.getRow(), startTile.getCol(), null, null));

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

    /* public static Border getCloserUnitPath(Unit unitFrom){

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
    } */

}