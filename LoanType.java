package BankSystem;
public enum LoanType {
    HOME(7.5),  PERSONAL(12.0),  EDUCATION(9.0);

    private final double annualRatePercent;

    LoanType(double annualRatePercent) {
        this.annualRatePercent = annualRatePercent;
    }
    public double getAnnualRatePercent() {
        return annualRatePercent;
    }
}
