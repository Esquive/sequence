package one.niu.sequence.entropycoding.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ReverseByteBufferInputStream extends InputStream {

  private final ByteBuffer buffer;
  private int remainingInBuffer;

  public ReverseByteBufferInputStream(ByteBuffer buffer) {
    this.buffer = buffer;
    this.buffer.flip();
    this.buffer.position(buffer.limit() - 1);
    this.remainingInBuffer = this.buffer.limit();
  }

  @Override
  public int read() throws IOException {
    if (remainingInBuffer == 0) {
      return -1;
    }
    int result = buffer.get() & 0xff;
    remainingInBuffer -= 1;
    if (remainingInBuffer > 0) {
      buffer.position(buffer.position() - 2);
    }
    return result;
  }
}
