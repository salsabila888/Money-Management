package com.sdd.caption.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.ext.Sortable;

import com.sdd.caption.dao.MexpensesDAO;
import com.sdd.caption.domain.Mexpenses;

public class MexpensesListModel extends AbstractPagingListModel<Mexpenses> implements Sortable<Mexpenses> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int _size = -1;
	List<Mexpenses> oList;  

	public MexpensesListModel(int startPageNumber, int pageSize, String filter, String orderby) {
		super(startPageNumber, pageSize, filter, orderby);
	}
	
	@Override
	protected List<Mexpenses> getPageData(int itemStartNumber, int pageSize, String filter, String orderby) {		
		MexpensesDAO oDao = new MexpensesDAO();		
		try {
			oList = oDao.listPaging(itemStartNumber, pageSize, filter, orderby);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oList;
	}

	@Override
	public int getTotalSize(String filter) {
		MexpensesDAO oDao = new MexpensesDAO();	
		try {
			_size = oDao.pageCount(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _size;
	}

	@Override
	public void sort(Comparator<Mexpenses> cmpr, boolean ascending) {		
		Collections.sort(oList, cmpr);
        fireEvent(ListDataEvent.CONTENTS_CHANGED, -1, -1);	
		
	}

	@Override
	public String getSortDirection(Comparator<Mexpenses> cmpr) {
		return null;
	}
}
