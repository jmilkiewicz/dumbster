package pl.softmil.dumbster;

import java.io.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import com.dumbster.smtp.SmtpMessage;
import com.google.common.io.CharStreams;

public class DumbsterSmtpMessageBodyExtractor {
    private static final String TEXT_HTML_CHARSET_UTF_8_CONTENT_TRANSFER_ENCODING = "text/html;charset=UTF-8Content-Transfer-Encoding:";
    private final SmtpMessage smtpMessage;

    public DumbsterSmtpMessageBodyExtractor(SmtpMessage smtpMessage) {
        super();
        this.smtpMessage = smtpMessage;
    }
    
    public Element extractHtmlElementById(String id) {
        String body = smtpMessage.getBody();
        String quotedPrintable = digoutHtmlBody(body);
        String contentTransferEncoding = digoutBodyContentTransferEncoding(body);
        String decodeMessage = decodeMimeMessage(quotedPrintable,contentTransferEncoding);
        Element elementById = getElementById(id, decodeMessage);
        if(elementById == null){
            throw new RuntimeException("element with id='"+ id +"' not found");
        }
        return elementById;
    }

    private String digoutBodyContentTransferEncoding(String body) {
        int startIndex = body.indexOf(TEXT_HTML_CHARSET_UTF_8_CONTENT_TRANSFER_ENCODING) + TEXT_HTML_CHARSET_UTF_8_CONTENT_TRANSFER_ENCODING.length();
        int endIndex = body.indexOf("\n",startIndex); 
        return body.substring(startIndex, endIndex).trim();
    }

    private Element getElementById(String id, String decodeMessage) {
        Document parse = Jsoup.parse(decodeMessage);
        Element elementById = parse.getElementById(id);
        return elementById;
    }

    private String decodeMimeMessage(String body, String contentTransferEncoding) {
        try {
            return decodeMimeMessageUnsafe(body, contentTransferEncoding);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String decodeMimeMessageUnsafe(String htmlBody, String contentTransferEncoding)
            throws MessagingException, IOException {
        if(contentTransferEncoding.equals("quoted-printable")){
            htmlBody = addCRLFAfter76(htmlBody);
        }
        InputStream is = new ByteArrayInputStream(
                htmlBody.getBytes("iso-8859-1"));
        InputStream decode = MimeUtility.decode(is, contentTransferEncoding);
        String content = CharStreams.toString(new InputStreamReader(decode,
                "iso-8859-1"));
        return content;
    }

    private String digoutHtmlBody(String body) {
        int indexOf = body.indexOf(TEXT_HTML_CHARSET_UTF_8_CONTENT_TRANSFER_ENCODING);
        
        int startPartIndex = body.indexOf("\n",indexOf) + "\n".length();              
        int endIndex = body.indexOf("------=_Part", startPartIndex);
        return body.substring(startPartIndex, endIndex);
    }

    private String addCRLFAfter76(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i > 0 && i % 76 == 0) {
                result.append("\r\n");
            }
            result.append(string.charAt(i));
        }
        return result.toString();
    }

}
