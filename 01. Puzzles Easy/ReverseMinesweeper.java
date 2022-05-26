//Reverse Minesweeper : https://www.codingame.com/training/easy/reverse-minesweeper

import java.util.*;

class Solution {

    static Scanner in;

    public static void main(String args[]) {

        in = new Scanner(System.in);
        
        int mapWidth = in.nextInt();
        int mapHeight = in.nextInt();        
        
        Map<String, int[]> minesCoords = ReadInputs(mapHeight);
        List<int[]> directions = GetDirections();

        for(int row = 0; row < mapHeight; row++){

            String answer = "";

            for(int column = 0; column < mapWidth; column++){

                String caseKey = "" + row + ":" + column;

                if(minesCoords.containsKey(caseKey)){

                    answer += ".";

                }else{

                    int mineCount = 0;

                    for(int[] direction : directions){
                        
                        int nextRow = row + direction[0];
                        int nextColumn = column + direction[1];

                        String nextKey = "" + nextRow + ":" + nextColumn;

                        mineCount += minesCoords.containsKey(nextKey) ? 1 : 0;
                    }

                    answer += mineCount > 0 ? mineCount : ".";
                }
            }

            System.out.println(answer);
        }
    }

    static List<int[]> GetDirections(){
        
        List<int[]> directions = new ArrayList<int[]>();

        directions.add(new int[]{ 0 ,  1});
        directions.add(new int[]{-1 ,  1});
        directions.add(new int[]{-1 ,  0});
        directions.add(new int[]{-1 , -1});

        directions.add(new int[]{ 0 , -1});
        directions.add(new int[]{ 1 , -1});
        directions.add(new int[]{ 1 ,  0});
        directions.add(new int[]{ 1 ,  1});

        return directions;
    }

    static Map<String, int[]> ReadInputs(int mapHeight){

        if (in.hasNextLine()) {
            in.nextLine();
        }

        int row = 0;

        Map<String, int[]> minesCoords = new HashMap<String, int[]>();
        
        for (int i = 0; i < mapHeight; i++) {

            String acsiiLine = in.nextLine();

            System.err.println(acsiiLine);

            int index = 0;
            int column = acsiiLine.indexOf("x", index);

            while(column > -1){

                String mineKey = "" + row + ":" + column;
                
                int[] mineCoord = new int[]{row, column};

                minesCoords.put(mineKey, mineCoord);

                index = acsiiLine.indexOf("x", index) + 1;
                column = acsiiLine.indexOf("x", index); 

            }

            row++;

        }

        return minesCoords;
    }
}