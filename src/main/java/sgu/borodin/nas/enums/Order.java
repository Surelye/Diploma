package sgu.borodin.nas.enums;

public enum Order {
    ASC, DESC;

    public static boolean isDescending(Order order) {
        return order == DESC;
    }

    public static boolean isAscending(Order order) {
        return order == ASC;
    }
}
