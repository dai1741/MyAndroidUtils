package jp.dai1741.android.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.MatrixCursor;

/**
 * カラム名に別名を定義できる {@link Cursor}。
 * 普通に{@link MatrixCursor}使ったほうがいいか・・・動作速度はこちらのほうが速いのですが。
 * 
 * select hoge as fuga なんてできるのか・・・ますますいらない子
 * @author dai
 *
 */
public class AliasColumnCursor extends CursorWrapper {

	public static final Map<String, String> DEFAULT_ALIAS_MAP = Collections.emptyMap();
	
	Map<String, String> mAliasMap;
	Map<String, String> mInverseAliasMap;
	
	public AliasColumnCursor(Cursor cursor) {
		this(cursor, DEFAULT_ALIAS_MAP);
	}
	
	public AliasColumnCursor(Cursor cursor, Map<String, String> aliasMap) {
		super(cursor);
		setAliasMap(aliasMap);
	}
	
	/**
	 * 別名定義マップを設定。
	 * @param aliasMap
	 */
	public void setAliasMap(Map<String, String> aliasMap) {
		mAliasMap = new HashMap<String, String>(aliasMap);
		mInverseAliasMap = createInverseMap(aliasMap);
	}
	
	/**
	 * 逆順にしたマップを作成。
	 * 同じ値が複数存在したときの動作は保証されない。
	 * 本当はBiMapがほしい。
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return
	 */
	private <K, V> Map<V, K> createInverseMap(Map<K, V> map) {
		if (map == DEFAULT_ALIAS_MAP) {
			return Collections.emptyMap();
		}
		Map<V, K> inverseMap = new HashMap<V, K>(map.size(), 1f);
		for (Map.Entry<K, V> entry : map.entrySet()) {
			inverseMap.put(entry.getValue(), entry.getKey());
		}
		return inverseMap;
	}

	@Override
	public int getColumnIndex(String columnName) {
		return super.getColumnIndex(resolveInverseAlias(columnName));
	}

	@Override
	public int getColumnIndexOrThrow(String columnName)
			throws IllegalArgumentException {
		return super.getColumnIndexOrThrow(resolveInverseAlias(columnName));
	}

	@Override
	public String getColumnName(int columnIndex) {
		return resolveAlias(super.getColumnName(columnIndex));
	}

	@Override
	public String[] getColumnNames() {
		String[] ret = super.getColumnNames();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = resolveAlias(ret[i]);
		}
		return ret;
	}
	
	private String resolveAlias(String orig) {
		String alias = mAliasMap.get(orig);
		return alias == null ? orig : alias;
	}
	
	private String resolveInverseAlias(String alias) {
		String orig = mInverseAliasMap.get(alias);
		return orig == null ? alias : orig;
	}
	

}
