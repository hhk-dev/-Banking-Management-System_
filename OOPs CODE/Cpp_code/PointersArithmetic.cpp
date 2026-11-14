#include <iostream>
using namespace std;

int main() {

    int transactions[5] = {
        1000, 2000, 3000, 4000, 5000
    };

    int *ptr = transactions;

    cout << "Transaction Details:" << endl;

    for (int i = 0; i < 5; i++) {

        cout << "Transaction "
             << i + 1 << ": " << *ptr << endl;

        ptr++;
    }

    return 0;
}