import java.util.*;

class Player {

    static Map<Integer, Application> applications;
    static Map<Integer, Team> teams;
    static Map<String, Poste> postes;

    static int actualPoste;

    static final int TRAINING = 0;
    static final int CODING = 1;
    static final int DAILY_ROUTINE = 2;
    static final int TASK_PRIORITIZATION = 3;
    static final int ARCHITECTURE_STUDY = 4;
    static final int CONTINUOUS_INTEGRATION = 5;
    static final int CODE_REVIEW = 6;
    static final int REFACTORING = 7;
    static final int BONUS = 8;
    static final int DETTE_TECHNIQUE = 9;

    public static void main(String args[]) {
        
        Scanner in = new Scanner(System.in);

        int[] postesCounts = new int[8];

        Arrays.fill(postesCounts, 5);

        actualPoste = -1;

        while (true) {

            applications = new HashMap<Integer, Application>();
            teams = new HashMap<Integer, Team>();
            postes = new HashMap<String, Poste>();
            
            String gamePhase = in.next(); // can be MOVE, GIVE_CARD, THROW_CARD, PLAY_CARD or RELEASE
            
            int applicationsCount = in.nextInt();

            int[] neededPostes = new int[8];
            
            
            for (int i = 0; i < applicationsCount; i++) {
                String objectType = in.next();

                Application mApplication = new Application(in);

                for(int j = 0; j < neededPostes.length; j++){
                    neededPostes[j] += mApplication.neededCards[j];
                }

                if(applications.containsKey(mApplication.id)) System.err.println("WARNING : Doublon sur les Applications !");
                applications.put(mApplication.id, mApplication);
            }

            for (int i = 0; i < 2; i++) {
                Team mTeam = new Team(in, i);
                teams.put(i, mTeam);

                if(mTeam.location >= 0 && mTeam.location <= 7){
                    if(postesCounts[mTeam.location] > 0) postesCounts[mTeam.location] -=1;
                }
            }

            System.err.println(Arrays.toString(neededPostes));
            System.err.println(Arrays.toString(postesCounts));
            
            int cardLocationsCount = in.nextInt();
            for (int i = 0; i < cardLocationsCount; i++) {
                
                String cardsLocation = in.next(); // the location of the card list. It can be HAND, DRAW, DISCARD or OPPONENT_CARDS (AUTOMATED and OPPONENT_AUTOMATED will appear in later leagues)
                Poste mPoste = new Poste(in, cardsLocation);    

                if(postes.containsKey(cardsLocation)) System.err.println("WARNING : Doublon sur les postes !");                
                postes.put(cardsLocation, mPoste);
            }

            int possibleMovesCount = in.nextInt();
            if (in.hasNextLine()) {
                in.nextLine();
            }

            List<Integer> moves = new ArrayList<Integer>();
            Map<Integer, Integer> appsReadyForRelease = new HashMap<Integer, Integer>();
            
            for (int i = 0; i < possibleMovesCount; i++) {
                
                String[] possibleMove = in.nextLine().split(" ");

                System.err.println(String.join(" ", possibleMove));

                String moveType = possibleMove[0];
                Integer locationMove = 0;

                if(possibleMove.length > 1) locationMove = Integer.parseInt(possibleMove[1]);

                if (moveType.equals("MOVE")){
                    moves.add(locationMove);
                }else if(moveType.equals("RELEASE")){

                    int missingToRelease = applications.get(locationMove).CanBeReleased(postes.get("HAND"));
                    
                    System.err.printf("App %s : Missingto realease = %s\n", applications.get(locationMove).id, missingToRelease);

                    if(missingToRelease <= 2) appsReadyForRelease.put(locationMove, missingToRelease);
                    //releases.add(locationMove);
                }
            }

            // In the first league: RANDOM | MOVE <zoneId> | RELEASE <applicationId> | WAIT; In later leagues: | GIVE <cardType> | THROW <cardType> | TRAINING | CODING | DAILY_ROUTINE | TASK_PRIORITIZATION <cardTypeToThrow> <cardTypeToTake> | ARCHITECTURE_STUDY | CONTINUOUS_DELIVERY <cardTypeToAutomate> | CODE_REVIEW | REFACTORING;
            if(gamePhase.equals("RELEASE")){
                
                if (appsReadyForRelease.size()>0){

                    int minMissingToRelease = 9999;
                    int bestAppToRelease = -1;

                    for(int appId : appsReadyForRelease.keySet()){

                        if(appsReadyForRelease.get(appId) < minMissingToRelease){
                            minMissingToRelease = appsReadyForRelease.get(appId);
                            bestAppToRelease = appId;
                        }
                    }
                    RELEASE(bestAppToRelease);
                }else{
                    WAIT();
                }
            }else{

                int bestPosteID = -1;

                if(bestPosteID == -1){

                    int minNeededCard = 9999;

                    for(int posteID : moves){
                        
                        if(postesCounts[posteID] > 0){

                            for(Application app : applications.values()){

                                if(app.neededCards[posteID] < minNeededCard){
                                    minNeededCard = app.neededCards[posteID];
                                    bestPosteID = posteID;
                                }
                            }
                        }
                    }
                }

                System.err.printf("BestPoste : %s\n", bestPosteID);

                if(bestPosteID == -1) bestPosteID = GetPosteWithMinCards(postesCounts, moves);

                MOVE(bestPosteID);
            }
        }
    }

    public static int GetPosteWithMinCards(int[] tableOfCardsPerPoste, List<Integer> possibleMoves){

        int minCardsCount = 9999;
        int bestPosteID = possibleMoves.get(0);

        for(int posteID : possibleMoves){

            if(tableOfCardsPerPoste[posteID] > 0 && tableOfCardsPerPoste[posteID] < minCardsCount){
                minCardsCount = tableOfCardsPerPoste[posteID];
                bestPosteID = posteID;
            }
        }

        return bestPosteID;
    }

    // CLASSES    

    public static class Poste{

        public String location;
        public int[] cards;
        public int bonusCardsCount;
        public int technicalDebtCardsCount;


        public Poste(Scanner in, String mLocation){

            cards = new int[8];

            location = mLocation;

            for(int i = 0; i < cards.length; i++){
                cards[i] = in.nextInt();
            }

            bonusCardsCount = in.nextInt();
            technicalDebtCardsCount = in.nextInt();

            Print();
        }

        public void Print(){
                                            System.err.printf("Location :                       %s\n", location);
            if(cards[0] > 0)                System.err.printf("trainingCardsCount :             %s\n", cards[0]);
            if(cards[1] > 0)                System.err.printf("codingCardsCount :               %s\n", cards[1]);
            if(cards[2] > 0)                System.err.printf("dailyRoutineCardsCount :         %s\n", cards[2]);
            if(cards[3] > 0)                System.err.printf("taskPrioritizationCardsCount :   %s\n", cards[3]);
            if(cards[4] > 0)                System.err.printf("architectureStudyCardsCount :    %s\n", cards[4]);
            if(cards[5] > 0)                System.err.printf("continuousDeliveryCardsCount :   %s\n", cards[5]);
            if(cards[6] > 0)                System.err.printf("codeReviewCardsCount :           %s\n", cards[6]);
            if(cards[7] > 0)                System.err.printf("refactoringCardsCount :          %s\n", cards[7]);
            if(bonusCardsCount > 0)         System.err.printf("bonusCardsCount :                %s\n", bonusCardsCount);
            if(technicalDebtCardsCount > 0) System.err.printf("technicalDebtCardsCount :        %s\n", technicalDebtCardsCount);

            System.err.println(" ");
        }        
    }

    public static class Team{

        public int id;
        public int location;                            // id of the zone in which the team is located
        public int score;                               // score of the team
        public int permanentDailyRoutineCards;          // number of DAILY_ROUTINE the team has played. It allows them to take cards from the adjacent zones
        public int permanentArchitectureStudyCards;     // number of ARCHITECTURE_STUDY the team has played. It allows them to draw more cards
        
        public Team(Scanner in, int mID){
            id = mID;
            location = in.nextInt();
            score = in.nextInt();
            permanentDailyRoutineCards = in.nextInt();
            permanentArchitectureStudyCards = in.nextInt();
        }

        public void Print(){
            
            System.err.printf("Team id :                            %s\n",id);
            System.err.printf("location :                           %s\n",location);
            System.err.printf("score :                              %s\n",score);
            System.err.printf("permanentDailyRoutineCards :         %s\n",permanentDailyRoutineCards);
            System.err.printf("permanentArchitectureStudyCards :    %s\n",permanentArchitectureStudyCards);

            System.err.println(" ");
        }
    }

    public static class Application{

        public int totalNeededCards;

        public int id;
        public int[] neededCards;       

        public Application(Scanner in){

            neededCards = new int[8];

            id = in.nextInt();
            
            for(int i = 0; i < neededCards.length; i++){
                neededCards[i] = in.nextInt();
                totalNeededCards+=neededCards[i];
            }

            Print();
           
        }

        public void Print(){

                                   System.err.printf("Application ID :             %s\n",id);
            if(neededCards[0] > 0) System.err.printf("trainingNeeded :             %s\n",neededCards[0]);
            if(neededCards[1] > 0) System.err.printf("codingNeeded :               %s\n",neededCards[1]);
            if(neededCards[2] > 0) System.err.printf("dailyRoutineNeeded :         %s\n",neededCards[2]);
            if(neededCards[3] > 0) System.err.printf("taskPrioritizationNeeded :   %s\n",neededCards[3]);
            if(neededCards[4] > 0) System.err.printf("architectureStudyNeeded :    %s\n",neededCards[4]);
            if(neededCards[5] > 0) System.err.printf("continuousDeliveryNeeded :   %s\n",neededCards[5]);
            if(neededCards[6] > 0) System.err.printf("codeReviewNeeded :           %s\n",neededCards[6]);
            if(neededCards[7] > 0) System.err.printf("refactoringNeeded :          %s\n",neededCards[7]);

            System.err.println(" ");
        }

        public int CanBeReleased(Poste mPoste){

            int missingToRelease = 0;

            for(int i = 0; i < neededCards.length; i++){
                missingToRelease += Math.max(0, neededCards[i] - mPoste.cards[i]);
            }            

            return missingToRelease - mPoste.bonusCardsCount;
        }
    }

    // ACTIONS FUNCTIONS

    public static void RELEASE(int appID){
        System.out.println("RELEASE " + appID);
    }

    public static void MOVE(int posteID){
        System.out.println("MOVE " + posteID);
        actualPoste = posteID;
    }

    public static void WAIT(){
        System.out.println("WAIT");
    }

}