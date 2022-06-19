import java.util.*;

import javax.sound.midi.SysexMessage;

class Player {

    static Team myTeam;
    static Team oppTeam;
    static CardCollection allMyCards;
    static Map<String, Integer> movesToEval;
    static Map<Integer, Application> applications;
    static Map<String, CardCollection> collections;
    static List<String> possibleMoves;
    static int[] remainingCards;
    static String action;
    static List<String> playType; 
  
    static String gamePhase;    

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

        public int GetMissingCardsForRelease(CardCollection mCollection){

            int missingToRelease = 0;

            for(int i = 0; i < neededCards.length; i++){
                missingToRelease += Math.max(0, neededCards[i] - mCollection.cards[i] * 2);
            }

            return missingToRelease - mCollection.cards[BONUS_CARD];
        }
    }

    //PHASE FUNCTIONS

    public static void PHASE_THROW() throws Exception{ //TODO: Choisir la carte nous fait perdre le moins d'appli dispo pour release /!\/!\/!\/!\/!\/!\/!\/!\/!\

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

    public static void PHASE_GIVE() throws Exception{ //TODO: compléter l'evaluation des cartes à donner en prenant en compte l'avantage qu'on perdra en jouant la carte

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

    public static void PHASE_PLAY() throws Exception{

        Map<String, Integer> moves = new HashMap<String, Integer>();

        if(CanIReleaseAnApp()){
            action = WAIT();
            return;
        }
        
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
                    eval = 0;
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

        /* 
            if(!MayIPlayThatCard(bestMove)){
            action = WAIT();
            }else if...
        */

        if(!bestMove.equals("")){
            action = bestMove;
        }else{
            action = RANDOM();
        } 
    }

    public static void PHASE_RELEASE(){ //TODO : evaluer les release sur 1) les release possibles pour l'adversaire 2) le nombre de dettes techniques qu'elle engendre

        Map<Integer, Integer> appsReadyForRelease = new HashMap<Integer, Integer>();

        for(String move : possibleMoves){

            if(move.split(" ")[0].equals("RELEASE")){

                CardCollection usefullHand = GetUsefullHand(); //collections.get("HAND");

                int appID = Integer.parseInt(move.split(" ")[1]);

                int missingToRelease = applications.get(appID).GetMissingCardsForRelease(usefullHand);

                movesToEval.put(move, missingToRelease);

                int myTechnicalDebt = allMyCards.cards[TECHNICAL_DEBT];
                int oppTechnicalDebt = collections.get("OPPONENT_CARDS").cards[TECHNICAL_DEBT];

                //System.err.printf("app %s : missing = %s, myDebt = %s, oppDebt = %s\n", appID, missingToRelease, myTechnicalDebt, oppTechnicalDebt);

                if(missingToRelease == 0 || missingToRelease + myTechnicalDebt < oppTechnicalDebt){
                    appsReadyForRelease.put(appID, missingToRelease);
                }else{
                    movesToEval.put(move, movesToEval.get(move) * -1);
                }
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
    
    public static void PHASE_RELEASE_NEW(){

        Map<String, Integer> releasesToEval = new HashMap<String, Integer>();

        for(String move : possibleMoves){

            if(move.startsWith("RELEASE")){

                int eval = EVAL_RELEASE(move);

                releasesToEval.put(move, eval);
                movesToEval.put(move, eval);
            
            }else if(move.startsWith("WAIT")){

                releasesToEval.put(move, 1);
                movesToEval.put(move, 1);                
            }
        }

        String bestMove = GetMinValue(releasesToEval);

        action = bestMove;
    }

    public static void PHASE_MOVE_SIMPLE(){

        System.err.printf("My position = %s\nOppPosition = %s\n\n", myTeam.location, oppTeam.location);
        
        int startZone = myTeam.location;
        boolean zoneFound = false;

        while(!zoneFound){
            startZone = startZone < 7 ? startZone + 1 : 0;
            zoneFound = !IsZoneAsideOpponent(startZone) && possibleMoves.contains(MOVE(startZone));
        }
        
        action = MOVE(startZone);
    }

    public static void PHASE_MOVE(){ 

        Map<String, Integer> moves = new HashMap<String, Integer>();

        for(String move : possibleMoves){

            if(move.split(" ")[0].equals("MOVE")){

                int eval = EVAL_MOVE(move);

                moves.put(move, eval);

                //System.err.printf("%s : %s\n", PadString(move, 8), eval);
            }
        }

        if(moves.size() > 0){

            int minEval = 9999;
            String bestMove = "";

            for(String move : moves.keySet()){

                if(moves.get(move) < minEval){
                    minEval = moves.get(move);
                    bestMove = move;
                }
            }

            action = bestMove;

        }else{
            action = RANDOM();
        }
    }    
        
    // EVALUATION FUNCTIONS

    public static int EVAL_RELEASE(String move){

        // 1) la release doit minimiser le nombre de dettes techniques  +1 pts
        // 2) la release doit empecher l'adversaire de release          +10 pts

        int appID = Integer.parseInt(move.split(" ")[1]);

        CardCollection myHand = GetUsefullHand();
        CardCollection oppHand = GetAllOppCards();

        int missingWithMyHand = applications.get(appID).GetMissingCardsForRelease(myHand);
        int missingWithOppHand = applications.get(appID).GetMissingCardsForRelease(oppHand);

        System.err.printf("app %s : myHand %s, oppHand %s\n", PadInteger(appID, 2, false), missingWithMyHand, missingWithOppHand);

        int eval = 999;

        if(missingWithMyHand > missingWithOppHand && missingWithMyHand == 0){
        
            eval = -999;
        
        }else if(missingWithMyHand < missingWithOppHand && missingWithOppHand > 0){

            eval = missingWithMyHand * missingWithOppHand;
        
        }        

        return eval;
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

    public static int EVAL_MOVE_NEW(String move){ //TODO: re-evaluer les moves sur 1) le nombre d'applis que ça peut nous permettre de gagner 2) les cartes à jouer qui nous avantagent

        // 1) la zone doit maximiser le nombre d'appli dispo pour release
        // 2) la zone doit nous offrir une carte dont on tirera un avantage certain si on la joue
        // 3) la zone doit être à la position supérieure la plus proche de notre location actuelle
        // 5) la zone doit favoriser un passage à l'administration, plutot que donner une compétence à l'adversaire
        // 4) la zone ne doit pas se situer à proximité de l'adversaire, on préferera passer à l'administration

        /* Regles de proximités entre joueurs du moteur de jeu
            if opponentPlayer.getZoneId()>=0
            && ((abs(opponentPlayer.getZoneId() - player.getZoneId())<=1)
            || (abs(opponentPlayer.getZoneId() - player.getZoneId())==7)))         
        */

        CardCollection handTemp = GetUsefullHand();//new CardCollection(collections.get("HAND"));
        
        String[] tMove = move.split(" ");
        int moveZoneID = Integer.parseInt(tMove[1]);

        int minEval = 9999;

        for(int i = 1; i < tMove.length; i++){

            int zoneID = Integer.parseInt(tMove[i]);

            if(remainingCards[zoneID] > 0){
                handTemp.AddCard(zoneID);
            }else{
                handTemp.AddCard(BONUS_CARD);
            }            
        }

        int coef_Length = tMove.length > 2 ? 0 : 1;
        int coef_Space = IsZoneAsideOpponent(moveZoneID) ? 2 : 1;
        int coef_Direction = moveZoneID > myTeam.location ? 2 : 1;
        int coef_Distance = GetDistance(moveZoneID, myTeam.location);

        for(Application mApp : applications.values()){

            int minMissingCards = mApp.GetMissingCardsForRelease(handTemp);

            int coef_Negatif = minMissingCards < 0 && tMove.length < 3 ? -1 : 1;
            int eval = (minMissingCards + coef_Length + coef_Space + coef_Direction + coef_Distance) * coef_Negatif;
            
            minEval = Math.min(minEval, eval);
        }        

        movesToEval.put(move, minEval);

        return minEval;
    }    

    public static int EVAL_MOVE(String move){

        CardCollection handTemp = GetUsefullHand();//new CardCollection(collections.get("HAND"));
        
        String[] tMove = move.split(" ");

        int minEval = 9999;
        int minSpace = 9999;

        for(int i = 1; i < tMove.length; i++){

            int zoneID = Integer.parseInt(tMove[i]);

            minSpace = Math.min(minSpace, GetDistance(oppTeam.location, zoneID));

            if(remainingCards[zoneID] > 0){
                handTemp.AddCard(zoneID);
            }else{
                handTemp.AddCard(BONUS_CARD);
            }            
        }

        int coef_Length = tMove.length > 2 ? 0 : 1;
        int coef_Space = minSpace <= 1 || minSpace == 7 ? 2 : 1;
        int coef_Zone = Integer.parseInt(tMove[1]) == 5 ? 1 : 2;

        for(Application mApp : applications.values()){

            int minMissingCards = mApp.GetMissingCardsForRelease(handTemp);

            int coef_Negatif = minMissingCards < 0 && tMove.length < 3 ? -1 : 1;            
            int eval = (minMissingCards + coef_Length) * coef_Space * coef_Negatif * coef_Zone;
            
            minEval = Math.min(minEval, eval);
        }

        movesToEval.put(move, minEval);

        return minEval;
    }    

    public static int EVAL_TRAINING(String move){

        // on renvoi un entier qui correspond à 1000 divisé par (le taux de carte dette qu'on a dans notre pioche + 5) * le nombre de dette technique en main
        // => 10% de dette, 0 cartes dettes en main : 1000 / 11 = 91
        // => 20% de dette, 0 cartes dettes en main : 1000 / 21 = 48
        // => 50% de dette, 0 cartes dettes en main : 1000 / 51 = 20
        // => 80% de dette, 0 cartes dettes en main : 1000 / 81 = 13
        
        int eval = 1;

        CardCollection nextDraw = GetUsefullDraw();

        if(nextDraw != null){

            int technicalProportion = nextDraw.proportions[TECHNICAL_DEBT];
            int technicalDebt = collections.get("HAND").cards[TECHNICAL_DEBT];
            
            eval = technicalProportion > 0 ? technicalDebt * (1000 / (technicalProportion + 5)) : 1000;
        }

        movesToEval.put(move, eval);

        return 24 * collections.get("HAND").cards[TECHNICAL_DEBT]; //eval;
    }    

    public static int EVAL_CODING(String move){

        // on renvoi un entier qui correspond à 1000 divisé par le taux de carte dette qu'on a dans notre pioche
        // => 10% de dette : 1000 / 10 = 100
        // => 20% de dette : 1000 / 20 = 50
        // => 50% de dette : 1000 / 50 = 20
        // => 80% de dette : 1000 / 80 = 13
        
        int eval = 1;

        CardCollection nextDraw = GetUsefullDraw();

        if(nextDraw != null){

            int technicalProportion = GetUsefullDraw().proportions[TECHNICAL_DEBT];
            int technicalDebt = collections.get("HAND").cards[TECHNICAL_DEBT];
            
            eval = technicalProportion > 0 ? technicalDebt * (1000 / (technicalProportion)) : 1000;
        }

        movesToEval.put(move, eval);

        return 24 * collections.get("HAND").cards[TECHNICAL_DEBT]; //eval;
    }

    public static int EVAL_DAILY_ROUTINE(String move){
        int eval = 9999;
        movesToEval.put(move, eval);
        return eval;
    }

    public static int EVAL_CONTINUOUS_INTEGRATION(String move){

        // On renvoi un entier qui correspond à 50 fois le nombre d'appli pouvant avoir une competence complétée avec cette carte + ce qu'on a en jeu
        // Si l'appli courante peut avoir cette compétence rempli sans carte supplémentaire, on ne la compte pas dans le calcul
        // 1 appli ayant une somme nulle => 50
        // 2 applis ayant une somme nulle => 100

        int cardID = Integer.parseInt(move.split(" ")[1]);
        int totalNeededCards = 0;
        int eval = -9999;

        if(cardID == 8){
            eval = 8 * 9999;
            movesToEval.put(move, eval);                
            return eval;            
        }

        for(Application mApp : applications.values()){
            totalNeededCards += mApp.neededCards[cardID];           
        }

        eval = totalNeededCards * 9999;
        movesToEval.put(move, eval);                
        return eval;
    }
    
    public static int EVAL_TASK_PRIORITIZATION(String move) throws Exception{

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
    
    public static int EVAL_ARCHITECTURE_STUDY(String move){
        int eval = 9998;
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

    public static CardCollection GetMostProbableOpponentHand(){ //TODO: compléter cette fonction...

        CardCollection oppCards = new CardCollection(collections.get("OPPONENT_CARDS"));

        if(collections.containsKey("OPPONENT_AUTOMATED")){

            int[] oppAutoCards = collections.get("OPPONENT_AUTOMATED").cards;

            for(int i = 0; i < oppAutoCards.length; i++){

                oppCards.cards[i] += oppAutoCards[i];
            }


            oppCards.ResetProportions();
        }

        return null;
    }

    public static CardCollection GetAllOppCards(){ //TODO: compléter cette fonction...

        CardCollection oppCards = new CardCollection(collections.get("OPPONENT_CARDS"));

        if(collections.containsKey("OPPONENT_AUTOMATED")){

            int[] oppAutoCards = collections.get("OPPONENT_AUTOMATED").cards;

            for(int i = 0; i < oppAutoCards.length; i++){

                oppCards.cards[i] += oppAutoCards[i];
            }

            oppCards.ResetProportions();
        }

        return oppCards;
    }    

    public static boolean MayIPlayThatCard(String bestMove) throws Exception{

        String bestMoveType = bestMove.split(" ")[0];
        int bestMoveCardID = playType.indexOf(bestMoveType);

        if(bestMoveCardID == -1){
            return true;
        }else{

            CardCollection myActualHand = GetUsefullHand();
            CardCollection myHandAfterPlay = GetUsefullHand();
    
            myHandAfterPlay.RemoveCard(bestMoveCardID);
    
            int missingWithMyHand = 999;
            int missingAfterPlay = 999;
    
            for(Application mApp : applications.values()){
    
                missingWithMyHand = Math.min(missingWithMyHand, mApp.GetMissingCardsForRelease(myActualHand));
                missingAfterPlay = Math.min(missingAfterPlay, mApp.GetMissingCardsForRelease(myHandAfterPlay));
            }
    
            if(missingWithMyHand < missingAfterPlay){
                return false;
            }else{
                return true;
            }
        }        
    }

    public static boolean CanIReleaseAnApp(){

        CardCollection useFullHand = GetUsefullHand();

        for(Application mApp : applications.values()){

            if(mApp.GetMissingCardsForRelease(useFullHand) == 0) return true;
        }

        return false;
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

    public static int GetDistance(int zoneA, int zoneB){

        if(zoneA == -1 && zoneB == -1){
            
            return 999;

        }else{

            int distance = Math.abs(zoneA - zoneB);

            return distance <= 4 ? distance : 8 - distance;
        }
    }

    public static boolean IsZoneAsideOpponent(int zoneID){

        int oppZoneID = oppTeam.location;

        if(zoneID > -1 && oppZoneID > -1 && GetDistance(zoneID, oppZoneID) > 1){
            return false;
        }else{
            return true;
        }
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
        }else{
            return collections.get("DISCARD");
        }
    }

    // PRINTING FUNCTIONS

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
        System.err.printf("myLoc : %s\noppLoc : %s\n", myTeam.location, oppTeam.location);

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

    public static void PrintApplications(){

        String[][] tableToPrint = new String[applications.size()][9];
        int i = 0;

        for(Application mApplication : applications.values()){

            tableToPrint[i][0]= PadString("App " + mApplication.id, DISPLAY_MARGIN);

            for(int j = 0; j < 8; j++){

                tableToPrint[i][j+1] = PadInteger(mApplication.neededCards[j], 2, true);
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
        
        for (int i = 0; i < applicationsCount; i++) {
            
            String objectType = in.next();

            Application mApplication = new Application(in);

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

    public static String WAIT(){
        return "WAIT";
    }

    public static String RANDOM(){
        return "RANDOM";
    }

}