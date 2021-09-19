[toc]

# 1. 什么是Morris遍历(莫里斯遍历)

普通的二叉树遍历大多需要栈在存储节点,最差的情况下需要存储整棵树,Morris遍历则是将空间复杂度降到了O(1)级别。Morris遍历用到了“<u>线索二叉树</u>”的概念，其实就是**利用了叶子节点的左右空指针来存储某种遍历前驱节点或者后继节点**。因此没有使用额外的空间。

> morris遍历的特点: 无栈;空间复杂度为O(1);无递归(用while代替了)
>
> 来源: https://blog.csdn.net/danmo_wuhen/article/details/104339630
>
> https://blog.csdn.net/woshinannan741/article/details/52839946



# 2. Morris遍历的算法思想

假设当前节点为cur，并且开始时赋值为根节点root。

1. 判断cur节点是否为空

2. 如果不为空

   1）如果cur没有左孩子，cur向右更新，即（cur = cur.right）

   2）如果cur有左孩子，则从左子树找到最右侧节点pre

   - 如果pre的右孩子为空，则将右孩子指向cur。pre.right = cur
   - 如果pre的右孩子为cur，则将其指向为空。pre.right = null。（还原树结构）

3. cur为空时，停止遍历

   > 算法核心是 : 把右叶子节点指向上级(或者上面节点),这样就不用回归(达到了栈的效果),此时树就变成了一个无向环形图,把右叶子节点取消后继节点,树就恢复了,此时也到了输出条件,如此往复直到遍历整棵树
   >
   > 来源: https://blog.csdn.net/danmo_wuhen/article/details/104339630
   >
   > https://www.cnblogs.com/AnnieKim/archive/2013/06/15/morristraversal.html
   >
   > https://blog.csdn.net/woshinannan741/article/details/52839946

   
   
   

<b style='color:red'>以后遇到了再详细看吧</b>