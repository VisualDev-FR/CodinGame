import java.util.*;

class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        double LON = Double.parseDouble(in.next().replace(",", "."));
        double LAT = Double.parseDouble(in.next().replace(",", "."));

        int N = in.nextInt();
        if (in.hasNextLine()) {
            in.nextLine();
        }

        double minDist = Double.MAX_VALUE;
        String minName = "";

        for (int i = 0; i < N; i++) {

            String DEFIB[] = in.nextLine().split(";");
            String defibName = DEFIB[1];

            double defibLon = Double.parseDouble(DEFIB[4].replace(",", "."));
            double defibLat = Double.parseDouble(DEFIB[5].replace(",", "."));

            double cDist = GetDistance(LON, LAT, defibLon, defibLat);

            if (cDist<minDist){
                minName = defibName;
                minDist = cDist;
            }
        }
        System.out.println(minName);
    }

    static double GetDistance(double lonA, double latA, double lonB, double latB){

        double x = (lonB-lonA)*Math.cos(0.5*(latA+latB));
        double y = (latB-latA);

        return 6371 * Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2));
    }
}
