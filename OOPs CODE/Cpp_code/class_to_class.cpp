
#include <iostream>
using namespace std;

class SavingsAccount {
public:
    int balance;

    SavingsAccount(int b) {
        balance = b;
    }
    void display() {
        cout << "Savings Account Balance: "
             << balance << endl;
    }
};

class CurrentAccount {
public:
    int balance;
    /*
    ye hamara conversion constructor hai, ye object ko accept karke uski copy save karta hai
    */

    CurrentAccount(SavingsAccount s) {
        balance = s.balance;
    }

    void display() {
        cout << "Current Account Balance: "
             << balance << endl;
    }
};

int main() {
    SavingsAccount s1(10000);
    s1.display();
    /*
    converts object of savings account class to current account class
    */
    CurrentAccount c1 = s1;

    c1.display();

    return 0;
}
