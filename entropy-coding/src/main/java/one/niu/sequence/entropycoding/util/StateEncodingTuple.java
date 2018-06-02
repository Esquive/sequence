package one.niu.sequence.entropycoding.util;

public class StateEncodingTuple {

  private int state;
  private final int symbol;

  public StateEncodingTuple(int state, int symbol) {
    this.state = state;
    this.symbol = symbol;
  }

  public int getState() {
    return state;
  }

  public int getSymbol() {
    return symbol;
  }

  public void setState(int state) {
    this.state = state;
  }

  @Override
  public int hashCode() {
    return state * 31 + symbol;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof StateEncodingTuple){
      return ((StateEncodingTuple)obj).getSymbol() == this.symbol && ((StateEncodingTuple)obj).getState() == this.state;
    }
    return false;
  }
}
