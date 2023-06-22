package org.apache.bookkeeper.benchmark;

import junit.framework.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class BenchBookieTest {

    BenchBookie benchBookie = mock(BenchBookie.class);

    @Test
    public void testState() {
        Assert.assertEquals(true, true);
    }

}
