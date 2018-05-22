package one.niu.sequence.ans.encoding;

import one.niu.sequence.ans.util.BitOutputStream;
import one.niu.sequence.ans.util.ByteBufferOutputStream;
import one.niu.sequence.ans.util.StateEncodingTuple;
import one.niu.sequence.ans.util.SymbolCorrectionDeltaTuple;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class TANSEncoder implements Closeable {

  //Constructor Stuff
  private final OutputStream outputStream;
  private Integer[] frequencies;
  private final Integer trueTotal;
  private Double[] trueProbabilities;


  //Entropy Coding parameters
  private final int precision;
  private final int m;
  private int[] scaledFrequencies;
  private int correction;

  //Variables for algorithm execution
  private final HashMap<StateEncodingTuple,Integer> stateTable;
  private int currentState;

  private final ByteBufferOutputStream encodingBuffer;
  private final BitOutputStream bitOutputStream;

  public TANSEncoder(OutputStream outputStream, Integer[] frequencies) {

    this.outputStream = outputStream;
    this.frequencies = frequencies;
    this.trueTotal = Arrays.stream(this.frequencies).reduce(0, (a,b) -> a + b);
    this.trueProbabilities = new Double[frequencies.length];
    Arrays.fill(this.trueProbabilities,0.0);
    for(int i = 0 ; i < this.frequencies.length ; i++){
      this.trueProbabilities[i] = ((double)this.frequencies[i]) / ((double)trueTotal);
    }

    this.precision = (int)Math.floor( Math.log10( frequencies.length ) / Math.log10(2) ) + 3;
    this.m = 1 << precision;

    this.scaledFrequencies = new int[frequencies.length];
    Arrays.fill(scaledFrequencies, 0);
    this.correction = m;

    this.currentState = (m << 1) - 1;

    this.encodingBuffer = new ByteBufferOutputStream(500 * 1024 * 1024);
    this.bitOutputStream = new BitOutputStream(this.encodingBuffer);

    this.stateTable = new HashMap<>();

    this.scale();
    if(correction != 0)    this.correctScaling();
    this.buildStateTable();


    //TODO: Make them method parameters and return values.
    this.frequencies = null;
    this.trueProbabilities = null;

  }

  private void scale() {
    double symbolScaledFrequency = -1.0;
    int scaledDownValue = -1;

    for(int i = 0; i < frequencies.length; i++){
      symbolScaledFrequency = ((double)(this.frequencies[i] * m)) / ((double)trueTotal);
      scaledDownValue = (int)symbolScaledFrequency;
      this.scaledFrequencies[i] = ( (symbolScaledFrequency * symbolScaledFrequency) <= (scaledDownValue * (scaledDownValue + 1)) ) ? scaledDownValue : scaledDownValue + 1;
      correction -= this.scaledFrequencies[i];
    }
  }

  private void correctScaling() {
    final int correctionSign = (correction > 0) ? 1 : -1;
    final LinkedList<SymbolCorrectionDeltaTuple> deltas = new LinkedList<>();
    final PriorityQueue<SymbolCorrectionDeltaTuple> heap = new PriorityQueue<>(new SymbolCorrectionHeapOrdering());

    for(int i = 0; i < scaledFrequencies.length; i++){
      if(scaledFrequencies[i] > 1){
        heap.add(new SymbolCorrectionDeltaTuple(
          i,
          this.calculateDelta(i, correctionSign)));
      }
    }

    SymbolCorrectionDeltaTuple iterator;
    while(this.correction != 0){
      iterator = heap.poll();
      this.scaledFrequencies[iterator.getSymbol()] += correctionSign;
      this.correction -= correctionSign;
      if(scaledFrequencies[iterator.getSymbol()] > 1){
        iterator.setDelta(this.calculateDelta(iterator.getSymbol(), correctionSign));
        heap.add(iterator);
      }
    }
  }

  private void buildStateTable() {
    PriorityQueue<StateEncodingTuple> order = new PriorityQueue<>(new StateEncodingComparable());
    for(int i = 0; i < scaledFrequencies.length; i++){
      for(int position = 1; position <= scaledFrequencies[i]; position++) {

        order.add( new StateEncodingTuple(
           (int)Math.round( this.m * position / (double)scaledFrequencies[i]), i) );

      }
    }

    StateEncodingTuple iterator;
    int nextState = 0;
    while(order.size() > 0){
      iterator = order.poll();
      iterator.setState(this.scaledFrequencies[iterator.getSymbol()]);
      this.stateTable.put(iterator, nextState+this.m);

      //Prepare for next iteration
      this.scaledFrequencies[iterator.getSymbol()] += 1;
      nextState++;
    }

  }


  private Double calculateDelta(int symbol, int correctionSign){
    return trueProbabilities[symbol] * Math.log10( ((double)scaledFrequencies[symbol]) / ((double)scaledFrequencies[symbol] + (double)correctionSign) ) / Math.log10(2.0);
  }


  @Override
  public void close() throws IOException {

  }

}


