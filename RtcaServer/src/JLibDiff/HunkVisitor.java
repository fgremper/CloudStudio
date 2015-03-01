package JLibDiff;

public abstract class HunkVisitor {

    public HunkVisitor() {
    }

    public void visitHunkAdd(HunkAdd hunk) {
	System.out.println("** visit of HunkAdd not implemented **");
    }

    public void visitHunkChange(HunkChange hunk) {
	System.out.println("** visit of HunkChange not implemented **");
    }

    public void visitHunkDel(HunkDel hunk) {
	System.out.println("** visit of HunkDel not implemented **");
    }
    
}
