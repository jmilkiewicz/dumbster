package pl.softmil.dumbster;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;

import com.dumbster.smtp.SmtpMessage;

public class SmtpMessageHelper {
    private final SmtpMessage smtpMessage;

    public SmtpMessageHelper(SmtpMessage smtpMessage) {
        super();
        this.smtpMessage = smtpMessage;
    }

    public void assertBody(Matcher<String> matcher) {
        assertThat("unable to match body", smtpMessage.getBody(), matcher);
    }

    public void assertTo(Matcher<String> matcher) {
        assertThat("unable to match \"From\"", getHeaderValue("To"), matcher);
    }

    public void assertFrom(Matcher<String> matcher) {
        assertThat("unable to match \"To\"", getHeaderValue("From"), matcher);
    }
    
    public void assertSubject(Matcher<String> matcher) {
        assertThat("unable to match \"To\"", getHeaderValue("Subject"), matcher);
    }

    private String getHeaderValue(String headerName) {
        return smtpMessage.getHeaderValue(headerName);
    }

}
