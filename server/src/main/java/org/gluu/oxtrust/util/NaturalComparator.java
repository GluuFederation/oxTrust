package org.gluu.oxtrust.util;

import java.util.Comparator;

class NaturalComparator<E extends Comparable<E>> implements Comparator<E> {

    @Override
    public int compare(E lhs, E rhs) {
        return lhs.compareTo(rhs);
    }
}