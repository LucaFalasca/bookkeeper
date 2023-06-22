package org.apache.bookkeeper.tools.cli.commands.bookie;

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LastMarkCommandTest {

    LastMarkCommand lastMarkCommand;
    ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);

    @Before
    public void beforeTest(){
        lastMarkCommand = new LastMarkCommand();
        when(serverConfiguration.getLedgerDirs()).thenReturn(new File[] { new File("dir1"), new File("dir2") });
        when(serverConfiguration.getDiskUsageThreshold()).thenReturn(0.9f);
        when(serverConfiguration.getDiskUsageWarnThreshold()).thenReturn(0.9f);
        when(serverConfiguration.getJournalDirs()).thenReturn(new File[] { new File("dir1"), new File("dir2") });
        when(serverConfiguration.getJournalDirNames()).thenReturn(new String[]{"dir1", "dir2"});
    }

    @Test
    public void applyTest(){
        try {
            lastMarkCommand.apply(serverConfiguration, null);
            fail( "My method didn't throw when I expected it to" );
        } catch (Exception expectedException) {
            Assert.assertEquals(1, 1);
        }

    }
}
