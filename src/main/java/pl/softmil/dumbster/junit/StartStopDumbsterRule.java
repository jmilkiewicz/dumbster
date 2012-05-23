package pl.softmil.dumbster.junit;

import org.junit.rules.ExternalResource;

import com.dumbster.smtp.SimpleSmtpServer;

public class StartStopDumbsterRule extends ExternalResource {
    private final int port;
    private SimpleSmtpServer simpleSmtpServer;

    public StartStopDumbsterRule(int port) {
        super();
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
        simpleSmtpServer = SimpleSmtpServer.start(port);
    }

    @Override
    protected void after() {
        simpleSmtpServer.stop();
    }
    
    public SimpleSmtpServer getSimpleSmtpServer() {
        return simpleSmtpServer;
    }
}
