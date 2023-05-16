package com.sdd.management.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.ext.Sortable;

import com.sdd.management.dao.MpaymentDAO;
import com.sdd.management.domain.Mpayment;


public class MpaymentListModel extends AbstractPagingListModel<Mpayment> implements Sortable<Mpayment> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int _size = -1;
	List<Mpayment> oList;  

	public MpaymentListModel(int startPageNumber, int pageSize, String filter, String orderby) {
		super(startPageNumber, pageSize, filter, orderby);
	}
	
	@Override
	protected List<Mpayment> getPageData(int itemStartNumber, int pageSize, String filter, String orderby) {		
		MpaymentDAO oDao = new MpaymentDAO();		
		try {
			oList = oDao.listPaging(itemStartNumber, pageSize, filter, orderby);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oList;
	}

	@Override
	public int getTotalSize(String filter) {
		MpaymentDAO oDao = new MpaymentDAO();	
		try {
			_size = oDao.pageCount(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _size;
	}

	@Override
	public void sort(Comparator<Mpayment> cmpr, boolean ascending) {		
		Collections.sort(oList, cmpr);
        fireEvent(ListDataEvent.CONTENTS_CHANGED, -1, -1);	
		
	}

	@Override
	public String getSortDirection(Comparator<Mpayment> cmpr) {
		return null;
	}
}
