#include <iostream>
using namespace std;

class Account {
public:
    virtual void calculateInterest() {
        cout << "General account interest" << endl;
    }
};

class SavingsAccount : public Account {
public:
    void calculateInterest() override {
        cout << "Savings account interest: 5%" << endl;
    }
};

class CurrentAccount : public Account {
public:
    void calculateInterest() override {
        cout << "Current account interest: 2%" << endl;
    }
};

int main() {
    Account *a;

    SavingsAccount s;
    CurrentAccount c;

    a = &s;
    a->calculateInterest();

    a = &c;
    a->calculateInterest();

    return 0;
}