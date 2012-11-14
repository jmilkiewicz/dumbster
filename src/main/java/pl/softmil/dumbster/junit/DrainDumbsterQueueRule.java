package pl.softmil.dumbster.junit;

import java.util.Iterator;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.dumbster.smtp.SimpleSmtpServer;

public class DrainDumbsterQueueRule extends TestWatcher{
	private SimpleSmtpServer simpleSmtpServer;
	
	public SimpleSmtpServer getSimpleSmtpServer() {
		return simpleSmtpServer;
	}

	public void setSimpleSmtpServer(SimpleSmtpServer simpleSmtpServer) {
		this.simpleSmtpServer = simpleSmtpServer;
	}
	
	@Override
	protected void finished(Description description) {
		Iterator<?> receivedEmails = simpleSmtpServer.getReceivedEmail();
        while(receivedEmails.hasNext()){
            receivedEmails.next();
            receivedEmails.remove();
        }
	}
	
	
}
