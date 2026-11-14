#include <iostream>
using namespace std;

class BankAccount {
public:
    virtual void display() {
        cout << "General Bank Account" << endl;
    }
};

class SavingsAccount : public BankAccount {
public:
    void display() override {
        cout << "Savings Account Details" << endl;
    }
};

class CurrentAccount : public BankAccount {
public:
    void display() override {
        cout << "Current Account Details" << endl;
    }
};

int main() {
    SavingsAccount s1;
    CurrentAccount c1;

    BankAccount *ptr;

    ptr = &s1;
    ptr->display();

    ptr = &c1;
    ptr->display();

    return 0;
}