package pl.softmil.dumbster;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class SimpleSmtpServerHelper {
    private final SimpleSmtpServer simpleSmtpServer;
    
    
    public SimpleSmtpServerHelper(SimpleSmtpServer simpleSmtpServer) {
        this.simpleSmtpServer = simpleSmtpServer;
    }

    public SmtpMessage aSingleMessageReceived() {
        Iterator<?> receivedEmails = simpleSmtpServer.getReceivedEmail();
        assertMessageExists(receivedEmails);        
        SmtpMessage email = (SmtpMessage)receivedEmails.next();
        assertNoMoreMessages(receivedEmails);
        receivedEmails.remove();
        return email;
    }

    private void assertNoMoreMessages(Iterator<?> receivedEmails) {
        assertThat("smtp messages queue must be empty ",receivedEmails.hasNext(), equalTo(false));
    }

    private void assertMessageExists(Iterator<?> receivedEmails) {
        assertThat("no messages sent to me :(",receivedEmails.hasNext(), equalTo(true));
    }
}
