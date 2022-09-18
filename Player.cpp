#include <iostream>
#include <string>
#include <utility>
#include <vector>
#include <algorithm>
#include <map>

using namespace std;

// INCLUDES


class Unit;
class Game;
class Tile;

void WAIT(string message);

// GAME CLASS

class Unit : public Game{

    private:

        int m_id;
        int m_hp;
        int m_row;
        int m_col;
        int m_ownerID;
        int m_type;

        bool isInGame;

    public:

        Unit(int id, int hp, int row, int col, int owner, int type){
            m_id = id;
            m_hp = hp;
            m_row = row;
            m_col = col;
            m_ownerID = owner;
            m_type = type;

            isInGame = true;    
        }

        // CONST METHODS

        void move(const int &row, const int &col) const {
            std::cout << m_id << " MOVE " << row << col << std::endl;
        }

        void shoot(const int &targetID) const {
            std::cout << m_id << " SHOOT " << targetID << std::endl;
        }

        void convert(const int &targetID) const {
            std::cout << m_id << " CONVERT " << targetID << std::endl;
        }

        int getRow() const {
            return 0;
        }

        int getCol() const{
            return 0;
        }

        int getHp() const{
            return 0;
        }

        bool isFriend() const{
            return 0;
        }

        bool isEnnemy() const{
            return 0;
        }

        bool isNeutral() const{
            return 0;
        }

        void takeDamage(int const &damage){

        }
};

class Game{
    
    public:

        Game(){

        }

        enum unitType{
            cultist,
            cultLeader
        };

        void initParsing(){

            cin >> m_myId;
            cin.ignore();

            cin >> m_width >> m_height;
            cin.ignore();

            for (int i = 0; i < m_height; i++) {
                string charLine;
                cin >> charLine; cin.ignore();
            }
        }

        void updateParsing(){

            int num_of_units;
            cin >> num_of_units; cin.ignore();

            for (int i = 0; i < num_of_units; i++) {

                int id;    // The unit's ID
                int type;  // The unit's type: 0 = Cultist, 1 = Cult Leader
                int hp;    // Health points of the unit
                int col;   // X coordinate of the unit
                int row;   // Y coordinate of the unit
                int owner; // id of owner player

                cin >> id >> type >> hp >> col >> row >> owner;
                cin.ignore();

                Unit unit = Unit(id, hp, row, col, owner, type);
            }            

        }

    protected:

        int m_myId;    // 0 - you are the first player, 1 - you are the second player
        int m_width;   // Width of the board
        int m_height;  // Height of the board

        vector<Unit> m_myUnits;
        vector<Unit> m_ennemyUnits;
        vector<Unit> m_neutralUnits;

        map<string, std::pair<int, int>> m_obstaclesCoords;
        map<string, Unit> m_unitsMap;    

};

// MAIN

int main(){
    
    Game game = Game();

    game.initParsing();

    while (1) {

        game.updateParsing();

        // WAIT | unitId MOVE x y | unitId SHOOT target| unitId CONVERT target
        cout << "WAIT" << endl;
    }
}

// GENERIC FUNCTIONS

void WAIT(string message){
    std::cout << "WAIT " << message;
}