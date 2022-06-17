import java.util.*;

class Player {

    static Team myTeam;
    static Team oppTeam;    

    static Map<Integer, Application> applications;
    static Map<String, CardCollection> collections;
    static List<String> possibleMoves;
    static int[] remainingCards;

    static int actualPoste;    
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

        PARSE_FIRST_TURN();

        while (true) {

            gamePhase = in.next(); // can be MOVE, GIVE_CARD, THROW_CARD, PLAY_CARD or RELEASE

            applications = new HashMap<Integer, Application>();
            collections = new HashMap<String, CardCollection>();
            possibleMoves = new ArrayList<String>();

            PARSE_APPLICATIONS(in);
            PARSE_TEAMS(in);
            PARSE_CARDS_COLLECTIONS(in);
            PARSE_POSSIBLE_MOVES(in);

            PRINT_GAME();

            // In the first league: RANDOM | MOVE <zoneId> | RELEASE <applicationId> | WAIT; In later leagues: | GIVE <cardType> | THROW <cardType> | TRAINING | CODING | DAILY_ROUTINE | TASK_PRIORITIZATION <cardTypeToThrow> <cardTypeToTake> | ARCHITECTURE_STUDY | CONTINUOUS_DELIVERY <cardTypeToAutomate> | CODE_REVIEW | REFACTORING;
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
        }
    }

    //PHASE FUNCTIONS

    public static void PHASE_THROW() throws Exception{
        
        /* Logique de choix des cartes à jeter :

            Je priorise les bonus, car quand on sera en galère j'en aurai beaucoup en main

            Si je n'ai pas de bonus dans ma main, je donne celle qui me permet d'obtenir une application avec le moins de cartes manquantes

        */

        int minMissingCard = 9999;
        int bestCardID = -1;

        int bonusCards = collections.get("HAND").cards[8];

        if (bonusCards > 0){

            bestCardID = 8;
        
        }else{

            // TODO: Choisir la carte qui nous avantage le plus
        }

        
        
        if(bestCardID > -1){
            THROW(bestCardID);
        }else{
            RANDOM();
        }
    }

    public static void PHASE_GIVE() throws Exception{

        /* Logique de choix des cartes à donner :

            Si je donne une carte, je donne celle qui me permet d'obtenir une application avec le moins de cartes manquantes

            for each card in myHand
            for each app in applications
                minMissingCard = GetMissingCard(main avec la carte en moins)
        */

        int minMissingCard = 9999;
        int bestCardID = -1;

        for(String move : possibleMoves){ 

            String moveType = move.split(" ")[0];

            if(moveType.equals("GIVE")){

                int cardID = Integer.parseInt(move.split(" ")[1]); //Pour chaque mouvement possible, s'il s'agit de donner une carte, on stocke l'ID de la carte à donner
                
                for(Application mApp : applications.values()){

                    CardCollection handTemp = new CardCollection(collections.get("HAND")); //Pour chaque appli, on crée une main virtuelle, dans laquelle on retire la carte que l'on doit eventuellement donner
    
                    handTemp.Remove(cardID); //S'il ne s'agit pas d'une carte bonus ou dette, on retire la carte courante de la main virtuelle que l'on vient de créer
    
                    int missingCard = mApp.GetMissingCardsForRelease(handTemp); //On stocke le nombre de carte manquantes pour construire l'appli courante, et on la compare au nombre mini de cartes manquantes pour chaque appli
    
                    if(missingCard < minMissingCard){
                        minMissingCard = missingCard;
                        bestCardID = cardID;
                    }
                }
            }
        }

        if(bestCardID > -1){
            GIVE(bestCardID);
        }else{
            RANDOM();
        }
    }

    //TODO: Evaluer toute les possibilités de jeu

    public static void PHASE_PLAY(){

        Map<String, Integer> movesToEval = new HashMap<String, Integer>();
        
        for(String move : possibleMoves){

            int eval = 0;

            switch(move.split(" ")[0]){
                case "TRAINING":
                    eval = 24 * collections.get("HAND").cards[TECHNICAL_DEBT];
                    break;
                case "CODING":
                    eval = 1;
                    break;
                case "DAILY_ROUTINE":
                    eval = 1;
                    break;
                case "TASK_PRIORITIZATION":
                    eval = 0; //24 * myTeam.score;
                    break;                                                            
                case "ARCHITECTURE_STUDY":
                    eval = 24 * myTeam.score;
                    break;
                case "CONTINUOUS_INTEGRATION":
                    eval = 1;
                    break;                    
                case "CODE_REVIEW":
                    eval = 95;
                    break;
                case "REFACTORING":
                    eval = 100 * collections.get("HAND").cards[TECHNICAL_DEBT];
                    break;

            }

            movesToEval.put(move, eval);
        }

        int maxEval = 0;
        String bestMove="";

        for(String move : movesToEval.keySet()){
            if(movesToEval.get(move) > maxEval){
                maxEval = movesToEval.get(move);
                bestMove = move;
            }
        }
        
        if(!bestMove.equals("")){
            PLAY(bestMove);
        }else{
            RANDOM();
        } 
    }

    public static void PHASE_RELEASE(){

        Map<Integer, Integer> appsReadyForRelease = new HashMap<Integer, Integer>();

        for(String move : possibleMoves){

            if(move.split(" ")[0].equals("RELEASE")){

                int locationMove = Integer.parseInt(move.split(" ")[1]);
                int missingToRelease = applications.get(locationMove).GetMissingCardsForRelease(collections.get("HAND"));

                if(missingToRelease == 0 || oppTeam.score > myTeam.score || myTeam.score == 4) appsReadyForRelease.put(locationMove, missingToRelease);
                //appsReadyForRelease.put(locationMove, missingToRelease);
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
            
            RELEASE(bestAppToRelease);
        }else{
            WAIT();
        }
    }

    public static void PHASE_MOVE(){

        List<Integer> moves = new ArrayList<Integer>();

        for(String move : possibleMoves){

            if(move.split(" ")[0].equals("MOVE")){

                int locationMove = Integer.parseInt(move.split(" ")[1]);

                moves.add(locationMove);
            }
        }

        if(moves.size() > 0){
            int bestPosteID = GetPosteWhereAppNeedMinCard(remainingCards, moves);  

            if(bestPosteID == -1) bestPosteID = GetPosteWithMinCards(remainingCards, moves);

            MOVE(bestPosteID);
        }else{
            WAIT();
        }
    }
    
    // EVALUATION FUNCTIONS

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

    public static int GetPosteWhereAppNeedMinCard(int[] cardsByZone, List<Integer> moves){

        int bestPosteID = -1;
        int minNeededCard = 9999;

        for(int posteID : moves){

            int minSpace = Math.abs(oppTeam.location - posteID);

            //System.err.printf("Poste %s : minSpace = %s\n", posteID, minSpace);            
            
            if(cardsByZone[posteID] > 0 && minSpace > 1){

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

    public static int GetPosteWithMinCards(int[] cardsByZone, List<Integer> possibleMoves){

        int minCardsCount = 9999;
        int bestPosteID = possibleMoves.get(0);

        for(int posteID : possibleMoves){

            if(cardsByZone[posteID] > 0 && cardsByZone[posteID] < minCardsCount){
                minCardsCount = cardsByZone[posteID];
                bestPosteID = posteID;
            }
        }

        return bestPosteID;
    }
    
    // CLASSES

    public static class CardCollection{

        public String location;
        public int[] cards;
        public List<Integer> cardsID;

        public CardCollection(CardCollection mCollection){

            location = mCollection.location;
            cards = mCollection.cards.clone();
            cardsID = new ArrayList<Integer>(mCollection.cardsID);
        }

        public CardCollection(Scanner in, String mLocation){

            cardsID = new ArrayList<Integer>();
            cards = new int[CARD_TYPE_COUNT + 2];
            location = mLocation;

            for(int i = 0; i < cards.length; i++){
                cards[i] = in.nextInt();
                if(cards[i] > 0) cardsID.add(i);
            }
        }

        public void Remove(int cardID) throws Exception{

            if(cards[cardID] <= 0){
                cards = new int[0];
                throw new Exception("Le nombre de cartes est déjà nul !");                
            }else{
                cards[cardID] = Math.max(0, cards[cardID] - 1);
            }            
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

    // PRINTING FUNCTIONS

    public static void PRINT_GAME(){
        PrintHeaders();
        PrintCollections();
        PrintRemainingCards();
        PrintApplications();
        PrintPossibleMoves();
        
        System.err.print(gamePhase + "\n\n");
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

        for(String move : possibleMoves){
            System.err.println(move);
        }

        System.err.println(" ");
    }

    public static void PrintRemainingCards(){

        String[] tableToPrint = new String[CARD_TYPE_COUNT + 1];

        tableToPrint[0] = PadString("CRD LFT", DISPLAY_MARGIN);

        for(int i = 0; i < remainingCards.length; i++){

            tableToPrint[i + 1] = PadInteger(remainingCards[i]);            
        }

        System.err.println(String.join(" ", tableToPrint) + "\n");
    }

    public static void PrintCollections(){

        String[][] tableToPrint = new String[collections.size()][CARD_TYPE_COUNT + 3];
        int i = 0;

        for(CardCollection mCollection : collections.values()){

            tableToPrint[i][0]= PadString(ParseLocation(mCollection.location), DISPLAY_MARGIN);

            for(int j = 0; j < mCollection.cards.length; j++){

                tableToPrint[i][j+1] = PadInteger(mCollection.cards[j]);
            }

            System.err.println(String.join(" ", tableToPrint[i]));
            i++;
        }

        System.err.println(" ");
    }

    public static String ParseLocation(String mLocation){

        switch(mLocation){
            case"OPPONENT_AUTOMATED":   return "O_AT";
            case"DRAW":                 return "DRAW";
            case"DISCARD":              return "DISC";
            case"HAND":                 return "HAND";
            case"OPPONENT_CARDS":       return "O_CD";
            case"AUTOMATED":            return "AUTO";
            default:                    return mLocation;
        }
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
        tableToPrint = new String[applications.size()][9];
        i = 0;

        for(Application mApplication : applications.values()){

            tableToPrint[i][0]= PadString("App " + mApplication.id, DISPLAY_MARGIN);

            for(int j = 0; j < 8; j++){

                int missingCard = Math.max(0, mApplication.neededCards[j] - collections.get("HAND").cards[j] * 2);

                tableToPrint[i][j+1] = PadInteger(missingCard);
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
    
    // PARSING FUNCTIONS

    public static void PARSE_POSSIBLE_MOVES(Scanner in){

        possibleMoves = new ArrayList<String>();
        
        int possibleMovesCount = in.nextInt();
        if (in.hasNextLine()) {
            in.nextLine();
        }

        for (int i = 0; i < possibleMovesCount; i++){

            possibleMoves.add(in.nextLine()); 
        }
    }

    public static void PARSE_CARDS_COLLECTIONS(Scanner in){
        
        int cardLocationsCount = in.nextInt();
        for (int i = 0; i < cardLocationsCount; i++) {
            
            String cardsLocation = in.next(); // the location of the card list. It can be HAND, DRAW, DISCARD or OPPONENT_CARDS (AUTOMATED and OPPONENT_AUTOMATED will appear in later leagues)
            
            CardCollection mCardCollection = new CardCollection(in, cardsLocation);    
            
            collections.put(cardsLocation, mCardCollection);
        }
    }

    public static void PARSE_FIRST_TURN(){
        remainingCards = new int[8];
        Arrays.fill(remainingCards, 5);
        actualPoste = -1;
    }

    public static void PARSE_TEAMS(Scanner in){

        myTeam = new Team(in, 0);
        oppTeam = new Team(in, 1);

        if(myTeam.location >= 0 && myTeam.location <= 7){
            if(remainingCards[myTeam.location] > 0) remainingCards[myTeam.location] -=1;
        }

        if(oppTeam.location >= 0 && oppTeam.location <= 7){
            if(remainingCards[oppTeam.location] > 0) remainingCards[oppTeam.location] -=1;
        }        
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

    public static void THROW(int cardID){
        System.out.println("THROW " + cardID);
    }

    public static void PLAY(String action){
        System.out.println(action);
    }

    public static void GIVE(int cardID){
        System.out.println("GIVE " + cardID);
    }

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