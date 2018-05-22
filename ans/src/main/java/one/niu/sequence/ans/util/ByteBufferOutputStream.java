package one.niu.sequence.ans.util;


import java.io.Closeable;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream implements Closeable{

  private ByteBuffer buffer;

  public  ByteBufferOutputStream(){
    this(8 * 1024);
  }

  public ByteBufferOutputStream(int bufferSize){
    this.buffer = ByteBuffer.allocateDirect(bufferSize);
  }

  @Override
  public void write(int b) {
    buffer.put( (byte)(b&0xff)  );
  }

  @Override
  public void close() {
     this.buffer = null;
  }
}
