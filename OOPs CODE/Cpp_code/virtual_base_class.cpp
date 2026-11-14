
#include <iostream>
using namespace std;

class BankAccount {
public:
    int balance;

    BankAccount() {
        balance = 5000;
        cout << "Bank Account constructor used" << endl;
    }
    void display(){
        cout << "Balance: " << balance << endl;
        }
};

class SavingsAccount : virtual public BankAccount {
    public:
    void calculateInterest() {
        cout << "Savings Account Interest: "
             << balance * 0.05 << endl;
    }
};

class CurrentAccount : virtual public BankAccount { 
    public:
    void checkMinimumBalance() {
        cout << "Current Account Balance: "
             << balance << endl;
    }   
};

class HybridAccount : public SavingsAccount,public CurrentAccount {
};

int main() {
    HybridAccount obj;

    cout << "\nBank Account Details:" << endl;
    obj.display();

    cout << "\nSavings Account:" << endl;
    obj.calculateInterest();

    cout << "\nCurrent Account:" << endl;
    obj.checkMinimumBalance();

    return 0;
}
/*
Virtual inheritance solves the Diamond Problem by preventing duplicate base-class subobjects.
*/