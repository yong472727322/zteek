package com.zteek.utils;

import org.apache.tomcat.jni.Address;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class EmailUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void mail() throws Exception {

        URLName url = new URLName("imap", "mail.aishk.com", -1, "INBOX", "xx@qq.com", "***password***");
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);
        Store store = session.getStore(url);
//      store.connect("mail.qq.com", "xx@aishk.com", "***password***");
        store.connect();
        Folder folder = store.getFolder(url);
        folder.open(Folder.READ_WRITE);
        Message[] message = folder.getMessages();
//      System.out.println("Message Length: " + message.length + "\nTop 3 Message subject:");
        for (int i = 0; i < 3; i++) {
            Message msg = message[i];
//            System.out.println("发件人:" + formatAddress(msg.getFrom())
//                    + "\n发送时间:" + formatDate(msg.getSentDate())
//                    + "\n收件人:" + formatAddress(msg.getRecipients(Message.RecipientType.TO))
//                    + "\n收件时间:" + formatDate(msg.getReceivedDate())
//                    + "\n抄送:" + formatAddress(msg.getRecipients(Message.RecipientType.CC))
////                  + "\n暗送:" + formatAddress(msg.getRecipients(Message.RecipientType.BCC))
//                    + "\n主题:" + msg.getSubject());
//          System.out.println("Next Reply to:" + formatAddress(msg.getReplyTo()));
            if (msg.isMimeType("text/plain")) {
                System.out.println("\t\tContent Type: " + msg.getContentType() + "\nContent:" + msg.getContent());
            } else if (msg.isMimeType("multipart/*")) {
                Multipart mp = (Multipart)msg.getContent();
                int count = mp.getCount();
//              System.out.println("\t\tContent Type: " + msg.getContentType() + "\n\nA total of " + count + " part");
                for (int j = 0; j < count; j++) {
                    BodyPart bp = mp.getBodyPart(j);
                    /*if (bp.isMimeType("text/plain")) {
//                      System.out.println(":Content Type:" + bp.getContentType() + "\nContent:\n");
                        System.out.println("-------------------------------------------------------------------------------------------------------------------------\n" + bp.getContent());
                    } else {
                        System.out.println("附件:Content Type:" + bp.getContentType() + "===" + bp.getFileName());
                    }*/
                    String disposition = bp.getDisposition();
                    if (disposition != null && disposition.equals(Part.ATTACHMENT)) {
                        System.out.println("*************************************************************************************************************************");
                        System.out.println("附件：" + bp.getFileName());
                        System.out.println("*************************************************************************************************************************");
                    } else {
                        if (bp.isMimeType("text/plain")) {
                            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
                            System.out.println(bp.getContent());
                        }
                    }
                }
            }

            System.out.println("\n\n\n=====================================================================================================================\n\n\n");
        }
    }

    public static String formatDate(Date date) {
        return sdf.format(date);
    }

    public static String formatAddress(Address[] addr) {
        StringBuffer sb = new StringBuffer();
        if (addr == null) {
            return "";
        }
        for (int i = 0; i < addr.length; i++) {
            Address a = addr[i];
//            if (a instanceof InternetAddress) {
//                InternetAddress ia = (InternetAddress)a;
//                sb.append(ia.getPersonal() + "<" + ia.getAddress() + ">,");
//            } else {
                sb.append(a.toString());
//            }
        }
        if (sb.length() != 0) {
            return sb.substring(0,sb.length()-1);
        }
        return "";
    }

    public static void main(String[] args) throws Exception {
        mail();
    }
}
