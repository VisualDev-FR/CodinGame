import java.util.*;

class Player {

    static Map<Integer, Application> applications;
    static Map<Integer, Team> teams;
    static Map<String, CardCollection> collections;

    static int actualPoste;

    static final int TRAINING = 0;
    static final int CODING = 1;
    static final int DAILY_ROUTINE = 2;
    static final int TASK_PRIORITIZATION = 3;
    static final int ARCHITECTURE_STUDY = 4;
    static final int CONTINUOUS_INTEGRATION = 5;
    static final int CODE_REVIEW = 6;
    static final int REFACTORING = 7;

    static final int CARD_TYPE_COUNT = 8;
    static final int DISPLAY_MARGIN = 15;

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);

        int[] postesCounts = new int[8];

        Arrays.fill(postesCounts, 5);

        actualPoste = -1;

        while (true) {

            applications = new HashMap<Integer, Application>();
            teams = new HashMap<Integer, Team>();
            collections = new HashMap<String, CardCollection>();

            int[] neededPostes = new int[8];
            
            String gamePhase = in.next(); // can be MOVE, GIVE_CARD, THROW_CARD, PLAY_CARD or RELEASE           

            int applicationsCount = in.nextInt();
            for (int i = 0; i < applicationsCount; i++) {
                
                String objectType = in.next();

                Application mApplication = new Application(in);

                for(int j = 0; j < neededPostes.length; j++){
                    neededPostes[j] += mApplication.neededCards[j];
                }

                applications.put(mApplication.id, mApplication);
            }

            for (int i = 0; i < 2; i++) {
                Team mTeam = new Team(in, i);
                teams.put(i, mTeam);

                if(mTeam.location >= 0 && mTeam.location <= 7){
                    if(postesCounts[mTeam.location] > 0) postesCounts[mTeam.location] -=1;
                }
            }

            int cardLocationsCount = in.nextInt();
            for (int i = 0; i < cardLocationsCount; i++) {
                
                String cardsLocation = in.next(); // the location of the card list. It can be HAND, DRAW, DISCARD or OPPONENT_CARDS (AUTOMATED and OPPONENT_AUTOMATED will appear in later leagues)
                
                CardCollection mCardCollection = new CardCollection(in, cardsLocation);    
                
                collections.put(cardsLocation, mCardCollection);
            }

            PrintCollections();
            PrintApplications();

            int possibleMovesCount = in.nextInt();
            if (in.hasNextLine()) {
                in.nextLine();
            }

            List<Integer> moves = new ArrayList<Integer>();
            Map<Integer, Integer> appsReadyForRelease = new HashMap<Integer, Integer>();
            
            for (int i = 0; i < possibleMovesCount; i++) {
                
                String[] possibleMove = in.nextLine().split(" ");

                String moveType = possibleMove[0];
                Integer locationMove = 0;

                if(possibleMove.length > 1) locationMove = Integer.parseInt(possibleMove[1]);

                if (moveType.equals("MOVE")){
                    moves.add(locationMove);
                }else if(moveType.equals("RELEASE")){

                    int missingToRelease = applications.get(locationMove).GetMissingCardsForRelease(collections.get("HAND"));

                    appsReadyForRelease.put(locationMove, missingToRelease);
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

                int bestPosteID = GetPosteWhereAppNeedMinCard(postesCounts, moves);  
                //int bestPosteID = GetPosteWhereMaxAppsNeedCard(postesCounts, moves);                

                System.err.printf("BestPoste : %s\n", bestPosteID);

                if(bestPosteID == -1) bestPosteID = GetPosteWithMinCards(postesCounts, moves);

                MOVE(bestPosteID);
            }
        }
    }

    public static int GetPosteWhereMaxAppsNeedCard(int[] postesCounts, List<Integer> moves){

        int bestPosteID = -1;
        int maxNeededCard = 0;

        for(int posteID : moves){

            if(postesCounts[posteID] > maxNeededCard){
                maxNeededCard = postesCounts[posteID];
                bestPosteID = posteID;
            }            
        }

        return bestPosteID;

    }

    public static int GetPosteWhereAppNeedMinCard(int[] postesCounts, List<Integer> moves){

        int bestPosteID = -1;
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

        return bestPosteID;

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

    public static class CardCollection{

        public String location;
        public int[] cards;
        public int bonusCardsCount;
        public int technicalDebtCardsCount;


        public CardCollection(Scanner in, String mLocation){

            cards = new int[8];

            location = mLocation;

            for(int i = 0; i < cards.length; i++){
                cards[i] = in.nextInt();
            }

            bonusCardsCount = in.nextInt();
            technicalDebtCardsCount = in.nextInt();
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

        public int GetMissingCardsForRelease(CardCollection mCollection){

            int missingToRelease = 0;

            for(int i = 0; i < neededCards.length; i++){
                missingToRelease += Math.max(0, neededCards[i] - mCollection.cards[i]);
            }            

            return missingToRelease - mCollection.bonusCardsCount;
        }
    }

    // DISPLAY FUNCTIONS

    public static void PrintCollections(){

        String[][] tableToPrint = new String[collections.size()][11];
        int i = 0;

        for(CardCollection mCollection : collections.values()){

            tableToPrint[i][0]= PadString(mCollection.location, DISPLAY_MARGIN);

            for(int j = 0; j < 8; j++){

                tableToPrint[i][j+1] = PadInteger(mCollection.cards[j]);
            }

            tableToPrint[i][9] = PadInteger(mCollection.bonusCardsCount);
            tableToPrint[i][10] = PadInteger(mCollection.technicalDebtCardsCount);

            System.err.println(String.join(" ", tableToPrint[i]));
            i++;
        }

        System.err.println(" ");
    }

    public static void PrintApplications(){

        String[][] tableToPrint = new String[applications.size()][9];
        int i = 0;

        for(Application mApplication : applications.values()){

            tableToPrint[i][0]= PadString("App " + mApplication.id, DISPLAY_MARGIN);

            for(int j = 0; j < 8; j++){

                tableToPrint[i][j+1] = PadInteger(mApplication.neededCards[j]);
            }

            System.err.println(String.join(" ", tableToPrint[i]));
            i++;
        }

        System.err.println(" ");
    }

    public static String PadString(String mStr, int maxWidth){

        if(mStr.length() >= maxWidth){
            return mStr;
        }else{

            String[] padSpaces = new String[maxWidth - mStr.length()];
            Arrays.fill(padSpaces, " ");

            return mStr + String.join("", padSpaces);
        }
    }

    public static String PadInteger(int mInt){
        if(mInt != 0){
            return String.format("%02d", mInt);
        }else{
            return "..";
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

    public static void RANDOM(){
        System.out.println("RANDOM");
    }

}