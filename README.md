## 2.实验期末考试

时间：2h，实现扩展的文法

### 2.1 考试题型

**新文法题(10)**
一般是新加两条文法，部分测试点只涉及第一个文法，部分测试点只涉及第二个文法，其余测试点涉及所有文法。第二个文法一般来说具有一些难度。

**竞速题(10)**
不涉及新文法，直接提交课下代码即可。白送分。

**其他的题（5）**
比如关于你自己代码设计的简答题等。白送分。

例如“对于如下代码，描述你的编译器是如何进行优化的。如果你没有进行运算优化，请你结合理论知识简述一下可能采取的优化策略”。

"对于数组的使用，你的编译器是如何取值的"

### 2.2 2024年

#### 期中考试

题目

新增两条文法，实现词法分析和语法分析

1.增加+=，-=，*=，/=符号
```
Stmt → LVal ('=' | '+=' | '-=' | '*=' | '/=') Exp ';'
```

2.过去半学期，有点儿记不清具体文法了，大概就是实现三元式 :)

```
Stmt →  Cond '?' Exp ':' Exp ';'
```

ps：因为原文法中已经有```Stmt -> Exp ';' ```

first(Cond) 和 first(Exp) 集合交集不为空，所以需要回溯（PS：预读无法处理所有情况）


#### 期末考试

题目
期末考试题目为新增两条文法

```
  UnaryOp → '+' | '−' | '!' | '++'
```

新增++符号，++UnaryExp的意思是，取值UnaryExp+1，与C语言的区别是，该文法下，变量的值不变

例如， b=++a，记为b=a+1，a不变

```
  Stmt → 'if' '(' BType Ident '=' InitVal ')' Stmt [ 'else' Stmt ]
```

例如，

```
if(int b=a){
    stmt1;
}else{
    stmt2;
}
```

  表示为: 

```
{
    int a = b;
    if(a){
    	stmt1;
    }else{
    	stmt2;
    }
}
```





