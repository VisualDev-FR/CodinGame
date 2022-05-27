import java.util.*;
import java.io.*;;

class Solution {

    static Map<String, Station> stations;
    static Map<String, Branch> branches;
    
    static String startID;
    static String endID;
    static List<String> answers;

    public static void main(String args[]) {
        
        stations = new HashMap<String, Station>();
        branches = new HashMap<String, Branch>();
        answers = new ArrayList<String>();

        int localSession = 0;

        if(localSession < 7 && localSession >= 0){
            LocalSession(localSession);
        }else{
            OnlineSession();
        }
    }

    static void SearchPath(){

        answers.add("Reponse bidon");

        System.err.printf("Stations : %s Branches : %s start = %s end = %s\n", stations.size(), branches.size(), stations.get(startID).name, stations.get(endID).name);

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

    static void ReadInputs(Scanner in){

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

        SearchPath();

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

            SearchPath();

            boolean sameSize = validatorAnswers.size() == answers.size() ? true : false;

            for(int i = 0; i < validatorAnswers.size(); i++){

                String valAnswer = validatorAnswers.get(i);

                String answer = i < answers.size() ? answers.get(i) : "Nothing";

                String status = sameSize && valAnswer == answer ? "OK" : "NOK"; 

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