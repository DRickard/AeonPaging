package edu.ucla.library.libservices.aeon.callslip.beans;

public class Item
{
  private int itemID;
  private String copyNumber;
  private int bibID;
  private int mfhdID;

  public Item()
  {
    super();
  }

  public void setItemID( int itemID )
  {
    this.itemID = itemID;
  }

  public int getItemID()
  {
    return itemID;
  }

  public void setCopyNumber( String copyNumber )
  {
    this.copyNumber = copyNumber;
  }

  public String getCopyNumber()
  {
    return copyNumber;
  }

  public void setBibID( int bibID )
  {
    this.bibID = bibID;
  }

  public int getBibID()
  {
    return bibID;
  }

  public void setMfhdID( int mfhdID )
  {
    this.mfhdID = mfhdID;
  }

  public int getMfhdID()
  {
    return mfhdID;
  }
}
