package one.niu.sequence.ans.util;

import one.niu.sequence.ans.encoding.TANSEncoder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class FrequencyTable {

  private final Integer scaleTarget;
  private Integer[] frequencies;
  private Integer[] scaledFrequencies;
  private Integer total;

  private Integer correction;

  public FrequencyTable(int scaleTarget, Integer[] frequencies) {
    this.scaleTarget = scaleTarget;
    this.frequencies = frequencies;
    this.total = Arrays.stream(frequencies).reduce(0,(a, b) -> a+b);

    this.scaledFrequencies = new Integer[frequencies.length];

   this.correction = scaleTarget;
  }

  private void scale(){
    double symbolScaledFrequency = -1.0;
    int scaledDownValue = -1;

    for(int i = 0; i < frequencies.length; i++){
      symbolScaledFrequency = ((double)(this.frequencies[i] * this.scaleTarget)) / ((double)total);
      scaledDownValue = (int)symbolScaledFrequency;
      this.scaledFrequencies[i] = ( (symbolScaledFrequency * symbolScaledFrequency) <= (scaledDownValue * (scaledDownValue + 1)) ) ? scaledDownValue : scaledDownValue + 1;
      correction -= this.scaledFrequencies[i];
    }
  }

  private void correctScaling() {
    final Double[] symbolProbabilities = new Double[this.frequencies.length];
    for(int i=0; i<symbolProbabilities.length; i++){
      symbolProbabilities[i] = (double)frequencies[i] / (double) total;
    }

    final int correctionSign = (correction > 0) ? 1 : -1;
    final LinkedList<SymbolCorrectionDeltaTuple> deltas = new LinkedList<>();
    final PriorityQueue<SymbolCorrectionDeltaTuple> heap = new PriorityQueue<>(new SymbolCorrectionHeapOrdering());

    for(int i = 0; i < scaledFrequencies.length; i++){
      if(scaledFrequencies[i] > 1){
        heap.add(new SymbolCorrectionDeltaTuple(
          i,
          this.calculateDelta( symbolProbabilities[i], scaledFrequencies[i], correctionSign)));
      }
    }

    SymbolCorrectionDeltaTuple iterator;
    while(this.correction != 0){
      iterator = heap.poll();
      this.scaledFrequencies[iterator.getSymbol()] += correctionSign;
      this.correction -= correctionSign;
      if(scaledFrequencies[iterator.getSymbol()] > 1){
        iterator.setDelta(this.calculateDelta(symbolProbabilities[iterator.getSymbol()], scaledFrequencies[iterator.getSymbol()],correctionSign));
        heap.add(iterator);
      }
    }
  }



  private Double calculateDelta(Double probability, Integer scaledFrequency, int correctionSign){
    return probability * Math.log10( ((double)scaledFrequency) / ((double)scaledFrequency + (double)correctionSign) ) / Math.log10(2.0);
  }



  /**
   * Inner class for the sort order of the tuples.
   */
  private class SymbolCorrectionHeapOrdering implements Comparator<SymbolCorrectionDeltaTuple> {

    @Override
    public int compare(SymbolCorrectionDeltaTuple o1, SymbolCorrectionDeltaTuple o2) {
      return Double.compare(o1.getDelta(), o2.getDelta());
    }
  }

  /**
   * Inner class for the ordering of the states in the encoding.
   */
  private class StateEncodingComparable implements Comparator<StateEncodingTuple>{

    @Override
    public int compare(StateEncodingTuple o1, StateEncodingTuple o2) {
      return Integer.compare(o1.getState(),o2.getState());
    }
  }


}
