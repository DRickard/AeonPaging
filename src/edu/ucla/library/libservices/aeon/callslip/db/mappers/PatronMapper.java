package edu.ucla.library.libservices.aeon.callslip.db.mappers;

import edu.ucla.library.libservices.aeon.callslip.beans.Patron;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class PatronMapper
  implements RowMapper
{
  public PatronMapper()
  {
    super();
  }

  public Object mapRow( ResultSet rs, int rowNum )
    throws SQLException
  {
    Patron bean;
    
    bean = new Patron();
    bean.setBarcode( rs.getString( "patron_barcode" ) );
    bean.setLastName( rs.getString( "last_name" ) );
    bean.setPatronId( rs.getInt( "patron_id" ) );

    return bean;
  }
}
