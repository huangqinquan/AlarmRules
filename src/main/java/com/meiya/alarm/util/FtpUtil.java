package com.meiya.alarm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class FtpUtil {
	private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);

	private static FTPClient ftp;

	public static void main(String[] args) {
		FTPFileFilter ftpFileFilter = new FTPFileFilter() {
			@Override
			public boolean accept(FTPFile file) {
				if (StringUtils.endsWithIgnoreCase(file.getName(), "zip"))
					return false;
				return true;
			}
		};
		FtpUtil.download("15.8.65.67", 21, 0, "dragon", "meiya@300188", "/YQYQYQ/", new File(FtpUtil.class.getResource("").getPath()), ftpFileFilter,
				1000, true);
	}

	public static void upload(String host, int port, int mode, String username, String password, String savePath, List<File> fileList)
			throws SocketException, IOException {
		if (ftp == null){
			logger.info("初次实例化ftp单例");
			ftp = getFTPClient(host, port, mode, username, password, savePath);
		}
		InputStream inputStream = null;
		try {
			if (ftp.makeDirectory(savePath))
				logger.info("创建目录：{}", savePath);
			ftp.changeWorkingDirectory(savePath);
			for (File file : fileList) {
				String tempFileName = file.getName() + "_" + UUID.randomUUID() + ".tmp";
				logger.info("准备上传文件{}到：{}", file.getPath(), savePath);
				inputStream = new FileInputStream(file);
				if (ftp.storeFile(tempFileName, inputStream)) {
					logger.info("上传文件成功：{}", tempFileName);
					ftp.rename(tempFileName, file.getName());// 重命名
					logger.info("修改文件名为{}", file.getName());
					inputStream.close();
					try {
						Files.delete(file.toPath());
						logger.info("删除文件成功");
					} catch (Throwable e) {
						logger.info("删除文件失败");
						logger.error("{}异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e);
					}
				} else {
					logger.info("上传文件失败：{}", tempFileName);
					inputStream.close();
				}
			}
		} catch (Throwable e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + "异常", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable e) {
					logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + "异常", e);
				}
			}
			try {
				ftp.logout();
			} catch (Throwable e2) {
				logger.error("{}异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e2);
			}
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (Throwable e) {
					logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + "异常", e);
				}
			}
		}
	}


	public static void upload(String host, int port, int mode, String username, String password, String savePath, File file)
			throws SocketException, IOException {
//		if (ftp == null){
//			logger.info("初次实例化ftp单例");
//		}
		ftp = getFTPClient(host, port, mode, username, password, savePath);
		InputStream inputStream = null;
		try {
			if (ftp.makeDirectory(savePath))
				logger.info("创建目录：{}", savePath);
			ftp.changeWorkingDirectory(savePath);

			String tempFileName = file.getName() + "_" + UUID.randomUUID() + ".tmp";
			logger.info("准备上传文件{}到：{}", file.getPath(), savePath);
			inputStream = new FileInputStream(file);
			if (ftp.storeFile(tempFileName, inputStream)) {
				logger.info("上传文件成功：{}", tempFileName);
				ftp.rename(tempFileName, file.getName());// 重命名
				logger.info("修改文件名为{}", file.getName());
				inputStream.close();
				try {
					Files.delete(file.toPath());
					logger.info("删除文件成功");
				} catch (Throwable e) {
					logger.info("删除文件失败");
					logger.error("{}异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e);
				}
			} else {
				logger.info("上传文件失败：{}", tempFileName);
				inputStream.close();
			}

		} catch (Throwable e) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + "异常", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable e) {
					logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + "异常", e);
				}
			}
			try {
				ftp.logout();
			} catch (Throwable e2) {
				logger.error("{}异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e2);
			}
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (Throwable e) {
					logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + "异常", e);
				}
			}
		}
	}

	public static FTPClient getFTPClient(String host, int port, int mode, String username, String password, String workingDirectory)
			throws SocketException, IOException {
		FTPClient ftp = new FTPClient();
		logger.info("FTP开始连接{}：{}", host, port);
		ftp.connect(host, port);
		ftp.setControlEncoding("GBK");// 防止损坏文件
		if (mode == FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
			ftp.enterLocalActiveMode();
			logger.info("主动模式");
		} else if (mode == FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
			ftp.enterLocalPassiveMode();
			logger.info("被动模式");
		}
		// ftp.enterLocalPassiveMode();//可能读不到文件
		ftp.login(username, password);
		ftp.setFileType(FTP.BINARY_FILE_TYPE);
		if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
			logger.info("FTP连接{}：{}失败", host, port);
			ftp.disconnect();
			ftp = null;
			return ftp;
		}

		logger.info("FTP连接到{}：{}成功", host, port);
		if (!ftp.makeDirectory(workingDirectory)) {
			logger.info("创建目录{}失败", workingDirectory);
		}
		if (!ftp.changeWorkingDirectory(workingDirectory)) {
			logger.info("改变工作路径失败：{}", workingDirectory);
		} else {
			logger.info("改变工作路径成功：{}", workingDirectory);
		}
		return ftp;
	}

	private static void download(FTPClient ftpClient, FTPFileFilter ftpFileFilter, int maxDownloadNum, File savePath, boolean deleteFile) {
		FTPListParseEngine engine;
		try {
			engine = ftpClient.initiateListParsing();
		} catch (Throwable e) {
			logger.error("初始化FTP引擎异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e);
			return;
		}
		while (engine.hasNext()) {
			FTPFile[] ftpFiles = engine.getNext(maxDownloadNum);
			try {
				logger.info("{}目录发现{}个文件和目录", ftpClient.printWorkingDirectory(), CollectionUtils.size(ftpFiles));
			} catch (Throwable e) {
				logger.error("{}异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e);
				return;
			}
			for (FTPFile ftpFile : ftpFiles) {
				try {
					if (!ftpFileFilter.accept(ftpFile))
						continue;
					String path = ftpClient.printWorkingDirectory() + "/" + ftpFile.getName();
					if (ftpFile.isDirectory()) {
						ftpClient.changeWorkingDirectory(ftpFile.getName());
						download(ftpClient, ftpFileFilter, maxDownloadNum, savePath, deleteFile);
						ftpClient.changeToParentDirectory();
					} else {
						logger.info("准备下载文件：{}", path);
						File file = null;
						file = new File(savePath, ftpFile.getName());
						File tempfile = new File(file.getPath() + ".tmp");
						logger.info("准备保存路径：{}", tempfile.getPath());
						if (file.exists() && file.length() > 0l) {
							logger.info("下载失败，文件已经存在：{}", file.getName());
						} else {
							if (file.exists()) {
								file.delete();
								logger.info("删除原来的空文件：{}", file.getPath());
							}
							tempfile.createNewFile();
							OutputStream outputStream = new FileOutputStream(tempfile);
							ftpClient.retrieveFile(ftpClient.printWorkingDirectory() + "/" + ftpFile.getName(), outputStream);
							outputStream.close();
							logger.info("下载成功：{}", tempfile.getName());
							boolean rename = tempfile.renameTo(file);
							if (rename) {
								logger.info("修改文件名成功：{}", file.getName());
							} else {
								logger.info("修改文件名失败：{}", file.getName());
							}
						}
						if (deleteFile) {
							if (ftpClient.deleteFile(path))
								logger.info("删除Ftp上文件成功：{}", path);
							else {
								logger.info("删除Ftp上文件失败：{}", path);
							}
						}
					}
				} catch (Throwable e) {
					logger.error("{}异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e);
				}
			}
		}
	}

	public static void download(String host, int port, int mode, String username, String password, String workingDirectory, File savePath,
			FTPFileFilter ftpFileFilter, int maxDownloadNum, boolean deleteFile) {
		FTPClient ftp = null;
		try {
			ftp = getFTPClient(host, port, mode, username, password, workingDirectory);
			if (ftp == null)
				return;
			download(ftp, ftpFileFilter, maxDownloadNum, savePath, deleteFile);
		} catch (Exception e) {
			logger.error("{}异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e);
		} finally {
			if (ftp != null) {
				try {
					ftp.logout();
				} catch (Throwable e2) {
					logger.error("{}异常：{}", Thread.currentThread().getStackTrace()[1].getMethodName(), e2);
				}
				if (ftp.isConnected()) {
					try {
						ftp.disconnect();
					} catch (IOException ioe) {
						logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() + "异常", ioe);
					}
				}
			}
		}
	}
}
