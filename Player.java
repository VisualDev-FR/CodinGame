import java.util.*;

class Player {

    private static Map<Integer, Human> humans;
    private static Map<Integer, Zombie> zombies;
    private static Hero hero;
    private static int[] fib;
    private static Random random;
    private static long globalSeed;

    public static void main(String args[]) {
        
        Scanner in = new Scanner(System.in);
        
        globalSeed = new Random().nextLong();
        random = new Random(globalSeed);
        fib = initFibonacci(45);

        debug("Seed = " + globalSeed);
        
        boolean isRectangle = false;
        boolean firstTurn = true;

        while (true) {

            long start = System.currentTimeMillis();
            int index = 0;

            hero = parseHero(in);
            humans = parseHumans(in, hero);
            zombies = parseZombies(in, humans);

            if(firstTurn){
                isRectangle = isRectangle(humans);
                firstTurn = false;
            }

            if(isRectangle){
                debug("Rectangle...");
                System.out.println(humans.get(0).getPosition().toString());
                continue;
            }

            Strategy bestStrategy = null;
            int bestScore = Integer.MIN_VALUE;

            while(getTimer(start) < 155){

                Strategy randomStrategy = new Strategy(hero, humans, zombies);
                int score = randomStrategy.getScore();

                if(score > bestScore){                    
                    bestScore = score;
                    bestStrategy = randomStrategy;                    
                }
                index++;
            }

            debug(String.format("nb Occurences = %s, bestScore = %s, nbMoves = %s, nbZombies = %s, nbHumans = %s", 
                index, 
                bestScore, 
                bestStrategy.getMovesCount(),
                bestStrategy.getZombieCOunt(),
                bestStrategy.getHumanCount()
            ));
            bestStrategy.printFirstMove();          
        }
    }

    // CLASSES

    private static class Strategy{
        
        private final int MAX_MOVE_COUNT = 3;

        private int m_moveCount;
        private List<Coords> m_moves;
        private int m_score;

        private Hero m_hero;
        private Map<Integer, Human> m_humanMap;
        private Map<Integer, Zombie> m_zombieMap;

        public Strategy(Hero hero, Map<Integer, Human> humanMap, Map<Integer, Zombie> zombieMap){

            m_humanMap = cloneHumanMap(humanMap);
            m_zombieMap = cloneZombieMap(zombieMap);            
            m_moveCount = random.nextInt(MAX_MOVE_COUNT);
            m_moves = new ArrayList<Coords>();
            m_hero = hero.clone();
            m_score = 0;
        }

        public int getHumanCount(){
            return m_humanMap.size();
        }

        public int getZombieCOunt(){
            return m_zombieMap.size();
        }

        public int getMovesCount(){
            return m_moves.size();
        }

        public int getScore(){
            
            this.run();

            if(m_humanMap.size() <= 1){
                return Integer.MIN_VALUE + 1;
            }
            else{
                return m_score;
            }
        }

        public void simulateOneMove(Coords nextTargetPoint){
            
            // Calcul de la prochaine position pour atteindre la target
            Coords nextPosition = m_hero.getPosition().getNextCoords(Hero.SPEED, nextTargetPoint);

            // Mise à jour de la position de Ash
            m_moves.add(nextPosition);
            m_hero.move(nextPosition);

            // mise à jour de la position des zombies
            int humanCountBeforeZombieUpdate = m_humanMap.size() - 1;
            int zombieCountBeforeMove = m_zombieMap.size();
            m_zombieMap = updateZombiePositions();

            // Mise à jour du score
            int zombieKilled = zombieCountBeforeMove - m_zombieMap.size();
            m_score += getCombo(humanCountBeforeZombieUpdate, zombieKilled); 
        }

        private void run(){

            // n random moves, in random positions
            for(int i = 0; i < m_moveCount; i ++){
                Coords nextTargetPoint = Coords.getRandom();
                simulateOneMove(nextTargetPoint);
            }

            // Kill all remaining zombies
            while(m_zombieMap.size() > 0){

                // select random spoiled meat
                Zombie nextZombie = m_zombieMap.values().toArray(new Zombie[0])[random.nextInt(m_zombieMap.size())];

                // find and eat that tasty spoiled meat
                while(m_zombieMap.containsKey(nextZombie.getID())){

                    // Simulate that move
                    simulateOneMove(nextZombie.getPosition());

                    // if all humans died, we stop here to investigate better possibilities
                    if(m_humanMap.size() <= 1) return;
                }
            }
        }

        private Map<Integer, Zombie> updateZombiePositions(){

            Map<Integer, Zombie> updatedZombies = new HashMap<Integer, Zombie>();

            for(Zombie z : m_zombieMap.values()){

                z.move(z.getPosition().getNextCoords(Zombie.SPEED, z.getTarget().getPosition()));

                // Si le zombie est en dehors de la zone mortelle, on met à jour sa position  et sa direction, sinon on ne le rajoute pas dans la sortie
                if(z.getDistanceFrom(m_hero) > Hero.MORTAL_RADIUS){
                    
                    // si des humains sont dans la zone mortelle du zombie, on les enlève du humanMap
                    //m_humanMap = z.removeKilledHumans(m_humanMap);

                    Human zombieTarget = z.getTarget();

                    if(z.getDistanceFrom(zombieTarget) <= Zombie.MORTAL_RADIUS && zombieTarget.getID() != -1){
                        m_humanMap.remove(zombieTarget.getID());
                    }

                    if(m_humanMap.size() <= 1) return new HashMap<Integer, Zombie>();

                    // On met à jour la cible du zombie
                    z.setTarget(z.findTarget(m_humanMap));
                    
                    updatedZombies.put(z.getID(), z);
                }
            }

            return updatedZombies;
        }

        public void printFirstMove(){
            
            Coords firstMove = m_moves.size() > 0 ? m_moves.get(0) : m_hero.getPosition().clone();
            
            System.out.println(firstMove.toString());
        }
    }

    private static class Coords{
        
        private static final int MAP_WIDTH = 16000;
        private static final int MAP_HEIGHT = 9000;
        private static final int RAND_RANGE = 707;
        
        public int m_x;
        public int m_y;

        public Coords(int x, int y){
            this.m_x = x;
            this.m_y = y;
        }

        public static Coords getRandom(){
            
            int randX = random.nextInt(Coords.MAP_WIDTH);// / 10) * 10;
            int randY = random.nextInt(Coords.MAP_HEIGHT);// / 10) * 10;

            return new Coords(randX, randY);
        }

        public static Coords getRandomFrom(Coords coords){

            Coords randCoord = new Coords(-1, -1);

            while(!randCoord.isValid()){
                int randX = randBetween(coords.getX() - Coords.RAND_RANGE, coords.getX() + Coords.RAND_RANGE);
                int randY = randBetween(coords.getY() - Coords.RAND_RANGE, coords.getY() + Coords.RAND_RANGE);    
                randCoord = new Coords(randX, randY);                
            }
            return randCoord;
        }

        public boolean isValid(){
            return this.m_x >= 0 && this.m_x < MAP_WIDTH && this.m_y >= 0 && this.m_y < MAP_HEIGHT;
        }

        public Coords clone(){
            return new Coords(m_x, m_y);
        }

        public int getX(){
            return this.m_x;
        }

        public int getY(){
            return this.m_y;
        }

        public double getDistance(Coords anotherPoint){
            return Math.sqrt((Math.pow((double)(this.m_x - anotherPoint.m_x), 2) + Math.pow((double)(this.m_y - anotherPoint.m_y), 2)));
        }        

        public Coords getNextCoords(int speed, Coords target){

            if(this.getDistance(target) <= speed){
                return new Coords(target.m_x, target.m_y);
            }
            else{

                double distance = this.getDistance(target);

                int nextX = (int)(this.m_x + speed * (target.m_x - this.m_x) / distance);
                int nextY = (int)(this.m_y + speed * (target.m_y - this.m_y) / distance);

                return new Coords(nextX, nextY);
            }
        }

        public String toString(){
            return String.format("%s %s", m_x, m_y);
        }
    }

    private static abstract class Entity{
        
        protected int m_ID;
        protected Coords m_position;

        public Entity(int id, int x, int y){
            this.m_ID = id;
            this.m_position = new Coords(x, y);            
        }

        public int getDistanceFrom(Entity anotherEntity){
            return (int) Math.floor(this.m_position.getDistance(anotherEntity.m_position));
        }

        public int getX(){
            return this.m_position.getX();
        }

        public int getY(){
            return this.m_position.getY();
        }

        public int getID(){
            return m_ID;
        }

        public Coords getPosition(){
            return m_position;
        }

        public void move(Coords nextPosition){
            this.m_position = nextPosition.clone();
        }

        public String toString(){
            return String.format("%s : %s", m_ID, this.m_position.toString());
        }
    }

    private static class Human extends Entity{

        public Human(int id, int x, int y){
            super(id, x, y);
        }

        public Human clone(){
            return new Human(this.m_ID, this.m_position.getX(), this.m_position.getY());
        }        
    }

    private static class Zombie extends Entity{

        private static final int SPEED = 400;
        private static final int MORTAL_RADIUS = 400;
        private Coords m_targetCoords;
        private Human m_target;

        public Zombie(int id, int x, int y, int nextX, int nextY){
            super(id, x, y);
            m_targetCoords = new Coords(nextX, nextY);
        }

        public Zombie clone(){        
            Zombie clone = new Zombie(this.m_ID, this.m_position.getX(), this.m_position.getY(), this.m_targetCoords.getX(), this.m_targetCoords.getY());
            clone.setTarget(m_target); 
            return clone;           
        }

        public void setTarget(Human human){
            m_target = human;
        }

        public Human getTarget(){
            return m_target;
        }

        public Map<Integer, Human> removeKilledHumans(Map<Integer, Human> humans){

            Map<Integer, Human> remainingHumans = new HashMap<Integer, Human>();
            
            for(Human h : humans.values()){
                if(this.getDistanceFrom(h) > Zombie.MORTAL_RADIUS || h.getID() == - 1){
                    remainingHumans.put(h.getID(), h);
                }
            }

            return remainingHumans;
        }

        public Human findTarget(Map<Integer, Human> humansMap){
            
            double minDist = Double.MAX_VALUE;
            Human closiestHuman = null;

            for(Human human : humansMap.values()){

                double humanDist = this.m_position.getDistance(human.m_position);

                if(humanDist < minDist){
                    minDist = humanDist;
                    closiestHuman = human;
                }
            }
            return closiestHuman;
        }

    }

    private static class Hero extends Human{
        
        private static final int SPEED = 1000;
        private static final int MORTAL_RADIUS = 2000;        

        public Hero(int x, int y){
            super(-1, x, y);
        }

        public Hero clone(){
            return new Hero(this.getX(), this.getY());
        }
    }

    // PARSING FUNCTIONS

    public static boolean isRectangle(Map<Integer, Human> humanMap){

        if(humanMap.size() != 5) return false;       

        Map<Integer, Integer> distanceMap = new HashMap<Integer, Integer>();

        for(Human human1 : humanMap.values()){
            for(Human human2 : humanMap.values()){
                if(human1.getID() != -1 && human2.getID() != -1 && human1.getID() != human2.getID()){
                    int distance = human1.getDistanceFrom(human2);
                    distanceMap.putIfAbsent(distance, distance);
                }
            }            
        }
        boolean boolRectangle = distanceMap.size() == 3;
        debug("Rectangle = " + boolRectangle + " " + distanceMap.toString());
        return boolRectangle;
    }

    public static int getCombo(int humanLeft, int killCount){

        int comboScore = 0;
        for(int i = 0; i < killCount; i++){
            comboScore += 10 * Math.pow(humanLeft, 2) * fib[i + 2];
        }

        return comboScore;
    }    

    private static Map<Integer, Human> parseHumans(Scanner in, Entity hero){
        
        Map<Integer, Human> parsedHumans = new HashMap<Integer, Human>();
        
        int humanCount = in.nextInt();
        for (int i = 0; i < humanCount; i++) {
            int humanId = in.nextInt();
            int humanX = in.nextInt();
            int humanY = in.nextInt();
            
            parsedHumans.put(humanId, new Human(humanId, humanX, humanY));
        }

        parsedHumans.put(hero.getID(), (Human)hero);
        
        return parsedHumans;
    }

    private static Map<Integer, Zombie> parseZombies(Scanner in, Map<Integer, Human> humansMap){
        
        Map<Integer, Zombie> parsedZombies = new HashMap<Integer, Zombie>();

        int zombieCount = in.nextInt();
        for (int i = 0; i < zombieCount; i++) {
            int zombieId = in.nextInt();
            int zombieX = in.nextInt();
            int zombieY = in.nextInt();
            int zombieXNext = in.nextInt();
            int zombieYNext = in.nextInt();

            Zombie zombie = new Zombie(zombieId, zombieX, zombieY, zombieXNext, zombieYNext);
            zombie.setTarget(zombie.findTarget(humansMap));
        
            parsedZombies.put(zombieId, zombie);
        }

        return parsedZombies;
    }

    private static Hero parseHero(Scanner in){
        int x = in.nextInt();
        int y = in.nextInt();        
        return new Hero(x, y);
    }

    private static int[] initFibonacci(int maxIndex){

        int[] fiboValues = new int[maxIndex];

        fiboValues[0] = 0;
        fiboValues[1] = 1;

        for(int i = 2; i < maxIndex; i++){
            fiboValues[i] = fiboValues[i - 1] + fiboValues[i - 2];
        }

        return fiboValues;
    }    
    
    private static long getTimer(long start){
        return System.currentTimeMillis() - start;
    }

    // DEBUG FUNCTIONS

    private static void debug(String message){
        System.err.println(message);
    }

    public static Map<Integer, Zombie> cloneZombieMap(Map<Integer, Zombie> mapToClone){

        Map<Integer, Zombie> clonedMap = new HashMap<Integer, Zombie>();

        for(int zombieID : mapToClone.keySet()){
            clonedMap.put(zombieID, mapToClone.get(zombieID).clone());
        }

        return clonedMap;
    }

    public static Map<Integer, Human> cloneHumanMap(Map<Integer, Human> mapToClone){

        Map<Integer, Human> clonedMap = new HashMap<Integer, Human>();

        for(int humanID : mapToClone.keySet()){
            clonedMap.put(humanID, mapToClone.get(humanID).clone());
        }

        return clonedMap;
    }

    public static int randBetween(int min, int max){
        return random.nextInt((max - min) + 1) + min;
    }
}