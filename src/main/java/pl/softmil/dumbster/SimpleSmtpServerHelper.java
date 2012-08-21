package pl.softmil.dumbster;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import pl.softmil.test.utils.waituntil.*;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class SimpleSmtpServerHelper {
    private final SimpleSmtpServer simpleSmtpServer;
    private final WaitUntilTimes waitUntilTimes;
    
    
    public SimpleSmtpServerHelper(SimpleSmtpServer simpleSmtpServer, WaitUntilTimes waitUntilTimes) {
        this.waitUntilTimes = waitUntilTimes;
        this.simpleSmtpServer = simpleSmtpServer;
    }

    public SmtpMessage aSingleMessageReceived() {
        waitUntilEmailQueueNotEmpty();
        Iterator<?> receivedEmails = simpleSmtpServer.getReceivedEmail();
        SmtpMessage email = (SmtpMessage)receivedEmails.next();
        assertNoMoreMessages(receivedEmails);
        receivedEmails.remove();
        return email;
    }

    private void waitUntilEmailQueueNotEmpty() {
        WaitUntil<Object> waitUntil = new WaitUntil<Object>(waitUntilTimes, new Until<Object>() {

            @Override
            public boolean isTrue(Object t) {
                Iterator<?> receivedEmails = simpleSmtpServer.getReceivedEmail();
                return receivedEmails.hasNext();
            }

            @Override
            public Object getContext() {
                return null;
            }
           
            @Override
            public String toString() {
                return "email message queue NOT empty";
            } 
            
        });
        waitUntil.waitFor();
    }

    private void assertNoMoreMessages(Iterator<?> receivedEmails) {
        assertThat("smtp messages queue must be empty ",receivedEmails.hasNext(), equalTo(false));
    }

    /*
    private void assertMessageExists(Iterator<?> receivedEmails) {
        assertThat("no messages sent to me :(",receivedEmails.hasNext(), equalTo(true));
    }*/

    public void drainEmailQueue() {
        Iterator<?> receivedEmails = simpleSmtpServer.getReceivedEmail();
        while(receivedEmails.hasNext()){
            receivedEmails.next();
            receivedEmails.remove();
        }
        
    }
}
