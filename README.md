# 编译器设计文档

PS：笔者后端部分碰上了软分大作业+编译理论考试，于是偷懒没有写Mem2Reg，Mem2Reg是中端重要优化，希望大家以我为鉴:(，早点儿写编译实验

## 1.参考编译器介绍

首先，在本学期逐步完成编译器的过程中，我感受到了做一个成熟的编译器是一项非常浩大且困难的工程。所以非常感谢往届学长们提供的代码以及网站实验指导书。我参考了往年编译器的实现，做了一定的修改和完善，并且补充了今年新拓展文法。参考编译器：https://github.com/Arksuzuran/BUAA-2023Autumn-Compiler/

### 1.1 总体结构

词法分析，语法分析，错误处理，语法分析与llvm生成，llvm优化，mips生成及优化。

- 前端
    - 词法分析。构建了贪心简化的DFA，输入源程序，输出token序列。
    - 语法分析。透过递归下降子程序，实际上进行的是最左推导。其中采用了扩充的BNF范式解决左递归问题，采用了预读FIRST集的方式来解决回溯问题。
    - 错误处理。在词法分析、语法分析中已进行了部分错误处理，这里专门再进行一次递归下降来诊查某些错误。
    - 语法分析与llvm代码生成。遍历语法分析生成的AST，构建llvm中间代码。构建过程中采用了“走后门”的SSA。

- 中端(llvm优化)
    - 中端实现了Mem2reg，重构并生成了带有phi函数的中间代码。
    - 构建控制流图(CFG)，求解支配者与支配边界。
      重构SSA，phi指令的插入与变量重命名。

- 后端(mips代码生成及优化)
    - 非phi指令中间代码的翻译，phi指令的翻译。至此为止分配的都是虚拟寄存器。
    - 图着色寄存器分配。
    - 少量窥孔优化。

### 1.2 文件组织与接口设计

```
├── config		// 参数
│   └── Config.java					// 参数设置
├── frontend	// 前端入口
│   └── Lexer.java					// 词法分析入口及分析方法
|	└── Parser.java					// 语法分析入口及分析方法
|	└── Checker.java				// 错误处理入口
├── token		// token包
│   └── Token.java					// TOken类
|	└── TokenType.java				// Token类型及保留字、分界符等规定信息
├── node		// 用于递归下降的node包
│   └── <文法非终结符>.java			// node类，放置有语法分析、错误处理、ir的递归下降方法	
├── error		// 错误处理
|	└── Error.java					// 错误类
│   └── ErrorHandler.java			// 错误的记录与输出
|	└── ErrorCheckTool.java			// 错误的工厂类
├── symbol		// 错误处理，符号及栈式符号表
│   └── Symbol.java					// 符号类
|	└── SymbolTable.java			// 符号表类
|	└── SymbolTableStack.java		// 栈式符号表类
├── ir			// 中间代码生成与优化
|   ├── analyze				// 中间代码优化
|   ├── types				// value的类型
|   ├── values				// values
|   |	└── constants				// Constant value们
|   │   └── instructions			// Instruction value们
|   |	└── BasicBlock.java			
|   |	└── Function.java			
|   |	└── GlobalVariable.java	
|   |	└── Module.java	
|   |	└── Value.java	
|   |	└── User.java		
|   ├── Irc.java					// 在中间代码生成中保存上下文信息
│   ├── Irbuilder.java				// 中间代码的入口类及工厂类
├── backend		// Mips生成与优化 
|   ├── instructions		// Instruction们
|   ├── operands			// 操作数们，包括立即数，虚拟、物理寄存器，标签
|   ├── opt					// 目标代码优化
|   |	└── BlockliveVarInfo.java	// 活跃变量分析
|   │   └── RegBuilder.java			// 图着色寄存器分配
|   |	└── Peephole.java			// 窥孔优化
|   ├── Mc.java						// 在目标代码生成中保存上下文信息
|   └── MipsBuilder.java			// 目标代码的入口及指令的工厂类
├── utils		// 存放工具类 
└── Compiler.java			// 编译器入口
```

编译器的入口为src/Compiler.java。

调用Compiler类的doCompileing()方法即可对指定文件进行编译，并生成相应的llvm和mips代码。

可以根据config.Config.java中进行编译器的配置，包括是否开启各种优化。

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

```
│  Compiler.java
│  config.json
│
├─backend // 后端mips代码有关的结构
│  │  Mc.java
│  │  MipsBuilder.java
│  │
│  ├─instructions // Mips指令
│  │      MipsAnnotation.java
│  │      MipsBinary.java
│  │      MipsBranch.java
│  │      MipsCall.java
│  │      MipsCompare.java
│  │      MipsCondType.java
│  │      MipsInstruction.java
│  │      MipsLoad.java
│  │      MipsMacro.java
│  │      MipsMove.java
│  │      MipsMoveHI.java
│  │      MipsRet.java
│  │      MipsShift.java
│  │      MipsStore.java
│  │
│  ├─operands // Mips操作数
│  │      MipsImme.java
│  │      MipsLabel.java
│  │      MipsOperand.java
│  │      MipsPhyReg.java
│  │      MipsVirReg.java
│  │      RegType.java
│  │
│  ├─opt // 图着色算法以及乘法优化
│  │      LiveVarInfo.java
│  │      MulOptimizer.java
│  │      RegBuilder.java
│  │
│  └─values // mips结构
│          MipsBasicBlock.java
│          MipsFunction.java
│          MipsGlobalVariable.java
│          MipsModule.java
│
├─error // 错误处理有关内容，包括词法分析、语法分析、语义分析时的错误
│      Error.java
│      ErrorHandler.java
│      SemanticError.java
│
├─frontend
│      LexicalAnalyze.java
│      ParserAnalyze.java
│      SemanticAnalyze.java
│
├─ir // 中间代码LLVM有关的结构
│  │  IrBuilder.java
│  │  IrSymbolStack.java
│  │  IrSymbolTable.java
│  │
│  ├─instructions
│  │  │  Call.java
│  │  │  Instruction.java
│  │  │  Phi.java
│  │  │  Trunc.java
│  │  │  Zext.java
│  │  │
│  │  ├─binary
│  │  │      Add.java
│  │  │      BinaryInstruction.java
│  │  │      Icmp.java
│  │  │      Mul.java
│  │  │      Sdiv.java
│  │  │      Srem.java
│  │  │      Sub.java
│  │  │
│  │  ├─memory
│  │  │      Alloca.java
│  │  │      Getelementptr.java
│  │  │      Load.java
│  │  │      Store.java
│  │  │
│  │  └─terminator
│  │          Br.java
│  │          Ret.java
│  │
│  ├─types
│  │  │  ArrayType.java
│  │  │  CharType.java
│  │  │  FunctionType.java
│  │  │  IntType.java
│  │  │  LabelType.java
│  │  │  PointerType.java
│  │  │  ValueType.java
│  │  │  VoidType.java
│  │  │
│  │  └─constants
│  │          Constant.java
│  │          ConstArray.java
│  │          ConstChar.java
│  │          ConstInt.java
│  │          ConstStr.java
│  │          ZeroInitializer.java
│  │
│  └─values
│          BasicBlock.java
│          Function.java
│          GlobalVariable.java
│          Module.java
│          User.java
│          Value.java
│
├─node
│      AddExp.java
│      Block.java
│      BlockItem.java
│      BType.java
│      Character.java
│      CompUnit.java
│      Cond.java
│      ConstDecl.java
│      ConstDef.java
│      ConstExp.java
│      ConstInitVal.java
│      Decl.java
│      EqExp.java
│      Exp.java
│      ForStmt.java
│      FuncDef.java
│      FuncFParam.java
│      FuncFParams.java
│      FuncRParams.java
│      FuncType.java
│      InitVal.java
│      LAndExp.java
│      LOrExp.java
│      LVal.java
│      MainFuncDef.java
│      MulExp.java
│      Node.java
│      Number.java
│      PrimaryExp.java
│      RelExp.java
│      Stmt.java
│      UnaryExp.java
│      UnaryOp.java
│      VarDecl.java
│      VarDef.java
│
├─settings
│      Settings.java
│
├─symbol
│      Symbol.java
│      SymbolStack.java
│      SymbolTable.java
│
├─token
│      Token.java
│
└─tools
        IO.java
        MipsMath.java
        Pair.java
        StrTool.java
        Triple.java
```

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

![mxLKeAl5FRZSaMX.png](https://img.picui.cn/free/2025/03/04/67c7007ac0f0d.png)

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

![gQKoPRs9lFh4DL2.png](https://img.picui.cn/free/2025/03/04/67c700be759d5.png)

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

```java
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

## 5 语义分析

**主要任务：**通过语法分析阶段构建的语法树，进一步检查程序的语义是否正确，并生成符号表。

**符号表：**记录程序中的标识符，包括它们的名称、类型、作用域

**语义检查：**变量的声明与使用，函数调用，类型检查，作用域管理

**产生新作用域：**

- 函数
- stmt语句中的定义

### 5.1 设计符号表结构

关于符号表的设计，我分成三层

- 单个符号，Symbol类，分为函数符号和变量符号，利用两种不同的构造方法进行区别。函数符号和变量符号共有一些属性，如名字，类型，作用域序号等，函数符号独有属性，如参数表和函数返回类型。

  ```
  package symbol;
  
  import frontend.SemanticAnalyze;
  import node.FuncType;
  import node.Node;
  
  import java.util.ArrayList;
  import java.util.List;
  
  public class Symbol {
      // type不仅说明是变量还是函数，也声明了变量是否为数组
      public enum SymbolType{
          ConstChar, ConstInt, ConstCharArray, ConstIntArray,
          Char, Int, CharArray, IntArray,
          VoidFunc, CharFunc, IntFunc
      }
      private String name; //名称
      private SymbolType type; //类型
      private int scopeLevel = 0; //作用范围
      private int lineNum = 0; //声明行号
      private Node node; //声明的结点
      // 函数
      private FuncType funcType = null; //返回类型
      private List<Symbol> funcParams = new ArrayList<>(); //参数列表
  
      public Symbol(String name, SymbolType type, int scopeLevel, int lineNum, Node node, FuncType funcType, List<Symbol> funcParams){ // 声明函数
          this.name = name;
          this.type = type;
          this.scopeLevel = scopeLevel;
          this.funcParams = funcParams;
          this.lineNum = lineNum;
          this.node = node;
          this.funcType = funcType;
      }
      public Symbol(String name, SymbolType type, int scopeLevel, int lineNum, Node node){
          this.name = name;
          this.type = type;
          this.scopeLevel = scopeLevel;
          this.lineNum = lineNum;
          this.node = node;
      }
      ……
      public void print(){
          SemanticAnalyze.semanticOutput.output(scopeLevel + " " + name + " " + type);
      }
  }
  
  ```

- 符号表，SymbolTable类，建立为双向连接的链表结构。每个符号表有一个父符号表和一个子符号表数组。当构造符号表的时候，添加父符号表的信息，当符号表入栈的时候，在其父符号表中的子数组添加该符号表（有点儿绕啊。

    - 操作：添加符号入符号表、查询符号是否位于符号表中

  ```
  package symbol;
  
  import node.Node;
  
  import java.util.*;
  
  import symbol.Symbol.SymbolType;
  // 符号表
  public class SymbolTable {
      private Map<String,Symbol> symbolMap = new LinkedHashMap<>();
      public SymbolTable fatherSymbolTable; // 父符号表
      private List<SymbolTable> sonSymbolTable = new ArrayList<>(); // 子符号表
      private Node node; //因为程序块对应一个Node
      private int scopeLevel; // 符号表存储当前的作用域序号
  
  	// 构造符号表的时候只需要传入父节点信息和当前node即可，scopeLevel是程序计数的
      public SymbolTable(SymbolTable fatherSymbolTable, Node node, int scopeLevel){
          this.fatherSymbolTable = fatherSymbolTable;
          this.node = node;
          this.scopeLevel = scopeLevel;
      }
  
      public int getScopeLevel(){
          return scopeLevel;
      }
  
      public void setNode(Node node){
          this.node = node;
      }
  
      // 增加一个符号
      public void addSymbol(Symbol symbol){
          symbolMap.put(symbol.getName(), symbol);
      }
  
      // 判断符号是否存在于当前符号表中
      public boolean containSymbol(String name){
          ……
      }
  
      // 判断给定名称和类型的符号是否存在于当前符号表中，强类型
      public boolean containSymbol(String name, SymbolType type){
          ……
      }
  
      // 根据名称获取某个符号
      public Symbol getSymbol(……){
          ……
      }
  
      // 添加子符号表
      public void addSon(SymbolTable symbolTable){
          ……
      }
  	
  	// 输出符号表
      public void print(){
          for(Map.Entry<String, Symbol> entry:symbolMap.entrySet()){
              entry.getValue().print();
          }
          for(SymbolTable symbolTable: sonSymbolTable){
              symbolTable.print();
          }
      }
  }
  
  ```

- 符号栈，SymbolStack类。栈顶元素为当前遍历中的程序段。只有遇到{}中的结构，才会在栈中新增加一个元素，并且作用域序号+1

    - 包括对符号表的处理，和对符号的处理，以及标识是否需要return语句，标识是否处于循环

  ```
  package symbol;
  
  import java.util.Stack;
  
  import node.Node;
  import symbol.Symbol.SymbolType;
  public class SymbolStack {
      // 单例模式，使用方式：SymbolStack.getSymbolStack获取唯一实例，利用函数对唯一实例的栈变量进行操作
      private static SymbolStack symbolStack = new SymbolStack();
      public static SymbolStack getSymbolStack(){
          return symbolStack;
      }
  
      private Stack<SymbolTable> stack = new Stack<>();
  
      // 入栈，符号表开始
      public void push(SymbolTable symbolTable){
          if(!stack.isEmpty()){
              // 添加子节点信息
              current().addSon(symbolTable);
          }
          stack.push(symbolTable);
      }
      public void push(Node node,int scopeLevel){
          // 父节点信息
          push(new SymbolTable(current(),node,scopeLevel));
      }
      // 出栈，符号表结束
      public void pop(){
          stack.pop();
      }
      // 获取第一个符号表，但是不出栈
      public SymbolTable current(){
          if (stack.isEmpty()){
              return null;
          }
          return stack.peek();
      }
      // 获取当前符号表的作用域序号
      public int getScopeLevel(){
          return current().getScopeLevel();
      }
      // 添加符号，只能加入栈顶符号表
      public void addSymbol(Symbol symbol){
          current().addSymbol(symbol);
      }
  
      /*=======查找符号=========*/
      // 查找符号是否存在于栈顶符号表中，应用：声明时，判断符号名是否存在
      public boolean isInCurrent(String name){
          return current().containSymbol(name);
      }
      public boolean isInStack(String name){
          for(SymbolTable item:stack){
              if(item.containSymbol(name)){
                  return true;
              }
          }
          return false;
      }
  
      // 查找符号是否存在于整个栈中，应用：检查名字
      public boolean isInStack(String name, SymbolType type){
          for(SymbolTable item:stack){
              if(item.containSymbol(name,type)){
                  return true;
              }
          }
          return false;
      }
      // 返回第一个匹配的符号
      public Symbol getSymbol(String name, SymbolType type){
          for(SymbolTable item:stack){
              if(item.containSymbol(name,type)){
                  return item.getSymbol(name,type);
              }
          }
          return null;
      }
  
      // 返回第一个匹配的符号
      public Symbol getSymbol(String name){
          for(SymbolTable item:stack){
              if(item.containSymbol(name)){
                  return item.getSymbol(name);
              }
          }
          return null;
      }
  
      /*=====标志是否处于int/char函数中，对return的处理======*/
      private boolean isReturnFunc = false;
  
      public void inReturnFunc(){
          isReturnFunc = true;
      }
      public void leaveReturnFunc(){
          isReturnFunc = false;
      }
      public boolean isReturnFunc(){
          return isReturnFunc;
      }
  
      /*=====标志是否处于循环中，对break，Continue的处理=====*/
      private boolean isInLoop = false;
      public boolean isInLoop(){
          return isInLoop;
      }
      public void inLoop(){
          isInLoop = true;
      }
      public void leaveLoop(){
          isInLoop = false;
      }
  }
  
  ```

> 符号表图示
>
> <img src="https://s2.loli.net/2024/10/13/ei6fRQdYItAjqgK.png" alt="image-20241013103100716.png" style="zoom:50%;" />

我们对符号表的操作分为两种：查表和填表，这里引用一下ppt里的叙述，方便我们整理思路

**查表：**(1) 填表前查表，检查在程序的同一作用域内名字是否重复定义；PS：只用查当前符号表
(2) 检查名字的种类是否与说明一致，检查表达式中各变量的类型是否一致；PS：查整个符号栈

<LVal>,<UnaryExp>中如果符号未定义，则报错

<ConstDef>,<VarDef>,<FuncDef>,<FuncFParam>中如果符号已定义，则报错

**填表：**当分析到程序中的说明或定义语句时，将说明或定义的名字，以及与之有关的信息填入符号表中。

<ConstDef>,<VarDef>,<FuncDef>,<FuncFParam>中需要进行填表

**入栈：**即添加符号表，Stmt中遇见的<Block>，<FuncDef>函数定义，以及<MainFuncDef>

**添加符号：**常量<ConstDef>,变量<VarDef>,函数定义<FuncDef>,函数形参定义<FuncFParam>

**查找符号：**包括添加符号的时候，以及<LVal>,<UnaryExp>

#### SemanticError处理

有了符号相关类及之前写的程序，我们已经可以进行语义分析了。但是在遇到符号时，直接对栈和符号表进行操作不太优雅，于是我写了一个语义错误的处理函数。包含了对栈，符号表的操作，以及各种语义错误处理。

里面的方法包括：添加符号，符号表入栈，符号表出栈，检查函数参数个数是否相等，检查函数参数类型是否相同，检查是否改变常量，检查返回是否异常，检查printf语句格式字符和变量数目是否相同，检查break、continue语句是否出现在非循环语句。

实质上都是对栈的操作，详情见代码部分。

### b.遍历语法树

类似于语法分析中的print，实现traverse函数，进行递归遍历，对不同的非终结点符号进行不同的处理

```
	public void analyze(CompUnit compUnit){
        this.compUnit = compUnit;
        // 将根节点加入符号栈
        rootSymbolTable = new SymbolTable(null,compUnit, SemanticError.scopeLevel);
        SymbolStack.getSymbolStack().push(rootSymbolTable);
        // 对compUnit进行处理
        compUnit.traverse();
    }
```

例如<FuncDef>中

```
	// 函数声明，先在当前栈顶符号表中增加函数符号，再新建一个符号表，如果有参数且这个函数非重定义，则将参数列表存入函数符号中
    public void traverse() {
        List<Symbol> funcParams = new ArrayList<>();
        // 如果添加成功，则返回符号；否则返回null
        Symbol symbol = SemanticError.addSymbol(ident.getValue(), getType(), ident.getLineNum() ,this,funcType,funcParams);
        SemanticError.addTable(this);
        // 形参的处理
        if(funcFParams!=null){
            funcFParams.traverse();
            funcParams = funcFParams.getParams();
        }
        if(symbol!=null){
            symbol.setFuncParams(funcParams);
        }

        if(!isVoidFunc()){ // 需要返回值
            SemanticError.inReturnFunc();
            SemanticError.checkReturn(block);
        }
        block.traverse();
        SemanticError.leaveReturnFunc();
        SemanticError.popTable();
    }
```

#### **参数的处理**

我们的遍历是一个递归的处理，在进入FuncDef的时候并不知道参数的列表是什么，但是我们需要在此时添加函数符号，否则无法处理递归函数（即函数体中也有当前函数符号），所以此时我们先为当前函数符号设置空的参数列表。并在<FuncFParams>中增加了一个List<Symbol>列表，当对每个<FuncFParm>进行处理的时候，会返回一个参数变量符号，在列表中增加参数符号。当<FuncFParams>递归结束的时候，其List<Symbol>变量就已经是正确的参数列表了，如果不为空，则重置函数符号的参数列表。

```
    public void traverse() {
    	// 先传一个空的参数列表
        List<Symbol> funcParams = new ArrayList<>();
        // 添加函数符号
        Symbol symbol = SemanticError.addSymbol(ident.getValue(), getType(), ident.getLineNum() ,this,funcType,funcParams);
        // 添加符号表，并入栈
        SemanticError.addTable(this);
        // 形参的处理，对参数节点进行递归
        if(funcFParams!=null){
            funcFParams.traverse();
            // 获取参数列表
            funcParams = funcFParams.getParams();
        }
        if(symbol!=null){
        	// 重置参数列表
            symbol.setFuncParams(funcParams);
        }

        if(!isVoidFunc()){ // 需要返回值
            SemanticError.inReturnFunc();
            SemanticError.checkReturn(block);
        }
        block.traverse();
        SemanticError.leaveReturnFunc();
        SemanticError.popTable();
    }
```

```
	// FuncFParams中的递归
	public void traverse() {
        for(FuncFParam funcFParam:funcFParams){
            params.add(funcFParam.traverse());
        }
    }
```

### c.作用域管理

在SemanticError中，定义一个变量scopeLevel，初始化为1

如果往栈中新加一个符号表，则scopeLevel+1，并设置当前符号表的作用域序号为加后的scopeLevel

对于往符号表中新增一个符号，其作用域序号就是当前栈顶符号表的作用域序号

### d.语义检查

- 对于重定义和未定义错误，处理比较简单，只需要判定当前符号栈或者符号表中是否存在同名符号即可；
- 对于改变常数的错误，在遇见赋值语句的时候，判断左边的符号是否为const类型；
- 对于函数参数个数不匹配错误，在调用函数的语句中，判断实参长度和函数符号形参长度是否相等；
- 对于printf格式字符和表达式不匹配的错误，在printf语句中，计算%c和%d的个数，对参数长度进行判断；
- 对于非循环语句使用break | continue的错误，我们的处理分为两步，1.在进入for循环语句的时候设置isInLoop为true，出循环时设置为false，2.遇见break | continue语句时，判断isInLoop是否为True，如果否，则报错
- 对于return的处理，我们也分为两步
    - 1.在函数定义的时候，若是函数返回类型为int或char，则设置inReturnFunc为true，表示需要return语句；
    - 第二步分为两种情况，第一是在无返回值的函数体中多写了return语句，第二是需要返回值，但是没有写return语句
        - 对于第一种情况，在遇见return语句时，判断inReturnFunc是否为true，若是true，则说明正确，否则说明在无返回值的函数体中多加了return语句；
        - 对于需要返回值的函数，我们在函数声明<FuncDef>中，直接对函数体进行判断，如果其<Block>中最后一个<BlockItem>为<Stmt>结点，且结点类型为RETURN，则说明正确返回，否则说明没有返回值，报错（文法里规定的，有返回值的函数体的return语句一定出现在最后一行，只需要在函数末尾进行判断）

最最最最最最最后，我们只剩下一个错误“函数参数类型不匹配”没有处理啦，真是可喜可贺啊。

首先我们在<UnaryExp>中会调用函数，其中的遍历部分如下所示：

> 如果遇见函数调用，则首先判断标识符是否存在，如果不存在，则不会进行参数个数和参数类型的判断；
>
> 如果存在，则判断参数个数是否匹配，如果匹配，则isTrueCount = true，否则isTrueCount = false，如果个数不匹配，则不会对类型进行判断；
>
> 如果参数列表为空，不需要进行类型判断和递归处理；
>
> 如果参数列表不为空，参数个数匹配则对参数类型进行判断；
>
> 以上判断不影响对参数进行递归的操作

```
	public void traverse() {
        if(primaryExp!=null){
            primaryExp.traverse();
        } else if (unaryExp!=null) {
            unaryExp.traverse();
        } else { // 函数调用
            // 标识符是否存在
            Symbol symbol = SemanticError.checkSymbol(ident.getValue(), ident.getLineNum());
            // 函数参数个数是否匹配
            Boolean isTrueCount = false;
            if(symbol != null){
                isTrueCount = SemanticError.checkFuncParamCount(symbol, getParamsCount(), ident.getLineNum());
            }
            // 参数类型是否匹配
            if(funcRParams!=null){
                // 函数参数类型是否匹配
                if(isTrueCount){
                    SemanticError.checkFuncParamsType(symbol,funcRParams.getExpList(), ident.getLineNum());
                }
                funcRParams.traverse();
            }
        }
    }
```

checkFuncParamsType函数

> 对于符号表中的函数符号和实参列表进行匹配判断，对于每一个形参，判断其和对应实参类型是否匹配
>
> 因为在当前文法和错误定义中，数组是无法参与计算的，所以我们只需要获取实参表达式的第一个变量/常量，判断其类型，就可以知道是Int/Char/intArray/CharArray
>
> 同理，我们还没有对参数进行递归，不知道当前的参数类型，所以我们额外写一个递归函数getVarType，不对参数未定义进行判断，只获取参数类型
>
> 如果是函数，则只会返回Int/Char，去掉其函数类型的Func字段；如果是const变量，去掉其const字段；如果是数组，但是ident[exp]中，exp存在，则说明对数组实现了降维，去掉Array字段

```
    // 检查函数参数的类型是否相同，e错误
    public static void checkFuncParamsType(Symbol symbol, List<Exp> expList,int lineNum){
        // 函数形参列表
        List<Symbol> funcFParams = symbol.getFuncParams();
        /* 判断每个exp的类型和函数形参是否相同
        * 错误类型
        * 传递数组给变量
        * 传递变量给数组
        * 传递char型数组给int型数组
        * 传递int型数组给char型数组
        * */
        for(int i=0;i<funcFParams.size();i++){
            String expType=expList.get(i).getVarType();
            String paramType = funcFParams.get(i).getType().toString();
            if((expType.contains("Array")&&!paramType.contains("Array")) || (!expType.contains("Array")&&paramType.contains("Array")) ){
                errorHandler.addError(new Error(Error.ErrorType.e,lineNum));
            } else if ((expType.equals("CharArray")&&paramType.equals("IntArray"))|| (paramType.equals("CharArray")&&expType.equals("IntArray"))) {
                errorHandler.addError(new Error(Error.ErrorType.e,lineNum));
            }
        }
    }
```

## 6 中间代码生成

生成中间代码的目标是LLVM IR

### 6.1 LLVM结构

根据涉及的语法结构，我们把LLVM语法结构由粒度从高到低划分为

1. 整个模块Module
2. 函数Function
3. 基本块BasicBlock
4. 指令Instruction
5. 变量/常量&&符号
6. 变量：参数、局部变量；常量：字面量

![_20241104100243.png](https://s2.loli.net/2024/11/04/KJmLy6QdnEc7lYb.png)

对于模块中不同粒度的所有语法结构，我们借鉴LLVM官方库的实现方法，将都作为 Value 类的子类。其中，User 类是 Value 的一个特殊的子类，是一种可以使用其他 Value 对象的 Value 类。Value为值的基类，User为操作数的基类。

Function、BasicBlock 和 Instruction 都有使用的语法结构，都是 User 的子类，也是 Value 的子类。

User 和 Value 之间的配对用 Use 类记录，通过 Use 类建立起了语法结构的上下级关系双向索引。这样所有的语法结构都可以统一成 Value 类，语法结构间的使用关系都可以统一成 Use 类。Use 关系是 LLVM 编译器实现的核心架构，索引关系的抽象使其能够在全局保存，可以大大提高代码优化的效率。

**Value类**

表示操作数的对象

```
%add = add nsw i32 %a, %b
```

%a, %b均指一个操作数

```
package ir;

import ir.types.ValueType;

import java.util.ArrayList;
import java.util.List;

public class Value {
    private final int id; // 唯一标识
    private ValueType valueType; // 类型
    private String name; // 虚拟寄存器名称
    private Value parent; // 包含当前Value的父Value，表示嵌套关系，例如Instruction可能嵌套在Function中
    private List<User> userList = new ArrayList<>(); // 使用者

    private static int idCount=0;

    ……
}

```

**User类**

操作数的对象，可以使用其他的Value，但是本身也是一个Value的对象，可以被其他User使用。类中增加了Use关系

```
	// 当前操作对象所引用的所有操作数
    private List<Value> operands = new ArrayList<>();
```

**指令Instruction类**

Function、BasicBlock 和 Instruction 都有使用的语法结构，都是 User 的子类，也是 Value 的子类。

`%7 = add i32 %5, %6`

拿上面一条指令来说，Instruction既指这条add指令本身，也指%7这个结果。`%7` 是一个临时寄存器，是 `Instruction` 的实例，它的操作数里面有两个值，一个是 `%5`，一个是 `%6`。`%5` 和 `%6` 也是临时寄存器，即前两条 `Instruction` 的实例。

<img src="https://s2.loli.net/2024/11/04/tuPJiFGzCDdnjgB.png" alt="_20241104113726.png" style="zoom:200%;" />

![30639449344bc202b2e4ac7ba5b5ab1a.png](https://s2.loli.net/2024/11/04/lj4sKcEIPUtdMk5.png)

**Function**

增加了参数列表属性，构造Function的时候只传入了每个参数的类型。

比如`int foo(int a, int b[])`

- 首先对此FuncDef节点进行构建
- 对FuncDef的FuncFParams节点进行构建，获取参数的类型，存入一个综合属性列表

**全局变量GlobalVariable**

**属性**

- name：名称
- valueType：类别，IntType | CharType | ArrayType，说明变量是int，char或者数组
- isConst：是否是常量
- initVal：初始化值，为ConstInt | ConstChar | ConstArray

实际上在全局变量类的内部会生成一个PointerType指向ValueType，因为全局变量是存储在内存中的。

**根节点Module**

中间代码的基本单元，全局唯一

### 6.2 架构设计

#### 6.2.1 buildIr：递归下降实现

所有的node类中均实现了父类的抽象方法buildIr()，用于进行基于属性翻译文法的递归下降

```java
public abstract void buildIr(){
	……
}
```

#### 6.2.2 IrBuilder：构建LLVM中间代码

IrBuilder类是属性翻译递归下降程序的入口，且封装有构建llvm元素的工厂模式方法

**IrBuilder.java**

实现对add指令的构建

```java
	// 命名计数器
    private int nameCnt = 0;
    public String getName(){
        return "%v"+nameCnt++;
    }
    /**
     * 方法描述： 构建Add指令，+
     * @param block 所属基本块
     * @param value1 操作数1
     * @param value2 操作数2
     */
    public Add buildAdd(BasicBlock block, Value value1, Value value2){
        value1 = transBinary(block, value1);
        value2 = transBinary(block, value2);
        Add add = new Add(new IntType(32), getName(), block, value1, value2);
        block.addTailInstruction(add);
        return add;
    }
    // 类型转换
    private Value transBinary(BasicBlock block, Value value){
        if(value.getValueType().isI1() || value.getValueType().isChar()){
            value = buildZext(block, value);
        }
        return value;
    }
```

在AddExp结点中，获取到了计算单个符号的值value1和value2

```java
	if(op.getType() == Token.TokenType.PLUS){
		value1 = builder.buildAdd(curBlock, value1, value2);
	} 
```

#### 6.2.3 递归下降的上下文信息

因为我们使用的是基于属性翻译文法的递归下降子程序生成中间代码，所以需要综合属性和继承属性来保存相关的信息。

在所有非终节点的父节点Node中保存相关信息

利用static静态变量，可以使全局只有唯一变量，免于复杂的setter和getter。

```java
	// 当前函数块
    protected static Function curFunc = null;
    // 当前基本块
    protected static BasicBlock curBlock = null;
	// 处理循环
    protected static Stack<BasicBlock> endLoop = new Stack<>();
    protected static Stack<BasicBlock> nextLoop = new Stack<>();

    /*==========继承属性==========*/
    protected static boolean needCalExp = false; // 表达式可求值，求值结果为valueUp，用于全局变量的初始化部分
    protected static ValueType rtnType = null; // 函数返回类型
    protected static boolean lValAtLeft = false; // 是赋值变量还是计算变量
    protected static boolean buildFuncRParams = false;

    /*==========综合属性==========*/
    protected static List<ValueType> sysArgs = new ArrayList<>(); // 函数的参数类型数组，所有的参数，往上传
    protected static ValueType argType = null; // 函数的参数类型
    protected static Value valueUp = null; // Value类型的回传
    protected static List<Value> valueUpList = new ArrayList<>(); // Value类型数组的回传
    protected static Stack<Value> funcParams = new Stack<>(); // 函数实参
    protected static boolean singleCmp = false; // 是否只有单个符号
```

### 6.3 关键处理

#### 6.3.1 声明语句的处理

需要进行的主要操作有：

- alloca（分配变量的栈空间，返回指向该空间的指针）

- store（给变量赋值，即向指针所指空间内进行写）

对于全局变量和全局常量，构建一个GlobalVariable变量，存入Module模块中，并加入符号表

对于int类型的变量，init值的ValueType为ConstInt，char则为ConstChar，如果是数组，则为ConstArray，且存有数组中的元素类型

对于局部变量，则先进行Alloca，分配一个指针指向当前变量，所以Alloca的ValueType均为PointerType。如果是int，则PointerType指向IntType，其他类型同理。在符号表中加入当前符号，包括变量名称和当前alloca类型的Value。如果有初始化，则利用buildStore函数将初始化的值存入alloca分配的空间之中。

当使用**store**为元素赋值的时候，我们需要利用**getelementptr**指令，获取对应元素具体存储地址的指针。

```
	// %2 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
    // 三个参数，第一个参数为为数组分配的空间，获取指针变量，%2为指针类型，获取数组的首地址
    // %3 = getelementptr inbounds i32, i32* %2, i32 1
    // 两个参数，第一个参数为数组首地址，第二个参数为获取的下标值，获取数组中某个元素的地址
```

> getelementptr求地址指令：
>
> 这个指令可以带一个偏移，或者两个偏移。
>
> 记getelementptr(a, op1)为：对基地址指针a，使用gep指令，带一个操作数（偏移）op1。
>
> 记getelementptr(a, op1, op2)为：对基地址指针a，使用gep指令，带两个操作数（偏移）op1, op2。[要求a一定是指向数组的指针]
>
> 结论为：
>
> - $getelementptr(a, op1) = a + op1*sizeof(a)$，返回指针类型与a相同。
> - $getelementptr(a, op1, op2) =  a + op1*sizeof(a) + op2*sizeof(a指向的数组的元素)$【例如如果a指向`a[2][3]`，那么这里`sizeof(a指向的数组的元素)`就是a[0]或者a[1]的大小】，返回指针类型是a“降了一维”后的类型。
>
> 以`int a[10]`为例，假设a基地址0，容易计算出一维数组a的大小为40
>
> - getelementptr(a, 0)会返回什么？
>
> 会返回0 *24 = 0，即a的地址。
>
> 其类型是`[10 x i32]*`（即与a的类型相同）。
>
> - getelementptr(a, 1)会返回什么？
>
> 会返回1 * 24 = 1，即跳过一整个a之后的地址，不在a数组之内。
>
> 其类型是`[10 x i32]*`（即与a的类型相同）。
>
> - getelementptr(a, 0, 0)会返回什么？
>
> 会返回0*40 +0 *4 = 0 ，即a[0]的地址。其类型是`i32*`（即脱掉了最外层的[]，但是地址的值不变。因此该指令经常用来转变一个指针的类型）
>
> - getelementptr(a, 0, 1)会返回什么？
>
> 会返回0*40 + 1 * 4 = 4，即a[1]的地址。
>
> 其类型是`i32*`。

VarDef.java

```java
	@Override
    public void buildIr() {
        String name = ident.getValue();
        if(constExp == null){ // 非数组
            // 全局变量的值都是可以计算出来的
            if(stack.isGlobal()){ // 全局变量
                Constant init = null;
                if(initVal == null){
                    init = bType.getbType().getType() == Token.TokenType.INTTK ? new ConstInt(0) : new ConstChar(0);
                }else{
                    needCalExp = true;
                    initVal.buildIr();
                    needCalExp = false;
                    init = (Constant) valueUp;
                    if(bType.getbType().getType() == Token.TokenType.CHARTK){
                        init = new ConstChar(((ConstInt)init).getValue());
                    }
                }
                builder.buildGlobalVariable(name, false, init);
            } else { // 局部变量
                Alloca alloca = builder.buildAlloca(getValueType(), curBlock);
                // 符号表中增加该局部变量
                stack.addSymbol(name, alloca);
                // 得到的可能是load，add，getelementptr，constant等
                if(initVal!=null){
                    initVal.buildIr();
                    if(alloca.getAllocatedType() instanceof CharType && valueUp.getValueType() instanceof IntType){
                        if(valueUp instanceof ConstInt){
                            valueUp = new ConstChar(((ConstInt)valueUp).getValue());
                        }else{
                            valueUp = builder.buildTrunc(curBlock, valueUp);
                        }
                    } else if (alloca.getAllocatedType() instanceof IntType && valueUp.getValueType() instanceof CharType) {
                        if(valueUp instanceof ConstChar){
                            valueUp = new ConstInt(((ConstChar)valueUp).getValue());
                        } else{
                            valueUp = builder.buildZext(curBlock, valueUp);
                        }
                    }
                    builder.buildStore(curBlock, valueUp, alloca);
                }
            }
        }else{ // 数组
            constExp.buildIr();
            int dim = ((ConstInt)valueUp).getValue();
            if(stack.isGlobal()){ // 全局数组变量
                // 为读取当前初始化节点，清空综合属性上传的值
                if (valueUpList!=null){
                    valueUpList.clear();
                }
                // 初始化值，构建常量数组
                Constant constArray = null;
                if(initVal!=null){ // 有初始化
                    initVal.buildIr();
                    // 存储计算出来的数组初始化值
                    ArrayList<Constant> constants = new ArrayList<>();
                    for(int i = 0; i<dim;i++){
                        if(i<valueUpList.size()){
                            if(isChar()){
                                constants.add(new ConstChar(((ConstInt)valueUpList.get(i)).getValue()));
                            }else {
                                constants.add((Constant) valueUpList.get(i));
                            }
                        }else{
                            if(isChar()){
                                constants.add(new ConstChar(0));
                            }else {
                                constants.add(new ConstInt(0));
                            }
                        }
                    }
                    constArray = new ConstArray(constants);
                } else { // 没有初始化，利用zeroInitializer进行出事啊
                    constArray = new ZeroInitializer(new ArrayType(getValueType(), dim));
                }
                builder.buildGlobalVariable(name,false, constArray);
            } else { // 局部数组变量
                Alloca alloca = builder.buildAlloca(getValueType(dim), curBlock);
                stack.addSymbol(name, alloca);
                Getelementptr base = builder.buildGetElementPtr(curBlock, alloca, new ConstInt(0), new ConstInt(0));
                if(valueUpList != null){
                    valueUpList.clear();
                }
                if(initVal!=null){
                    initVal.buildIr();
                }
                for(int i = 0; i < valueUpList.size(); i++){
                    Value addr = builder.buildGetElementPtr(curBlock, base, new ConstInt(i));
                    Value value = valueUpList.get(i);
                    if(((PointerType)addr.getValueType()).getPointingType() instanceof IntType && value.getValueType() instanceof CharType){
                        if(value instanceof ConstChar){
                            value = new ConstInt(((ConstChar)value).getValue());
                        }else{
                            value = builder.buildZext(curBlock, value);
                        }
                    } else if(((PointerType)addr.getValueType()).getPointingType() instanceof CharType && value.getValueType() instanceof IntType){
                        if(value instanceof ConstInt){
                            value = new ConstChar(((ConstInt)value).getValue());
                        }else{
                            value = builder.buildTrunc(curBlock, value);
                        }
                    }
                    builder.buildStore(curBlock, value, addr);
                }
            }
        }
    }
```

### 6.4 SSA处理

#### 6.4.1 基本概念

静态单赋值（Static Single Assignment, SSA）是编译器中间表示中的一种变量的命名约定。当程序中的每个变量都有且只有一个赋值语句时，称一个程序是 SSA 形式的。

在llvm中，每个变量都在使用前都必须先定义，且每个变量只能被赋值一次。

### 6.5 易错点

- 类型转换可太容易错了（哭，但是有一种较为稳妥的方法。就是比如在构建二元指令的时候判断类型是否为int，不是则zext。但是这个方法在声明语句的时候不太好用emm，就嗯转
- 函数调用嵌套时，实参会被覆盖掉。解决方法，在FuncDef中记录参数个数num，在每次调用的时候取出最近num个参数
- void类型函数也需要ret语句

## 7 目标代码生成

对中间代码LLVM IR生成的module进行分析，生成mipsModule

### 7.1 mips基础知识

#### 7.1.1 代码结构

mips代码通常分为.data数据段和.text代码段

##### .data段

- .word，四字节整数，int类型
- .byte，一字节整数，char类型
- .space，未初始化的变量分配空间
- .asciiz，字符串

##### .text段

函数块，通常将入口函数放在最前面

#### 7.1.3 寄存器类型

**通用寄存器**

| **寄存器**  | **编号**  |                **用途**                |
| :---------: | :-------: | :------------------------------------: |
|   `$zero`   |   `$0`    |     永远存储值 `0`，只读，无法修改     |
|    `$at`    |   `$1`    |      汇编器保留寄存器，用于伪指令      |
|  `$v0-$v1`  |  `$2-$3`  |       函数返回值或表达式计算结果       |
|  `$a0-$a3`  |  `$4-$7`  |          函数调用时的参数传递          |
|  `$t0-$t7`  | `$8-$15`  |    临时寄存器，不保留函数调用间的值    |
|  `$s0-$s7`  | `$16-$23` |  保存寄存器，调用者需要负责保存与恢复  |
|  `$t8-$t9`  | `$24-$25` | 额外的临时寄存器，不保留函数调用间的值 |
|  `$k0-$k1`  | `$26-$27` |      内核保留寄存器，用于异常处理      |
|    `$gp`    |   `$28`   |            全局数据段的基址            |
|    `$sp`    |   `$29`   |         栈顶指针，指向当前栈顶         |
| `$fp`/`$s8` |   `$30`   |  栈帧指针，函数调用时用于维护局部变量  |
|    `$ra`    |   `$31`   |         存储函数调用的返回地址         |

#### 7.1.4 栈帧结构

![image-20241121193731096.png](https://s2.loli.net/2024/11/21/EZAPH5nbj96CSsk.png)

通过`$sp`维护栈帧，栈向下生长

栈的空间中，从上到下分别是：

- 调用者保存的寄存器
- 寄存器外的参数
- a0-a3

### 7.2 架构设计

与LLVM IR的架构类似，只是将其模块改为mips对应的模块

- Module→MipsModule
- GlobalVariable→MipsGlobalVariable
- Function→MipsFunction
- BasicBlock→MipsBasicBlock

**分析入口**，MipsBuilder中buildMips

```java
public void buildMips(Module irmodule){
    irmodule.buildMips();
}
```

### 7.3 转换过程

#### 7.3.1 全局变量

生成对应的.data段

```java
	// 只需要生成global
        if(initValue instanceof ConstStr){ // 字符串
            mipsBuilder.buildGlobalVariable(getName(), ((ConstStr)initValue).getString().replace("\\0A", "\\n"));
        } else if (initValue instanceof ZeroInitializer) { // 没有初始化
            mipsBuilder.buildGlobalVariable(getName(), initValue.getValueType().getSize());
        } else if(initValue instanceof ConstInt){

            mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.intType, new ArrayList<>(){{
                add(((ConstInt) initValue).getValue());
            }});
        } else if (initValue instanceof ConstChar) {
//            mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.charType, new ArrayList<>(){{
//                add(((ConstChar)initValue).getValue());
//            }});
            mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.intType, new ArrayList<>(){{
                add(((ConstChar)initValue).getValue()&0xff);
            }});
        } else if (initValue instanceof ConstArray){
            List<Constant> values = ((ConstArray) initValue).getValues(); // ConstArray中的值
            int initLen = values.size(); // 初始化参数的个数
            List<Integer> initArray = new ArrayList<>(); // 参数值列表
            if(((ArrayType)initValue.getValueType()).getValueType() instanceof IntType){
                for(int i = 0;i<initLen; i++){
                    initArray.add(((ConstInt)values.get(i)).getValue());
                }
                mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.intType, initArray);
            } else {
                for(int i = 0; i < initLen; i++){
                    initArray.add(((ConstChar)values.get(i)).getValue()&0xff);
                }
                // TODO : 暂时都当做4字节来做
                mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.intType, initArray);
            }
        }
```



#### 7.3.2 处理函数

处理参数、局部变量、基本块

##### **第一遍遍历构建函数时的任务**

在第一遍遍历函数时，目标更多是 **分析和准备函数的结构信息**，如：

- 确定局部变量的数量和存储需求。
- 计算每个变量或临时值的内存布局。
- 标记需要使用的寄存器、内存、以及栈帧的总大小。
- 收集目标代码生成时所需的控制流和数据流信息。

##### **函数调用时的栈帧操作**

- **现场保存（Caller Save）**： 调用者需要保存被调用函数可能会破坏的寄存器值（一般是临时寄存器）。
- **参数传递**： 按照目标平台的调用约定，将函数参数压入栈或存入指定寄存器。
- **返回地址保存**： 将返回地址存入栈（或通过特定寄存器），以确保函数返回时能继续执行。
- **局部变量的栈空间分配**： 分配函数的局部变量所需的栈空间。
- **现场恢复**： 函数调用结束后，恢复寄存器和堆栈状态。

#### 7.3.3 处理基本块

遍历指令，将每条指令生成其对应的目标代码

为指令的入口和出口生成跳转指令

| LLVM IR指令          | MIPS 映射                                                    |
| -------------------- | ------------------------------------------------------------ |
| alloca               | 调整`$sp`指针                                                |
| store                | `sw`指令                                                     |
| load                 | `lw`指令                                                     |
| add，sub，srem，sdiv | mips二元计算式                                               |
| icmp                 | 应用分为两种情况，zext \| br                                 |
| br                   | beq，bne，j                                                  |
| call                 | 保存寄存器，跳转到函数地址                                   |
| ret                  | 恢复寄存器，跳转到返回地址                                   |
| trunc                | i32->i8, 只需要and 0xff即可                                  |
| zext                 | i1->i32,即icmp指令，在icmp中进行处理；i8->i32，返回对应char变量即可 |
| phi                  | TODO                                                         |
| getelementptr        | 计算地址                                                     |