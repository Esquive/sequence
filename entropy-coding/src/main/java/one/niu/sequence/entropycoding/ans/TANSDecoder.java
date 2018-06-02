package one.niu.sequence.entropycoding.ans;

import one.niu.libs.files.fileio.ByteBufferFileInputStream;
import one.niu.sequence.entropycoding.util.ReverseBitInputStream;
import one.niu.sequence.entropycoding.util.StateDecodingTuple;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TANSDecoder implements Closeable {

  private RandomAccessFile raf;
  private int currentState;
  private HashMap<Integer, StateDecodingTuple> stateTable;

  public TANSDecoder(File file) throws FileNotFoundException {
    raf = new RandomAccessFile(file, "r");
    this.currentState = 0;
    this.stateTable = new HashMap<>();
  }

  public void readStateTable(int stateTableSize) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(stateTableSize);
    //I had to remove the try-with-resource statement here.
    FileChannel fileChannel = raf.getChannel();

    //tODO: This was wrong it should be alphabet size
//    List<Integer>[] states = new List[stateTableSize];
    List<Integer>[] states = new List[256];
    for (int i = 0; i < states.length; i++) {
      states[i] = new LinkedList<>();
    }

    fileChannel.read(buffer);
    buffer.flip();
    int index = 0;
    while (buffer.position() < buffer.limit()) {
      int symbol = buffer.get() & 0xff;
      states[symbol].add(index + stateTableSize);
      index++;
    }

    for (int i = 0; i < states.length; i++) {
      index = 0;
      List<Integer> state = states[i];
      Iterator<Integer> iter = state.iterator();
      while (iter.hasNext()) {
        stateTable.put(iter.next(), new StateDecodingTuple(i, index + state.size()));
        index++;
      }
    }

  }

  public void decode(OutputStream output) throws IOException {

    try (InputStream inputStream = new ReverseBitInputStream(
      new ByteBufferFileInputStream(raf.getChannel()))) {
      int nextBit = -1;
      StateDecodingTuple symbolAndState;
      while (((nextBit = inputStream.read()) != -1)) {
        if (currentState >= 2048) {
          if (currentState == (2048 * 2) - 1) {
            System.out.println("Maxstate Reached");
          }


          symbolAndState = this.stateTable.get(currentState);
          if (symbolAndState == null) {
            throw new IllegalStateException("Current state was not found in the state table: " + currentState);
          }
          output.write(symbolAndState.getSymbol() & 0xff);
          currentState = symbolAndState.getPreviousState();

        }
        currentState = (currentState << 1);
        currentState = currentState | nextBit;
      }
    }

  }

  @Override
  public void close() throws IOException {
    this.raf.close();
  }

}
