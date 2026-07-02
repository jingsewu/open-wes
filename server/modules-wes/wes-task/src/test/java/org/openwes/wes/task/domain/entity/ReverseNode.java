package org.openwes.wes.task.domain.entity;

public class ReverseNode {

    public static void main(String[] args) {
        Node node = createNode(3);
        Node reverseNode = reverseNode(node);
        System.out.println(reverseNode);
    }

    private static Node createNode(int num) {
        Node node = new Node("node-0");
        Node curr = node;
        for (int i = 0; i < num; i++) {
            curr.next = new Node("node-" + (i + 1));
            curr = curr.next;
        }
        return node;
    }

    public static Node reverseNode(Node head) {

        if (head == null) {
            return null;
        }

        Node prev = null;
        Node curr = head;

        while (curr != null) {
            Node tmp = curr.next;

            curr.next = prev;
            prev = curr;
            curr = tmp;
        }

        return prev;
    }

    public static class Node {
        String name;
        private Node next;

        public Node(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
