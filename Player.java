import java.util.*;

class Player {

    static final int MINIMAX_DEPTH = 6;

    static int nbOfPlayer;
    static int myPlayerID;

    static LightCycle myLightCycle;
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

            String bestDirection = "UP";
            int maxSurface = 0;
            int minOppDist = 9999;

            System.err.printf("myPosition = %s %s\n", myLightCycle.Y1, myLightCycle.X1);

            for(String direction : directions.keySet()){

                int[] dirCoord = directions.get(direction);

                int nextRow = myLightCycle.Y1 + dirCoord[0];
                int nextCol = myLightCycle.X1 + dirCoord[1];

                int surface = GetDiffusedSurface(nextRow, nextCol, grid.arrayGrid);
                int oppDist = GetCloserOpponent(nextRow, nextCol);

                boolean validPosition = IsPositionValid(nextRow, nextCol, grid.arrayGrid);

                System.err.printf("%s : surface = %s / oppDist : %s\n", direction, surface, oppDist);

                if(validPosition && (surface > maxSurface || (surface == maxSurface && oppDist < minOppDist))){
                    bestDirection = direction;
                    maxSurface = surface;
                    minOppDist = oppDist;
                }
            }

            System.out.println(bestDirection);

            firstTurn = false;
        }
    }

    // UTILITIES

    static int GetCloserOpponent(int row, int col){

        int minDist = 9999;

        for(LightCycle lightCycle : lightCycles.values()){

            if(lightCycle.ID != myPlayerID && !lightCycle.IsDead()){
                int dist = GetDistance(row, col, lightCycle.Y1, lightCycle.X1);
                minDist = Math.min(minDist, dist);
            }
        }

        return minDist;
    }

    static int GetDiffusedSurface(int row, int col, String[][] arrayGrid){

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

    static int GetDistance(int row1, int col1, int row2, int col2){
        return Math.abs(row1-row2) + Math.abs(col1-col2);
    }

    // CLASSES

    public static class LightCycle{

        private int ID;
        private int X0;
        private int Y0;
        private int X1;
        private int Y1;

        private boolean erased;

        private List<int[]> coords;
        
        public LightCycle(int mID){            
            coords = new ArrayList<int[]>();
            ID = mID;
            erased = false;
        }

        public void Erase(){

            for(int[] coord : coords){
                grid.set(coord[1], coord[0], ".");
            }           
        }

        public boolean IsDead(){
            return X0 == -1 && Y0 == -1 && X1 == -1 && Y1 == -1;
        }

        public void Update(Scanner in){

            X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
            Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
            X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
            Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

            if(!IsDead()){
                coords.add(new int[]{X1, Y1});
                grid.set(Y1, X1, Integer.toString(this.ID)); 
                grid.set(Y0, X0, Integer.toString(this.ID)); 
            }else if(!erased){
                Erase();
                erased = true;
            }
        }
    }
    
    public static class Grid{

        String[][] arrayGrid;
        
        public Grid(){

            arrayGrid = new String[20][30];

            for(int i = 0; i < arrayGrid.length; i++){
                for(int j = 0; j < arrayGrid[0].length; j++){
                    arrayGrid[i][j] = ".";
                }
            }
        }

        public String get(int row, int column){            
            return arrayGrid[row][column];
        }

        public void set(int row, int column, String value){
            arrayGrid[row][column] = value;
        }

        public void Print(){
            for(int i = 0; i < arrayGrid.length; i++){
                System.err.println(String.join(" ", arrayGrid[i]));
            }
        }
    }
    
    // INPUTS READING

    static void ReadFirstInputs(Scanner in){

        directions = new HashMap<String, int[]>();
        directions.put("RIGHT", new int[]{ 0 ,  1});
        directions.put("DOWN",  new int[]{ 1 ,  0});
        directions.put("LEFT",  new int[]{ 0 , -1});
        directions.put("UP",    new int[]{-1 ,  0});
        
        grid = new Grid();

        nbOfPlayer = in.nextInt(); // total number of players (2 to 4).
        myPlayerID = in.nextInt(); // your player number (0 to 3).

        lightCycles = new HashMap<Integer, LightCycle>();

        for (int i = 0; i < nbOfPlayer; i++){
            
            LightCycle mLightCycle = new LightCycle(i);

            mLightCycle.Update(in);
            lightCycles.put(i, mLightCycle);                   
        }

        myLightCycle = lightCycles.get(myPlayerID);
    }

    static void ReadInputs(Scanner in){

        nbOfPlayer = in.nextInt(); // total number of players (2 to 4).
        myPlayerID = in.nextInt(); // your player number (0 to 3).

        for(LightCycle lightCycle : lightCycles.values()){
            lightCycle.Update(in);            
        }
    }    
}