package pl.softmil.dumbster;

import static org.hamcrest.Matchers.is;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class SimpleSmtpServerHelperTest {
    private static final int SMTP_PORT = 9999;

    private static SimpleSmtpServerHelper simpleSmtpServerHelper;
    private static JavaMailSender javaMailSender;

    @ClassRule
    public static ExternalResource startDumbster = new ExternalResource() {
        private SimpleSmtpServer simpleSmtpServer;

        @Override
        protected void before() throws Throwable {
            simpleSmtpServer = SimpleSmtpServer.start(SMTP_PORT);
            simpleSmtpServerHelper = new SimpleSmtpServerHelper(
                    simpleSmtpServer);
            javaMailSender = buildMailSender();
        }

        private JavaMailSender buildMailSender() {
            JavaMailSenderImpl result = new JavaMailSenderImpl();
            result.setDefaultEncoding("UTF-8");
            result.setPort(SMTP_PORT);
            result.setHost("localhost");
            return result;
        }

        @Override
        protected void after() {
            simpleSmtpServer.stop();
        }

    };

    @Test
    public void testASingleMessageReceived() throws MailException,
            MessagingException {
        String from = "from@foo.pl";
        String to = "to@bar.pl";
        String subject = "jubject";
        String body = "Hi i am home";
        sendMailMessage(from, to, subject, body);

        SmtpMessage aSingleMessageReceived = simpleSmtpServerHelper
                .aSingleMessageReceived();

        SmtpMessageHelper smtpMessageHelper = new SmtpMessageHelper(
                aSingleMessageReceived);
        smtpMessageHelper.assertSubject(is(subject));
        smtpMessageHelper.assertFrom(is(from));
        smtpMessageHelper.assertTo(is(to));
        smtpMessageHelper.assertBody(is(body));
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
