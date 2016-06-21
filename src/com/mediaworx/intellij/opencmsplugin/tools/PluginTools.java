/*
 * This file is part of the OpenCms plugin for IntelliJ by mediaworx.
 *
 * For further information about the OpenCms plugin for IntelliJ, please
 * see the project website at GitHub:
 * https://github.com/mediaworx/opencms-intellijplugin
 *
 * Copyright (C) 2007-2014 mediaworx berlin AG (http://www.mediaworx.com)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.mediaworx.intellij.opencmsplugin.tools;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.mediaworx.intellij.opencmsplugin.opencms.OpenCmsModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PluginTools {

	public static String getModuleContentRoot(Module module) {
		String moduleContentRoot = null;
		if (module != null) {
			VirtualFile[] moduleRoots = ModuleRootManager.getInstance(module).getContentRoots();
			if (moduleRoots.length == 0) {
				return null;
			}
			// we assume that for OpenCms modules there is only one content root
			moduleContentRoot = moduleRoots[0].getPath();
		}
		return moduleContentRoot;
	}


	public static List<File> getRealFilesFromVirtualFiles(VirtualFile[] virtualFiles) {
		List<File> realFiles = new ArrayList<>(virtualFiles.length);
		for (VirtualFile virtualFile : virtualFiles) {
			realFiles.add(new File(virtualFile.getPath()));
		}
		return realFiles;
	}
	
	public static String ensureUnixPath(String path) {
		return path.replaceAll("\\\\", "/");
	}
	
	public static String ensureUnixNewline(String in) {
		return in.replaceAll("\\r\\n|\\r", "\n");
	}

	public static String stripVfsSiteRootFromVfsPath(OpenCmsModule module, String vfsPath) {
		String localPath;
		if ("/".equals(module.getExportImportSiteRoot())) {
			localPath = vfsPath;
		}
		else {
			localPath = vfsPath.replaceFirst("^" + module.getExportImportSiteRoot(), "");
		}
		return localPath;
	}

	public static String addVfsSiteRootToLocalPath(OpenCmsModule module, String localPath) {
		String vfsRootPath = localPath;
		if (!"/".equals(module.getExportImportSiteRoot())) {
			vfsRootPath = module.getExportImportSiteRoot() + vfsRootPath;
		}
		return vfsRootPath;

	}
}
