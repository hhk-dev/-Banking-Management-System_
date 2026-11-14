#ifndef BANKACCOUNT_H
#define BANKACCOUNT_H
class BankAccount
{
private:
    int accountNumber;
    double balance;
public:
    BankAccount(int accNo, double bal);
    void deposit(double amount);
    void withdraw(double amount);
    void display() const;
};
#endif