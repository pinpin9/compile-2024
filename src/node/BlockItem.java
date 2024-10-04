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

    @Override
    public void print() {
        if(decl!=null){
            decl.print();
        }else{
            stmt.print();
        }
    }
}
