package org.apache.bookkeeper.bookie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.apache.bookkeeper.bookie.BookKeeperServerStats.JOURNAL_SCOPE;

@RunWith(value = Parameterized.class)
public class JournalTest {


    private File journalDir;
    private Journal.JournalIdFilter journalIdFilter;
    private static Class<? extends Exception> expectedException;
    private List<Long> expectedResult;



    private enum JournalDirType{
        ONE_LOG_DIR,
        ONE_LOG_ONE_TEXT_DIR,
        ONE_TEXT_DIR,
        VOID_DIR,
        LOG_FILE;

        private final String PROJECT_ROOT_PATH = System.getProperty("user.dir");
        public File getJournalDir(){
            switch(this){
                case ONE_LOG_DIR:
                    return new File(PROJECT_ROOT_PATH,"src/test/resources/journals/logs_dir");
                case ONE_LOG_ONE_TEXT_DIR:
                    return new File(PROJECT_ROOT_PATH,"src/test/resources/journals/logs_and_other_dir");
                case ONE_TEXT_DIR:
                    return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/other_files_dir");
                case VOID_DIR:
                    return new File(PROJECT_ROOT_PATH, "src/test/resources/journals/void_dir");
                case LOG_FILE:
                    return new File(PROJECT_ROOT_PATH,"src/test/resources/journals/logs_dir/0.log");
                default:
                    return null;
            }
        }
    }

    private enum JournalIdFilterType{
        JOURNAL_ROLLING_FILTER,
        ALWAYS_TRUE_FILTER,
        ALWAYS_FALSE_FILTER,
        NEW_FILTER;

        public Journal.JournalIdFilter getJournalIdFilter(){
            switch(this){
                case JOURNAL_ROLLING_FILTER:
                    return new Journal.JournalIdFilter(){

                        @Override
                        public boolean accept(long journalId) {
                            //TODO
                            return journalId < 10;
                        }
                    };
                case ALWAYS_TRUE_FILTER:
                    return new Journal.JournalIdFilter(){
                        @Override
                        public boolean accept(long journalId) {
                            return true;
                        }
                    };
                case ALWAYS_FALSE_FILTER:
                    return new Journal.JournalIdFilter(){
                        @Override
                        public boolean accept(long journalId) {
                            return false;
                        }
                    };
                case NEW_FILTER:
                    return new Journal.JournalIdFilter(){
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

    public JournalTest(List<Long> expectedResult, Class<? extends Exception> expectedException, File journalDir, Journal.JournalIdFilter journalIdFilter){
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
                {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                {new ArrayList<>(Arrays.asList(1l)), null, JournalDirType.ONE_LOG_ONE_TEXT_DIR.getJournalDir(), null},
                {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.ONE_TEXT_DIR.getJournalDir(), null},
                {new ArrayList<Long>(), null, JournalDirType.VOID_DIR.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.VOID_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.VOID_DIR.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.VOID_DIR.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), null, JournalDirType.VOID_DIR.getJournalDir(), null},
                {new ArrayList<Long>(), Exception.class, JournalDirType.LOG_FILE.getJournalDir(), JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), Exception.class, JournalDirType.LOG_FILE.getJournalDir(), JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), Exception.class, JournalDirType.LOG_FILE.getJournalDir(), JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), Exception.class, JournalDirType.LOG_FILE.getJournalDir(), JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                {new ArrayList<Long>(), Exception.class, JournalDirType.LOG_FILE.getJournalDir(), null},
                {null, Exception.class, null, JournalIdFilterType.JOURNAL_ROLLING_FILTER.getJournalIdFilter()},
                {null, Exception.class, null, JournalIdFilterType.ALWAYS_TRUE_FILTER.getJournalIdFilter()},
                {null, Exception.class, null, JournalIdFilterType.ALWAYS_FALSE_FILTER.getJournalIdFilter()},
                {null, Exception.class, null, JournalIdFilterType.NEW_FILTER.getJournalIdFilter()},
                {null, Exception.class, null, null}
        });
    }

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getJournalIdsTest(){

        try {

            System.out.println("Journal dir:\n" + journalDir);
            System.out.println("Journal dir list files:\n" + journalDir.listFiles());

            List<Long> journalIds = Journal.listJournalIds(journalDir, journalIdFilter);

            System.out.println("Result:\n" + journalIds);
            System.out.println("Expected result:\n" + expectedResult);

            assert (journalIds.equals(expectedResult));
        }
        catch(Exception e){
            System.out.println("Exception:\n" + e);
            if(expectedException != null && expectedException.isAssignableFrom(e.getClass())){
                assert(true);
            }
            else{
                assert(false);
            }
        }
    }



}
