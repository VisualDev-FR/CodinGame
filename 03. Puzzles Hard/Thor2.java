//Based on the denvash's solution, see at https://github.com/denvash/codingame-puzzles-solutions/blob/master/3-Hard/Power-of-Thor-Episode-2.py

import java.util.*;

class Player {

    static int[][] giants = new int[0][0];

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);

        int[] position = new int[] {in.nextInt(), in.nextInt()};

        while (true) {

            System.err.println("" + position[0] + " " + position[1]);

            int hammerCount = in.nextInt(); // the remaining number of hammer strikes.
            int giantCount = in.nextInt(); // the number of giants which are still present on the map.

            giants = new int[giantCount][2];
            
            for (int i = 0; i < giantCount; i++) {

                giants[i][0]=in.nextInt();
                giants[i][1]=in.nextInt();

            }

            int[] center = GetCenter();

            if(GetKillableGiants(position) == giantCount){
            
                System.out.println("STRIKE");
            
            }else if(!MortalPosition(position)){
                System.err.println("no danger");
                MOVE(position, center[0], center[1]);
            
            }else{
                System.err.println("run away");
                SearchBestMove(position, center);
            }
        }
    }

    public static void SearchBestMove(int[] position, int[] centerPosition){

        int px = position[0];
        int py = position[1];

        TreeMap<String, int[]> possibleMoves = new TreeMap<String, int[]>();

        if(px < 40){
            int x = px+1;
            int y = py;

            if(!MortalPosition(new int[]{x, y})) possibleMoves.put("E", new int[]{x, y});
        }

        if(py > 0){
            int x = px;
            int y = py-1;
            if(!MortalPosition(new int[]{x, y})) possibleMoves.put("N", new int[]{x, y});
        }

        if(px < 40 && py > 0){
            int x = px+1;
            int y = py-1;
            if(!MortalPosition(new int[]{x, y})) possibleMoves.put("NE", new int[]{x, y});
        }

        if(px > 0 && py > 0){
            int x = px-1;
            int y = py-1;
            if(!MortalPosition(new int[]{x, y})) possibleMoves.put("NW", new int[]{x, y});
        }

        if(py < 18){
            int x = px+1;
            int y = py;
            if(!MortalPosition(new int[]{x, y})) possibleMoves.put("S", new int[]{x, y});
        }

        if(px < 40 && py < 18){
            int x = px+1;
            int y = py+1;
            if(!MortalPosition(new int[]{x, y})) possibleMoves.put("SE", new int[]{x, y});
        }

        if(px > 0 && py > 0){
            int x = px;
            int y = py-1;
            if(!MortalPosition(new int[]{x, y})) possibleMoves.put("SW", new int[]{x, y});
        }

        if(px > 0){
            int x = px-1;
            int y = py;
            if(!MortalPosition(new int[]{x, y})) possibleMoves.put("W", new int[]{x, y});
        }

        int maxKillableGiants = 0;
        int maxCenterDist = 0;

        String bestMove = "STRIKE";

        int[] mMove = null;
        int[] bestMoveCoord = null;

        for(String strMove : possibleMoves.keySet()){

            mMove = possibleMoves.get(strMove);

            int killableGiants = GetKillableGiants(mMove);
            int centerDist = GetDistance(mMove, centerPosition);

            System.err.println(strMove + " " + killableGiants + " " + MortalPosition(mMove));

            if((killableGiants > maxKillableGiants) || (killableGiants == maxKillableGiants && centerDist > maxCenterDist)){ 

                bestMove = strMove;
                bestMoveCoord = mMove;
                maxKillableGiants = killableGiants;
                maxCenterDist = centerDist;

            }
        }

        if(bestMove != "STRIKE"){
            MOVE(position, bestMoveCoord[0], bestMoveCoord[1]);
        }else{
            System.out.println("STRIKE");
        }

    }

    public static int GetDistance(int[] pos1, int[] pos2){
        return Math.abs(pos1[0]-pos2[0]) + Math.abs(pos1[1]-pos2[1]);
    }

    public static int GetKillableGiants(int[] position){

        int px = position[0];
        int py = position[1];

        int nbKillableGiants = 0;

        for(int i =0; i <giants.length; i++){

            if(Math.abs(giants[i][0]-px)<=4 && Math.abs(giants[i][1]-py)<=4) nbKillableGiants+=1;

        }

        return nbKillableGiants;
    }    

    public static boolean MortalPosition(int[] position){

        int px = position[0];
        int py = position[1];

        for(int i =0; i<giants.length; i++){

            //System.err.println("" + Math.abs(giants[i][0]-px) + " " + Math.abs(giants[i][1]-py) + " ");

            if(Math.abs(giants[i][0]-px)<=1 && Math.abs(giants[i][1]-py)<=1) return true;

        }

        return false;
    }

    public static int[] GetCenter(){
        
        int averageX = 0;
        int averageY = 0;

        for(int i = 0; i<giants.length; i++){
            averageX+=giants[i][0];
            averageY+=giants[i][1];
        }

        return new int[]{averageX/giants.length, averageY/giants.length};
    }

    //ACTION FUNCTIONS

    public static void MOVE(int[] position, int moveX, int moveY) {

        int lightX=moveX;
        int lightY=moveY;
        int initialTx=position[0];
        int initialTy=position[1];

        if(initialTx==lightX && initialTy== lightY){
            WAIT();
            return;
        }

        boolean isNorth = initialTy>0 && lightY<initialTy;
        boolean isSouth = initialTy<18 && lightY>initialTy;
        boolean isWest = initialTx>0 && lightX<initialTx;
        boolean isEast = initialTx<40 && lightX>initialTx;

        System.out.println((isNorth?"N":isSouth?"S":"")+(isWest?"W":isEast?"E":""));

        initialTy+=(isSouth?1:(isNorth?-1:0));
        initialTx+=(isEast?1:(isWest?-1:0));

        position[0] = initialTx;
        position[1] = initialTy;

    }

    public static void STRIKE(int hammerCount){
        if(hammerCount>0){
            System.out.println("STRIKE");
        }else{
            WAIT();
        }
    }

    public static void WAIT(){
        System.out.println("WAIT");
    }

}
