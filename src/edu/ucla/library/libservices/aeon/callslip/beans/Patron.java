package edu.ucla.library.libservices.aeon.callslip.beans;

public class Patron
{
  private String barcode;
  private String lastName;
  private int patronId;

  public Patron()
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

  public void setLastName( String lastName )
  {
    this.lastName = lastName;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setPatronId( int patronId )
  {
    this.patronId = patronId;
  }

  public int getPatronId()
  {
    return patronId;
  }
}
