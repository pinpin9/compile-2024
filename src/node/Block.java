package node;

import error.SemanticError;
import token.Token;

import java.util.List;

// Block → '{' { BlockItem } '}'
public class Block extends Node{
    private Token lBrace;
    private List<BlockItem> blockItemList;
    private Token rBrace;

    public List<BlockItem> getBlockItemList() {
        return blockItemList;
    }
    public Token getrBrace(){
        return rBrace;
    }
    public Block(Token lBrace,List<BlockItem> blockItemList,Token rBrace){
        super(NodeType.Block);
        this.lBrace = lBrace;
        this.blockItemList = blockItemList;
        this.rBrace = rBrace;
    }

    @Override
    public void print() {
        lBrace.print();
        for(BlockItem blockItem : blockItemList){
            blockItem.print();
        }
        rBrace.print();
        printType();
    }


    public void traverse() {
        for(BlockItem blockItem : blockItemList){
            blockItem.traverse();
        }
    }
}
