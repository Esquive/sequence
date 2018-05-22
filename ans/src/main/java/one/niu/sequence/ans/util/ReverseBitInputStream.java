package one.niu.sequence.ans.util;

import java.io.IOException;
import java.io.InputStream;

public class ReverseBitInputStream extends BitInputStream {

  private int numBitsRead = 8;

  public ReverseBitInputStream(InputStream inputStream) {
    super(inputStream);
  }

  @Override
  public int read() throws IOException {
    if (numBitsRead == 8) {
      currentByte = inputStream.read();
      numBitsRead = 0;
    }
    if (currentByte != -1) {
//      println(Integer.toBinaryString(currentByte))
      bitsRead++;
      int result = currentByte & 1;
      numBitsRead++;
      currentByte = currentByte >> 1;
      return result;
    } else {
      return -1;
    }
  }
}
