package com.amence.graphparse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeNodeTest {

    private static class ThenNode<T> {

        T value;
        ArrayList<ThenNode<T>> childs = new ArrayList<>();

        Set<ThenNode<T>> tmp = new HashSet<>(); // 用于记录分叉的起始点
        boolean marked;

        ThenNode<T> next;

        public ThenNode(T value) {
            this.value = value;
        }

        public void print(int deep) {
            printDeep(deep);
            System.out.print("THEN(" + value + ")");

            if (next != null) {
                System.out.println();
                next.print(deep);
            }
        }
    }

    private static class WhenNode<T> extends ThenNode<T> {

        public WhenNode() {
            super(null);
        }

        @Override
        public void print(int deep) {
            printDeep(deep);
            System.out.print("WHEN (\n");
            for (ThenNode<T> child : childs) {
                printDeep(deep + 1);
                System.out.print("THEN(\n");
                child.print(deep + 2);
                System.out.println();
                printDeep(deep + 1);
                System.out.print(")\n");
            }
            printDeep(deep);
            System.out.print(")");
            if (next != null) {
                System.out.println();
                next.print(deep);
            }
        }
    }

    public static void main(String[] args) {
        // 构建数据
        ThenNode<Integer> node1 = new ThenNode<>(1);
        ThenNode<Integer> node2 = new ThenNode<>(2);
        ThenNode<Integer> node3 = new ThenNode<>(3);
        ThenNode<Integer> node4 = new ThenNode<>(4);
        ThenNode<Integer> node5 = new ThenNode<>(5);
        ThenNode<Integer> node6 = new ThenNode<>(6);
//        ThenNode<Integer> node7 = new ThenNode<>(7);
//        ThenNode<Integer> node8 = new ThenNode<>(8);
//        ThenNode<Integer> node9 = new ThenNode<>(9);
//        ThenNode<Integer> node10 = new ThenNode<>(10);

        node1.childs.add(node2);
        node1.childs.add(node6);

        node2.childs.add(node3);
        node2.childs.add(node4);


        node3.childs.add(node5);
        node4.childs.add(node5);



        build(node1);
        System.out.println("THEN(");
        node1.print(1);
        System.out.println("\n)");
    }

    static void printDeep(int deep) {
        for (int i = 0; i < deep; i++) {
            System.out.print("\t");
        }
    }

    private static void build(ThenNode<Integer> node) {
        if (node == null || node.marked) {
            return;
        }
        node.marked = true;
        if (node.childs.size() > 1) {
            // 当前节点存在分叉，单独处理子节点
            WhenNode<Integer> whenNode = new WhenNode<>();
            whenNode.childs.addAll(node.childs);
            node.childs.clear();
            // 拼接下一个表达式
            node.next = whenNode;
            // 获取分叉之后的聚合点
            ThenNode<Integer> next = findNext(whenNode.childs);
            // 聚合点作为分叉之后的表达式拼接
            whenNode.next = next;
            // 聚合点作为新的开始
            build(next);
            for (ThenNode<Integer> child : whenNode.childs) {
                build(child);
            }
        } else if (!node.childs.isEmpty()) {
            // 当前节点有且只有一个子节点，开始遍历子节点，子节点与父节点连接的表达式为Then
            ThenNode<Integer> thenNode = node.childs.get(0);
            if (!thenNode.marked) {
                node.next = thenNode;
                build(thenNode);
            }
        }
    }

    /**
     * @param nodes 分叉的首节点，在每个经过的节点记录，分叉点信息
     * @return
     */
    private static ThenNode<Integer> findNext(List<ThenNode<Integer>> nodes) {
        // 从多个分支出发，标记沿途的所有点
        for (ThenNode<Integer> node : nodes) {
            for (ThenNode<Integer> child : node.childs) {
                markTmp(child, node);
            }
        }

        // 找到汇合点
        ThenNode<Integer> find = null;
        start:
        for (ThenNode<Integer> node : nodes) {
            for (ThenNode<Integer> child : node.childs) {
                find = deepFind(child, nodes);
                if (find != null) {
                    break start;
                }
            }
        }
        // 找到了聚合点，就要清除每个点对应的分叉点
        for (ThenNode<Integer> node : nodes) {
            clean(node);
        }
        return find;
    }

    /**
     * 全部标记分叉节点
     * @param node 子节点
     * @param mark 父节点
     */
    private static void markTmp(ThenNode<Integer> node, ThenNode<Integer> mark) {
        // 被父节点遍历过？
        node.tmp.add(mark);

        // 这里在标记父节点遍历过所有的祖孙节点
        for (ThenNode<Integer> child : node.childs) {
            markTmp(child, mark);
        }
    }

    /**
     * 寻找分叉多个首节点 的交汇点，判断依据：1.子节点已经记录了，所有经过他的分叉节点
     * @param node 经过的后续子节点
     * @param roots 分叉的首节点list
     * @return
     */
    private static ThenNode<Integer> deepFind(ThenNode<Integer> node, List<ThenNode<Integer>> roots) {
        // 如果该节点已经被处理过，已经输出编入了表达式 就不需要再处理了。在每次build的时候就会标记
        if (node.marked) {
            return null;
        }

        // 如果当前节点记录的所有分叉节点和 起始的分叉节点list 完全相同
        if (node.tmp.containsAll(roots)) {
            return node;
        }

        // 也需要遍历node的子节点，后面会有很多子节点
        for (ThenNode<Integer> child : node.childs) {
            ThenNode<Integer> find = deepFind(child, roots);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

    private static void clean(ThenNode<Integer> node) {
        if (node.marked) {
            return;
        }
        node.tmp.clear();
        for (ThenNode<Integer> child : node.childs) {
            clean(child);
        }
    }
}
