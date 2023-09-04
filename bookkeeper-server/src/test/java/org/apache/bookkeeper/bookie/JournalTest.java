package org.apache.bookkeeper.bookie;

import org.apache.bookkeeper.conf.ServerConfiguration;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class JournalTest {


    @RunWith(value = Parameterized.class)
    public static class GetJournalIdsTest {


        private File journalDir;
        private Journal.JournalIdFilter journalIdFilter;
        private static Class<? extends Exception> expectedException;
        private List<Long> expectedResult;


        private enum JournalDirType {
            ONE_LOG_DIR,
            ONE_LOG_ONE_TEXT_DIR,
            ONE_TEXT_DIR,
            NOT_EXISTING_DIR,
            LOG_FILE,
            VOID_DIR;

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
                    default:
                        return null;
                }
            }
        }

        private enum JournalIdFilterType {
            JOURNAL_ROLLING_FILTER,
            ALWAYS_TRUE_FILTER,
            ALWAYS_FALSE_FILTER,
            NEW_FILTER;

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
                    default:
                        return null;
                }
            }
        }

        public GetJournalIdsTest(List<Long> expectedResult, Class<? extends Exception> expectedException, File journalDir, Journal.JournalIdFilter journalIdFilter) {
            this.expectedResult = expectedResult;
            this.expectedException = expectedException;
            this.journalDir = journalDir;
            this.journalIdFilter = journalIdFilter;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_DIR.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                    {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.ONE_LOG_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                    {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_DIR.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                    {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_DIR.getJournalDir(), null},

                    //{new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                    {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                    {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                    {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), null},

                    /*{new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},*/
                    {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), null},

                    /*{new ArrayList<Long>(), null, JournalDirType.NOT_EXISTING_DIR.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.NOT_EXISTING_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.NOT_EXISTING_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.NOT_EXISTING_DIR.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},*/
                    {new ArrayList<Long>(), null, JournalDirType.NOT_EXISTING_DIR.getJournalDir(), null},

                    {new ArrayList<Long>(), null, JournalDirType.LOG_FILE.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.LOG_FILE.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                    /*{new ArrayList<Long>(), null, JournalDirType.LOG_FILE.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.LOG_FILE.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                    {new ArrayList<Long>(), null, JournalDirType.LOG_FILE.getJournalDir(), null},*/

                    {null, Exception.class, null, JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                    {null, Exception.class, null, JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                    {null, Exception.class, null, JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                    {null, Exception.class, null, JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                    {null, Exception.class, null, null},

                    {new ArrayList<Long>(), null, JournalDirType.VOID_DIR.getJournalDir(), null},
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
        }

        @Test
        public void getJournalIdsTest() {

            try {

                System.out.println("Journal dir:\n" + journalDir.getPath());
                System.out.println("Journal dir list files:\n" + journalDir.listFiles());

                List<Long> journalIds = Journal.listJournalIds(journalDir, journalIdFilter);

                System.out.println("Result:\n" + journalIds);
                System.out.println("Expected result:\n" + expectedResult);

                assert (journalIds.equals(expectedResult));
            } catch (Exception e) {
                System.out.println("Exception:\n" + e);
                if (expectedException != null && expectedException.isAssignableFrom(e.getClass())) {
                    assert (true);
                } else {
                    assert (false);
                }
            }
        }


    }

    @RunWith(Parameterized.class)
    public static class JournalThreadTest {
        private Class<? extends Exception> expectedException;
        private List<Long> expectedResult;

        @Mock
        private LedgerDirsManager ledgerDirsManager;


        private ServerConfiguration conf;

        private File journalDir;

        @Rule //initMocks
        public MockitoRule rule = MockitoJUnit.rule();


        public JournalThreadTest(List<Long> expectedResult, Class<? extends Exception> expectedException) {
            this.expectedResult = expectedResult;
            this.expectedException = expectedException;
        }


        @Parameterized.Parameters
        public static Collection<Object[]> getTestParameters() {
            return Arrays.asList(new Object[][]{
                    {null, null}
            });
        }

        @Before
        public void setUp() {
            when(ledgerDirsManager.getAllLedgerDirs()).then(invocation -> {
                List<File> files = new ArrayList<File>();
                File file = new File("src/test/resources/journals/logs_dir");
                files.add(file);
                return files;
            });
            journalDir = new File("src/test/resources/journals/create_log");
            conf = new ServerConfiguration();

            //when(conf.getJournalRemovePagesFromCache()).thenReturn(false);
        }

        @Test
        public void journalThreadTest() throws Exception {
            ServerConfiguration confSpy = PowerMockito.spy(conf);
            doReturn(false).when(confSpy).getJournalRemovePagesFromCache();
            doReturn(1).when(confSpy).getJournalQueueSize();
            Journal journal = new Journal(1, journalDir, confSpy, ledgerDirsManager);
            journal.start();

            Thread.sleep(10000);
            journal.shutdown();
            assert (true);
        }


    }
}
