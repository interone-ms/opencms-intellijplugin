package com.mediaworx.intellij.opencmsplugin.opencms;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.mediaworx.intellij.opencmsplugin.components.OpenCmsPlugin;
import com.mediaworx.intellij.opencmsplugin.configuration.ModuleExportPoint;
import com.mediaworx.intellij.opencmsplugin.configuration.module.OpenCmsModuleConfigurationData;
import com.mediaworx.intellij.opencmsplugin.entities.SyncMode;

import java.util.List;

public class OpenCmsModule {

	OpenCmsPlugin plugin;
	Module ideaModule;
	OpenCmsModuleConfigurationData moduleConfig;

	List<ModuleExportPoint> exportPoints;
	List<String> moduleResources;
	String intelliJModuleRoot;
	String localVfsRoot;
	SyncMode syncMode;

	public OpenCmsModule(OpenCmsPlugin plugin, Module ideaModule) {
		this.plugin = plugin;
		this.ideaModule = ideaModule;
	}

	public void init(OpenCmsModuleConfigurationData moduleConfig) {
		this.moduleConfig = moduleConfig;
		exportPoints = plugin.getOpenCmsConfiguration().getExportPointsForModule(moduleConfig.getModuleName());
		moduleResources = plugin.getOpenCmsConfiguration().getModuleResourcesForModule(moduleConfig.getModuleName());

		String relativeVfsRoot;

		if (moduleConfig.isUseProjectDefaultVfsRootEnabled()) {
			relativeVfsRoot = plugin.getPluginConfiguration().getDefaultLocalVfsRoot();
		}
		else {
			relativeVfsRoot = moduleConfig.getLocalVfsRoot();
		}

		VirtualFile[] moduleRoots = ModuleRootManager.getInstance(ideaModule).getContentRoots();
		if (moduleRoots.length == 0) {
			intelliJModuleRoot = null;
			localVfsRoot = null;
		}
		intelliJModuleRoot = moduleRoots[0].getPath();
		StringBuilder vfsRootBuilder = new StringBuilder();
		vfsRootBuilder.append(intelliJModuleRoot).append("/").append(relativeVfsRoot);
		localVfsRoot = vfsRootBuilder.toString();

		if (moduleConfig.isUseProjectDefaultSyncModeEnabled()) {
			syncMode = plugin.getPluginConfiguration().getDefaultSyncMode();
		}
		else {
			syncMode = moduleConfig.getSyncMode();
		}
	}

	public void refresh(OpenCmsModuleConfigurationData moduleConfig) {
		// for now a refresh just does the same as init
		init(moduleConfig);
	}

	public void refresh() {
		// for now a refresh just does the same as init
		init(moduleConfig);
	}

	public String getModuleName() {
		return moduleConfig.getModuleName();
	}

	public String getLocalVfsRoot() {
		return localVfsRoot;
	}

	public String getManifestRoot() {
		return intelliJModuleRoot + "/" + plugin.getPluginConfiguration().getManifestRoot();
	}

	public SyncMode getSyncMode() {
		return syncMode;
	}

	public List<ModuleExportPoint> getExportPoints() {
		return exportPoints;
	}

	public List<String> getModuleResources() {
		return moduleResources;
	}

	public String getVfsPathForIdeaVFile(final VirtualFile ideaVFile) {
		String filepath = ideaVFile.getPath().replace('\\', '/');
		if (!filepath.contains(localVfsRoot)) {
			return "";
		}
		String relativeName = filepath.substring(filepath.indexOf(localVfsRoot) + localVfsRoot.length(), filepath.length());
		if (relativeName.length() == 0) {
			relativeName = "/";
		}
		return relativeName;
	}
}
