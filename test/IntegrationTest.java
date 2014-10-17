import org.junit.Test;
import play.libs.F;
import play.test.TestBrowser;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {

        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                assertThat(browser.pageSource()).contains("zbw hosting subap");
                browser.goTo("http://localhost:3333/record/sipstatus/oai:nationallizenzen.zbw.eu:10836%2F16");
                assertThat(browser.pageSource()).contains("zbw hosting subap");
            }
        });

    }

}
