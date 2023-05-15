package com.sdd.caption.viewmodel;

import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.utils.AppData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;

public class ReportOrderDataVm {
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private Date startdate;
	private Date finishdate;
	private Map<String, Mbranch> mapBranch = new HashMap<>();
	private List<File> embossList = new ArrayList<File>();

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);

		try {
			List<Mbranch> branchList = AppData.getMbranch();
			for (Mbranch data : branchList) {
				mapBranch.put(data.getBranchid(), data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Command
	@NotifyChange("*")
	public void doDownload() {
		try {
			Calendar tanggal = (Calendar) startdate.clone();
			while (tanggal.before(finishdate)) {
				daysBetween(tanggal);
				tanggal.add(Calendar.DAY_OF_MONTH, 1);
			}
			daysBetween(tanggal);
			doProcess();
		} catch (Exception var29) {
			var29.printStackTrace();
		}
	}
	
	@NotifyChange("*")
	public void doProcess() {
		try {
			String pathCreate = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath("/files/report/RPT_ORDERDATA_" + dateFormatter.format(new Date()));
			
			FileWriter file = new FileWriter(pathCreate);
			BufferedWriter bufferedWriter = new BufferedWriter(file);
			
			bufferedWriter.write(
					"No Kartu;Nama;Add1;Add2;Add3;Kode Pos;BIN;Tgl Data;Type Kartu;Kode Cabang;Nama Cabang;Kode KLN;Kolam Khusus;Acc NO;Seqnum");
			bufferedWriter.newLine();
			String cardno = "";
			String cardname = "";
			String add1 = "";
			String add2 = "";
			String add3 = "";
			String codepos = "";
			String bin = "";
			String tgldata = "";
			String productcode = "";
			String branchcode = "";
			String branchname = "";
			String klncode = "";
			String accno = "";
			String seqnum = "";
			String kolamkhusus = "";
			
			for(File emboss : embossList) {
				BufferedReader reader = new BufferedReader(new FileReader(emboss));
				String line = "";

				while ((line = reader.readLine()) != null) {
					try {
						cardno = line.substring(1, 20).trim();
						cardname = line.substring(52, 82).trim();
						add1 = line.substring(502, 535).trim();
						add1 = add1.replace(";", "");
						add2 = line.substring(536, 569).trim();
						add3 = line.substring(570, 591).trim();
						codepos = line.substring(592, 597).trim();
						bin = cardno.substring(0, 6);
						tgldata = line.substring(652, 658);
						productcode = line.substring(670, 674).trim();
						branchcode = line.substring(701, 704).trim();
						Mbranch mbranch = mapBranch.get(branchcode);
						if (mbranch != null) {
							branchname = mbranch.getBranchname();
						} else {
							branchname = "Cabang belum terdaftar diparameter";
						}

						klncode = line.substring(705, 707);
						kolamkhusus = line.substring(707, 742).trim();
						if (line.length() > 762) {
							accno = line.substring(742, 759).trim();
							seqnum = line.substring(759, 765);
						} else {
							seqnum = line.substring(755, 761);
						}
						bufferedWriter.write(cardno + ";" + cardname + ";" + add1 + ";" + add2 + ";" + add3
								+ ";" + codepos + ";" + bin + ";" + tgldata + ";" + productcode + ";"
								+ branchcode + ";" + branchname + ";" + klncode + ";" + kolamkhusus + ";"
								+ accno + ";" + seqnum + ";");
						bufferedWriter.newLine();
					} catch (Exception var28) {
						var28.printStackTrace();
					}
				}

				reader.close();
			}
			
			bufferedWriter.close();
			Filedownload.save(new File(pathCreate), "text/plain");
			Clients.showNotification("File berhasil di download", "info", (Component) null, "middle_center",
					3000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@NotifyChange("*")
	public void daysBetween(Calendar tanggal) {
		try {
			String pathEmboss = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath("/files/emboss/" + dateFormatter.format(tanggal));
			File[] files = (new File(pathEmboss + "/")).listFiles();
			if (files != null) {
				File[] fileList = files;
				for (int i = 0; i < files.length; i++) {
					File emboss = fileList[i];
					embossList.add(emboss);
					System.out.println(emboss.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}