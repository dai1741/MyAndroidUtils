package jp.dai1741.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Apache Commonsの同名のクラスと同じような連鎖比較クラス。
 * <p>
 * 1つ以上の比較関数を内包し、先頭の比較関数が大小判定できなかった場合、 後続の比較関数が連鎖的に使用されて大小を判定する。
 * </p>
 * 
 * <p>
 * 比較関数が1つも提供されない場合、このクラスはどの2つのオブジェクトも等しいと判定する。
 * </p>
 * 
 * <p>
 * 与えられる比較関数がcompare規約を守る場合、 このクラスのインスタンスは不変。
 * </p>
 * 
 * @author dai
 */
public final class ComparatorChain<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1077296719111657513L;

    final List<Comparator<? super T>> comparators;

    public ComparatorChain(Comparator<? super T>... comparators) {
        this(Arrays.asList(comparators));
    }

    public ComparatorChain(List<? extends Comparator<? super T>> comparators) {
        this.comparators = new ArrayList<Comparator<? super T>>(comparators);
    }

    public int compare(T object1, T object2) {
        for (Comparator<? super T> comparator : comparators) {
            int ret = comparator.compare(object1, object2);
            if (ret != 0) return ret;
        }
        return 0;
    }

    public List<Comparator<? super T>> getComparators() {
        return Collections.unmodifiableList(comparators);
    }

    @Override
    public int hashCode() {
        final int prime = 47;
        int result = 1;
        result = prime * result + ((comparators == null) ? 0 : comparators.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ComparatorChain<?> other = (ComparatorChain<?>) obj;
        if (comparators == null) {
            if (other.comparators != null) return false;
        }
        else if (!comparators.equals(other.comparators)) return false;
        return true;
    }


}
