package pl.softmil.dumbster;

import static org.hamcrest.Matchers.is;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.*;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.*;

import pl.softmil.dumbster.junit.StartStopDumbsterRule;

import com.dumbster.smtp.SmtpMessage;

public class SimpleSmtpServerHelperTest {
    private static final int SMTP_PORT = 9999;
    private static JavaMailSender javaMailSender;
    private String from = "from@foo.pl";
    private String to = "to@bar.pl";
    private String subject = "jubject";
    private String body = "Hi i am home";
    
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
            MessagingException {      
        sendMailMessage(from, to, subject, body);

        SmtpMessage aSingleMessageReceived = new SimpleSmtpServerHelper(
                startStopDumbster.getSimpleSmtpServer())
                .aSingleMessageReceived();

        SmtpMessageHelper smtpMessageHelper = new SmtpMessageHelper(
                aSingleMessageReceived);
        smtpMessageHelper.assertSubject(is(subject));
        smtpMessageHelper.assertFrom(is(from));
        smtpMessageHelper.assertTo(is(to));
        smtpMessageHelper.assertBody(is(body));
    }
    
    
    @Test
    public void testASingleMessageReceivedCallRemovesReceivedMessage() throws MailException,
            MessagingException {
        sendMailMessage(from, to, subject, body);

        SimpleSmtpServerHelper simpleSmtpServerHelper = new SimpleSmtpServerHelper(
                startStopDumbster.getSimpleSmtpServer());
        
        simpleSmtpServerHelper.aSingleMessageReceived();
        
        sendMailMessage(from, to, subject, body);
        
        simpleSmtpServerHelper.aSingleMessageReceived();
    }

    private void sendMailMessage(String from, String to, String subject,
            String body) throws MessagingException {
        javaMailSender.send(buildMessage(from, to, subject, body));
    }

    private MimeMessage buildMessage(String from, String to, String subject,
            String body) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, false);
        return message;
    }

}
