package cn.esoule.distributed.terminal.util.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  实现 java.util.Iterator<? extends java.util.Iterator<V>> 遍历
 * 
 * @author caoxin
 * @param <V> 
 */
public class IteratorIterator<V> implements Iterator<V> {

    /**
     * 1st Level iterator
     */
    private Iterator<? extends Iterable<V>> firstLevelIterator;
    /**
     * 2nd level iterator
     */
    private Iterator<V> secondLevelIterator;

    public IteratorIterator(Iterable<? extends Iterable<V>> itit) {
        this.firstLevelIterator = itit.iterator();
    }

    @Override
    public boolean hasNext() {
        if (secondLevelIterator != null && secondLevelIterator.hasNext()) {
            return true;
        }
        while (firstLevelIterator.hasNext()) {
            Iterable<V> iterable = firstLevelIterator.next();
            if (iterable != null) {
                secondLevelIterator = iterable.iterator();
                if (secondLevelIterator.hasNext()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V next() {
        if (secondLevelIterator == null || !secondLevelIterator.hasNext()) {
            throw new NoSuchElementException();
        }
        return secondLevelIterator.next();
    }

    /**
     * 不支持操作
     * 
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("This operation is not supported.");
    }
}