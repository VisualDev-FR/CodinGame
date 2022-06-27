import java.util.*;

/*  IMPROVE LIST

    # Comparaison des meilleurs PLAY_ACTION :
        1) comparer toute les play possibles les unes par rapport aux autres
        2) choisir la meilleure carte à jouer dans chaque catégorie
        3) choisir la meilleure carte à jouer parmi les meillleures   

    # Monter à 4 bonus le plus vite possible, en passant les release si besoin

    # adapter la stratégie de mouvement en fonction du type de départ effectué

    # analyser la possibilité de donner une carte à l'adversaire pour looter du bonus, attention on ne peut pas donner de bonus a l'adversaire, il suffit de voir si on a pas de carte 5

    # si plusieurs releases sont à -100, on va choisir celle qui a le plus de cartes automatisées par l'adversaire, cela revient à faire comme dans la phase play et à évaluer les releases en deux fois

    # dans le GetCardToThrow, prendre en compte l'avantage qu'on peut donner à l'adversaire

    # remmettre le forcing du stop play si on peut release sans dette

    # remmettre en place les evals de TASK et CONTINUOUS sur la phase_play_Release

    # faire une simulation des coups jouables pour accelerer le bonus_looting

    */

/* BUG REPORT
        BOSS BLUE / seed=-5943367980926338000 / mauvais move au tour 56
    */

class Player {

    // PUBLIC VARIABLES

    static Map<Integer, Application> applications;
    static Map<String, CardCollection> collections;
    static Map<String, String> evalsToPrint;
    static Map<String, Integer> releasablesApps;
    static Map<String, Integer> bestActions;
    
    static List<String> commentaries;
    static List<String> possibleMoves;
    static List<String> playType;

    static CardCollection allMyCards;
    
    static Team myTeam;
    static Team oppTeam;

    static int[] remainingCards;
    static int[] totalNeeds;
    
    static String action;
    static String gamePhase;

    static boolean optimalStart;
    
    static int remainingCompetenceCards;    
    static int gameTurn;

    // CONSTANTS

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

    static final int MAX_DAILY_ROUTINE_PLAYABLE = 1;
    static final int MAX_ARCHITECTURE_PLAYABLE = 1;

    static final int MAX_AUTOMATED_CARDS = 4;
    static final int MAX_AUTOMATED_BONUS = 4;
    static final int MAX_AUTOMATED_COMPETENCE = 1;
    static final int MIN_NEED_TO_AUTOMATE = 4;      // if automation doesnot affect the current release and is needed for other release, it will allow the remove one card for the opponent

    static final int MAX_TO_RELEASE = 2;

    public static void main(String args[]) throws Exception {

        Scanner in = new Scanner(System.in);

        PARSE_PLAY_TYPES();

        while (true) {

            evalsToPrint = new HashMap<String, String>();
            applications = new HashMap<Integer, Application>();
            releasablesApps = new HashMap<String, Integer>();
            collections = new HashMap<String, CardCollection>();
            
            possibleMoves = new ArrayList<String>();
            commentaries = new ArrayList<String>();

            PARSE_APPLICATIONS(in);
            PARSE_TEAMS(in);
            PARSE_CARDS_COLLECTIONS(in);
            PARSE_POSSIBLE_MOVES(in);
            
            PARSE_RELEASABLE_APPS();

            int autoBonus = collections.containsKey("AUTOMATED") ? collections.get("AUTOMATED").cards[BONUS_CARD] : 0;

            if(autoBonus < MAX_AUTOMATED_BONUS){
                commentaries.add("PHASE LOOT BONUS");
                LOOT_BONUS();
            }else{
                commentaries.add("PHASE LOOT RELEASE");
                LOOT_RELEASES();
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

        // CONSTRUCTORS

        public CardCollection(CardCollection mCollection){

            location = mCollection.location;
            cards = mCollection.cards.clone();
            proportions = mCollection.proportions.clone();

            countCards();
        }

        public CardCollection(Scanner in, String mLocation){
            
            proportions = new int[CARD_TYPE_COUNT + 2];
            cards = new int[CARD_TYPE_COUNT + 2];
            location = mLocation;

            for(int i = 0; i < cards.length; i++){
                cards[i] = in.nextInt();
                totalCards += cards[i];
            }

        }

        public CardCollection(String mLocation){
            proportions = new int[CARD_TYPE_COUNT + 2];
            cards = new int[CARD_TYPE_COUNT + 2];
            location = mLocation;

            //countCards();
        }

        public CardCollection clone(){
            return new CardCollection(this);            
        }

        // STACK HANDLING

        public void RemoveCard(int cardID) throws Exception{

            if(cards[cardID] > 0){
                cards[cardID] = Math.max(0, cards[cardID] - 1);
                totalCards --;
                
            }else{
                cards = new int[0];
                throw new Exception("Le nombre de cartes est déjà nul ! carteID = " + cardID);                
            }            
        }
        
        public void AddCard(int cardID){
            cards[cardID] += 1;
            totalCards++;
        }

        // COUNT METHODS
        
        public void countCards(){
            totalCards = 0;

            for(int i = 0; i < cards.length; i++){
                totalCards += cards[i];
            }
        }

        public int nonBonusLootableCardCount(){
            return Math.max(0, competenceCardsCount() - cards[CONTINUOUS_INTEGRATION]);
        }

        public int bonusLootableCardCount(){
            
            int bonusCount = 0;

            for(int i = 0; i < cards.length - 1; i++){
                if(i == 8 || i ==5) bonusCount += cards[i];
            }

            return bonusCount;
        }

        public int releasableAppsCount(boolean withDebt){

            int releasableApps = 0;

            for(Application mApp : applications.values()){

                boolean debtIsAcceptable = mApp.GetMissingCardsForRelease(AddAutomatedCards(this)) <= (withDebt ? MAX_TO_RELEASE : 0);

                if(debtIsAcceptable) releasableApps++;
            }
            return releasableApps;
        }        

        public int nonTechnicalDebtCardCount(){
            return totalCards - cards[TECHNICAL_DEBT];
        }

        public int competenceCardsCount(){
            return Math.max(0, totalCards - cards[BONUS_CARD] - cards[TECHNICAL_DEBT]);
        }

        public int technicalDebtCount(){
            return cards[TECHNICAL_DEBT];
        }

        public int bonusCardCount(){
            return cards[BONUS_CARD];
        }

        // PRINTING METHODS

        public String toString(){

            String[] tableToPrint = new String[CARD_TYPE_COUNT + 3]; // +3 car on rajoute le nom de la collection en [0]

            tableToPrint[0]= PadString(ParseLocation(location), DISPLAY_MARGIN);

            for(int j = 0; j < cards.length; j++){

                tableToPrint[j+1] = PadInteger(cards[j], 2,true);
            }

            return String.join(" ", tableToPrint);
        }
    
        public void Print(){
            System.err.println(this.toString());
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

        public boolean isReleasable(boolean withDebt, CardCollection myHand){

            int totalNeeds = totalNeededCards;

            CardCollection usefullHand = AddAutomatedCards(myHand);

            int[] neededCardsTemp = neededCards.clone();
            int[] myCards = usefullHand.cards;            

            for(int i = 0; i < neededCardsTemp.length; i++){

                if(neededCardsTemp[i] > 0 && myCards[i] > 0){
                    
                    totalNeeds -= Math.max(4, 2 * myCards[i]);
                    myCards[i]  = Math.max(0, myCards[i] -  neededCardsTemp[i] / 2);                
                }

                if(withDebt) totalNeeds -= 2 * myCards[i];
            }

            if(withDebt){

                return totalNeeds - 2 * myCards[BONUS_CARD] <= 0;
            
            }else{

                return totalNeeds - myCards[BONUS_CARD] <= 0;
            }
        }

        public boolean isReleaseableByOpponent(){          
            return isReleasable(true, GetOpponnentCards());
        }

        public void Print(){

            String[] tableToPrint = new String[9];

            tableToPrint[0]= PadString("App " + PadInteger(id, 2, false), DISPLAY_MARGIN);

            for(int j = 0; j < 8; j++){
                tableToPrint[j+1] = PadInteger(neededCards[j], 2, true);
            }

            String strReleasable = "X";

            boolean canBeReleased = isReleasable(true, GetMyHand());
            boolean considered = releasablesApps.containsKey(Integer.toString(id));

            if(canBeReleased && considered){
                
                strReleasable = " RELEASABLE    " + releasablesApps.get(Integer.toString(id));
            
            }else if(canBeReleased && !considered){

                strReleasable = "(RELEASABLE)   " + GetMissingCardsForRelease(GetUsefullHand()); // + " > " + MAX_TO_RELEASE;
            }

            System.err.println(String.join(" ", tableToPrint) + " " + strReleasable);
        }
    }

    // SIMULATION FUNCTIONS

    public static class Move{

        Move parent;
        int startPosition;
        int zoneToMove;

        // CONSTRUCTORS

        public Move(int mStart, int mZoneToMove){

            startPosition = mStart;
            zoneToMove = mZoneToMove;
            parent = null;
        }

        public Move(Move mParent, int mZoneToMove){
            startPosition = mParent.zoneToMove;
            zoneToMove = mZoneToMove;
            parent = mParent;
        }

        public Move clone(){
            
            if(parent != null){
                return new Move(startPosition, zoneToMove);
            }else{
                return new Move(parent, zoneToMove);
            }
        }
    
        // PRINTING FUNCTIONS

        public String toString(){

            return String.format("[%s -> %s], parent = [%s -> %s]", startPosition, zoneToMove, parent != null ? parent.startPosition : "null", parent != null ? parent.zoneToMove : "null");
        }

        public void Print(){
            System.err.println(this.toString());
        }
    }

    public static void SimulateMoves(int myPosition, int oppPosition, int deepth, List<Move> simulatedMoves){

        for(int i = 0; i < 8; i++){



        }

    }

    public static int GetNextPosition(int start, int distance){

        if(start + distance <= 7){
            return start + distance;
        }else{
            return start + distance - 8;
        } 
    }

    // RELEASE LOOTING

    public static void LOOT_RELEASES() throws Exception{
        
        switch (gamePhase) {
            case "MOVE":
                PHASE_MOVE_RELEASE();
                break;
            case "GIVE_CARD":
                PHASE_GIVE_RELEASE();
                break;
            case "THROW_CARD":
                PHASE_THROW_RELEASE();
                break;
            case "PLAY_CARD":
                PHASE_PLAY_RELEASES();
                break;
            case "RELEASE":
                PHASE_RELEASE();
                break;
            default:
                RANDOM();
                break;                    
        }
    }

    public static void PHASE_MOVE_RELEASE() throws Exception{

        boolean ignoreRemaining = false;
        boolean zoneFound = false;

        int startZone = myTeam.location;
        int bonusZone = -1;
        int cycles = 0;

        if(gameTurn == 0){

            action = GetFirstMove();

        }else{

            int zoneAbleToRelease = GetZoneAbleToRelease(startZone, myTeam.score < 4); //TODO: valider ou non le forcing du false dans le withDebt

            if(zoneAbleToRelease > -1){
    
                action = MOVE(zoneAbleToRelease); // TODO: complexifier un peu ça, si on a un daily routine, on vient se mettre à côté
                
                int minSideZone = zoneAbleToRelease > 0 ? zoneAbleToRelease - 1 : 7;
                int maxSideZone = zoneAbleToRelease < 7 ? zoneAbleToRelease + 1 : 0;
    
                if(myTeam.permanentDailyRoutineCards > 0){
    
                    if(EVAL_ZONE(minSideZone, true)){
                    
                        action = MOVE(minSideZone, zoneAbleToRelease);
                    
                    }else if(EVAL_ZONE(zoneAbleToRelease, true)){
                    
                        action = MOVE(zoneAbleToRelease);
                    
                    }else{
                    
                        action = MOVE(maxSideZone, zoneAbleToRelease);
                    }
    
                }else{
    
                    action = MOVE(zoneAbleToRelease);
                }
    
            }else{
    
                commentaries.add("Any zone able to release");
    
                while(!zoneFound){
                    
                    ignoreRemaining = cycles > 7;
                    
                    startZone = startZone < 7 ? startZone + 1 : 0;                
                    bonusZone = ChooseBonusZoneForRelease(startZone);                    
                    zoneFound = EVAL_ZONE(startZone, ignoreRemaining);
    
                    cycles++;
                }
    
                action = MOVE(startZone, bonusZone);            
            }
        
        }
    }
    
    public static void PHASE_PLAY_RELEASES() throws Exception{

        Map<String, Integer> playActions = new HashMap<String, Integer>();
        Map<String, Integer> taskActions = new HashMap<String, Integer>();
        Map<String, Integer> contActions = new HashMap<String, Integer>();

        int priority_TRAINING = 1;
        int priority_CODING = 1;
        //int priority_CONTINUOUS = 2;        
        int priority_DAILY = 3;
        int priority_ARCHITECTURE = 4;
        int priority_CODE_REVIEW = 5;
        int priority_REFACTORING = 6;
        //int pirority_TASK = 7;        

        action = WAIT();

        for(String move : possibleMoves){

            int eval = -99;

            switch(move.split(" ")[0]){
                
                case "TRAINING": // 2 draws + 1 more play
                    eval = Integer.MAX_VALUE / priority_TRAINING;
                    break;
                case "CODING": // 1 draw + 2 more plays
                    eval = Integer.MAX_VALUE / priority_CODING;
                    break;
                case "DAILY_ROUTINE":
                    eval = Integer.MAX_VALUE / priority_DAILY;
                    break;
                    
                case "TASK_PRIORITIZATION":
                    taskActions.put(move, EVAL_TASK_PRIORITIZATION(move));
                    break;

                case "ARCHITECTURE_STUDY":
                    eval = Integer.MAX_VALUE / priority_ARCHITECTURE; 
                    break;
                case "CONTINUOUS_INTEGRATION":                
                    contActions.put(move, EVAL_CONTINUOUS_INTEGRATION(move));
                    break;

                case "CODE_REVIEW":
                    eval = Integer.MAX_VALUE / priority_CODE_REVIEW;
                    break;
                case "REFACTORING":
                    eval = Integer.MAX_VALUE / priority_REFACTORING;
                    break;                    
            }

            playActions.put(move, eval);
            evalsToPrint.put(move, Integer.toString(eval));
        }

        // if task_prioritization have been evaluated, we insert the best in the playAction dictionary, and we eval it according to his pirority
        
/*         if(taskActions.size() > 0){

            String bestTask = GetMaxValue(taskActions);

            if(!bestTask.equals("")) playActions.put(bestTask, Integer.MAX_VALUE / pirority_TASK);            
        }

        // IDEM for continuous encountered

        if(contActions.size() > 0){

            String bestCont = GetMaxValue(contActions);

            if(!bestCont.equals("")) playActions.put(bestCont, Integer.MAX_VALUE / priority_CONTINUOUS);            
        }    */     

        // we analyse if the cardPlay make us loose a release occasion

        if(playActions.size() > 0){

            String bestMove = GetMaxValue(playActions);
            int playedCardID = GetCardID(bestMove);

            if(releasablesApps.size() > 0 && playedCardID > -1){ // do we have cards to release ? the playedCard is a competence card ?

                Application releasableApp = applications.get(Integer.parseInt(GetMinValue(releasablesApps)));
                int missingForBestApp = releasablesApps.get(Integer.toString(releasableApp.id));

                // do i have more cards than needed for releasing app ?

                boolean stopPlay = 2 * GetMyHand().cards[playedCardID] <= releasableApp.neededCards[playedCardID]; // we dont look automated cards, because they can't be played, so we can't lose them

                if(playedCardID == 3 && !debtIsNull(missingForBestApp)){

                    if(Integer.parseInt(evalsToPrint.get(bestMove)) >= Integer.MAX_VALUE / applications.size()){
                        commentaries.add(String.format("%s allows to release more apps -> PLAY_FORCING", bestMove));
                        stopPlay = false;
                    }
                
                }

                if(stopPlay){

                    commentaries.add(String.format("%s cancelled, for release app %s", bestMove, releasableApp.id));
                    action = WAIT();
                
                }else{

                    commentaries.add(String.format("playing %s (appId = %s) appNeeds[%s] = %s | myCards[%s] = %s", bestMove, releasableApp.id, playedCardID, releasableApp.neededCards[playedCardID], playedCardID, GetMyHand().cards[playedCardID]));
                    action = bestMove;              
                }
                
            }else{

                commentaries.add(String.format("playing %s, releasable.size = %s | playedCardID = %s", bestMove, releasablesApps.size(), playedCardID));
                action = bestMove;
            }
        
        }        
    }

    public static void PHASE_THROW_RELEASE() throws Exception{

        CardCollection myHand = GetMyHand();

        int minMissingAfterThrow = Integer.MAX_VALUE;
        int bestCardToThrow = -1;

        for(Application app : applications.values()){

            CardCollection myHandAfterThrow = myHand.clone();

            int cardToThrow = GetCardToThrowForRelease(myHand, app);

            myHandAfterThrow.RemoveCard(cardToThrow);

            int missingCardAfterThrow = app.GetMissingCardsForRelease(myHandAfterThrow);

            if(missingCardAfterThrow < minMissingAfterThrow){
                minMissingAfterThrow = missingCardAfterThrow;
                bestCardToThrow = cardToThrow;
            }
        }

        action = THROW(bestCardToThrow);
    }

    public static void PHASE_GIVE_RELEASE() throws Exception{

        CardCollection myHand = GetMyHand();

        int minMissingAfterThrow = Integer.MAX_VALUE;
        int bestCardToThrow = -1;

        for(Application app : applications.values()){

            CardCollection myHandAfterThrow = myHand.clone();

            int cardToThrow = GetCardToThrowForRelease(myHand, app);

            myHandAfterThrow.RemoveCard(cardToThrow);

            int missingCardAfterThrow = app.GetMissingCardsForRelease(myHandAfterThrow);

            if(missingCardAfterThrow < minMissingAfterThrow){
                minMissingAfterThrow = missingCardAfterThrow;
                bestCardToThrow = cardToThrow;
            }
        }

        action = GIVE(bestCardToThrow);
    }

    public static void PHASE_RELEASE(){

        commentaries.add("releasableApps content : " + releasablesApps.toString());

        if(releasablesApps.size() > 0){

            int bestAppID = Integer.parseInt(GetMinValue(releasablesApps));

            action = RELEASE(bestAppID);

        }else{
            action = WAIT();
        }
        
    } 

    public static int GetCardToThrowForRelease(CardCollection myHand_WITHOUT_AUTO, Application appToRelease) throws Exception{

        int cardToThrow = -1;
        int missingForAppToRelease = appToRelease.GetMissingCardsForRelease(AddAutomatedCards(myHand_WITHOUT_AUTO));

        // we search a card wich can avoid the decrease of missingCard for release the appToRelease

        for(int cardID = 0; cardID < appToRelease.neededCards.length; cardID++){

            CardCollection myHand = myHand_WITHOUT_AUTO.clone();

            if(myHand.cards[cardID] > 0){

                myHand.RemoveCard(cardID);

                int minMissing = appToRelease.GetMissingCardsForRelease(AddAutomatedCards(myHand_WITHOUT_AUTO));

                if(minMissing >= missingForAppToRelease){
                    cardToThrow = cardID;
                    break;
                }
            }
        }

        // if we found one we throw it, else we are going to give a card wich is the less needed for apps releasing

        if(cardToThrow > -1){
            return cardToThrow;
        }else if(GetMyHand().bonusCardCount() > 0){
            return BONUS_CARD;
        }else{

            // we search the zone where the needs for app release is mini
            
            int minNeed = Integer.MAX_VALUE;
            int bestCardID = -1;

            int[] oppCards = GetOpponnentCards().cards;

            for(int i = 0; i < 8; i++){

                if(myHand_WITHOUT_AUTO.cards[i] > 0 && (totalNeeds[i] < minNeed || (totalNeeds[i] == minNeed && oppCards[i] >= 2))){
                    minNeed = totalNeeds[i];
                    bestCardID = i;
                }
            }

            return bestCardID;
        }

    }

    public static int ChooseBonusZoneForRelease(int startZone){

        int bestZone = startZone;
        if(myTeam.permanentDailyRoutineCards <= 0) return startZone;

        switch(startZone){

            case TRAINING:
                bestZone = Prioritize(TRAINING, CODING, REFACTORING);
                break;

            case CODING:
                bestZone = Prioritize(TRAINING, CODING, DAILY_ROUTINE);                                
                break;

            case DAILY_ROUTINE:
                bestZone = Prioritize(CODING, TASK_PRIORITIZATION, DAILY_ROUTINE);
                break;

            case TASK_PRIORITIZATION:

                if(remainingCards[3] > 0 && (releasablesApps.size() <= 0 || GetMyHand().cards[5] <= 0)){
                    bestZone = TASK_PRIORITIZATION;
                }else if(remainingCards[4] > 0 && myTeam.permanentArchitectureStudyCards <= 0){
                    bestZone = ARCHITECTURE_STUDY;                 
                }else{
                    bestZone = Prioritize(DAILY_ROUTINE, TASK_PRIORITIZATION, ARCHITECTURE_STUDY);
                }                
                break;

            case ARCHITECTURE_STUDY:                
                bestZone = Prioritize(CONTINUOUS_INTEGRATION, TASK_PRIORITIZATION, ARCHITECTURE_STUDY);
                break;

            case CONTINUOUS_INTEGRATION:
                bestZone = Prioritize(CONTINUOUS_INTEGRATION, CODE_REVIEW, ARCHITECTURE_STUDY);
                break;

            case CODE_REVIEW:
                bestZone = Prioritize(CONTINUOUS_INTEGRATION, CODE_REVIEW, REFACTORING);
                break;

            case REFACTORING:
                bestZone = Prioritize(TRAINING, CONTINUOUS_INTEGRATION, REFACTORING);
                break;

            default:
                bestZone = startZone;
        }

        return bestZone;
    }

    public static boolean debtIsNull(int missingToRelease){
        return !(missingToRelease > 0 || (missingToRelease > -100 && missingToRelease < -50));
    }

    public static int EVAL_TASK_PRIORITIZATION(String move) throws Exception{

        String[] tMove = move.split(" ");

        int eval = -9999;

        int cardToThrow = Integer.parseInt(tMove[1]);
        int cardToTake = Integer.parseInt(tMove[2]);

        if(cardToTake == cardToThrow){
            evalsToPrint.put(move, PadEval(eval));
            return eval;            
        }

        CardCollection myNewHand = GetMyHand(); // we dont add automated cards, it will be done in the calculation of the releasable app below
        CardCollection myActualHand = GetMyHand();

        myNewHand.RemoveCard(TASK_PRIORITIZATION);
        myNewHand.RemoveCard(cardToThrow);
        myNewHand.AddCard(cardToTake);

        int releasableAppWithMyNewHand = myNewHand.releasableAppsCount(myTeam.score < 4);
        int releasableAppWithMyActualHand = myActualHand.releasableAppsCount(myTeam.score < 4);

        if(releasableAppWithMyNewHand > releasableAppWithMyActualHand){
            
            eval = (Integer.MAX_VALUE / applications.size()) * releasableAppWithMyNewHand; // alows to prioritize wich one have the most releasableApps
        
        }else if(releasableAppWithMyActualHand == releasableAppWithMyNewHand){

            int priority_TRAINING = 1;
            int priority_CODING = 1;
            int piority_BONUS = 2;
            int priority_DAILY = 3;
            int priority_ARCHITECTURE = 4;
            int priority_CODE_REVIEW = 5;
            int piority_CONTINUOUS = 6;
            int priority_REFACTORING = 6;
            int pirority_TASK = 7;            

            switch(cardToTake){
                
                case TRAINING:
                    eval = 9999  / priority_TRAINING;
                    break;

                case CODING:
                    eval = 9999  / priority_CODING;
                    break;

                case DAILY_ROUTINE:
                    eval = 9999  / priority_DAILY;
                    break;

                case TASK_PRIORITIZATION:
                    eval = 9999  / pirority_TASK;
                    break;   
                    
                case ARCHITECTURE_STUDY:
                    eval = 9999  / priority_ARCHITECTURE;
                    break;                     

                case CONTINUOUS_INTEGRATION:
                    eval = 9999  / piority_CONTINUOUS;
                    break;

                case CODE_REVIEW:
                    eval = 9999  / priority_CODE_REVIEW;
                    break;      
                    
                case REFACTORING:
                    eval = 9999  / priority_REFACTORING;
                    break;                       

                case BONUS_CARD:
                    eval = 5 / piority_BONUS;
                    break;
                    
                default: break;
            }

        }else{

            commentaries.add(String.format("%s stopped because it decrease the releasableAppCount (before = %s, after = %s)", move, releasableAppWithMyActualHand, releasableAppWithMyNewHand));
            eval = -9999;
        }

        evalsToPrint.put(move, PadEval(eval));

        return eval;
    }    
    
    public static int EVAL_CONTINUOUS_INTEGRATION(String move){

        int eval = -9999;
        int cardID = Integer.parseInt(move.split(" ")[1]);

        eval = 9999 * totalNeeds[cardID];

        evalsToPrint.put(move, PadEval(eval));                
        return eval;
    }
    
    // BONUS LOOTING

    public static void LOOT_BONUS() throws Exception{

        switch (gamePhase) {
            case "MOVE":
                PHASE_MOVE_BONUS();
                break;
            case "GIVE_CARD":
                PHASE_GIVE_BONUS();
                break;
            case "THROW_CARD":
                PHASE_THROW_BONUS();
                break;
            case "PLAY_CARD":
                PHASE_PLAY_BONUS();
                break;
            case "RELEASE":
                action = WAIT();

                if(releasablesApps.size() > 0){
                    
                    for(String appID : releasablesApps.keySet()){

                        int missingCards = releasablesApps.get(appID);
                        
                        if(debtIsNull(missingCards)) action = RELEASE(Integer.parseInt(appID));
                        break;
                    }
                }
                break;
                
            default:
                RANDOM();
                break;                    
        }
    }

    public static void PHASE_RELEASE_BONUS(){

        action = WAIT();

        if(releasablesApps.size() > 0){

            for(String appID : releasablesApps.keySet()){

                int[] cardNeeds = applications.get(Integer.parseInt(appID)).neededCards;
                int[] myCard = GetUsefullHand().cards; // ----------------------------------  ATTENTION, voir si c'est pertinent d'utiliser la main utile

                boolean check_throws = cardNeeds[5] <= 0 || (cardNeeds[5] > 0 && myCard[5] > 1);

                if(check_throws) action = RELEASE(Integer.parseInt(appID));
            }
        }
    }

    public static void PHASE_MOVE_BONUS() throws Exception{

        int autoBonus = collections.containsKey("AUTOMATED") ? collections.get("AUTOMATED").cards[BONUS_CARD] : 0;

        boolean canIGive = allMyCards.bonusCardCount() >= (MAX_AUTOMATED_BONUS - autoBonus);;
        //boolean canIThrow = allMyCards.bonusCardCount() >= (MAX_AUTOMATED_BONUS - autoBonus -1);
        //boolean canIThrowAndGive = GetMyHand().nonBonusLootableCardCount() >= 2; // the two available cards will be thrown at the administration desk, so we will have 0 cards to give to the opponent
        boolean canILoot = GetMyHand().cards[5] > 0 && GetMyHand().cards[8] > 0;

        if(gameTurn == 0){
        
            action = GetFirstMove();

        }else{

            if(myTeam.permanentDailyRoutineCards == 0){ // on va essayer de récup un DAILY le plus vite possible, et de récup un max de BONUS / de CONTINUOUS
                
                switch(myTeam.location){ 

                    case TRAINING:

                        if(canILoot && canIGive || EVAL_ZONE(CODING, true)){
                            action = MOVE(CODING);
                        }else{
                            action = GetNextMoveForBonus();
                        }
                        break;

                    case CODING:

                        if(canIGive || EVAL_ZONE(ARCHITECTURE_STUDY, true)){
                            action = MOVE(ARCHITECTURE_STUDY);
                        }else{
                            action = GetNextMoveForBonus();
                        }                    
                        break;
        
                    case DAILY_ROUTINE:

                        if(canIGive || EVAL_ZONE(ARCHITECTURE_STUDY, true)){
                            action = MOVE(ARCHITECTURE_STUDY);
                        }else{
                            action = GetNextMoveForBonus();
                        }  
                        break;

                    case TASK_PRIORITIZATION:

                        if(canIGive || EVAL_ZONE(ARCHITECTURE_STUDY, true)){
                            action = MOVE(ARCHITECTURE_STUDY);
                        }else{
                            action = GetNextMoveForBonus();
                        }  
                        break;

                    case ARCHITECTURE_STUDY:

                        if(canIGive || EVAL_ZONE(CONTINUOUS_INTEGRATION, true)){ // on ignore s'il reste des cartes, car au pire ça bloquera l'adversaire
                            action = MOVE(CONTINUOUS_INTEGRATION);
                        }else{
                            action = GetNextMoveForBonus();
                        }                     
                        break;
        
                    case CONTINUOUS_INTEGRATION:

                        if(canIGive || EVAL_ZONE(CODE_REVIEW, true)){ // on ignore s'il reste des cartes, car au pire ça bloquera l'adversaire
                            action = MOVE(CODE_REVIEW);
                        }else{ // on ne regarde pas les cartes importantes, car ici on a de grandes chances d'avoir déjà joué
                            action = GetNextMoveForBonus();
                        }                     
                        break;
        
                    case CODE_REVIEW:

                        if(canILoot && canIGive || EVAL_ZONE(REFACTORING, true)){
                            action = MOVE(REFACTORING);
                        }else if(canIGive || EVAL_ZONE(TRAINING, true)){ // on ignore s'il reste des cartes, car au pire ça bloquera l'adversaire
                            action = MOVE(TRAINING);
                        }else{ // on ne regarde pas les cartes importantes, car ici on a de grandes chances d'avoir déjà joué
                            action = GetNextMoveForBonus();
                        }                       
                    case REFACTORING:
                        action = GetNextMoveForBonus();
                        break;
        
                    default:
                        action = RANDOM();
                }  
                
            }else{ 

                switch(myTeam.location){

                    case TRAINING:
                        
                        if(remainingCards[0] > 0 && (canIGive || EVAL_ZONE(CODING, true))){ // on bouge direct sur daily routine, car ça permet de se positionner en optimalStart
                            action = MOVE(CODING, TRAINING);
                        }if(remainingCards[1] > 0 && (canIGive || EVAL_ZONE(CODING, true))){ // on bouge direct sur daily routine, car ça permet de se positionner en optimalStart
                            action = MOVE(CODING);
                        }else{
                            action = GetNextMoveForBonus();
                        }
                        break;

                    case CODING:

                        if(remainingCards[2] > 0 && (canIGive || EVAL_ZONE(DAILY_ROUTINE, true))){ // on bouge direct sur daily routine, car ça permet de se positionner en optimalStart
                            action = MOVE(DAILY_ROUTINE);
                        }else{
                            action = GetNextMoveForBonus();
                        }                     
                        break;
        
                    case DAILY_ROUTINE:

                        if(remainingCards[4] > 0 && (canIGive || EVAL_ZONE(TASK_PRIORITIZATION, false))){ // le canIgive sert à voir si on peut échanger une carte qui ne nous intéresse pas
                            action = MOVE(TASK_PRIORITIZATION, ARCHITECTURE_STUDY);
                        }else{
                            action = GetNextMoveForBonus();
                        }
                        
                        commentaries.add(String.format("Choosen location = %s\n", action));
                        break;
        
                    case TASK_PRIORITIZATION:

                        if(remainingCards[5] > 0 && (canIGive || EVAL_ZONE(ARCHITECTURE_STUDY, true))){ 
                            action = MOVE(ARCHITECTURE_STUDY, CONTINUOUS_INTEGRATION);
                        }else if(canIGive || EVAL_ZONE(CONTINUOUS_INTEGRATION, true)){
                            action = MOVE(CONTINUOUS_INTEGRATION);
                        }else if(remainingCards[5] > 0 && (canIGive || EVAL_ZONE(CODE_REVIEW, true))){
                            action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                        }else{
                            action = GetNextMoveForBonus();
                        }                                  
                        break;
        
                    case ARCHITECTURE_STUDY:

                        if(remainingCards[5] > 0 && (canIGive || EVAL_ZONE(CONTINUOUS_INTEGRATION, true))){
                            action = MOVE(CONTINUOUS_INTEGRATION);
                        }else if(remainingCards[5] > 0 && (canIGive || EVAL_ZONE(CODE_REVIEW, true))){
                            action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                        }else{
                            action = GetNextMoveForBonus();
                        }                     
                        break;
        
                    case CONTINUOUS_INTEGRATION:
                        if(remainingCards[5] > 0 && (canIGive || EVAL_ZONE(CODE_REVIEW, true))){
                            action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                        }else{
                            action = GetNextMoveForBonus();
                        }                    
                        break;
        
                    case CODE_REVIEW:

                        if(remainingCards[0] > 0 && (canIGive || EVAL_ZONE(REFACTORING, true))){
                            action = MOVE(REFACTORING, TRAINING);
                        }else{
                            action = GetNextMoveForBonus();
                        }
                        break;

                    case REFACTORING:
                        if(remainingCards[0] > 0 && (canIGive || EVAL_ZONE(TRAINING, true))){
                            action = MOVE(TRAINING);
                        }else{
                            action = GetNextMoveForBonus();
                        }
                        break;
        
                    default:
                        action = RANDOM();
                } 

                
            }
        }
    }

    public static void PHASE_PLAY_BONUS() throws Exception{

        Map<String, Integer> moves = new HashMap<String, Integer>();

        for(String move : possibleMoves){

            int eval = -99;

            int priority_TRAINING = 1;
            int priority_CODING = 1;
            int piority_CONTINUOUS_AUTO = 2;
            int priority_DAILY = 3;
            int priority_ARCHITECTURE = 4;
            int priority_CODE_REVIEW = 5;
            int piority_CONTINUOUS_COMP = 6;
            int priority_REFACTORING = 6;
            int pirority_TASK = 7;

            switch(move.split(" ")[0]){
                
                case "TRAINING": // 2 draws + 1 more play
                    eval = Integer.MAX_VALUE / priority_TRAINING;
                    break;
                case "CODING": // 1 draw + 2 more plays
                    eval = Integer.MAX_VALUE / priority_CODING;
                    break;
                case "DAILY_ROUTINE":

                    if(myTeam.permanentDailyRoutineCards < MAX_DAILY_ROUTINE_PLAYABLE){
                        eval = Integer.MAX_VALUE / priority_DAILY;
                    }
                    break;
                    
                case "TASK_PRIORITIZATION":
                    
                    int cardToThrow = Integer.parseInt(move.split(" ")[1]);
                    int cardToTake = Integer.parseInt(move.split(" ")[2]);

                    if(cardToTake != cardToThrow){

                        if(cardToTake == 5 && cardToThrow != 5){
                            eval = Integer.MAX_VALUE / pirority_TASK;
                        }else if(cardToTake == 8 && cardToThrow != 5){
                            eval = Integer.MAX_VALUE / pirority_TASK;
                        }else if(myTeam.permanentDailyRoutineCards < MAX_DAILY_ROUTINE_PLAYABLE && cardToTake == 2 && cardToThrow != 5 && cardToThrow != 8){
                            eval = Integer.MAX_VALUE / pirority_TASK;
                        }else if(GetUsefullDraw().cards[BONUS_CARD] <= 0 && cardToTake == 6){
                            eval = Integer.MAX_VALUE / pirority_TASK;
                        }
                    }

                    commentaries.add(String.format("eval : take %s throw %s = %s\n", cardToTake, cardToThrow, eval));
                    break;

                case "ARCHITECTURE_STUDY":
                    if(myTeam.permanentArchitectureStudyCards < MAX_ARCHITECTURE_PLAYABLE){
                        eval = Integer.MAX_VALUE / priority_ARCHITECTURE;
                    }
                    break;
                case "CONTINUOUS_INTEGRATION":
                
                    int autoCardID = Integer.parseInt(move.split(" ")[1]);
                    int autoCardCount = collections.containsKey("AUTOMATED") ? collections.get("AUTOMATED").cards[autoCardID] : 0;

                    if(autoCardID == BONUS_CARD){
                        eval = Integer.MAX_VALUE / piority_CONTINUOUS_AUTO;
                    }else if(autoCardID != 5 && (autoCardID != 2 || myTeam.permanentDailyRoutineCards >= 2) && autoCardID != 6 && autoCardCount < MAX_AUTOMATED_COMPETENCE){
                        eval = Integer.MAX_VALUE / piority_CONTINUOUS_COMP;
                    }
                    break;

                case "CODE_REVIEW":
                    eval = Integer.MAX_VALUE / priority_CODE_REVIEW;
                    break;
                case "REFACTORING":
                    eval = Integer.MAX_VALUE / priority_REFACTORING;
                    break;                    
            }

            moves.put(move, eval);
            evalsToPrint.put(move, Integer.toString(eval));
        }

        if(moves.size() > 0){
            
            String bestMove = GetMaxValue(moves);
            
            if(!bestMove.equals("") && moves.get(bestMove) > 0){
                action = bestMove;
            }else{                
                action = WAIT();
            }

        }else{
            action = WAIT();
        } 
    }

    public static void PHASE_THROW_BONUS(){
        
        int cardToThrow = GetCardToThrowForBonus(GetMyHand());
        
        action = THROW(cardToThrow);
    }

    public static void PHASE_GIVE_BONUS(){
        
        int cardToThrow = GetCardToThrowForBonus(GetMyHand());
        
        action = GIVE(cardToThrow);
    }

    public static String GetNextMoveForBonus(){

        boolean zoneFound = false;
        boolean canIgive = GetMyHand().nonBonusLootableCardCount() >= 1;

        int startZone = myTeam.location;
        int bonusZone = startZone;

        while(!zoneFound){

            startZone = startZone < 7 ? startZone + 1 : 0;                
            bonusZone = ChooseBonusZone_BONUS(startZone);                    
            zoneFound = startZone != myTeam.location && (canIgive || EVAL_ZONE(startZone, true)); // dans la phase de loot, on s'en fou d'avoir un bonus, au contraire
        }

        //System.err.printf("final move = %s\n", startZone);
        
        action = MOVE(startZone, bonusZone);  
        
        return action;
    }

    public static String GetFirstMove(){
        
        int bestZone = 0;

        if(EVAL_ZONE(DAILY_ROUTINE, true) == true){
            bestZone = DAILY_ROUTINE;
            optimalStart = true;
        }else{
            bestZone = CONTINUOUS_INTEGRATION;
            optimalStart = false;
        }

        return MOVE(bestZone);
    }

    public static int ChooseBonusZone_BONUS(int startZone){

        int bestZone = startZone;
        if(myTeam.permanentDailyRoutineCards <= 0) return startZone;

        switch(startZone){

            case TRAINING:
                bestZone = Prioritize(TRAINING, CODING, REFACTORING);
                break;

            case CODING:
                bestZone = Prioritize(TRAINING, CODING, DAILY_ROUTINE);                                
                break;

            case DAILY_ROUTINE:
                bestZone = Prioritize(CODING, TASK_PRIORITIZATION, DAILY_ROUTINE);                
                break;

            case TASK_PRIORITIZATION:

                if(remainingCards[4] > 0 && myTeam.permanentArchitectureStudyCards <= 0){
                    bestZone = ARCHITECTURE_STUDY;                 
                }else{
                    bestZone = Prioritize(TASK_PRIORITIZATION, ARCHITECTURE_STUDY, DAILY_ROUTINE);
                }               
                break;

            case ARCHITECTURE_STUDY:
                
                if(myTeam.permanentArchitectureStudyCards <= 0){
                    bestZone = Prioritize(CONTINUOUS_INTEGRATION, ARCHITECTURE_STUDY, TASK_PRIORITIZATION);
                }else{
                    bestZone = Prioritize(CONTINUOUS_INTEGRATION, TASK_PRIORITIZATION, ARCHITECTURE_STUDY);
                }
                break;

            case CONTINUOUS_INTEGRATION:

                if(myTeam.permanentArchitectureStudyCards <= 0){
                    bestZone = Prioritize(CONTINUOUS_INTEGRATION, ARCHITECTURE_STUDY, CODE_REVIEW);
                }else{
                    bestZone = Prioritize(CONTINUOUS_INTEGRATION, CODE_REVIEW, ARCHITECTURE_STUDY);
                }
                break;

            case CODE_REVIEW:
                
                bestZone = Prioritize(CONTINUOUS_INTEGRATION, CODE_REVIEW, REFACTORING);
                break;

            case REFACTORING:

                bestZone = Prioritize(TRAINING, CODE_REVIEW, REFACTORING);
                break;

            default:
                bestZone = startZone;
        }

        return bestZone;
    }

    public static int GetCardToThrowForBonus(CardCollection myHand_WITHOUT_AUTO){

        // we search a card wich doesn't participate to the bonus_looting

        int[] myCards = myHand_WITHOUT_AUTO.cards;

        if(myHand_WITHOUT_AUTO.nonBonusLootableCardCount() > 0){
            
            for(int i = 0; i < 9; i++){

                if(i != CONTINUOUS_INTEGRATION && i != BONUS_CARD && myCards[i] > 0) return i;
            }
        
        }else if(myHand_WITHOUT_AUTO.bonusCardCount() > 0){

            return BONUS_CARD;

        }

        // we search the zone where the needs for app release is mini / where the opponent has already 
        
        int minNeed = Integer.MAX_VALUE;
        int bestCardID = -1;

        int[] oppCards = GetOpponnentCards().cards;

        for(int i = 0; i < 9; i++){

            if(myHand_WITHOUT_AUTO.cards[i] > 0 && (totalNeeds[i] < minNeed || (totalNeeds[i] == minNeed && oppCards[i] >= 2))){
                minNeed = totalNeeds[i];
                bestCardID = i;
            }
        }

        return bestCardID;
    }

    // MOVE FUNCTIONS

    public static int GetZoneAbleToRelease(int startZone, boolean withDebt) throws Exception{

        int bestZone = -1;
        int minScore = withDebt ? MAX_TO_RELEASE : 0;
        int appID = -1;

        for(int i = 0; i < 8; i++){

            int zoneID = i;

            if(zoneID != myTeam.location){

                CardCollection myHand = new CardCollection(collections.get("HAND")); // on ne prends pas les cartes automatisée car cela fausserait les résultats des procédures appelées plus bas
            
                if(remainingCards[zoneID] > 0){
                    myHand.AddCard(zoneID);
                }else{
                    myHand.AddCard(BONUS_CARD);
                }
    
                for(Application app : applications.values()){

                    CardCollection myHandAfterMove = GetMyHandAfterMove_RELEASE(GetMyHand(), zoneID, app);                    
                    
                    if(myHandAfterMove != null){

                        int missingToRelease = app.GetMissingCardsForRelease(AddAutomatedCards(myHandAfterMove));

                        if(missingToRelease <= minScore){
                            minScore = missingToRelease;
                            bestZone = zoneID;
                            appID = app.id;
                        }
                        
                    }
                }
            }
        }

        if(bestZone > -1) commentaries.add(String.format("Zone %s able to release app %s with %s Debts", bestZone, appID, minScore));

        return bestZone;
    }

    public static CardCollection GetMyHandAfterMove_RELEASE(CardCollection myHand, int zoneToMove, Application appToRelease) throws Exception{

        if(zoneToMove == myTeam.location || remainingCards[zoneToMove] <= 0) return null; //return false;

        CardCollection myHandTemp = myHand.clone();
        CardCollection myHandAfterThrow = myHand.clone();; //boolean canIMove = false;

        if(GetDistance(zoneToMove, oppTeam.location) > 1){

            if(zoneToMove < myTeam.location){ // we can go to zoneToMove without throwing any card
                
                myHandAfterThrow = GetMyHandAfterThrow_RELEASE(myHandTemp, appToRelease); 
            
            }

        }else if(zoneToMove > myTeam.location){ // we need to give one card to the opponent
            
            myHandAfterThrow = GetMyHandAfterGive_RELEASE(myHandTemp, appToRelease); // we have to give one card to release
        
        }else{ // we need to throw two cards to the admin desk, and to give one card to the opponent

            myHandAfterThrow = GetMyHandAfterThrow_RELEASE(myHandTemp, appToRelease);
            myHandAfterThrow = GetMyHandAfterGive_RELEASE(myHandAfterThrow, appToRelease);        
        }

        return myHandAfterThrow;
    }

    public static CardCollection GetMyHandAfterThrow_RELEASE(CardCollection myHand, Application appToRelease) throws Exception{

        CardCollection myHandAfterThrow = myHand.clone();
        int[] myCards = myHandAfterThrow.cards;

        if(myHand.nonTechnicalDebtCardCount() <= 0){

            myHandAfterThrow.AddCard(TECHNICAL_DEBT);
            myHandAfterThrow.AddCard(TECHNICAL_DEBT);
            
            return myHandAfterThrow;
        
        }else if(myHand.nonTechnicalDebtCardCount() == 1){
            
            int cardToThrow = -1;

            for(int i = 0; i < myCards.length - 1; i++){
                if(myCards[i] > 0) cardToThrow = i; 
            }

            myHandAfterThrow.RemoveCard(cardToThrow);
            myHandAfterThrow.AddCard(TECHNICAL_DEBT);

            return myHandAfterThrow;
        
        }else{

            for(int i = 0; i < 2; i++){ // on boucle deux fois pour retirer deux cartes

                int cardToThrow = GetCardToThrowForRelease(myHandAfterThrow, appToRelease);
                myHandAfterThrow.RemoveCard(cardToThrow);
            }

            return myHandAfterThrow;
        
        }
        
    }

    public static CardCollection GetMyHandAfterGive_RELEASE(CardCollection myHand, Application appToRelease) throws Exception{

        CardCollection myHandAfterThrow = myHand.clone();

        if(myHand.nonTechnicalDebtCardCount() <= 0){

            myHandAfterThrow.AddCard(TECHNICAL_DEBT);
            
            return myHandAfterThrow;
        
        }else{

            int cardToThrow = GetCardToThrowForRelease(myHandAfterThrow, appToRelease);

            myHandAfterThrow.RemoveCard(cardToThrow);

            return myHandAfterThrow;        
        }

    }

    public static boolean EVAL_ZONE(int zoneToMove, boolean ignoreRemainingCards){

        boolean check_Remaining  = remainingCards[zoneToMove] > 0 || ignoreRemainingCards;
        boolean check_Distance = GetDistance(zoneToMove, oppTeam.location) > 1;
        boolean check_Possible = myTeam.location != zoneToMove;

        boolean zoneFound = check_Distance && check_Remaining && check_Possible; //&& check_Needs

        evalsToPrint.put(MOVE(zoneToMove), String.format("%S %S %S", check_Remaining, check_Distance, check_Possible));

        return zoneFound;       
    }
        
    // GENERIC FUNCTIONS

    public static int Prioritize(int zone1, int zone2, int zone3){
        
        int bestZone = -1;
        
        if(remainingCards[zone1] > 0){            
            bestZone = zone1;        
        }else if(remainingCards[zone2] > 0){
            bestZone = zone2;        
        }else{            
            bestZone = zone3;
        }

        return bestZone;
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

    public static int GetCardID(String playAction){

        if(playAction.startsWith("TRAINING"))               return TRAINING;
        if(playAction.startsWith("CODING"))                 return CODING;
        if(playAction.startsWith("DAILY_ROUTINE"))          return DAILY_ROUTINE;
        if(playAction.startsWith("TASK_PRIORITIZATION"))    return TASK_PRIORITIZATION;
        if(playAction.startsWith("ARCHITECTURE_STUDY"))     return ARCHITECTURE_STUDY;
        if(playAction.startsWith("CONTINUOUS_INTEGRATION")) return CONTINUOUS_INTEGRATION;
        if(playAction.startsWith("CODE_REVIEW"))            return CODE_REVIEW;
        if(playAction.startsWith("REFACTORING"))            return REFACTORING;

        return -1;
    }
    
    // COLLECTION FUNCTIONS

    public static CardCollection GetUsefullHand(){
        return AddAutomatedCards(GetMyHand());
    }

    public static CardCollection AddAutomatedCards(CardCollection myHand){

        CardCollection usefullHand = new CardCollection(myHand);
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
            return new CardCollection(collections.get("DRAW"));
        }else if(collections.containsKey("DISCARD")){
            return new CardCollection(collections.get("DISCARD"));
        }else{
            return new CardCollection("USEFULL_DRAW");
        }
    }

    public static CardCollection GetMyHand(){
        return collections.get("HAND").clone();
    }    

    public static CardCollection GetOpponnentCards(){

        CardCollection usefullHand = collections.get("OPPONENT_CARDS").clone();
        usefullHand.location = "ALOP";

        if(collections.containsKey("OPPONENT_AUTOMATED")){

            int[] autoCards = collections.get("OPPONENT_AUTOMATED").cards;

            for(int i = 0; i < autoCards.length; i++){

                usefullHand.cards[i] += autoCards[i];
            }
        }

        return usefullHand;
    }
    
    // PRINTING FUNCTIONS

    public static void PRINT_GAME(){

        System.err.println(action + "\n");

        PrintHeaders();
        PrintCollections();        
        PrintApplications();
        PrintCommentaries();
        PrintPossibleMoves();
    }

    public static void PrintCommentaries(){

        commentaries.add(String.format("PHASE = %s, MAX_TO_RELEASE = %s\n\n", gamePhase, MAX_TO_RELEASE));
        commentaries.add(String.format("releasable apps : %s", releasablesApps.toString()));

        for(String commentary : commentaries){
            System.err.println(commentary);
        }
        
    }

    public static void PrintHeaders(){
        
        String[] tableToPrint = new String[CARD_TYPE_COUNT + 3];

        tableToPrint[0] = PadString("ZONES", DISPLAY_MARGIN);

        for(int i = 0; i < tableToPrint.length-1; i++){

            tableToPrint[i+1] = String.format("%02d", i);
        }

        System.err.println(String.join(" ", tableToPrint)); 
        
        PrintRemainingCards();
        
        System.err.println(" ");        
    }

    public static void PrintPossibleMoves(){

        for(String move : possibleMoves){

            if(evalsToPrint.containsKey(move)){

                String[] tableToPrint = new String[2];

                tableToPrint[0] = move;
                tableToPrint[1] = evalsToPrint.get(move);
    
                System.err.println(String.join(" ", tableToPrint));

            }else{

                System.err.println(move);
            }
        }

        System.err.println(" ");
    }

    public static void PrintRemainingCards(){

        String[] tableToPrint = new String[CARD_TYPE_COUNT + 1];

        tableToPrint[0] = PadString("REMAIN", DISPLAY_MARGIN);

        for(int i = 0; i < remainingCards.length; i++){

            tableToPrint[i + 1] = PadInteger(remainingCards[i], 2,true);            
        }

        System.err.println(String.join(" ", tableToPrint));
    }

    public static void PrintCollections(){

        CardCollection usefullHand = GetUsefullHand();
        
        PrintCollection("OPPONENT_AUTOMATED");
        PrintCollection("OPPONENT_CARDS");

        System.err.println(" ");

        PrintCollection("PLAYED_CARDS");
        PrintCollection("DISCARD");
        PrintCollection("DRAW");
        PrintCollection("HAND");
        PrintCollection("AUTOMATED");
        usefullHand.Print();

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
    
    public static String PadEval(int eval){
        return PadInteger(eval, 3,true);        
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
        collections.put(allMyCards.location, allMyCards);

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

        commentaries.add(String.format("SCORE : %s", myTeam.score));
        commentaries.add(String.format("DAILY : %s", myTeam.permanentDailyRoutineCards));
        commentaries.add(String.format("ARCHI : %s", myTeam.permanentArchitectureStudyCards));
        commentaries.add(String.format("MYLOC : %s", myTeam.location));
        commentaries.add(String.format("OPLOC : %s", oppTeam.location));
        
    }

    public static void PARSE_APPLICATIONS(Scanner in){

        gamePhase = in.next();
        
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

    public static void PARSE_RELEASABLE_APPS(){
        
        for(Application app : applications.values()){

            int missingToRelease = app.GetMissingCardsForRelease(GetUsefullHand());

            boolean appIsReleasable = app.isReleasable(myTeam.score < 4, GetMyHand());
            boolean debtIsAcceptable = myTeam.score < 4 ? missingToRelease <= MAX_TO_RELEASE : missingToRelease <= 0;

            missingToRelease -= app.isReleaseableByOpponent() ? 100 : 0;

            if(appIsReleasable && debtIsAcceptable) releasablesApps.put(Integer.toString(app.id), missingToRelease);
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

    public static void CRASH(){
        int i = 0;
        while(true){
            System.err.println("CRASH " + i);
            i++;
        }
    }

}