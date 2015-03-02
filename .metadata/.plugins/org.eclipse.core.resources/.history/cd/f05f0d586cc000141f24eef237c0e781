package JLibDiff;

import java.util.*;

/**
  * The <code>HunkDel</code> class represents a bloc of difference that will
  * be deleted.
  */
public class HunkDel extends Hunk{

  int ld1;
  int lf1;
  int ld2;

  Vector a=new Vector();

    public void accept(HunkVisitor visitor) {
	visitor.visitHunkDel(this);
    }


    /**
     *  Returns string to delete.
     */
    public String getOldContents(){
	String s=new String();
	for (Enumeration e = a.elements() ; e.hasMoreElements() ;)
	    s=s.concat((String)e.nextElement());
	return s;
    }

  /**
   *  Returns a string representation of the current hunk
   *  with normal format .
   */
  public String convert(){
    String s=new String(Integer.toString(ld1));
    if(ld1!=lf1)
      s=s.concat(","+lf1);
    s=s.concat("d"+ld2+"\n");
    for (Enumeration e = a.elements() ; e.hasMoreElements() ;)
	s=s.concat("< "+(String)e.nextElement());
    return s;
  }

 /**
   *  Returns a string representation of the current hunk
   *  with ED_script format .
   */ 
  public String convert_ED(){
    String s=new String(Integer.toString(ld1));
    if(ld1!=lf1)
      s=s.concat(","+lf1);
    s=s.concat("d\n");
    return s;
  }

  /**
    *  Returns a string representation of the current hunk
    *  with RCS_script format .
    */ 
  public String convert_RCS(){
    String s=new String("d"+ld1+" "+(lf1-ld1+1)+"\n");
    return s;
  }

  /**
   *  Returns the number of low line of file passed in argument .
   *  Lines are inclusif.
   *
   * @param filenum The number of file (the first file '0', or the second '1').
   */
  public  int lowLine(int filenum){
    if(filenum==0)
      return ld1; 
    else
      return ld2;
  }

 /**
   *  Returns the number of high line of file passed in argument .
   *  Lines are inclusif.
   *
   * @param filenum The number of file (the first file '0', or the second '1').
   */
  public  int highLine(int filenum){
    if(filenum==0)
      return lf1;
    else
      return ld2;
  }

  /**
   *  Returns the number of lines consedered in this hunk and which 
   *  came from file passed in argument .
   *
   * @param filenum The number of file (the first file '0', or the second '1').
   */
  public  int numLines(int filenum){
    if(filenum==0)
      return (lf1 - ld1 +1);
    else
      return 1;
  }

 /**
   *  Returns a string representing the line in file and position
   *  passed in argument.
   *
   * @param filenum The number of file (the first file '0', or the second '1').
   * @param linenum the number of line that will be returned.
   */
  public  String relNum(int filenum,int linenum){
    if(filenum==0)
      return (String)a.elementAt(linenum);
    else
      return null;
  }

}
