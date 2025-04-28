package com.bookstore.Utils;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;

import java.util.Arrays;

public class CheckCanChangeOrderStatus {
    public static boolean check(String role, String before, String after) {
        System.err.println("ROLE " + role);
        if (before == null ||
            after == null ||
            !Arrays.stream(OrderStatus.values()).anyMatch(e -> e.name().equals(before)) ||
            !Arrays.stream(OrderStatus.values()).anyMatch(e -> e.name().equals(after))) {
            return false;
        }
        if (role.equals("ADMIN") || role.equals("EMPLOYEE") || role.equals("SHIPPER") || role.equals("USER")) {
            if (before.equals(OrderStatus.PENDING.name()) && after.equals(OrderStatus.CANCELLED.name())) {
                return true;
            }
            if (before.equals(OrderStatus.IN_PREPARATION.name()) && after.equals(OrderStatus.CANCELLATION_REQUESTED.name())) {
                return true;
            }
        }
        if (role.equals("ADMIN") || role.equals("EMPLOYEE")) {
            if (before.equals(OrderStatus.PENDING.name()) && after.equals(OrderStatus.REJECTED.name())) {
                return true;
            }
            if (before.equals(OrderStatus.IN_PREPARATION.name()) && after.equals(OrderStatus.REJECTED.name())) {
                return true;
            }
            if (before.equals(OrderStatus.PENDING.name()) && after.equals(OrderStatus.IN_PREPARATION.name())) {
                return true;
            }
            if (before.equals(OrderStatus.READY_TO_SHIP.name()) && after.equals(OrderStatus.REJECTED.name())) {
                return true;
            }
            if (before.equals(OrderStatus.IN_PREPARATION.name()) && after.equals(OrderStatus.READY_TO_SHIP.name())) {
                return true;
            }
            if (before.equals(OrderStatus.CANCELLATION_REQUESTED.name()) && after.equals(OrderStatus.CANCELLED.name())) {
                return true;
            }
        }
        if (role.equals("ADMIN") || role.equals("SHIPPER")) {
            if (before.equals(OrderStatus.READY_TO_SHIP.name()) && after.equals(OrderStatus.DELIVERING.name())) {
                return true;
            }
            if (before.equals(OrderStatus.DELIVERING.name()) && after.equals(OrderStatus.DELIVERED.name())) {
                return true;
            }
        }
        return false;
    }
}
