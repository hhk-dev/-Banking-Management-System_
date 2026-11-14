#include <iostream>
using namespace std;

class BankAccount {
private:
    int accountNumber;
    int balance;

public:
    BankAccount(int accNo, int amount) {
        accountNumber = accNo;
        balance = amount;
    }

    friend class BankManager;
};

class BankManager {
public:
    void displayDetails(BankAccount a) {
        cout << "Account Number: " << a.accountNumber << endl;
        cout << "Account Balance: " << a.balance << endl;
    }
};

int main() {
    BankAccount a1(101, 10000);

    BankManager manager;

    manager.displayDetails(a1);

    return 0;
}