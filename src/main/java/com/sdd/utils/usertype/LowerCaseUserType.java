package com.sdd.utils.usertype;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

public class LowerCaseUserType implements UserType {
	private static final int[] TYPES = { Types.VARCHAR };

	public int[] sqlTypes() {
		return TYPES;
	}

	@SuppressWarnings("rawtypes")
	public Class returnedClass() {
		return String.class;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		}
		if (null == x || null == y) {
			return false;
		}
		return new EqualsBuilder().append(x, y).isEquals();
	}

	public int hashCode(Object o) throws HibernateException {
		return new HashCodeBuilder().append(o).toHashCode();
	}
	
	
	
	public Object deepCopy(Object o) throws HibernateException {
		if (null == o) {
			return null;
		}
		return new String(o.toString());
	}

	public boolean isMutable() {
		return false;
	}

	public Serializable disassemble(Object o) throws HibernateException {
		return (String) o;
	}

	public Object assemble(Serializable serializable, Object o)
			throws HibernateException {
		return serializable;
	}

	public Object replace(Object o, Object arg1, Object arg2)
			throws HibernateException {
		return o;
	}
	
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names,
			SharedSessionContractImplementor session, Object obj) throws HibernateException,
			SQLException {
		String s = rs.getString(names[0]);
		if (s != null) s = s.toLowerCase();
		return s;
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object obj, int index,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		String s = (String) obj;
		if (s != null) s = s.toLowerCase();
		StandardBasicTypes.STRING.nullSafeSet(st, s, index, session);
	}	

	
}
