import java.util.*;

class Player {

    static int nbOfPlayer;
    static int myPlayerID;

    static Map <Integer, LightCycle> lightCycles;
    static Map<String, int[]> directions;
    static Grid grid;

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);

        boolean firstTurn = true;

        ReadFirstInputs(in);

        while (true){

            if(!firstTurn) ReadInputs(in);

            grid.Print();

            String bestDirection = "";

            System.err.printf("myPosition = %s %s\n", lightCycles.get(myPlayerID).Y1, lightCycles.get(myPlayerID).X1);

            for(String direction : directions.keySet()){

                System.err.print(direction + " ");

                int[] dirCoord = directions.get(direction);

                int nextRow = lightCycles.get(myPlayerID).Y1 + dirCoord[0];
                int nextCol = lightCycles.get(myPlayerID).X1 + dirCoord[1];

                if(IsPositionValid(nextRow, nextCol)) bestDirection = direction;
            }

            System.out.println(bestDirection);
            firstTurn = false;
        }
    }

    static boolean IsPositionValid(int row, int col){

        boolean isValid = false;

        if(col >= 0 && col < 30 && row >=0 && row < 20){

            isValid = grid.get(row, col) == -1;

            System.err.printf("%s %s : %s (%s)\n", row, col, grid.get(row, col), isValid);
        
        }else{

            System.err.printf("%s %s : %s (%s)\n", row, col, "?", isValid);
        }

        return isValid;

    }

    static void ReadFirstInputs(Scanner in){

        directions = new HashMap<String, int[]>();
        directions.put("RIGHT", new int[]{ 0 ,  1});
        directions.put("LEFT",  new int[]{ 0 , -1});
        directions.put("UP",    new int[]{-1 ,  0});
        directions.put("DOWN",  new int[]{ 1 ,  0});  
        
        grid = new Grid();

        nbOfPlayer = in.nextInt(); // total number of players (2 to 4).
        myPlayerID = in.nextInt(); // your player number (0 to 3).

        lightCycles = new HashMap<Integer, LightCycle>();
        
        for (int i = 0; i < nbOfPlayer; i++){
            
            LightCycle mLightCycle = new LightCycle(i);

            mLightCycle.Update(in);

            lightCycles.put(i, mLightCycle);            
        }
    }

    static void ReadInputs(Scanner in){

        nbOfPlayer = in.nextInt(); // total number of players (2 to 4).
        myPlayerID = in.nextInt(); // your player number (0 to 3).

        for(LightCycle lightCycle : lightCycles.values()){
            lightCycle.Update(in);
        }
    }

    public static class LightCycle{

        private int ID;
        private int X0;
        private int Y0;
        private int X1;
        private int Y1;

        private List<int[]> coords;
        private boolean isDead;

        public LightCycle(int mID){            
            coords = new ArrayList<int[]>();
            ID = mID;
            isDead = false;
        }

        public void Update(Scanner in){

            X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
            Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
            X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
            Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

            isDead = X0 == -1 && Y0 == -1 && X1 == -1 && Y1 == -1;

            if(isDead){

                for(int[] coord : coords){
                    grid.set(coord[1], coord[0], -1);
                }

            }else{
                coords.add(new int[]{X1, Y1});
                grid.set(Y1, X1, ID);
            }
            
        }
    }
    
    public static class Grid{

        int[][] exploredGrid;
        
        public Grid(){

            exploredGrid = new int[20][30];

            for(int i = 0; i < exploredGrid.length; i++){
                for(int j = 0; j < exploredGrid[0].length; j++){
                    exploredGrid[i][j] = -1;
                }
            }
        }

        public int get(int row, int column){            
            return exploredGrid[row][column];
        }

        public void set(int row, int column, int value){
            exploredGrid[row][column] = value;
        }

        public void Print(){

            for(int i = 0; i < exploredGrid.length; i++){

                for(int j = 0; j < exploredGrid[0].length; j++){

                    //System.err.print(exploredGrid[i][j]);

                    if(j % 5 == 0) System.err.print ("  ");

                    if(exploredGrid[i][j] == -1){
                        System.err.print(".");
                    }else if(exploredGrid[i][j] == myPlayerID){
                        System.err.print("#");
                        //System.err.printf("%s %s val = %s / myID = %s\n", i, j, exploredGrid[i][j], myPlayerID);
                    }else{                        
                        System.err.print(exploredGrid[i][j]);
                    }   
                }
                System.err.print("\n");
            }
        }
    }
}