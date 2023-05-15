/**
 * 
 */
package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.ImmutableFields;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Table;
import org.zkoss.zhtml.Td;
import org.zkoss.zhtml.Tr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.domain.Ttokenserial;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;

public class TokenScanBatchVm {

	private TtokenitemDAO TtokenitemDao = new TtokenitemDAO();

	private Torder obj;
	private Ttokenserial objForm;
	private int total;
	private Integer outstanding;
	private String itemno;
	private String itemnoend;

	private List<Ttokenitem> objList = new ArrayList<>();
	private List<Ttokenitem> dataList = new ArrayList<>();
	private List<String> listData;

	@Wire
	private Window winItemBatch;
	@Wire
	private Textbox tbItem, tbItemend;
	@Wire
	private Button btnRegister, btnCheck, btnAdd;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	@Wire
	private Table table;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder obj,
			@ExecutionArgParam("outstanding") Integer outstanding,
			@ExecutionArgParam("listData") List<String> listDataManual) throws Exception {
		Selectors.wireComponents(view, this, false);
		this.obj = obj;
		this.outstanding = outstanding;
		this.listData = listDataManual;
		doReset();

		grid.setRowRenderer(new RowRenderer<Ttokenitem>() {

			@Override
			public void render(Row row, final Ttokenitem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getItemnoinject()));
				Button btn = new Button("Cancel");
				btn.setAutodisable("self");
				btn.setSclass("btn btn-danger btn-sm");
				btn.setStyle("border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important;");
				btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
								Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

									@Override
									public void onEvent(Event event) throws Exception {
										listData.remove(data.getItemno().trim());
										dataList.remove(data);

										refresh();
										BindUtils.postNotifyChange(null, null, TokenScanBatchVm.this, "outstanding");
									}
								});
					}
				});

				Div div = new Div();
				div.appendChild(btn);
				row.getChildren().add(div);
			}
		});
	}

	@Command
	@NotifyChange({ "total", "outstanding" })
	public void doSearch() {
		try {
			if (itemno == null || itemno.trim().length() == 0)
				Messagebox.show("Silahkan isi data nomor serial awal dengan data numerik", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			else if (!StringUtils.isNumeric(itemno.trim()))
				Messagebox.show("Silahkan isi data nomor serial awal dengan data numerik", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			else if (itemnoend == null || itemnoend.trim().length() == 0)
				Messagebox.show("Silahkan isi data nomor serial akhir dengan data numerik", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			else if (!StringUtils.isNumeric(itemnoend.trim()))
				Messagebox.show("Silahkan isi data nomor serial akhir dengan data numerik", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			else {
				objList = TtokenitemDao.listNativeByFilter(
						"cast(itemno as integer) between " + itemno.trim() + " and " + itemnoend.trim(),
						"Ttokenitempk");

				int inserted = 1;
				for (Ttokenitem data : objList) {
					if (!listData.contains(data.getItemno().trim())) {
						if (data.getStatus().equals(AppUtils.STATUS_SERIALNO_INJECTED)) {
							if (outstanding >= inserted) {
								dataList.add(data);
								listData.add(data.getItemno().trim());
								inserted++;
							}
						}
					}
				}
				System.out.println("INSERTED : " + inserted);
				refresh();
				if (total == 0) {
					Messagebox.show("Data tidak ditemukan", "Info", Messagebox.OK, Messagebox.INFORMATION);
				} else {
					tbItem.setReadonly(true);
					tbItemend.setReadonly(true);
					btnCheck.setDisabled(true);
					btnAdd.setDisabled(false);
					outstanding = outstanding - inserted + 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<>(dataList));
		total = dataList.size();
	}

	@Command
	@NotifyChange("*")
	public void doAdd() {
		if (total >= outstanding) {
			Messagebox.show(
					"Tidak bisa menambah data karna jumlah data yang dimasukan sudah setara atau melebihi outstanding",
					"Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			btnAdd.setDisabled(true);

			Tr tr = new Tr();
			Td td = new Td();
			td.setColspan(2);
			Textbox txtAwal = new Textbox();
			txtAwal.setPlaceholder("Entri Nomor Serial Awal");
			Label label = new Label(" ");
			Textbox txtAkhir = new Textbox();
			txtAkhir.setPlaceholder("Entri Nomor Serial Akhir");
			Label label2 = new Label(" ");
			Button btn = new Button("Check Data");
			btn.setAutodisable("self");
			btn.setSclass("btn btn-info btn-sm");
			btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					if (txtAwal == null || txtAwal.getValue().trim().length() == 0)
						Messagebox.show("Silahkan isi data nomor serial awal dengan data numerik", "Info",
								Messagebox.OK, Messagebox.INFORMATION);
					else if (txtAkhir == null || txtAkhir.getValue().trim().length() == 0)
						Messagebox.show("Silahkan isi data nomor serial akhir dengan data numerik", "Info",
								Messagebox.OK, Messagebox.INFORMATION);
					else {
						objList = TtokenitemDao.listNativeByFilter("cast(itemno as integer) between "
								+ txtAwal.getValue().trim() + " and " + txtAkhir.getValue().trim(), "Ttokenitempk");
						int inserted = 1;
						for (Ttokenitem data : objList) {
							if (!listData.contains(data.getItemno().trim())) {
								if (data.getStatus().equals(AppUtils.STATUS_SERIALNO_INJECTED)) {
									if (outstanding >= inserted) {
										dataList.add(data);
										listData.add(data.getItemno().trim());
										inserted++;
									}
								}
							}
						}

						if (inserted > 0) {
							txtAwal.setReadonly(true);
							txtAkhir.setReadonly(true);
							btn.setDisabled(true);
							btnAdd.setDisabled(false);
							outstanding = outstanding - inserted + 1;
							refresh();
							BindUtils.postNotifyChange(null, null, TokenScanBatchVm.this, "total");
							BindUtils.postNotifyChange(null, null, TokenScanBatchVm.this, "outstanding");
						} else {
							Messagebox.show("Data yang dicari tidak ditemukan atau sudah masuk kedalam daftar", "Info",
									Messagebox.OK, Messagebox.INFORMATION);
						}
					}
				}
			});

			td.appendChild(txtAwal);
			td.appendChild(label);
			td.appendChild(txtAkhir);
			td.appendChild(label2);
			td.appendChild(btn);

			tr.appendChild(td);
			table.appendChild(tr);

		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		total = 0;
		itemno = "";
		tbItem.setFocus(true);
		btnAdd.setDisabled(true);
	}

	@NotifyChange("*")
	@Command
	public void doRegisterBatch() {
		System.out.println("JUMLAH : " + dataList.size());
		if (dataList.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			Event closeEvent = new Event("onClose", winItemBatch, dataList);
			Events.postEvent(closeEvent);
		}
	}

	@ImmutableFields
	public Ttokenserial getObjForm() {
		return objForm;
	}

	public void setObjForm(Ttokenserial objForm) {
		this.objForm = objForm;
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getOutstanding() {
		return outstanding;
	}

	public void setOutstanding(int outstanding) {
		this.outstanding = outstanding;
	}

	public String getItemno() {
		return itemno;
	}

	public void setItemno(String itemno) {
		this.itemno = itemno;
	}

	public String getItemnoend() {
		return itemnoend;
	}

	public void setItemnoend(String itemnoend) {
		this.itemnoend = itemnoend;
	}

}
