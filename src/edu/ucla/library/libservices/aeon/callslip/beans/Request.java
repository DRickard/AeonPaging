package edu.ucla.library.libservices.aeon.callslip.beans;

public class Request
{
  private String barcode;
  private String library;
  private String note;
  private String reqID;
  
  public Request()
  {
    super();
  }

  public void setBarcode( String barcode )
  {
    this.barcode = barcode;
  }

  public String getBarcode()
  {
    return barcode;
  }

  public void setLibrary( String library )
  {
    this.library = library;
  }

  public String getLibrary()
  {
    return library;
  }

  public void setNote( String note )
  {
    this.note = note;
  }

  public String getNote()
  {
    return note;
  }

  public void setReqID( String reqID )
  {
    this.reqID = reqID;
  }

  public String getReqID()
  {
    return reqID;
  }
}
