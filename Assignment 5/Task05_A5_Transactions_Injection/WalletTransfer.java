import java.math.BigDecimal;
import java.sql.*;
public class WalletTransfer {
 public static void transfer(Connection c,int fromId,int toId,BigDecimal amount)throws SQLException{
  if(amount.signum()<=0)throw new IllegalArgumentException("Amount must be positive");
  boolean old=c.getAutoCommit();c.setAutoCommit(false);
  try{
   try(PreparedStatement debit=c.prepareStatement("UPDATE wallet SET balance=balance-? WHERE wallet_id=? AND balance>=?")){
    debit.setBigDecimal(1,amount);debit.setInt(2,fromId);debit.setBigDecimal(3,amount);
    if(debit.executeUpdate()!=1)throw new SQLException("Insufficient funds or source wallet missing");
   }
   try(PreparedStatement credit=c.prepareStatement("UPDATE wallet SET balance=balance+? WHERE wallet_id=?")){
    credit.setBigDecimal(1,amount);credit.setInt(2,toId);
    if(credit.executeUpdate()!=1)throw new SQLException("Destination wallet missing");
   }
   c.commit();
  }catch(SQLException|RuntimeException ex){try{c.rollback();}catch(SQLException rollback){ex.addSuppressed(rollback);}throw ex;}
  finally{c.setAutoCommit(old);}
 }
}
