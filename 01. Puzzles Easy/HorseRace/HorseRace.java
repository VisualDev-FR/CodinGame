import java.util.*;
import java.io.*;
import java.math.*;

class Solution {

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);
        int N = in.nextInt();

        int ecartMin = Integer.MAX_VALUE;
        TreeMap<Integer, Integer> powerMap = new TreeMap<Integer, Integer>();        

        for (int i = 0; i < N; i++) {
            int mPower = in.nextInt();
            powerMap.put(mPower, mPower);
        }

        List<Integer> keys = new ArrayList<Integer>(powerMap.keySet());      

        for(int i = 0; i < keys.size()-1; i++){

            Integer key1 = keys.get(i);
            Integer key2 = keys.get(i+1);            

            int ecart = Math.abs(powerMap.get(key1)-powerMap.get(key2));

            //System.err.println(key1 + " " + key2 + " " + " : " + ecart);

            if(ecart<ecartMin){
                ecartMin = ecart;
            }
        }

        System.out.println(ecartMin);
    }
}
