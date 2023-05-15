package com.sdd.caption.viewmodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

import com.sdd.caption.dao.MbranchproductgroupDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MusergroupmenuDAO;
import com.sdd.caption.dao.TnotifDAO;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergroupmenu;
import com.sdd.caption.domain.Tnotif;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.ListModelFlyweight;
import com.sdd.utils.db.StoreHibernateUtil;

public class UserInitialization3Vm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Muser oUser;
	private Mmenu mmenu;
	private MmenuDAO mmenuDao = new MmenuDAO();

	private String filter;
	private String filterNotif;
	private Integer branchlevel;

	private Map<Integer, Tnotif> mapNotif = new HashMap<Integer, Tnotif>();

	@Wire
	private Treechildren root;
	@Wire
	private Div divContent;
	@Wire
	private Combobox cbMenu;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);

		try {
			oUser = (Muser) zkSession.getAttribute("oUser");

			if (oUser != null) {
				setMenuAutocomplete();
				boolean isOpen = false;
				boolean isSubgroup = false;

				String menugroup = "";
				String menusubgroup = "";
				Treechildren treechildrenGroup = null;
				Treechildren treechildrenSub = null;

				branchlevel = oUser.getMbranch().getBranchlevel();
				// --------------------MAPPING MENU YANG TERDAFTAR DI TABLE
				// TNOTIF--------------------------------------------------------------------
				if (branchlevel == 1) {
					filter = "mbranchfk = " + oUser.getMbranch().getMbranchpk();
					List<Mbranchproductgroup> oList = new MbranchproductgroupDAO().listNativeByFilter(filter,
							"productgroupcode");
					for (Mbranchproductgroup data : oList) {
						if(data.getMbranch().getBranchid().equals("723") || data.getMbranch().getBranchid().equals("760") || data.getMbranch().getBranchid().equals("752")) {
							filterNotif = "tnotif.branchlevel = " + branchlevel + " and productgroup = '"
								+ data.getMproductgroup().getProductgroupcode() + "'";
						} else {
							filterNotif = "tnotif.branchlevel = " + branchlevel + " and productgroup = '"
									+ data.getMproductgroup().getProductgroupcode() + "' and mbranchfk = " + oUser.getMbranch().getMbranchpk();
						}
						List<Tnotif> notifList = new TnotifDAO().listNativeByFilter(filterNotif, "mmenufk");
						if (notifList.size() > 0) {
							for (Tnotif notif : notifList) {
								if (mapNotif.get(notif.getMmenu().getMmenupk()) != null) {
									notif.setNotifcount(mapNotif.get(notif.getMmenu().getMmenupk()).getNotifcount()
											+ notif.getNotifcount());
								}
								mapNotif.put(notif.getMmenu().getMmenupk(), notif);
							}
						}
					}
				} else {
					filterNotif = "tnotif.branchlevel = " + branchlevel;
					if (branchlevel == 2)
						filterNotif += " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
					else if (branchlevel == 3)
						filterNotif += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();

					List<Tnotif> notifList = new TnotifDAO().listNativeByFilter(filterNotif, "mmenufk");
					if (notifList.size() > 0) {
						for (Tnotif notif : notifList) {
							if (mapNotif.get(notif.getMmenu().getMmenupk()) != null) {
								notif.setNotifcount(mapNotif.get(notif.getMmenu().getMmenupk()).getNotifcount()
										+ notif.getNotifcount());
							}
							mapNotif.put(notif.getMmenu().getMmenupk(), notif);
						}
					}
				}
				// ------------------------------------------------------------------------------------------------

				List<Musergroupmenu> oList = new MusergroupmenuDAO().listByFilter(
						"musergroup.musergrouppk = " + oUser.getMusergroup().getMusergrouppk(),
						"mmenu.menuorderno, mmenu.menuname");

				// -------------RENDER UNTUK GROUPING TOTAL NOTIF PADA MENU YANG MEMPUNYAI
				// NOTIF------------
				int notifgroup = 0;
				int notifsubgroup = 0;
				int notifname = 0;
				Map<String, Integer> mapTotalnotif = new HashMap<String, Integer>();
				for (final Musergroupmenu obj : oList) {
					if (mapNotif.get(obj.getMmenu().getMmenupk()) != null) {
						if (mapTotalnotif.get(obj.getMmenu().getMenugroup().trim()) != null) {
							notifgroup += mapNotif.get(obj.getMmenu().getMmenupk()).getNotifcount();
						} else {
							notifgroup = mapNotif.get(obj.getMmenu().getMmenupk()).getNotifcount();
						}
						mapTotalnotif.put(obj.getMmenu().getMenugroup().trim(), notifgroup);

						if (!obj.getMmenu().getMenugroup().trim().equals(obj.getMmenu().getMenusubgroup().trim())) {
							if (mapTotalnotif.get(obj.getMmenu().getMenusubgroup().trim()) != null) {
								notifsubgroup += mapNotif.get(obj.getMmenu().getMmenupk()).getNotifcount();
							} else {
								notifsubgroup = mapNotif.get(obj.getMmenu().getMmenupk()).getNotifcount();
							}
							mapTotalnotif.put(obj.getMmenu().getMenusubgroup().trim(), notifsubgroup);
						}

						notifname = mapNotif.get(obj.getMmenu().getMmenupk()).getNotifcount();
						mapTotalnotif.put(obj.getMmenu().getMenuname().trim(), notifname);

					}
				}
				// -----------------------------------------------------------------------------------------

				for (final Musergroupmenu obj : oList) {
					if (!menugroup.equals(obj.getMmenu().getMenugroup())) {
						menugroup = obj.getMmenu().getMenugroup();

						Treeitem treeitem = new Treeitem();
						treeitem.setOpen(isOpen);
						root.appendChild(treeitem);

						Treerow treerow = new Treerow();
						Treecell treecell = new Treecell();

						Hlayout hlayout = new Hlayout();
						hlayout.setStyle("display: flex;");
						hlayout.setHflex("1");
						Label menugrouplabel = new Label(" " + menugroup);
						menugrouplabel.setStyle("font-size:16px");

						if (obj.getMmenu().getMenugroupicon() != null && obj.getMmenu().getMenugroupicon() != null
								&& obj.getMmenu().getMenugroupicon().trim().length() > 0) {
							Image menugroupicon = new Image();
							menugroupicon.setSrc(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/"
									+ obj.getMmenu().getMenugroupicon());
							menugroupicon.setWidth("24px");
							menugroupicon.setHeight("24px");

							hlayout.appendChild(menugroupicon);
						}

						// ----------CEK APAKAH MENU GROUP INI MEMPUNYAI TOTAL
						// NOTIF-------------------------------
						if (mapTotalnotif.get(obj.getMmenu().getMenugroup().trim()) != null) {
							menugrouplabel.setStyle("font-size:16px;font-weight: bold");

							Div div = new Div();
							div.setWidth("20px");
							div.setHeight("20px");
							div.setStyle(
									"background:red; border-radius:100%;text-align: center;justify-content: center;align-items: center;display: flex;");
							Label notiflabel = new Label(" " + mapTotalnotif.get(obj.getMmenu().getMenugroup().trim()));
							notiflabel.setStyle("font-weight: bold; color:white");
							div.appendChild(notiflabel);

							hlayout.appendChild(menugrouplabel);
							hlayout.appendChild(div);
							treecell.appendChild(hlayout);
						} else {
							hlayout.appendChild(menugrouplabel);
							treecell.appendChild(hlayout);
						}
						// ---------------------------------------------------------------------------------------

						treerow.appendChild(treecell);
						treeitem.appendChild(treerow);

						treechildrenGroup = new Treechildren();
						treeitem.appendChild(treechildrenGroup);
					}

					if (!obj.getMmenu().getMenugroup().equals(obj.getMmenu().getMenusubgroup())) {
						if (!menusubgroup.equals(obj.getMmenu().getMenusubgroup())) {
							menusubgroup = obj.getMmenu().getMenusubgroup();
							isSubgroup = true;

							Treeitem treeitem = new Treeitem();
							treeitem.setOpen(isOpen);
							treechildrenGroup.appendChild(treeitem);

							Treerow treerow = new Treerow();
							Treecell treecell = new Treecell();

							Hlayout hlayout = new Hlayout();
							hlayout.setStyle("display: flex;");
							hlayout.setHflex("1");
							Label menusubgrouplabel = new Label(menusubgroup);
							menusubgrouplabel.setStyle("font-size:16px");

							if (obj.getMmenu().getMenusubgroupicon() != null
									&& obj.getMmenu().getMenusubgroupicon().trim().length() > 0) {
								Image menusubgroupicon = new Image();
								menusubgroupicon.setSrc(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/"
										+ obj.getMmenu().getMenusubgroupicon());
								menusubgroupicon.setWidth("24px");
								menusubgroupicon.setHeight("24px");

								hlayout.appendChild(menusubgroupicon);
							}

							// ----------CEK APAKAH MENU SUB GROUP INI MEMPUNYAI TOTAL
							// NOTIF---------------------------
							if (mapTotalnotif.get(obj.getMmenu().getMenusubgroup().trim()) != null) {
								menusubgrouplabel.setStyle("font-size:16px;font-weight: bold");

								Div div = new Div();
								div.setWidth("19px");
								div.setHeight("19px");
								div.setStyle(
										"background:red; border-radius:100%;text-align: center;justify-content: center;align-items: center;display: flex;");
								Label notiflabel = new Label(
										" " + mapTotalnotif.get(obj.getMmenu().getMenusubgroup().trim()));
								notiflabel.setStyle("font-weight: bold; color:white");
								div.appendChild(notiflabel);
								hlayout.appendChild(menusubgrouplabel);
								hlayout.appendChild(div);

								treecell.appendChild(hlayout);
							} else {
								hlayout.appendChild(menusubgrouplabel);
								treecell.appendChild(hlayout);
							}
							// -------------------------------------------------------------------------------------

							treerow.appendChild(treecell);
							treeitem.appendChild(treerow);

							treechildrenSub = new Treechildren();
							treeitem.appendChild(treechildrenSub);
						}
					} else
						isSubgroup = false;

					Treeitem treeitem = new Treeitem();
					treeitem.setOpen(isOpen);
					if (isSubgroup)
						treechildrenSub.appendChild(treeitem);
					else
						treechildrenGroup.appendChild(treeitem);

					Treerow treerow = new Treerow();
					Treecell treecell = new Treecell();

					Hlayout hlayout = new Hlayout();
					hlayout.setStyle("display: flex;");
					hlayout.setHflex("1");
					Label menulabel = new Label(obj.getMmenu().getMenuname().trim());
					menulabel.setStyle("font-size:16px");

					if (obj.getMmenu().getMenuicon() != null && obj.getMmenu().getMenuicon().trim().length() > 0) {

						Image menuicon = new Image();
						menuicon.setSrc(
								AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + obj.getMmenu().getMenuicon());
						menuicon.setWidth("24px");
						menuicon.setHeight("24px");

						hlayout.appendChild(menuicon);
					}
					// ----------CEK APAKAH MENU NAME INI MEMPUNYAI TOTAL
					// NOTIF-------------------------------
					if (mapTotalnotif.get(obj.getMmenu().getMenuname().trim()) != null) {
						menulabel.setStyle("font-size:16px;font-weight: bold");

						Div div = new Div();
						div.setWidth("19px");
						div.setHeight("19px");
						div.setStyle(
								"background:red; border-radius:100%;text-align: center;justify-content: center;align-items: center;display: flex;");
						Label notiflabel = new Label(" " + mapTotalnotif.get(obj.getMmenu().getMenuname().trim()));
						notiflabel.setStyle("font-weight: bold; color:white");
						div.appendChild(notiflabel);
						hlayout.appendChild(menulabel);
						hlayout.appendChild(div);
						treecell.appendChild(hlayout);
					} else {
						hlayout.appendChild(menulabel);
						treecell.appendChild(hlayout);
					}

					treecell.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							divContent.getChildren().clear();

							System.out.println("MMENU : " + obj.getMmenu().getMmenupk() + ", MBRANCH : " + oUser.getMbranch().getMbranchpk()
									+ ", LEVEL : " + oUser.getMbranch().getBranchlevel());

							// ------------------ CEK MENU MEMILIKI NOTIF APA TIDAK
							// ---------------------------------
							// (SCRIPT CEK NOTIF)
							// -------------------------------------------------------------------------------------
							Map<String, Object> map = new HashMap<String, Object>();
							map.put(obj.getMmenu().getMenuparamname().trim(),
									obj.getMmenu().getMenuparamvalue().trim());
							map.put("content", divContent);
							Executions.createComponents(obj.getMmenu().getMenupath().trim(), divContent, map);
						}
					});

					treerow.appendChild(treecell);
					treeitem.appendChild(treerow);
				}

				Executions.createComponents("/view/welcome.zul", divContent, null);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doRedirect(@BindingParam("item") Mmenu obj) {
		if (obj != null) {
			try {
				divContent.getChildren().clear();
				Map<String, String> map = new HashMap<String, String>();
				map.put(obj.getMenuparamname(), obj.getMenuparamvalue());

				map.put("a", null);
				map.put("b", null);

				Executions.createComponents(obj.getMenupath(), divContent, map);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public void setMenuAutocomplete() {
		try {
			List<Musergroupmenu> oList = new MusergroupmenuDAO().listByFilter(
					"musergroup.musergrouppk = " + oUser.getMusergroup().getMusergrouppk(), "mmenu.menuorderno");
			cbMenu.setModel(new SimpleListModel(oList) {
				public ListModel getSubModel(Object value, int nRows) {
					if (value != null && value.toString().trim().length() > AppUtils.AUTOCOMPLETE_MINLENGTH) {
						String nameStartsWith = value.toString().trim().toUpperCase();
						List data = mmenuDao.startsWith(AppUtils.AUTOCOMPLETE_MAXROWS, nameStartsWith);
						return ListModelFlyweight.create(data, nameStartsWith, "menuname");
					}
					return ListModelFlyweight.create(Collections.emptyList(), "", "menuname");
				}
			});

			cbMenu.setItemRenderer(new ComboitemRenderer<Mmenu>() {
				@Override
				public void render(Comboitem item, Mmenu data, int index) throws Exception {
					item.setLabel(data.getMenuname());
					// item.setDescription(data.getProducttype());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Mmenu getMmenu() {
		return mmenu;
	}

	public void setMmenu(Mmenu mmenu) {
		this.mmenu = mmenu;
	}

}
