package JLibDiff;

class DiffType{

  private static final short CODE_ERROR=1;
  private static final short CODE_ADD=2;
  private static final short CODE_CHANGE=3;
  private static final short CODE_DELETE=4;
  private static final short CODE_DIFF_ALL=5;
  private static final short CODE_DIFF_1ST=6;
  private static final short CODE_DIFF_2ND=7;
  private static final short CODE_DIFF_3RD=8;

  private short typecode;

  public static DiffType ERROR =new DiffType(CODE_ERROR);
  public static DiffType ADD   =new DiffType(CODE_ADD);
  public static DiffType CHANGE=new DiffType(CODE_CHANGE);
  public static DiffType DELETE=new DiffType(CODE_DELETE);
  public static DiffType DIFF_ALL=new DiffType(CODE_DIFF_ALL);
  public static DiffType DIFF_1ST=new DiffType(CODE_DIFF_1ST);
  public static DiffType DIFF_2ND=new DiffType(CODE_DIFF_2ND);
  public static DiffType DIFF_3RD=new DiffType(CODE_DIFF_3RD);

  private DiffType(short pcode){
    typecode =pcode;
  }

  short code(){
    return typecode;
  }

  public String toString(){
    switch(typecode)
      {
      case CODE_ERROR:
	return "ERROR";
      case CODE_ADD:
	return "ADD";
      case CODE_CHANGE:
	return "CHANGE";
      case CODE_DELETE:
	return "DELETE";
      case CODE_DIFF_ALL:
	return "DIFF_ALL";
      case CODE_DIFF_1ST:
	return "DIFF_1ST";
      case CODE_DIFF_2ND:
	return "DIFF_2ND";
      case CODE_DIFF_3RD:
	return "DIFF_3RD";
      default:
	return "";
      }
  }
  public int toInt(){
    switch(typecode)
      {
      case CODE_DIFF_ALL:
	return 5;
      case CODE_DIFF_1ST:
	return 6;
      case CODE_DIFF_2ND:
	return 7;
      case CODE_DIFF_3RD:
	return 8;
      default:
	return 0;
      }
  }
}  
