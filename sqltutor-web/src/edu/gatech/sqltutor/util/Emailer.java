/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Emailer {

	private static Emailer instance = null;
	
	private static Session mailSession;
	
	private static final String HOST = "smtp.gmail.com";
	private static final int PORT = 465;
	private static final String USER = "sql.tutor.gatech";    
	private static final String PASSWORD = "sqltutordev!"; 
	private static final String FROM = "SQLTutor <sql-tutor@googlegroups.com>";
	
	public void sendMessage(String recipient, String subject, String message) throws MessagingException {
	    MimeMessage mimeMessage = new MimeMessage(mailSession);
	
		mimeMessage.setFrom(new InternetAddress(FROM));
		mimeMessage.setSender(new InternetAddress(FROM));
		mimeMessage.setSubject(subject);
	    mimeMessage.setContent(message, "text/plain");
	
	    mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
	
		Transport transport = mailSession.getTransport("smtps");
	    transport.connect(HOST, PORT, USER, PASSWORD);
	
	    transport.sendMessage(mimeMessage, mimeMessage.getRecipients(Message.RecipientType.TO));
	    transport.close();
	}
	
	private Emailer() {
	    Properties props = new Properties();
	
	    props.put("mail.transport.protocol", "smtps");
		props.put("mail.smtps.host", HOST);
		props.put("mail.smtps.auth", "true");
		props.put("mail.smtp.from", FROM);
		props.put("mail.smtps.quitwait", "false");
	
	    mailSession = Session.getDefaultInstance(props);
	    mailSession.setDebug(true);
	}

	public static Emailer getInstance() {
		if(instance == null) 
			instance = new Emailer();
		return instance;
	}
}