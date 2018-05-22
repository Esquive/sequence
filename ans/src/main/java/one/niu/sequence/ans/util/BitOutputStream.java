package one.niu.sequence.ans.util;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream extends OutputStream {

  private final OutputStream outputStream;
  private int currentByte = 0;
  private int numBitsFilled = 0;
  private int bitCount = 0;

  public BitOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public void write(int b) throws IOException {
    if(b != 0 && b != 1)
      throw new IllegalArgumentException("The bit the write must be 0 or 1, it was: " + b);

    currentByte = (currentByte << 1) | b;
    numBitsFilled++;
    bitCount++;
    if(numBitsFilled == 8){
      outputStream.write(currentByte & 0xff);
      currentByte = 0;
      numBitsFilled = 0;
    }
  }

  @Override
  public void flush() throws IOException {
    while(numBitsFilled!=0){
      write(0);
    }
  }

  @Override
  public void close() throws IOException {
    this.outputStream.close();
  }

}
