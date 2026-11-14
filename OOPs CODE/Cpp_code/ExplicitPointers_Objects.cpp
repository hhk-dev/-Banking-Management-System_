#include <iostream>
using namespace std;

class BankAccount {
public:
    int balance;

    BankAccount(int b) {
        balance = b;
    }

    void display() {
        cout << "Account Balance: " << balance << endl;
    }
};

int main() {
    BankAccount a1(5000);

    // Pointer stores the address of account object
    BankAccount *ptr = &a1;

    // Accessing object using pointer
    ptr->display();

    ptr->balance = 7000;

    cout << "Updated Balance: " << ptr->balance << endl;

    return 0;
}