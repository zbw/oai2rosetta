import models.Record;
import org.junit.Test;
import utils.RecordUtils;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {


    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }

    @Test
    public void fetchRecord() {
        running(fakeApplication(inMemoryDatabase("test")), new Runnable() {
                    public void run() {
                        Record record = new Record();
                        record.identifier = "oai:nationallizenzen.zbw.eu:10836/19";
                        record.save();
                        boolean ok = RecordUtils.fetchRecord(record);
                        assertThat(ok);
                    }
                }
        );
    }

}
