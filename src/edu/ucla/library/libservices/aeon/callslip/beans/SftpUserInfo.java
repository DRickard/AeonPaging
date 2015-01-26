package edu.ucla.library.libservices.aeon.callslip.beans;

import com.jcraft.jsch.UserInfo;

public class SftpUserInfo
  implements UserInfo
{
  public SftpUserInfo()
  {
    super();
  }

  public String getPassphrase()
  {
    return null;
  }

  public String getPassword()
  {
    return null;
  }

  public boolean promptPassword( String string )
  {
    return false;
  }

  public boolean promptPassphrase( String string )
  {
    return false;
  }

  public boolean promptYesNo( String string )
  {
    return false;
  }

  public void showMessage( String string )
  {
  }
}
