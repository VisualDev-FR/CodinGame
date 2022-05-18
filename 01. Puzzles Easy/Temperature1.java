import java.util.*;

class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        int minDeltaAbs = Integer.MAX_VALUE;
        int minDelta = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {

            int t = in.nextInt();

            if (Math.abs(t)<minDeltaAbs){
                minDeltaAbs = Math.abs(t);
                minDelta = t;            
            } else if(Math.abs(t)==minDeltaAbs) {
                if (t > minDelta){
                    minDelta = t;
                }
            }
        }

        if (minDelta == Integer.MAX_VALUE){
            minDelta = 0;
        }

        System.out.println(minDelta);
    }
}
