/**
 * Created by Tim Zhang on 2019/1/7
 */
public class BinarySearchTree {

    public static void main(String[] args) {
        BinarySearchTree binarySearchTree = new BinarySearchTree();
        binarySearchTree.insert(1);
        binarySearchTree.insert(5);
        binarySearchTree.insert(4);
        binarySearchTree.insert(3);
        binarySearchTree.insert(2);
        binarySearchTree.preOrder(binarySearchTree.tree);

    }

    private Node tree;

    public Node findNode(int value) {
        Node node = tree;
        while(null != node) {
            if(value == node.data) {
                return node;
            }
            else if(value > node.data) {
                node = node.right;
            }
            else if(value < node.data) {
                node = node.left;
            }
        }
        return null;
    }

    public void insert(int value) {
        if(null == tree) {
            tree = new Node(value);
            return;
        }
        Node node = tree;
        while(null != node) {
            if(value > node.data){
                if(null == node.right) {
                    node.right = new Node(value);
                    return;
                }
                node = node.right;
            }
            else {
                if(null == node.left) {
                    node.left = new Node(value);
                    return;
                }
                node = node.left;
            }
        }
    }

    public void delete(int value) {
        Node node = tree;
        Node parentNode = null;
        //查找待删除的节点，与父节点
        while(null != node && node.data != value) {
            parentNode = node;
            if (value > node.data) node = node.right;
            else node = node.left;
        }
        //没有找到要删除的节点
        if(null == node) {
            return;
        }
        //待删除节点的左右节点都为null
        if(node.left == null && node.right == null) {
            if(parentNode == null) {
                tree = null;
            }
            else {
                if(parentNode.left == node) {
                    parentNode.left = null;
                }
                if(parentNode.right == node) {
                    parentNode.right = null;
                }
            }
        }
        //待删除节点左节点为null，右节点有值
        else if(node.left != null && node.right == null) {
            //待删除节点为父节点左节点
            if(parentNode.left == node) {
                parentNode.left = node.left;
            }
            else {
                parentNode.right = node.left;
            }
        }
        else if(node.right != null && node.left == null) {
            if(parentNode.left == node) {
                parentNode.left = node.right;
            }
            else {
                parentNode.right = node.right;
            }
        }
        //待删除节点左右都不为null
        else {
            //待删除节点找到右子树最小的节点
            Node minNode = node.right;
            Node minParentNode = node;
            while(minNode.left != null) {
                minParentNode = minNode;
                minNode = minNode.left;
            }
            node.data = minNode.data;
            if(minNode.right != null) {
                minParentNode.left = minNode.right;
            }
            else {
                minParentNode.left = null;
            }
        }
    }


    public static class Node {
        private int data;
        private Node left;
        private Node right;
        public Node(int value) {
            this.data = value;
        }
    }

    public void preOrder(Node node) {
        if(node == null) {
            return;
        }
        preOrder(node.left);
        System.out.println(node.data);
        preOrder(node.right);
    }
}

