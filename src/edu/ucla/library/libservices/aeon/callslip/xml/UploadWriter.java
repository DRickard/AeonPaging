package edu.ucla.library.libservices.aeon.callslip.xml;

import edu.ucla.library.libservices.aeon.callslip.beans.Item;
import edu.ucla.library.libservices.aeon.callslip.beans.Patron;

//import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.Result;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.Text;

public class UploadWriter
{
  private Document requestBody;
 // private Transformer identity;
  //private Result result;
  private Patron thePatron;
  private String dbID;
  private String comment;
  private String pickUp;
  private Item theItem;

  public UploadWriter()
  {
    super();
  }

  public Document getRequestBody()
  {
    Element root;

    try
    {
      requestBody =
          DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      root = requestBody.createElement( "ser:serviceParameters" );
      //root.setAttributeNS( "http://www.endinfosys.com/Voyager/serviceParameters", "ser", "ser" );
      root.setAttribute( "xmlns:ser","http://www.endinfosys.com/Voyager/serviceParameters" );
      requestBody.appendChild( root );

      root.appendChild( makeParametersNode() );
      
      root.appendChild( makePatronNode() );
      //identity = TransformerFactory.newInstance().newTransformer();
      //result = new StreamResult( new File( "C:\\Temp\\aeon\\srlf." + getTheItem().getBibID() + ".xml" ) );
      //identity.transform( new DOMSource( requestBody ), result );
    }
    catch ( ParserConfigurationException pce )
    {
      pce.printStackTrace();
      requestBody = null;
    }
    /*catch ( TransformerConfigurationException tce )
    {
      tce.printStackTrace();
      requestBody = null;
    }
    catch ( TransformerException te )
    {
      te.printStackTrace();
      requestBody = null;
    }*/
    return requestBody;
  }

  private Element makeParametersNode()
  {
    Element parameters;
    parameters = requestBody.createElement( "ser:parameters" );
    parameters.appendChild( makeParameterNode( "bibDbName", "UCLA" ) );
    parameters.appendChild( makeParameterNode( "mfhdId",
                                               String.valueOf( getTheItem().getMfhdID() ) ) );
    parameters.appendChild( makeParameterNode( "bibDbCode", "LOCAL" ) );
    parameters.appendChild( makeParameterNode( "requestCode",
                                               "CALLSLIP" ) );
    parameters.appendChild( makeParameterNode( "CVAL", "thisCopy" ) );
    parameters.appendChild( makeParameterNode( "requestSiteId",
                                               "1@" + getDbID() ) );
    parameters.appendChild( makeParameterNode( "itemId",
                                               String.valueOf( getTheItem().getItemID() ) ) );
    parameters.appendChild( makeParameterNode( "bibId",
                                               String.valueOf( getTheItem().getBibID() ) ) );
    parameters.appendChild( makeParameterNode( "REQCOMMENTS",
                                               getComment() ) );
    parameters.appendChild( makeParameterNode( "PICK", getPickUp() ) );
    
    return parameters;
  }

  private Element makePatronNode()
  {
    Element patron;
    Element authFactor;

    patron = requestBody.createElement( "ser:patronIdentifier" );
    patron.setAttribute( "lastName", getThePatron().getLastName() );
    patron.setAttribute( "patronHomeUbId", "1@" + getDbID() );
    patron.setAttribute( "patronId",
                         String.valueOf( getThePatron().getPatronId() ) );

    authFactor = requestBody.createElement( "ser:authFactor" );
    authFactor.setAttribute( "type", "B" );
    authFactor.appendChild( requestBody.createTextNode( getThePatron().getBarcode() ) );

    patron.appendChild( authFactor );
    return patron;
  }

  private Element makeParameterNode( String keyName, String paramValue )
  {
    Element parameter;
    Element value;

    parameter = requestBody.createElement( "ser:parameter" );
    parameter.setAttribute( "key", keyName );
    value = requestBody.createElement( "ser:value" );
    value.appendChild( requestBody.createTextNode( paramValue ) );
    parameter.appendChild( value );
    return parameter;
  }

  public void setThePatron( Patron thePatron )
  {
    this.thePatron = thePatron;
  }

  private Patron getThePatron()
  {
    return thePatron;
  }

  public void setDbID( String dbID )
  {
    this.dbID = dbID;
  }

  private String getDbID()
  {
    return dbID;
  }

  public void setTheItem( Item theItem )
  {
    this.theItem = theItem;
  }

  private Item getTheItem()
  {
    return theItem;
  }

  public void setComment( String comment )
  {
    this.comment = comment;
  }

  private String getComment()
  {
    return comment;
  }

  public void setPickUp( String pickUp )
  {
    this.pickUp = pickUp;
  }

  private String getPickUp()
  {
    return pickUp;
  }
}
/*
    Element subjectNode;

    subjectNode = output.createElement( "subject" );
    subjectNode.setAttribute( "id", subj.getId() );
    subjectNode.setAttribute( "type", 
                              ( subj instanceof SingleLevelSubject? "single" : 
                                "multi" ) );
    subjectNode.appendChild( makeTextNode( "name", subj.getName() ) );
    return subjectNode;
 */
