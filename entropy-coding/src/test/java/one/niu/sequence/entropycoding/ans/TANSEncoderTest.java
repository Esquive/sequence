package one.niu.sequence.entropycoding.ans;

import junit.framework.TestSuite;
import one.niu.libs.files.fileio.ByteBufferFileInputStream;
import one.niu.libs.files.fileio.ByteBufferFileOutputStream;
import one.niu.libs.files.fileio.ReverseByteBufferFileInputStream;
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
        int debugCounter = 0; //768771
        while ( (
          currByte = encodingInputStream.read() ) != -1) {
          debugCounter++;
          encoder.encode(currByte);
        }
      }


    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void Test03_Decoding(){
    try (
      TANSDecoder decoder = new TANSDecoder(new File("book1cpp.tans"));
      ByteBufferFileOutputStream output = new ByteBufferFileOutputStream(new RandomAccessFile("book1cpp","rw").getChannel())
    ){
      decoder.readStateTable(2*1024);
      decoder.decode(output);
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  @Test
  @SuppressWarnings("Duplicates")
  public void Test04_ProblemEncoding(){
    File inputFile = new File(getClass().getClassLoader().getResource("book1_problem").getFile());

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
          new RandomAccessFile("book1_problem.tans", "rw").getChannel());
        InputStream encodingInputStream  =  new ReverseByteBufferFileInputStream(inputRAF);
        TANSEncoder encoder = new TANSEncoder(outputStream,frequencies)) {

        encoder.writeStateTable(outputStream);
        int currByte = -1;
        int debugCounter = 0; //768771
        while ( (
          currByte = encodingInputStream.read() ) != -1) {
          debugCounter++;
//          System.out.print(new String(new byte[]{(byte)curByte}));
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