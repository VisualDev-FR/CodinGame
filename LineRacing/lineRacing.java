package Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import Test.Player.*;

public class lineRacing {

    private static Map<String, int[]> directions;

    public static void main(String[] args) {

        clearConsole();

        Grid gameGrid = new Grid();
        Snake[] snakes = createSnakes(gameGrid);
        directions = Player.initDirections();
        
        Simulation simulation = new Simulation(gameGrid, "LEFT", snakes);

        simulation.endGame();
    }

    public static void printGrid(String [][] grid){

        for(int i = 0; i < grid.length; i++){
            System.out.println(String.join(" ", grid[i]));
        }
    }

    public static void clearConsole(){
        try {
            System.out.println("Press any key to continue.");
            new Scanner(System.in).nextLine();
            TimeUnit.MILLISECONDS.sleep(5);
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();

        } catch (Exception e) {
        }
}

    public static Snake[] createSnakes(Grid grid){

        Snake snake0 = new Snake(0, 5, 5, grid);
        Snake snake1 = new Snake(1, 5, 25, grid);
        Snake snake2 = new Snake(2, 15, 5, grid);
        Snake snake3 = new Snake(3, 15, 25, grid);

        Player.nbOfPlayer = 4;
        Player.myID = 1;

        Snake[] snakes = new Snake[]{
            snake0,
            snake1,
            snake2,
            snake3
        };

        return snakes;
    }
}
