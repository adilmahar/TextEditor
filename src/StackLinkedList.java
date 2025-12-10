public class StackLinkedList<T> {
    private Node<T> top;
    private int size;    // maximum capacity
    private int items;   // current number of items

    public StackLinkedList(int size) {
        this.size = size;
        top = null;
        items = 0;
    }

    public void push(T data) {
        // If stack is full, remove the bottom element
        if (items >= size) {
            removeBottom();
        }

        Node<T> newNode = new Node<>(data);
        newNode.setNext(top);
        top = newNode;
        items++;
    }

    private void removeBottom() {
        if (top == null) return;

        if (top.getNext() == null) {
            // Only one element
            top = null;
        } else {
            Node<T> temp = top;
            while (temp.getNext().getNext() != null) {
                temp = temp.getNext();
            }
            // Remove last node
            temp.setNext(null);
        }
        items--;
    }

    public T pop() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        T data = top.getData();
        top = top.getNext();
        items--;
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return top.getData();
    }

    public boolean isEmpty() {
        return top == null;
    }

    public void flush() {
        top = null;
        items = 0;
    }

}