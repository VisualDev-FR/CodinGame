#include <cstdlib>
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <map>

using namespace std;

int main()
{
    int n;
    cin >> n; cin.ignore();

    int ecartMin = 2147483647;
    vector<int> powerMap;

    for (int i = 0; i < n; i++) {
        int pi;
        cin >> pi; cin.ignore();

        powerMap.push_back(pi);
    }

    std::sort(powerMap.begin(), powerMap.end());

    for(int i = 1; i < powerMap.size(); i++){

        int key1 = powerMap[i - 1];
        int key2 = powerMap[i];

        ecartMin = std::min(ecartMin, abs(key1 - key2));
    }

    cout << ecartMin << endl;
}