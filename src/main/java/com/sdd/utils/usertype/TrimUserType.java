package com.sdd.utils.usertype;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

/**
 * 
 * based on www.hibernate.org/388.html
 */

public class TrimUserType implements UserType {

	/**
	 * 
	 * default constructor
	 */

	public TrimUserType() {
	}

	/**
	 * 
	 * @see org.hibernate.usertype.UserType#sqlTypes()
	 */

	public int[] sqlTypes() {
		return new int[] { Types.CHAR };
	}

	/**
	 * 
	 * @see org.hibernate.usertype.UserType#returnedClass()
	 */

	
	@SuppressWarnings("rawtypes")
	public Class returnedClass() {
		return String.class;
	}

	/**
	 * 
	 * @see org.hibernate.usertype.UserType#equals(java.lang.Object,
	 *      java.lang.Object)
	 */

	public boolean equals(Object x, Object y) {
		return (x == y) || (x != null && y != null && (x.equals(y)));
	}


	public Object deepCopy(Object o) {
		if (o == null) {
			return null;
		}
		return new String(((String) o));
	}

	/**
	 * 
	 * @see org.hibernate.usertype.UserType#isMutable()
	 */

	public boolean isMutable() {
		return false;
	}

	/**
	 * 
	 * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable,
	 *      java.lang.Object)
	 */

	public Object assemble(Serializable cached, Object owner) {
		return cached;
	}

	/**
	 * 
	 * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
	 */

	public Serializable disassemble(Object value) {
		return (Serializable) value;
	}

	/**
	 * 
	 * @see org.hibernate.usertype.UserType#replace(java.lang.Object,
	 *      java.lang.Object, java.lang.Object)
	 */

	public Object replace(Object original, Object target, Object owner) {
		return original;
	}

	/**
	 * 
	 * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
	 */

	public int hashCode(Object x) {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names,
			SharedSessionContractImplementor arg2, Object arg3) throws HibernateException,
			SQLException {
		String s = rs.getString(names[0]);
		if (s != null) s = s.trim();
		return s;
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object obj, int index,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		String s = (String) obj;
		if (s != null) s = s.trim();
		StandardBasicTypes.STRING.nullSafeSet(st, s, index, session);
		
	}

}