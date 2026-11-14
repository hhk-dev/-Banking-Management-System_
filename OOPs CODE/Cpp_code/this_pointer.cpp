
#include <iostream>
using namespace std;

class BankAccount {
private:
    int balance;

public:
    BankAccount(int balance) {
        this->balance = balance;
        /*
        this->balance ye class ka data member hai
        balance ye consturctor parameter hai jo main me se lena hai 
        */
       /*
       this->balance is equivalent to (*this).balance
       only used in non static member function
       solves naming conflict
       */
    }

    void display() {
        cout << "Balance: "
             << this->balance << endl;
    }
};

int main() {
    BankAccount a1(9000);

    a1.display();

    return 0;
}
