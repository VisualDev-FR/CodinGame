import java.util.*;
import java.io.*;

class Solution {

    static Map<String, Station> stations;
    
    static String startID;
    static String endID;
    static List<String> answers;
    static double minDist;

    public static void main(String args[]) {

        int localSession = 0;

        if(localSession < 7 && localSession >= 0){
            LocalSession(localSession);
        }else{
            OnlineSession();
        }
    }

    public static void SubMain(){ 
        
        minDist = Double.MAX_VALUE;

        if(answers.size() == 0){

            Border bestBorder = FindShortPath(stations.get(startID), stations.get(endID));
            
            if(bestBorder == null){
                answers.add("IMPOSSIBLE");
            }else{
                for(String strParent : bestBorder.parentList){
                    answers.add(strParent);
                }
                answers.add(bestBorder.station.name);
            }            
        }
    }

    static Border FindShortPath(Station startStation, Station endStation){

        List<Border> borders = new ArrayList<Border>();
        borders.add(new Border(startStation));

        List<String> investigateStations = new ArrayList<String>();
        investigateStations.add(startStation.ID);

        Border bestBorder = null;
        boolean pathFound = false;

        while(borders.size()>0 && !pathFound){

            List<Border> bordersTemp = new ArrayList<Border>(borders);

            for(Border border : bordersTemp){

                borders.remove(borders.indexOf(border));                

                for(Connection connection : border.station.connections){

                    Station nextStation = connection.endStation;

                    if(!investigateStations.contains(nextStation.ID)){

                        Border mNewBorder = new Border(nextStation, border);

                        if(nextStation.ID.equals(endID)){
                            pathFound = true;
                            bestBorder = mNewBorder;
                        }
                        
                        borders.add(mNewBorder);                            
                    }

                    investigateStations.add(nextStation.ID);
                          
                }
            }
        }

        return bestBorder;
    }

    public static class Border{

        private Station station;
        private List<String> parentList;

        public Border(Station mStation, Border mParent){
            
            station = mStation;

            parentList = new ArrayList<String>(mParent.parentList);
            parentList.add(mParent.station.name);        
        }

        public Border(Station mStation){
            
            station = mStation;

            parentList = new ArrayList<String>();            
        }
    }    

    static double GetTotalDist(List<Station> mStations){

        double totalDist = 0;
        Station lastStation = null;

        for(Station mStation : mStations){

            if(lastStation != null){
                totalDist += GetDistance(lastStation, mStation);
            }

            lastStation = mStation;
        }

        return totalDist;
    }

    static double GetDistance(Station stationA, Station stationB){

        double x = (stationB.longitude - stationA.longitude) * Math.cos((stationA.latitude + stationB.latitude) / 2);
        double y = (stationB.latitude - stationA.latitude);

        return 6371 * Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

    }

    static String ParseLine(String line_){
        return line_.split(":")[1].replace("\"", "");
    }    

    static void ReadInputs(Scanner in){

        stations = new HashMap<String, Station>();
        answers = new ArrayList<String>();

        startID = ParseLine(in.next());
        endID = ParseLine(in.next());
        
        int N = in.nextInt();
        
        if (in.hasNextLine()) in.nextLine();
        
        for (int i = 0; i < N; i++) {
            Station station = new Station(in.nextLine());
            stations.putIfAbsent(station.ID, station);

            if(startID.equals(endID) && startID.equals(station.ID)) answers.add(station.name);
        }
        
        int M = in.nextInt();
        
        if (in.hasNextLine()) in.nextLine();
        
        for (int i = 0; i < M; i++) {
            
            String[] strBranch = in.nextLine().split(" ");

            Station from = stations.get(ParseLine(strBranch[0]));
            Station to = stations.get(ParseLine(strBranch[1]));

            if(startID.equals(from.ID) && endID.equals(to.ID)){
                answers.add(from.name);
                answers.add(to.name);
            }

            from.Connect(to);;
        }

        System.err.printf("Stations : %s start = %s end = %s\n\n", stations.size(), stations.get(startID).name, stations.get(endID).name);
    }    

    public static class Station{

        public String ID;
        public String name;

        public double latitude;
        public double longitude;

        public int type;

        List<Connection> connections;

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

            connections = new ArrayList<Connection>();
        }

        public void Connect(Station station){
            connections.add(new Connection(this, station));
        }

        public void Print(){
            System.err.printf("%s : name = %s / latitude = %s / longitude = %s / Type = %s / Connections = %s", ID, name, latitude, longitude, type, connections.size());
        }
    }

    public static class Connection{

        Station startStation;
        Station endStation;
        double distance;

        public Connection(Station stationFrom_, Station stationTo_){

            startStation = stationFrom_;
            endStation = stationTo_;

            distance = GetDistance(startStation, endStation);

        }
    }

    //Running Sessions

    static void OnlineParsing(){

        ReadInputs(new Scanner(System.in));
    }

    static void LocalParsing(int validator){

        try {

            String validatorPath = "Validators\\TanNetwork\\Validator_" + validator + ".txt";

            File myObj = new File(validatorPath);

            Scanner in = new Scanner(myObj);

            ReadInputs(in);            

            in.close();
        
        }catch (FileNotFoundException e) {
            
            System.out.println("An error occurred.");
            e.printStackTrace();
        
        }    
    }

    static void OnlineSession(){

        OnlineParsing();

        SubMain();

        String finalAnswers = String.join("\n", answers.toArray(new String[0]));

        System.out.println(finalAnswers);        
    }

    static void LocalSession(int number){

        if(number == 0){

            System.out.print("Validator to run ?  ");

            number = new Scanner(System.in).nextInt();

            System.out.print("\n");

            if(number > 6 || number < 0) return;

            if(number == 0){

                for(int i = 1; i < 7; i++) RunLocal(i);

            }else{

                RunLocal(number);
            }        

            System.out.println(" ");
            LocalSession(0);
        
        }else{
            RunLocal(number);
        }
    }

    static void RunLocal(int validator){

        LocalParsing(validator);

        System.err.println(" ");

        try {

            String answerPath = "Validators\\TanNetwork\\Answers_" + validator + ".txt";

            File myObj = new File(answerPath);
            Scanner in = new Scanner(myObj);

            List<String> validatorAnswers = new ArrayList<String>();

            while(in.hasNextLine()){
                validatorAnswers.add(in.nextLine());
            }

            in.close();

            SubMain();

            boolean sameSize = validatorAnswers.size() == answers.size() ? true : false;

            for(int i = 0; i < validatorAnswers.size(); i++){

                String valAnswer = validatorAnswers.get(i);

                String answer = i < answers.size() ? answers.get(i) : "Nothing";

                String status = sameSize && valAnswer.equals(answer) ? "OK" : "NOK"; 

                System.err.printf("%s : %s / %s\n", status, valAnswer, answer);
            }            
        
        }catch (FileNotFoundException e) {
            
            System.out.println("An error occurred.");
            e.printStackTrace();
        
        }    
    }

    static long GetValidator(int number){

/*         switch (number){

            case 1: return Validator_1();
            case 2: return Validator_2();
            case 3: return Validator_3();
            case 4: return Validator_4();
            case 5: return Validator_5();
            case 6: return Validator_6();
        } */

        return -1;
    }
}