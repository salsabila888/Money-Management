package com.sdd.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.sdd.caption.utils.AppUtils;

public class FTPController {
	
	private String host;
	private int port;
	private String username;
	private String password;
	private String filesrc;
	private String filedest;
	
	public FTPController(String host, int port, String username, String password, String filesrc, String filedest) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.filesrc = filesrc;
		this.filedest = filedest;
	}
	
	public String upload() throws Exception {
		String msg = AppUtils.STATUS_CEMTEXT_UPLOADED;
		FTPClient ftp = new FTPClient();
		InetAddress addr = null;

		try {
			addr = InetAddress.getByName(host);	
		} catch (UnknownHostException ex) {
			msg = "Host unknown.";
			System.err.println(msg);
		}
		
		try {
			int reply;

			ftp.connect(addr);
			System.out.println("Connected to " + addr + ".");
			System.out.print(ftp.getReplyString());

			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				msg = "FTP server refused connection.";
				System.err.println(msg);
			}

			if (!ftp.login(username, password)) {
				System.out.print(ftp.getReplyString());
				ftp.logout();
			} else {
				System.out.print(ftp.getReplyString());

				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				System.out.print(ftp.getReplyString());

				ftp.enterLocalPassiveMode();
				System.out.print("Enter local passive mode.\n");

				/*
				 * FTPFile[] files = ftp.listFiles("/"); System.out.print(ftp.getReplyString());
				 * for (FTPFile ftpFile : files) { System.out.println(ftpFile.getName() +
				 * " \t\t# " + ftpFile.getType() + " \t# " + ftpFile.getUser()); }
				 */

				System.out.println("CEMTEXT PATH : " + filedest);
				boolean cd = ftp.changeWorkingDirectory(filedest);
				System.out.print(ftp.getReplyString());

				if (!cd) {
					boolean mkdir = ftp.makeDirectory(filedest);
					System.out.print(ftp.getReplyString());
					if (!mkdir) {
						msg = "Cannot create upload directory: "
								+ filedest + " at server: " + addr;
						System.err.println(msg);						
					}
					cd = ftp.changeWorkingDirectory(filedest);
				}
				
				File uploadFile = new File(filesrc);
				if (uploadFile.exists()) {
					System.out.println("Uploading file... "
							+ uploadFile.getName());
					BufferedInputStream is = new BufferedInputStream(new FileInputStream(uploadFile));
					ftp.storeFile(uploadFile.getName(), is);
					System.out.print(ftp.getReplyString());
				} else {
					msg = "Not uploading file, because it doesn't exists.";
					System.out.println(msg);
				}

				ftp.logout();
				System.out.print(ftp.getReplyString());
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
					System.out.print("Disconnected from server: " + addr);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			msg = "Couldn't connect to server: " + addr;
			System.err.println(msg);
			ex.printStackTrace();			
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
					System.out.print("Disconnected from server: " + addr);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return msg;
	}

}