package jp.dai1741.util;

/**
 * 
 * 何かの基準に従って対象を評価するクラス。
 * 抽象化しすぎて使いどころがない。
 * @author dai
 *
 * @param <E> 比較対象の型
 */
public interface Validator<E> {

	boolean validate(E e);
	
	String validateAndGetErrorString(E e);
}
