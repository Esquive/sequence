package one.niu.sequence.ans.encoding;

import one.niu.sequence.ans.util.BitOutputStream;
import one.niu.sequence.ans.util.ByteBufferOutputStream;
import one.niu.sequence.ans.util.FrequencyTable;
import one.niu.sequence.ans.util.ReverseByteBufferInputStream;
import one.niu.sequence.ans.util.StateEncodingTuple;
import one.niu.sequence.ans.util.SymbolScaledFrequencyTuple;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.IntStream;

public class TANSEncoder implements Closeable {

  //Constructor Stuff
  private final OutputStream outputStream;
  private final FrequencyTable frequencies;


  //Entropy Coding parameters
  private final int precision;
  private final int m;

  //Variables for algorithm execution
  private final HashMap<StateEncodingTuple, Integer> stateTable;
  private int currentState;

  private final ByteBufferOutputStream encodingBuffer;
  private final BitOutputStream bitOutputStream;

  public TANSEncoder(OutputStream outputStream, Integer[] frequencies) {

    this.outputStream = outputStream;

    this.precision = (int) Math.floor(Math.log10(frequencies.length) / Math.log10(2)) + 3;
    this.m = 1 << precision;

    this.frequencies = new FrequencyTable(this.m, frequencies);

    this.currentState = (m << 1) - 1;

    this.encodingBuffer = new ByteBufferOutputStream(500 * 1024 * 1024);
    this.bitOutputStream = new BitOutputStream(this.encodingBuffer);


    this.stateTable = new HashMap<>();
    this.buildStateTable();
  }



  //TODO: Make a pluggable state table building.
  private void buildStateTable() {
    PriorityQueue<StateEncodingTuple> order = new PriorityQueue<>(new StateEncodingComparable());
    Iterator<SymbolScaledFrequencyTuple> symbolScaledFrequencyTupleIterator = frequencies.iterator();
    SymbolScaledFrequencyTuple symbolScaledFrequencyTuple;

    while (symbolScaledFrequencyTupleIterator.hasNext()) {
      symbolScaledFrequencyTuple = symbolScaledFrequencyTupleIterator.next();
      for (int position = 1; position <= symbolScaledFrequencyTuple.getScaledFrequency(); position++) {
        order.add(new StateEncodingTuple(
          (int) Math.round(this.m * position / (double) symbolScaledFrequencyTuple.getScaledFrequency()), symbolScaledFrequencyTuple.getSymbol()));
      }
    }

    StateEncodingTuple iterator;
    int nextState = 0;
    while (order.size() > 0) {
      iterator = order.poll();
      iterator.setState(this.frequencies.getScaledFrequencyForSymbol(iterator.getSymbol()));
      this.stateTable.put(iterator, nextState + this.m);

      //Prepare for next iteration
      this.frequencies.incrementScaledFrequencyForSymbol(iterator.getSymbol());
      nextState++;
    }

  }

  public void encode(int symbol) throws IOException {
    this.writeSymbol(symbol);
  }

  public void encode(ByteBuffer buffer) throws IOException {
    while (buffer.position() < buffer.limit()) {
      writeSymbol(buffer.get());
    }
  }

  private void writeSymbol(int symbol) throws IOException {
    Integer transition;
    while ((transition = this.stateTable.get(new StateEncodingTuple(this.currentState, symbol))) == null) {
      this.bitOutputStream.write(currentState & 0x01);
      this.currentState = currentState >> 1;
    }
    this.currentState = transition;
  }

  public void writeStateTable(OutputStream outputStream) throws IOException {
    IntStream compactTable = this.stateTable.entrySet().stream()
      .sorted(Comparator.comparingInt(Map.Entry::getValue))
      .mapToInt(entry -> (entry.getKey().getSymbol()));

    //TODO: Change that if I cannot throw an exception in a fucking Lambda
    compactTable.forEach(x -> {
      try {
        outputStream.write(x);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  protected void writeFinalState(int b) throws IOException {
    int iterator = b;
    while (iterator != 0) {
      int value = iterator & 0x01;
      bitOutputStream.write(value);
      iterator >>= 1;
    }
  }

//  public Integer flushToEnd() {
//    var byteCopy = bitOutputStream.getCurrentByte();
//    bitOutputStream.reset();
//    return byteCopy;
//  }

  @Override
  public void close() throws IOException {
    this.writeFinalState(currentState);
    bitOutputStream.flush();

    //Writing the encoding in reverse order.
    ReverseByteBufferInputStream reverseReadBuffer = new ReverseByteBufferInputStream(this.encodingBuffer.getBuffer());
    int cByte = -1;
    while(
      (cByte = reverseReadBuffer.read())!= -1 ){
      outputStream.write(cByte);
    }
    this.outputStream.flush();
    this.outputStream.close();
  }


  /**
   * Inner class for the ordering of the states in the encoding.
   */
  private class StateEncodingComparable implements Comparator<StateEncodingTuple> {

    @Override
    public int compare(StateEncodingTuple o1, StateEncodingTuple o2) {
      return Integer.compare(o1.getState(), o2.getState());
    }
  }

}


