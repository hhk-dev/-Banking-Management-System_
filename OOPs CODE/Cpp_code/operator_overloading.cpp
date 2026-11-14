#include <iostream>
using namespace std;

class BankAccount {
public:
    int balance;

    BankAccount(int b) { 
        // Parametried constructor that we use to initailze balance of account
        balance = b;     
    
    }

    // addtion operator overload
    BankAccount operator+(BankAccount obj) { 
        BankAccount temp(0);
        temp.balance = balance + obj.balance;
        return temp;
    }

    void display() {
        cout << "Balance: " << balance << endl;
    }
};

int main() {
    BankAccount a1(5000);
    BankAccount a2(3000);

    BankAccount a3 = a1 + a2;
    // upr ka statement is interpreted as BankAccount a3 = a1.operator+(a2);
    // adds te balance of both accounts using user-defined operator overloading
    // addition operator is used in this example 

    a3.display();

    return 0;
}
