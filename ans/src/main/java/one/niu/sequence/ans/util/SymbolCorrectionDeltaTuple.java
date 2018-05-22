package one.niu.sequence.ans.util;

public class SymbolCorrectionDeltaTuple {

  private final int symbol;
  private double delta;

  public SymbolCorrectionDeltaTuple(int symbol, double delta) {
    this.symbol = symbol;
    this.delta = delta;
  }

  public int getSymbol() {
    return symbol;
  }

  public double getDelta() {
    return delta;
  }

  public void setDelta(double delta) {
    this.delta = delta;
  }
}
