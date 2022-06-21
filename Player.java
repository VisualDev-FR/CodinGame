import java.util.*;

/* lose Seeds : 
    seed=4884180411688883200
    seed=-1860785835977721340
    seed=6533722682535114800
    seed=-3752517535242261500   
*/

/* Inputs :
    12
    APPLICATION 14 0 0 4 0 4 0 0 0
    APPLICATION 10 0 4 0 0 0 4 0 0
    APPLICATION 20 0 0 0 4 0 0 4 0
    APPLICATION 21 0 0 0 4 0 0 0 4
    APPLICATION 24 0 0 0 0 4 0 0 4
    APPLICATION 17 0 0 4 0 0 0 0 4
    APPLICATION 27 0 0 0 0 0 0 4 4
    APPLICATION 5 4 0 0 0 0 0 4 0
    APPLICATION 1 4 0 4 0 0 0 0 0
    APPLICATION 9 0 4 0 0 4 0 0 0
    APPLICATION 4 4 0 0 0 0 4 0 0
    APPLICATION 23 0 0 0 0 4 0 4 0
    -1 0 0 0
    -1 0 0 0
    3
    HAND 0 0 0 0 0 0 0 0 2 2
    DRAW 0 0 0 0 0 0 0 0 2 2
    OPPONENT_CARDS 0 0 0 0 0 0 0 0 4 4
    9
    MOVE 0
    MOVE 1
    MOVE 2
    MOVE 3
    MOVE 4
    MOVE 5
    MOVE 6
    MOVE 7
    RANDOM  
*/

class Player {

    static Team myTeam;
    static Team oppTeam;
    static CardCollection allMyCards;
    static Map<String, Integer> movesToEval;
    static Map<Integer, Application> applications;
    static Map<String, CardCollection> collections;
    static List<String> possibleMoves;
    static int[] remainingCards;
    static int[] totalNeeds;
    static String action;
    static List<String> playType;
    static int remainingCompetenceCards;
  
    static String gamePhase;
    static int gameTurn = 0;    

    static final int TRAINING = 0;
    static final int CODING = 1;
    static final int DAILY_ROUTINE = 2;
    static final int TASK_PRIORITIZATION = 3;
    static final int ARCHITECTURE_STUDY = 4;
    static final int CONTINUOUS_INTEGRATION = 5;
    static final int CODE_REVIEW = 6;
    static final int REFACTORING = 7;
    static final int BONUS_CARD = 8;
    static final int TECHNICAL_DEBT = 9;

    static final int CARD_TYPE_COUNT = 8;
    static final int DISPLAY_MARGIN = 7;

    public static void main(String args[]) throws Exception {

        Scanner in = new Scanner(System.in);

        PARSE_PLAY_TYPES();

        while (true) {

            gamePhase = in.next(); // can be MOVE, GIVE_CARD, THROW_CARD, PLAY_CARD or RELEASE

            movesToEval = new HashMap<String, Integer>();
            applications = new HashMap<Integer, Application>();
            collections = new HashMap<String, CardCollection>();
            possibleMoves = new ArrayList<String>();

            PARSE_APPLICATIONS(in);
            PARSE_TEAMS(in);
            PARSE_CARDS_COLLECTIONS(in);
            PARSE_POSSIBLE_MOVES(in); 

            switch (gamePhase) {
                case "MOVE":
                    PHASE_MOVE();
                    break;
                case "GIVE_CARD":
                    PHASE_GIVE();
                    break;
                case "THROW_CARD":
                    PHASE_THROW();
                    break;
                case "PLAY_CARD":
                    PHASE_PLAY();
                    break;
                case "RELEASE":
                    PHASE_RELEASE();
                    break;
                default:
                    RANDOM();
                    break;                    
            }

            PRINT_GAME();

            System.out.println(action);
            
            gameTurn++;
        }
    }
    
    // CLASSES

    public static class CardCollection{

        public String location;
        public int totalCards;
        public int[] cards;
        public int[] proportions;

        public CardCollection(CardCollection mCollection){

            location = mCollection.location;
            cards = mCollection.cards.clone();
            proportions = mCollection.proportions.clone();
        }

        public CardCollection(Scanner in, String mLocation){
            
            proportions = new int[CARD_TYPE_COUNT + 2];
            cards = new int[CARD_TYPE_COUNT + 2];
            location = mLocation;

            for(int i = 0; i < cards.length; i++){
                cards[i] = in.nextInt();
                totalCards += cards[i];
            }

            ResetProportions();
        }

        private void ResetProportions(){
            
            for(int i = 0; i < proportions.length; i++){
                
                if(totalCards > 0){
                    proportions[i] = 100 * cards[i] / totalCards;
                }else{
                    proportions[i] = 0;
                } 
            }
        }        

        public void RemoveCard(int cardID) throws Exception{

            if(cards[cardID] > 0){
                cards[cardID] = Math.max(0, cards[cardID] - 1);
                totalCards --;
                ResetProportions();
                
            }else{
                cards = new int[0];
                throw new Exception("Le nombre de cartes est déjà nul ! carteID = " + cardID);                
            }            
        }
        
        public void AddCard(int cardID){
            cards[cardID] += 1;
            totalCards++;
            ResetProportions();
        }

        public int CardCount(int cardID){
            return cards[cardID];
        }
        
        public int nonTechnicalDebtCardCount(){
            return totalCards - cards[TECHNICAL_DEBT];
        }

        public int competenceCardsCount(){
            return totalCards - cards[BONUS_CARD] - cards[TECHNICAL_DEBT];
        }

        public int technicalDebtCount(){
            return cards[TECHNICAL_DEBT];
        }

        public int bonusCardCount(){
            return cards[BONUS_CARD];
        }

        public int releasableAppsCount(){

            int releasableApps = 0;

            for(Application mApp : applications.values()){
                if(mApp.GetMissingCardsForRelease(this) <= 0) releasableApps++;
            }
            return releasableApps;
        }

        public int usefullCardCount(boolean withBonus){
            
            int totalUsefullCards = 0;

            for(int i = 0; i < cards.length; i++){
                if(totalNeeds[i] > 0 && cards[i] > 0) totalUsefullCards++;
            }

            return totalUsefullCards + (withBonus ? cards[BONUS_CARD] : 0);
        }

        public void Print(){

            String[] tableToPrint = new String[CARD_TYPE_COUNT + 3]; // +3 car on rajoute le nom de la collection en [0]

            tableToPrint[0]= PadString(ParseLocation(location), DISPLAY_MARGIN);

            for(int j = 0; j < cards.length; j++){

                tableToPrint[j+1] = PadInteger(cards[j], 2,true);
            }

            System.err.println(String.join(" ", tableToPrint));
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
    }

    public static class Application{

        public int totalNeededCards;
        public String objectType;

        public int id;
        public int[] neededCards;       

        public Application(Scanner in){

            neededCards = new int[8];
            
            objectType = in.next();
            id = in.nextInt();
            
            for(int i = 0; i < neededCards.length; i++){
                neededCards[i] = in.nextInt();
                totalNeededCards+=neededCards[i];
            }
        }

        public int GetMissingCardsForRelease(CardCollection mCollection){

            int missingToRelease = 0;

            for(int i = 0; i < neededCards.length; i++){
                missingToRelease += Math.max(0, neededCards[i] - mCollection.cards[i] * 2);
            }

            return missingToRelease - mCollection.cards[BONUS_CARD];
        }

        public boolean releaseableByOpponent(){
            CardCollection oppCards = GetOpponnentCards();            
            return GetMissingCardsForRelease(oppCards) <= 0;
        }

        public void Print(){

            String[] tableToPrint = new String[9];

            tableToPrint[0]= PadString("App " + PadInteger(id, 2, false), DISPLAY_MARGIN);

            for(int j = 0; j < 8; j++){
                tableToPrint[j+1] = PadInteger(neededCards[j], 2, true);
            }

            System.err.println(String.join(" ", tableToPrint));
        }
    }

    // PHASE FUNCTIONS

    public static void PHASE_THROW() throws Exception{ //TODO: ne pas jeter les cartes de jeu les plus importantes + donner les cartes qui ne servent plus à rien si elles sont pas trop importantes

        Map<String, Integer> throwsToEval = new HashMap<String, Integer>();
        int bonusCards = collections.get("HAND").cards[8];
        String bestMove = "";

        if (bonusCards > 0){

            bestMove = THROW(8);
        
        }else{

            for(String move : possibleMoves){

                if(move.startsWith("THROW")){
                    int eval = EVAL_THROW(move);
                    throwsToEval.put(move, eval);
                }
            }

            bestMove = GetMinValue(throwsToEval);
        }   

        if(!bestMove.equals("")){
            action = bestMove;
        }else{
            action = RANDOM();
        }
    }

    public static boolean EVAL_THROW_NEW(int cardID){

        int[] throwableCards = collections.get("HAND").cards;

        boolean throwCard = false;

        if(throwableCards[cardID] > 0){
            switch(cardID){
                case BONUS_CARD: 
                    throwCard = true;
                    break;
                //case 
            }
        }

        return throwCard;
    }

    public static void PHASE_GIVE() throws Exception{ //TODO: ne pas jeter les cartes de jeu les plus importantes

        Map<String, Integer> throwsToEval = new HashMap<String, Integer>();
        String bestMove = "";

        for(String move : possibleMoves){

            if(move.startsWith("GIVE")){
                int eval = EVAL_THROW(move);
                throwsToEval.put(move, eval);
            }
        }

        bestMove = GetMinValue(throwsToEval);

        if(!bestMove.equals("")){
            action = bestMove;
        }else{
            action = RANDOM();
        }
    }

    public static void PHASE_PLAY() throws Exception{ //TODO: prioriser les cartes permanentes si on en a pas d'active

        Map<String, Integer> moves = new HashMap<String, Integer>();

        for(String move : possibleMoves){

            int eval = 0;

            switch(move.split(" ")[0]){
                case "TRAINING":
                    eval = EVAL_TRAINING(move);
                    break;
                case "CODING":
                    eval = EVAL_CODING(move);
                    break;
                case "DAILY_ROUTINE":
                    eval = EVAL_DAILY_ROUTINE(move);
                    break;
                case "TASK_PRIORITIZATION":
                    eval = EVAL_TASK_PRIORITIZATION(move);
                    break;                                                            
                case "ARCHITECTURE_STUDY":
                    eval = EVAL_ARCHITECTURE_STUDY(move);
                    break;
                case "CONTINUOUS_INTEGRATION":
                    eval = EVAL_CONTINUOUS_INTEGRATION(move);
                    break;                    
                case "CODE_REVIEW":
                    eval = EVAL_CODE_REVIEW(move);
                    break;
                case "REFACTORING":
                    eval = EVAL_REFACTORING(move);
                    break;
                case "WAIT":
                    eval = 1;
                    break;                    
            }

            moves.put(move, eval);
        }

        int maxEval = 0;
        String bestMove="";

        for(String move : moves.keySet()){
            if(moves.get(move) > maxEval){
                maxEval = moves.get(move);
                bestMove = move;
            }
        }

        if(!bestMove.equals("")){

            int releasableApp = CanIReleaseAnApp();
            int playedCardID = GetCardID(bestMove);

            if(releasableApp > -1 && playedCardID > -1 && !bestMove.startsWith("CONTINUOUS_INTEGRATION")){

                if(applications.get(releasableApp).neededCards[playedCardID] > 0){
                    possibleMoves.add("RELEASE " + releasableApp + "(at next turn) => we dont play");
                    action = WAIT();
                    return;
                }
            }

            if(bestMove.startsWith("DAILY_ROUTINE")) myTeam.permanentDailyRoutineCards++;
            if(bestMove.startsWith("ARCHITECTURE_STUDY")) myTeam.permanentArchitectureStudyCards++;
            
            action = bestMove;

        }else{
            action = RANDOM();
        } 
    }

    public static void PHASE_RELEASE(){

        Map<Integer, Integer> appsReadyForRelease = new HashMap<Integer, Integer>();

        for(String move : possibleMoves){

            if(move.split(" ")[0].equals("RELEASE")){

                int appID = Integer.parseInt(move.split(" ")[1]);

                CardCollection usefullHand = GetUsefullHand(); //collections.get("HAND");
                Application releasableApp = applications.get(appID);

                int missingToRelease = releasableApp.GetMissingCardsForRelease(usefullHand);
                int eval = missingToRelease;

                //System.err.printf("app %s : missing = %s, myDebt = %s, oppDebt = %s\n", appID, missingToRelease, myTechnicalDebt, oppTechnicalDebt);

                if(missingToRelease <= 1){
                    eval =  releasableApp.releaseableByOpponent() ? missingToRelease - 100 : missingToRelease;
                    appsReadyForRelease.put(appID, eval);
                }

                movesToEval.put(move, eval);
            }
        }

        if (appsReadyForRelease.size()>0){

            int minMissingToRelease = 9999;
            int bestAppToRelease = -1;

            for(int appId : appsReadyForRelease.keySet()){

                if(appsReadyForRelease.get(appId) < minMissingToRelease){
                    minMissingToRelease = appsReadyForRelease.get(appId);
                    bestAppToRelease = appId;
                }
            }
            
            action = RELEASE(bestAppToRelease);
        }else{
            action = WAIT();
        }
    } 

    public static void PHASE_MOVE(){

        System.err.printf("My position = %s\nOppPosition = %s\n\n", myTeam.location, oppTeam.location);

        boolean zoneFound = false;
        boolean ignoreRemaining = false;

        int startZone = myTeam.location;
        int bonusZone = -1;
        int cycles = 0;

        if(gameTurn > 0){

             if(myTeam.permanentDailyRoutineCards > 0){
                
                while(!zoneFound){
                    
                    ignoreRemaining = cycles > 7;
                    
                    startZone = startZone < 7 ? startZone + 1 : 0;                
                    bonusZone = ChooseBonusZone(startZone);
                    
                    if(bonusZone == startZone){
                        zoneFound = EVAL_ZONE(startZone, ignoreRemaining);
                    }else{
                        zoneFound = EVAL_ZONE(startZone, bonusZone, ignoreRemaining);
                    }
                    cycles++;
                }
                action = MOVE(startZone, bonusZone);
            }else{

                while(!zoneFound){

                    ignoreRemaining = cycles > 7;

                    startZone = startZone < 7 ? startZone + 1 : 0;    
                    zoneFound = EVAL_ZONE(startZone, ignoreRemaining);

                    cycles++;
                }                
                action = MOVE(startZone);           
            }       
        }else{
            action = GetFirstMove();
        }
    }   
        
    // EVALUATION FUNCTIONS

    public static boolean EVAL_ZONE(int zoneToMove, int bonusZone, boolean ignoreRemaining){

        int collectionCount = collections.get("AUTOMATED") != null ? collections.get("AUTOMATED").cards[zoneToMove] : 0;
        
        boolean check_Distance = GetDistance(zoneToMove, oppTeam.location) > 1;
        boolean check_Remaining  = remainingCards[zoneToMove] > 0 || ignoreRemaining;
        boolean check_Needs = totalNeeds[zoneToMove] - collectionCount > 0;
        boolean check_Possible = possibleMoves.contains(MOVE(zoneToMove, bonusZone));

        boolean zoneFound = check_Distance && check_Remaining && check_Needs && check_Possible;

        movesToEval.put(MOVE(zoneToMove, bonusZone), zoneFound ? 999 : -99);

        return zoneFound;

    }

    public static boolean EVAL_ZONE(int zoneToMove, boolean ignoreRemaining){

        int collectionCount = collections.get("AUTOMATED") != null ? collections.get("AUTOMATED").cards[zoneToMove] : 0;
        
        boolean check_Remaining  = remainingCards[zoneToMove] > 0 || ignoreRemaining;
        boolean check_Distance = GetDistance(zoneToMove, oppTeam.location) > 1;
        boolean check_Needs = totalNeeds[zoneToMove] - collectionCount > 0 || (zoneToMove >= DAILY_ROUTINE && zoneToMove <= CONTINUOUS_INTEGRATION);
        boolean check_Possible = possibleMoves.contains(MOVE(zoneToMove));

        System.err.printf("EVAL_%s : %s, %s, %s, %s\n", MOVE(zoneToMove), check_Remaining, check_Distance, check_Needs, check_Possible);

        boolean zoneFound = check_Distance && check_Remaining && check_Needs && check_Possible;

        movesToEval.put(MOVE(zoneToMove), zoneFound ? 1 : 0);

        return zoneFound;       
    }

    public static int EVAL_THROW(String move) throws Exception{

        // 1) le throw doit favoriser le jet de cartes bonus, sur le jet de compétences                 OK
        // 2) le throw doit maximiser le nombre d'appli dispo pour release                              OK
        // 3) le throw doit défavoriser le jet de cartes à jouer qui nous offrent un avantage certain   NOK

        int cardID = Integer.parseInt(move.split(" ")[1]); 
        int minMissingCard = 9999;
        
        for(Application mApp : applications.values()){

            CardCollection handTemp = GetUsefullHand(); //new CardCollection(collections.get("HAND"));

            handTemp.RemoveCard(cardID);

            int missingCard = mApp.GetMissingCardsForRelease(handTemp);
            
            minMissingCard = Math.min(minMissingCard, missingCard);
        }

        movesToEval.put(move, minMissingCard);
        
        return minMissingCard;                
    }

    public static int EVAL_TRAINING(String move){

        int eval = -999;

        CardCollection nextDraw = GetUsefullDraw();

        if(nextDraw != null && nextDraw.nonTechnicalDebtCardCount() > 1){
            if(collections.get("HAND").cards[TECHNICAL_DEBT] > nextDraw.cards[TECHNICAL_DEBT] || nextDraw.cards[TECHNICAL_DEBT] == 0){
                eval = 999;
            }
        }

        movesToEval.put(move, eval);

        return eval;
    }    

    public static int EVAL_CODING(String move){

        int eval = -999;

        CardCollection nextDraw = GetUsefullDraw();

        if(nextDraw != null && nextDraw.nonTechnicalDebtCardCount() > 0){
            if(collections.get("HAND").cards[TECHNICAL_DEBT] > nextDraw.cards[TECHNICAL_DEBT] || nextDraw.cards[TECHNICAL_DEBT] == 0){
                eval = 999;
            }
        }

        movesToEval.put(move, eval);

        return eval;
    }

    public static int EVAL_DAILY_ROUTINE(String move){
        int eval = 9999 * (1 - myTeam.permanentDailyRoutineCards);
        movesToEval.put(move, eval);
        return eval;
    }

    public static int EVAL_CONTINUOUS_INTEGRATION(String move){

        int cardID = Integer.parseInt(move.split(" ")[1]);
        int eval = -9999;

        int automatedCards = collections.containsKey("AUTOMATED") ? collections.get("AUTOMATED").cards[cardID] : 0;

        if(automatedCards > 1 && cardID < 8){
            eval = -9999;
        }else{
            if(collections.get("HAND").nonTechnicalDebtCardCount() > 0){
                if(cardID == 8){            
                    eval = Integer.MAX_VALUE;
                }else if(cardID < 8){
                    eval = totalNeeds[cardID] * 9999;
                }
            }
        }

        movesToEval.put(move, eval);                
        return eval;
    }
    
    public static int EVAL_TASK_PRIORITIZATION_OLD(String move) throws Exception{

        // 1) on calcul le nombre mini de cartes necessaires pour chaque appli avec notre main actuelle, puis on stocke le mini
        // 2) on fait de meme avec une CollectionCard virtuelle, basée sur la nouvelle main faisant suite à l'échange en cours d'évaluation

        int minCardsWithMyHand = 9999;
        int minCardsWithNewHand = 9999;

        String[] tMove = move.split(" ");

        int cardToThrow = Integer.parseInt(tMove[1]);
        int cardToTake = Integer.parseInt(tMove[2]);

        CardCollection myNewHand = GetUsefullHand();        //new CardCollection(collections.get("HAND"));
        CardCollection myActualHand = GetUsefullHand();     //collections.get("HAND")

        myNewHand.RemoveCard(cardToThrow);
        myNewHand.AddCard(cardToTake);

        for(Application mApp : applications.values()){

            int missingWithNewHand = mApp.GetMissingCardsForRelease(myNewHand);
            int missingWithMyHand = mApp.GetMissingCardsForRelease(myActualHand);

            minCardsWithNewHand = Math.min(minCardsWithNewHand, missingWithNewHand);
            minCardsWithMyHand = Math.min(minCardsWithMyHand, missingWithMyHand);
        }
        
        // 3) on renvoi un entier en fonction de la différence entre les deux valeurs mini trouvées

        int absDifference = Math.abs(minCardsWithMyHand - minCardsWithNewHand);
        int eval = 0;
        
        if(absDifference > 0){
            
            if(minCardsWithNewHand > 0){
                int coef_sign = (minCardsWithMyHand - minCardsWithNewHand) / Math.abs(minCardsWithMyHand - minCardsWithNewHand);
                
                eval = coef_sign * 100 / minCardsWithNewHand;
            }else{
                eval = 100;
            }
        }else{
            eval = 1;
        }

        movesToEval.put(move, eval);

        return eval;
    }
    
    public static int EVAL_TASK_PRIORITIZATION(String move) throws Exception{ //TODO: priporiser l'echange de cartes bonus

        String[] tMove = move.split(" ");

        int cardToThrow = Integer.parseInt(tMove[1]);
        int cardToTake = Integer.parseInt(tMove[2]);

        if(totalNeeds[cardToTake] == 0 || totalNeeds[cardToThrow] > 0) return -999;

        CardCollection myNewHand = GetUsefullHand();
        CardCollection myActualHand = GetUsefullHand();

        myNewHand.RemoveCard(cardToThrow);
        myNewHand.AddCard(cardToTake);

        int releasableAppWithMyNewHand = myNewHand.releasableAppsCount();
        int releasableAppWithMyActualHand = myActualHand.releasableAppsCount();

        int eval = -999;

        if(releasableAppWithMyNewHand > releasableAppWithMyActualHand){
            eval = 999;
        }

        movesToEval.put(move, eval);

        return eval;
    }  

    public static int EVAL_ARCHITECTURE_STUDY(String move){
        int eval = 9999 * (1 - myTeam.permanentArchitectureStudyCards);
        movesToEval.put(move, eval);
        return eval;
    }

    public static int EVAL_CODE_REVIEW(String move){
        int eval = 50;
        movesToEval.put(move, eval);
        return eval;
    }

    public static int EVAL_REFACTORING(String move){
        int eval = 10000 * collections.get("HAND").cards[TECHNICAL_DEBT];
        movesToEval.put(move, eval);
        return eval;
    }

    // GENERIC FUNCTIONS

    public static String GetFirstMove(){
        
        int bestZone = 0;

        if(EVAL_ZONE(DAILY_ROUTINE, true) == true){
            bestZone = DAILY_ROUTINE;
        }else{
            bestZone = CONTINUOUS_INTEGRATION;
        }

        return MOVE(bestZone);
    }

    public static int ChooseBonusZone(int startZone){

        boolean[] canRelease = new boolean[10];
        int bestZone = startZone;

        /* 
            TRAINING = 0;
            CODING = 1;
            DAILY_ROUTINE = 2;
            TASK_PRIORITIZATION = 3;
            ARCHITECTURE_STUDY = 4;
            CONTINUOUS_INTEGRATION = 5;
            CODE_REVIEW = 6;
            REFACTORING = 7;
        */        

        switch(startZone){

            case TRAINING:

                canRelease[0] = CanCardRelease(0);
                canRelease[1] = CanCardRelease(1);

                if(!canRelease[0] && !canRelease[1] && remainingCards[7] > 0){
                    bestZone = 7;
                }else if(canRelease[0] || (!canRelease[1] && totalNeeds[0] >= totalNeeds[1])){
                    bestZone = 0;
                }else{
                    bestZone = 1;
                }
                break;

            case CODING:

                canRelease[0] = CanCardRelease(0);
                canRelease[1] = CanCardRelease(1);

                if(!canRelease[0] && !canRelease[1] && remainingCards[2] > 0){
                    bestZone = 2;
                }else if(canRelease[0] || (!canRelease[1] && totalNeeds[0] >= totalNeeds[1])){
                    bestZone = 0;
                }else{
                    bestZone = 1;
                }
                break;

            case DAILY_ROUTINE:

                canRelease[1] = CanCardRelease(1);
                canRelease[3] = CanCardRelease(3);

                if(!canRelease[1] && !canRelease[3] && remainingCards[2] > 0){
                    bestZone = 2;
                }else if(canRelease[1] || (!canRelease[3] && totalNeeds[1] >= totalNeeds[3])){
                    bestZone = 1;
                }else{
                    bestZone = 3;
                }
                break;

            case TASK_PRIORITIZATION:

                canRelease[3] = CanCardRelease(3);
                canRelease[4] = CanCardRelease(4);

                if(!canRelease[3] && !canRelease[4] && remainingCards[2] > 0){
                    bestZone = 2;
                }else if(canRelease[4] || (!canRelease[3] && totalNeeds[4] >= totalNeeds[3]) || (!canRelease[3] && myTeam.permanentArchitectureStudyCards <= 0)){
                    bestZone = 4;
                }else{
                    bestZone = 3;
                }
                break;

            case ARCHITECTURE_STUDY:

                canRelease[3] = CanCardRelease(3);
                canRelease[4] = CanCardRelease(4);            

                if(!canRelease[3] && !canRelease[4] && remainingCards[5] > 0){
                    bestZone = 5;
                }else if(canRelease[4] || (!canRelease[3] && totalNeeds[4] >= totalNeeds[3]) || (!canRelease[3] && myTeam.permanentArchitectureStudyCards <= 0)){
                    bestZone = 4;
                }else{
                    bestZone = 3;
                }
                break;

            case CONTINUOUS_INTEGRATION:

                canRelease[4] = CanCardRelease(4); 
                canRelease[6] = CanCardRelease(6);                            

                if(!canRelease[4] && !canRelease[6] && remainingCards[5] > 0){
                    bestZone = 5;
                }else if(canRelease[4] || (!canRelease[6] && totalNeeds[4] >= totalNeeds[6]) || (!canRelease[6] && myTeam.permanentArchitectureStudyCards <= 0)){
                    bestZone = 4;
                }else{
                    bestZone = 6;
                }
                break;

            case CODE_REVIEW:

                canRelease[6] = CanCardRelease(6); 
                canRelease[7] = CanCardRelease(7);                            

                if(!canRelease[6] && !canRelease[7] && remainingCards[5] > 0){
                    bestZone = 5;
                }else if(canRelease[6] || (!canRelease[7] && totalNeeds[6] >= totalNeeds[7])){
                    bestZone = 6;
                }else{
                    bestZone = 7;
                }
                break;

            case REFACTORING:

                canRelease[6] = CanCardRelease(6); 
                canRelease[0] = CanCardRelease(0);                            

                if(!canRelease[6] && !canRelease[0] && remainingCards[7] > 0){
                    bestZone = 7;
                }else if(canRelease[6] && (!canRelease[0] && totalNeeds[6] >= totalNeeds[0]) || (!canRelease[0] && collections.get("HAND").bonusCardCount() <= 0)){
                    bestZone = 6;
                }else if(canRelease[0]){
                    bestZone = 0;
                }
                break;

            default:
                bestZone = startZone;
        }

        return bestZone;
    }

    public static int Prioritize(int zone0, int zone1, int zone2){

        boolean[] canRelease = new boolean[3];
        int bestZone = zone1;

        canRelease[0] = CanCardRelease(zone0);
        canRelease[1] = CanCardRelease(zone1);
        canRelease[2] = CanCardRelease(zone2);

        if(canRelease[0] || (!canRelease[1] && !canRelease[2] && remainingCards[zone0] > 0 && totalNeeds[zone0] >= totalNeeds[zone1] && totalNeeds[zone0] >= totalNeeds[zone2])){
            bestZone = zone0;
        }else if(remainingCards[zone1] > 0 && (canRelease[1] || (!canRelease[2] && totalNeeds[zone1] >= totalNeeds[zone2]))){
            bestZone = zone1;
        }else{
            bestZone = zone2;
        }

        return bestZone;
    }

    public static int ChooseBonusZone_NEW(int startZone){

        int bestZone = startZone;

        switch(startZone){

            case TRAINING:
                bestZone = Prioritize(REFACTORING, TRAINING, CODING);
                break;

            case CODING:
                bestZone = Prioritize(DAILY_ROUTINE, TRAINING, CODING);
                break;

            case DAILY_ROUTINE:
                bestZone = Prioritize(DAILY_ROUTINE, CODING, TASK_PRIORITIZATION);
                break;

            case TASK_PRIORITIZATION:
                bestZone = Prioritize(ARCHITECTURE_STUDY, DAILY_ROUTINE, TASK_PRIORITIZATION);
                break;

            case ARCHITECTURE_STUDY:
                bestZone = Prioritize(CONTINUOUS_INTEGRATION, ARCHITECTURE_STUDY, TASK_PRIORITIZATION);
                break;

            case CONTINUOUS_INTEGRATION:
                bestZone = Prioritize(CONTINUOUS_INTEGRATION, ARCHITECTURE_STUDY, CODE_REVIEW);
                break;

            case CODE_REVIEW:
                bestZone = Prioritize(CONTINUOUS_INTEGRATION, REFACTORING, CODE_REVIEW);
                break;

            case REFACTORING:
                bestZone = Prioritize(REFACTORING, CODE_REVIEW, TRAINING);
                break;

            default:
                bestZone = startZone;
        }

        return bestZone;
    }

    public static int CanIReleaseAnApp(){

        CardCollection useFullHand = GetUsefullHand();

        for(Application mApp : applications.values()){

            if(mApp.GetMissingCardsForRelease(useFullHand) <= 0) return mApp.id;
        }

        return -1;
    }

    public static int GetDistance(int zoneA, int zoneB){

        int distance = 999;

        if(zoneA != -1 && zoneB != -1){
            distance = Math.abs(zoneA - zoneB);
            distance = Math.min(distance, 8 - distance);
        }

        //System.err.printf("distance between %s and %s = %s\n", zoneA, zoneB, distance);

        return distance;
    }

    public static boolean CanCardRelease(int cardID){
        
        if(remainingCards[cardID] <= 0) return false;

        CardCollection handTemp = GetUsefullHand();
        handTemp.AddCard(cardID);
        return handTemp.releasableAppsCount() > 0;
    }

    public static CardCollection GetUsefullHand(){

        CardCollection usefullHand = new CardCollection(collections.get("HAND"));
        usefullHand.location = "USEF";

        if(collections.containsKey("AUTOMATED")){

            int[] autoCards = collections.get("AUTOMATED").cards;

            for(int i = 0; i < autoCards.length; i++){

                usefullHand.cards[i] += autoCards[i];
            }
        }

        return usefullHand;
    }

    public static CardCollection GetUsefullDraw(){

        if(collections.containsKey("DRAW")){
            return collections.get("DRAW");
        }else if(collections.containsKey("DISCARD")){
            return collections.get("DISCARD");
        }else{
            return null;
        }
    }

    public static CardCollection GetOpponnentCards(){

        CardCollection usefullHand = new CardCollection(collections.get("OPPONENT_CARDS"));
        usefullHand.location = "ALOP";

        if(collections.containsKey("OPPONENT_AUTOMATED")){

            int[] autoCards = collections.get("OPPONENT_AUTOMATED").cards;

            for(int i = 0; i < autoCards.length; i++){

                usefullHand.cards[i] += autoCards[i];
            }
        }

        return usefullHand;
    }

    public static String GetMinValue(Map<String, Integer> mapToEval){

        int minValue = Integer.MAX_VALUE;
        String minKey = "";

        for(String key : mapToEval.keySet()){
            int val = mapToEval.get(key);
            if(val < minValue){
                minValue = val;
                minKey = key;
            }
        }
        return minKey;
    }

    public static String GetMaxValue(Map<String, Integer> mapToEval){

        int maxValue = -Integer.MAX_VALUE;
        String maxKey = "";

        for(String key : mapToEval.keySet()){
            int val = mapToEval.get(key);
            if(val > maxValue){
                maxValue = val;
                maxKey = key;
            }
        }
        return maxKey;
    }    

    // PRINTING FUNCTIONS

    public static int GetCardID(String playAction){

        if(playAction.startsWith("TRAINING"))               return 0;
        if(playAction.startsWith("CODING"))                 return 1;
        if(playAction.startsWith("DAILY_ROUTINE"))          return 2;
        if(playAction.startsWith("TASK_PRIORITIZATION"))    return 3;
        if(playAction.startsWith("ARCHITECTURE_STUDY"))     return 4;
        if(playAction.startsWith("CONTINUOUS_INTEGRATION")) return 5;
        if(playAction.startsWith("CODE_REVIEW"))            return 6;
        if(playAction.startsWith("REFACTORING"))            return 7;

        return -1;
    }

    public static void PRINT_GAME(){
        PrintHeaders();
        PrintCollections();
        PrintRemainingCards();
        PrintApplications();
        PrintPossibleMoves();
    }

    public static void PrintHeaders(){
        
        String[] tableToPrint = new String[CARD_TYPE_COUNT + 3];

        tableToPrint[0] = PadString("Zones", DISPLAY_MARGIN);

        for(int i = 0; i < tableToPrint.length-1; i++){

            tableToPrint[i+1] = String.format("%02d", i);
        }

        System.err.println(String.join(" ", tableToPrint) + "\n");        
    }

    public static void PrintPossibleMoves(){

        System.err.println("PHASE = " + gamePhase + "\n");
        if(gamePhase.startsWith("MOVE")) System.err.printf("daily = %s, architecure = %s\n", myTeam.permanentDailyRoutineCards, myTeam.permanentArchitectureStudyCards);
        System.err.printf("myLoc : %s / oppLoc : %s\n\n", myTeam.location, oppTeam.location);

        for(String move : possibleMoves){

            if(movesToEval.containsKey(move)){

                String[] tableToPrint = new String[2];

                tableToPrint[0] = PadString(move, 25);
                tableToPrint[1] = PadInteger(movesToEval.get(move), 3,true);
    
                System.err.println(String.join(" ", tableToPrint));

            }else{

                System.err.println(move);
            }
        }

        System.err.println(" ");
    }

    public static void PrintRemainingCards(){

        String[] tableToPrint = new String[CARD_TYPE_COUNT + 1];

        tableToPrint[0] = PadString("ZONES", DISPLAY_MARGIN);

        for(int i = 0; i < remainingCards.length; i++){

            tableToPrint[i + 1] = PadInteger(remainingCards[i], 2,true);            
        }

        System.err.println(String.join(" ", tableToPrint) + "\n");
    }

    public static void PrintCollections(){

        
        PrintCollection("PLAYED_CARDS");
        System.err.println(" ");

        allMyCards.Print();
        PrintCollection("AUTOMATED");
        PrintCollection("DISCARD");        
        PrintCollection("DRAW");
        PrintCollection("HAND");

        System.err.println(" ");

        PrintCollection("OPPONENT_AUTOMATED");
        PrintCollection("OPPONENT_CARDS");

        System.err.println(" ");
    }

    public static void PrintCollection(String location){
        if(collections.containsKey(location)) collections.get(location).Print();        
    }

    public static void PrintApplications(){

        String[] tableToPrint = new String[11];

        tableToPrint[0] = PadString("NEEDS", DISPLAY_MARGIN);

        for(int i = 0; i < 10; i++){
            tableToPrint[i+1] = PadInteger(totalNeeds[i], 2, true);
        }

        System.err.println(String.join(" ", tableToPrint) + "\n");

        for(Application mApplication : applications.values()){
            mApplication.Print();
        }

        System.err.println(" ");

    }

    public static String ParseLocation(String mLocation){

        switch(mLocation){
            case"OPPONENT_AUTOMATED":   return "O_AT";
            case"OPPONENT_CARDS":       return "O_CD";
            case"PLAYED_CARDS":         return "PLYD";
            case"AUTOMATED":            return "AUTO";
            case"DISCARD":              return "DISC";
            case"DRAW":                 return "DRAW";
            case"HAND":                 return "HAND";
            default:                    return mLocation;
        }
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

    public static String PadInteger(int mInt, int maxWidth, boolean zerosToPoints){

        String mStr = Integer.toString(mInt);

        if(mStr.length() >= maxWidth){
            
            return mStr;

        }else{

            if(mInt > 0){

                String[] padSpaces = new String[maxWidth - mStr.length()];
                Arrays.fill(padSpaces, "0");
    
                return String.join("", padSpaces) + mStr;

            }else if(mInt < 0){

                int posInt = -mInt;
                mStr = Integer.toString(posInt);

                String[] padSpaces = new String[maxWidth - mStr.length()-1];
                Arrays.fill(padSpaces, "0");
    
                return "-" + String.join("", padSpaces) + mStr;    

            }else{

                String[] padSpaces = new String[maxWidth];
                Arrays.fill(padSpaces, zerosToPoints ? "." : "0");
    
                return String.join("", padSpaces);
            }
        }
    } 
    
    // PARSING FUNCTIONS

    public static void PARSE_PLAY_TYPES(){
        
        playType = new ArrayList<String>();

        playType.add("TRAINING");
        playType.add("CODING");
        playType.add("DAILY_ROUTINE");
        playType.add("TASK_PRIORITIZATION");
        playType.add("ARCHITECTURE_STUDY");
        playType.add("CONTINUOUS_INTEGRATION");
        playType.add("CODE_REVIEW");
        playType.add("REFACTORING");        
    }

    public static void PARSE_POSSIBLE_MOVES(Scanner in){

        possibleMoves = new ArrayList<String>();
        
        int possibleMovesCount = in.nextInt();
        if (in.hasNextLine()) {
            in.nextLine();
        }

        for (int i = 0; i < possibleMovesCount; i++){

            String move = in.nextLine();
            possibleMoves.add(move);
        }
    }

    public static void PARSE_CARDS_COLLECTIONS(Scanner in){

        remainingCards = new int[8];
        Arrays.fill(remainingCards, 5);

        int cardLocationsCount = in.nextInt();
        for (int i = 0; i < cardLocationsCount; i++){
            
            String cardsLocation = in.next(); // the location of the card list. It can be HAND, DRAW, DISCARD or OPPONENT_CARDS (AUTOMATED and OPPONENT_AUTOMATED will appear in later leagues)            
            CardCollection mCardCollection = new CardCollection(in, cardsLocation);

            for(int j = 0; j < remainingCards.length; j++){
                remainingCards[j] -= mCardCollection.cards[j];
            }
            
            collections.put(cardsLocation, mCardCollection);
        }

        remainingCards[DAILY_ROUTINE] -= (myTeam.permanentDailyRoutineCards + oppTeam.permanentDailyRoutineCards);
        remainingCards[ARCHITECTURE_STUDY] -= (myTeam.permanentArchitectureStudyCards + oppTeam.permanentArchitectureStudyCards);

        for(int i = 0; i < 8; i++){
            remainingCompetenceCards += remainingCards[i] ;            
        }

        allMyCards = new CardCollection(collections.get("HAND"));
        allMyCards.location = "AMYC";

        for(CardCollection collection : collections.values()){

            String location = collection.location;

            if(location.equals("AUTOMATED") || location.equals("DISCARD") || location.equals("DRAW")){ //On ne met pas la "HAND" car allMyCard est un clone de "HAND"

                for(int i = 0; i < collection.cards.length; i++){

                    allMyCards.cards[i] += collection.cards[i]; 
                }
            }
        }
    }

    public static void PARSE_TEAMS(Scanner in){

        myTeam = new Team(in, 0);
        oppTeam = new Team(in, 1);        
    }

    public static void PARSE_APPLICATIONS(Scanner in){
        
        int applicationsCount = in.nextInt();
        totalNeeds = new int[10];
        
        for (int i = 0; i < applicationsCount; i++) {
            
            

            Application mApplication = new Application(in);

            for(int j = 0; j < CARD_TYPE_COUNT; j++){

                totalNeeds[j] += mApplication.neededCards[j];
            }

            applications.put(mApplication.id, mApplication);
        }        
    }

    // ACTIONS FUNCTIONS

    public static String THROW(int cardID){
        return "THROW " + cardID;
    }

    public static String GIVE(int cardID){
        return "GIVE " + cardID;
    }

    public static String RELEASE(int appID){
        return "RELEASE " + appID;
    }

    public static String MOVE(int posteID){
        return "MOVE " + posteID;
    }

    public static String MOVE(int zone1, int zone2){
        
        if(zone1 == zone2){
            return "MOVE " + zone1;
        }else{
            return "MOVE " + zone1 + " " + zone2;
        }        
    }    

    public static String WAIT(){
        return "WAIT";
    }

    public static String RANDOM(){
        return "RANDOM";
    }

}