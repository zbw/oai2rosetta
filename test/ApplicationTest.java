import actors.CreateIEActor;
import actors.FetchActor;
import actors.PushActor;
import actors.SipStatusActor;
import models.Record;
import models.Repository;
import org.junit.Test;
import play.mvc.Result;
import play.test.FakeRequest;

import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {



    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void testDB() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Repository repos = Repository.findById(1);
                assertNull(repos);

                //delete testrecord
                //testrecord.delete();
            }
        });
    }
    //@Test
    public void fetchRecord() {

        //Record record = new Record();
        Record record = mock(Record.class);
        record.identifier = "oai:nationallizenzen.zbw.eu:10836/19";
        record.save();
        assertEquals(true,FetchActor.fetchRecord(record));


    }

    @Test
    public void testCallIndex() {

    }

}
