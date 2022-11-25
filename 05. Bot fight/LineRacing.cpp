/* 
    * see the playground wlesavo's playground for more explainations about the voronoi areas calculation:
        https://tech.io/playgrounds/66330/bfs-and-voronoi-diagrams-using-bit-shift-operations/voronoi-area-in-line-racing

    * other usefull playgrounds :
        https://www.codingame.com/playgrounds/58038/fast-connected-components-for-6x12-bitboard
        https://www.codingame.com/playgrounds/38626/optimizing-breadth-first-search
 */

#include <iostream>
#include <chrono>
#include <bitset>
#include <math.h>
#include <vector>
#include <string>
#include <map>
#include <random>

using namespace std;
using namespace std::chrono;

const int WIDTH = 30;
const int HEIGHT = 20;
const int SIZE = WIDTH * HEIGHT;

int nbOfPlayer;
int myID;

int index(int row, int col){
    return col + row * WIDTH;
}

class State{
public:

    vector<bitset<SIZE>> player_coords;
    vector<bitset<SIZE>> player_pos;
    bitset<SIZE> obstacles;
    bitset<SIZE> con[4] = {};

    int voronoi_score[4] = {};
    int floofFill_score[4] = {};
    int nbPlayer;
    bool gameOver = false;

    State(){};
    State(vector<bitset<SIZE>> const& playerPositions, bitset<SIZE> const& nonWalkablePoints, vector<bitset<SIZE>> const& playersCoords){

        obstacles = nonWalkablePoints;
        nbPlayer = playerPositions.size();
        player_coords = playersCoords;
        player_pos = playerPositions;

        // activate all connections
        for (int i = 0; i < 4; i++){
            con[i] = ~(con[i]);
        }
        // remove boundary connections
        for (int i = 0; i < WIDTH; i++) {
            con[0].reset(i);
            con[2].reset(i + WIDTH * (HEIGHT - 1));
        }
        for (int i = 0; i < HEIGHT; i++) {
            con[1].reset(i * WIDTH + WIDTH - 1);
            con[3].reset(i * WIDTH);
        }
    };

    vector<bitset<SIZE>> getNextMoves(int playerID){

        vector<bitset<SIZE>> nextMoves = {};

        bitset<SIZE> upDir = ((player_pos[playerID] & con[0]) >> WIDTH) & ~obstacles;
        bitset<SIZE> rhDir = ((player_pos[playerID] & con[1]) << 1)     & ~obstacles;
        bitset<SIZE> dwDir = ((player_pos[playerID] & con[2]) << WIDTH) & ~obstacles;
        bitset<SIZE> lhDir = ((player_pos[playerID] & con[3]) >> 1)     & ~obstacles;        

        if(upDir.any())
            nextMoves.push_back(upDir);

        if(rhDir.any())
            nextMoves.push_back(rhDir);

        if(dwDir.any())
            nextMoves.push_back(dwDir);

        if(lhDir.any())
            nextMoves.push_back(lhDir);                                    

        return nextMoves;
    }    

    vector<State> getChilds(int playerID){

        vector<bitset<SIZE>> nextMoves = getNextMoves(playerID);
        vector<State> childs = {};

        // search the best voronoi score in neighboor cells
        for(bitset<SIZE> nextMove : nextMoves){

            // duplicate the player positions and add replace my actual position by the next possible Move
            vector<bitset<SIZE>> playersTemp(player_pos);
            playersTemp[playerID] = nextMove;

            // add the current + next player position in the obstacles
            bitset<SIZE> obstaclesTemp(obstacles);
            obstaclesTemp |= player_pos[playerID];
            obstaclesTemp |= nextMove;

            // add the current + next player position in the coordsBuffer of state s
            vector<bitset<SIZE>> player_coordsTemp(player_coords);

            player_coordsTemp[playerID] |= player_pos[playerID];
            player_coordsTemp[playerID] |= nextMove; 

            // updating the game state
            childs.push_back(State(playersTemp, obstaclesTemp, player_coordsTemp));  

        }

        return childs;
    }

    int alivePlayers(){

        int livingPlayers = 0;

        for(bitset<SIZE> playerPos : this->player_pos){
            livingPlayers += playerPos.any();
        }

        return livingPlayers;
    }

};

State voronoi(State& s){

    // reset the voronoi scores
    s.voronoi_score[0] = 0;
    s.voronoi_score[1] = 0;
    s.voronoi_score[2] = 0;
    s.voronoi_score[3] = 0;

    // set starting positions for both units
    vector<bitset<SIZE>> cur(s.player_pos);    
    vector<bitset<SIZE>> owned_cells(s.nbPlayer, 0);
    bitset<SIZE> visits{};  


    for (int p = 0; p < s.nbPlayer; p++) {
        owned_cells[p] = cur[p];
        visits |= cur[p];
    }
    
    vector<bool> skip(s.nbPlayer, false);

    while (true){

        for (int i = 0; i < s.nbPlayer; i++){
            
            if (skip[i]) continue;

            cur[i] = 
                (cur[i] & s.con[0])>>WIDTH      //
                | (cur[i] & s.con[1])<<1        //
                | (cur[i] & s.con[2])<<WIDTH    //    
                | (cur[i] & s.con[3])>>1;       //
            
            cur[i] &= ~visits;
            cur[i] &= ~s.obstacles;
        }
        
        vector<bitset<SIZE>> opp_total(s.nbPlayer);

        for (int i = 0; i < s.nbPlayer; i++){
            
            if (skip[i]) continue;

            for (int j = 0; j < s.nbPlayer; j++){
                
                if (i != j) opp_total[i] |= cur[j];                    
            }
        }

        for (int i = 0; i < s.nbPlayer; i++){

            if (skip[i]) continue;
            owned_cells[i] |= cur[i] & (~opp_total[i]);
        }

        bool flag = true;
        for (int i = 0; i < s.nbPlayer; i++){
            skip[i] = !(cur[i].any());
            flag = flag && skip[i];
        }

        if (flag) break;

        for (int i = 0; i < s.nbPlayer; i++){
            visits |= cur[i];
        }
    }

    // output the calculated voronoi scores
    for (int i = 0; i < s.nbPlayer; i++){
        s.voronoi_score[i] = owned_cells[i].count();
    }

    return s;
}

int minimax(State state, int depth, int playerID){

    if(state.player_pos[playerID] == 0)
        return 0;

    if(depth == 0){
        state = voronoi(state);
        return state.voronoi_score[myID];
    }

    bool maximizingPlayer = playerID == myID; 

    if (maximizingPlayer){
        int maxEval = 0;
        for(State child : state.getChilds(playerID)){
            int eval = minimax(child, depth - 1, (playerID + 1) % nbOfPlayer);
            maxEval = std::max(eval, maxEval);  
        }
        return maxEval;
    }
    else{
        int minEval = 600;   
        for(State child : state.getChilds(playerID)){
            int eval = minimax(child, depth - 1, (playerID + 1) % nbOfPlayer);
            minEval = std::min(eval, minEval);
        }
        return minEval;
    }
}

string runMinimax(int myID, State const& gameState){
    
    cerr << "runMinimax" << endl;

    auto start = high_resolution_clock::now();

    map<string, bitset<SIZE>> bitDirections = {};
    map<string, bitset<SIZE>>::iterator it;

    bitDirections["UP"]    = ((gameState.player_pos[myID] & gameState.con[0]) >> WIDTH) & ~gameState.obstacles;
    bitDirections["RIGHT"] = ((gameState.player_pos[myID] & gameState.con[1]) << 1)     & ~gameState.obstacles;
    bitDirections["DOWN"]  = ((gameState.player_pos[myID] & gameState.con[2]) << WIDTH) & ~gameState.obstacles;
    bitDirections["LEFT"]  = ((gameState.player_pos[myID] & gameState.con[3]) >> 1)     & ~gameState.obstacles;

    pair<string, bitset<SIZE>> bestDirection;
    bestDirection.first = "I'LL BE BACK !";
    bestDirection.second = 0;

    int maxScore = 0;
    int maxVoronoi = 0;

    for(it = bitDirections.begin(); it != bitDirections.end(); it++){

        // check if the next position is valid
        if(!it->second.any()) continue;

        vector<bitset<SIZE>> playersTemp(gameState.player_pos);
        playersTemp[myID] = it->second;

        State nextState = State(playersTemp, gameState.obstacles, gameState.player_coords);

        int simScore = minimax(nextState, nbOfPlayer, myID);
        nextState = voronoi(nextState);

        // determinate if it is the best move to play
        if(simScore > maxScore ||simScore == maxScore && nextState.voronoi_score[myID] > maxVoronoi){
            maxScore = std::max(simScore, maxScore);
            maxVoronoi = std::max(maxVoronoi, nextState.voronoi_score[myID]);
            bestDirection.first = it->first;
            bestDirection.second = it->second;
        }
        
        cerr << (it->first) << " : minimax = " << simScore << ", voronoi = " << nextState.voronoi_score[myID] << endl;
    }

    auto full_count = duration_cast<microseconds>(high_resolution_clock::now() - start).count() / 1000;
    cerr << "update time " << full_count << "ms, bestDirection = " << bestDirection.first << endl;

    return bestDirection.first;    
}

int main(){

    // game data buffer
    vector<bitset<SIZE>> playerCoords = {0, 0, 0, 0};
    bitset<SIZE> obstaclesMap = {};

    while (true) {

        auto start = high_resolution_clock::now();

        cin >> nbOfPlayer >> myID; cin.ignore();
        
        vector<bitset<SIZE>> players = {0, 0, 0, 0};

        for (int i = 0; i < nbOfPlayer; i++){

            int col0; // starting X coordinate of lightcycle (or -1)
            int row0; // starting Y coordinate of lightcycle (or -1)
            int col1; // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
            int row1; // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)
            
            std::cin >> col0 >> row0 >> col1 >> row1; cin.ignore();

            // add the location of the current player
            bitset<SIZE> playerPos = 0;

            // check if the current player is alive
            if(col0 > -1){

                // calculate the bitset indexes of both positions
                int index0 = index(row0, col0);
                int index1 = index(row1, col1);                 
                
                // set the current player position
                playerPos.set(index1);

                // put all player positions in the same bitset
                obstaclesMap.set(index0);
                obstaclesMap.set(index1);

                // keep trace of players positions
                playerCoords[i].set(index0);
                playerCoords[i].set(index1);
            }
            else if(playerCoords[i].any()){

                // remove all dead players positions from the obstacle bitset
                obstaclesMap ^= playerCoords[i];
                
                // empty all current player positions to avoid useless operations
                playerCoords[i] = 0;
            }
            
            // add the current player position into buffer
            players[i] = playerPos;
        }

        // update the actual game state
        State gameState(players, obstaclesMap, playerCoords);

        // calculate the best direction to move on
        string bestDirection = runMinimax(myID, gameState);
        
        // write the outputs
        std::cout << bestDirection << std::endl; // A single line with UP, DOWN, LEFT or RIGHT
    }
}