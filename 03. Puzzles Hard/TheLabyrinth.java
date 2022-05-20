import java.util.*;

import javax.swing.border.Border;

class Player {

    static Scanner in = new Scanner(System.in); 

    static List<String> actions; 

    static int mapHeight;
    static int mapWidth; 
    static int nbRounds;

    static boolean teleporterFound;
    static boolean commandFound;

    static char[][] charMap;
    static int[] teleporterCoords;
    static int[] commandCoords;

    static TreeMap<String, int[]> directions;
    public static void main(String args[]) {

        InitDirections();

        int x = -1;
        int y = -1;

        mapHeight = in.nextInt(); // number of rows.
        mapWidth = in.nextInt(); // number of columns.
        nbRounds = in.nextInt(); // number of rounds between the time the alarm countdown is activated and the time the alarm goes off.
        actions = new ArrayList<String>();

        //First, we search to discover all accessible points on the charMap

        boolean mapExplored = false;

        while (!mapExplored){

            y = in.nextInt(); // row where Rick is located.
            x = in.nextInt(); // column where Rick is located.

            ParseMap(x, y);

            mapExplored = ExploreNewRoad(x, y);        
        }

        System.err.println("Map 100% explored.. 33% done :)");        

        //In a second time, we search the shortest way to reach the command center

        boolean commandReached = false;

        while(!commandReached){

            if(mapExplored){
                mapExplored = false;
            }else{

                y = in.nextInt(); // row where Rick is located.
                x = in.nextInt(); // column where Rick is located.

                ParseMap(x, y);
            }

            FindShortPath(x, y, commandCoords[0], commandCoords[1]);

            commandReached = (x == commandCoords[0] && y == commandCoords[1]);

        }

        System.err.println("Command center reached.. 66% done :)");

        //At the end, we go-back to the teleporter, by finding the shortest path

        boolean teleporterReached = false;

        while(!teleporterReached){

            if(commandReached){
                commandReached = false;
            }else{

                y = in.nextInt(); // row where Rick is located.
                x = in.nextInt(); // column where Rick is located.

                ParseMap(x, y);
            }

            FindShortPath(x, y, teleporterCoords[0], teleporterCoords[1]);

            teleporterReached = (x == teleporterCoords[0] && y == teleporterCoords[1]);

        }

        System.err.println("teleporter reached.. 100% well done :)");        

    }

    static boolean FindShortPath(int heroX, int heroY, int destX, int destY){

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(heroX, heroY, null, null));

        boolean[][] investigatePoints = new boolean[mapWidth][mapHeight];

        investigatePoints[heroX][heroY] = true;

        Border bestBorder = null;
        boolean pathFound = false;

        while(borders.size()>0 && !pathFound){

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));                

                for(String strDir : directions.keySet()){

                    int[] dirCoord = directions.get(strDir);

                    int nextX = border.x + dirCoord[0];
                    int nextY = border.y + dirCoord[1];

                    if(IsPointOnMap(nextX, nextY)){

                        char currentChar = charMap[nextX][nextY];

                        if(!investigatePoints[nextX][nextY] && currentChar != '#'){

                            Border mNewBorder = new Border(nextX, nextY, strDir, border);

                            if(nextX == destX && nextY == destY){
                                pathFound = true;
                                bestBorder = mNewBorder;
                            }
                            
                            borders.add(mNewBorder);                            
                        }

                        investigatePoints[nextX][nextY] = true;
                    }        
                }
            }
        }

        if(Math.abs(heroX - destX) + Math.abs(heroY - destY) == 1){
            Move(heroX, heroY, destX, destY);
        }else if(bestBorder != null){
            System.out.println(bestBorder.FirstAction());
            return true;
        }
        
        return false;
    }

    static boolean ExploreNewRoad(int heroX, int heroY){

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(heroX, heroY, null, null));

        boolean[][] investigatePoints = new boolean[mapWidth][mapHeight];

        investigatePoints[heroX][heroY] = true;

        Border bestBorder = null;

        while(borders.size()>0 && bestBorder == null){

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            int minBorderDist = mapWidth * mapHeight;

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));                

                for(String strDir : directions.keySet()){

                    int[] dirCoord = directions.get(strDir);

                    int nextX = border.x + dirCoord[0];
                    int nextY = border.y + dirCoord[1];

                    if(IsPointOnMap(nextX, nextY)){

                        char currentChar = charMap[nextX][nextY];

                        if(!investigatePoints[nextX][nextY] && currentChar != '#'){

                            Border mNewBorder = new Border(nextX, nextY, strDir, border);

                            int borderDist = mNewBorder.TotalDist();

                            if(borderDist < minBorderDist && currentChar == '?'){
                                minBorderDist = borderDist;
                                bestBorder = mNewBorder;
                            }
                            
                            borders.add(mNewBorder);                            
                        }

                        investigatePoints[nextX][nextY] = true;
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

    static void ParseMap(int heroX, int heroY){

        charMap = new char[mapWidth][mapHeight];

        for (int i = 0; i < mapHeight; i++) {            
            
            char[] ROW = in.next().toCharArray();

            for(int j = 0; j < mapWidth; j++){
                
                if(j == heroX && i == heroY && ROW[j] != 'C' && ROW[j] != 'T'){
                    ROW[j] = '@';
                }else if(j == 0 || j == mapWidth-1 || i == 0 || i == mapHeight-1){
                    ROW[j] = '#';
                }

                charMap[j][i] = ROW[j];
                
                if(ROW[j] == 'C' && !commandFound){
                    commandCoords = new int[]{j, i}; 
                    commandFound = true;
                }else if(ROW[j] == 'T'){
                    teleporterCoords = new int[]{j, i};
                    teleporterFound = true;
                }
                
            }

            System.err.println(ROW);
        }

    }

    static boolean IsPointOnMap(int px, int py){
        return px >= 0 && px < mapWidth && py >= 0 && py < mapHeight ? true : false;
    }

    static boolean Move(int heroX, int heroY, int destX, int destY){

        for(String direction : directions.keySet()){
            
            int[] dirCoord = directions.get(direction);
            
            int nextX = heroX + dirCoord[0];
            int nextY = heroY + dirCoord[1];

            if(nextX == destX && nextY == destY){
                System.out.println(direction);
                return true;
            }
        }

        return false;
    }

    static void InitDirections(){
        directions = new TreeMap<String, int[]>();
        directions.put("RIGHT", new int[]{ 1 ,  0});
        directions.put("LEFT",  new int[]{-1 ,  0});
        directions.put("UP",    new int[]{ 0 , -1});
        directions.put("DOWN",  new int[]{ 0 ,  1});         
    }

    static void debug(String message){
        System.err.println(message);
    }

    public static class Border{

        private String action;
        private List<Border> parentList;
        private Border parent;
        private int x;
        private int y;

        public Border(int px, int py, String mAction, Border mParent){
            
            x = px;
            y = py;

            parent = mParent;
            action = mAction;

            if(mParent == null){
                parentList = new ArrayList<Border>();
            }else{
                parentList = new ArrayList<Border>(mParent.parentList);
                //mParent.action = mAction; 
                parentList.add(mParent);
            }            
        }

        public String FirstAction(){
            return parentList.get(1).action;
        }

        public int TotalDist(){
            return parentList.size();
        }
    }
}
