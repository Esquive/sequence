package one.niu.sequence.ans.encoding;

import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;


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

}