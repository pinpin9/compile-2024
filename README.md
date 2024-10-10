# 编译器设计文档

> 学号：21374035
>
> 姓名：周琳萍
>
> 班级：222114

## 1.



## 2 编译器总体设计

我打算利用java语言编写一个编译器，总体结构参考了编译器的基本流程，包括词法分析，语法分析，错误处理，生成中间代码llvm，中间代码llvm优化，生成mips。

### 2.1 总体结构

- **前端**
  - **词法分析**：将源代码转换为一系列词法单元（Token）。这个阶段通过词法分析器（LexerAnalyse）处理源代码中的字符流，并识别出有效的标记（如关键字、变量名、常量、运算符等）。
  - **语法分析**：根据语法规则，将词法单元序列转换为语法树（AST）。语法分析器（ParserAnalyse）负责验证代码是否符合语言的语法规范。
  - **错误处理：**处理错误，包括词法分析，语法分析部分均存在错误
  - **语义分析与中间代码生成**：检查语法树的语义是否合理，如类型检查、作用域解析等。可能还会进行一些初步的优化。将语法树转换为中间表示（IR），这是一种介于源代码和目标代码之间的抽象表示，便于后续的优化和生成目标代码。
- **中端**
  - **代码优化**：对中间代码进行优化，减少冗余指令，改进性能。
- **后端**
  - **目标代码生成**：将优化后的中间代码转换为mips指令语言。

### 2.2 文件组织与接口设计

#### 2.2.1 文件组织



#### 2.2.2 接口设计

**编译器入口**

编译器的入口为src/Compiler.java，会依次进行读取输入文件，词法分析，语法分析，语义分析，生成中间代码，代码优化，生成目标代码。

## 3 词法分析

词法分析的主要任务是遍历源程序，生成一系列词法单元（Token）。词法单元包括保留字、字符串常量、数字常量、字符常量、标识符、符号等。此外，还需要处理注释中的内容。

```
+---frontend //前端入口
|   |   LexicalAnalyze.java // 词法分析
|   |
|   +---error
|   |       Error.java //错误类
|   |       ErrorNode.java //错误处理
|   |
|   \---token
|           Token.java //词法单元
|
+---settings
|       Settings.java //设置输入输出文件的名称
|
\---tools
        IO.java //从testfile.txt中读输入，输出到lexer.txt或error.txt文件
```

在LexicalAnalyse.java文件中，有一个analyse函数，对参数源代码逐字符读取，对字符的不同情况进行不同的处理。比如如果碰见$\n$字符，则lineNum行号增加1。

![image-20240919193704428](C:\Users\周琳萍\AppData\Roaming\Typora\typora-user-images\image-20240919193704428.png)

代码处理逻辑如下

```java
 for (int i = 0; i < len; i++) {
	char c = sourceCode.charAt(i);
	// 换行符，更新行号
	if (c == '\n') {
		lineNum++;
	} else if (c == '_' || isLetter(c)) {
		// 标识符或保留字
		i = handleWord(sourceCode, len, i);
	} else if (isDigit(c)) {
		// 数字
		i = handleNumber(sourceCode, len, i);
	} else if (c == '\'') {
		// 字符常量
		i = handleCharacter(sourceCode, len, i);
	} else if (c == '\"') {
		// 字符串常量
		i = handleString(sourceCode, len, i);
	} else if (c == '/') {
		// 注释
		i = handleComment(sourceCode, len, i);
	} else if (c == '&') {
		// 逻辑与 &&
		i = handleAndOperator(sourceCode, len, i);
	} else if (c == '|') {
		// 逻辑或 ||
		i = handleOrOperator(sourceCode, len, i);
	} else if (c == '<') {
		//  < 或 <=
		i = handleLessOperator(sourceCode, len, i);
	} else if (c == '>') {
		// > 或 >=
		i = handleGreaterOperator(sourceCode, len, i);
	} else if (c == '!') {
		// ! 或 !=
		i = handleNotOperator(sourceCode, len, i);
	} else if (c == '=') {
		// = 或 ==
		i = handleEqualsOperator(sourceCode, len, i);
	} else {
		// 处理单个符号
		i = handleSymbol(sourceCode, i);
	}
}
```


```java
// 标识符或保留字处理
private int handleWord(String sourceCode, int len, int i) {
	// 一直读入直到字符既不是_也不是字符和数字
	int j = i + 1;
	while (j < len && (sourceCode.charAt(j) == '_' || isDigitOrLetter(sourceCode.charAt(j))))
		j++;
	// 根据读入的子串判断是否为保留字，若不是，则为标识符
	String letter = sourceCode.substring(i, j);
	Token.TokenType type = wordMatch(letter);
	// 将该子串添加到tokenList
	addToken(type, lineNum, letter);
	return j - 1;
}
```

**Token类**

保存词法单元的类型，值以及行号，以便于在输出中打印。

```java
public class Token {
    /**
     * 词法单元类型
     */
    // 根据枚举类列举Token所有的类型
    public enum TokenType IDENFR,INTCON,STRCON,CHRCON,MAINTK,CONSTTK,INTTK,CHARTK,BREAKTK,CONTINUETK,IFTK,ELSETK,NOT,AND,OR,FORTK,GETINTTK,GETCHARTK,PRINTFTK,RETURNTK,PLUS,MINU,VOIDTK,MULT,DIV,MOD,LSS,LEQ,GRE,GEQ,EQL,NEQ,ASSIGN,SEMICN,COMMA,LPARENT,RPARENT,LBRACK,RBRACK,LBRACE,RBRACE
    }
    private TokenType type;
    private String value;
    private int lineNum;
    public Token(TokenType type, int lineNum, String value) {
        this.type = type;
        this.lineNum = lineNum;
        this.value = value;
    }
    ……
```

## 4 语法分析

**主要任务：**对词法分析生成的结果词法单元序列进行遍历，检查源代码是否符合语法规则，并将其组织成结构化的抽象语法树。

**方法：**递归下降子程序分析法，为每一个非终结符编写一个递归子程序，来处理对该终非结符的分析和识别工作。如果成功识别，则返回该非终结符结点，失败则说明该程序段不符合文法规则，则进行相应的错误处理。

> 递归下降子程序的本质上是进行最左推导，可能会遇到左递归和回溯问题

### 4.1 建立非终结符结点

#### **4.1.1 建立父节点Node**

因为每个节点都需要实现输出函数，且均具有Type，即节点类型属性，且输出Type的函数相同，则建立一个抽象类作为父节点。其具有未实现的print函数和公共printType函数，简化非终结符节点类

```java
public abstract class Node {
    public enum NodeType{ // 非终结符节点类型
        AddExp,
		……
    }
    public NodeType type;
    public Node(NodeType nodeType) {
        type = nodeType;
    }

    public abstract void print();

    public void printType() {
        ParserAnalyze.parserOutput.output("<"+type.toString()+">");
    }
}

```

#### 4.1.2 非终结符节点

例如对于节点`ConstDecl`

```java
// 文法：ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
public class ConstDecl extends Node{
    // 匹配终结符,'const'
    private Token constToken;
    // 匹配非终结符BType
    private BType bType;
    // 匹配对ConstDef的声明，可能有多个ConstDef
    private List<ConstDef> constDefList;
    // 匹配终结符，','
    private List<Token> commas;
    // 匹配终结符,';'
    private Token semicolonToken;

    // 构造方法
    public ConstDecl(Token constToken,BType bType,List<ConstDef> constDefList,List<Token> commas,Token semicolonToken){
        // 定义类型为ConstDecl
        super(NodeType.ConstDecl);
        this.constToken = constToken;
        ……
    }

    // 重写print方法
    @Override
    public void print() {
        constToken.print();
        bType.print();
        for(int i=0;i<constDefList.size();i++){
            // 先输出一个ConstDef
            constDefList.get(i).print();
            // 若commas里面还有内容，则输出
            if(i<commas.size()){
                commas.get(i).print();
            }
        }
        semicolonToken.print();
        printType();
    }
}
```

### 4.2 递归下降子程序

#### 4.2.1 nextsym()的实现

所有匹配最终都是对终结符的匹配，只有当上一个终结符匹配成功的时候，才会尝试去读下一个终结符。

currentToken为当前尝试匹配的Token，若当前Token的类型与指定的Token类型相匹配，则匹配进度currentIndex++，否则会报错

对于规定的错误，比如缺少';'，')'，']'，记录并补全

```java
// 匹配当前终结符的类型，并且改变currentToken
    private Token match(TokenType tokenType){
        Token temp = currentToken;
        if(currentToken.getType()==tokenType){
            next();
            return temp;
        }else{
            if(tokenType==TokenType.SEMICN){ // 缺少;
                errorHandler.addError(new Error(Error.ErrorType.i,tokens.get(currentIndex-1).getLineNum()));
                return new Token(TokenType.SEMICN,tokens.get(currentIndex-1).getLineNum(),";");
            } 
            ……
        }
        return null;
    }
    private void next(){
        if(currentIndex+1<len){
            currentIndex++;
            currentToken = tokens.get(currentIndex);
        }
    }
```

为了判断该进入哪个分析子程序，我们常常会对Token列表进行一些提前的匹配，并且不希望改变currentToken的值，所以我们另外创建了一个函数`preMatch`

```java
// 读取index个偏移以后的Token，判断非终结符的类型
    private boolean preMatch(int index, TokenType tokenType){
        if(index+currentIndex < len && tokens.get(currentIndex+index).getType() == tokenType){
            return true;
        }
        return false;
    }
```

#### 4.2.2 递归子程序

以处理ConstDecl的子程序为例，我们分析一下处理非终结符的子程序应该怎么编写

```java
	// ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDecl getConstDecl(){
        // 匹配终结符
        Token constToken = match(TokenType.CONSTTK);
        // 处理非终结符 BType
        BType bType = getBType();
        List<ConstDef> constDefList = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token semicolonToken = null;
        constDefList.add(getConstDef());
        while(preMatch(0,TokenType.COMMA)){ // { ','
            commas.add(match(TokenType.COMMA));
            constDefList.add(getConstDef());
        }
        semicolonToken = match(TokenType.SEMICN);
        // 匹配成功，则返回创建好的ConstDecl节点
        return new ConstDecl(constToken,bType,constDefList,commas,semicolonToken);
    }
```

### 4.3 左递归

对于文法出现的部分左递归文法，例如

```
 AddExp → MulExp | AddExp ('+' | '−') MulExp
```

可采用转换为扩展的 BNF 范式形式的方法予以解决，使得同种类型的连续表达式将不再通过递归推导而是通过循环直到到达边界得到。在完成分析后，还需要将相关的树结构进行重构，使得其结构符合文法的定义。即：

```
AddExp → MulExp { ('+' | '−') MulExp }
```

图示为

<img src="C:\Users\周琳萍\AppData\Roaming\Typora\typora-user-images\image-20241010162950424.png" alt="image-20241010162950424" style="zoom:50%;" />

对于此类的节点，在输出和后续生成中间代码的时候，应该格外注意

### 4.4 回溯

对于一般的产生式，我们采用FIRST集来判别即可，但是也有一些无法根据FIRST集直接判断的式子，则会产生问题：

- 选择哪个产生式。
- 产生式中{xxx}的重复部分，到底应该何时结束。

我们利用`savaPos函数`记录某次进入子程序之前的Token位置，如果发现当前读取失败，则利用`backPos()`返回之前记录的位置。

例如对于stmt的分析程序，可以根据FIRST集判断其他情况，但剩下四种情况

```
Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
| Exp ';'
| LVal '=' 'getint''('')'';'
| LVal '=' 'getchar''('')'';'
```

Exp也可以推导出LVal，则无法通过简单的getExp或者getLVal处理这几种情况。则我们先`savePos()`，进行一次`getExp();`的尝试，这个尝试会将LVal和Exp均处理到，如果读取之后，剩下的Token中的最新字符为"="，则说明为`LVal = xxx`式子，我们的尝试失败，`backPos()`回到之前记录的位置，读取`LVal`

```
private Stmt getStmt(){
        Exp exp = null;
        Token lParent = null;
        Token rParent = null;
        Cond cond = null;
        Stmt stmt1 = null;
        Stmt stmt2 = null;
        Token semiColonToken = null;
        if(preMatch(0,TokenType.LBRACE)){ // Block
            ……
        } else if (preMatch(0,TokenType.IFTK)) { // if
            ……
        } else if (preMatch(0,TokenType.FORTK)) { // for
            ……
        } else if (preMatch(0,TokenType.BREAKTK)||preMatch(0,TokenType.CONTINUETK)) { // break;continue;
            ……
        } else if (preMatch(0,TokenType.RETURNTK)) { // return
            ……
        } else if (preMatch(0,TokenType.PRINTFTK)) { // printf
            ……
        } else if (preMatch(0,TokenType.SEMICN)) { //只有分号
            ……
        } else {
            /*
            Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
            | Exp ';'
            | LVal '=' 'getint''('')'';'
            | LVal '=' 'getchar''('')'';'
             */
            savePos(); // 保存位置
            exp = getExp();
            LVal lVal = null;
            Token assign = null;
            Token token = null; // 读取getint或者getchar
            if(preMatch(0,TokenType.ASSIGN)){ // 剩下'=',说明为LVal = 语句
                backPos(); //回溯
                lVal = getLVal();
                assign = match(TokenType.ASSIGN);
                if(preMatch(0,TokenType.GETINTTK)){ // LVal = getint();
                    token = match(TokenType.GETINTTK);
                    lParent = match(TokenType.LPARENT);
                    rParent = match(TokenType.RPARENT);
                    semiColonToken = match(TokenType.SEMICN);
                    return new Stmt(Stmt.StmtType.LVALGETINT,lVal,assign,token,lParent,rParent,semiColonToken);
                } else if (preMatch(0,TokenType.GETCHARTK)) {
                    token = match(TokenType.GETCHARTK);
                    lParent = match(TokenType.LPARENT);
                    rParent = match(TokenType.RPARENT);
                    semiColonToken = match(TokenType.SEMICN);
                    return new Stmt(Stmt.StmtType.LVALGETCHAR,lVal,assign,token,lParent,rParent,semiColonToken);
                } else {
                    exp = getExp();
                    semiColonToken = match(TokenType.SEMICN);
                    return new Stmt(Stmt.StmtType.LVALASSIGN,lVal,assign,exp,semiColonToken);
                }
            }else{ //读取exp以后，如果剩下';',说明为Exp;
                semiColonToken = match(TokenType.SEMICN);
                return new Stmt(Stmt.StmtType.EXP,exp,semiColonToken);
            }
        }
    }
```

### 4.5 易错点

当我们遇到文法，`  UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp`时，FuncRParams为可选部分，我们理所当然将当前Token是否为')'符号作为判断是否需要读取FuncRParams的标志。

但是忽略了丢失`Ident '(' [FuncRParams] ')'`右括号的错误，上述判断天然失效。

比如`func(;`，这个时候，并没有参数，但由于丢失右括号，只剩下分号，则程序认为存在参数，进入getFuncRParams子程序，最后得到一个内容为空的FuncRParams结点。因为有缺少右括号的错误，不会尝试去输出这个节点，检测不到空指针错误，但为后续埋下了大雷。

我们可以根据Exp的FIRST集，编写一个判断是否为Exp的函数isExp()，若是isExp()成立，则读取Exp，规避错误~

```java
	// UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private UnaryExp getUnaryExp(){
        if(isUnaryOp(currentToken)){ // UnaryOp UnaryExp
            UnaryOp unaryOp = getUnaryOp();
            UnaryExp unaryExp = getUnaryExp();
            return new UnaryExp(unaryOp,unaryExp);
        } else if (preMatch(1,TokenType.LPARENT)) {
            Token ident = match(TokenType.IDENFR);
            Token lParent = match(TokenType.LPARENT);
            FuncRParams funcRParams = null;
            if(isExp()){ // 判断是否为Exp表达式
                funcRParams = getFuncRParams();
            }
            Token rParent = match(TokenType.RPARENT);
            return new UnaryExp(ident,lParent,funcRParams,rParent);
        } else {
            PrimaryExp primaryExp = getPrimaryExp();
            return new UnaryExp(primaryExp);
        }
    }
	private boolean isExp() {
        return  currentToken.getType() == TokenType.IDENFR ||
                isUnaryOp(currentToken) ||
                currentToken.getType() == TokenType.LPARENT ||
                currentToken.getType() == TokenType.INTCON ||
                currentToken.getType() == TokenType.CHRCON;
    }
```

## 5.语义分析

**主要任务：**通过语法分析阶段构建的语法树，进一步检查程序的语义是否正确，并生成符号表。

**符号表：**记录程序中的标识符，包括它们的名称、类型、作用域、位置等

**语义检查：**变量的声明与使用，函数调用，类型检查，作用域管理

### a.设计符号表结构



### b.遍历语法树

### c.作用域管理

### d.语义检查
