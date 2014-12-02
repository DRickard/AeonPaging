package edu.ucla.library.libservices.aeon.callslip.webclients;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import edu.ucla.library.libservices.aeon.callslip.xml.UploadWriter;

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
  
  public void postCallSlip()
  {
    client = Client.create();
    webResource =
        client.resource( getRequetURL() );
    webResource.post( String.class, writer.getRequestBody() );
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
