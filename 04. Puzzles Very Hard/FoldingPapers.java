import java.util.*;

class Solution {

    public static int height;
    public static int width;

    public static void main(String args[]) {
        
        Scanner in = new Scanner(System.in);
        
        int foldsCount = in.nextInt();
        
        width = in.nextInt();
        height = in.nextInt();

        System.err.printf("%s %s\n", width, height);
        
        if (in.hasNextLine()) in.nextLine();

        String[][] folded = parsePaper(in, width, height);
        List<Zone> zones = getZones(folded);

        long count = 0;
        for(Zone zone : zones){
            long zoneCount = zone.getCount(foldsCount);
            count += zoneCount;
        }
        System.out.println(count);
    }

    private static class Zone{

        private boolean touchUp;
        private boolean touchLeft;
        private boolean touchDown;
        private boolean touchRight;

        private List<int[]> points;

        public Zone(int row, int col){
            this.points = new ArrayList<int[]>();
            this.addPoint(row, col);
        }

        public void addPoint(int row, int col){
            this.points.add(parseInt(col, row));
            this.touchUp = this.touchUp || row == 0;
            this.touchLeft = this.touchLeft || col == 0;
            this.touchDown = this.touchDown || row == height-1;
            this.touchRight = this.touchRight || col == width-1;
        }

        public long getCount(int foldsCount){

            long count = 0;

            if(touchUp && touchLeft && touchRight && touchDown){
                //###  ###  ###  ..#  #..
                //#.#  #..  ..#  ..#  #..
                //###  #..  ..#  ###  ###
                count+= 1;
            }
            else if( touchUp &&  touchLeft && !touchRight &&  touchDown){
                //#..
                //#..
                //#..
                count+= (long) Math.pow(2, foldsCount - 1);
            }
            else if( touchUp && !touchLeft && !touchRight &&  touchDown){
                //.#.
                //.#.
                //.#.
                count+= (long) Math.pow(2, foldsCount);
            }            
            else if( touchUp && !touchLeft &&  touchRight &&  touchDown){
                //..#
                //..#
                //..#
                count+= (long) Math.pow(2, foldsCount - 1) + 1;
            }
            else if( touchUp &&  touchLeft &&  touchRight && !touchDown){
                //###
                //...
                //...
                count+= (long) Math.pow(2, foldsCount - 1);
            }
            else if(!touchUp &&  touchLeft &&  touchRight && !touchDown){
                //...
                //###
                //...
                count = (long) Math.pow(2, foldsCount);
            }            
            else if(!touchUp &&  touchLeft &&  touchRight &&  touchDown){
                //...
                //...
                //###
                count+= (long) Math.pow(2, foldsCount - 1) + 1;
            }
            else if( touchUp &&  touchLeft && !touchRight && !touchDown){
                //#..
                //...
                //...
                count+= (long) Math.pow(4, foldsCount - 1);
            }            
            else if( touchUp && !touchLeft && !touchRight && !touchDown){
                //.#.
                //...
                //...
                count+= (long) Math.pow(4, foldsCount) / 2;
            }
            else if( touchUp && !touchLeft &&  touchRight && !touchDown){
                //..#
                //...
                //...
                count+= (long) Math.pow(4, foldsCount) / 4 + (long) Math.pow(2, foldsCount - 1);
            }
            else if(!touchUp &&  touchLeft && !touchRight && !touchDown){
                //...
                //#..
                //...
                count+= (long) Math.pow(4, foldsCount) / 2;
            }
            else if(!touchUp && !touchLeft && !touchRight && !touchDown){
                //...
                //.#.
                //...
                count+= (long) Math.pow(4, foldsCount);
            }
            else if(!touchUp && !touchLeft &&  touchRight && !touchDown){
                //...
                //..#
                //...
                count+= (long) Math.pow(2, foldsCount) + (long) Math.pow(4, foldsCount) / 2;
            }
            else if(!touchUp &&  touchLeft && !touchRight &&  touchDown){
                //...
                //...
                //#..
                count+= (long) Math.pow(4, foldsCount) / 4 + (long) Math.pow(2, foldsCount - 1);
            }           
            else if(!touchUp && !touchLeft && !touchRight &&  touchDown){
                //...
                //...
                //.#.
                count+= (long) Math.pow(2, foldsCount) + (long) Math.pow(4, foldsCount) / 2;
            } 
            else if(!touchUp && !touchLeft &&  touchRight &&  touchDown){
                //...
                //...
                //..#
                count += Math.pow(1 + (long) Math.pow(2, foldsCount) / 2, 2);
            }
            else{
                System.err.printf("NOT FOUND : up = %s, left = %s, right = %s, down = %s\n",touchUp, touchLeft, touchRight, touchDown);
            }

            return count;
        }

        public String toString(){
            return String.format("start : [%s, %s](%s), [%s %s %s %s], count =", points.get(0)[0], points.get(0)[1], points.size(),touchUp, touchLeft, touchRight, touchDown);
        }
    }

    private static String[][] parsePaper(Scanner in, int width, int height){        
        String[][] folded = new String[height][width];
        for (int i = 0; i < height; i++) {            
            folded[i] = in.nextLine().split("");
        }
        return folded;     
    }

    private static int[] parseInt(int row, int col){
        return new int[]{col, row};
    }

    static List<Zone> getZones(String[][] pattern){
        
        int[][] directions = new int[][]{{0, 1},{0, -1},{1, 0},{-0, 0},};
        boolean[][] visitedPoints = new boolean[pattern.length][pattern[0].length];

        List<Zone> zones = new ArrayList<Zone>();

        for(int row = 0; row < pattern.length; row++){

            for(int col = 0; col < pattern[0].length; col++){

                if(!visitedPoints[row][col] && pattern[row][col].equals("#")){

                    visitedPoints[row][col] = true;

                    Zone zone = new Zone(row, col);
                    
                    List<int[]> borders = new ArrayList<int[]>();
                    borders.add(new int[]{row, col});

                    while(borders.size() > 0){
                        
                        List<int[]> bordersTemp = new ArrayList<int[]>(borders);

                        for(int[] border : bordersTemp){
                            
                            borders.remove(borders.indexOf(border));

                            for(int[] direction : directions){

                                int nextRow = border[0] + direction[0];
                                int nextCol = border[1] + direction[1];

                                if(nextRow >= 0 && nextRow < pattern.length && nextCol >=0 && nextCol < pattern[0].length){
                                    
                                    if(!visitedPoints[nextRow][nextCol]){

                                        visitedPoints[nextRow][nextCol] = true;       
            
                                        if(pattern[nextRow][nextCol].equals("#")){
    
                                            borders.add(new int[]{nextRow, nextCol});
                                            zone.addPoint(nextRow, nextCol);                                           
                                        }
                                    }
                                }
                            }
                        }
                    }
                    zones.add(zone);
                }
            }
        }
        return zones;        
    }
  
}