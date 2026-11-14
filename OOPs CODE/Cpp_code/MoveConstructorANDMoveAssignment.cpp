#include <iostream>
#include <utility>
using namespace std;

class BankAccount {
public:
    int* transactions;
    int size;

    BankAccount(int s) {
        size = s;
        transactions = new int[size];

        for (int i = 0; i < size; i++) {
            transactions[i] = (i + 1) * 1000;
        }
    }

    // Move constructor
    BankAccount(BankAccount&& obj) {
        transactions = obj.transactions;
        size = obj.size;

        obj.transactions = nullptr;
        obj.size = 0;
    }

    // Move assignment operator
    BankAccount& operator=(BankAccount&& obj) {
        if (this != &obj) {
            delete[] transactions;

            transactions = obj.transactions;
            size = obj.size;

            obj.transactions = nullptr;
            obj.size = 0;
        }

        return *this;
    }

    void display() {
        for (int i = 0; i < size; i++) {
            cout << transactions[i] << " ";
        }
        cout << endl;
    }

    ~BankAccount() {
        delete[] transactions;
    }
};

int main() {
    BankAccount a1(3);

    // Calling move constructor
    BankAccount a2 = move(a1);

    cout << "Moved Transaction Records: ";
    a2.display();

    BankAccount a3(2);

    // Calling move assignment
    a3 = move(a2);

    cout << "After Move Assignment: ";
    a3.display();

    return 0;
}