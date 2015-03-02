package JLibDiff;

import java.util.*;
 /**
   *  The <code>Hunk</code> class is a super class of classes <code>HunkAdd ,
   *   HunkChange</code> and <code>HunkDel</code> .it is an abstarct class.
   */ 
public abstract class Hunk implements HunkVisitable {
    
    Hunk next=null;
    
  public abstract void accept(HunkVisitor visitor);
    
  /**
   *  Returns a string representation of the current hunk
   *  with normal format .
   */
  public abstract String convert();

  /**
   *  Returns a string representation of the current hunk
   *  with ED_script format .
   */ 
  public abstract String convert_ED();

  /**
    *  Returns a string representation of the current hunk
    *  with RCS_script format .
    */ 
  public abstract String convert_RCS();

  /**
   *  Returns the number of low line of file passed in argument .
   *  Lines are inclusif.
   *
   * @param filenum The number of file (the first file '0', or the second '1').
   */
  public abstract int lowLine(int filenum);

  /**
   *  Returns the number of high line of file passed in argument .
   *  Lines are inclusif.
   *
   * @param filenum The number of file (the first file '0', or the second '1').
   */
  public abstract int highLine(int filenum);

  /**
   *  Returns the number of lines consedered in this hunk and which 
   *  came from file passed in argument .
   *
   * @param filenum The number of file (the first file '0', or the second '1').
   */
  public abstract int numLines(int filenum);

  /**
   *  Returns a string representing the line in  file and position
   *  passed in argument.
   *
   * @param filenum The number of file (the first file '0', or the second '1').
   * @param linenum the number of line that will be returned.
   */
  public abstract String relNum(int filenum,int linenum);


}
