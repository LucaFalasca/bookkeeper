package org.apache.bookkeeper.bookie;

import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.util.*;

import static org.apache.commons.lang.ArrayUtils.reverse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class JournalTest {

    @RunWith(value = Parameterized.class)
    public static class GetJournalIdsTest {


        private File journalDir;
        private final Journal.JournalIdFilter journalIdFilter;
        private static Class<? extends Exception> expectedException;
        private final List<Long> expectedResult;


        private enum JournalDirType {
            ONE_LOG_DIR,
            ONE_LOG_ONE_TEXT_DIR,
            ONE_TEXT_DIR,
            NOT_EXISTING_DIR,
            LOG_FILE,
            VOID_DIR,
            ADF_DIR;

            private final String PROJECT_ROOT_PATH = System.getProperty("user.dir");

            public File getJournalDir() {
                switch (this) {
                    case ONE_LOG_DIR:
                        return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/logs_dir");
                    case ONE_LOG_ONE_TEXT_DIR:
                        return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/logs_and_other_dir");
                    case ONE_TEXT_DIR:
                        return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/other_files_dir");
                    case NOT_EXISTING_DIR:
                        return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/not_existing_dir");
                    case LOG_FILE:
                        return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/logs_dir/0.log");
                    case VOID_DIR:
                        return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/void_dir");
                    case ADF_DIR:
                        return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/adf_dir");
                    default:
                        return null;
                }
            }
        }

        private enum JournalIdFilterType {
            JOURNAL_ROLLING_FILTER,
            ALWAYS_TRUE_FILTER,
            ALWAYS_FALSE_FILTER,
            NEW_FILTER,
            ADF_FILTER;

            public Journal.JournalIdFilter getJournalIdFilter() {
                switch (this) {
                    case JOURNAL_ROLLING_FILTER:
                        return new Journal.JournalIdFilter() {

                            @Override
                            public boolean accept(long journalId) {
                                //TODO
                                return journalId < 10;
                            }
                        };
                    case ALWAYS_TRUE_FILTER:
                        return new Journal.JournalIdFilter() {
                            @Override
                            public boolean accept(long journalId) {
                                return true;
                            }
                        };
                    case ALWAYS_FALSE_FILTER:
                        return new Journal.JournalIdFilter() {
                            @Override
                            public boolean accept(long journalId) {
                                return false;
                            }
                        };
                    case NEW_FILTER:
                        return new Journal.JournalIdFilter() {
                            @Override
                            public boolean accept(long journalId) {
                                return journalId > 0;
                            }
                        };
                    case ADF_FILTER:
                        return new Journal.JournalIdFilter() {
                            @Override
                            public boolean accept(long journalId) {
                                return journalId > 1;
                            }
                        };
                    default:
                        return null;
                }
            }
        }

        public GetJournalIdsTest(List<Long> expectedResult, Class<? extends Exception> expectedException, File journalDir, Journal.JournalIdFilter journalIdFilter) {
            this.expectedResult = expectedResult;
            GetJournalIdsTest.expectedException = expectedException;
            this.journalDir = journalDir;
            this.journalIdFilter = journalIdFilter;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new ArrayList<>(Collections.singletonList(1L)), null, JournalDirType.ONE_LOG_DIR.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                    {new ArrayList<>(Collections.singletonList(1L)), null, JournalDirType.ONE_LOG_DIR.getJournalDir(), null},
                    {new ArrayList<>(Collections.singletonList(1L)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.NOT_EXISTING_DIR.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.LOG_FILE.getJournalDir(), null},
                    {null, Exception.class, null, null},
                    {new ArrayList<Long>(), null, JournalDirType.VOID_DIR.getJournalDir(), null},
                    {new ArrayList<>(Collections.singletonList(2L)), null, JournalDirType.ADF_DIR.getJournalDir(), JournalIdFilterType.ADF_FILTER.getJournalIdFilter()},
                    {new ArrayList<>(Arrays.asList(1L, 2L)), null, JournalDirType.ADF_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()}
            });
        }
        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            if(journalDir != null && journalDir.getPath().substring(journalDir.getPath().length() - 8).equals("void_dir")) {
                boolean succes = journalDir.mkdir();
                if(!succes) {
                    System.out.println("Could not create void dir");
                }else {
                    System.out.println("Created void dir");
                }
            }

            if(journalDir != null && journalDir.getPath().equals(JournalDirType.ADF_DIR.getJournalDir().getPath())) {
                File[] output = journalDir.listFiles();
                reverse(output);
                System.out.println("reversed output:\n" + Arrays.toString(output));
                journalDir = Mockito.spy(journalDir);
                doReturn(output).when(journalDir).listFiles();
            }
        }
        @Test
        public void getJournalIdsTest() {
            try {
                System.out.println("Journal dir:\n" + journalDir.getPath());
                System.out.println("Journal dir list files:\n" + Arrays.toString(journalDir.listFiles()));

                List<Long> journalIds = Journal.listJournalIds(journalDir, journalIdFilter);

                System.out.println("Result:\n" + journalIds);
                System.out.println("Expected result:\n" + expectedResult);

                assertTrue (journalIds.equals(expectedResult));
            } catch (Exception e) {
                System.out.println("Exception:\n" + e);
                if (expectedException != null && expectedException.isAssignableFrom(e.getClass())) {
                    assertTrue(true);
                } else {
                    fail();
                }
            }
        }
    }
}
