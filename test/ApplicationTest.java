import actors.CreateIEActor;
import actors.FetchActor;
import actors.PushActor;
import actors.SipStatusActor;
import models.Record;
import models.Repository;
import org.junit.Test;
import play.mvc.Result;
import play.test.FakeRequest;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static play.mvc.Http.Status.OK;
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
    public void testDB() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Repository repos = Repository.findById(1);
                assertThat(repos.title).contains("EIU");
                assertThat(repos.records.size()).isGreaterThan(0);
                //create testrecord
                Record record = repos.records.get(0);
                System.out.println("testing: " + record);
                Record tmprecord = (Record) record._ebean_createCopy();
                record.loguser = "TEST";
                record.id = record.id+"_test";
                record.status = 0;
                record.save();
                boolean ok = FetchActor.fetchRecord(record);
                assertThat(ok);
                assertThat(CreateIEActor.createIE(record));
                assertThat(PushActor.move(record));
                assertThat(SipStatusActor.getSipStatus(record));
                System.out.println(record);
                record = (Record) tmprecord._ebean_createCopy();

                record.update();
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
        boolean ok = FetchActor.fetchRecord(record);
        assertThat(ok);

    }

    @Test
    public void testCallIndex() {
        Result result = callAction(
                controllers.routes.ref.Application.index(),
                new FakeRequest(GET, "/")
        );
        assertThat(status(result)).isEqualTo(OK);
    }

}
