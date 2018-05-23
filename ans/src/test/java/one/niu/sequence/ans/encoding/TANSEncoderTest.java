package one.niu.sequence.ans.encoding;

import junit.framework.TestSuite;
import one.niu.libs.files.fileio.ByteBufferFileInputStream;
import one.niu.libs.files.fileio.ByteBufferFileOutputStream;
import one.niu.libs.files.fileio.ReverseByteBufferFileInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TANSEncoderTest extends TestSuite{

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void Test01_Construction(){
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Integer[] frequencies = new Integer[3];
    frequencies[0] = 15;
    frequencies[1] = 13;
    frequencies[2] = 5;
    TANSEncoder encoder = new TANSEncoder(out,frequencies);
  }

  @Test
  public void Test02_Encoding(){

    File inputFile = new File(getClass().getClassLoader().getResource("book1").getFile());

    try(RandomAccessFile inputRAF = new RandomAccessFile(inputFile, "r");
        InputStream inputStream = new ByteBufferFileInputStream(inputRAF.getChannel());
        ) {



      Integer[] frequencies = new Integer[256];
      Arrays.fill(frequencies, 0);
      int curByte;
      while( (curByte = inputStream.read()) != -1 ){
        frequencies[curByte]++;
      }

      try (
        ByteBufferFileOutputStream outputStream = new ByteBufferFileOutputStream(
          new RandomAccessFile("book1.tans", "rw").getChannel());
        InputStream encodingInputStream  =  new ReverseByteBufferFileInputStream(inputRAF);
        TANSEncoder encoder = new TANSEncoder(outputStream,frequencies);) {

        encoder.writeStateTable(outputStream);
        int currByte = -1;
        while ( (
          currByte = encodingInputStream.read() ) != -1) {
          encoder.encode(currByte);
        }
      }


    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}