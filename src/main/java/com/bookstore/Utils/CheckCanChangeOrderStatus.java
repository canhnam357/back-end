package com.bookstore.Utils;

import com.bookstore.Constant.OrderStatus;

public class CheckCanChangeOrderStatus {
    public static boolean check(String before, String after) {
        if (before.equals(OrderStatus.PENDING.name()) && (after.equals(OrderStatus.IN_PREPARATION.name()) || after.equals(OrderStatus.CANCELLED.name()) || after.equals(OrderStatus.REJECTED.name()))) {
            return true;
        }
        if (before.equals(OrderStatus.IN_PREPARATION.name()) && (after.equals(OrderStatus.CANCELLED.name()) || after.equals(OrderStatus.READY_TO_SHIP.name()))) {
            return true;
        }
        if (before.equals(OrderStatus.READY_TO_SHIP.name()) && (after.equals(OrderStatus.DELIVERING.name()) || after.equals(OrderStatus.REJECTED.name()))) {
            return true;
        }
        if (before.equals(OrderStatus.DELIVERING.name()) && (after.equals(OrderStatus.DELIVERED.name()) || after.equals(OrderStatus.FAILED_DELIVERY.name()))) {
            return true;
        }
        return before.equals(OrderStatus.FAILED_DELIVERY.name()) && after.equals(OrderStatus.RETURNED.name());
    }
}
