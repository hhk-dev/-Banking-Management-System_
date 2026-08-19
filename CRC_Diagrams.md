# CS(263)-ASSIGNMENT-02
# BANKING MANAGEMENT SYSTEM
## Topic: CRC Diagram

#### Group Member
## 20251501040 Rudra Bavaliya (Leader)
## 20251501086 Harsh Kanzariya
## 20521501041 Bhakti Bhuva
## 20251501130 Rushi Patel
## 20251501119 Nirja Paghadal

### Class: Account
**Methods (Operations):**
* Create Account
* Deposit Money
* Withdraw Money
* View Balance
* Transfer Money

**Collaborators:** Customer, Transaction, Employee

### Class: Customer
**Methods (Operations):**
* Authenticate Login
* Create Account
* Request for Loan
* Deposit Money
* View Transaction
* Apply For Loan

**Collaborators:** Customer, Transaction, Employee

### Class: Transaction
**Methods (Operations):**
* Record Transaction
* View Transaction
* Get Transaction Details
* Validate Transaction

**Collaborators:** Account, Employee, Customer

### Class: Loan
**Methods (Operations):**
* Request Loan
* Calculate Interest
* Approve Loan
* View Loan Details
* Set Loan Terms
* Set Loan Types

**Collaborators:** Customer, Manager, Account

### Class: Employee
**Methods (Operations):**
* Authenticate User
* Process Transaction
* View Transaction
* Generate Report
* Verify Customer

**Collaborators:** Customer, Manager, Account

### Class: Manager
**Methods (Operations):**
* Approve Loan
* Authenticate User
* View Transaction
* Review Loan Request
* System Maintenance
* Manage Employees
* Generate Reports

**Collaborators:** Loan, Employee, Account, Transaction
