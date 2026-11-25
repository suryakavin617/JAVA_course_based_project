package com.polarlog.util;

import com.polarlog.model.Reportable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Generic methods and wildcards (Unit IV). */
public class CollectionUtil {
    private CollectionUtil() { }

    /** Generic method: returns a sorted copy of any collection of comparable items. */
    public static <T extends Comparable<? super T>> List<T> sortedCopy(Collection<T> items) {
        List<T> list = new ArrayList<>(items);
        Collections.sort(list);
        return list;
    }

    /** Generic method: returns the largest item, or null if the collection is empty. */
    public static <T extends Comparable<? super T>> T findMax(Collection<T> items) {
        if (items.isEmpty()) {
            return null;
        }
        return Collections.max(items);
    }

    /** Upper-bounded wildcard: accepts a collection of Reportable or any subclass. */
    public static void printReports(Collection<? extends Reportable> items) {
        for (Reportable item : items) {
            System.out.println(item.report());
        }
    }

    /** Unbounded wildcard: prints any collection as a numbered list. */
    public static void printNumbered(Collection<?> items) {
        int number = 1;
        for (Object item : items) {
            System.out.println("  " + number + ". " + item);
            number++;
        }
    }
}
