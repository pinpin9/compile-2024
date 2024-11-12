package node;

import ir.types.constants.ConstChar;
import token.Token;

public class Character extends Node{
    private Token charConst;
    public Character(Token charConst) {
        super(NodeType.Character);
        this.charConst = charConst;
    }

    @Override
    public void print() {
        charConst.print();
        printType();
    }

    @Override
    public void buildIr() {
        int ch;
        if(charConst.getValue().length()==3){
            ch = charConst.getValue().charAt(1);
        } else{ // 转义字符
            ch = getValue(charConst.getValue().charAt(2));
        }
        valueUp = new ConstChar(ch);
    }

    private int getValue(char ch){
        switch (ch){
            case '0' -> {
                return 0;
            }
            case 'a' ->{
                return 7;
            }
            case 'b' -> {
                return 8;
            }
            case 't' -> {
                return 9;
            }
            case 'n' -> {
                return 10;
            }
            case 'v' -> {
                return 11;
            }
            case 'f' -> {
                return 12;
            }
            case '\"' -> {
                return 34;
            }
            case '\'' -> {
                return 39;
            }
            case '\\' -> {
                return 92;
            }
            default -> {
                return -1;
            }
        }
    }

    public void traverse() {

    }
}
