#include <iostream>
using namespace std;

class BankAccount {
public:
    int* balance;

    BankAccount(int b) {
        
        balance = new int(b);
    }

    void display() {
        cout << "Account Balance: "
             << *balance << endl;
    }

    ~BankAccount() {
    
        delete balance;

        cout << "Memory Released" << endl;
    }
};

int main() {

    BankAccount *ptr = new BankAccount(15000);

    ptr->display();

    delete ptr;

    return 0;
}