package com.amence.graphparse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeNodeTest {

    private static class ThenNode<T> {

        T value;
        ArrayList<ThenNode<T>> childs = new ArrayList<>();
        Set<ThenNode<T>> tmp = new HashSet<>();
        boolean marked;

        ThenNode<T> next;

        public ThenNode(T value) {
            this.value = value;
        }

        public void print(int deep) {
            printDeep(deep);
            System.out.print("THEN(" + value + ")");

            if (next != null) {
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
            System.out.println();
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
            System.out.println(")");
            if (next != null) {
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
        ThenNode<Integer> node7 = new ThenNode<>(7);
        node1.childs.add(node2);
        node1.childs.add(node6);
        node2.childs.add(node3);
        node2.childs.add(node4);
        node3.childs.add(node5);
        node4.childs.add(node5);
        node5.childs.add(node7);
        node6.childs.add(node7);
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
            WhenNode<Integer> whenNode = new WhenNode<>();
            whenNode.childs.addAll(node.childs);
            node.childs.clear();
            node.next = whenNode;
            ThenNode<Integer> next = findNext(whenNode.childs);
            whenNode.next = next;
            build(next);
            for (ThenNode<Integer> child : whenNode.childs) {
                build(child);
            }
        } else if (!node.childs.isEmpty()) {
            ThenNode<Integer> thenNode = node.childs.get(0);
            if (!thenNode.marked) {
                node.next = thenNode;
                build(thenNode);
            }
        }
    }

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
        for (ThenNode<Integer> node : nodes) {
            clean(node);
        }
        if (find != null) {
            find.marked = true;
        }
        return find;
    }

    private static void markTmp(ThenNode<Integer> node, ThenNode<Integer> mark) {
        node.tmp.add(mark);
        for (ThenNode<Integer> child : node.childs) {
            markTmp(child, mark);
        }
    }

    private static ThenNode<Integer> deepFind(ThenNode<Integer> node, List<ThenNode<Integer>> roots) {
        if (node.marked) {
            return null;
        }
        if (node.tmp.containsAll(roots)) {
            return node;
        }
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
