
#include <iostream>
using namespace std;
/* friend keyword allows external function to use private and protected members 
of a class 
*/
class BankAccount {
private:
    int accountNumber;
    double balance;

public:
    friend istream& operator>>(istream& in,BankAccount& obj);

    friend ostream& operator<<(ostream& out,const BankAccount& obj);
};
/* istram& gives reference to te input stream, which allows  us to use direct 
input instead of a copy. */
istream& operator>>(istream& in, BankAccount& obj) {
    cout << "Enter account number: ";
    in >> obj.accountNumber;

    cout << "Enter balance: ";
    in >> obj.balance;

    return in;
}

ostream& operator<<(ostream& out,const BankAccount& obj) {
    out << "Account Number: " << obj.accountNumber << endl;
    out << "Balance: " << obj.balance << endl;

    return out;
}

int main() {
    BankAccount a1;

    cin >> a1;

    cout << "\nAccount Details:\n";
    cout << a1;

    return 0;
}
/*
istream → Input → cin → >>
ostream → Output → cout → <<
friend → Access private members.
& → Pass/return by reference.
const → Prevent modification.
return in/out → Supports operator chaining.
*/