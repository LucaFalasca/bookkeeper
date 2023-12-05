package org.apache.bookkeeper.bookie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class FileInfoTest {

    private final String PROJECT_ROOT_PATH = System.getProperty("user.dir");
    private FileInfo fileInfo;

    private int magicBytes;
    private byte[] masterKey;
    private int lenMasterKey;
    private int state;
    private Class<? extends Exception> expectedException;
    private int version;
    private int excplicitBufLength;


    public FileInfoTest(Class<? extends Exception> expectedException, int magicBytes, byte[] masterKey, int lenMasterKey, int state, int version, int excplicitBufLength){
        this.expectedException = expectedException;
        this.magicBytes = magicBytes;
        this.masterKey = masterKey;
        this.lenMasterKey = lenMasterKey;
        this.state = state;
        this.version = version;
        this.excplicitBufLength = excplicitBufLength;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {null, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[1], 1, 0, 1, 0},
                {null, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[1], 2, 0, 1, 0}, // True expectedException: Exception.class
                {null, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[1], 0, 0, 1, 0}, // True expectedException: Exception.class
                {Exception.class, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[0], -1, 0, 0, 0}, // True expectedException: null
                {null, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[0], 0, 0, 0, 0},
                {null, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[0], 1, 1, 0, 0}, // True expectedException: Exception.class
                {Exception.class, ByteBuffer.wrap("BKLU".getBytes(UTF_8)).getInt(), new byte[1], 1, 0, 1, 0},

                //Jacoco Increment
                {null, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[1], 1, 0, 1, 16},
                {Exception.class, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[1], 1, 0, 1, 15},
                {Exception.class, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[1], 1, 0, 1, -1},

                //Badua Increment
                {Exception.class, ByteBuffer.wrap("BKLE".getBytes(UTF_8)).getInt(), new byte[1], 1, 0, 2, 0},

                //Pit Increment*/
        });
    }

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        File file = new File(PROJECT_ROOT_PATH, "src/test/resources/ledgers/tt.txn");
        byte[] bytes = new byte[1];
        bytes[0] = 1;
        fileInfo = new FileInfo(file, bytes, 0);

        ByteBuffer bb = ByteBuffer.allocate((int) 1024);
        bb.putInt(magicBytes);
        bb.putInt(version);
        bb.putInt(lenMasterKey);
        if(masterKey != null) {
            bb.put(masterKey);
            System.out.println(masterKey.length);
        }
        bb.putInt(state);
        bb.putInt(excplicitBufLength);


        bb.rewind();
        try(FileChannel fc = new RandomAccessFile(file, "rw").getChannel()){
            fc.position(0);
            fc.write(bb);
        }

    }

    @Test
    public void readHeaderTest(){
        try {
            fileInfo.readHeader();
            if(expectedException == null)
                assertTrue(true);
            else
                fail();
        } catch (Exception e) {
            e.printStackTrace();
            if (expectedException != null && expectedException.isAssignableFrom(e.getClass())) {
                assertTrue(true);
            } else {
                fail();
            }
        }
    }

}
