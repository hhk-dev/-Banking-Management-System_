#include <iostream>
using namespace std;
class BankAccount
{
private:
    int accountNumber;
    double balance;
public:
    BankAccount(int accNo, double bal)
    {
        accountNumber = accNo;
        balance = bal;
    }
    double getBalance() const
    {
        return balance;
    }
    void display() const
    {
        cout << "Account Number: " << accountNumber << endl;
        cout << "Balance: " << balance << endl;
    }
    void deposit(double amount)
    {
        balance = balance + amount;
    }
};
void printAccount(const BankAccount& acc)
{
    acc.display();
}
int main()
{
    BankAccount a1(101, 5000);
    a1.deposit(1000);
    const BankAccount a2(102, 3000);
    a2.display();
    printAccount(a1);
    cout << "Balance of a1: " << a1.getBalance() << endl;
    return 0;
}