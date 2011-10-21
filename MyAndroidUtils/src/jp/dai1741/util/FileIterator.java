package jp.dai1741.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ファイル集合をイテレートするクラス。
 * 
 * @author dai
 * 
 */
public final class FileIterator implements Iterator<File> {

    /* final */ArrayList<File> mFiles;
    Comparator<File> mComparator;
    int mCurrentIndex = -1;

    protected FileIterator(ArrayList<File> files, Comparator<File> comparator) {
        mFiles = files;
        setComparatorWithFixedIndex(comparator);
    }

    protected FileIterator(ArrayList<File> files, File currentFile,
            Comparator<File> comparator) {
        mFiles = files;
        setComparator(comparator, currentFile);
    }


    /**
     * 
     * @param currentFile
     * @param ff
     * @param comparator
     * @throws NullPointerException
     *             if {@code currentFile} is null
     * @throws IllegalArgumentException
     *             if currentFile is not a file or not in a directory
     * @return このファイルを含むディレクトリ内のイテレーター
     */
    public static FileIterator inDirectoryOf(File currentFile, FileFilter ff,
            Comparator<File> comparator) {
        File dir = new File(currentFile.getParent());
        if (!currentFile.isFile() || !dir.isDirectory()) throw new IllegalArgumentException(
                "invalid file type: " + currentFile);

        return new FileIterator(new ArrayList<File>(Arrays.asList(dir.listFiles(ff))),
                currentFile, comparator);
    }


    /**
     * 
     * @param dir
     * @param ff
     * @param comparator
     * @throws NullPointerException
     *             if {@code dir} is null
     * @throws IllegalArgumentException
     *             if {@code dir} is not a directory
     * @return このディレクトリ内のイテレーター
     */
    /**
     * @return
     */
    public static FileIterator in(File dir, FileFilter ff, Comparator<File> comparator) {
        if (!dir.isDirectory()) throw new IllegalArgumentException(
                "given file doesn't represent a directory: " + dir);

        return new FileIterator(new ArrayList<File>(Arrays.asList(dir.listFiles(ff))),
                comparator);
    }

    /**
     * @param files
     *            non-null
     * @param comparator
     * @return
     */
    public static FileIterator of(List<File> files, Comparator<File> comparator) {
        if (files == null) {
            throw new NullPointerException("files must be specified");
        }
        return new FileIterator(new ArrayList<File>(files), comparator);
    }


    public Comparator<File> getComparator() {
        return mComparator;
    }

    public List<File> getFiles() {
        return Collections.unmodifiableList(mFiles);
    }


    /**
     * 比較関数をセットする。
     * 同時にソートもする。
     * 現在参照中のカーソルの位置を保持する。
     * 
     * @param comparator
     */
    public void setComparator(Comparator<File> comparator) {
        setComparator(comparator, mCurrentIndex >= 0 ? mFiles.get(mCurrentIndex) : null);
    }

    /**
     * 比較関数をセットする。
     * 同時にソートもする。
     * 
     * @param comparator
     *            null可
     * @param currentFile
     *            null可
     */
    public void setComparator(Comparator<File> comparator, File currentFile) {
        mComparator = comparator;
        ensurePosition(currentFile);
    }

    /**
     * @param currentFile
     *            nullなら先頭に、要素内にないなら適宜追加する
     */
    private void ensurePosition(File currentFile) {
        Collections.sort(mFiles, mComparator);
        if (currentFile == null) {
            mCurrentIndex = -1;
            return;
        }
        mCurrentIndex = Collections.binarySearch(mFiles, currentFile, mComparator);

        if (mCurrentIndex < 0) {
            mFiles.add(-mCurrentIndex - 1, currentFile);
        }

    }

    public void setComparatorWithFixedIndex(Comparator<File> comparator) {
        mComparator = comparator;
        Collections.sort(mFiles, mComparator);
    }

    public boolean hasNext() {
        return mCurrentIndex + 1 < mFiles.size();
    }

    public File next() {
        if (!hasNext()) {
            throw new NoSuchElementException("index: " + (mCurrentIndex + 1));
        }

        return mFiles.get(++mCurrentIndex);
    }

    public boolean hasPrevious() {
        return mCurrentIndex > 0;
    }

    /**
     * 直前のファイルを返す。 {@link ListIterator#previous()}と挙動が違います。
     * （it.next()==it.previous()にならない）
     * 注意。
     * 
     * @throws NoSuchElementException
     *             if !this.hasPrevious()
     * @return 1つ前の要素
     */
    public File previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException("index: " + (mCurrentIndex - 1));
        }
        return mFiles.get(--mCurrentIndex);
    }

    /**
     * @return このイテレーターが何も指していないときにfalse、そうでなければtrue。
     *         何も指していないのは、
     *         一度も {@code this.next()} を呼び出していないとき、
     *         または {@code this.moveToFirst()} の直後、
     *         または {@code this.moveToLast()} の直後、
     *         または先頭の要素を指しているときに {@code this.remove()} を呼び出した直後
     */
    public boolean hasCurrent() {
        return 0 <= mCurrentIndex && mCurrentIndex < mFiles.size();
    }

    /**
     * 現在のファイルを返す。
     * 
     * @throws NoSuchElementException
     *             if !this.hasCurrent()
     * @return 現在ポインタが指しているもの
     */
    public File current() {
        if (mCurrentIndex < 0 || mFiles.size() <= mCurrentIndex) {
            throw new NoSuchElementException("index: " + mCurrentIndex);
        }
        return mFiles.get(mCurrentIndex);
    }

    /**
     * カーソルを先頭に移動する。
     * このメソッドを呼んだ直後に{@code this.next()}を使用すると、先頭のファイルを取得する。
     */
    public void moveToFirst() {
        mCurrentIndex = -1;
    }

    public void moveToLast() {
        mCurrentIndex = mFiles.size();
    }

    /**
     * @throws IllegalStateException
     *             このイテレーターが何も指していないとき
     */
    public void remove() {
        if (mCurrentIndex < 0 || mFiles.size() <= mCurrentIndex) {
            throw new IllegalStateException("index: " + mCurrentIndex);
        }
        mFiles.remove(mCurrentIndex--);
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentIndex(int index) {
        mCurrentIndex = index;
    }

    public int size() {
        return mFiles.size();
    }

    /**
     * 比較順序集
     */
    public static enum FileComparators implements Comparator<File> {
        NATURAL {
            public int compare(File file1, File file2) {
                return file1.compareTo(file2);
            }
        },
        FILENAME {
            public int compare(File file1, File file2) {
                return file1.getName().compareTo(file2.getName());
            }
        },
        FILETYPE {
            public int compare(File file1, File file2) {
                return getExtension(file1).compareTo(getExtension(file2));
            }
        },
        FILESIZE {
            public int compare(File file1, File file2) {
                long ret = file1.length() - file2.length();
                return ret > 0 ? 1 : ret < 0 ? -1 : 0;
            }
        },
        DATE {
            public int compare(File file1, File file2) {
                long ret = file1.lastModified() - file2.lastModified();
                return ret > 0 ? 1 : ret < 0 ? -1 : 0;
            }
        };

        private static final Pattern EXTENSION_PATTERN = Pattern
                .compile("\\.[a-zA-Z0-9]+$");

        private static String getExtension(File file) {
            Matcher m = EXTENSION_PATTERN.matcher(file.getName());
            return m.find() ? m.group() : "";
        }
    }

}
