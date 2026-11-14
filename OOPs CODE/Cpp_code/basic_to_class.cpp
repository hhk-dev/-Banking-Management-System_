
#include <iostream>
using namespace std;

class BankAccount {
private:
    int balance;

public: 
    // this is the conversion constructor used to convert integer(amount) to class object
    BankAccount(int amount) {
        balance = amount;
    }

    void display() {
        cout << "Account Balance: " << balance << endl;
    }
};

int main() {
    int amount = 5000;
    cout << "Integer amount : " << amount << endl ;

    BankAccount a1 = amount; // changes integer(amount) to object (a1)

    a1.display();

    return 0;
}
