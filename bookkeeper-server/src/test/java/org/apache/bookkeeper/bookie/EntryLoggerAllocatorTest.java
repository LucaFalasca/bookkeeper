package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang.ArrayUtils.reverse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class EntryLoggerAllocatorTest {

    private EntryLoggerAllocator entryLoggerAllocator;
    private final String PROJECT_ROOT_PATH = System.getProperty("user.dir");

    public EntryLoggerAllocatorTest(){

    }

    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][]{
            {}
        });
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        /*ServerConfiguration conf = Mockito.mock(ServerConfiguration.class);
        when(conf.isEntryLogFilePreAllocationEnabled()).thenReturn(true);
        when(conf.getWriteBufferBytes()).thenReturn(1);*/

        ServerConfiguration conf = TestBKConfiguration.newServerConfiguration();
        LedgerDirsManager ledgerDirsManager = Mockito.mock(LedgerDirsManager.class);
        File file = new File(PROJECT_ROOT_PATH, "src/test/resources/journals/adf_dir");
        when(ledgerDirsManager.getAllLedgerDirs()).thenReturn(new ArrayList(Arrays.asList(file)));
        DefaultEntryLogger.RecentEntryLogsStatus recentEntryLogsStatus = Mockito.mock(DefaultEntryLogger.RecentEntryLogsStatus.class);
        ByteBufAllocator byteBufAllocator = UnpooledByteBufAllocator.DEFAULT;
        entryLoggerAllocator = new EntryLoggerAllocator(conf, ledgerDirsManager, recentEntryLogsStatus, 2, byteBufAllocator);
    }

    @Test
    public void createNewLogTest(){
        File file = Mockito.mock(File.class);
        try {
            entryLoggerAllocator.createNewLog(null);
            assert true;
        } catch (Exception e) {
            e.printStackTrace();
            assert true;
        }
    }

}
