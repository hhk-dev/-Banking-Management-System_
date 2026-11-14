
#include <iostream>
using namespace std;

class BankAccount {
private:
    int *balance;

public:
    BankAccount(int b) {
        balance = new int;

        *balance = b;

        cout << "Account Created" << endl;
    }

    void display() {
        cout << "Balance: " << *balance << endl;
    }

    ~BankAccount() {
        delete balance;

        cout << "Memory Released" << endl;
    }
};

int main() {
    BankAccount a1(10000);

    a1.display();

    return 0;
}
