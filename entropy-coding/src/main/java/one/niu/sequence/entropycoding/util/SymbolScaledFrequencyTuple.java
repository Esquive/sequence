package one.niu.sequence.entropycoding.util;

public class SymbolScaledFrequencyTuple {

  private final int symbol;
  private final int scaledFrequency;

  public SymbolScaledFrequencyTuple(int symbol, int scaledFrequency) {
    this.symbol = symbol;
    this.scaledFrequency = scaledFrequency;
  }

  public int getSymbol() {
    return symbol;
  }

  public int getScaledFrequency() {
    return scaledFrequency;
  }

  @Override
  public int hashCode() {
    return symbol;
  }
}
