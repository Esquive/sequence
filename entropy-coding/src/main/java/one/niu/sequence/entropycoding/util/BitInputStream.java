package one.niu.sequence.entropycoding.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream extends InputStream {

  protected final InputStream inputStream;
  protected int numBitsRemaining = 0;
  protected int currentByte = 0;
  protected int bitsRead = 0;

  public BitInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public int read() throws IOException {
    if (numBitsRemaining == 0) {
      currentByte = inputStream.read();
      numBitsRemaining = 8;
    }

    if (currentByte != -1) {
      bitsRead++;
      numBitsRemaining--;
      return (currentByte >>> numBitsRemaining) & 1;
    } else {
      return -1;
    }
  }

  @Override
  public void close() {
    IOUtils.closeQuietly(inputStream);
  }
}
