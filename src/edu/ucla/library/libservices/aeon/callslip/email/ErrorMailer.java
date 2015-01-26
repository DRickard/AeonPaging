package edu.ucla.library.libservices.aeon.callslip.email;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class ErrorMailer
{
  private static final String FROM_ADDRESS = "no-reply@library.ucla.edu";

  public ErrorMailer()
  {
    super();
  }

  public void sendMessage( String type, String item, String reqID,
                           String to )
  {
    StringBuffer messageBody;
    InternetAddress[] address;
    Message message;
    MimeBodyPart content;
    Multipart letter;
    Properties sysProps;
    Session mailSession;

    messageBody = new StringBuffer();

    sysProps = System.getProperties();
    sysProps.put( "mail.smtp.host", "em.library.ucla.edu" );
    mailSession = Session.getDefaultInstance( sysProps, null );
    message = new MimeMessage( mailSession );
    try
    {
      String[] tos = to.split( ";" );
      message.setFrom( new InternetAddress( FROM_ADDRESS ) );
      address = new InternetAddress[ tos.length ];
      for ( int i = 0; i < tos.length; i++ )
        address[ i ] = new InternetAddress( tos[ i ] );
      //{ new InternetAddress( to ) };
      message.setRecipients( Message.RecipientType.TO, address );
      message.setSubject( "Failed to create callslip for item " + item +
                          " for Aeon request " + reqID + ".\n" );
      message.setSentDate( new Date() );
      content = new MimeBodyPart();
      //messageBody.append( "A problem occurred while trying to page item " );
      //messageBody.append( item ).append( " for Aeon processing.\n" );
      if ( type.equalsIgnoreCase( "error" ) )
        messageBody.append( "There was a system error while submitting the request to Voyager.\n" );
      if ( type.equalsIgnoreCase( "block" ) )
        messageBody.append( "The request was blocked in Voyager.\n" );
      if ( type.equalsIgnoreCase( "file" ) )
        messageBody.append( "The file downloaded from Aeon did not contain a barcode.\n" );
      if ( type.equalsIgnoreCase( "db" ) )
        messageBody.append( "Barcode " + item + " in Aeon doesn’t match a Voyager record.\n" );
      //messageBody.append( "Exact error/block messages not currently available." );
      content.setText( messageBody.toString() );
      letter = new MimeMultipart();
      letter.addBodyPart( content );
      message.setContent( letter );
      Transport.send( message );
    }
    catch ( MessagingException me )
    {
      me.printStackTrace();
    }
  }
}
