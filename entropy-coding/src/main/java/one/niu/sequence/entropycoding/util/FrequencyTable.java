package one.niu.sequence.entropycoding.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class FrequencyTable implements Iterable<SymbolScaledFrequencyTuple> {

  private final Integer scaleTarget;
  private Integer[] frequencies;
  private Integer[] scaledFrequencies;
  private Integer total;

  private Integer correction;

  public FrequencyTable(int scaleTarget, Integer[] frequencies) {
    this.scaleTarget = scaleTarget;
    this.frequencies = frequencies;
    this.total = Arrays.stream(frequencies).reduce(0, (a, b) -> a + b);

    this.scaledFrequencies = new Integer[frequencies.length];

    this.correction = scaleTarget;

    this.scale();
    this.correctScaling();
  }

  private void scale() {
    double symbolScaledFrequency = -1.0;
    int scaledDownValue = -1;

    for (int i = 0; i < frequencies.length; i++) {
      symbolScaledFrequency = ((double) (this.frequencies[i] * this.scaleTarget)) / ((double) total);
      scaledDownValue = (int) symbolScaledFrequency;
      this.scaledFrequencies[i] = ((symbolScaledFrequency * symbolScaledFrequency) <= (scaledDownValue * (scaledDownValue + 1))) ? scaledDownValue : scaledDownValue + 1;
      correction -= this.scaledFrequencies[i];
//      System.out.println(correction);
    }
  }

  private void correctScaling() {
    final Double[] symbolProbabilities = new Double[this.frequencies.length];
    for (int i = 0; i < symbolProbabilities.length; i++) {
      symbolProbabilities[i] = (double) frequencies[i] / (double) total;
    }

    final int correctionSign = (correction > 0) ? 1 : -1;
    final PriorityQueue<SymbolCorrectionDeltaTuple> heap = new PriorityQueue<>(new SymbolCorrectionHeapOrdering());

    for (int i = 0; i < scaledFrequencies.length; i++) {
      if (scaledFrequencies[i] > 1) {
        heap.add(new SymbolCorrectionDeltaTuple(
          i,
          this.calculateDelta(symbolProbabilities[i], scaledFrequencies[i], correctionSign)));
      }
    }

    SymbolCorrectionDeltaTuple iterator;
    while (this.correction != 0) {
      iterator = heap.poll();
      this.scaledFrequencies[iterator.getSymbol()] += correctionSign;
      this.correction -= correctionSign;
      if (scaledFrequencies[iterator.getSymbol()] > 1) {
        iterator.setDelta(this.calculateDelta(symbolProbabilities[iterator.getSymbol()], scaledFrequencies[iterator.getSymbol()], correctionSign));
        heap.add(iterator);
      }
    }
  }

  private Double calculateDelta(Double probability, Integer scaledFrequency, int correctionSign) {
    return probability * Math.log10(((double) scaledFrequency) / ((double) scaledFrequency + (double) correctionSign)) / Math.log10(2.0);
  }

  @Override
  public Iterator<SymbolScaledFrequencyTuple> iterator() {
    return new Iterator<SymbolScaledFrequencyTuple>() {
      int current = 0;

      @Override
      public boolean hasNext() {
        return current < scaledFrequencies.length;
      }

      @Override
      public SymbolScaledFrequencyTuple next() {
        return new SymbolScaledFrequencyTuple(current, scaledFrequencies[current++]);
      }
    };
  }


  public int getScaledFrequencyForSymbol(int symbol) {
    //TODO: Make tests
    return scaledFrequencies[symbol];
  }

  //TODO: Find another solution for this.
  public void incrementScaledFrequencyForSymbol(int symbol) {
    scaledFrequencies[symbol]++;
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

}
