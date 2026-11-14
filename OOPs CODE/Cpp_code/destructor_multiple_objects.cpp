
#include <iostream>
using namespace std;

class BankAccount {
public:
    string name;

    BankAccount(string objName) {
        name = objName;
        cout << name << " Created" << endl;
    }

    ~BankAccount() {
        cout << name << " Destroyed" << endl;
    }
};
/*
Constructor follows FIFO order, and destructor follows LIFO order.
*/
int main() {
    BankAccount a1("a1");
    BankAccount a2("a2");
    BankAccount a3("a3");

    return 0;
}
