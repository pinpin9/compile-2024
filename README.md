2.实验期末考试
强烈建议在期末考前做一遍前两年期末考题，仅仅熟悉代码或许是不够的，尤其是对于平常借鉴较多的同学。因为期末考试需要你在短短的2h内实现一些挑战性的文法。

题干里一般会给出一些提示，请务必重视。

2.1 考试题型
新文法题(10)
一般是新加两条文法，部分测试点只涉及第一个文法，部分测试点只涉及第二个文法，其余测试点涉及所有文法。第二个文法一般来说具有一些难度。

竞速题(10)
不涉及新文法，直接提交课下代码即可。白送分。

其他的题
比如关于你自己代码设计的简答题等。白送分。

例如“对于如下代码，描述你的寄存器是如何进行运算优化的。如果你没有进行运算优化，请你结合理论知识简述一下可能采取的优化策略”。

2.2 2024年期末考试
题目
期末考试题目为新增两条文法

  UnaryOp → '+' | '−' | '!' | '++'

新增++符号，++UnaryExp的意思是，取值UnaryExp+1，与C语言的区别是，该文法下，变量的值不变

例如， b=++a，记为b=a+1，a不变

  Stmt → 'if' '(' BType Ident '=' InitVal ')' Stmt [ 'else' Stmt ]

例如，if(int b=a){
        stmt1;
      }else{
        stmt2;
      }
  表示为: 
  {
    int a = b;
    if(a){
      stmt1;
    }else{
      stmt2;
    }

  
2.2 2023年期末考试
题目
期末考试题目为增加两条文法：

 ForStmt → BType Ident '=' InitVal.

即在for循环头处声明并初始化变量的效果，例如for(int i = 1;;)。这里声明的变量作用域应当为这个for循环。

MulExp → MulExp ('*' | '/' | '%' | '**') UnaryExp。

我们把MulExp记为a，把UnaryExp记为b，则有规定：

算符**的效果是(a+b)**b
保证b一定是常数
分析
本次期末考试难度适中。

建议尝试移除"保证b一定是常数"这一条件，再进行解题，挑战一下。

对于第一条文法，请注意新声明的变量的作用域（例如建立符号表的时机，新符号加入符号表的时机）。

对于第二条文法，题目里b是常量，直接在编译器里取出来常量b的值，生成b条乘法中间代码即可。如果b不是常量，可以考虑手动生成一个for循环，结合对于 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt翻译为中间代码的实现，略加修改即可。

2.3 2022年期末考试
相对来说比较友好的题目。

新增文法：

VarDef → Ident = getint``(``)
修改文法：按位与 bitand

MulExp → UnaryExp | MulExp (*|/|%| bitand ) UnaryExp
说明：

变量定义时，只可能 是一个普通int变量定义，不会出现数组变量赋值，如int a[10] = getint();这种情况。
按位与的运算符号 & 被替换为了关键字 bitand 。特别注意其运算优先级与 乘除模 同级，与C/Java 不同。例如 a + b bitand c * d 的中间代码为
t1 = b bitand c
t2 = t1 * d
t3 = a + t2
常量表达式 ConstExp 的计算中不会出现按位与运算，例如 const int p = N bitand M 和 int a[N bitand M] （其中M和N为常量）这些是不合法的。
新增语法规则中，bitand为保留关键字，即测试样例不会出现 ident为bitand 的情况。
int i=getint(); 等价于 int i; 与 i=getint(); 两条语句。
a bitand b 运算效果等价于C/Java中的 a & b 。
提示：按位与运算可以用 and 指令实现，其格式与 add 等指令相同。
测试样例

int main()
{
	int i = getint(), j = getint();
	printf("%d", i bitand j);
	return 0;
}
样例输入
5
9
样例输出
1
样例说明
// i = 5(00000101), j = 9(00001001)
// 按位与结果为 1(00000001)
评分标准
C级样例
testfile1-3 不涉及新增文法
testfile4-5 仅增加了形如 int i=getint(); 内容
testfile6-7 仅增加了bitand内容
B级样例
testfile8 不涉及新增文法
testfile9 仅增加形如 int i=getint(); 内容
testfile10 仅增加了bitand内容
testfile11 增加全部两项内容
A级样例
testfile12 不涉及新增文法
testfile13 仅增加形如 int i=getint(); 内容
testfile14 仅增加了bitand内容
testfile15 增加全部两项内容
