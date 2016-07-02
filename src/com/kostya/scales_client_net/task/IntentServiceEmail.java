package com.kostya.scales_client_net.task;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.kostya.scales_client_net.Internet;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Kostya  on 28.06.2016.
 */
public class IntentServiceEmail extends IntentService {
    public static final String EXTRA_ADDRESS_EMAIL = "com.kostya.scales_client_net.task.EXTRA_ADDRESS_EMAIL";
    public static final String EXTRA_TEXT_EMAIL = "com.kostya.scales_client_net.task.EXTRA_TEXT_EMAIL";
    private static final String EXTRA_SUBJECT_EMAIL = "com.kostya.scales_client_net.task.EXTRA_SUBJECT_EMAIL";
    private static final String EXTRA_PERSONAL_EMAIL = "com.kostya.scales_client_net.task.EXTRA_PERSONAL_EMAIL";
    private static final String EXTRA_USER_EMAIL = "com.kostya.scales_client_net.task.EXTRA_USER_EMAIL";
    private static final String EXTRA_PASSWORD_EMAIL = "com.kostya.scales_client_net.task.EXTRA_PASSWORD_EMAIL";

    public IntentServiceEmail(String name) {
        super(name);
    }
    public IntentServiceEmail() {
        super(IntentServiceEmail.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            MailObject mailObject;

            try {
                mailObject = new MailObject(bundle.getString(EXTRA_ADDRESS_EMAIL));
                mailObject.setBody(bundle.getString(EXTRA_TEXT_EMAIL));
                mailObject.setSubject(bundle.getString(EXTRA_SUBJECT_EMAIL, IntentServiceEmail.class.getName()));
                mailObject.setPersonal(bundle.getString(EXTRA_PERSONAL_EMAIL, ""));
                mailObject.setUser(bundle.getString(EXTRA_USER_EMAIL));
                mailObject.setPassword(bundle.getString(EXTRA_PASSWORD_EMAIL));
            }catch (Exception e){return;}

            if (!Internet.getConnection(10000, 10)) {
                return;
            }

            try {
                MailSender mail = new MailSender(getApplicationContext(), mailObject);
                mail.send();
            } catch (MessagingException | UnsupportedEncodingException e) {

            }
        }
    }



    /**
     * Класс для отправки почты.
     */
    public static class MailSender {
        protected final Context mContext;
        protected final MailObject mailObject;


        public MailSender(Context cxt, MailObject object) {
            mContext = cxt;
            mailObject = object;
        }

    /*public MailSend(Context cxt, String email, String subject, String messageBody) {
        mContext = cxt;
        mailObject = new MailObject(email, subject, messageBody);
    }*/

        /**
         * Отправляем письмо.
         *
         * @throws MessagingException
         * @throws UnsupportedEncodingException
         */
        public void send() throws MessagingException, UnsupportedEncodingException {
            Session session = createSessionObject();
            Message message = createMessage(mailObject.getSubject(), mailObject.getBody(), session);
            Transport.send(message);
        }

        /**
         * Отправляем письмо с приклепленным файлом.
         * Добавляем путь файла при создании обьекта письма. {@link com.kostya.scales_client_net.task.IntentServiceEmail.MailObject#addFile(String)}.
         *
         * @throws MessagingException           Ошибка.
         * @throws UnsupportedEncodingException Ошибка.
         */
        public void sendMailAttach() throws MessagingException, UnsupportedEncodingException {
            Session session = createSessionObject();

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailObject.getUser(), mailObject.getPersonal()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailObject.getEmail(), mailObject.getEmail()));
            message.setSubject(mailObject.getSubject());

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            // Now set the actual message
            messageBodyPart.setText(mailObject.getBody());
            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            // Set text message part
            multipart.addBodyPart(messageBodyPart);
            // Part two is attachment

            String[] files = (String[]) mailObject.getFiles().toArray();
            for (String file : files) {
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(file);
                multipart.addBodyPart(messageBodyPart);
            }

            // Send the complete message parts
            message.setContent(multipart);

            Transport.send(message);
        }

        private Session createSessionObject() {
            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", "smtp.gmail.com");
            properties.setProperty("mail.smtp.socketFactory.port", "465");
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.port", "465");
            properties.put("mail.smtp.timeout", 10000);
            properties.put("mail.smtp.connectiontimeout", 10000);

            return Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailObject.getUser(), mailObject.getPassword());
                }
            });
        }

        private Message createMessage(String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailObject.getUser(), mailObject.getPersonal()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailObject.getEmail(), mailObject.getEmail()));
            message.setSubject(subject);
            message.setText(messageBody);
            return message;
        }
    }

    /**
     * Обьект электронной почты.
     */
    static class MailObject {
        List<String> files;
        protected String mEmail;
        protected String mSubject;
        protected String mBody;
        protected String mUser;
        protected String mPassword;
        protected String personal = "";

        public MailObject(String email) {
            mEmail = email;
        }

        MailObject(String email, String subject, String message) {
            mEmail = email;
            mSubject = subject;
            mBody = message;
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(String mEmail) {
            this.mEmail = mEmail;
        }

        public String getSubject() {
            return mSubject;
        }

        public void setSubject(String mSubject) {
            this.mSubject = mSubject;
        }

        public String getBody() {
            return mBody;
        }

        public void setBody(String mBody) {
            this.mBody = mBody;
        }

        public String getUser() {
            return mUser;
        }

        public void setUser(String mUser) {
            this.mUser = mUser;
        }

        public String getPassword() {
            return mPassword;
        }

        public void setPassword(String mPassword) {
            this.mPassword = mPassword;
        }

        public String getPersonal() {
            return personal;
        }

        public void setPersonal(String personal) {
            this.personal = personal;
        }

        public void addFile(String path) {
            files.add(path);
        }

        public List<String> getFiles() {
            return files;
        }
    }
}
