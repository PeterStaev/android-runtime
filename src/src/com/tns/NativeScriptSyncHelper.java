package com.tns;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class NativeScriptSyncHelper
{
	private static String SYNC_ROOT_SOURCE_DIR = "/data/local/tmp/";
	private static final String SYNC_SOURCE_DIR = "/sync/";
	private static final String FULL_SYNC_SOURCE_DIR = "/fullsync/";
	private static final String REMOVED_SYNC_SOURCE_DIR = "/removedsync/";

	public static void sync(Context context)
	{
		if (BuildConfig.DEBUG)
		{
			String syncPath = SYNC_ROOT_SOURCE_DIR + context.getPackageName() + SYNC_SOURCE_DIR;
			String fullSyncPath = SYNC_ROOT_SOURCE_DIR + context.getPackageName() + FULL_SYNC_SOURCE_DIR;
			String removedSyncPath = SYNC_ROOT_SOURCE_DIR + context.getPackageName() + REMOVED_SYNC_SOURCE_DIR;

			if (Platform.IsLogEnabled)
			{
				Log.d(Platform.DEFAULT_LOG_TAG, "syncPath: " + syncPath);
				Log.d(Platform.DEFAULT_LOG_TAG, "fullSyncPath: " + fullSyncPath);
				Log.d(Platform.DEFAULT_LOG_TAG, "removedSyncPath: " + removedSyncPath);
			}

			File fullSyncDir = new File(fullSyncPath);
			if (fullSyncDir.exists())
			{
				executeFullSync(context, fullSyncDir);
				return;
			}

			File syncDir = new File(syncPath);
			if (syncDir.exists())
			{
				executePartialSync(context, syncDir);
			}

			File removedSyncDir = new File(removedSyncPath);
			if (removedSyncDir.exists())
			{
				executeRemovedSync(context, removedSyncDir);
			}
		}
	}
	
	final static FileFilter deletingFilesFilter = new FileFilter()
	{
		@Override
		public boolean accept(File pathname)
		{
			if (pathname.isDirectory())
			{
				return true;
			}
			
			boolean success = pathname.delete();
			if (!success)
			{
				Log.e(Platform.DEFAULT_LOG_TAG, "Syncing: file not deleted: " + pathname.getAbsolutePath().toString());
			}
			return false;
		}
	};
	
	private static void deleteDir(File directory)
	{
		File[] subDirectories = directory.listFiles(deletingFilesFilter);
		if (subDirectories != null)
		{
			for (int i = 0; i < subDirectories.length; i++)
			{
				File subDir = subDirectories[i];
				deleteDir(subDir);
			}
		}
		
		boolean success = directory.delete();
		if (!success && directory.exists())
		{
			Log.e(Platform.DEFAULT_LOG_TAG, "Syncing: directory not deleted: " + directory.getAbsolutePath().toString());
		}
	}
	
	private static void moveFiles(File sourceDir, String sourceRootAbsolutePath, String targetRootAbsolutePath)
	{
		File[] files = sourceDir.listFiles();
		
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				File file = files[i];
				if (file.isFile())
				{
					if (Platform.IsLogEnabled)
					{
						Log.d(Platform.DEFAULT_LOG_TAG, "Syncing: " + file.getAbsolutePath().toString());
					}
					
					String targetFilePath = file.getAbsolutePath().replace(sourceRootAbsolutePath, targetRootAbsolutePath);
					File targetFileDir = new File(targetFilePath);
					
					File targetParent = targetFileDir.getParentFile();
					if (targetParent != null)	
					{
						targetParent.mkdirs();
					}
					
					file.renameTo(targetFileDir);
				}
				else
				{
					moveFiles(file, sourceRootAbsolutePath, targetRootAbsolutePath);
				}
			}
		}
		
		if (sourceDir.exists())
		{
			sourceDir.delete();
		}
	}
	
	// this removes only the app directory from the device to preserve
	// any existing files in /files directory on the device
	private static void executeFullSync(Context context, final File sourceDir)
	{
		String appPath = context.getFilesDir().getAbsolutePath() + "/app";
		final File appDir = new File(appPath);
		
		if (appDir.exists())
		{
			deleteDir(appDir);
			moveFiles(sourceDir, sourceDir.getAbsolutePath(), appDir.getAbsolutePath());
		}
	}

	private static void executePartialSync(Context context, File sourceDir)
	{
		String appPath = context.getFilesDir().getAbsolutePath() + "/app";
		final File appDir = new File(appPath);
		
		if (appDir.exists())
		{
			moveFiles(sourceDir, sourceDir.getAbsolutePath(), appDir.getAbsolutePath());
		}
	}

	private static void deleteRemovedFiles(File sourceDir,  String sourceRootAbsolutePath, String targetRootAbsolutePath)
	{
		File[] files = sourceDir.listFiles();
		
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				File file = files[i];
				if (file.isFile())
				{
					if (Platform.IsLogEnabled)
					{
						Log.d(Platform.DEFAULT_LOG_TAG, "Syncing removed file: " + file.getAbsolutePath().toString());
					}
					
					String targetFilePath = file.getAbsolutePath().replace(sourceRootAbsolutePath, targetRootAbsolutePath);
					File targetFile = new File(targetFilePath);
					targetFile.delete();
				}
				else
				{
					deleteRemovedFiles(file, sourceRootAbsolutePath, targetRootAbsolutePath);
				}
			}
		}
		
		if (sourceDir.exists())
		{
			sourceDir.delete();
		}
	}
	
	private static void executeRemovedSync(final Context context, final File sourceDir)
	{
		String appPath = context.getFilesDir().getAbsolutePath() + "/app";
		deleteRemovedFiles(sourceDir, sourceDir.getAbsolutePath(), appPath);
	}
}