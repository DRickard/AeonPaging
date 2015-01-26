package edu.ucla.library.libservices.aeon.callslip.webclients;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import edu.ucla.library.libservices.aeon.callslip.xml.UploadWriter;

import java.io.PrintStream;

public class SendRequestClient
{
  private Client client;
  private WebResource webResource;
  private UploadWriter writer;
  private String requetURL;

  public SendRequestClient()
  {
    super();
  }

  public String postCallSlip()
  {
    String response;
  
    try
    {
      client = Client.create();
      webResource = client.resource( getRequetURL() );
      response = webResource.post( String.class, writer.getRequestBody() );
      return response;
    }
    catch ( Exception e )
    {
      System.out.println( "in postCallSlip: " + e.getMessage() );
      e.printStackTrace();
      return null;
    }
  }

  public void setWriter( UploadWriter writer )
  {
    this.writer = writer;
  }

  public void setRequetURL( String requetURL )
  {
    this.requetURL = requetURL;
  }

  private String getRequetURL()
  {
    return requetURL;
  }
}
