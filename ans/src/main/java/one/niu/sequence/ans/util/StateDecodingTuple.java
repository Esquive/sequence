package one.niu.sequence.ans.util;

public class StateDecodingTuple {

  private final Integer symbol;
  private final Integer previousState;

  public StateDecodingTuple(Integer symbol, Integer previousState) {
    this.symbol = symbol;
    this.previousState = previousState;
  }

  public Integer getSymbol() {
    return symbol;
  }

  public Integer getPreviousState() {
    return previousState;
  }
}
