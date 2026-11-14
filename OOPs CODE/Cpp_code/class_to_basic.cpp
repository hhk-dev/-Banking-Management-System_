#include <iostream>
using namespace std;

class BankAccount {
private:
    int balance;

public:
    BankAccount(int b) {
        balance = b;
    }
    void display(){
        cout << "Account Balance (object): " << balance << endl;
    }
    // this is the conversion operator used for type casting 
    operator int() {
        return balance;
    }
};

int main() {
    BankAccount a1(7500);
    a1.display();

    int amount;

    amount = a1;
    /*
    converts object(a1) to integer(amount) 
    */
    cout << "Integer: " << amount << endl;

    return 0;
}
