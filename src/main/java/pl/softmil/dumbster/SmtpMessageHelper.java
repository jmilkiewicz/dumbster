package pl.softmil.dumbster;

import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.MimeUtility;

import org.hamcrest.Matcher;

import com.dumbster.smtp.SmtpMessage;

public class SmtpMessageHelper {
    private final SmtpMessage smtpMessage;

    public SmtpMessageHelper(SmtpMessage smtpMessage) {
        super();
        this.smtpMessage = smtpMessage;
    }

    public void assertBody(Matcher<? super String> matcher) {
        assertThat("unable to match body", smtpMessage.getBody(), matcher);
    }

    public void assertTo(Matcher<? super String> matcher) {
        assertThat("unable to match \"To\"", getHeaderValue("To"), matcher);
    }

    public void assertFrom(Matcher<? super String> matcher) {
        assertThat("unable to match \"From\"", getHeaderValue("From"), matcher);
    }
    
    public void assertSubject(Matcher<? super String> matcher) throws UnsupportedEncodingException {
        String decodeText = MimeUtility.decodeText(getHeaderValue("Subject"));
        
        assertThat("unable to match \"Subject\"",decodeText, matcher);
    }

    private String getHeaderValue(String headerName) {
        return smtpMessage.getHeaderValue(headerName);
    }

}
