package JLibDiff;

class edit{ 	  

  int op;
  int line1;
  int line2;
  edit next;

  public void setop(int p){
       op=p;
  }
  public void setline1(int i){
    line1=i;
  }
  public void setline2 (int j){
    line2=j;
  }
  public void setnext (edit n){
    next=n;
  }

  public  void setedit(int i,int j,int k){
   op=i;
   line1=j;
   line2=k;
   next=null;
  }

 public int getop(){
  return op;
 }
 public int getline1(){
  return line1;
 }
 public int getline2(){
  return line2; 
 }

  public static  void add(edit e,edit m){
   m.next=e;
   e=m;
  }

 public  void affiche(){
   System.out.println("-----------------------");
   System.out.println(Integer.toString(op)); 
   System.out.println(Integer.toString(line1));
   System.out.println(Integer.toString(line2)); 
   System.out.println("-----------------------");
   if(next!=null)
     next.affiche();  
  }

}
