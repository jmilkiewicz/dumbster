package pl.softmil.dumbster;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.*;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.*;

import pl.softmil.dumbster.junit.StartStopDumbsterRule;
import pl.softmil.test.utils.waituntil.WaitUntilTimes;

import com.dumbster.smtp.SmtpMessage;

public class SimpleSmtpServerHelperTest {
    private static final int SMTP_PORT = 9999;
    private static JavaMailSender javaMailSender;
    private String from = "from@foo.pl";
    private String to = "to@bar.pl";
    private String subject = "zażółć żółtą gęśl";
    private String body = "<html>Hi i am home</html>";

    @BeforeClass
    public static void buildMailSender() {
        JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();
        senderImpl.setDefaultEncoding("UTF-8");
        senderImpl.setPort(SMTP_PORT);
        senderImpl.setHost("localhost");
        javaMailSender = senderImpl;
    }

    @ClassRule
    public static StartStopDumbsterRule startStopDumbster = new StartStopDumbsterRule(
            SMTP_PORT);

    @Test
    public void testASingleMessageReceived() throws MailException,
            MessagingException, UnsupportedEncodingException {
        sendMailMessage(from, to, subject, body);

        SmtpMessage aSingleMessageReceived = smtpServerHelper()
                .aSingleMessageReceived();

        SmtpMessageHelper smtpMessageHelper = new SmtpMessageHelper(
                aSingleMessageReceived);
        smtpMessageHelper.assertSubject(is(subject));
        smtpMessageHelper.assertFrom(is(from));
        smtpMessageHelper.assertTo(is(to));
    }

    private SimpleSmtpServerHelper smtpServerHelper() {
        return new SimpleSmtpServerHelper(
                startStopDumbster.getSimpleSmtpServer(),
                WaitUntilTimes.withMaxAndSleepInteval(1000, 100));
    }

    @Test
    public void testASingleMessageReceivedCallRemovesReceivedMessage()
            throws MailException, MessagingException {
        sendMailMessage(from, to, subject, body);

        SimpleSmtpServerHelper simpleSmtpServerHelper = smtpServerHelper();

        simpleSmtpServerHelper.aSingleMessageReceived();

        sendMailMessage(from, to, subject, body);

        simpleSmtpServerHelper.aSingleMessageReceived();
    }

    @Test
    public void testDrainEmailQueue() throws MailException, MessagingException {
        sendMailMessage(from, to, subject, body);
        sendMailMessage(from, to, subject, body);
        sendMailMessage(from, to, subject, body);

        SimpleSmtpServerHelper simpleSmtpServerHelper = smtpServerHelper();
        simpleSmtpServerHelper.drainEmailQueue();

        assertThat(startStopDumbster.getSimpleSmtpServer()
                .getReceivedEmailSize(), equalTo(0));
    }

    private void sendMailMessage(String from, String to, String subject,
            String body) throws MessagingException {
        javaMailSender.send(buildMessage(from, to, subject, body));
    }

    private MimeMessage buildMessage(String from, String to, String subject,
            String body) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        return message;
    }

}
