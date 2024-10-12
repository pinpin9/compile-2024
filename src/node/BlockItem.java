package node;

// BlockItem → Decl | Stmt
public class BlockItem extends Node{
    Decl decl;
    Stmt stmt;

    public BlockItem(Decl decl,Stmt stmt){
        super(NodeType.BlockItem);
        this.decl =decl;
        this.stmt = stmt;
    }

    public Stmt getStmt(){
        return stmt;
    }

    @Override
    public void print() {
        if(decl!=null){
            decl.print();
        }else{
            stmt.print();
        }
    }

    public void traverse() {
        if(decl!=null){
            decl.traverse();
        }else{
            stmt.traverse();
        }
    }
}
