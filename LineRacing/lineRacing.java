package LineRacing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import LineRacing.Player.*;

public class lineRacing {

    private static final String THIS_PATH = "C:/Users/menan/Desktop/CodinGame/LineRacing/log_";

    public static void main(String[] args) {

        clearConsole(); 
        
        Log log = new Log("01");

        Grid gameGrid = log.getGrid();
        Snake[] snakes = log.getSnakes();      
        
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
            //TimeUnit.MILLISECONDS.sleep(1);
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();

        } catch (Exception e) {
        }
}

    public static Snake[] createSnakes(Grid grid){

        Snake snake0 = new Snake(0, 5, 5, grid);
        Snake snake1 = new Snake(1, 5, 25, grid);
        Snake snake2 = new Snake(2, 15, 25, grid);
        Snake snake3 = new Snake(3, 15, 5, grid);

        Snake[] snakes = new Snake[]{
            snake0,
            snake1,
            snake2,
            snake3
        };

        return snakes;
    }

    public static String readLog(String logName){

        String everything = "";

        try (BufferedReader br = new BufferedReader(new FileReader(THIS_PATH + logName + ".txt"))) {
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
            
                while (line != null && line != "") {
                    sb.append(line + "\n");
                    line = br.readLine();
                }
                everything = sb.toString();
            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return everything;
    }

    private static class Log{

        private Snake[] m_snakes;
        private Grid m_grid;

        public Log(String logIndex){

            String[] logContent = readLog(logIndex).split("\n");

            // SNAKES PARSING
            int mID = Integer.parseInt(logContent[Grid.GRID_HEIGHT].split(" ")[0]);
            int nbPlayers = Integer.parseInt(logContent[Grid.GRID_HEIGHT].split(" ")[1]);           

            Player.initPlayer(nbPlayers, mID);

            m_snakes = new Snake[]{null, null, null, null};

            for(int i = 0; i < nbPlayers; i++){

                int snakeRow = Integer.parseInt(logContent[Grid.GRID_HEIGHT + i + 1].split(",")[1]);
                int snakeCol = Integer.parseInt(logContent[Grid.GRID_HEIGHT + i + 1].split(",")[2]);

                m_snakes[i] = new Snake(i, snakeRow, snakeCol);
            }            

            // GRID PARSING
            String[][] arrayGrid = new String[Grid.GRID_HEIGHT][Grid.GRID_WIDTH];

            for(int i = 0; i < Grid.GRID_HEIGHT; i++){
                arrayGrid[i] = logContent[i].split(" ");
                for (int j = 0; j < arrayGrid[i].length; j++){                    
                    if(!arrayGrid[i][j].equals(".")){
                        int snakeIndex = Integer.parseInt(arrayGrid[i][j]);
                        m_snakes[snakeIndex].addCoords(i, j);
                    }
                }
            }

            m_grid = new Grid(arrayGrid);
        }

        public Grid getGrid(){
            return m_grid;
        }

        public Snake[] getSnakes(){
            return m_snakes;
        }
    }
}
