package edu.ucla.library.libservices.aeon.callslip.db.mappers;

import edu.ucla.library.libservices.aeon.callslip.beans.Item;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class ItemMapper
  implements RowMapper
{
  public ItemMapper()
  {
    super();
  }

  public Object mapRow( ResultSet rs, int rowNum )
    throws SQLException
  {
    Item bean;
    
    bean = new Item();
    bean.setBibID( rs.getInt( "bib_id" ) );
    bean.setCopyNumber( rs.getString( "copy_number" ) );
    bean.setItemID( rs.getInt( "item_id" ) );
    bean.setMfhdID( rs.getInt( "mfhd_id" ) );

    return bean;
  }
}
