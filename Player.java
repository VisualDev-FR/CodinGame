import java.time.ZoneId;
import java.util.*;

/*  

    # IMPROVE LIST

    * retirer certains déplacement si on a pas de cartes compétences en main, car sinon on va perdre un tour (ex : si on va sur 3 ou 5 sans carte à prioriser, à automatiser on ne pourra pas jouer et on perdra un tour)
    * dans GetZoneAbleToRelease(), renvoyer toute les zones capables de release, et faire l'évaluation en dehors de la méthode
    * dans la phase PLAY_RELEASE, prendre la décision de jouer ou d'attendre dans une méthode spécifique
    * améliorer la phase MOVE_RELEASE

    # BUG REPORT
    
    * BOSS BLUE / seed=-5943367980926338000 / mauvais move au tour 56

*/

class Player {

    // PUBLIC VARIABLES

    static Map<Integer, Application> applications;
    static Map<String, CardCollection> collections;
    static Map<String, String> evalsToPrint;
    static Map<String, Integer> releasablesApps;
    static Map<String, Integer> bestActions;
    
    static List<String> comments;
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
    static int cycles;

    // CONSTANTS

    static final String CARDS_OPP_AUTO = "OPPONENT_AUTOMATED";
    static final String CARDS_OPP_CARDS = "OPPONENT_CARDS";
    static final String CARDS_PLAYED = "PLAYED_CARDS";
    static final String CARDS_AUTO = "AUTOMATED";
    static final String CARDS_DISCARD = "DISCARD";
    static final String CARDS_DRAW = "DRAW";
    static final String CARDS_HAND = "HAND";

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

    static final int MAX_AUTOMATED_BONUS = 3;
    static final int MAX_AUTOMATED_COMPETENCE = 1;
    static final int MIN_NEED_TO_AUTOMATE = 4;      // if automation doesnot affect the current release and is needed for other release, it will allow the remove one card for the opponent

    static final int MAX_TO_RELEASE = 4;

    public static void main(String args[]) throws Exception {

        Scanner in = new Scanner(System.in);

        PARSE_PLAY_TYPES();

        while (true) {

            evalsToPrint = new HashMap<String, String>();
            applications = new HashMap<Integer, Application>();
            releasablesApps = new HashMap<String, Integer>();
            collections = new HashMap<String, CardCollection>();
            
            possibleMoves = new ArrayList<String>();
            comments = new ArrayList<String>();

            PARSE_APPLICATIONS(in);
            PARSE_TEAMS(in);
            PARSE_CARDS_COLLECTIONS(in);
            PARSE_POSSIBLE_MOVES(in);
            
            PARSE_RELEASABLE_APPS();

            int autoBonus = collections.containsKey("AUTOMATED") ? collections.get("AUTOMATED").cards[BONUS_CARD] : 0;

            if(autoBonus < MAX_AUTOMATED_BONUS){
                comments.add("PHASE LOOT BONUS");
                LOOT_BONUS();
            }else{
                comments.add("PHASE LOOT RELEASE");
                LOOT_RELEASES();
            }

            PRINT_GAME();

            if(gamePhase == "MOVE") COUNT_CYCLES(action);

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

                boolean debtIsAcceptable = false;

                int missingToRelease = mApp.GetMissingCardsForRelease(this);
                
                if(withDebt){
                    debtIsAcceptable = missingToRelease <= MAX_TO_RELEASE;
                }else{
                    debtIsAcceptable = debtIsNull(missingToRelease);
                }

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

        public int needsAutoByOpponent(){

            if(collections.get(CARDS_OPP_AUTO) == null){
                
                return 0;
            }else{

                int totalNeeds = 0;

                for (int i = 0; i < neededCards.length; i++){
                    if(neededCards[i] > 0 && collections.get(CARDS_OPP_AUTO).cards[i] > 0) totalNeeds++;
                }
    
                return totalNeeds;
            }


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
                action = WAIT();
                break;                    
        }
    }

    public static void PHASE_MOVE_RELEASE() throws Exception{

        int startZone = myTeam.location;

        if(gameTurn == 0){

            action = GetFirstMove();

        }else{

            Map<String, Integer> zoneAbleToRelease = GetZoneAbleToRelease(startZone, myTeam.score < 4); //TODO: valider ou non le forcing du false dans le withDebt

            if(zoneAbleToRelease.size() > 0){

                int minMissingToRelease = Integer.MAX_VALUE;
                int bestZone = Integer.parseInt(GetNextMoveForRelease().split(" ")[1]);
    
                for(String strKey : zoneAbleToRelease.keySet()){

                    int zoneID = Integer.parseInt(strKey.split(":")[0]);
                    int appID = Integer.parseInt(strKey.split(":")[1]);
                    int missingToRelease = zoneAbleToRelease.get(strKey);

                    missingToRelease += applications.get(appID).isReleaseableByOpponent() ? -100 : 0;

                    boolean canIgive = EVAL_ZONE(zoneID, true) || myTeam.score == 4 || zoneAbleToRelease.size() == 1;

                    if(missingToRelease < minMissingToRelease && canIgive){
                        
                        bestZone = zoneID;
                        minMissingToRelease = missingToRelease;
                    
                    }else if(missingToRelease == minMissingToRelease && zoneID < bestZone && canIgive){

                        bestZone = zoneID;
                    
                    }else if(missingToRelease > minMissingToRelease && !EVAL_ZONE(bestZone, true) && EVAL_ZONE(zoneID, true)){

                        bestZone = zoneID;
                    }
                }

                action = MOVE(bestZone);
    
            }else{
    
                comments.add("Any zone able to release");

                int autoBonus = collections.containsKey("AUTOMATED") ? collections.get("AUTOMATED").cards[8] : 0;

                if(myTeam.permanentDailyRoutineCards == 0){ // on va essayer de récup un DAILY le plus vite possible, et de récup un max de BONUS / de CONTINUOUS

                    if(EVAL_ZONE(CONTINUOUS_INTEGRATION, false) && (GetMyHand().bonusCardCount() > 0 && myTeam.location < 5)){
                        
                        action = MOVE(CONTINUOUS_INTEGRATION);
                    
                    }else if(GetMyHand().nonTechnicalDebtCardCount() > 0 && EVAL_ZONE(TASK_PRIORITIZATION, false) && myTeam.location < 3){

                        action = MOVE(TASK_PRIORITIZATION);

                    }else if(allMyCards.bonusCardCount() - autoBonus <= 0 && EVAL_ZONE(CODE_REVIEW, false) && myTeam.location < 6){

                        action = MOVE(CODE_REVIEW);

                    }else{

                        action = GetNextMoveForRelease();
                    }

                }else if(EVAL_ZONE(CONTINUOUS_INTEGRATION, false) && (GetMyHand().bonusCardCount() > 0 && myTeam.location <= 5)){

                    if(EVAL_ZONE(ARCHITECTURE_STUDY, true)){
                        action = MOVE(ARCHITECTURE_STUDY, CONTINUOUS_INTEGRATION);
                    }else if(EVAL_ZONE(TASK_PRIORITIZATION, true)){
                        action = MOVE(CONTINUOUS_INTEGRATION);
                    }else if(EVAL_ZONE(CODE_REVIEW, true)){
                        action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                    }

                }else if(remainingCards[3] > 0 && oppTeam.location != 3 && GetMyHand().nonTechnicalDebtCardCount() > 0 && myTeam.location <= 3){
    
    
                    if(EVAL_ZONE(DAILY_ROUTINE, true)){
                        action = MOVE(DAILY_ROUTINE, TASK_PRIORITIZATION);
                    }else if(EVAL_ZONE(TASK_PRIORITIZATION, true)){
                        action = MOVE(TASK_PRIORITIZATION);
                    }else if(EVAL_ZONE(ARCHITECTURE_STUDY, true)){
                        action = MOVE(ARCHITECTURE_STUDY, TASK_PRIORITIZATION);
                    }
             
    
                }else if(remainingCards[6] > 0 && allMyCards.cards[CODE_REVIEW] <= 0 && allMyCards.bonusCardCount() - autoBonus <= 0 && oppTeam.location != 6){ // if we dont have any bonus in hand or in draw, we try to get a code review as fast as possible

                    if(EVAL_ZONE(CONTINUOUS_INTEGRATION, true)){
                        action = MOVE(CONTINUOUS_INTEGRATION, CODE_REVIEW);
                    }else if(EVAL_ZONE(CODE_REVIEW, true)){
                        action = MOVE(CODE_REVIEW);
                    }else if(EVAL_ZONE(REFACTORING, true)){
                        action = MOVE(REFACTORING, CODE_REVIEW);
                    }
    
                }else{ 
                    
                    action = GetNextMoveForRelease();
                }                      
            }       
        }
    }

    public static String GetNextMoveForRelease(){

        boolean ignoreRemaining = false;
        boolean zoneFound = false;

        int startZone = myTeam.location;
        int bonusZone = -1;
        int cycles = 0;
    
        while(!zoneFound){
                    
            ignoreRemaining = cycles > 7;
            startZone = startZone < 7 ? startZone + 1 : 0;

            boolean blankMove = ignoreRemaining || myTeam.permanentDailyRoutineCards > 0 || GetMyHand().nonTechnicalDebtCardCount() > 0 || (startZone != 3 && startZone != 5);

            bonusZone = ChooseBonusZoneForRelease(startZone);
            zoneFound = EVAL_ZONE(startZone, ignoreRemaining) && blankMove;
            
            cycles++;
        }

        comments.add("zone choosen = " + startZone);

        action = MOVE(startZone, bonusZone); 
        
        return action;
        
    }
    
    public static void PHASE_PLAY_RELEASES() throws Exception{

        Map<String, Integer> playActions = new HashMap<String, Integer>();
        Map<String, Integer> taskActions = new HashMap<String, Integer>();
        Map<String, Integer> contActions = new HashMap<String, Integer>();

        int priority_TRAINING = 2;
        int priority_CODING = 2;        
        int priority_CONTINUOUS = GetMyHand().bonusCardCount() > 0 ? 1 : 3;
        int priority_CODE_REVIEW = allMyCards.bonusCardCount() - collections.get("AUTOMATED").cards[8] <= 0 ? 1 : 4;
        int priority_REFACTORING = 5;        
        int priority_TASK = 5;   
        int priority_DAILY = myTeam.score == 4 ? 2 : 6; 
        int priority_ARCHITECTURE = myTeam.score == 4 ? 2 : 6;
              

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
                    //eval = Integer.MAX_VALUE / priority_TASK;                   //FLAG 
                    break;
                case "ARCHITECTURE_STUDY":
                    eval = Integer.MAX_VALUE / priority_ARCHITECTURE; 
                    break;
                case "CONTINUOUS_INTEGRATION":                
                    contActions.put(move, EVAL_CONTINUOUS_INTEGRATION(move));
                    //eval = Integer.MAX_VALUE / priority_CONTINUOUS;             //FLAG 
                    break;
                case "CODE_REVIEW":
                    eval = Integer.MAX_VALUE / priority_CODE_REVIEW;
                    break;
                case "REFACTORING":
                    eval = Integer.MAX_VALUE / priority_REFACTORING;
                    break;                    
            }

            if(!move.equals("RANDOM") && !move.startsWith("TASK") && !move.startsWith("CONTINUOUS")){
                playActions.put(move, eval);
                evalsToPrint.put(move, Integer.toString(eval));
            }
        }

        // if task_prioritization have been evaluated, we insert the best in the playAction dictionary, and we eval it according to his pirority
        
         if(taskActions.size() > 0){

            String bestTask = GetMaxValue(taskActions);

            if(taskActions.get(bestTask) > 0 ){
                playActions.put(bestTask, Integer.MAX_VALUE / priority_TASK);  
            }          
        }

        // IDEM for continuous encountered

        if(contActions.size() > 0){

            String bestCont = GetMaxValue(contActions);

            if(contActions.get(bestCont) > 0) playActions.put(bestCont, Integer.MAX_VALUE / priority_CONTINUOUS);            
        }     

        // we analyse if the cardPlay make us loose a release occasion

        if(playActions.size() > 0){

            while(playActions.size() > 0){

                String bestMove = GetMaxValue(playActions);
                int playedCardID = GetCardID(bestMove);
    
                if(releasablesApps.size() > 0 && playedCardID > -1){ // do we have cards to release ? the playedCard is a competence card ?
    
                    Application releasableApp = applications.get(Integer.parseInt(GetMinValue(releasablesApps)));
                    int missingForBestApp = releasablesApps.get(Integer.toString(releasableApp.id));
    
                    // do i have more cards than needed for releasing app ?
    
                    boolean stopPlay = 2 * GetMyHand().cards[playedCardID] <= releasableApp.neededCards[playedCardID]; // we dont look automated cards, because they can't be played, so we can't lose them
    
                    if(playedCardID == 3 && !debtIsNull(missingForBestApp)){
    
                        if(Integer.parseInt(evalsToPrint.get(bestMove)) >= Integer.MAX_VALUE / applications.size()){
                            comments.add(String.format("%s allows to release more apps -> PLAY_FORCING", bestMove));
                            stopPlay = false;
                        }
                    
                    }
    
                    if(stopPlay && (!bestMove.equals("CONTINUOUS_INTEGRATION 8") || myTeam.score == 4)){
    
                        comments.add(String.format("%s cancelled, for release app %s", bestMove, releasableApp.id));
                        playActions.remove(bestMove);
                    
                    }else{
    
                        comments.add(String.format("playing %s (appId = %s) appNeeds[%s] = %s | myCards[%s] = %s", bestMove, releasableApp.id, playedCardID, releasableApp.neededCards[playedCardID], playedCardID, GetMyHand().cards[playedCardID]));
                        action = bestMove;
                        break;
                    }
                    
                }else{
    
                    comments.add("Any card to play");
                    action = bestMove;
                    break;
                }    
            }
        }        
    }

    public static void PHASE_THROW_RELEASE() throws Exception{

        CardCollection myHand = GetMyHand();

        int minMissingAfterThrow = Integer.MAX_VALUE;
        int bestCardToThrow = -1;

        if(GetMyHand().bonusCardCount() > 0 && GetMyHand().cards[5] > 0 && GetMyHand().nonTechnicalDebtCardCount() > 2 && collections.get("AUTOMATED").cards[8] < 4){
        
            bestCardToThrow = GetCardToThrowForBonus(GetMyHand());
        
        }else{

            for(Application app : applications.values()){

                CardCollection myHandAfterThrow = myHand.clone();
    
                int cardToThrow = GetCardToThrowForRelease(myHand, app);
    
                myHandAfterThrow.RemoveCard(cardToThrow);
    
                int missingAfterThrow = app.GetMissingCardsForRelease(myHandAfterThrow);
    
                if(missingAfterThrow < minMissingAfterThrow){
                    minMissingAfterThrow = missingAfterThrow;
                    bestCardToThrow = cardToThrow;
                }
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

        comments.add("releasableApps content : " + releasablesApps.toString());

        if(releasablesApps.size() > 0){

            int bestAppID = -1;
            int minScore = Integer.MAX_VALUE;
            int maxAutoByOpponent = 0;

            for(String strAppID : releasablesApps.keySet()){

                int appID = Integer.parseInt(strAppID);
                int score = releasablesApps.get(strAppID);
                int needsAutoByOpponent = applications.get(appID).needsAutoByOpponent();              
                
                if(score < minScore || (score == minScore && needsAutoByOpponent > maxAutoByOpponent)){
                    minScore = score;
                    bestAppID = appID;
                    maxAutoByOpponent = needsAutoByOpponent;
                }                         
            }

            action = RELEASE(bestAppID);

        }else{
            action = WAIT();
        }        
    } 

    public static int ChooseBonusZoneForRelease(int startZone){

        int bestZone = startZone;
        if(myTeam.permanentDailyRoutineCards <= 0) return startZone;

        switch(startZone){            

            case TRAINING:
                bestZone = PrioritizeForRelease(TRAINING, CODING, REFACTORING);                
                break;

            case CODING:
                bestZone = PrioritizeForRelease(TRAINING, CODING, DAILY_ROUTINE);                                
                break;

            case DAILY_ROUTINE:
                
                if(GetMyHand().nonTechnicalDebtCardCount() > 0 && remainingCards[3] > 0 && (releasablesApps.size() <= 0 || GetMyHand().cards[5] <= 0)){
                    bestZone = TASK_PRIORITIZATION;
                }else{
                    bestZone = PrioritizeForRelease(CODING, DAILY_ROUTINE, TASK_PRIORITIZATION);
                }                  
                break;

            case TASK_PRIORITIZATION:

                if(GetMyHand().nonTechnicalDebtCardCount() > 0 && remainingCards[3] > 0 && (releasablesApps.size() <= 0 || GetMyHand().cards[5] <= 0)){
                    bestZone = TASK_PRIORITIZATION;
                }else if(remainingCards[4] > 0 && myTeam.permanentArchitectureStudyCards <= 0){
                    bestZone = ARCHITECTURE_STUDY;                 
                }else{
                    bestZone = PrioritizeForRelease(DAILY_ROUTINE, ARCHITECTURE_STUDY, TASK_PRIORITIZATION);
                }                
                break;

            case ARCHITECTURE_STUDY:                
                if(GetMyHand().nonTechnicalDebtCardCount() > 0){
                    bestZone = PrioritizeForRelease(CONTINUOUS_INTEGRATION, TASK_PRIORITIZATION, ARCHITECTURE_STUDY);
                }else{
                    bestZone = PrioritizeForRelease(ARCHITECTURE_STUDY, TASK_PRIORITIZATION, ARCHITECTURE_STUDY);
                }
                break;

            case CONTINUOUS_INTEGRATION:
                bestZone = PrioritizeForRelease(CONTINUOUS_INTEGRATION, CODE_REVIEW, ARCHITECTURE_STUDY);
                break;

            case CODE_REVIEW:
                bestZone = PrioritizeForRelease(CONTINUOUS_INTEGRATION, CODE_REVIEW, REFACTORING);
                break;

            case REFACTORING:
                bestZone = PrioritizeForRelease(TRAINING, CONTINUOUS_INTEGRATION, REFACTORING);
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

        int releasableAppWithMyNewHand = AddAutomatedCards(myNewHand).releasableAppsCount(myTeam.score < 4);
        int releasableAppWithMyActualHand = AddAutomatedCards(myActualHand).releasableAppsCount(myTeam.score < 4);

        if(releasableAppWithMyNewHand > releasableAppWithMyActualHand){
            
            eval = (Integer.MAX_VALUE / applications.size()) * releasableAppWithMyNewHand; // alows to prioritize wich one have the most releasableApps
        
        }/* else if(releasableAppWithMyActualHand == releasableAppWithMyNewHand){

                        
                int piority_CONTINUOUS = 1;
                int pirority_TASK = 2;     
                int priority_TRAINING = 3;
                int priority_CODING = 3;
                int priority_DAILY = 4;
                int priority_ARCHITECTURE = 4;
                int priority_CODE_REVIEW = 6;            
                int priority_REFACTORING = 7;                   

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

                    default: break;
                }

        } */else{

            comments.add(String.format("%s stopped because it doesn't increase the releasableAppCount (before = %s, after = %s)", move, releasableAppWithMyActualHand, releasableAppWithMyNewHand));
            eval = -9999;
        }

        evalsToPrint.put(move, PadEval(eval));

        return eval;
    }    
    
    public static int EVAL_CONTINUOUS_INTEGRATION(String move){

        int eval = -9999;
        int cardID = Integer.parseInt(move.split(" ")[1]);

        if(cardID == BONUS_CARD){
            eval = Integer.MAX_VALUE;
        }else{
            eval = 9999 * totalNeeds[cardID];
        }        

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

                if(releasablesApps.size() > 0 && myTeam.permanentDailyRoutineCards <= 0 && myTeam.permanentArchitectureStudyCards <= 0){                    
                    PHASE_RELEASE();
                }
                break;
                
            default:
                RANDOM();
                break;                    
        }
    }

    public static void PHASE_MOVE_BONUS() throws Exception{

        int autoBonus = collections.containsKey("AUTOMATED") ? collections.get("AUTOMATED").cards[BONUS_CARD] : 0;

        //boolean canILoot = GetMyHand().cards[5] > 0 && GetMyHand().cards[8] > 0;

        if(gameTurn == 0){
        
            action = GetFirstMove();

        }else{

            if(myTeam.permanentDailyRoutineCards == 0){ // on va essayer de récup un DAILY le plus vite possible, et de récup un max de BONUS / de CONTINUOUS

                if(EVAL_ZONE(DAILY_ROUTINE, false) && myTeam.location < 2 || myTeam.location == 7){
                    action = MOVE(DAILY_ROUTINE);
                }else if(EVAL_ZONE(CONTINUOUS_INTEGRATION, false) && myTeam.location < 5 && allMyCards.cards[CONTINUOUS_INTEGRATION] <= 0){
                    action = MOVE(CONTINUOUS_INTEGRATION);
                }else if(EVAL_ZONE(CODE_REVIEW, false) && myTeam.location < 6 && allMyCards.cards[BONUS_CARD] - autoBonus <= 0){
                    action = MOVE(CODE_REVIEW);
                }else{
                    action = GetNextMoveForBonus();
                }
                
            }else if(remainingCards[5] > 0 && allMyCards.cards[CONTINUOUS_INTEGRATION] <= 0 && oppTeam.location != 5){

                if(myTeam.location < 4){

                    if(EVAL_ZONE(ARCHITECTURE_STUDY, true)){
                        action = MOVE(ARCHITECTURE_STUDY, CONTINUOUS_INTEGRATION);
                    }else if(EVAL_ZONE(CONTINUOUS_INTEGRATION, true)){
                        action = MOVE(CONTINUOUS_INTEGRATION);
                    }else if(EVAL_ZONE(CODE_REVIEW, true)){
                        action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                    }else{
                        action = MOVE(ARCHITECTURE_STUDY, CONTINUOUS_INTEGRATION);
                    }
                    
                }else{

                    switch(myTeam.location){

                        case ARCHITECTURE_STUDY:
                            action = MOVE(CONTINUOUS_INTEGRATION);
                            break;

                        case CONTINUOUS_INTEGRATION:
                            action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                            break;

                        case REFACTORING:
                        case CODE_REVIEW:
                            action = MOVE(ARCHITECTURE_STUDY, CONTINUOUS_INTEGRATION);                        
                            break;

                    }                    
                }                

            }else if(remainingCards[6] > 0 && allMyCards.cards[CODE_REVIEW] <= 0 && allMyCards.bonusCardCount() - autoBonus <= 0 && oppTeam.location != 6){ // if we dont have any bonus in hand or in draw, we try to get a code review as fast as possible
                
                if(myTeam.location < 5){

                    if(EVAL_ZONE(CONTINUOUS_INTEGRATION, true)){
                        action = MOVE(CONTINUOUS_INTEGRATION, CODE_REVIEW);
                    }else if(EVAL_ZONE(CODE_REVIEW, true)){
                        action = MOVE(CODE_REVIEW);
                    }else if(EVAL_ZONE(REFACTORING, true)){
                        action = MOVE(REFACTORING, CODE_REVIEW);
                    }else{
                        action = MOVE(CONTINUOUS_INTEGRATION, CODE_REVIEW);
                    }
                    
                }else{

                    switch(myTeam.location){

                        case CONTINUOUS_INTEGRATION:
                            action = MOVE(CODE_REVIEW);
                            break;

                        case CODE_REVIEW:
                            action = MOVE(REFACTORING, CODE_REVIEW);                        
                            break;

                        case REFACTORING:
                            action = MOVE(ARCHITECTURE_STUDY, CONTINUOUS_INTEGRATION);   
                            break;
                    }                    
                }

            }else{ 

                switch(myTeam.location){

                    case TRAINING:
                        
                        if(remainingCards[0] > 0 && EVAL_ZONE(CODING, true)){ // on bouge direct sur daily routine, car ça permet de se positionner en optimalStart
                            action = MOVE(CODING, TRAINING);
                        }if(remainingCards[1] > 0 && EVAL_ZONE(CODING, true)){ // on bouge direct sur daily routine, car ça permet de se positionner en optimalStart
                            action = MOVE(CODING);
                        }else{
                            action = GetNextMoveForBonus();
                        }
                        break;

                    case CODING:

                        if(remainingCards[3] > 0 && EVAL_ZONE(DAILY_ROUTINE, true)){ // on bouge direct sur daily routine, car ça permet de se positionner en optimalStart
                            action = MOVE(DAILY_ROUTINE, TASK_PRIORITIZATION);
                        }else{
                            action = GetNextMoveForBonus();
                        }                     
                        break;
        
                    case DAILY_ROUTINE:

                        if(remainingCards[4] > 0 && EVAL_ZONE(TASK_PRIORITIZATION, false) && myTeam.permanentArchitectureStudyCards < MAX_ARCHITECTURE_PLAYABLE){ // le canIgive sert à voir si on peut échanger une carte qui ne nous intéresse pas
                            action = MOVE(TASK_PRIORITIZATION, ARCHITECTURE_STUDY);
                        }else if(remainingCards[3] > 0 && EVAL_ZONE(TASK_PRIORITIZATION, true)){ // on bouge direct sur daily routine, car ça permet de se positionner en optimalStart
                            action = MOVE(TASK_PRIORITIZATION);
                        }else{
                            action = GetNextMoveForBonus();
                        }
                        break;
        
                    case TASK_PRIORITIZATION:

                        if(remainingCards[5] > 0){

                            if (EVAL_ZONE(CONTINUOUS_INTEGRATION, true)){
                                action = MOVE(CONTINUOUS_INTEGRATION);
                            }else if(EVAL_ZONE(ARCHITECTURE_STUDY, true)){ 
                                action = MOVE(ARCHITECTURE_STUDY, CONTINUOUS_INTEGRATION);
                            }else if(EVAL_ZONE(CODE_REVIEW, true)){
                                action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                            }else{
                                action = GetNextMoveForBonus();
                            }

                        }else if(remainingCards[4] > 0 && myTeam.permanentArchitectureStudyCards < MAX_ARCHITECTURE_PLAYABLE){
                            action = MOVE(ARCHITECTURE_STUDY);
                        }else if(remainingCards[3] > 0 && EVAL_ZONE(ARCHITECTURE_STUDY, true)){
                            action = MOVE(ARCHITECTURE_STUDY, TASK_PRIORITIZATION);
                        }else{
                            action = GetNextMoveForBonus();
                        }                                  
                        break;
        
                    case ARCHITECTURE_STUDY:

                        if(remainingCards[5] > 0 && EVAL_ZONE(CONTINUOUS_INTEGRATION, true)){
                            action = MOVE(CONTINUOUS_INTEGRATION);
                        }else if(remainingCards[5] > 0 && EVAL_ZONE(CODE_REVIEW, true)){
                            action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                        }else{
                            action = GetNextMoveForBonus();
                        }                     
                        break;
        
                    case CONTINUOUS_INTEGRATION:
                        if(remainingCards[5] > 0 && EVAL_ZONE(CODE_REVIEW, true)){
                            action = MOVE(CODE_REVIEW, CONTINUOUS_INTEGRATION);
                        }else if(remainingCards[6] > 0 && allMyCards.bonusCardCount() - autoBonus <= (4 - autoBonus) && EVAL_ZONE(CODE_REVIEW, true)){
                            action = MOVE(CODE_REVIEW);
                        }else{
                            action = GetNextMoveForBonus();
                        }                    
                        break;
        
                    case CODE_REVIEW:

                        if(remainingCards[6] > 0 && allMyCards.bonusCardCount() - autoBonus <= (4 - autoBonus) && EVAL_ZONE(REFACTORING, true)){
                            action = MOVE(REFACTORING, CODE_REVIEW);
                        }else if(remainingCards[0] > 0 && EVAL_ZONE(REFACTORING, true)){
                            action = MOVE(REFACTORING, TRAINING);
                        }else{
                            action = GetNextMoveForBonus();
                        }
                        break;

                    case REFACTORING:
                        if(remainingCards[0] > 0 && EVAL_ZONE(TRAINING, true)){
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

                    comments.add(String.format("eval : take %s throw %s = %s\n", cardToTake, cardToThrow, eval));
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
        boolean canIgive = false; //GetMyHand().nonBonusLootableCardCount() >= 1;

        int startZone = myTeam.location;
        int bonusZone = startZone;

        while(!zoneFound){

            startZone = startZone < 7 ? startZone + 1 : 0;                
            bonusZone = ChooseBonusZone_BONUS(startZone);                    
            zoneFound = startZone != myTeam.location && (canIgive || EVAL_ZONE(startZone, true)); // dans la phase de loot, on s'en fou d'avoir un bonus, au contraire
        }

        action = MOVE(startZone, bonusZone);  
        
        return action;
    }

    public static String GetFirstMove(){
        
        return DAILY_START();
        //return CONTIUNOUS_START();

/*         int bestZone = 0;

        if(EVAL_ZONE(DAILY_ROUTINE, true) == true){
            bestZone = DAILY_ROUTINE;
            optimalStart = true;
        }else if(EVAL_ZONE(ARCHITECTURE_STUDY, true) == true){
            bestZone = ARCHITECTURE_STUDY;
        }else{
            bestZone = CONTINUOUS_INTEGRATION;
            optimalStart = false;
        }

        return MOVE(bestZone);  */

    }

    public static String DAILY_START(){
        
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

    public static String CONTIUNOUS_START(){

        int bestZone = 0;

        if(EVAL_ZONE(CONTINUOUS_INTEGRATION, true) == true){
            bestZone = CONTINUOUS_INTEGRATION;
            optimalStart = true;
        }else{
            bestZone = DAILY_ROUTINE;
            optimalStart = false;
        }

        return MOVE(bestZone);        
        
    }    

    public static int ChooseBonusZone_BONUS(int startZone){

        int bestZone = startZone;
        if(myTeam.permanentDailyRoutineCards <= 0) return startZone;

        switch(startZone){

            case TRAINING:
                bestZone = PrioritizeForBonus(TRAINING, CODING, REFACTORING);
                break;

            case CODING:
                bestZone = PrioritizeForBonus(TRAINING, CODING, DAILY_ROUTINE);                                
                break;

            case DAILY_ROUTINE:
                bestZone = PrioritizeForBonus(CODING, TASK_PRIORITIZATION, DAILY_ROUTINE);                
                break;

            case TASK_PRIORITIZATION:

                if(remainingCards[4] > 0 && myTeam.permanentArchitectureStudyCards <= 0){
                    bestZone = ARCHITECTURE_STUDY;                 
                }else{
                    bestZone = PrioritizeForBonus(TASK_PRIORITIZATION, ARCHITECTURE_STUDY, DAILY_ROUTINE);
                }               
                break;

            case ARCHITECTURE_STUDY:
                
                if(myTeam.permanentArchitectureStudyCards <= 0){
                    bestZone = PrioritizeForBonus(CONTINUOUS_INTEGRATION, ARCHITECTURE_STUDY, TASK_PRIORITIZATION);
                }else{
                    bestZone = PrioritizeForBonus(CONTINUOUS_INTEGRATION, TASK_PRIORITIZATION, ARCHITECTURE_STUDY);
                }
                break;

            case CONTINUOUS_INTEGRATION:

                if(myTeam.permanentArchitectureStudyCards <= 0){
                    bestZone = PrioritizeForBonus(CONTINUOUS_INTEGRATION, ARCHITECTURE_STUDY, CODE_REVIEW);
                }else{
                    bestZone = PrioritizeForBonus(CONTINUOUS_INTEGRATION, CODE_REVIEW, ARCHITECTURE_STUDY);
                }
                break;

            case CODE_REVIEW:
                
                bestZone = PrioritizeForBonus(CONTINUOUS_INTEGRATION, CODE_REVIEW, REFACTORING);
                break;

            case REFACTORING:

                bestZone = PrioritizeForBonus(TRAINING, CODE_REVIEW, REFACTORING);
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

        for(int i = 0; i < 8; i++){

            if(myHand_WITHOUT_AUTO.cards[i] > 0 && (totalNeeds[i] < minNeed || (totalNeeds[i] == minNeed && oppCards[i] >= 2))){
                minNeed = totalNeeds[i];
                bestCardID = i;
            }
        }

        return bestCardID;
    }

    // MOVE FUNCTIONS

    public static int GetZoneAbleToRelease_OLD(int startZone, boolean withDebt) throws Exception{

        int bestZone = -1;
        int minScore = withDebt ? MAX_TO_RELEASE : 1;
        int appID = -1;

        for(int i = 1; i < 8; i++){

            int zoneID = (startZone + i) % 8;

            if(zoneID != myTeam.location){ // && (GetDistance(zoneID, oppTeam.location) > 1 || myTeam.score == 4)){

                CardCollection myHand = new CardCollection(collections.get("HAND")); // on ne prends pas les cartes automatisée car cela fausserait les résultats des procédures appelées plus bas

                //comments.add(String.format("%s : zone %s", AddAutomatedCards(myHand).toString(), zoneID));

                for(Application app : applications.values()){

                    CardCollection myHandAfterMove = GetMyHandAfterMove_RELEASE(myHand, zoneID, app);  

                    if(remainingCards[zoneID] > 0){
                        myHandAfterMove.AddCard(zoneID);
                    }else{
                        myHandAfterMove.AddCard(BONUS_CARD);
                    }                    

                    int missingToRelease = app.GetMissingCardsForRelease(AddAutomatedCards(myHandAfterMove));

                    if(zoneID == 7) comments.add(String.format("%s : zone %s / missing for app %s = %s", AddAutomatedCards(myHandAfterMove).toString(), zoneID, app.id, missingToRelease));

                    if(missingToRelease < minScore || (missingToRelease == minScore && zoneID < bestZone)){
                        minScore = missingToRelease;
                        bestZone = zoneID;
                        appID = app.id;
                    }                    
                }
            }
        }

        if(bestZone > -1) comments.add(String.format("Zone %s able to release app %s with %s Debts", bestZone, appID, minScore));

        return bestZone;
    }

    public static Map<String, Integer> GetZoneAbleToRelease(int startZone, boolean withDebt) throws Exception{

        Map<String, Integer> zonesAbleToRelease = new HashMap<String, Integer>();

        int minScore = withDebt ? MAX_TO_RELEASE : 0;
 
        for(int i = 1; i < 8; i++){

            int zoneID = (startZone + i) % 8;

            if(zoneID != myTeam.location){ // && (GetDistance(zoneID, oppTeam.location) > 1 || myTeam.score == 4)){

                CardCollection myHand = new CardCollection(collections.get("HAND")); // on ne prends pas les cartes automatisée car cela fausserait les résultats des procédures appelées plus bas

                //comments.add(String.format("%s : zone %s", AddAutomatedCards(myHand).toString(), zoneID));

                for(Application app : applications.values()){

                    CardCollection myHandAfterMove = GetMyHandAfterMove_RELEASE(myHand, zoneID, app);  

                    if(remainingCards[zoneID] > 0){
                        myHandAfterMove.AddCard(zoneID);
                    }else{
                        myHandAfterMove.AddCard(BONUS_CARD);
                    }                    

                    int missingToRelease = app.GetMissingCardsForRelease(AddAutomatedCards(myHandAfterMove));
                    
                    if(missingToRelease <= minScore){
                        zonesAbleToRelease.put(String.format("%s:%s", zoneID, app.id), missingToRelease);
                        comments.add(String.format("Zone %s able to release app %s with %s debt", zoneID, app.id, missingToRelease));
                    }
                }
            }
        }

        if(zonesAbleToRelease.size() > 0) comments.add(String.format("%s zones able to release", zonesAbleToRelease.size()));

        return zonesAbleToRelease;
    }

    public static CardCollection GetMyHandAfterMove_RELEASE(CardCollection myHand, int zoneToMove, Application appToRelease) throws Exception{

        CardCollection myHandAfterThrow = myHand.clone(); //boolean canIMove = false;

        if(GetDistance(zoneToMove, oppTeam.location) > 1){

            if(zoneToMove < myTeam.location){ // we only need to throw 2 cards, else we dont need to throw any card and we return the same hand given in parameters
                
                myHandAfterThrow = GetMyHandAfterThrow_RELEASE(myHandAfterThrow, appToRelease); 
            
            }

        }else if(zoneToMove > myTeam.location){ // we only need to give one card to the opponent
            
            myHandAfterThrow = GetMyHandAfterGive_RELEASE(myHandAfterThrow, appToRelease); // we have to give one card to release
        
        }else{ // we need to throw two cards to the admin desk, and to give one card to the opponent

            myHandAfterThrow = GetMyHandAfterThrow_RELEASE(myHandAfterThrow, appToRelease);
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

    public static boolean EVAL_ZONE(int zoneToMove, boolean ignoreRemainingCards){

        boolean check_Remaining  = remainingCards[zoneToMove] > 0 || ignoreRemainingCards;
        boolean check_Distance = GetDistance(zoneToMove, oppTeam.location) > 1;
        boolean check_Possible = myTeam.location != zoneToMove;

        boolean zoneFound = check_Distance && check_Remaining && check_Possible; //&& check_Needs

        evalsToPrint.put(MOVE(zoneToMove), String.format("%S %S %S", check_Remaining, check_Distance, check_Possible));

        return zoneFound;       
    }
        
    // GENERIC FUNCTIONS

    public static int PrioritizeForBonus(int zone1, int zone2, int zone3){
        
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

    public static int PrioritizeForRelease(int zone1, int zone2, int zone3){
        
        int bestZone = -1;

        int zone1Count = totalNeeds[zone1];
        int zone2Count = totalNeeds[zone2];
        int zone3Count = totalNeeds[zone3];
        
        if(remainingCards[zone1] > 0 && zone1Count >= zone2Count && zone1Count >= zone3Count){            
            bestZone = zone1;        
        }else if(remainingCards[zone2] > 0 && zone2Count >= zone1Count && zone2Count >= zone3Count){
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
    
    public static void COUNT_CYCLES(String action){
        
        int zoneToMove = Integer.parseInt(action.split(" ")[1]);

        if(zoneToMove < myTeam.location) cycles++;
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
        PrintComments();
        PrintPossibleMoves();
    }

    public static void PrintComments(){

        
        //comments.add(0, String.format("releasable apps : %s", releasablesApps.toString()));
        comments.add(0, String.format("PHASE = %s, MAX_TO_RELEASE = %s\n", gamePhase, MAX_TO_RELEASE));

        for(String commentary : comments){
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

        comments.add(String.format("SCORE : %s", myTeam.score));
        comments.add(String.format("DAILY : %s", myTeam.permanentDailyRoutineCards));
        comments.add(String.format("ARCHI : %s", myTeam.permanentArchitectureStudyCards));
        comments.add(String.format("MYLOC : %s", myTeam.location));
        comments.add(String.format("OPLOC : %s", oppTeam.location));
        
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