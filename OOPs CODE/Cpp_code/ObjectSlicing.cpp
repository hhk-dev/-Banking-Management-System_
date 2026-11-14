#include <iostream>
using namespace std;

class BankAccount {
public:
    int balance;

    BankAccount(int b) {
        balance = b;
    }

    void display() {
        cout << "Balance: " << balance << endl;
    }
};

class SavingsAccount : public BankAccount {
public:
    int interest;

    SavingsAccount(int b, int i)
        : BankAccount(b) {
        interest = i;
    }
};

int main() {
    SavingsAccount s1(10000, 500);

    BankAccount b1 = s1;

    b1.display();


    return 0;
}