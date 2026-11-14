#include <iostream>
#include "BankAccount.h"
using namespace std;
BankAccount::BankAccount(int accNo, double bal)
{
    accountNumber = accNo;
    balance = bal;
}
void BankAccount::deposit(double amount)
{
    balance = balance + amount;
}
void BankAccount::withdraw(double amount)
{
    if (amount <= balance)
    {
        balance = balance - amount;
    }
    else
    {
        cout << "Insufficient balance" << endl;
    }
}
void BankAccount::display() const
{
    cout << "Account Number: " << accountNumber << endl;
    cout << "Balance: " << balance << endl;
}