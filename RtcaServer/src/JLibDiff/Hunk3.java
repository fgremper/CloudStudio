package JLibDiff;

import java.io.*;
import java.util.*;

/**
  * The <code>Hunk3</code> class represents a bloc of difference of three
  * files or  three Buffers.
  */
public class Hunk3{
 
  DiffType diff;
  int range[][]=new int[3][2];
  
  Vector a=new Vector();
  Vector b=new Vector();
  Vector c=new Vector();

  /**
    * Allocates a new Hunk3.
    */
  public Hunk3(){}

  /**
    * Set range of difference which consern this bloc in etch file by values
    * passed in argument.
    *
    * @param low0  start position of bloc of difference in the first file.
    * @param high0 end position of bloc ob difference in thefirst file.   
    * @param low1  start position of bloc of difference in the second file.
    * @param high1 end position of bloc ob difference in the second file. 
    * @param lowc  start position of bloc of difference in the therd file.
    * @param highc end position of bloc ob difference in the therd file.
    */
  public void setRange(int low0,int high0,int low1,int high1,
		       int lowc,int highc){
    range[0][0]=low0;
    range[0][1]=high0;
    range[1][0]=low1;
    range[1][1]=high1;
    range[2][0]=lowc;
    range[2][0]=highc;
  }

  /**
    * Returns a string representation of the current Hunk with normal format.
    */
  public String convert(){
    int i;
    int dontprint=0;
    int oddoneout;
   System.out.print("----------"+diff.code()+"\n");
    String s=new String("====");
    switch(diff.code())
      {
      case 5:
	dontprint=3;
	oddoneout=3;
	s=s.concat("\n");
	break;
      case 6:
      case 7:
      case 8:
	oddoneout=(int)diff.code() - 6;
	if(oddoneout==0)
	  dontprint=1;
	else
	  dontprint=0;
	s=s.concat((+oddoneout+1)+"\n");
	break;
      default:
	break;
      }
    for(i=0;i<3; i++)
      {
	int lowt=this.lowLine(i),
	    hight=this.highLine(i);
	s=s.concat(+(i+1)+":");
	switch(lowt-hight)
	  {
	  case 1:
	    s=s.concat(+(lowt-1)+"a\n");
	    break;
	  case 0:
	    s=s.concat(+lowt+"c\n");
	    break;
	  default:
	    s=s.concat(+lowt+","+hight+"c\n");
	    break;
	  }
	if(i==dontprint)
	  continue;
	if(lowt<=hight)
	  {

	  }
      }
    return s;
  }

  /**
    * Returns the value of position of bloc start line reliding the
    * file passed in argument.
    *
    * @param filenum   file number which can be:
    *                    0 to indicate first file.
    *                    1 to indicate second file.
    *                    2 to indicate therd file.
    */
  public  int lowLine(int filenum){
    return range[filenum][0];
  }

  /**
    * Returns the value of position of bloc end line reliding the
    * file passed in argument.
    *
    * @param filenum   file number which can be:
    *                    0 to indicate first file.
    *                    1 to indicate second file.
    *                    2 to indicate therd file.
    */
  public  int highLine(int filenum){
    return range[filenum][1];
  }
  
  /**
    * Returns the number of lines reliding this bloc of difference and the
    * file passed in argument.
    *
    * @param filenum   file number which can be:
    *                    0 to indicate first file.
    *                    1 to indicate second file.
    *                    2 to indicate therd file.
    */
  public int numLines(int filenum){
    return (range[filenum][1]-range[filenum][0]+1);
  }

}
