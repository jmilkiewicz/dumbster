package pl.softmil.dumbster;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.hamcrest.Matcher;
import org.jsoup.nodes.Element;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.*;

import pl.softmil.dumbster.junit.StartStopDumbsterRule;
import pl.softmil.test.utils.waituntil.WaitUntilTimes;

import com.dumbster.smtp.SmtpMessage;

public class DumbsterSmtpMessageBodyExtractorTest {
    private static final int SMTP_PORT = 9900;
    private static JavaMailSender javaMailSender;

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

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testExtractLinkByIdFromHtmlEmailWithAttachment()
            throws MessagingException {
        String emailBody = "<html><body><img src='cid:financeRingLogo'><br/>Zarejestruj sie przez kliknięcie na: <a id=\"registration-link\" href=\"http://www.onet.pl?costam=aaa&asas=333\">http://www.onet.pl?costam=aaa&asas=333</a></body></html>";
        sendEmailWithAttachement(emailBody);

        assertElementHrefAttributeMatches("registration-link",equalTo("http://www.onet.pl?costam=aaa&asas=333"));
    }

    @Test
    public void testExtractLinkByIdFromNonHtmlMessage()
            throws MessagingException {
        String emailBody = "http://www.onet.pl?costam=aaa&asas=333";
        sendEmailNoAttachement(emailBody);

        SmtpMessage aSingleMessageReceived = smtpMessageHelper()
                .aSingleMessageReceived();
        DumbsterSmtpMessageBodyExtractor dumbsterSmtpMessageBodyExtractor = new DumbsterSmtpMessageBodyExtractor(
                aSingleMessageReceived);
        exception.expect(RuntimeException.class);
        dumbsterSmtpMessageBodyExtractor
                .extractHtmlElementById("registration-link");
    }

    @Test
    public void testExtractLinkByIdFrom7bitHtml() throws MessagingException {
        String emailBody = "<html><body><br/>Click on <a id=\"weird_id\" href=\"http://www.onet.pl?costam=aaa&asas=333\">http://www.onet.pl?costam=aaa&asas=333</a></body></html>";
        sendEmailNoAttachement(emailBody);
        
        assertElementHrefAttributeMatches(
                "weird_id",
                equalTo("http://www.onet.pl?costam=aaa&asas=333"));
    }

    private void assertElementHrefAttributeMatches(String elemId,
            Matcher<? super String> matcher) {
        SmtpMessage aSingleMessageReceived = new SimpleSmtpServerHelper(
                startStopDumbster.getSimpleSmtpServer(), WaitUntilTimes.withMaxAndSleepInteval(1000, 100))
                .aSingleMessageReceived();
        DumbsterSmtpMessageBodyExtractor dumbsterSmtpMessageBodyExtractor = new DumbsterSmtpMessageBodyExtractor(
                aSingleMessageReceived);
        Element elem = dumbsterSmtpMessageBodyExtractor
                .extractHtmlElementById(elemId);
        assertThat(elem.attr("href"), matcher);
    }

    @Test
    public void testExtractLinkByIdFrom7bitHtmlWhenNoGivenElementIdExists()
            throws MessagingException {
        String emailBody = "<html><body><br/>Click on <a id=\"foo_id\" href=\"http://www.onet.pl?costam=aaa&asas=333\">http://www.onet.pl?costam=aaa&asas=333</a></body></html>";
        sendEmailNoAttachement(emailBody);

        SmtpMessage aSingleMessageReceived = smtpMessageHelper()
                .aSingleMessageReceived();
        DumbsterSmtpMessageBodyExtractor dumbsterSmtpMessageBodyExtractor = new DumbsterSmtpMessageBodyExtractor(
                aSingleMessageReceived);
        exception.expect(RuntimeException.class);
        dumbsterSmtpMessageBodyExtractor.extractHtmlElementById("bar_id");
    }

    private SimpleSmtpServerHelper smtpMessageHelper() {
        return new SimpleSmtpServerHelper(
                startStopDumbster.getSimpleSmtpServer(), WaitUntilTimes.withMaxAndSleepInteval(1000, 100));
    }

    @Test
    public void testExtractLinkByIdFromHtmlEmailWithNoAttachment()
            throws MessagingException {
        String emailBody = "<html><body><br/>Zarejestruj się przez kliknięcie na: <a id=\"registration-link\" href=\"http://www.onet.pl?costam=aaa&asas=333\">http://www.onet.pl?costam=aaa&asas=333</a></body></html>";
        sendEmailNoAttachement(emailBody);

        assertElementHrefAttributeMatches("registration-link", equalTo("http://www.onet.pl?costam=aaa&asas=333"));
    }

    private void sendEmailWithAttachement(String emailBody)
            throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("from@bar.foo");
        helper.setTo("jmil@test.bar");
        helper.setSubject("sample subject");
        helper.setText(emailBody, true);
        ClassPathResource image = new ClassPathResource("icon.png");
        helper.addInline("financeRingLogo", image);
        javaMailSender.send(message);
    }

    private void sendEmailNoAttachement(String emailBody)
            throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("from@bar.foo");
        helper.setTo("jmil@test.bar");
        helper.setSubject("sample subject");
        helper.setText(emailBody, true);
        javaMailSender.send(message);
    }
}
