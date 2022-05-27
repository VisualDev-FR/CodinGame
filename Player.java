import java.util.*;

class Solution {

    static Map<String, Station> stations;
    static Map<String, Branch> branches;
    
    static String startID;
    static String endID;

    public static void main(String args[]) {
        
        stations = new HashMap<String, Station>();
        branches = new HashMap<String, Branch>();

        ReadInputs();

        System.err.printf("Stations : %s Branches : %s start = %s end = %s\n", stations.size(), branches.size(), stations.get(startID).name, stations.get(endID).name);

    }

    static void ReadInputs(){

        Scanner in = new Scanner(System.in);

        startID = ParseLine(in.next());
        endID = ParseLine(in.next());
        
        int N = in.nextInt();
        
        if (in.hasNextLine()) in.nextLine();
        
        for (int i = 0; i < N; i++) {
            Station station = new Station(in.nextLine());
            stations.putIfAbsent(station.ID, station);
        }
        
        int M = in.nextInt();
        
        if (in.hasNextLine()) in.nextLine();
        
        for (int i = 0; i < M; i++) {
            String[] strBranch = in.nextLine().split(" ");

            Station from = stations.get(ParseLine(strBranch[0]));
            Station to = stations.get(ParseLine(strBranch[1]));

            branches.putIfAbsent(from+":"+to, new Branch(from, to));
        }
    }

    static double GetDistance(Station stationA, Station stationB){

        double x = (stationB.longitude - stationA.longitude) * Math.cos((stationA.latitude + stationB.latitude) / 2);
        double y = (stationB.latitude - stationA.latitude);

        return 6371 * Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

    }

    static String ParseLine(String line_){
        return line_.split(":")[1].replace("\"", "");
    }
    
    public static class Branch{

        public Station from;
        public Station to;
        
        public Branch(Station from_, Station to_){
            from = from_;
            to = to_;
        }
    }

    public static class Station{

        public String ID;
        public String name;

        public double latitude;
        public double longitude;

        public int type; 

        public Station(String line_){

            /* 
            0 L'identifiant unique de l'arrêt
            1 Le nom complet de l'arrêt entouré du caractère guillemet "
            2 La description de l'arrêt (non utilisée)
            3 La latitude de l'arrêt (en degrés)
            4 La longitude de l'arrêt (en degrés)
            5 L'identifiant de la zone (non utilisé)
            6 L'url de l'arrêt (non utilisée)
            7 Le type d'arrêt
            8 La station parente (non utilisée)
            */            

            String[] line = ParseLine(line_).split(",");

            ID = line[0];
            name = line[1].replace("\"", "");

            latitude = Double.parseDouble(line[3]);
            longitude = Double.parseDouble(line[4]);

            type = Integer.parseInt(line[7]);
        }
    }
}