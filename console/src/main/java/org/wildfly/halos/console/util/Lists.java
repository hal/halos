package org.wildfly.halos.console.util;

import java.math.RoundingMode;
import java.util.AbstractList;
import java.util.List;

import com.google.common.math.IntMath;

import static org.wildfly.halos.console.util.Preconditions.checkArgument;
import static org.wildfly.halos.console.util.Preconditions.checkElementIndex;
import static org.wildfly.halos.console.util.Preconditions.checkNotNull;

public class Lists {

    public <T> List<List<T>> partition(List<T> list, int size) {
        checkNotNull(list);
        checkArgument(size > 0);
        return new Partition<>(list, size);
    }

    private static class Partition<T> extends AbstractList<List<T>> {

        final List<T> list;
        final int size;

        Partition(List<T> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<T> get(int index) {
            checkElementIndex(index, size());
            int start = index * size;
            int end = Math.min(start + size, list.size());
            return list.subList(start, end);
        }

        @Override
        public int size() {
            return IntMath.divide(list.size(), size, RoundingMode.CEILING);
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }
    }
}
