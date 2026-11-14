#include <iostream>
using namespace std;
class BankAccount
{
private:
    int accountNumber;
    double balance;
    mutable int balanceCheckCount;
public:
    BankAccount(int accNo, double bal)
    {
        accountNumber = accNo;
        balance = bal;
        balanceCheckCount = 0;
    }
    double getBalance() const
    {
        balanceCheckCount++;
        return balance;
    }
    void display() const
    {
        cout << "Account Number: " << accountNumber << endl;
        cout << "Balance: " << balance << endl;
        cout << "Balance checked " << balanceCheckCount << " times" << endl;
    }
};
int main()
{
    const BankAccount a1(101, 8000);
    a1.getBalance();
    a1.getBalance();
    a1.getBalance();
    a1.display();
    return 0;
}