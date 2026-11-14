#include <iostream>
using namespace std;
#define BANK_NAME "ABC National Bank"
#define MIN_BALANCE 1000
#define INTEREST(amount, rate) ((amount) * (rate) / 100)
#define DEBUG
#ifndef TAX_RATE
#define TAX_RATE 10
#endif
class BankAccount
{
private:
    int balance;
public:
    BankAccount(int b)
    {
        balance = b;
#ifdef DEBUG
        cout << "Account created with balance " << balance << endl;
#endif
    }
    void display()
    {
        cout << "Bank: " << BANK_NAME << endl;
        cout << "Balance: " << balance << endl;
#if MIN_BALANCE > 0
        if (balance < MIN_BALANCE)
        {
            cout << "Warning: Balance is below minimum" << endl;
        }
#endif
    }
    int getBalance()
    {
        return balance;
    }
};
#undef DEBUG
int main()
{
    BankAccount a1(5000);
    a1.display();
    cout << "Interest at 5%: " << INTEREST(a1.getBalance(), 5) << endl;
    cout << "Tax rate: " << TAX_RATE << "%" << endl;
    cout << "File: " << __FILE__ << endl;
    cout << "Line: " << __LINE__ << endl;
    return 0;
}