import java.util.*;

class Solution {

    static boolean[][] visitedPoints;
    static int[][][] lakeMap;
    static char[][] charMap;
    static int mapWidth;
    static int mapHeight;

    public static void main(String args[]) {

        TreeMap<Integer, Lake> lakes = new TreeMap<Integer, Lake>();
        
        Scanner in = new Scanner(System.in);
        
        mapWidth = in.nextInt();
        mapHeight = in.nextInt();

        visitedPoints = new boolean[mapWidth][mapHeight];
        lakeMap = new int[mapWidth][mapHeight][2];
        charMap = new char[mapWidth][mapHeight];

        if (in.hasNextLine()) in.nextLine();

        int nbSand = 0;
        int nbWater = 0;
        
        for (int i = 0; i < mapHeight; i++) {

            char[] charArray = in.nextLine().toCharArray();
            for(int j = 0; j < charArray.length; j++){
                
                charMap[j][i] = charArray[j];

                nbSand += charArray[j] == '#' ? 1 : 0;
                nbWater += charArray[j] == 'O' ? 1 : 0;
            }
        }

        int nbCoordsToTest = in.nextInt();

        String[] answers = new String[nbCoordsToTest];

        for (int i = 0; i < nbCoordsToTest; i++) {

            int px = in.nextInt();
            int py = in.nextInt();

            if(nbWater == 0 || charMap[px][py] == '#'){

                answers[i]="0\n";
            
            }else if(nbSand == 0){

                answers[i]= "" + mapWidth * mapHeight + "\n";

            }else if(nbSand < mapWidth && nbSand < mapHeight){
            
                answers[i]= nbWater + "\n";

            }else if(charMap[px][py] == 'O'){
                
                if (lakeMap[px][py][0]==1){

                    int lakeKey = lakeMap[px][py][1];

                    answers[i]=("" + lakes.get(lakeKey).surface + "\n");
                
                }else{

                    lakes.put(lakes.size(), new Lake(lakes.size(), px, py));

                    answers[i]=("" + lakes.get(lakes.size()-1).surface + "\n");
                }
            
            }else{

                answers[i]="0\n";
            } 
                      
        }

        System.out.println(String.join("", answers));

    }

    public static class Lake{

        public int surface;

        public Lake(int ID, int currentX, int currentY){

            visitedPoints[currentX][currentY] = true;
            surface++;

            int[][] directions = new int[][]{
                { 0 , -1}, //North
                { 0 ,  1}, //South
                { 1 ,  0}, //East
                {-1 ,  0}  //West
            };

            lakeMap[currentX][currentY][0]=1;
            lakeMap[currentX][currentY][1]=ID;

            List<int[]> borders = new ArrayList<int[]>(); //a dictionary wich will be dynamicly alimented / erased during the investigations ==> for the first turn, we need only one border, wich represent the first point where we found water

            borders.add(new int[]{currentX, currentY});

            while(borders.size() > 0){ //while there is at least one waterpoint found, we continue to investigate around him at next turn

                List<int[]> bordersTemp = new ArrayList<int[]>(borders);

                for(int[] borderCoord : bordersTemp){ //at each turn, we investigate all four directions of all borders we previously found

                    borders.remove(borders.indexOf(borderCoord)); //we dont want to investigate again this point at the next turn  

                    for(int k = 0; k < directions.length; k++){ //we investigate all four directions around the currentPoint
                        
                        int x = borderCoord[0] + directions[k][0];
                        int y = borderCoord[1] + directions[k][1];

                        if(x < charMap.length && x >=0 && y < charMap[0].length && y >= 0){ //if the invesigated point is not out of the map

                            if(visitedPoints[x][y] == false){ //if we never visited before the point, we want to investigate it

                                visitedPoints[x][y]=true; //we are investigating this point, so we say it has already been visited

                                if(charMap[x][y]=='O'){ //if the point is water, we add waterpoint on our Lake, and this new waterpoint is a new border that we will investigate at next turn

                                    surface++;

                                    borders.add(new int[]{x, y});
                                    
                                    lakeMap[x][y][0] = 1;
                                    lakeMap[x][y][1] = ID; 
                                }
                            }
                        }
                    }                                      
                }
            }
        }
    }
}
